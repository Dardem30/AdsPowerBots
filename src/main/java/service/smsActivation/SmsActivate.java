package service.smsActivation;

import bo.SmsActivationResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SmsActivate {
    private static final String API_KEY = "3d7A7953d4f7bebbfA107e924d32d22c";
    private static final String BASE_URL = "https://api.sms-activate.org/stubs/handler_api.php?";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final BasicResponseHandler responseHandler = new BasicResponseHandler();
    private static SmsActivate instance;
    public static String VK_PRODUCT = "vk_0";

    public static SmsActivate getInstance() {
        if (instance == null) {
            instance = new SmsActivate();
        }
        return instance;
    }

    public boolean canPurchaseVkProduct() throws Exception {
//&country=0 - Россия
//  final HttpGet requestForProducts = new HttpGet(constructUrl("getNumbersStatus") + "&country=0&operator=tele2");
        final HttpGet requestForProducts = new HttpGet(constructUrl("getNumbersStatus") + "&country=0&operator=tele2");
        final JSONObject products = new JSONObject(responseHandler.handleResponse(httpClient.execute(requestForProducts)));
        final int productAmount = products.getInt(VK_PRODUCT);
        System.out.println("[SmsActivate] Available vk products: " + productAmount);
        if (productAmount <= 0) {
            return false;
        }
        final HttpGet requestForBalance = new HttpGet(constructUrl("getBalance"));
        final double balance = Double.parseDouble(responseHandler.handleResponse(httpClient.execute(requestForBalance)).split(":")[1]);
        System.out.println("[SmsActivate] Available balance: " + balance);
        return balance >= 28;

    }

    //        {
//            "activationId": 635468024,
//                "phoneNumber": "79584******",
//                "activationCost": "12.50",
//                "countryCode": "0",
//                "canGetAnotherSms": "1",
//                "activationTime": "2022-06-01 17:30:57",
//                "activationOperator": "mtt"
//        }
    public static void main(String[] args) throws Exception {
        getInstance().purchaseVkActivation();
    }
    private static final List<String> banMask = Arrays.asList("928", "999");
    public SmsActivationResponse purchaseNumber(final String service, final String operator, final String additionalParams) throws Exception {
        final HttpGet requestForPurchase = new HttpGet(constructUrl("getNumberV2") + "&country=0&service=" + service + "&operator=" + operator + (additionalParams == null ? "" : additionalParams));
        final String strResponse = responseHandler.handleResponse(httpClient.execute(requestForPurchase));
        if (strResponse.contains("NO_NUMBERS")) {
            return null;
        }
        System.out.println(new Date() + " Purchased activation " + strResponse);
        final JSONObject purchaseResponse = new JSONObject(strResponse);
        final SmsActivationResponse smsActivationResponse = new SmsActivationResponse();
        smsActivationResponse.setId(purchaseResponse.getInt("activationId"));
        smsActivationResponse.setPhone(purchaseResponse.getString("phoneNumber"));
//        if (banMask.contains(smsActivationResponse.getPhone().substring(1, 4))) {
//            System.out.println("Phone number [" + smsActivationResponse.getPhone() + "] not allowed for verification!!!!");
//            try {
//                cancelRequest(smsActivationResponse.getId());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return purchaseNumber(service, operator, additionalParams);
//        }
        return waitUntilPhoneIsReadyToReceiveCode(smsActivationResponse);
    }

    public SmsActivationResponse waitUntilPhoneIsReadyToReceiveCode(final SmsActivationResponse smsActivationResponse) throws Exception {
        int index = 0;
        while (true) {
            Thread.sleep(3000);
            if (index > 20) {
                System.out.println("Took too long to prepare phone [" + smsActivationResponse.getPhone() + "] going to cancell");
                finishRequest(smsActivationResponse.getId());
                return null;
            }
            index++;
            try {
                String requestStatus = checkRequestStatus(smsActivationResponse.getId());
                System.out.println(requestStatus);
                if ("STATUS_WAIT_CODE".equals(requestStatus)) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return smsActivationResponse;
    }

    public String retrieveLastResultCodeOfThePurchase(final Integer purchaseId, final int smsIndex) throws Exception {
        int index = 0;
        while (index != 15) {
            try {
                Thread.sleep(3000);
                String requestStatus = checkRequestStatus(purchaseId);
                System.out.println(requestStatus);
                if (requestStatus.contains("STATUS_OK")) {
                    return requestStatus.split(":")[1];
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            index++;
        }
        return null;
    }
    public String retrieveLastResultCodeOfTheCallReset(final Integer purchaseId, final int smsIndex) throws Exception {
        int index = 0;
        while (index != 15) {
            try {
                Thread.sleep(3000);
                String requestStatus = checkCallStatus(purchaseId);
                System.out.println(requestStatus);
                final JSONObject result = new JSONObject(requestStatus);
                if (
                        "3".equals(result.getString("status"))
                                || "5".equals(result.getString("status"))
                ) {
                    return result.getString("phone");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            index++;
        }
        return null;
    }

    public JSONObject checkRequestStatusWithSms(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet(constructUrl("getStatus") + "&id=" + purchaseId);
        String s = responseHandler.handleResponse(httpClient.execute(checkRequest));
        System.out.println(s);
        return new JSONObject(s);
    }
    public String checkRequestStatus(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet(constructUrl("getStatus") + "&id=" + purchaseId);
        String s = responseHandler.handleResponse(httpClient.execute(checkRequest));
        System.out.println(s);
        return s;
    }

    public String checkCallStatus(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet(constructUrl("getIncomingCallStatus") + "&activationId=" + purchaseId);
        String s = responseHandler.handleResponse(httpClient.execute(checkRequest));
        System.out.println(s);
        return s;
    }

    private String constructUrl(final String action) {
        return BASE_URL + "api_key=" + API_KEY + "&action=" + action;
    }

    public void finishRequest(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet(constructUrl("setStatus") + "&id=" + purchaseId + "&status=6");
        System.out.println(responseHandler.handleResponse(httpClient.execute(checkRequest)));
    }
    public void cancelRequest(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet(constructUrl("setStatus") + "&id=" + purchaseId + "&status=8");
        System.out.println(responseHandler.handleResponse(httpClient.execute(checkRequest)));
    }
    public void requestAdditionalSms(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet(constructUrl("setStatus") + "&id=" + purchaseId + "&status=3");
        System.out.println(responseHandler.handleResponse(httpClient.execute(checkRequest)));
        Thread.sleep(3000);
//        while (true) {
//            Thread.sleep(3000);
//            try {
//                String requestStatus = checkRequestStatus(purchaseId);
//                System.out.println(requestStatus);
//                if ("ACCESS_RETRY_GET".equals(requestStatus)) {
//                    break;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }


    public void banRequest(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet(constructUrl("setStatus") + "&id=" + purchaseId + "&status=8");
        System.out.println(responseHandler.handleResponse(httpClient.execute(checkRequest)));
    }

    public SmsActivationResponse purchaseVkActivation() throws Exception {
        while (true) {
//  Thread.sleep(25);
            try {
                SmsActivationResponse smsActivationResponse = purchaseNumber("vk", "tele2", null);
                if (smsActivationResponse != null) {
                    System.out.println("[SmsActivate] Purchased tele2 phone number: " + smsActivationResponse.getPhone());
                    return smsActivationResponse;
                }
//                smsActivationResponse = purchaseVkSmsVerification("yota");
//                if (smsActivationResponse != null) {
//                    System.out.println("[SmsActivate] Purchased yota phone number: " + smsActivationResponse.getPhone());
//                    return smsActivationResponse;
//                }
////                smsActivationResponse = purchaseVkSmsVerification("mts");
////                if (smsActivationResponse != null) {
////                    System.out.println("[SmsActivate] Purchased mts phone number: " + smsActivationResponse.getPhone());
////                    return smsActivationResponse;
////                }
//                smsActivationResponse = purchaseVkSmsVerification("beeline");
//                if (smsActivationResponse != null) {
//                    System.out.println("[SmsActivate] Purchased beeline phone number: " + smsActivationResponse.getPhone());
//                    return smsActivationResponse;
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}