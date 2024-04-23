package service;

import bo.*;
import da.DbConnection;
import org.json.JSONObject;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import service.smsActivation.SmsActivate;
import util.ProxyCustomizer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.logging.Level;

import static util.IConstants.YOUTUBE_ACCS_PASSWORD;
import static util.SeleniumUtils.*;
import static util.SeleniumUtils.waitUntilElementWillBePresent;

public class AdsPowerRegistratePayUpVideoUtil {
    private static final RestTemplate localRestTemplate = new RestTemplate();
    private static final String BASE_URL = "http://local.adspower.com:50325/";

    public static void main(String[] args) throws Exception {
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        DbConnection dbConnection = DbConnection.getInstance();
        for (final VkSerfingAccount account : dbConnection.getAllVkSerfingAccounts()) {
            VkAccount vkAccount = account.getVkAccount();
            if (vkAccount.getPayUpVideoAcc() != null) {
                continue;
            }
            final ChromeDriver driver;
            final RestTemplate restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
            vkAccount.setRestTemplate(restTemplate);
            final String accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=" + vkAccount.getBrowserProfile()
                    + "&ip_tab=0&headless=0&open_tabs=1", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();
            final JSONObject accParamsJson = new JSONObject(accParams);
            final JSONObject seleniumConnectionParams = accParamsJson.
                    getJSONObject("data");
            System.setProperty("webdriver.chrome.driver", seleniumConnectionParams.getString("webdriver")); // Set the webdriver returned by the API
            final ChromeOptions options = new ChromeOptions();
            options.setPageLoadStrategy(PageLoadStrategy.NONE);
            options.setExperimentalOption("debuggerAddress", seleniumConnectionParams.getJSONObject("ws").getString("selenium"));
            final LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
            options.setCapability("goog:loggingPrefs", logPrefs);
            driver = new ChromeDriver(options);
            final String email = account.getEmail();

            driver.manage().window().minimize();
            driver.manage().window().maximize();
            final Robot robot = new Robot();
            openNewTab(robot);
            typeString("https://payup.video/", robot);
            Thread.sleep(1000);
            pressEnter(robot);
//            Thread.sleep(getRandomNumber(2500, 5000));
//            clickOn(getRandomNumber(713, 1191), getRandomNumber(567, 603), robot);
//            Thread.sleep(getRandomNumber(500, 2000));
//            typeString(email, robot);
//            Thread.sleep(getRandomNumber(500, 2000));
//            clickOn(getRandomNumber(715, 1191), getRandomNumber(678, 709), robot);
//            Thread.sleep(getRandomNumber(500, 2000));
//            typeString("X2uKGRYhn9KvRzXP9UhL", robot);
//            clickOn(getRandomNumber(723, 741), getRandomNumber(777, 795), robot);
            final PayUpVideoAcc payUpVideoAcc = new PayUpVideoAcc();
            payUpVideoAcc.setVkAccount(vkAccount);
            dbConnection.savePayUpVideoAcc(payUpVideoAcc);
            System.out.println(vkAccount.getPayeerAccount().getAccName());
            return;
        }
    }
}
