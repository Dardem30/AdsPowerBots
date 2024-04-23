package withdraw;

import bo.PayeerAccount;
import da.DbConnection;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.logging.Level;

import static util.SeleniumUtils.*;

public class AdsPowerPayeerWithdraw {

    private static final String BASE_URL = "http://local.adspower.com:50325/";

    public static void main(String[] args) throws Exception {
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        final RestTemplate localRestTemplate = new RestTemplate();
        final Robot robot = new Robot();
        final DbConnection dbConnection = DbConnection.getInstance();
        int totalInvoiced = 0;
        for (final PayeerAccount payeerAccount : dbConnection.getPayeerAccsToInvoice()) {
            final ChromeDriver driver;
            try {
                final String accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=" + payeerAccount.getVkAccount().getBrowserProfile()
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
                Thread.sleep(1000);
                continue;
            }
            driver.manage().window().minimize();
            driver.manage().window().maximize();

            openNewTab(robot);
            Thread.sleep(300);
            robot.keyPress(KeyEvent.VK_BACK_SPACE);
            Thread.sleep(300);
            typeString("https://payeer.com/ru/auth/", robot);
            Thread.sleep(300);
            pressDelete(robot);
            Thread.sleep(300);
            pressEnter(robot);
            Thread.sleep(2000);
            clickOn(786, 564, robot);
            Thread.sleep(300);
            typeString(payeerAccount.getAccName(), robot);
            Thread.sleep(300);
            clickOn(771, 660, robot);
            Thread.sleep(300);
            typeString(payeerAccount.getPassword(), robot);
            Thread.sleep(300);
            clickOn(953,761, robot);
            clickOn(953,761, robot);
            Thread.sleep(4000);
            clickOn(836,636, robot);
            clickOn(836,636, robot);
            Thread.sleep(1000);
            clickOn(929,727, robot);
            clickOn(929,727, robot);
            Thread.sleep(1000);
            clickOn(121, 183, robot);
            try {
                ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(windows.get(1));
                try {
                    waitUntilElementWillBePresent(driver, "button[@class='login-form__login-btn step1']", 4).click();
                } catch (Exception ignored) {
                }
                final Integer balance = Integer.valueOf(waitUntilElementWillBePresent(driver, "div[@class='balance__cache curr-RUB']/span[@class='int']").getText());
                System.out.println("Payeer account " + payeerAccount.getId() + " current balance is " + balance);
                if (balance > 10) {
                    driver.findElement(By.xpath("//i[@class='icon-transfer']")).click();
                } else {
                    dbConnection.markPayeerAsInvoiced(payeerAccount.getId(), payeerAccount.getBalance().intValue());
                    localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + payeerAccount.getVkAccount().getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                    continue;
                }
                waitUntilElementWillBePresent(driver, "input[@name='param_ACCOUNT_NUMBER']").sendKeys("P1084286324");
                final WebElement currencyReceiveSelector = waitUntilElementWillBePresent(driver, "select[@name='curr_receive']/./..");
                currencyReceiveSelector.click();
                try {
                    currencyReceiveSelector.findElement(By.xpath(".//div[@class='jq-selectbox__dropdown']/ul/li[text()='RUB']")).click();
                } catch (Exception ignored) {
                }//RUB was selected before
                WebElement receiveAmount = driver.findElement(By.xpath("//input[@name='sum_receive']"));
                receiveAmount.clear();
                receiveAmount.sendKeys(String.valueOf(balance - 2));
                Thread.sleep(1000);
                waitUntilElementWillBePresent(driver, "button[@class='btn n-form--btn n-form--btn-mod']").click();
                for (int index = 0; index < payeerAccount.getMasterKey().length(); index++) {
                    waitUntilElementWithTextWillBePresent(driver, "a", String.valueOf(payeerAccount.getMasterKey().charAt(index))).click();
                }
                waitUntilElementWillBePresent(driver, "a[@class='ok button_green2']").click();
                dbConnection.markPayeerAsInvoiced(payeerAccount.getId(), (int) (payeerAccount.getBalance() - balance - 2));
                System.out.println("Account payeer " + payeerAccount.getId() + " successfully invoiced for sum " + (balance - 1));
                totalInvoiced += balance - 2;
                Thread.sleep(10000);
                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + payeerAccount.getVkAccount().getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
            } catch (Exception e) {
                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + payeerAccount.getVkAccount().getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
            }
        }
        System.out.println("Finished invoicing. Total sum: " + totalInvoiced);
    }
}
