import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class AdsPowerServiceTest {
    private static final String BASE_URL = "http://local.adspower.com:50325/";
    private static final String API_KEY = "69d9110c6fd972cf985f90f4de1fe2fc";

    public static void main(String[] args) throws Exception {
        final RestTemplate localRestTemplate = new RestTemplate();
        localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + "j4p3ycj", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

//        final String accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=j4naeq8&ip_tab=0&headless=0", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();
//        final JSONObject accParamsJson = new JSONObject(accParams);
//        final JSONObject seleniumConnectionParams = accParamsJson.getJSONObject("data");
//        System.setProperty("webdriver.chrome.driver", seleniumConnectionParams.getString("webdriver")); // Set the webdriver returned by the API
//        final ChromeOptions options = new ChromeOptions();
//        options.setExperimentalOption("debuggerAddress", seleniumConnectionParams.getJSONObject("ws").getString("selenium"));
//        final LoggingPreferences logPrefs = new LoggingPreferences();
//        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
//        options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
//        final ChromeDriver driver = new ChromeDriver(options);
//
//        driver.manage().window().maximize();
//
//        WebElement element = driver.findElement(By.xpath("//a[@href='/cash']"));
//        String text = element.getText();
//        String s = text.replaceAll(" ₽", "");
//        float v = Float.parseFloat(s);
//        System.out.println();
//        driver.get("https://vkserfing.ru/assignments");
//
//        String intermediateLink = "/assignments/6234230/go";
//
//        skipTask(driver, intermediateLink);
//        Thread.sleep(5000);
//        int index = 0;
//        while (index != 1) {
//            if (index == 1) {
//                intermediateLink = "/assignments/6234230/go";
//            }
//            clickButton(driver, "a[@href='" + intermediateLink + "'][@class='task__do']");
//            index++;
//            Thread.sleep(4000);
////            Set<String> windowHandles = driver.getWindowHandles();
//            ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
//            if (windows.size() == 1) {
//                clickButton(driver, "a[@href='" + intermediateLink + "'][@class='form__btn btn btn--block']");
//                Thread.sleep(4000);
//                windows = new ArrayList<>(driver.getWindowHandles());
//            }
//            driver.switchTo().window(windows.get(1));
//            driver.close();
//            driver.switchTo().window(windows.get(0));
//            Thread.sleep(5000);
//            System.out.println();
//        }
    }

    private static void clickButton(final ChromeDriver driver, final String xpath) throws Exception {
        final WebElement button = waitUntilElementWillBePresent(driver, xpath);
        final Actions builder = new Actions(driver);
        Dimension size = button.getSize();
        builder.moveToElement(button, getRandomNumber(size.width / 2 * -1 + 1, size.width / 2 - 1), getRandomNumber(size.height / 2 * -1 + 1, size.height / 2 - 1))
                .pause(Duration.ofMillis(getRandomNumber(500, 1500)))
                .click().build().perform();
    }

    private static void clickButton(final ChromeDriver driver, final WebElement button) throws Exception {
        final Actions builder = new Actions(driver);
        Dimension size = button.getSize();
        builder.moveToElement(button, getRandomNumber(size.width / 2 * -1 + 1, size.width / 2 - 1), getRandomNumber(size.height / 2 * -1 + 1, size.height / 2 - 1))
                .pause(Duration.ofMillis(getRandomNumber(1000, 1500)))
                .click().build().perform();
    }

    private static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
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

    private static WebElement waitUntilElementWillBePresentWithoutTimeout(final ChromeDriver driver, final String xpath) throws Exception {
        final By searchCriteria = By.xpath("//" + xpath);
        new WebDriverWait(driver, Duration.ofSeconds(1200)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        Thread.sleep(2000);
        return driver.findElement(searchCriteria);
    }


    private static void beginTask(final ChromeDriver driver, final String intermediateLink) throws Exception {
        clickButton(driver, "a[@href='" + intermediateLink + "'][@class='task__do']");
        Thread.sleep(getRandomNumber(1000, 2000));
        ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
        if (windows.size() == 1) {
            clickButton(driver, "a[@href='" + intermediateLink + "'][@class='form__btn btn btn--block']");
            Thread.sleep(getRandomNumber(1000, 2000));
            windows = new ArrayList<>(driver.getWindowHandles());
        }
        driver.switchTo().window(windows.get(1));
        driver.close();
        driver.switchTo().window(windows.get(0));
    }

    private static void submitTask(final ChromeDriver driver, final String intermediateLink) throws Exception {
        clickButton(driver, "a[@href='" + intermediateLink + "'][@class='task__do']");
    }


    private static void skipTask(final ChromeDriver driver, final String intermediateLink) throws Exception {
        WebElement performTaskButton = waitUntilElementWillBePresent(driver, "a[@href='" + intermediateLink + "'][@class='task__do']");
        WebElement taskBottomPanel = performTaskButton.findElement(By.xpath("./.."));
        WebElement taskGroup = taskBottomPanel.findElement(By.xpath("./.."));
        List<WebElement> options = taskGroup.findElements(By.xpath("./div[@class='task__top']/ul/li[@class='card-menu__item']"));
        final Actions builder = new Actions(driver);
        Dimension size = options.get(1).getSize();
        builder.moveToElement(options.get(1), getRandomNumber(size.width / 3 * -1 + 2, size.width / 3 - 2), getRandomNumber(size.height / 3 * -1 + 2, size.height / 3 - 2))
                .pause(Duration.ofMillis(getRandomNumber(1000, 1500)))
                .click().build().perform();
        WebElement messageBlock = waitUntilElementWithTextWillBePresent(driver, "div", "Вы уверены что хотите скрыть данное задание?");
        WebElement messageContainer = messageBlock.findElement(By.xpath("./.."));
        List<WebElement> yesNoOptions = messageContainer.findElements(By.xpath("./div[@class='row']/div[@class='col-6']"));
        WebElement confirmButton = yesNoOptions.get(0).findElement(By.xpath("./a"));
        clickButton(driver, confirmButton);
    }
}
