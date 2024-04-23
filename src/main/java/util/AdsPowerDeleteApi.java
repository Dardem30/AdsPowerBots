package util;

import da.DbConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;

public class AdsPowerDeleteApi {
    private static final String BASE_URL = "http://local.adspower.com:50325/";
    public static void main(String[] args) throws Exception {
        final RestTemplate localRestTemplate = new RestTemplate();
        final DbConnection dbConnection = DbConnection.getInstance();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        LinkedHashMap body = localRestTemplate.exchange(BASE_URL + "/api/v1/user/list?page_size=1000",
                HttpMethod.GET, new HttpEntity<>(headers), LinkedHashMap.class).getBody();
        LinkedHashMap data = (LinkedHashMap) body.get("data");
        List<LinkedHashMap> list = (List<LinkedHashMap>) data.get("list");
        if (!list.isEmpty()) {
            StringBuilder requestBody = new StringBuilder("{\"user_ids\": [");
            for (final LinkedHashMap profile : list) {
                final String userId = (String) profile.get("user_id");
                if (!dbConnection.isProfileUsed(userId)) {
                    requestBody.append("\"").append(userId).append("\",");
                }
            }
            requestBody.deleteCharAt(requestBody.length() - 1);
            requestBody.append("]}");
            System.out.println(requestBody);
            localRestTemplate.exchange(BASE_URL + "/api/v1/user/delete",
                    HttpMethod.POST, new HttpEntity<>(requestBody.toString(), headers), LinkedHashMap.class).getBody();
        }


    }
}
