import da.DbConnection;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class Proxy6Client {
    private static final String BASE_URL = "https://proxy6.net/api/";
    private static final String API_KEY = "7c7eeb1906-0e6de27d79-f4863c04d4";
    public static void main(String[] args) throws Exception {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final DbConnection dbConnection = DbConnection.getInstance();
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<LinkedHashMap> getproxyResponse = restTemplate.getForEntity(constructUrl("getproxy"), LinkedHashMap.class);
        final LinkedHashMap proxies = getproxyResponse.getBody();
        final LinkedHashMap<String, LinkedHashMap> list = (LinkedHashMap) proxies.get("list");
        int countUsedProxy = 0;
        Date earliestDateEnd = null;
        LinkedHashMap earliestProxy = null;
        List<String> proxyIdsToProlong = new ArrayList<>();
        for (final LinkedHashMap proxy : list.values()) {
            if (dbConnection.isProxyUsed((String) proxy.get("host"))) {
                countUsedProxy++;
                Date proxyDateEnd = dateFormat.parse((String) proxy.get("date_end"));
                if (earliestDateEnd == null || earliestDateEnd.after(proxyDateEnd)) {
                    earliestDateEnd = proxyDateEnd;
                    earliestProxy = proxy;
                }
                proxyIdsToProlong.add((String) proxy.get("id"));
                System.out.println(proxy.get("host"));
            }
        }
        System.out.println("Date to pay for proxies: " + earliestDateEnd);
        System.out.println("Amount of using proxy: " + countUsedProxy);
        System.out.println("Ids to prolong: " + StringUtils.join(proxyIdsToProlong, ","));

//        final ResponseEntity<LinkedHashMap> prolongResponse = restTemplate.getForEntity(constructUrl("prolong") + "?period=30&ids=" + StringUtils.join(proxyIdsToProlong, ","), LinkedHashMap.class);
//        System.out.println(prolongResponse.getBody().toString());
    }
    private final static String constructUrl(final String method) {
        return BASE_URL + API_KEY + "/" + method;
    }
}
