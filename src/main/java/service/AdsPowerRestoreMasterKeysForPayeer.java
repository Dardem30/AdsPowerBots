//package service;
//
//import bo.PayeerAccount;
//import da.DbConnection;
//import org.json.JSONObject;
//import org.openqa.selenium.By;
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
//
//import java.awt.*;
//import java.awt.event.KeyEvent;
//import java.util.ArrayList;
//import java.util.logging.Level;
//
//import static util.SeleniumUtils.*;
//import static util.SeleniumUtils.copyTextFromScreen;
//
//public class AdsPowerRestoreMasterKeysForPayeer {
//    private static final String BASE_URL = "http://local.adspower.com:50325/";
//
//    public static void main(String[] args) throws Exception {
//        System.setProperty("webdriver.http.factory", "jdk-http-client");
//        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
//        final RestTemplate localRestTemplate = new RestTemplate();
//        final DbConnection dbConnection = DbConnection.getInstance();
//        final Robot robot = new Robot();
//        for (final PayeerAccount payeerAccount : dbConnection.getAllPayeerAccs()) {
//            if (payeerAccount.getMasterKey() != null) {
//                continue;
//            }
//            try {
//                final String accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=" + payeerAccount.getVkAccount().getBrowserProfile()
//                        + "&ip_tab=0&headless=0&open_tabs=1", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();
//                final JSONObject accParamsJson = new JSONObject(accParams);
//                final JSONObject seleniumConnectionParams = accParamsJson.getJSONObject("data");
//                System.setProperty("webdriver.chrome.driver", seleniumConnectionParams.getString("webdriver")); // Set the webdriver returned by the API
//                final ChromeOptions options = new ChromeOptions();
//                options.setPageLoadStrategy(PageLoadStrategy.NONE);
//                options.setExperimentalOption("debuggerAddress", seleniumConnectionParams.getJSONObject("ws").getString("selenium"));
//                final LoggingPreferences logPrefs = new LoggingPreferences();
//                logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
//                options.setCapability("goog:loggingPrefs", logPrefs);
//                final ChromeDriver driver = new ChromeDriver(options);
//                driver.manage().window().minimize();
//                driver.manage().window().maximize();
//
//                openNewTab(robot);
//                Thread.sleep(300);
//                robot.keyPress(KeyEvent.VK_BACK_SPACE);
//                Thread.sleep(300);
//                typeString("https://payeer.com/ru/auth/", robot);
//                Thread.sleep(300);
//                pressEnter(robot);
//                Thread.sleep(5000);
//                if (copyTextFromScreen(348, 307, 995, 309, robot).contains("ПОЖАЛУЙСТА, СОХРАНИТЕ ДАННЫЕ АВТОРИЗАЦИИ В НАДЕЖНОМ МЕСТЕ")) {
//                    switchToLastWindow(driver);
//                    String masterKey = waitUntilElementWhichContainsTextWillBePresent(driver, "div", "Master key:", 30).findElement(By.xpath("./..")).findElement(By.xpath(".//div[@class='info-security__data']")).getText();
//                    payeerAccount.setMasterKey(masterKey);
//                    dbConnection.savePayeerAccount(payeerAccount);
//                    localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + payeerAccount.getVkAccount().getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
//                    continue;
//                }
//                clickOn(1088, 658, robot);
//                Thread.sleep(1000);
//                typeString(payeerAccount.getAccName(), robot);
//                pressKey(KeyEvent.VK_TAB, robot);
//                Thread.sleep(300);
//                typeString(payeerAccount.getSecretCode(), robot);
//                clickOn(945, 744, robot);
//                Thread.sleep(3500);
//                if (copyTextFromScreen(794, 330, 1056, 365, robot).contains("Введите email, номер счета или мобильный телефон")) {
//                    clickOn(843, 637, robot);
//                    Thread.sleep(1500);
//                    clickOn(967, 773, robot);
//                }
//
//                switchToLastWindow(driver);
//                String newPassword = waitUntilElementWillBePresent(driver, "input[@name='new_password']").getAttribute("value");
//                payeerAccount.setPassword(newPassword);
//                clickElement(driver, waitUntilElementWillBePresent(driver, "button[@class='login-form__login-btn mini-mid-btn']"));
//                String masterKey = waitUntilElementWhichContainsTextWillBePresent(driver, "div", "Master key:", 30).findElement(By.xpath("./..")).findElement(By.xpath(".//div[@class='info-security__data']")).getText();
//                payeerAccount.setMasterKey(masterKey);
//                dbConnection.savePayeerAccount(payeerAccount);
//                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + payeerAccount.getVkAccount().getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
//            } catch (Exception e) {
//                //    localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + payeerAccount.getVkAccount().getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
//            }
//        }
//    }
//}
