package service.smsActivation;

import bo.SmsActivationResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

public class FiveSimService {
    private static FiveSimService instance;
    private static final String token = "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MDI3NDY1MzEsImlhdCI6MTY3MTIxMDUzMSwicmF5IjoiNjc0OGI3ZWU0NzE5NjllZDZjZWJiOWI3ZTI4NTM5NzQiLCJzdWIiOjEzMzkxMTh9.EnKKtC6lxgoNtCihdGFSzjeUY1PV736288GZPCBmludtzbEzcoprRXn198nsHafHrW8qchpoZ_L6-PadMhi7GwNb_PvkOZIttr-BgFIrD8HRSVPTdBvQtIf50yemUApSLK725CW8OzA2mSzlL23RRnKYDfcyuD9BYLRJeSr8Vn3IYatdOfalj41M5JEt073ynPf-sUJKPb0MVDNm_LMCxPuntlnELFy9ScTLuVkObmRtRhVE0obj5GPUVNtkIhTzgqcfUhX1HsVsVwja2Ya5S3eCPl_uoqKhANM5piFLIY9Rtay8qC1IW0gOvpR5p6mXfqa9k6BG8QD09SPH_c8OGg";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final BasicResponseHandler responseHandler = new BasicResponseHandler();

    public static FiveSimService getInstance() {
        if (instance == null) {
            instance = new FiveSimService();
        }
        return instance;
    }

    public boolean canPurchaseVkProduct() throws Exception {
        final HttpGet requestForProducts = new HttpGet("https://5sim.net/v1/guest/products/russia/tele2");
        requestForProducts.addHeader("Authorization", "Bearer " + token);
        requestForProducts.addHeader("Accept", "application/json");
        final JSONObject products = new JSONObject(responseHandler.handleResponse(httpClient.execute(requestForProducts)));
        final JSONObject blockChainProduct = products.getJSONObject("vkontakte");
        final int amount = blockChainProduct.getInt("Qty");
        System.out.println("[FiveSimService] Available vk products: " + amount);
        if (amount <= 0) {
            return false;
        }
        HttpGet request = new HttpGet("https://5sim.net/v1/user/profile");
        request.addHeader("Authorization", "Bearer " + token);
        request.addHeader("Accept", "application/json");
        final JSONObject profileInfo = new JSONObject(responseHandler.handleResponse(httpClient.execute(request)));
        final float balance = profileInfo.getFloat("balance");
        System.out.println("[FiveSimService] Available balance: " + balance);
        return balance >= 16;
    }
    public boolean canPurchaseCallResetProduct() throws Exception {
        final HttpGet requestForProducts = new HttpGet("https://5sim.net/v1/guest/products/russia/tele2");
        requestForProducts.addHeader("Authorization", "Bearer " + token);
        requestForProducts.addHeader("Accept", "application/json");
        final JSONObject products = new JSONObject(responseHandler.handleResponse(httpClient.execute(requestForProducts)));
        final JSONObject blockChainProduct = products.getJSONObject("blockchain");
        final int amount = blockChainProduct.getInt("Qty");
        System.out.println("[FiveSimService] Available call resets products: " + amount);
        if (amount <= 0) {
            return false;
        }
        HttpGet request = new HttpGet("https://5sim.net/v1/user/profile");
        request.addHeader("Authorization", "Bearer " + token);
        request.addHeader("Accept", "application/json");
        final JSONObject profileInfo = new JSONObject(responseHandler.handleResponse(httpClient.execute(request)));
        final float balance = profileInfo.getFloat("balance");
        System.out.println("[FiveSimService] Available balance: " + balance);
        return balance >= 16;
    }

    public SmsActivationResponse purchaseCallReset() throws Exception {
        final HttpGet requestForProducts = new HttpGet("https://5sim.net/v1/user/buy/activation/russia/megafon/blockchain");
        requestForProducts.addHeader("Authorization", "Bearer " + token);
        requestForProducts.addHeader("Accept", "application/json");
        String response = responseHandler.handleResponse(httpClient.execute(requestForProducts));
        System.out.println(response);
        if ("no free phones".equals(response)) {
            return null;
        }
        final JSONObject purchaseResponse = new JSONObject(response);
        final SmsActivationResponse smsActivationResponse = new SmsActivationResponse();
        smsActivationResponse.setId(purchaseResponse.getInt("id"));
        smsActivationResponse.setPhone(purchaseResponse.getString("phone"));
        return waitUntilPhoneIsReadyToReceiveCode(smsActivationResponse);
    }
    public SmsActivationResponse purchaseCallResetForVkSerfing() throws Exception {
        final HttpGet requestForProducts = new HttpGet("https://5sim.net/v1/user/buy/activation/russia/megafon/blockchain");
        requestForProducts.addHeader("Authorization", "Bearer " + token);
        requestForProducts.addHeader("Accept", "application/json");
        String response = responseHandler.handleResponse(httpClient.execute(requestForProducts));
        System.out.println(response);
        if ("no free phones".equals(response)) {
            return null;
        }
        final JSONObject purchaseResponse = new JSONObject(response);
        final SmsActivationResponse smsActivationResponse = new SmsActivationResponse();
        smsActivationResponse.setId(purchaseResponse.getInt("id"));
        smsActivationResponse.setPhone(purchaseResponse.getString("phone"));
        return waitUntilPhoneIsReadyToReceiveCode(smsActivationResponse);
    }

