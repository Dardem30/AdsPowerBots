package withdraw;

import bo.PayeerAccount;
import da.DbConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import util.ProxyCustomizer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class PayeerWithdraw {
    public static void main(String[] args) throws Exception {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("##.00", decimalFormatSymbols);

        final DbConnection dbConnection = DbConnection.getInstance();
        for (final PayeerAccount payeerAccount : dbConnection.getPayeerAccsToInvoice()) {
            final RestTemplate restTemplate = ProxyCustomizer.buildRestTemplate(payeerAccount.getVkAccount());
            final HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.add("Accept-Language", "ru-RU,ru;q=0.9");
            headers.add("Connection", "keep-alive");
            headers.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            headers.add("Cookie", payeerAccount.getCookies());
            headers.add("Origin", "https://payeer.com");
            headers.add("Referer", "https://payeer.com/ru/account/send/");
            headers.add("sec-ch-ua", "\"Google Chrome\";v=\"108\", \"Chromium\";v=\"108\", \"Not=A?Brand\";v=\"8\"");
            headers.add("sec-ch-ua-mobile", "?0");
            headers.add("sec-ch-ua-platform", "\"Windows\"");
            headers.add("sec-fetch-dest", "empty");
            headers.add("sec-fetch-mode", "cors");
            headers.add("sec-fetch-site", "same-site");
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");
            headers.add("X-Requested-With", "XMLHttpRequest");

            final Float balance = payeerAccount.getBalance() - 5;
            final double amountToPay = balance + (balance * 0.005);
            final MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
            params.add("payout_method", "1136053");
            params.add("ps", "1136053");
            params.add("param_ACCOUNT_NUMBER", "P1084286324");
            params.add("protect_day", "1");
            params.add("sum_receive", decimalFormat.format(balance));
            params.add("curr_receive", "RUB");
            params.add("curr_pay", "RUB");
            params.add("sum_pay", decimalFormat.format(amountToPay));
            params.add("block", "0");
            params.add("output_type", "list");
            params.add("fee_0", "N");
            params.add("sessid", payeerAccount.getSessionId());
            final String response = restTemplate.exchange("https://payeer.com/bitrix/components/payeer/account.send.08_18/templates/as_add8/ajax.php?action=output", HttpMethod.POST,
                    new HttpEntity<>(params, headers), String.class).getBody();
            System.out.println(response);
            Thread.sleep(20000);
        }
    }
}
