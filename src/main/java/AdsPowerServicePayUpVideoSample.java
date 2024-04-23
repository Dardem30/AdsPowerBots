import bo.PayUpVideoAcc;
import bo.VkAccount;
import bo.VkSerfingAccount;
import da.DbConnection;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import service.RuCaptcha;
import service.VkService;
import util.ExecuteService;
import util.ProxyCustomizer;

import java.net.http.HttpTimeoutException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static util.SeleniumUtils.*;

public class AdsPowerServicePayUpVideoSample {
    private static final String BASE_URL = "http://local.adspower.com:50325/";
    private static final Map<Integer, Map<String, Integer>> failedTasks = new HashMap<>();
    private static final DbConnection dbConnection = DbConnection.getInstance();
    private static final RestTemplate localRestTemplate = new RestTemplate();

    //15000000 ~ 1h 10min
    public static void main(String[] args) throws Exception {
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        final VkService vkService = VkService.getInstance();
        //    final TimeZone timeZone = TimeZone.getDefault();
        //   int rawOffset = timeZone.getRawOffset();
        final ExecuteService executeService = ExecuteService.getInstance();
        while (true) {
//            final boolean isNight = new Date().getHours() < 10;
            final boolean isNight = true;
            final List<VkSerfingAccount> allVkSerfingAccounts;
            if (isNight) {
                allVkSerfingAccounts = dbConnection.getAllVkSerfingAccounts()
                        .stream()
                        .filter(record -> record.getVkAccount().getPayUpVideoAcc() != null)
                        .collect(Collectors.toList());
            } else {
                allVkSerfingAccounts = dbConnection.getAllVkSerfingAccounts();
            }
            final Date startExecution = new Date();
            executeService.execute(allVkSerfingAccounts, (accounts, index) -> {
                try {
                    // Thread.sleep(1000L * (index - 1));
//                    Thread.sleep((long) getRandomNumber(10000, 20000) * (index - 1));
                    Thread.sleep((long) getRandomNumber(60000, 120000) * (index - 1));
//                    Thread.sleep((long) getRandomNumber(30000, 60000) * (index - 1));
//                    Thread.sleep((long) 2000 * (index - 1));
                } catch (Exception e) {

                }
                for (int accIndex = 0; accIndex < accounts.size(); accIndex++) {
                    final VkSerfingAccount account = accounts.get(accIndex);
                    final VkAccount vkAccount = account.getVkAccount();
                    if (vkAccount.getId() != 554
                    ) {
                        continue;
                    }
                    vkAccount.setAvailableSubscribes(getRandomNumber(1, 5));
                    // vkAccount.setAvailableSubscribes(0);
                    //   vkAccount.setAvailableShares(0);
                    vkAccount.setAvailableFriendRequests(getRandomNumber(-10, 1));
                    vkAccount.setAvailableShares(getRandomNumber(-5, 1));
                    //   vkAccount.setAvailableFriendRequests(getRandomNumber(0, 1));
                    try {

                        final ChromeDriver driver;
                        final Date now = new Date();
                        final RestTemplate restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
                        vkAccount.setRestTemplate(restTemplate);
                        try {
                            final String accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=" + vkAccount.getBrowserProfile()
                                    + "&ip_tab=0&headless=0&open_tabs=1", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();
                            final JSONObject accParamsJson = new JSONObject(accParams);
                            final JSONObject seleniumConnectionParams = accParamsJson.getJSONObject("data");
                            System.setProperty("webdriver.chrome.driver", seleniumConnectionParams.getString("webdriver")); // Set the webdriver returned by the API
                            final ChromeOptions options = new ChromeOptions();
                            options.setPageLoadStrategy(PageLoadStrategy.NONE);
                            options.setExperimentalOption("debuggerAddress", seleniumConnectionParams.getJSONObject("ws").getString("selenium"));
                            final LoggingPreferences logPrefs = new LoggingPreferences();
                            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
                            options.setCapability("goog:loggingPrefs", logPrefs);
                            driver = new ChromeDriver(options);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Failed to start browser for " + vkAccount.getId());
                            try {
                                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            accIndex--;
                            try {
                                Thread.sleep(getRandomNumber(500, 1000));
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            continue;
                        }
                        System.out.println("Start processing acc " + vkAccount.getId() + " " + now);

                        if (!isNight) {
                            boolean skip = false;
                            while (true) {
                                try {
                                    if (!vkService.refreshCookie(driver, vkAccount, restTemplate, false)) {
                                        localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                                        skip = true;
                                    }
                                    break;
                                } catch (HttpTimeoutException e) {
                                    System.out.println(e.getClass().getSimpleName() + " - going to retry refresh cookie");
                                }
                            }
                            if (skip) {
                                continue;
                            }
                        }

                        final AtomicBoolean jobIsFinished = new AtomicBoolean(false);
                        final Thread execution = new Thread(() -> {
                            try {
                                final List<Runnable> operations = new ArrayList<>(3);
                                operations.add(() -> {
                                    try {
                                        processPayUpVideoAcc(driver, account);
                                    } catch (Exception e) {
                                        System.out.println("Failed to process payup acc [" + account.getVkAccount().getId() + "]");
                                        e.printStackTrace();
                                    }
                                });
                                for (final Runnable operation : operations) {
                                    operation.run();
                                    closeAllTabsExceptFirstOne(driver);
                                }
                                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                                jobIsFinished.set(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                                jobIsFinished.set(true);
                            }
                        });
                        execution.start();
                        int tick = 0;
                        while (!jobIsFinished.get()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            tick++;
                            if (tick > 1800) {
                                execution.interrupt();
                                driver.quit();
                                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                                System.out.println("Processing vk acc [" + vkAccount.getId() + "] took more than 30 min!!!!!");
                                jobIsFinished.set(true);
                            }

                        }
                        //Thread.sleep(getRandomNumber(60000, 120000));
                    } catch (Exception e) {
                        System.out.println("Failed to process tasks for " + vkAccount.getId());
                        e.printStackTrace();
                        try {
                            localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                        } catch (Exception ex) {

                        }
                    }
                }
                //}, 5);
            }, 99);
            System.out.println("Finished processing all accs at " + new Date() + " started at " + startExecution);
//            System.exit(0);
//            if (true) {
//                return;
//            }

            final Date now = new Date();
            if (now.getHours() < 10) {
                Thread.sleep(getRandomNumber(3600000, 7200000)); //1-2h
            } else {
                Thread.sleep(getRandomNumber(120000, 240000)); //2-4min
            }
            //Thread.sleep(getRandomNumber(3600000, 7200000)); //1-2h
            // Thread.sleep(getRandomNumber(1200000, 2400000)); //20-40min
            //  Thread.sleep(getRandomNumber(14400000, 15400000)); //4-4.33h
        }
    }

    private static void processPayUpVideoAcc(final ChromeDriver driver,
                                             final VkSerfingAccount account) throws Exception {
        driver.get("file:///C:/work/VkTargetBots/test.html");
        driver.manage().window().maximize();
        WebElement captchaNoteDiv = waitUntilElementWillBePresent(driver, "div[contains(text(), 'Verify you are human:')]");
        if (Integer.parseInt(captchaNoteDiv.getDomProperty("offsetWidth")) != 0) {
            try {
                List<WebElement> captchaElements = captchaNoteDiv.findElement(By.xpath("./.."))
                        .findElements(By.xpath(".//*"))
                        .get(1)
                        .findElements(By.xpath(".//*"))
                        .get(0)
                        .findElements(By.xpath(".//*"))
                        .get(0)
                        .findElements(By.xpath(".//*"));
                if (captchaElements.isEmpty()) {
                    captchaElements = captchaNoteDiv.findElement(By.xpath("./.."))
                            .findElements(By.xpath(".//*"))
                            .get(1)
                            .findElements(By.xpath(".//*"))
                            .get(0)
                            .findElements(By.xpath(".//*"));
                }
                WebElement captcha = null;
                for (final WebElement captchaElement : captchaElements) {
                    if (Integer.parseInt(captchaElement.getDomProperty("offsetWidth")) > 100) {
                        captcha = captchaElement;
                        break;
                    }
                }
                final String captchaBase64Image = captcha.getCssValue("background");
                RuCaptcha.CaptchaAnswer answer = new RuCaptcha.CaptchaAnswer();
                answer.addCoordinates("coordinates:x=103,y=49");
//                RuCaptcha.CaptchaAnswer answer = RuCaptcha.getInstance().solveCoordinatesCaptcha(
//                        captchaBase64Image.split("base64,")[1].split("\"\\)")[0]
//                );
                for (Map<String, Integer> coordinate : answer.getCoordinates()) {
                    clickElement(driver, captcha, coordinate.get("x"), coordinate.get("y"));
                }
                System.out.printf("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