    public SmsActivationResponse purchaseVkSmsVerification(final String operator) throws Exception {
        final HttpGet requestForProducts = new HttpGet("https://5sim.net/v1/user/buy/activation/russia/" + operator + "/vkontakte");
        //final HttpGet requestForProducts = new HttpGet("https://5sim.net/v1/user/buy/activation/russia/any/vkontakte");
        requestForProducts.addHeader("Authorization", "Bearer " + token);
        requestForProducts.addHeader("Accept", "application/json");
        String response = responseHandler.handleResponse(httpClient.execute(requestForProducts));
        //System.out.println(response);
        if ("no free phones".equals(response)) {
            return null;
        }
        System.out.println(response);
        final JSONObject purchaseResponse = new JSONObject(response);
        final SmsActivationResponse smsActivationResponse = new SmsActivationResponse();
        smsActivationResponse.setId(purchaseResponse.getInt("id"));
        smsActivationResponse.setPhone(purchaseResponse.getString("phone"));
        return waitUntilPhoneIsReadyToReceiveCode(smsActivationResponse);
    }
    public SmsActivationResponse waitUntilPhoneIsReadyToReceiveCode(final SmsActivationResponse smsActivationResponse) throws Exception {
        int index = 0;
        while (true) {
            Thread.sleep(3000);
            JSONObject requestStatus = checkRequestStatus(smsActivationResponse.getId());
            System.out.println(requestStatus);
            if (requestStatus.get("status").equals("RECEIVED")) {
                break;
            }
            if (index > 20) {
                System.out.println("Took too long to prepare phone [" + smsActivationResponse.getPhone() + "] going to cancell");
                cancelRequest(smsActivationResponse.getId());
                return null;
            }
            index++;
        }
        return smsActivationResponse;
    }

    public String retrieveLastResultCodeOfThePurchase(final Integer purchaseId, final int smsIndex) throws Exception {
        int index = 0;
        while (index != 15) {
            try {
                Thread.sleep(3000);
                JSONObject requestStatus = checkRequestStatus(purchaseId);
                System.out.println(requestStatus);
                try {
                    JSONArray sms = requestStatus.getJSONArray("sms");
                    if (sms.length() == smsIndex) {
                        return sms.getJSONObject(smsIndex - 1).getString("code");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            index++;
        }
        return null;
    }

    private JSONObject checkRequestStatus(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet("https://5sim.net/v1/user/check/" + purchaseId);
        checkRequest.addHeader("Authorization", "Bearer " + token);
        checkRequest.addHeader("Accept", "application/json");
        return new JSONObject(responseHandler.handleResponse(httpClient.execute(checkRequest)));
    }

    public void finishRequest(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet("https://5sim.net/v1/user/finish/" + purchaseId);
        checkRequest.addHeader("Authorization", "Bearer " + token);
        checkRequest.addHeader("Accept", "application/json");
        System.out.println(responseHandler.handleResponse(httpClient.execute(checkRequest)));
    }

    public void cancelRequest(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet("https://5sim.net/v1/user/cancel/" + purchaseId);
        checkRequest.addHeader("Authorization", "Bearer " + token);
        checkRequest.addHeader("Accept", "application/json");
        System.out.println(responseHandler.handleResponse(httpClient.execute(checkRequest)));
    }
    public void banRequest(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet("https://5sim.net/v1/user/ban/" + purchaseId);
        checkRequest.addHeader("Authorization", "Bearer " + token);
        checkRequest.addHeader("Accept", "application/json");
        System.out.println(responseHandler.handleResponse(httpClient.execute(checkRequest)));
    }
    public SmsActivationResponse purchaseVkActivation() throws Exception {
        while (true) {
            //  Thread.sleep(25);
            try {
                SmsActivationResponse smsActivationResponse = purchaseVkSmsVerification("tele2");
                if (smsActivationResponse != null) {
                    System.out.println("[FiveSimService] Purchased tele2 phone number: " + smsActivationResponse.getPhone());
                    return smsActivationResponse;
                }
//                smsActivationResponse = purchaseVkSmsVerification("yota");
//                if (smsActivationResponse != null) {
//                    System.out.println("[FiveSimService] Purchased yota phone number: " + smsActivationResponse.getPhone());
//                    return smsActivationResponse;
//                }
////                smsActivationResponse = purchaseVkSmsVerification("mts");
////                if (smsActivationResponse != null) {
////                    System.out.println("[FiveSimService] Purchased mts phone number: " + smsActivationResponse.getPhone());
////                    return smsActivationResponse;
////                }
//                smsActivationResponse = purchaseVkSmsVerification("beeline");
//                if (smsActivationResponse != null) {
//                    System.out.println("[FiveSimService] Purchased beeline phone number: " + smsActivationResponse.getPhone());
//                    return smsActivationResponse;
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
