//package refresh;
//
//import bo.VkAccount;
//import da.DbConnection;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.openqa.selenium.By;
//import org.openqa.selenium.TimeoutException;
//import org.openqa.selenium.UnexpectedAlertBehaviour;
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
//import service.VkService;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.logging.Level;
//
//public class CheckVkAcc {
//    private static final String BASE_URL = "http://local.adspower.com:50325/";
//
//    public static void main(String[] args) throws Exception {
//        final VkService vkService = VkService.getInstance();
//        final DbConnection dbConnection = DbConnection.getInstance();
//        final RestTemplate localRestTemplate = new RestTemplate();
//        List<VkAccount> vkAccounts = dbConnection.getAllVkAccs();
//        List<String> cookies = new ArrayList<>();
//        List<Integer> blockedAccs = new ArrayList<>();
//        //   List<String> tokens = new ArrayList<>();
//        try {
//            for (VkAccount vkAccount : vkAccounts) {
////                if (vkAccount.getId() < 68) {
////                    continue;
////                }//23
//                try {
//                    final String vkPhone = vkAccount.getPhone();
//                    final String vkPassword = vkAccount.getPassword();
//                    final String accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=" + vkAccount.getBrowserProfile()
//                            + "&ip_tab=0&headless=1&open_tabs=1", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();
//                    final JSONObject accParamsJson = new JSONObject(accParams);
//                    final JSONObject seleniumConnectionParams = accParamsJson.getJSONObject("data");
//                    System.setProperty("webdriver.chrome.driver", seleniumConnectionParams.getString("webdriver")); // Set the webdriver returned by the API
//                    final ChromeOptions options = new ChromeOptions();
//                    options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
//                    options.setExperimentalOption("debuggerAddress", seleniumConnectionParams.getJSONObject("ws").getString("selenium"));
//                    final LoggingPreferences logPrefs = new LoggingPreferences();
//                    logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
//                    options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
//                    final ChromeDriver driver = new ChromeDriver(options);
//
//                    driver.manage().window().maximize();
//
//                    driver.get("https://vk.com/");
////                    if (driver.getPageSource().contains("<h1>Аккаунт временно заблокирован</h1>")) {
////                        System.out.println(vkAccount.getId() + " blocked");
////                    }
//                    try {
//                        final String blockMessage = waitUntilElementWillBePresent(driver, "div[@id='login_blocked_wrap']").getAttribute("innerText");
//                        System.out.println(vkAccount.getId() + " blocked");
//                        blockedAccs.add(vkAccount.getId());
//                    } catch (TimeoutException ignored) {
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        waitUntilElementWillBePresent(driver, "input[@name='login']").sendKeys(vkAccount.getPhone());
//                        System.out.println("Going to login into vk acc " + vkAccount.getId());
//                        findElement(driver, "button[@type='submit']").click();
//                        Thread.sleep(5000);
//                        try {
//                            findElement(driver, "input[@type='password']").sendKeys(vkAccount.getPassword());
//                        } catch (Exception e) {
//                            findElement(driver, "button[@type='button']/span").click();
//                            waitUntilElementWillBePresent(driver, "input[@type='password']").sendKeys(vkAccount.getPassword());
//                        }
//                        findElement(driver, "button[@type='submit']").click();
//                    } catch (Exception ignored) {
//
//                    }
//                    try {
//                        final String blockMessage = waitUntilElementWillBePresent(driver, "div[@id='login_blocked_wrap']").getAttribute("innerText");
//                        System.out.println(vkAccount.getId() + " blocked");
//                        blockedAccs.add(vkAccount.getId());
//                    } catch (TimeoutException ignored) {
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
////                try {
////                    waitUntilElementWillBePresent(driver, "a[@id='top_profile_link']").click();
////                    waitUntilElementWillBePresent(driver, "a[@id='top_logout_link']").click();
////                    driver.get("https://vk.com/");
////                    waitUntilElementWillBePresent(driver, "input[@name='login']").sendKeys(vkPhone);
////                } catch (Exception e) {
////                    e.printStackTrace();
////                    waitUntilElementWillBePresent(driver, "input[@name='login']").sendKeys(vkPhone);
//////                waitUntilElementWillBePresent(driver, "div[@class='ui_gallery_item']").click();
//////                waitUntilElementWillBePresent(driver, "a[@id='top_profile_link']");
////                }
////                findElement(driver, "button[@type='submit']").click();
////                Thread.sleep(5000);
////                try {
////                    findElement(driver, "input[@type='password']").sendKeys(vkPassword);
////                } catch (Exception e) {
////                    findElement(driver, "button[@type='button']/span").click();
////                    waitUntilElementWillBePresent(driver, "input[@type='password']").sendKeys(vkPassword);
////                }
////                findElement(driver, "button[@type='submit']").click();
////                waitUntilElementWillBePresent(driver, "a[@id='top_profile_link']");
////                Thread.sleep(4000);
////                final LogEntries vkLogs = driver.manage().logs().get("performance");
////                String vkCookies = null;
////                for (final LogEntry entry : vkLogs) {
////                    final JSONObject json = new JSONObject(entry.getMessage());
////                    final JSONObject message = json.getJSONObject("message");
////                    final String method = message.getString("method");
////
////                    if ("Network.responseReceived".equals(method)) {
////                        final JSONObject params = message.getJSONObject("params");
////
////                        final JSONObject response = params.getJSONObject("response");
////                        final String messageUrl = response.getString("url");
////
////                        if (messageUrl.contains("https://login.vk.com/?act=web_token")) {
////                            final String requestId = params.getString("requestId");
////                            for (final LogEntry reqEntry : vkLogs) {
////                                final JSONObject reqJson = new JSONObject(reqEntry.getMessage());
////                                final JSONObject reqMessage = reqJson.getJSONObject("message");
////                                final JSONObject reqParams = reqMessage.getJSONObject("params");
////                                try {
////                                    final String reqId = reqParams.getString("requestId");
////                                    if (requestId.equals(reqId)) {
////                                        final JSONObject headers = reqParams.getJSONObject("headers");
////                                        try {
////                                            vkCookies = (String) headers.get("cookie");
////                                            break;
////                                        } catch (Exception e) {
////                                            vkCookies = (String) headers.get("Cookie");
////                                            break;
////                                        }
////                                    }
////                                } catch (JSONException ignored) {
////                                }
////                            }
////                            break;
////                        }
////                    }
////                }
////                for (final LogEntry entry : vkLogs) {
////                    final JSONObject json = new JSONObject(entry.getMessage());
////                    final JSONObject message = json.getJSONObject("message");
////                    final String method = message.getString("method");
////
////                    if ("Network.responseReceived".equals(method)) {
////                        final JSONObject params = message.getJSONObject("params");
////
////                        final JSONObject response = params.getJSONObject("response");
////                        final String messageUrl = response.getString("url");
////
////                        if (messageUrl.contains("https://vk.com/feed")) {
////                            final String requestId = params.getString("requestId");
////                            for (final LogEntry reqEntry : vkLogs) {
////                                final JSONObject reqJson = new JSONObject(reqEntry.getMessage());
////                                final JSONObject reqMessage = reqJson.getJSONObject("message");
////                                final JSONObject reqParams = reqMessage.getJSONObject("params");
////                                try {
////                                    final String reqId = reqParams.getString("requestId");
////                                    if (requestId.equals(reqId)) {
////                                        final JSONObject headers = reqParams.getJSONObject("headers");
////                                        try {
////                                            vkCookies += ";" + headers.get("cookie");
////                                            break;
////                                        } catch (Exception e) {
////                                            vkCookies += ";" + headers.get("Cookie");
////                                            break;
////                                        }
////                                    }
////                                } catch (JSONException ignored) {
////                                }
////                            }
////                            break;
////                        }
////                    }
////                }
////                //  System.out.println(vkCookies);
////                final Map<String, String> cookiesToSave = new HashMap<>();
////                for (final String cookieRow : vkCookies.split(";")) {
////                    final String[] cookieProperties = cookieRow.trim().split("=");
////                    try {
////                        final String cookieName = cookieProperties[0];
////                        final String cookieValue = cookieProperties[1];
////                        cookiesToSave.put(cookieName, cookieValue);
////                    } catch (Exception e) {
////                        //    System.out.println(cookieRow);
////                    }
////                }
////                final String authTokenLocalStorageItem = (String) driver.executeScript("" +
////                        "const items = { ...localStorage };" +
////                        "        for (let key in items) {" +
////                        "            if (key.indexOf('web_token:login:auth') !=-1) {" +
////                        "                return items[key];" +
////                        "            }" +
////                        "        }");
////                final JSONObject authTokenLocalStorageItemJson = new JSONObject(authTokenLocalStorageItem);
////                final Integer userId = authTokenLocalStorageItemJson.getInt("user_id");
////                final String accessToken = authTokenLocalStorageItemJson.getString("access_token");
////                vkAccount.setCookie(vkCookies);
////                vkAccount.setUserId(String.valueOf(userId));
////                //   System.out.println(userId);
////                //  System.out.println(vkCookies);
////                cookies.add(vkCookies);
////                //cookies.
////                //  RestTemplate vkRestTemplate = ProxyCustomizer.buildRestTemplate(vkAccount, Proxy.Type.HTTP);
////                //   vkAccount.setAccessToken(vkService.getAccessToken(vkAccount, vkRestTemplate));
////                dbConnection.saveVkAccCookies(cookiesToSave, vkAccount.getId());
////            if (true) {
////                return;
////            }
//                    localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
//                } catch (Exception у) {
//                    System.out.println("Failed to check acc " + vkAccount.getId());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        for (final Integer accId : blockedAccs) {
//            System.out.println(accId);
//        }
//    }
//
//    private static WebElement waitUntilElementWithTextWillBePresent(final ChromeDriver driver, final String tag, final String text) {
//        final By searchCriteria = By.xpath("//" + tag + "[text()='" + text + "']");
//        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
//        return driver.findElement(searchCriteria);
//
//    }
//
//    private static WebElement waitUntilElementWithTextWillBePresentWithoutTimeout(final ChromeDriver driver, final String tag, final String text) {
//        final By searchCriteria = By.xpath("//" + tag + "[text()='" + text + "']");
//        new WebDriverWait(driver, Duration.ofSeconds(1200)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
//        return driver.findElement(searchCriteria);
//
//    }
//
//    private static WebElement waitUntilElementWillBePresent(final ChromeDriver driver, final String xpath) throws Exception {
//        final By searchCriteria = By.xpath("//" + xpath);
//        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
//        Thread.sleep(2000);
//        return driver.findElement(searchCriteria);
//    }
//
//    private static WebElement waitUntilElementWillBePresentWithoutTimeout(final ChromeDriver driver, final String xpath) throws Exception {
//        final By searchCriteria = By.xpath("//" + xpath);
//        new WebDriverWait(driver, Duration.ofSeconds(1200)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
//        Thread.sleep(2000);
//        return driver.findElement(searchCriteria);
//    }
//
//    private static WebElement findElement(final ChromeDriver driver, final String xpath) {
//        return driver.findElement(By.xpath("//" + xpath));
//    }
//}
