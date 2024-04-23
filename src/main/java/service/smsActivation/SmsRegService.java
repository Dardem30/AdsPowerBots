package service.smsActivation;

import bo.SmsActivationResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

public class SmsRegService implements SmsActivationService {
    private static SmsRegService smsRegService;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final BasicResponseHandler responseHandler = new BasicResponseHandler();
    private static final String token = "xxosqzomkfdn6hqfe2bqp5hvfga6o0ap";

    public static SmsRegService getInstance() {
        if (smsRegService == null) {
            smsRegService = new SmsRegService();
        }
        return smsRegService;
    }

    public boolean canPurchaseVkProduct() throws Exception {
        final HttpGet requestForBalance = new HttpGet("https://api.sms-reg.com/getBalance.php?apikey=" + token);
        final JSONObject balanceInfo = new JSONObject(responseHandler.handleResponse(httpClient.execute(requestForBalance)));
        double balance = Double.parseDouble(balanceInfo.getString("balance"));
        System.out.println("[SmsRegService] Available balance: " + balance);
        return balance > 30;
    }

    public SmsActivationResponse purchaseVkSmsVerification() throws Exception {
        final HttpGet requestForProducts = new HttpGet("https://api.sms-reg.com/getNum.php?country=ru&service=vk&apikey=" + token);
        final String response = responseHandler.handleResponse(httpClient.execute(requestForProducts));
        System.out.println(response);
        if (response.contains("WARNING_NO_NUMS")) {
            return null;
        }
        final JSONObject purchaseResponse = new JSONObject(response);
        final SmsActivationResponse smsActivationResponse = new SmsActivationResponse();
        smsActivationResponse.setId(purchaseResponse.getInt("tzid"));
        int index = 0;
        while (true) {
            Thread.sleep(3000);
            JSONObject requestStatus = checkRequestStatus(smsActivationResponse.getId());
            System.out.println(requestStatus);
            if (requestStatus.get("response").equals("TZ_NUM_PREPARE")) {
                break;
            }
            if (index > 20) {
                System.out.println("Took too long to prepare phone [" + smsActivationResponse.getId() + "] going to cancell");
                banRequest(smsActivationResponse.getId());
                return null;
            }
            index++;
        }
        Thread.sleep(5000);
        System.out.println(responseHandler.handleResponse(
                httpClient.execute(new HttpGet("https://api.sms-reg.com/setReady.php?tzid=" + smsActivationResponse.getId() + "&apikey=" + token))));
        Thread.sleep(15000);
        index = 0;
        while (true) {
            Thread.sleep(5000);
            JSONObject requestStatus = checkRequestStatus(smsActivationResponse.getId());
            System.out.println(requestStatus);
            smsActivationResponse.setPhone(requestStatus.getString("number"));
            if (requestStatus.get("response").equals("TZ_NUM_WAIT")) {
                break;
            }
            if (index > 20) {
                System.out.println("Took too long to prepare phone [" + smsActivationResponse.getId() + "] going to cancell");
                finishRequest(smsActivationResponse.getId());
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
                Thread.sleep(5000);
                JSONObject requestStatus = checkRequestStatus(purchaseId);
                System.out.println(requestStatus);
                final String answer = requestStatus.getString("msg");
                if (StringUtils.isNotEmpty(answer)) {
                    return answer;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            index++;
        }
        return null;
    }

    @Override
    public void finishRequest(Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet("https://api.sms-reg.com/setOperationOk.php?tzid=" + purchaseId + "&apikey=" + token);
        System.out.println(responseHandler.handleResponse(httpClient.execute(checkRequest)));
    }

    @Override
    public void banRequest(Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet("https://api.sms-reg.com/setOperationUsed.php?tzid=" + purchaseId + "&apikey=" + token);
        System.out.println(responseHandler.handleResponse(httpClient.execute(checkRequest)));
    }

    private JSONObject checkRequestStatus(final Integer purchaseId) throws Exception {
        final HttpGet checkRequest = new HttpGet("https://api.sms-reg.com/getState.php?tzid=" + purchaseId + "&apikey=" + token);
        return new JSONObject(responseHandler.handleResponse(httpClient.execute(checkRequest)));
    }
}
