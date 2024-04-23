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

import java.util.logging.Level;

import static util.SeleniumUtils.clickElement;
import static util.SeleniumUtils.switchToLastWindow;

public class AdsPowerTestService {
    private static final String BASE_URL = "http://local.adspower.com:50325/";
    private static final DbConnection dbConnection = DbConnection.getInstance();
    private static final RestTemplate localRestTemplate = new RestTemplate();
    private static final String TEST_PROFILE = "jctk3ym";

    public static void main(String[] args) throws Exception {
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        final String accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=" + TEST_PROFILE
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
        final ChromeDriver driver = new ChromeDriver(options);

        switchToLastWindow(driver);
        clickElement(driver, "mat-form-field");
    }
}