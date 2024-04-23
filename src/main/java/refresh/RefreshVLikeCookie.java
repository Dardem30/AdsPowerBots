//package refresh;
//
//import bo.VLikeAccount;
//import bo.VkAccount;
//import da.DbConnection;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.logging.LogEntries;
//import org.openqa.selenium.logging.LogEntry;
//import org.openqa.selenium.logging.LogType;
//import org.openqa.selenium.logging.LoggingPreferences;
//import org.openqa.selenium.remote.CapabilityType;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.web.client.RestTemplate;
//
//import java.awt.*;
//import java.awt.datatransfer.DataFlavor;
//import java.awt.event.InputEvent;
//import java.awt.event.KeyEvent;
//import java.io.File;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Level;
//
//public class RefreshVLikeCookie {
//    public static void main(String[] args) throws Exception {
//        final DbConnection dbConnection = DbConnection.getInstance();
//        final Robot robot = new Robot();
//        List<VLikeAccount> allVLikeAccs = dbConnection.getAllVLikeAccs();
//        clickOn(216, 1426, robot);
//        for (int vLikeAccIndex = 0; vLikeAccIndex < allVLikeAccs.size(); vLikeAccIndex++) {
//            final VLikeAccount vLikeAccount = allVLikeAccs.get(vLikeAccIndex);
//            if (vLikeAccount.getCookie() != null) {
//                continue;
//            }
//            final VkAccount vkAccount = vLikeAccount.getVkAccount();
//            final String proxyUrl = vkAccount.getProxy();
//            final String proxyPort = vkAccount.getPort();
//            final String proxyPassword = vkAccount.getProxyPassword();
//            final String proxyUsername = vkAccount.getProxyUsername();
//            Thread.sleep(300);
//            clickOn(379, 700, robot);
//            clickOn(379, 700, robot);
//            clickOn(379, 700, robot);
//            typeString(proxyUrl, robot);
//            robot.keyPress(KeyEvent.VK_TAB);
//            cleanProxy(robot);
//            Thread.sleep(300);
//            typeString(proxyPort, robot);
//            Thread.sleep(800);
//            clickOn(374, 948, robot);
//            clickOn(374, 948, robot);
//            Thread.sleep(2500);
//            clickOn(374, 948, robot);
//            Thread.sleep(2500);
//
//            Runtime.getRuntime().exec("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe --start-maximized  --remote-debugging-port=9222 --user-data-dir=\"C:\\selenum\\ChromeProfile\"");
//            Thread.sleep(800);
//            clickOnUrl(robot);
//            Thread.sleep(300);
//            typeString("https://v-like.ru/", robot);
//            Thread.sleep(300);
//            //  clickOn(398, 129, robot);
//            pressEnter(robot);
//            Thread.sleep(1000);
//
//            typeString(proxyUsername, robot);
//            robot.keyPress(KeyEvent.VK_TAB);
//            typeString(proxyPassword, robot);
//            pressEnter(robot);
//            Thread.sleep(5000);
//
//
//            clickOn(1716, 134, robot);
//            Thread.sleep(15000);
//            robot.mouseMove(822, 280);
//            robot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
//            robot.mouseMove(1260, 278);
//            robot.keyPress(KeyEvent.VK_CONTROL);
//            Thread.sleep(300);
//            robot.keyPress(KeyEvent.VK_C);
//            Thread.sleep(300);
//            robot.keyRelease(KeyEvent.VK_C);
//            Thread.sleep(300);
//            robot.keyRelease(KeyEvent.VK_CONTROL);
//            Thread.sleep(200);
//            robot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
//            final String captchaTest = (String) Toolkit.getDefaultToolkit()
//                    .getSystemClipboard().getData(DataFlavor.stringFlavor);
//            if (captchaTest.contains("Checking if the site connection is secure")) {
//                clickOn(2539, 8, robot);
//                Thread.sleep(1000);
//                System.out.println("CAPTCHA going to try rerun refresh cookies for acc id " + vLikeAccount.getId());
//                vLikeAccIndex--;
//                continue;
//            }
//
//            ChromeOptions options = new ChromeOptions();
//            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
//            final LoggingPreferences logPrefs = new LoggingPreferences();
//            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
//            options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
//            final ChromeDriver driver = new ChromeDriver(options);
//            driver.get("https://v-like.ru/auth/login");
//            waitUntilElementWillBePresent(driver, "input[@id='email']").sendKeys(vLikeAccount.getEmail());
//            findElement(driver, "input[@id='password']").sendKeys(vLikeAccount.getPassword());
//            findElement(driver, "button[@type='submit']").click();
//            Thread.sleep(2000);
//            driver.get("https://v-like.ru/en/tasks/vk");
//            Thread.sleep(1000);
//            final LogEntries logs = driver.manage().logs().get("performance");
//            for (final LogEntry entry : logs) {
//                final JSONObject json = new JSONObject(entry.getMessage());
//                final JSONObject message = json.getJSONObject("message");
//                final String method = message.getString("method");
//
//                if ("Network.responseReceived".equals(method)) {
//                    final JSONObject params = message.getJSONObject("params");
//
//                    final JSONObject response = params.getJSONObject("response");
//                    final String messageUrl = response.getString("url");
//
//                    if (messageUrl.contains("https://v-like.ru/en/tasks/vk")) {
//                        final String requestId = params.getString("requestId");
//                        for (final LogEntry reqEntry : logs) {
//                            final JSONObject reqJson = new JSONObject(reqEntry.getMessage());
//                            final JSONObject reqMessage = reqJson.getJSONObject("message");
//                            final JSONObject reqParams = reqMessage.getJSONObject("params");
//                            try {
//                                final String reqId = reqParams.getString("requestId");
//                                if (requestId.equals(reqId)) {
//                                    final JSONObject headers = reqParams.getJSONObject("headers");
//                                    try {
//                                        vLikeAccount.setCookie((String) headers.get("cookie"));
//                                        break;
//                                    } catch (Exception e) {
//                                        vLikeAccount.setCookie((String) headers.get("Cookie"));
//                                        break;
//                                    }
//                                }
//                            } catch (JSONException ignored) {
//                            }
//                        }
//                        break;
//                    }
//                }
//            }
//
//
//            dbConnection.saveVLikeCookies(vLikeAccount.getId(), vLikeAccount.getCookie());
//
//            clickOn(2539, 8, robot);
//            Thread.sleep(1000);
//            FileUtils.deleteDirectory(new File("C:\\selenum\\ChromeProfile"));
////            if (true) {
////                return;
////            }
//        }
//    }
//
//    private static WebElement waitUntilElementWillBePresent(final ChromeDriver driver, final String xpath) throws Exception {
//        final By searchCriteria = By.xpath("//" + xpath);
//        new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
//        Thread.sleep(2000);
//        return driver.findElement(searchCriteria);
//    }
//
//    private static WebElement findElement(final ChromeDriver driver, final String xpath) {
//        return driver.findElement(By.xpath("//" + xpath));
//    }
//
//    private static void cleanProxy(Robot robot) throws Exception {
//        clickOn(609, 699, robot);
//        Thread.sleep(400);
//        clickOn(609, 699, robot);
//    }
//    private static WebElement waitUntilElementWithTextWillBePresent(final ChromeDriver driver, final String tag, final String text) {
//        final By searchCriteria = By.xpath("//" + tag + "[text()='" + text + "']");
//        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
//        return driver.findElement(searchCriteria);
//
//    }
//    private static void pressEnter(final Robot robot) {
//        robot.keyPress(KeyEvent.VK_ENTER);
//        robot.keyRelease(KeyEvent.VK_ENTER);
//    }
//
//    private static void clickOn(final int x, final int y, final Robot robot) throws Exception {
//        robot.mouseMove(x, y);
//        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
//        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
//        Thread.sleep(150);
//    }
//
//    private static void clickOnUrl(final Robot robot) throws Exception {
//        clickOn(312, 53, robot);
//    }
//
//    private static void typeString(String string, final Robot robot) throws Exception {
//
//        //Looping through every char
//        for (int i = 0; i < string.length(); i++) {
//            //Getting current char
//            char c = string.charAt(i);
//            if (StringUtils.isEmpty(String.valueOf(c))) {
//                return;
//            }
//            try {
//                //Pressing shift if it's uppercase
//                if (Character.isUpperCase(c)) {
//                    robot.keyPress(KeyEvent.VK_SHIFT);
//                }
//
//                if (c == '@') {
//                    robot.keyPress(KeyEvent.VK_SHIFT);
//                    robot.keyPress(KeyEvent.VK_2);
//                    robot.keyRelease(KeyEvent.VK_2);
//                    robot.keyRelease(KeyEvent.VK_SHIFT);
//                } else if (c == ':') {
//                    robot.keyPress(KeyEvent.VK_SHIFT);
//                    robot.keyPress(KeyEvent.VK_SEMICOLON);
//                    robot.keyRelease(KeyEvent.VK_SEMICOLON);
//                    robot.keyRelease(KeyEvent.VK_SHIFT);
//                } else if (c == '?') {
//                    robot.keyPress(KeyEvent.VK_SHIFT);
//                    robot.keyPress(KeyEvent.VK_SLASH);
//                    robot.keyRelease(KeyEvent.VK_SLASH);
//                    robot.keyRelease(KeyEvent.VK_SHIFT);
//                } else {
//                    //Actually pressing the key
//                    robot.keyPress(Character.toUpperCase(c));
//                    robot.keyRelease(Character.toUpperCase(c));
//                }
//
//
//                //Releasing shift if it's uppercase
//                if (Character.isUpperCase(c)) {
//                    robot.keyRelease(KeyEvent.VK_SHIFT);
//                }
//            } catch (Exception e) {
//
//            }
//            //Optional delay to make it look like it's a human typing
//            Thread.sleep(50);
//        }
//    }
//}
