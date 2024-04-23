import bo.VkAccount;
import da.DbConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import service.smsActivation.FiveSimService;
import service.smsActivation.SmsActivate;
import service.smsActivation.SmsRegService;
import util.ProxyCustomizer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static util.SeleniumUtils.typeRussianString;
import static util.SeleniumUtils.typeString;

public class Test {
    private static final String BASE_URL = "http://local.adspower.com:50325/";
    private static final RestTemplate localRestTemplate = new RestTemplate();
    public static void main(String[] args) throws Exception {
        final Robot robot = new Robot();

        typeRussianString("Соловьеваё", robot);

//        System.setProperty("webdriver.http.factory", "jdk-http-client");
//        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
//
//        VkAccount vkAccount = DbConnection.getInstance().getVkAccById(505);
//        final ChromeDriver driver;
//        final Date now = new Date();
//        final RestTemplate restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
//        vkAccount.setRestTemplate(restTemplate);
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
//        driver.get("https://v-like.ru/account/withdraws");
//        Thread.sleep(2000);
//        String innerText = driver.findElement(By.xpath("//span[text()='Штрафы']")).getAttribute("innerText");
//        System.out.println();
//
//        Robot robot = new Robot();
//        openNewTab(robot);
//        Thread.sleep(1000);
//        robot.keyPress(KeyEvent.VK_BACK_SPACE);
//        Thread.sleep(300);
//        robot.keyRelease(KeyEvent.VK_BACK_SPACE);
//        Thread.sleep(300);
//        typeString("https://payeer.com/ru/auth/?register=yes", robot);

//        try {
//            FiveSimService fiveSimService = FiveSimService.getInstance();
//            fiveSimService.finishRequest(471922197);
//        } catch (HttpResponseException e) {
//            System.out.println(e.getMessage());
//        }

////        SmsActivate smsActivate = SmsActivate.getInstance();
////        SmsRegService smsRegService = SmsRegService.getInstance();
//        while (true) {
//            Thread.sleep(10000);
//            try {
//                fiveSimService.canPurchaseCallResetProduct();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }
    private static void openNewTab(Robot robot) throws Exception {
        robot.keyPress(KeyEvent.VK_CONTROL);
        Thread.sleep(300);
        robot.keyPress(KeyEvent.VK_T);
        Thread.sleep(300);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        Thread.sleep(300);
        robot.keyRelease(KeyEvent.VK_T);
        Thread.sleep(1000);
    }

    private static void typeString(String string, final Robot robot) throws Exception {

        //Looping through every char
        for (int i = 0; i < string.length(); i++) {
            //Getting current char
            char c = string.charAt(i);
            if (StringUtils.isEmpty(String.valueOf(c))) {
                return;
            }
            try {

                if (c == '@') {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_2);
                    robot.keyRelease(KeyEvent.VK_2);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                } else if (c == ':') {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_SEMICOLON);
                    robot.keyRelease(KeyEvent.VK_SEMICOLON);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                } else if (c == '?') {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_SLASH);
                    robot.keyRelease(KeyEvent.VK_SLASH);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                } else if (c == '.') {
                    robot.keyPress(KeyEvent.VK_PERIOD);
                    robot.keyRelease(KeyEvent.VK_PERIOD);
                } else {
                    //Pressing shift if it's uppercase
                    if (Character.isUpperCase(c)) {
                        robot.keyPress(KeyEvent.VK_SHIFT);
                    }
                    //Actually pressing the key
                    robot.keyPress(Character.toUpperCase(c));
                    robot.keyRelease(Character.toUpperCase(c));
                    //Releasing shift if it's uppercase
                    if (Character.isUpperCase(c)) {
                        robot.keyRelease(KeyEvent.VK_SHIFT);
                    }
                }


            } catch (Exception e) {

            }
            //Optional delay to make it look like it's a human typing
            Thread.sleep(50);
        }
    }
    private static String getFacebookHtml(final String link) throws Exception {
        final URLConnection connection = new URL(link).openConnection();
        connection.setRequestProperty("Cookie", "sb=8UgIZOXFdHEO4-_SWHdm03hF; datr=80gIZF3UpJftAkIWrJSy8NAF; c_user=100017201528846; xs=2%3AgxpCM1taFBAJrw%3A2%3A1678264578%3A-1%3A16234%3A%3AAcUHRfqc1sLYqVIksD5lJ9FWfi1SZ-4XZWzTqEADo3c; fr=0eKdwMfsQNTH9NsI4.AWXuAttc6Ar0QjTe73fZ97a3zcM.BkPWLQ.9m.AAA.0.0.BkPWLQ.AWWUxvP1Yq0; presence=C%7B%22t3%22%3A%5B%5D%2C%22utc3%22%3A1681744600656%2C%22v%22%3A1%7D; wd=1365x937");
        final Scanner scanner = new Scanner(connection.getInputStream());
        scanner.useDelimiter("\\Z");
        final String profileHtml = scanner.next();
        scanner.close();
        return profileHtml;
    }
    public static void removeLine(String lineContent) throws IOException
    {
        File file = new File("forge/proxies.txt");
        List<String> out = Files.lines(file.toPath())
                .filter(line -> !line.contains(lineContent))
                .collect(Collectors.toList());
        Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
