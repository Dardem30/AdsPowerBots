//package service;
//
//import bo.SmsActivationResponse;
//import bo.VkAccount;
//import bo.VkSerfingAccount;
//import bo.YoutubeAccount;
//import da.DbConnection;
//import org.apache.commons.lang3.StringUtils;
//import org.json.JSONObject;
//import org.openqa.selenium.By;
//import org.openqa.selenium.Keys;
//import org.openqa.selenium.PageLoadStrategy;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.logging.LogType;
//import org.openqa.selenium.logging.LoggingPreferences;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.web.client.RestTemplate;
//import service.smsActivation.SmsActivate;
//import util.ProxyCustomizer;
//
//import java.awt.*;
//import java.awt.event.InputEvent;
//import java.awt.event.KeyEvent;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.logging.Level;
//
//import static util.IConstants.YOUTUBE_ACCS_PASSWORD;
//import static util.SeleniumUtils.*;
//
//public class AdsPowerRegistrateYoutubeUtil {
//    private static final RestTemplate localRestTemplate = new RestTemplate();
//    private static final String BASE_URL = "http://local.adspower.com:50325/";
//
//    public static void main(String[] args) throws Exception {
//        System.setProperty("webdriver.http.factory", "jdk-http-client");
//        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
//        DbConnection dbConnection = DbConnection.getInstance();
//        SmsActivate smsActivate = SmsActivate.getInstance();
//        for (final VkSerfingAccount account : dbConnection.getAllVkSerfingAccounts()) {
//            VkAccount vkAccount = account.getVkAccount();
//            if (vkAccount.getYoutubeAccount() != null) {
//                continue;
//            }
//            final ChromeDriver driver;
//            final RestTemplate restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
//            vkAccount.setRestTemplate(restTemplate);
//            final String accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=" + vkAccount.getBrowserProfile()
//                    + "&ip_tab=0&headless=0&open_tabs=1", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();
//            final JSONObject accParamsJson = new JSONObject(accParams);
//            final JSONObject seleniumConnectionParams = accParamsJson.getJSONObject("data");
//            System.setProperty("webdriver.chrome.driver", seleniumConnectionParams.getString("webdriver")); // Set the webdriver returned by the API
//            final ChromeOptions options = new ChromeOptions();
//            options.setPageLoadStrategy(PageLoadStrategy.NONE);
//            options.setExperimentalOption("debuggerAddress", seleniumConnectionParams.getJSONObject("ws").getString("selenium"));
//            final LoggingPreferences logPrefs = new LoggingPreferences();
//            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
//            options.setCapability("goog:loggingPrefs", logPrefs);
//            driver = new ChromeDriver(options);
//            updatePersonalInfoForVkAcc(driver, vkAccount);
//            final String email = account.getEmail();
//
//            driver.manage().window().minimize();
//            driver.manage().window().maximize();
//            final Robot robot = new Robot();
//            openNewTab(robot);
//            typeString("https://www.youtube.com/", robot);
//            Thread.sleep(2000);
//            pressEnter(robot);
//            Thread.sleep(3500);
//            clickOn(1830, 97, robot);//Войти
//            Thread.sleep(3000);
//            clickOn(820, 719, robot);//Создать акк
//            Thread.sleep(getRandomNumber(2000, 2500));
//            clickOn(830, 767, robot);//Для лчного
//            Thread.sleep(getRandomNumber(1000, 1500));
//            typeRussianString(vkAccount.getFirstName(), robot);
//            pressEnter(robot);
//            Thread.sleep(getRandomNumber(1000, 1500));
//            typeRussianString(vkAccount.getLastName(), robot);
//            pressEnter(robot);
//            Thread.sleep(getRandomNumber(1000, 1500));
//            clickOn(806, 519, robot);
//            typeString(String.valueOf(vkAccount.getDayOfBirth()), robot);
//            pressKey(KeyEvent.VK_TAB, robot);
//            Thread.sleep(300);
//            pressEnter(robot);
//            for (int index = 0; index < vkAccount.getMonthOfBirth(); index++) {
//                Thread.sleep(getRandomNumber(200, 450));
//                robot.keyPress(KeyEvent.VK_DOWN);
//                robot.keyRelease(KeyEvent.VK_DOWN);
//            }
//            Thread.sleep(300);
//            pressEnter(robot);
//            Thread.sleep(300);
//            pressKey(KeyEvent.VK_TAB, robot);
//            typeString(String.valueOf(vkAccount.getYearOfBirth()), robot);
//            Thread.sleep(300);
//            pressKey(KeyEvent.VK_TAB, robot);
//            pressEnter(robot);
//            Thread.sleep(getRandomNumber(200, 450));
//            robot.keyPress(KeyEvent.VK_DOWN);
//            robot.keyRelease(KeyEvent.VK_DOWN);
//            if (vkAccount.getSex().startsWith("Муж")) {
//                Thread.sleep(getRandomNumber(200, 450));
//                robot.keyPress(KeyEvent.VK_DOWN);
//                robot.keyRelease(KeyEvent.VK_DOWN);
//            }
//            Thread.sleep(300);
//            pressEnter(robot);
//            Thread.sleep(300);
//            clickOn(1099, 691, robot);
//            Thread.sleep(getRandomNumber(2000, 2500));
//            clickOn(888, 668, robot);//Создать электронный адрес Gmail
//            Thread.sleep(getRandomNumber(1000, 1500));
//            if (copyTextFromScreen(783, 416, 1019, 440, robot).contains("Выберите адрес электронной почты Gmail или создайте свой")) {
//                clickOn(890, 502, robot);
//                clickOn(955, 698, robot);
//            } else {
//                String copiedText;
//                String gmailPrefix = email.split("@")[0].replaceAll("\\d", "");
//                if (gmailPrefix.endsWith(".")) {
//                    gmailPrefix = gmailPrefix.substring(0, gmailPrefix.length() - 1);
//                }
//                boolean isFirstTime = true;
//                do {
//                    if (isFirstTime) {
//                        clickOn(1009, 539, robot);
//                        typeString(gmailPrefix, robot);
//                        Thread.sleep(300);
//                        clickOn(956, 643, robot);//Далее
//                        isFirstTime = false;
//                    } else {
//                        clickOn(1030, 512, robot);
//                        for (int index = 0; index < gmailPrefix.length(); index++) {
//                            Thread.sleep(50);
//                            robot.keyPress(KeyEvent.VK_BACK_SPACE);
//                            Thread.sleep(50);
//                            robot.keyRelease(KeyEvent.VK_BACK_SPACE);
//                        }
//                        typeString(gmailPrefix, robot);
//                        Thread.sleep(300);
//                        clickOn(951, 681, robot);//Далее
//                    }
//                    Thread.sleep(1000, 1500);
//                    copiedText = copyTextFromScreen(777, 612, 837, 612, robot);
//                } while (!copiedText.contains("Доступно"));
//
//                clickOn(900, 608, robot);
//                Thread.sleep(300);
//                pressKey(KeyEvent.VK_TAB, robot);
//                pressEnter(robot);
//            }
//            Thread.sleep(getRandomNumber(2000, 2500));
//            typeString(YOUTUBE_ACCS_PASSWORD, robot);
//            Thread.sleep(300);
//            pressKey(KeyEvent.VK_TAB, robot);
//            typeString(YOUTUBE_ACCS_PASSWORD, robot);
//            clickOn(1094, 728, robot);
//
//            SmsActivationResponse smsActivationResponse = smsActivate.purchaseNumber("go", "tele2", null);
//            typeString(smsActivationResponse.getPhone(), robot);
//            clickOn(1092, 676, robot);
//            String verificationCode = smsActivate.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 1);
//            System.out.println(verificationCode);
//            if (verificationCode == null) {
//                throw new Exception("Verfication code is null!!!");
//            }
//            typeString(verificationCode, robot);
//            Thread.sleep(2000);
//            clickOn(1047, 705, robot);
//            Thread.sleep(2000);
//            clickOn(1059, 633, robot);
//            Thread.sleep(2000);
//            clickOn(1110, 591, robot);
//            final YoutubeAccount youtubeAccount = new YoutubeAccount();
//            youtubeAccount.setPhone(smsActivationResponse.getPhone());
//            youtubeAccount.setVkAccId(vkAccount.getId());
//            smsActivate.finishRequest(smsActivationResponse.getId());
//
//            ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
//            driver.switchTo().window(windows.get(windows.size() - 1));
//            while (true) {//v rot ebal vashi formachki
//                try {
//                    clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Пропустить", 5));
//                } catch (Exception e) {
//                    try {
//                        clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Далее", 5));
//                    } catch (Exception e2) {
//                        try {
//                            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "button", "Другие варианты", 5));
//                            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "div", "Добавить мой номер телефона только в целях безопасности аккаунта", 5));
//                            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Готово", 5));
//                        } catch (Exception e3) {
//                            break;
//                        }
//                    }
//                }
//            }
//            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Принимаю", 5));
//            clickElement(driver, waitUntilElementWillBePresent(driver, "button[@id='avatar-btn']"));
//            youtubeAccount.setGmail(waitUntilElementWillBePresent(driver, "yt-formatted-string[@id='email']").getAttribute("title"));
//            dbConnection.saveYouTubeAcc(youtubeAccount);
//            driver.get("https://www.youtube.com/account_privacy");
//            clickElement(driver, waitUntilElementWillBePresent(driver, "div[@id='toggleButton']"));
//
//            if (true) {
//                return;
//            }
////            driver.get("https://www.youtube.com/");
////            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Войти"));
////            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Создать аккаунт"));
////            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Для личного использования"));
////            clickAndFillField(driver, "input[@name='firstName']", vkAccount.getFirstName());
////            Thread.sleep(getRandomNumber(500, 1000));
////            clickAndFillField(driver, "input[@name='lastName']", vkAccount.getLastName());
////            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Далее"));
////            clickAndFillField(driver, "input[@name='day']", String.valueOf(vkAccount.getDayOfBirth()));
////            WebElement monthCombo = clickElement(driver, "select[@id='month']");
////            for (int index = 0; index < vkAccount.getMonthOfBirth(); index++) {
////                Thread.sleep(getRandomNumber(200, 450));
////                monthCombo.sendKeys(Keys.ARROW_DOWN);
////            }
////            Thread.sleep(getRandomNumber(200, 450));
////            monthCombo.sendKeys(Keys.ENTER);
////            clickAndFillField(driver, "input[@name='year']", String.valueOf(vkAccount.getYearOfBirth()));
////
////            WebElement genderCombo = clickElement(driver, "select[@id='gender']");
////            String sexPrefix = vkAccount.getSex().substring(0, 3);
////            System.out.println(sexPrefix);
////            for (WebElement genderOption : genderCombo.findElements(By.xpath("./option"))) {
////                String genderOptionText = genderOption.getText();
////                System.out.println(genderOptionText);
////                if (StringUtils.isNotEmpty(genderOptionText)) {
////                    Thread.sleep(getRandomNumber(100, 350));
////                    genderCombo.sendKeys(Keys.ARROW_DOWN);
////                    if (genderOptionText.startsWith(sexPrefix)){
////                        genderCombo.sendKeys(Keys.ENTER);
////                        break;
////                    }
////                }
////            }
////
////            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Далее"));
////            clickAndFillField(driver, "input[@type='email']", account.getEmail());
////            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Далее"));
////            final String verificationCode = processMailByTitle(driver, "Подтвердите адрес электронной почты", mailTab -> waitUntilElementWhichContainsTextWillBePresent(mailTab, "p", "Используйте этот код, чтобы подтвердить, что адрес принадлежит Вам.")
////                    .findElement(By.xpath("./../div"))
////                    .getText());
////            System.out.println(verificationCode);
////            clickAndFillField(driver, "input[@id='code']", verificationCode);
////            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Далее"));
////            clickAndFillField(driver, "input[@name='Passwd']", account.getPassword());
////            clickAndFillField(driver, "input[@name='PasswdAgain']", account.getPassword());
////            clickElement(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "span", "Далее"));
//
//        }
//    }
//}
