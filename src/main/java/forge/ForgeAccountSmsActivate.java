package forge;

import bo.*;
import da.DbConnection;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import service.smsActivation.OnlineSim;
import service.smsActivation.SmsActivate;
import util.ExecuteService;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static util.IConstants.YOUTUBE_ACCS_PASSWORD;
import static util.SeleniumUtils.*;
import static util.SeleniumUtils.waitUntilElementWillBePresent;

public class ForgeAccountSmsActivate {
    private static final String BASE_URL = "http://local.adspower.com:50325/";

    public static void main(String[] args) throws Exception {
        //System.setProperty("webdriver.chrome.driver", "C:\\Chrom\\chromedriver");
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        final Robot robot = new Robot();
        final DbConnection dbConnection = DbConnection.getInstance();
        final RestTemplate localRestTemplate = new RestTemplate();
        final SmsActivate smsActivate = SmsActivate.getInstance();
        final ExecuteService executeService = ExecuteService.getInstance();
        for (final ForgeDetails forgeDetail : readForgeDetails()) {
//            if (new Date().getHours() < 12) {
//                System.out.println(new Date() + " it's night - going to sleep >_<");
//                Thread.sleep(3600000 * (12 - new Date().getHours()));
//                continue;
//            }
            final String sex = forgeDetail.getSex();
            final String facebookLink = forgeDetail.getFacebookUrl();
            final String proxyUrl = forgeDetail.getProxy();
            final String proxyPort = forgeDetail.getProxyPort();
            final String proxyPassword = forgeDetail.getProxyPassword();
            final String proxyUsername = forgeDetail.getProxyUsername();
            final String vkPassword = generateRandomSpecialCharacters(getRandomNumber(8, 11)).replace("\\", "") + getRandomNumber(0, 15) + "qA";
            final String browserProfile = forgeDetail.getBrowserProfile();
            System.out.println(browserProfile);
            System.out.println(vkPassword);
            System.out.println(proxyUrl);
            System.out.println(proxyPort);
            System.out.println(proxyUsername);
            System.out.println(proxyPassword);
//            if (!browserProfile.equals("jb3iked")) {
//                continue;
//            }
            if (dbConnection.isSubNetBlacklisted(proxyUrl)) {
                System.out.println(proxyUrl + " - sub net is banned!!!");
                continue;
            }
            int amountOfAttemptsForRegistration = dbConnection.getAmountOfAttemptsForRegistration(proxyUrl);
            System.out.println("Amount of attempts to registrate profile: " + amountOfAttemptsForRegistration);
            if (amountOfAttemptsForRegistration >= 2) {
                //    removeLine(browserProfile, new File("forge/proxies.txt"));
                continue;
            }
            final VkAccount vkAccount = new VkAccount();
            vkAccount.setPassword(vkPassword);
            vkAccount.setProxy(proxyUrl);
            vkAccount.setPort(proxyPort);
            vkAccount.setProxyUsername(proxyUsername);
            vkAccount.setProxyPassword(proxyPassword);
            vkAccount.setBrowserProfile(browserProfile);
            final String accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=" + browserProfile + "&ip_tab=0&headless=0&open_tabs=1", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();

            String name = null;
            final java.util.List<String> photos = new ArrayList<>();
            try {
                String facebookProfileName = facebookLink.split("/")[3];
                if (facebookProfileName.contains("?")) {
                    facebookProfileName = facebookProfileName.split("id=")[1];
                }
                final String facebookPageHtml = getFacebookHtml(facebookLink);
                String mainPhotoTag;
                String mainPhotoLink;
                //               try {
//                    final Matcher nameMatcher = Pattern.compile("<h1 class=\"(.*?)\">(.*?)</h1>", Pattern.DOTALL).matcher(facebookPageHtml);
//                    nameMatcher.find();
//                    name = nameMatcher.group(2);

                try {
                    final Matcher nameMatcher = Pattern.compile("<svg aria-label=\"(.*?)\"", Pattern.DOTALL).matcher(facebookPageHtml);
                    nameMatcher.find();
                    name = nameMatcher.group(1);
                    while ("Your profile".equals(name)) {
                        nameMatcher.find();
                        name = nameMatcher.group(1);
                    }
                    final Matcher mainPhotoMatcher = Pattern.compile("<a aria-label=\"" + name + "\"(.*?)>").matcher(facebookPageHtml);
                    mainPhotoMatcher.find();
                    mainPhotoMatcher.find();
                    mainPhotoTag = mainPhotoMatcher.group();
                    mainPhotoLink = mainPhotoTag.split("href=\"")[1].split("\"")[0];
                } catch (Exception e) {
                    final Matcher nameMatcher = Pattern.compile("<h1(.*?)>(.*?)</h1>", Pattern.DOTALL).matcher(facebookPageHtml);
                    nameMatcher.find();
                    name = nameMatcher.group(2);
                    final Matcher mainPhotoMatcher = Pattern.compile("<a aria-label=\"" + name + "\"(.*?)>").matcher(facebookPageHtml);
                    mainPhotoMatcher.find();
                    mainPhotoTag = mainPhotoMatcher.group();
                    mainPhotoLink = mainPhotoTag.split("href=\"")[1].split("\"")[0];
                }
                photos.add(downloadFacebookPhoto(mainPhotoLink, 0, facebookProfileName));
//                } catch (Exception e) {
//                    final Matcher mainPhotoMatcher = Pattern.compile("<image(.*?)xlink:href=\"(.*?)\"", Pattern.DOTALL).matcher(facebookPageHtml);
//                    mainPhotoMatcher.find();
//                    mainPhotoLink = mainPhotoMatcher.group(2).replaceAll("&amp;", "&");
//                    photos.add(downloadFile(mainPhotoLink, 0, facebookProfileName));
//                }
                int botImageIndex = 1;
                final Matcher albumMatcher = Pattern.compile("<a aria-hidden=\"true\" class=\"(.*?)\" href=\"/photo(.*?)\">", Pattern.DOTALL).matcher(facebookPageHtml);
                while (albumMatcher.find()) {
                    final String facebookPhotoParams = albumMatcher.group(2).split("\"")[0];
                    final String facebookPhotoLink = "https://www.facebook.com/photo" + facebookPhotoParams.replaceAll("&amp;", "&");
                    botImageIndex++;
                    photos.add(downloadFacebookPhoto(facebookPhotoLink, botImageIndex, facebookProfileName));
                }
            } catch (Exception e) {
                System.out.println(forgeDetail.toString());
                e.printStackTrace();
                continue;
            }
            final JSONObject accParamsJson = new JSONObject(accParams);
            final JSONObject seleniumConnectionParams = accParamsJson.getJSONObject("data");
            System.setProperty("webdriver.chrome.driver", seleniumConnectionParams.getString("webdriver")); // Set the webdriver returned by the API

            ChromeDriver driver;
//            try {
//                final ChromeOptions options = new ChromeOptions();
//                //  options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
//                options.setExperimentalOption("debuggerAddress", seleniumConnectionParams.getJSONObject("ws").getString("selenium"));
//                final LoggingPreferences logPrefs = new LoggingPreferences();
//                logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
//                options.setCapability("goog:loggingPrefs", logPrefs);
//                driver = new ChromeDriver(options);
//            } catch (Exception e) {
            final ChromeOptions options = new ChromeOptions();
            //  options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
            options.setExperimentalOption("debuggerAddress", seleniumConnectionParams.getJSONObject("ws").getString("selenium"));
            final LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
            options.setCapability("goog:loggingPrefs", logPrefs);
            options.addArguments("--remote-allow-origins=*");
            driver = new ChromeDriver(options);
            // }

            driver.get("https://vk.com/");
            boolean alreadyRegistrated = false;
            try {
                waitUntilElementWillBePresent(driver, "a[contains(@onclick, 'changeLang')][text()='Русский']").click();
                waitUntilElementWillBePresent(driver, "button[@class='FlatButton FlatButton--positive FlatButton--size-l FlatButton--wide VkIdForm__button VkIdForm__signUpButton']").click();
            } catch (Exception e) {
                try {
                    waitUntilElementWillBePresent(driver, "a[@id='top_profile_link']");
                    alreadyRegistrated = true;
                } catch (Exception ex) {
                    for (final String image : photos) {
                        Files.delete(Paths.get(image));
                    }
                    System.out.println("Proxy is broken: " + browserProfile);
                    continue;
                }
            }

            final String email;
            String[] enVkName = null;
            if (!alreadyRegistrated) {
                SmsActivationResponse smsActivationResponse;
                String vkPhone;
                driver.switchTo().newWindow(WindowType.TAB);
                driver.get("https://id.vk.com/restore/#/resetPassword");
                while (true) {
                    smsActivationResponse = smsActivate.purchaseVkActivation();
                    vkPhone = smsActivationResponse.getPhone();
                    System.out.println(vkPhone);
                    waitUntilElementWillBePresent(driver, "input[@data-test-id='loginInput']").sendKeys(vkPhone);
                    Thread.sleep(1000);
                    findElement(driver, "button[@data-test-id='nextButton']").click();
                    try {
                        waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Чтобы продолжить, подтвердите, что вы владелец аккаунта.");
                        System.out.println("Phone number " + smsActivationResponse.getPhone() + " was already used...");
                        try {
                            smsActivate.cancelRequest(smsActivationResponse.getId());
                            driver.navigate().refresh();
                        } catch (Exception e) {

                        }
                    } catch (Exception e) {
                        System.out.println("Going to use phone number " + smsActivationResponse.getPhone());
//                        smsActivationResponse = smsActivate.waitUntilPhoneIsReadyToReceiveCode(smsActivationResponse);
//                        if (smsActivationResponse == null) {
//                            continue;
//                        }
                        break;
                    }
                }
                driver.close();
                driver.switchTo().window(new ArrayList<>(driver.getWindowHandles()).get(0));
                System.out.println(vkPhone);
                vkAccount.setPhone(vkPhone);

                WebElement phoneField = waitUntilElementWillBePresent(driver, "input[@name='phone']");
                Thread.sleep(3000);
                if (smsActivationResponse.getPhone().contains("+")) {
                    phoneField.sendKeys(smsActivationResponse.getPhone().replace("+7", ""));
                } else {
                    phoneField.sendKeys(smsActivationResponse.getPhone().substring(1));
                }
                findElement(driver, "button[@type='submit']").click();
                try {
                    waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Auth token is expired", 5);
                    driver.navigate().refresh();
                    driver.switchTo().alert().accept();
                    phoneField = waitUntilElementWillBePresent(driver, "input[@name='phone']");
                    if (smsActivationResponse.getPhone().contains("+")) {
                        phoneField.sendKeys(smsActivationResponse.getPhone().replace("+7", ""));
                    } else {
                        phoneField.sendKeys(smsActivationResponse.getPhone().substring(1));
                    }
                    findElement(driver, "button[@type='submit']").click();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    driver.findElement(By.xpath("//span[contains(text(), 'На данный номер телефона зарегистрироваться нельзя'))]"));
                    System.out.println("Phone number " + smsActivationResponse.getPhone() + " banned cause 'На данный номер телефона зарегистрироваться нельзя. Попробуйте использовать другой номер. [1000]'...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    driver.findElement(By.xpath("//span[contains(text(), 'Maximum of api retries exceeded')]"));
                    System.out.println("Phone number " + smsActivationResponse.getPhone() + " banned cause 'Maximum of api retries exceeded'...");
                    for (final String image : photos) {
                        Files.delete(Paths.get(image));
                    }
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    driver.findElement(By.xpath("//span[contains(text(), 'Достигнут лимит верификаций для телефонного номера. Попробуйте позже.')]"));
                    System.out.println("Phone number " + smsActivationResponse.getPhone() + " banned cause 'Достигнут лимит верификаций для телефонного номера. Попробуйте позже.'...");
                    for (final String image : photos) {
                        Files.delete(Paths.get(image));
                    }
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final WebElement verificationCodeField;
                try {
                    verificationCodeField = waitUntilElementWillBePresentWithoutTimeout(driver, "input[@id='otp']");
                } catch (Exception e) {
                    continue;
                }
                String verificationCode = smsActivate.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 1);
                System.out.println(verificationCode);
                if (verificationCode == null) {
                    while (true) {
                        try {
                            waitUntilElementWithTextWillBePresent(driver, "span", "Отправить ещё раз").click();
                            break;
                        } catch (Exception e) {
                            try {
                                driver.findElement(By.xpath("//button/span/span/span[text()='Подтвердить другим способом']")).click();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    verificationCode = smsActivate.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 1);
                    if (verificationCode == null) {
                        smsActivate.cancelRequest(smsActivationResponse.getId());
                        System.out.println("Phone number " + smsActivationResponse.getPhone() + " banned...");
                        for (final String image : photos) {
                            Files.delete(Paths.get(image));
                        }
                        dbConnection.fixateRegistrationAttempt(proxyUrl);
                        continue;
                    }
                }
                verificationCodeField.sendKeys(verificationCode);
                findElement(driver, "button[@type='submit']").click();
                try {
                    waitUntilElementWillBePresent(driver, "span[contains(text(), 'Это не я')]", 5).click();
                    System.out.println("Phone number " + smsActivationResponse.getPhone() + " was already used...");
                    for (final String image : photos) {
                        Files.delete(Paths.get(image));
                    }
                    smsActivate.banRequest(smsActivationResponse.getId());
                    continue;
                } catch (Exception e) {
                }
                final String firstName = randomName(sex);
                waitUntilElementWillBePresent(driver, "input[@name='first_name']").sendKeys(firstName);

                final String lastName = randomLastName(sex);
                findElement(driver, "input[@name='last_name']").sendKeys(lastName);
                String birthday = generateRandomBirthday();
                String birthYear = birthday.substring(birthday.lastIndexOf(".") + 1);
                findElement(driver, "input[@name='birthday']").sendKeys(birthday);

                try {
                    WebElement sexCombo = waitUntilElementWithTextWillBePresent(driver, "span", "Пол").findElement(By.xpath("./..")).findElement(By.xpath("./.."));
                    sexCombo.click();
                    Thread.sleep(300);
                    sexCombo.sendKeys(Keys.ARROW_DOWN);
                    if (sex.equals("Мужской")) {
                        sexCombo.sendKeys(Keys.ARROW_DOWN);
                    }
                    sexCombo.sendKeys(Keys.ENTER);
                } catch (Exception e) {
                    try {
                        waitUntilElementWillBePresent(driver, "div[@title='" + sex + "']").click();
                    } catch (Exception ignored) {

                    }
                    e.printStackTrace();
                    System.out.println(driver.getPageSource());
                }

                findElement(driver, "button[@type='submit']").click();
                try {
                    waitUntilElementWithTextWillBePresent(driver, "div", "Телефон был заблокирован для регистрации");
                    System.out.println("Phone number " + smsActivationResponse.getPhone() + " was already used...");
                    for (final String image : photos) {
                        Files.delete(Paths.get(image));
                    }

                    continue;
                } catch (Exception e) {
                }
                smsActivate.requestAdditionalSms(smsActivationResponse.getId());
                try {
                    WebElement cityInput = waitUntilElementWillBePresent(driver, "input[@aria-owns='list_options_container_1']");
                    cityInput.clear();
                    cityInput.sendKeys(randomRuCity());
                    Thread.sleep(1000);
                    waitUntilElementWillBePresent(driver, "li[@id='option_list_options_container_1_1']").click();
                    Thread.sleep(1000);
                    waitUntilElementWhichContainsTextWillBePresent(driver, "button", "Продолжить").click();
                    waitUntilElementWillBePresent(driver, "a[@class='join_skip_link']").click();
                    waitUntilElementWillBePresent(driver, "a[@class='join_skip_link']").click();
                    try {
                        waitUntilElementWillBePresent(driver, "span[contains(text(), 'Пропустить')]")
                                .findElement(By.xpath("./.."))
                                .findElement(By.xpath("./.."))
                                .click();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception ignored) {
                    driver.get("https://vk.com/edit?act=contacts");
                    WebElement cityInput = waitUntilElementWillBePresent(driver, "input[@aria-owns='list_options_container_1']");
                    cityInput.clear();
                    cityInput.sendKeys(randomRuCity());
                    Thread.sleep(1000);
                    waitUntilElementWillBePresent(driver, "li[@id='option_list_options_container_1_1']").click();
                    Thread.sleep(1000);
                    clickButton(driver, findElement(driver, "button[@onclick='ProfileEditor.saveContacts(this)']"));
                }


                waitUntilElementWillBePresentWithoutTimeout(driver, "a[@id='top_profile_link']").click();

                waitUntilElementWillBePresent(driver, "a[@id='top_settings_link']").click();
                waitUntilElementWillBePresent(driver, "a[@id='ui_rmenu_security']", 100).click();
                Thread.sleep(300);
                driver.switchTo().window(new ArrayList<>(driver.getWindowHandles()).get(1));
                waitUntilElementWithTextWillBePresent(driver, "span", "Двухфакторная аутентификация").click();
                waitUntilElementWithTextWillBePresent(driver, "span", "Телефон").click();
                Thread.sleep(15000);
//                waitUntilElementWithTextWillBePresent(driver, "button/span/span", "Продолжить").click();
//                WebElement verificationCodeFieldForPassword;
//                try {
//                    verificationCodeFieldForPassword = waitUntilElementWillBePresent(driver, "input[@placeholder='Введите код из SMS']");
//                } catch (Exception e) {
//                    try {
//                        waitUntilElementWithTextWillBePresent(driver, "button/span/span", "Продолжить").click();
//                        verificationCodeFieldForPassword = waitUntilElementWillBePresent(driver, "input[@placeholder='Введите код из SMS']");
//                    } catch (Exception ex) {
//                        System.out.println("Waitin until sms code field will be shown");
//                        try {
//                            waitUntilElementWillBePresentWithoutTimeout(driver, "button/span/span[text()='Подтвердить по SMS']").click();
//                        } catch (Exception e3) {
//                            e3.printStackTrace();
//                        }
//                        verificationCodeFieldForPassword = waitUntilElementWillBePresentWithoutTimeout(driver, "input[@placeholder='Введите код из SMS']");
//                    }
//
//
//                }

                try {
                    String verificationCodeForPassword = smsActivate.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 2);
                    if (verificationCodeForPassword == null) {
                        smsActivate.requestAdditionalSms(smsActivationResponse.getId());
                        verificationCodeForPassword = smsActivate.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 2);
                        Thread.sleep(1000);
                    }
                    if (verificationCodeForPassword != null && verificationCodeForPassword.equals(verificationCode)) {
                        Thread.sleep(1000);
                        verificationCodeForPassword = smsActivate.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 2);
                    }
                    if (verificationCodeForPassword == null) {
                        waitUntilElementWithTextWillBePresentWithoutTimeout(driver, "button/span/span/span", "Отправить код ещё раз").click();
                        verificationCodeForPassword = smsActivate.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 2);
                        if (verificationCodeForPassword == null) {
                            smsActivate.finishRequest(smsActivationResponse.getId());
                            throw new Exception("Phone number " + smsActivationResponse.getPhone() + " didn't receive second sms...");
//                        for (final String image : photos) {
//                            Files.delete(Paths.get(image));
//                        }
//                        continue;
                        }
                    }
                    System.out.println(verificationCodeForPassword);
                    try {
                        List<WebElement> verificationCodeCells = driver.findElements(By.xpath("//input[@type='tel']"));
                        for (int index = 0; index < verificationCodeForPassword.length(); index++) {
                            final WebElement verificationCodeCell = verificationCodeCells.get(index);
                            clickButton(driver, verificationCodeCell);
                            verificationCodeCell.sendKeys(String.valueOf(verificationCodeForPassword.charAt(index)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        waitUntilElementWillBePresentWithoutTimeout(driver, "input[@placeholder='Введите новый пароль']").sendKeys(vkPassword);
                        Thread.sleep(1000);
                        waitUntilElementWillBePresent(driver, "input[@placeholder='Повторите новый пароль']").sendKeys(vkPassword);
                        Thread.sleep(1000);
                        waitUntilElementWithTextWillBePresent(driver, "span", "Продолжить").click();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    driver.switchTo().newWindow(WindowType.TAB);
                    driver.get("https://mail.ru/");
                    waitUntilElementWithTextWillBePresent(driver, "a", "Создать почту").click();
                    ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                    Thread.sleep(1000);
                    driver.switchTo().window(windows.get(3));
                    Thread.sleep(5000);
                    while (true) {
                        try {
                            waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Продолжить как").click();
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Thread.sleep(1000);
                    windows = new ArrayList<>(driver.getWindowHandles());
                    driver.switchTo().window(windows.get(4));
                    waitUntilElementWillBePresent(driver, "button[@data-test-id='continue-as-button']").click();
                    Thread.sleep(2500);
                    windows = new ArrayList<>(driver.getWindowHandles());
                    driver.switchTo().window(windows.get(3));
                    WebElement emailField = waitUntilElementWillBePresent(driver, "input[@name='partial_login']");
                    driver.switchTo().newWindow(WindowType.TAB);
                    driver.get("https://translate.google.com/?hl=ru");
                    try {
                        waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Принять все").findElement(By.xpath("./..")).click();
                    } catch (Exception ignore) {
                    }
                    try {
                        waitUntilElementWillBePresent(driver, "textarea[@aria-label='Исходный текст']").sendKeys("mirror");
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        waitUntilElementWillBePresent(driver, "button[@aria-label='Принять все']").click();
                        waitUntilElementWillBePresent(driver, "textarea[@aria-label='Исходный текст']").sendKeys("mirror");
                        Thread.sleep(1000);
                    }
                    waitUntilElementWillBePresent(driver, "textarea[@aria-label='Исходный текст']").clear();
                    waitUntilElementWillBePresent(driver, "textarea[@aria-label='Исходный текст']").sendKeys("mirror");
                    Thread.sleep(1000);
                    findElement(driver, "i[text()='swap_horiz']").findElement(By.xpath("./..")).click();
                    WebElement translateField = waitUntilElementWillBePresent(driver, "textarea[@aria-label='Исходный текст']");
                    translateField.clear();
                    translateField.sendKeys(firstName + " " + lastName);
                    Thread.sleep(1000);
                    translateField.clear();
                    translateField.sendKeys(firstName + " " + lastName);
                    Thread.sleep(3000);
                    String enVkFullName = findElement(driver, "h2[text()='Результаты перевода']").findElement(By.xpath("./..")).findElement(By.xpath(".//div")).getAttribute("innerText").split("\n")[1];
                    enVkName = enVkFullName.split(" ");
                    Thread.sleep(1000);
                    driver.close();
                    driver.switchTo().window(windows.get(3));
                    emailField.sendKeys(enVkName[0]);
                    Thread.sleep(500);
                    emailField.sendKeys("." + enVkName[1]);
                    Thread.sleep(1500);
                    try {
                        driver.findElement(By.xpath("//div[contains(@data-email, '" + birthYear + "')]")).click();
                    } catch (Exception e) {
                        try {
                            driver.findElement(By.xpath("//div[contains(@data-email, '@mail.ru')]")).click();
                        } catch (Exception ex) {
                            try {
                                driver.findElement(By.xpath("//div[contains(@data-email, '@internet.ru')]")).click();
                            } catch (Exception ex1) {
                                try {
                                    driver.findElement(By.xpath("//div[contains(@data-email, '@bk.ru')]")).click();
                                } catch (Exception ex2) {
                                    driver.findElement(By.xpath("//div[contains(@data-email, '@inbox.ru')]")).click();
                                }
                            }
                        }
                    }
                    Thread.sleep(1500);
                    driver.findElements(By.xpath("//button[@type='submit']")).get(1).click();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Waiting for mail registration blyat.......");
                    Thread.sleep(30000);
                    waitUntilElementWillBePresentWithoutTimeout(driver, "div[@data-testid='whiteline-account']").getAttribute("aria-label");
                }
                email = waitUntilElementWillBePresentWithoutTimeout(driver, "div[@data-testid='whiteline-account']").getAttribute("aria-label");
                ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(1));
                //   79022319746
                //      waitUntilElementWillBePresent(driver, "input[@placeholder='Укажите почту']").sendKeys(email);
                try {
                    waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Пропустить").findElement(By.xpath("./..")).findElement(By.xpath("./..")).click();
                    waitUntilElementWhichContainsTextWillBePresent(driver, "h2", "Резервные коды");
                    final StringBuilder reserveCodesBuilder = new StringBuilder();
                    for (WebElement element : driver.findElements(By.xpath("//div[@class='CodesList-codeContainer-lpcsxi']/h2[2]"))) {
                        reserveCodesBuilder.append(element.getAttribute("innerText").trim()).append(" ");
                    }
                    vkAccount.setReserveCodes(reserveCodesBuilder.toString().trim());
                    System.out.println("Accquired reserve codes: " + vkAccount.getReserveCodes());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Failed to save secure codes");
                }
//                waitUntilElementWithTextWillBePresent(driver, "button/span/span", "Продолжить").click();
//                Thread.sleep(1000);
//                driver.switchTo().window(windows.get(3));
//                while (true) {
//                    try {
//                        waitUntilElementWillBePresent(driver, "span[@class='ll-crpt'][text()='ВКонтакте']").click();
//                        break;
//                    } catch (Exception e) {
//                        driver.navigate().refresh();
//                    }
//                }
//                Thread.sleep(1000);
//                waitUntilElementWillBePresent(driver, "a[contains(text(), 'bind_confirm')]").click();
//                Thread.sleep(1000);
//                windows = new ArrayList<>(driver.getWindowHandles());
//                driver.switchTo().window(windows.get(4));
//                driver.close();
//                Thread.sleep(1000);
//                driver.switchTo().window(windows.get(1));
//                waitUntilElementWithTextWillBePresent(driver, "button/span/span", "Продолжить").click();
//                Thread.sleep(1000);
//                driver.close();
                driver.switchTo().window(windows.get(0));

                smsActivate.finishRequest(smsActivationResponse.getId());

                try {
                    waitUntilElementWithTextWillBePresent(driver, "span", "Фотографии").click();
                    waitUntilElementWithTextWillBePresent(driver, "span", "Фотографии").click();
                    waitUntilElementWithTextWillBePresent(driver, "span", "Загрузить фото");
                    WebElement fileField = findElement(driver, "input[@type='file']");
                    fileField.sendKeys(StringUtils.join(photos, "\n"));

                    Thread.sleep(10000);
                    while (true) {
                        try {
                            waitUntilElementWithTextWillBePresent(driver, "span", "Сохранить").findElement(By.xpath("./..")).findElement(By.xpath("./..")).click();
                            break;
                        } catch (Exception e) {
                            Thread.sleep(1000);
                        }
                    }
                    Thread.sleep(5000);
                    driver.findElements(By.xpath("//div[@aria-label='Закрыть']")).get(1).click();
                    Thread.sleep(1000);
                    waitUntilElementWillBePresent(driver, "div[contains(@class, 'PhotoAlbumPhotoItem-module__photo')]").click();
                    waitUntilElementWillBePresent(driver, "*[contains(@class, 'pv_actions_more')]").click();
                    waitUntilElementWillBePresent(driver, "*[@id='pv_more_act_to_profile']").click();

                    dragElement(driver, "div[@class='PhotoAreaSelector__linzaFrame']", "div[@class='PhotoAreaSelector__linza']");
                    Thread.sleep(300);
                    dragElement(driver, "div[@class='PhotoAreaSelector__handler PhotoAreaSelector__handler--type-se PhotoAreaSelector__handler--dragged-no']",
                            "button[@class='FCPanel__add'][@aria-label='Развернуть']");

                    waitUntilElementWithTextWillBePresent(driver, "button", "Сохранить и продолжить").click();
                    Thread.sleep(1000);
                    dragElement(driver, "div[@class='PhotoAreaSelector__linzaFrame']", "div[@class='PhotoAreaSelector__linza']");
                    Thread.sleep(300);
                    dragElement(driver, "div[@class='PhotoAreaSelector__handler PhotoAreaSelector__handler--type-se PhotoAreaSelector__handler--dragged-no']",
                            "button[@class='Button Button--size-m Button--secondary'][contains(text(), 'Вернуться назад')]");
                    waitUntilElementWithTextWillBePresent(driver, "button", "Сохранить изменения").click();
                    waitUntilElementWillBePresent(driver, "label[@class='CheckBox']").click();
                    waitUntilElementWithTextWillBePresent(driver, "button", "Продолжить").click();
                    for (final String image : photos) {
                        Files.delete(Paths.get(image));
                    }
                } catch (Exception e) {
                    System.out.println("Failed to process photos");
                    e.printStackTrace();
                }


            } else {
//                for (final String image : photos) {
//                    Files.delete(Paths.get(image));
//                }
                driver.switchTo().newWindow(WindowType.TAB);
                driver.get("https://mail.ru/");
                driver.switchTo().newWindow(WindowType.TAB);
                driver.get("https://e.mail.ru/inbox");
                email = waitUntilElementWillBePresentWithoutTimeout(driver, "div[@data-testid='whiteline-account']").getAttribute("aria-label");
            }
            final VkSerfingAccount vkSerfingAccount = new VkSerfingAccount();
            vkSerfingAccount.setEmail(email);
            vkSerfingAccount.setPassword(vkPassword);
            final Integer vkSerfingAccId = dbConnection.saveVkSerfingAccount(vkSerfingAccount);
            vkSerfingAccount.setId(vkSerfingAccId);
            vkAccount.setVkSerfingAccId(vkSerfingAccId);
            dbConnection.markBrowserAsNotReadyYet(browserProfile);
            final Integer vkAccId = dbConnection.saveVKAccSerfing(vkAccount);
            int amountOfToBeExecutedScenarious = getRandomNumber(1, 5);


            try {
                driver.switchTo().newWindow(WindowType.TAB);
                while (true) {
                    try {
                        driver.get("https://www.google.com/");
                        Thread.sleep(2000);
                        WebElement googleSearchField = null;
                        try {
                            googleSearchField = findElement(driver, "form[@action='/search']/div/div/div/div/div[2]/textarea");
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                googleSearchField = findElement(driver, "form[@action='/search']/div/div/div/div/div[2]/input");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (googleSearchField == null) {
                            try {
                                googleSearchField = findElement(driver, "form[@action='/search']/div/div/div/div/div[2]/div[2]/textarea");
                            } catch (Exception e) {
                                e.printStackTrace();
                                googleSearchField = findElement(driver, "form[@action='/search']/div/div/div/div/div[2]/div[2]/input");
                            }
                        }
                        clickButton(driver, googleSearchField);
                        googleSearchField.sendKeys(randomSearchForVkSerfing());
                        googleSearchField.sendKeys(Keys.ENTER);
                        waitUntilElementWillBePresent(driver, "a[@href='https://vkserfing.ru/']").click();
//                if (getRandomNumber(0, 100) > 50) {
//                    clickButton(driver, "a[@class='h-header__auth-btn']");
//                } else {
                        Thread.sleep(2500);
                        clickButton(driver, driver.findElements(By.xpath("//span[text()='Начать сейчас']")).get(1).findElement(By.xpath("./..")));
//                }
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                clickButton(driver, "button[@class='h-social h-social--vk']");
                Thread.sleep(2500);
                ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(5));
                try {
                    Thread.sleep(1000);
                    clickElement(driver, driver.findElements(By.xpath("//button[@type='submit']")).get(1));
                } catch (Exception e) {
                    try {
                        waitUntilElementWillBePresent(driver, "button[@data-test-id='continue-as-button']").click();
                    } catch (Exception ex) {
                        waitUntilElementWithTextWillBePresent(driver, "button", "Разрешить").click();
                    }
                }
                windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(4));
//                while (true) {
//                    try {
                waitUntilElementWillBePresent(driver, "label[@class='checkbox checkbox--center checkbox--border']");
//                        break;
//                    } catch (Exception e) {
                //   clickButton(driver, "button[@class='h-social h-social--vk']");
//                        Thread.sleep(1000);
//                    }
//                }
                for (WebElement checkbox : driver.findElements(By.xpath("//label[@class='checkbox checkbox--center checkbox--border']"))) {
                    clickButton(driver, checkbox);
                }
                Thread.sleep(getRandomNumber(600, 1000));
                clickButton(driver, findElement(driver, "span[text()='Привязать']").findElement(By.xpath("./..")));
                //waitUntilElementWillBePresent(driver, "label[@class='complex-input__label'][contains(text(),'E-mail адрес')");
                Thread.sleep(getRandomNumber(3000, 4000));
                waitUntilElementWillBePresent(driver, "input");
                WebElement vkSerfingEmailField = driver.findElements(By.xpath("//input")).get(1);
                clickButton(driver, vkSerfingEmailField);
                vkSerfingEmailField.sendKeys(email);
                Thread.sleep(getRandomNumber(300, 600));
                findElement(driver, "button[@class='form__btn btn btn--block']").click();
                for (int index = 0; index < amountOfToBeExecutedScenarious; index++) {
                    final int scenarioId = getRandomNumber(0, 11);
                    vkSerfingScenarious.get(scenarioId).accept(driver);
                }
            } catch (Exception e) {
                System.out.println("Failed to registrate vkserfing acc");
                e.printStackTrace();
            }
            try {
                driver.switchTo().newWindow(WindowType.TAB);
                driver.get("https://www.google.com/");
                Thread.sleep(2000);
                WebElement googleSearchField = null;
                try {
                    googleSearchField = findElement(driver, "form[@action='/search']/div/div/div/div/div[2]/textarea");
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        googleSearchField = findElement(driver, "form[@action='/search']/div/div/div/div/div[2]/input");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (googleSearchField == null) {
                    try {
                        googleSearchField = findElement(driver, "form[@action='/search']/div/div/div/div/div[2]/div[2]/textarea");
                    } catch (Exception e) {
                        e.printStackTrace();
                        googleSearchField = findElement(driver, "form[@action='/search']/div/div/div/div/div[2]/div[2]/input");
                    }
                }
                clickButton(driver, googleSearchField);
                googleSearchField.sendKeys(randomSearchForVkTarget());
                googleSearchField.sendKeys(Keys.ENTER);
                waitUntilElementWillBePresent(driver, "a[@href='https://vktarget.ru/']").click();
                waitUntilElementWillBePresent(driver, "div[@class='right']/a[@href='/registration/']").click();
                WebElement vktargetEmailField = waitUntilElementWillBePresent(driver, "input[@name='email']");
                clickButton(driver, vktargetEmailField);
                Thread.sleep(getRandomNumber(500, 1000));
                vktargetEmailField.sendKeys(email);
                WebElement vkTargetPasswordField = waitUntilElementWillBePresent(driver, "input[@name='password']");
                clickButton(driver, vkTargetPasswordField);
                Thread.sleep(getRandomNumber(500, 1000));
                vkTargetPasswordField.sendKeys(vkPassword);
                System.out.println(vkPassword);
                WebElement vkTargetConfirmPasswordField = waitUntilElementWillBePresent(driver, "input[@name='password_confirm']");
                clickButton(driver, vkTargetConfirmPasswordField);
                Thread.sleep(getRandomNumber(500, 1000));
                vkTargetConfirmPasswordField.sendKeys(vkPassword);
                Thread.sleep(getRandomNumber(500, 1000));
                clickButton(driver, findElement(driver, "label[@for='user_performer']"));
                Thread.sleep(getRandomNumber(500, 1000));
                clickButton(driver, findElement(driver, "label[@for='terms']"));
                Thread.sleep(getRandomNumber(500, 1000));
                clickButton(driver, "button[@data-registration='submit']");
                clickButton(driver, waitUntilElementWillBePresent(driver, "button[@data-network='vk']"));
                Thread.sleep(getRandomNumber(1500, 2500));
                clickButton(driver, waitUntilElementWillBePresent(driver, "div[@data-uloginbutton='vkontakte']"));
                Thread.sleep(getRandomNumber(2000, 3000));
                ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(6));
                try {
                    Thread.sleep(1000);
                    clickElement(driver, driver.findElements(By.xpath("//button[@type='submit']")).get(1));
                } catch (Exception e) {
                    try {
                        waitUntilElementWillBePresent(driver, "button[@data-test-id='continue-as-button']").click();
                    } catch (Exception ex) {
                        waitUntilElementWithTextWillBePresent(driver, "button", "Разрешить").click();
                    }
                }
                windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(5));
                Thread.sleep(400, 700);
                clickButton(driver, waitUntilElementWillBePresent(driver, "a[@href='https://vktarget.ru/list/']"));

                for (int index = 0; index < amountOfToBeExecutedScenarious; index++) {
                    final int scenarioId = getRandomNumber(0, 3);
                    vkTargetScenarious.get(scenarioId).accept(driver);
                }
            } catch (Exception e) {
                System.out.println("Failed to registrate vktarget acc");
                e.printStackTrace();
            }

            try {
                driver.manage().window().minimize();
                driver.manage().window().maximize();

                Thread.sleep(3000);
                openNewTab(robot);
                Thread.sleep(1000);
                robot.keyPress(KeyEvent.VK_BACK_SPACE);
                Thread.sleep(300);
                robot.keyRelease(KeyEvent.VK_BACK_SPACE);
                Thread.sleep(300);
                typeString("https://payeer.com/ru/auth/?register=yes", robot);
                Thread.sleep(300);
                pressEnter(robot);
                Thread.sleep(10000);
                typeString(email, robot);
                Thread.sleep(2000);
                clickOn(932, 741, robot);
                Thread.sleep(3000);
                clickOn(835, 654, robot);
                ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(6));
                Thread.sleep(3000);
                try {
                    waitUntilElementWillBePresent(driver, "button[@class='login-form__login-btn step1']").click();
                } catch (Exception ignored) {
                }
                waitUntilElementWillBePresent(driver, "button[@class='login-form__confirmation-text']").click();
                final String payeerPassword = waitUntilElementWillBePresent(driver, "input[@name='new_password']").getAttribute("value");
                final String payeerSecret = findElement(driver, "input[@name='secret_word']").getAttribute("value");
                final String payeerAccName = findElement(driver, "input[@name='nick']").getAttribute("value");
                final PayeerAccount payeerAccount = new PayeerAccount();
                payeerAccount.setAccName(payeerAccName);
                payeerAccount.setEmail(email);
                payeerAccount.setSecretCode(payeerSecret);
                payeerAccount.setPassword(payeerPassword);
                payeerAccount.setVkAccId(vkAccId);
                waitUntilElementWillBePresent(driver, "button[@class='login-form__login-btn mini-mid-btn']").click();
                // final String payeerName = email.split("@")[0].replaceAll("\\d", "");
                waitUntilElementWillBePresent(driver, "input[@name='name']").sendKeys(enVkName[0]);
                waitUntilElementWillBePresent(driver, "input[@name='last_name']").sendKeys(enVkName[1]);
                Thread.sleep(500);
                waitUntilElementWillBePresent(driver, "button[@class='login-form__login-btn mini-mid-btn']").click();
                String masterKey = waitUntilElementWhichContainsTextWillBePresent(driver, "div", "Master key:").findElement(By.xpath("./..")).findElement(By.xpath(".//div[@class='info-security__data']")).getText();
                payeerAccount.setMasterKey(masterKey);
                dbConnection.savePayeerAccount(payeerAccount);
                System.out.println("registrated payeer acc: " + payeerAccName);
            } catch (Exception e) {
                System.out.println("Failed to registrate payeer acc");
                e.printStackTrace();
            }

            try {
                openNewTab(robot);
                Thread.sleep(1000);
                robot.keyPress(KeyEvent.VK_BACK_SPACE);
                Thread.sleep(300);
                robot.keyRelease(KeyEvent.VK_BACK_SPACE);
                Thread.sleep(300);
                typeString("https://v-like.ru/", robot);
                Thread.sleep(300);
                pressEnter(robot);
                Thread.sleep(getRandomNumber(3000, 5000));
                clickOn(getRandomNumber(1365, 1486), getRandomNumber(113, 134), robot);
                Thread.sleep(getRandomNumber(8000, 10000));
                clickOn(534, 360, robot);
                Thread.sleep(2000);
                ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(7));
                try {
                    clickButton(driver, waitUntilElementWillBePresent(driver, "a[@href='https://v-like.ru/auth/register']"));
                } catch (Exception e) {
                    clickButton(driver, waitUntilElementWillBePresent(driver, "a[@href='https://v-like.ru/the/auth/register']"));
                }
                if (getRandomNumber(0, 100) > 50) {
                    clickButton(driver, "a[@data-cookie='user']");
                    WebElement vlikeEmailField = waitUntilElementWillBePresent(driver, "input[@id='email']");
                    clickButton(driver, vlikeEmailField);
                    Thread.sleep(getRandomNumber(500, 1000));
                    vlikeEmailField.sendKeys(email);
                } else {
                    WebElement vlikeEmailField = waitUntilElementWillBePresent(driver, "input[@id='email']");
                    clickButton(driver, vlikeEmailField);
                    Thread.sleep(getRandomNumber(500, 1000));
                    vlikeEmailField.sendKeys(email);
                    clickButton(driver, "a[@data-cookie='user']");
                }
                WebElement vlikePasswordField = waitUntilElementWillBePresent(driver, "input[@id='password']");
                clickButton(driver, vlikePasswordField);
                Thread.sleep(getRandomNumber(500, 1000));
                vlikePasswordField.sendKeys(vkPassword);
                WebElement confirmVlikePasswordField = waitUntilElementWillBePresent(driver, "input[@id='password-confirm']");
                clickButton(driver, confirmVlikePasswordField);
                Thread.sleep(getRandomNumber(500, 1000));
                confirmVlikePasswordField.sendKeys(vkPassword);
                Thread.sleep(getRandomNumber(500, 1000));
                clickButton(driver, "button[@type='submit']");
                clickButton(driver, "a[@href='https://v-like.ru/tasks/vk']");
                try {
                    clickButton(driver, "button[@class='fc-close']");
                } catch (Exception e) {

                }
                clickButton(driver, "a[@class='btn-oauth vk']");
                Thread.sleep(1000);
                windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(8));
                try {
                    waitUntilElementWillBePresent(driver, "button[@data-test-id='continue-as-button']").click();
                } catch (Exception e) {
                    waitUntilElementWithTextWillBePresent(driver, "button", "Разрешить").click();

                }
                clickButton(driver, "a[@href='https://v-like.ru/tasks/vk']");
                clickButton(driver, "a[@href='https://v-like.ru/account/payments']");
                clickButton(driver, "a[@href='https://v-like.ru/account/settings']");
                clickButton(driver, "input[@value='Выйти с аккаунта']");
                Thread.sleep(3000);
                clickOn(getRandomNumber(1365, 1486), getRandomNumber(113, 134), robot);
                Thread.sleep(1000);
                windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(8));
                WebElement vlikeEmailField = waitUntilElementWillBePresent(driver, "input[@id='email']");
                clickButton(driver, vlikeEmailField);
                Thread.sleep(getRandomNumber(500, 1000));
                vlikeEmailField.sendKeys(email);
                vlikePasswordField = waitUntilElementWillBePresent(driver, "input[@id='password']");
                clickButton(driver, vlikePasswordField);
                Thread.sleep(getRandomNumber(500, 1000));
                vlikePasswordField.sendKeys(vkPassword);
                clickButton(driver, "button[@type='submit']");
            } catch (Exception e) {
                System.out.println("Failed to registrate vlike acc");
                e.printStackTrace();
            }
            try {
                openNewTab(robot);
                typeString("https://payup.video/", robot);
                Thread.sleep(2000);
                pressEnter(robot);
//                Thread.sleep(getRandomNumber(2500, 5000));
//                clickOn(getRandomNumber(441, 914),getRandomNumber(566, 608), robot);
//                Thread.sleep(getRandomNumber(500, 2000));
//                typeString(email, robot);
//                Thread.sleep(getRandomNumber(500, 2000));
//                clickOn(getRandomNumber(437, 908),getRandomNumber(676, 711), robot);
//                Thread.sleep(getRandomNumber(500, 2000));
//                typeString("X2uKGRYhn9KvRzXP9UhL", robot);
//                clickOn(getRandomNumber(444, 463),getRandomNumber(777, 792), robot);
                final PayUpVideoAcc payUpVideoAcc = new PayUpVideoAcc();
                payUpVideoAcc.setVkAccount(vkAccount);
                dbConnection.savePayUpVideoAcc(payUpVideoAcc);
            } catch (Exception e) {
                System.out.println("Failed to registrate payup video acc");
                e.printStackTrace();
            }
            try {
                openNewTab(robot);
                Thread.sleep(300);
                robot.keyPress(KeyEvent.VK_BACK_SPACE);
                Thread.sleep(300);

                typeString("chrome://settings/content/popups", robot);
                Thread.sleep(300);
                pressEnter(robot);
                Thread.sleep(2000);
                clickOn(834, 361, robot);
            } catch (Exception e) {
                System.out.println("Failed to enable redirections");
                e.printStackTrace();
            }
            try {
                driver.switchTo().newWindow(WindowType.TAB);
                updatePersonalInfoForVkAcc(driver, vkAccount);

                openNewTab(robot);
                typeString("https://www.youtube.com/", robot);
                Thread.sleep(2000);
                pressEnter(robot);
                Thread.sleep(3500);
                clickOn(1830, 97, robot);//Войти
                Thread.sleep(3000);
                clickOn(820, 719, robot);//Создать акк
                Thread.sleep(getRandomNumber(2000, 2500));
                clickOn(830, 767, robot);//Для лчного
                Thread.sleep(getRandomNumber(1000, 1500));
                typeRussianString(vkAccount.getFirstName(), robot);
                pressEnter(robot);
                Thread.sleep(getRandomNumber(1000, 1500));
                typeRussianString(vkAccount.getLastName(), robot);
                pressEnter(robot);
                Thread.sleep(getRandomNumber(1000, 1500));
                clickOn(806, 519, robot);
                typeString(String.valueOf(vkAccount.getDayOfBirth()), robot);
                pressKey(KeyEvent.VK_TAB, robot);
                Thread.sleep(300);
                pressEnter(robot);
                for (int index = 0; index < vkAccount.getMonthOfBirth(); index++) {
                    Thread.sleep(getRandomNumber(200, 450));
                    robot.keyPress(KeyEvent.VK_DOWN);
                    robot.keyRelease(KeyEvent.VK_DOWN);
                }
                Thread.sleep(300);
                pressEnter(robot);
                Thread.sleep(300);
                pressKey(KeyEvent.VK_TAB, robot);
                typeString(String.valueOf(vkAccount.getYearOfBirth()), robot);
                Thread.sleep(300);
                pressKey(KeyEvent.VK_TAB, robot);
                pressEnter(robot);
                Thread.sleep(getRandomNumber(200, 450));
                robot.keyPress(KeyEvent.VK_DOWN);
                robot.keyRelease(KeyEvent.VK_DOWN);
                if (vkAccount.getSex().startsWith("Муж")) {
                    Thread.sleep(getRandomNumber(200, 450));
                    robot.keyPress(KeyEvent.VK_DOWN);
                    robot.keyRelease(KeyEvent.VK_DOWN);
                }
                Thread.sleep(300);
                pressEnter(robot);
                Thread.sleep(300);
                clickOn(1099, 691, robot);
                Thread.sleep(getRandomNumber(2000, 2500));
                clickOn(888, 668, robot);//Создать электронный адрес Gmail
                Thread.sleep(getRandomNumber(1000, 1500));
                if (copyTextFromScreen(783, 416, 1019, 440, robot).contains("Выберите адрес электронной почты Gmail или создайте свой")) {
                    clickOn(890, 502, robot);
                    clickOn(955, 698, robot);
                } else {
                    String copiedText;
                    String gmailPrefix = email.split("@")[0].replaceAll("\\d", "");
                    if (gmailPrefix.endsWith(".")) {
                        gmailPrefix = gmailPrefix.substring(0, gmailPrefix.length() - 1);
                    }
                    boolean isFirstTime = true;
                    do {
                        if (isFirstTime) {
                            clickOn(1009, 539, robot);
                            typeString(gmailPrefix, robot);
                            Thread.sleep(300);
                            clickOn(956, 643, robot);//Далее
                            isFirstTime = false;
                        } else {
                            clickOn(1030, 512, robot);
                            for (int index = 0; index < gmailPrefix.length(); index++) {
                                Thread.sleep(50);
                                robot.keyPress(KeyEvent.VK_BACK_SPACE);
                                Thread.sleep(50);
                                robot.keyRelease(KeyEvent.VK_BACK_SPACE);
                            }
                            typeString(gmailPrefix, robot);
                            Thread.sleep(300);
                            clickOn(951, 681, robot);//Далее
                        }
                        Thread.sleep(1000, 1500);
                        copiedText = copyTextFromScreen(777, 612, 837, 612, robot);
                    } while (!copiedText.contains("Доступно"));

                    clickOn(900, 608, robot);
                    Thread.sleep(300);
                    pressKey(KeyEvent.VK_TAB, robot);
                    pressEnter(robot);
                }
                Thread.sleep(getRandomNumber(2000, 2500));
                typeString(YOUTUBE_ACCS_PASSWORD, robot);
                Thread.sleep(300);
                pressKey(KeyEvent.VK_TAB, robot);
                typeString(YOUTUBE_ACCS_PASSWORD, robot);
                clickOn(1094, 728, robot);

                SmsActivationResponse smsActivationResponse = smsActivate.purchaseNumber("go", "tele2", null);
                typeString(smsActivationResponse.getPhone(), robot);
                clickOn(1092, 676, robot);
                String verificationCode = smsActivate.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 1);
                System.out.println(verificationCode);
                if (verificationCode == null) {
                    throw new Exception("Verfication code is null!!!");
                }
                typeString(verificationCode, robot);
                Thread.sleep(2000);
                clickOn(1047, 705, robot);
                Thread.sleep(2000);
                clickOn(1059, 633, robot);
                Thread.sleep(2000);
                clickOn(1110, 591, robot);
                final YoutubeAccount youtubeAccount = new YoutubeAccount();
                youtubeAccount.setPhone(smsActivationResponse.getPhone());
                youtubeAccount.setVkAccId(vkAccount.getId());
                smsActivate.finishRequest(smsActivationResponse.getId());

                ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(windows.size() - 1));
                while (true) {//v rot ebal vashi formachki
                    try {
                        clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Пропустить", 5));
                    } catch (Exception e) {
                        try {
                            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Далее", 5));
                        } catch (Exception e2) {
                            try {
                                clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "button", "Другие варианты", 5));
                                clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "div", "Добавить мой номер телефона только в целях безопасности аккаунта", 5));
                                clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Готово", 5));
                            } catch (Exception e3) {
                                break;
                            }
                        }
                    }
                }
                clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Принимаю", 5));
                clickElement(driver, waitUntilElementWillBePresent(driver, "button[@id='avatar-btn']"));
                youtubeAccount.setGmail(waitUntilElementWillBePresent(driver, "yt-formatted-string[@id='email']").getAttribute("title"));
                dbConnection.saveYouTubeAcc(youtubeAccount);
                driver.get("https://www.youtube.com/account_privacy");
                clickElement(driver, waitUntilElementWillBePresent(driver, "div[@id='toggleButton']"));


            } catch (Exception e) {
                System.out.println("Failed to registrate youtube acc");
                e.printStackTrace();
            }
            removeLine(browserProfile, new File("forge/proxies.txt"));
            removeLine(facebookLink, new File("forge/facebook_accs.txt"));
            System.out.println("Registrated profile: " + browserProfile);
            System.out.println(facebookLink);
            Thread.sleep(3600000);
//            Thread.sleep(getRandomNumber(3600000, 3600000 * 2));
//            Thread.sleep(10000);
        }
    }

    private static void dragElement(final ChromeDriver driver, final String fromElementPath, final String toElementPath) throws Exception {
        WebElement fromElement = waitUntilElementWillBePresent(driver, fromElementPath);
        WebElement toElement = waitUntilElementWillBePresent(driver, toElementPath);
        Actions builder = new Actions(driver);
        Action dragAndDrop = builder.clickAndHold(fromElement)
                .moveToElement(toElement)
                .release(toElement)
                .build();
        dragAndDrop.perform();
    }

    public static void removeLine(String lineContent, File file) throws IOException {
        List<String> out = Files.lines(file.toPath())
                .filter(line -> !line.contains(lineContent))
                .collect(Collectors.toList());
        Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String generateRandomSpecialCharacters(int length) {
        return new Random().ints(length, 33, 122).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
    }

    private static String randomSearchForVkSerfing() {
        return new String[]{"вк серфинг", "вксерфинг", "vkserfing", "вксрфинг", "vk serfing", "vkserf", "вксерф"}[getRandomNumber(0, 6)];
    }

    private static String randomName(final String sex) {
        final String[] names;
        if (sex.equals("Мужской")) {
            names = new String[]{"Иван", "Илья", "Сергей", "Алексей", "Андрей", "Михаил", "Петр", "Дмитрий", "Владислав", "Мирон", "Юрий"};
        } else {
            names = new String[]{"Анастасия", "Ева", "Ольга", "Мария", "Дина", "Ангелина", "Арина", "Юля", "Дарья", "Ирина", "Карина", "Яна", "Элина", "Алена", "Анжелика", "Варвара", "Вера", "Вероника"};
        }
        return names[getRandomNumber(0, names.length - 1)];
    }

    private static String randomLastName(final String sex) {
        final String[] lastNames;
        if (sex.equals("Мужской")) {
            lastNames = new String[]{"Иванов", "Васильев", "Петров", "Смирнов", "Михайлов", "Фёдоров", "Соколов", "Яковлев", "Попов", "Андреев", "Алексеев", "Александров", "Лебедев", "Григорьев", "Степанов", "Семёнов", "Павлов", "Богданов", "Николаев", "Егоров", "Волков", "Кузнецов", "Никитин", "Соловьёв"};
        } else {
            lastNames = new String[]{"Иванова", "Васильева", "Петрова", "Смирнова", "Михайлова", "Фёдорова", "Соколова", "Яковлева", "Попова", "Андреева", "Алексеева", "Александрова", "Лебедева", "Григорьева", "Степанова", "Семёнова", "Павлова", "Богданова", "Николаева", "Егорова", "Волкова", "Кузнецова", "Никитина", "Соловьёва"};
        }
        return lastNames[getRandomNumber(0, lastNames.length - 1)];
    }

    private static String randomSearchForVkTarget() {
        return new String[]{"вк таргет", "вктаргет", "vktarget", "вктргет", "vktrget"}[getRandomNumber(0, 4)];
    }

    private static String randomSearchForVLike() {
        return new String[]{"vlike", "v like"}[getRandomNumber(0, 1)];
    }

    private static String randomRuCity() {
        return new String[]{"Москва",
                "Санкт-Петербург",
                "Екатеринбург",
                "Казань",
                "Пермь"
        }[getRandomNumber(0, 4)];
    }

    private static String generateRandomBirthday() {
        final StringBuilder birthday = new StringBuilder();
        int day = getRandomNumber(1, 27);
        if (day < 10) {
            birthday.append("0");
        }
        birthday.append(day).append(".");
        int month = getRandomNumber(1, 12);
        if (month < 10) {
            birthday.append("0");
        }
        birthday.append(month).append(".");
        int year = getRandomNumber(1994, 2000);
        birthday.append(year);
        return birthday.toString();
    }

    private static java.util.List<ForgeDetails> readForgeDetails() throws Exception {
        String content = new String(Files.readAllBytes(new File("forge/vk_accs.txt").toPath())).replaceAll("\r", "");
        final String[] vkAccs = content.split("\n");
        final List<ForgeDetails> forgeDetails = new ArrayList<>(vkAccs.length);
        final String[] proxies = new String(Files.readAllBytes(new File("forge/proxies.txt").toPath())).replaceAll("\r", "").split("\n");
        for (final String row : proxies) {
            final ForgeDetails forgeDetail = new ForgeDetails();
            final String[] info = row.split("\\|");
            forgeDetail.setProxy(info[0]);
            forgeDetail.setProxyPort(info[1]);
            forgeDetail.setProxyUsername(info[2]);
            forgeDetail.setProxyPassword(info[3]);
            forgeDetail.setBrowserProfile(info[4]);
            forgeDetails.add(forgeDetail);
        }
        final String facebookResources = new String(Files.readAllBytes(new File("forge/facebook_accs.txt").toPath())).replaceAll("\r", "");
        if (StringUtils.isNotEmpty(facebookResources)) {
            final String[] facebookInfo = facebookResources.split("\n");
            for (int index = 0; index < forgeDetails.size(); index++) {
                final ForgeDetails forgeDetail = forgeDetails.get(index);
                String[] info = facebookInfo[index].split("\\|");
                forgeDetail.setFacebookUrl(info[0]);
                forgeDetail.setSex(info[1]);
            }
        }
        Collections.shuffle(forgeDetails);
        return forgeDetails;
    }

    private static void click(Robot robot) {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private static void clickAway(Robot robot) {
        robot.mouseMove(2264, 539);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private static void cleanProxy(Robot robot) throws Exception {
        clickOn(609, 699, robot);
        Thread.sleep(400);
        clickOn(609, 699, robot);
    }

    private static WebElement findElement(final ChromeDriver driver, final String xpath) {
        return driver.findElement(By.xpath("//" + xpath));
    }

    private static void refreshPage(Robot robot) throws Exception {
        robot.keyPress(KeyEvent.VK_CONTROL);
        Thread.sleep(300);
        robot.keyPress(KeyEvent.VK_R);
        Thread.sleep(300);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        Thread.sleep(300);
        robot.keyRelease(KeyEvent.VK_R);
        Thread.sleep(1000);
    }

    private static void closePopup(Robot robot) {
        robot.mouseMove(1520, 438);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private static void copyCookiesFromDevTools(final Robot robot) throws Exception {
        openDevTools(robot);
        Thread.sleep(500);
        clickOn(2354, 180, robot);
        Thread.sleep(500);
        clickOn(2416, 267, robot);
        Thread.sleep(500);
        clickOn(2063, 428, robot);
        clickOn(2063, 428, robot);
        Thread.sleep(500);
        clickOn(2080, 448, robot);
        Thread.sleep(500);
        robot.keyPress(KeyEvent.VK_CONTROL);
        Thread.sleep(300);
        robot.keyPress(KeyEvent.VK_R);
        Thread.sleep(300);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        Thread.sleep(300);
        robot.keyRelease(KeyEvent.VK_R);
        Thread.sleep(1000);
    }

    private static void copyCookies(final Robot robot) throws Exception {
        robot.mouseMove(2498, 1114);
        robot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(300);
        robot.mouseMove(2210, 229);
        robot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(300);
        robot.keyPress(KeyEvent.VK_CONTROL);
        Thread.sleep(300);
        robot.keyPress(KeyEvent.VK_C);
        Thread.sleep(300);
        robot.keyRelease(KeyEvent.VK_C);
        Thread.sleep(300);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        Thread.sleep(200);
    }

    private static WebElement waitUntilElementWithTextWillBePresent(final ChromeDriver driver, final String tag, final String text) {
        final By searchCriteria = By.xpath("//" + tag + "[text()='" + text + "']");
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        return driver.findElement(searchCriteria);

    }


    private static WebElement waitUntilElementWithTextWillBePresentWithoutTimeout(final ChromeDriver driver, final String tag, final String text) {
        final By searchCriteria = By.xpath("//" + tag + "[text()='" + text + "']");
        new WebDriverWait(driver, Duration.ofSeconds(1200)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        return driver.findElement(searchCriteria);

    }

    private static WebElement waitUntilElementWillBePresent(final ChromeDriver driver, final String xpath) throws Exception {
        final By searchCriteria = By.xpath("//" + xpath);
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        Thread.sleep(2000);
        return driver.findElement(searchCriteria);
    }

    private static WebElement waitUntilElementWillBePresent(final ChromeDriver driver, final String xpath, final Integer timeout) throws Exception {
        final By searchCriteria = By.xpath("//" + xpath);
        new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        Thread.sleep(2000);
        return driver.findElement(searchCriteria);
    }

    private static WebElement waitUntilElementWillBePresentWithoutTimeout(final ChromeDriver driver, final String xpath) throws Exception {
        final By searchCriteria = By.xpath("//" + xpath);
        new WebDriverWait(driver, Duration.ofSeconds(1200)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        Thread.sleep(2000);
        return driver.findElement(searchCriteria);
    }

    private static String getFacebookHtml(final String link) throws Exception {
        final URLConnection connection = new URL(link).openConnection();
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        //connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
        connection.setRequestProperty("Sec-Fetch-Dest", "document");
        connection.setRequestProperty("Sec-Fetch-Site", "same-origin");
        connection.setRequestProperty("Cookie", "sb=rcgNZRBddsJd-n2cxREU1LBs; datr=rcgNZcLWwVnqNeMmP3vCgQUR; dpr=1.25; c_user=100017201528846; xs=29%3AdyLiLpFWlOPDPw%3A2%3A1699618338%3A-1%3A16234; fr=1169cE7GcTwwpFWX9.AWWITvJtpkCvqjjuRmOCny7Iylw.BlTMke.d-.AAA.0.0.BlTh4i.AWXcVQeGWbY; presence=C%7B%22t3%22%3A%5B%5D%2C%22utc3%22%3A1699618343690%2C%22v%22%3A1%7D; wd=1068x745; m_ls=%7B%22c%22%3A%7B%221%22%3A%22HCwAABasLhaW1_XCCBMFFpyAppzivS0A%22%2C%222%22%3A%22GSwVQBxMAAAWARaKzd7UDBYAABV-HEwAABYAFtKLpdQMFgAAFigA%22%2C%2295%22%3A%22HCwAABYQFuSn8rQEEwUWnICmnOK9LQA%22%7D%2C%22d%22%3A%22a4e2a442-f0a3-49be-a903-c15ad618d59d%22%2C%22s%22%3A%220%22%2C%22u%22%3A%22fsm6he%22%7D");
        final Scanner scanner = new Scanner(connection.getInputStream());
        scanner.useDelimiter("\\Z");
        final String profileHtml = scanner.next();
        scanner.close();
        return profileHtml;
    }

    private static String downloadFacebookPhoto(final String link, final int botImageIndex, final String facebookProfileName) throws Exception {
        final String photoHtml = getFacebookHtml(link);
        final Matcher photoTagMatcher = Pattern.compile("<link rel=\"preload\" href=\"(.*?)\"").matcher(photoHtml);
        String photoTag = null;
        while (photoTagMatcher.find()) {
            String group = photoTagMatcher.group();
            if (group.contains("scontent")) {
                photoTag = group;
                break;
            }
        }
        final String src;
        if (photoTag == null) {
            Matcher srcPhotoMatcher = Pattern.compile("\"image\":\\{\"uri\":\"(.*?)\"").matcher(photoHtml);
            srcPhotoMatcher.find();
            src = srcPhotoMatcher.group(1).replace("\\/", "/");
        } else {
            src = photoTag.split("href=\"")[1].split("\"")[0].replaceAll("&amp;", "&");
        }
        final String imageFileName = "C:\\work\\VkTargetBots\\bot_photos\\" + facebookProfileName + "_" + botImageIndex + ".png";
        try (InputStream in = new URL(src).openStream()) {
            Files.copy(in, Paths.get(imageFileName));
        }
        return imageFileName;
    }

    private static String downloadFile(final String src, final int botImageIndex, final String facebookProfileName) throws Exception {
        final String imageFileName = "C:\\work\\VkTargetBots\\bot_photos\\" + facebookProfileName + "_" + botImageIndex + ".png";
        try (InputStream in = new URL(src).openStream()) {
            Files.copy(in, Paths.get(imageFileName));
        }
        return imageFileName;
    }


    private static void clickButton(final ChromeDriver driver, final String xpath) throws Exception {
        final WebElement button = waitUntilElementWillBePresent(driver, xpath);
        final Actions builder = new Actions(driver);
        Dimension size = button.getSize();
        builder.moveToElement(button, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
                .pause(Duration.ofMillis(getRandomNumber(200, 500)))
                .click().build().perform();
    }

    private static void clickButton(final ChromeDriver driver, final WebElement button) throws Exception {
        final Actions builder = new Actions(driver);
        Dimension size = button.getSize();
        builder.moveToElement(button, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
                .pause(Duration.ofMillis(getRandomNumber(200, 500)))
                .click().build().perform();
    }

    private static void openDevTools(final Robot robot) throws Exception {
//        robot.mouseMove(609, 532);
//        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
//        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
//        Thread.sleep(1000);
//        clickOn(681, 821);
        robot.keyPress(KeyEvent.VK_CONTROL);
        Thread.sleep(200);
        robot.keyPress(KeyEvent.VK_SHIFT);
        Thread.sleep(200);
        robot.keyPress(KeyEvent.VK_I);
        Thread.sleep(200);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        Thread.sleep(200);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        Thread.sleep(200);
        robot.keyRelease(KeyEvent.VK_I);
        Thread.sleep(150);
    }


    public static class VkTargetScenarious {
        public static void clickStatisticForWeek(final ChromeDriver driver) throws Exception {
            clickButton(driver, "div[@data-chart-mode='week']");
        }

        public static void clickStatisticForAllTime(final ChromeDriver driver) throws Exception {
            clickButton(driver, "div[@data-chart-mode='all']");
        }

        public static void clickNextPageForNews(final ChromeDriver driver) throws Exception {
            WebElement element = driver.findElement(By.xpath("(//div[@class='news__btns__wrap']/div)[2]"));
            clickButton(driver, element);
        }

        public static void checkCompleted(final ChromeDriver driver) throws Exception {
            clickButton(driver, "li[@data-tab-id='completed']");
            Thread.sleep(getRandomNumber(1000, 2000));
            clickButton(driver, "li[@data-tab-id='available']");
        }
    }

    private static final List<Consumer<ChromeDriver>> vkTargetScenarious = Arrays.asList(
            driver -> {
                try {
                    VkTargetScenarious.clickStatisticForWeek(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkTargetScenarious.clickStatisticForAllTime(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkTargetScenarious.clickNextPageForNews(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkTargetScenarious.checkCompleted(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    );

    public static class VkSerfingScenarious {
        public static void checkExecutedTasks(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/assignments/logs']");
            Thread.sleep(getRandomNumber(1000, 4500));
            if (random()) {
                clickButton(driver, "a[@href='/assignments'][contains(@class, 'sidebar__link')]");
            } else {
                clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link is-active')]");
            }
        }

        public static void checkFails(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/assignments/fails']");
            Thread.sleep(getRandomNumber(1000, 4500));
            if (random()) {
                clickButton(driver, "a[@href='/assignments'][contains(@class, 'sidebar__link')]");
            } else {
                clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link is-active')]");
            }
        }

        public static void checkHelp(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndAccountAndProfiles(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickButton(driver, "a[@href='/help/accounts']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndViolations(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickButton(driver, "a[@href='/help/violations']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndAssignments(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickButton(driver, "a[@href='/help/assignments']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndSettings(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickButton(driver, "a[@href='/help/settings']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndPayments(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickButton(driver, "a[@href='/help/payments']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndOrders(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickButton(driver, "a[@href='/help/orders']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndTaskVerifications(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickButton(driver, "a[@href='/help/check_users']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndReferrals(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickButton(driver, "a[@href='/help/ref']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndOthers(final ChromeDriver driver) throws Exception {
            clickButton(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickButton(driver, "a[@href='/support/add']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickButton(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        private static boolean random() {
            return getRandomNumber(0, 100) > 50;
        }
    }

    public static final List<Consumer<ChromeDriver>> vkSerfingScenarious = Arrays.asList(
            driver -> {
                try {
                    VkSerfingScenarious.checkExecutedTasks(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkFails(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelp(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndAccountAndProfiles(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndViolations(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndAssignments(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndSettings(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndPayments(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndOrders(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndTaskVerifications(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndReferrals(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndOthers(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    );//12


    public static String getRegionFromPhoneNumber(String phoneNumber) {
        String regionCode = phoneNumber.substring(1, 4);
        HashMap<String, String> regions = new HashMap<>();
        regions.put("900", randomRuCity());
        regions.put("901", randomRuCity());
        regions.put("902", randomRuCity());
        regions.put("904", "Пермь");
        regions.put("908", randomRuCity());
        regions.put("915", "Белгород");
        regions.put("950", "Ростов");
        regions.put("951", "Пермь");
        regions.put("952", "Ростов");
        regions.put("953", randomRuCity());
        regions.put("958", randomRuCity());
        regions.put("977", "Москва");
        regions.put("991", "Санкт-Петербург");
        regions.put("992", "Тюмень");
        regions.put("993", "Орлов");
        regions.put("994", "Амурское");
        regions.put("995", "Краснодар");
        regions.put("996", "Оренбург");
        regions.put("999", "Москва");
        return regions.getOrDefault(regionCode, randomRuCity());
    }
}
