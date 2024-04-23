package refresh;

import bo.VkAccount;
import bo.VkCookie;
import da.DbConnection;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import service.VkService;
import util.IConstants;
import util.ProxyCustomizer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RefreshVkCookiesForSocks5 {
    public static void main(String[] args) throws Exception {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        final DbConnection dbConnection = DbConnection.getInstance();
        final VkService vkService = VkService.getInstance();
        while (true) {
            for (final VkAccount vkAccount : dbConnection.getAllVkAccs()) {
                try {
                    if (vkAccount.getId() != 92) {
                        continue;
                    }
                    final RestTemplate restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
                    final Map<String, String> cookies = new HashMap<>();
                    for (final VkCookie cookie : dbConnection.getVkCookies(vkAccount.getId())) {
                        cookies.put(cookie.getName(), cookie.getValue());
                    }
                    final ResponseEntity<String> loginPage = restTemplate.exchange("https://vk.com/", HttpMethod.GET, new HttpEntity<>(populateVkHeaders(cookies)), String.class);
                    for (final String header : loginPage.getHeaders().get("Set-Cookie")) {
                        if (!header.contains("DELETED")) {
                            String[] cookieParts = header.split("=");
                            cookies.put(cookieParts[0], cookieParts[1].split(";")[0]);
                        }
                    }
                    final MultiValueMap<String, String> vkAuthConnectSavedUserRequestUrlParameters = new LinkedMultiValueMap<String, String>();
                    vkAuthConnectSavedUserRequestUrlParameters.add("version", "1");
                    vkAuthConnectSavedUserRequestUrlParameters.add("app_id", IConstants.VK_CLIENT_ID);
                    vkAuthConnectSavedUserRequestUrlParameters.add("access_token", "");
                    final String vkSavedUserResponse = restTemplate.exchange("https://login.vk.com/?act=connect_get_saved_users", HttpMethod.POST,
                            new HttpEntity<>(vkAuthConnectSavedUserRequestUrlParameters, populateVkAuthGetSavedUsersHeaders(cookies)), String.class).getBody();
                    final String authHash = vkSavedUserResponse.split("\"auth_hash\":\"")[1].split("\"")[0];
                    final MultiValueMap<String, String> vkAuthRequestUrlParameters = new LinkedMultiValueMap<String, String>();
                    vkAuthRequestUrlParameters.add("user_id", vkAccount.getUserId());
                    vkAuthRequestUrlParameters.add("auth_hash", authHash);
                    vkAuthRequestUrlParameters.add("from", "carousel");
                    vkAuthRequestUrlParameters.add("version", "1");
                    vkAuthRequestUrlParameters.add("app_id", IConstants.VK_CLIENT_ID);
                    vkAuthRequestUrlParameters.add("access_token", "");
                    final ResponseEntity<String> vkAuthRequest = restTemplate.exchange("https://login.vk.com/?act=connect_auth_saved_users", HttpMethod.POST,
                            new HttpEntity<>(vkAuthRequestUrlParameters, populateVkAuthHeaders(cookies)), String.class);
                    for (final String header : vkAuthRequest.getHeaders().get("Set-Cookie")) {
                        if (!header.contains("DELETED")) {
                            String[] cookieParts = header.split("=");
                            String cookieName = cookieParts[0];
                            if (cookieName.equals("Array")) {
                                continue;
                            }
                            cookies.put(cookieName, cookieParts[1].split(";")[0]);
                        }
                    }
//                    final ResponseEntity<String> vkAuthRestoreRequest = restTemplate.exchange("https://vk.com/login.php", HttpMethod.GET,
//                            new HttpEntity<>(vkAuthRequestUrlParameters, populateVkAuthHeaders(cookies)), String.class);
//                    for (final String header : vkAuthRestoreRequest.getHeaders().get("Set-Cookie")) {
//                        if (!header.contains("DELETED")) {
////                            String[] cookieParts = header.split("=");
////                            String cookieName = cookieParts[0];
////                            if (cookieName.equals("Array")) {
////                                continue;
////                            }
////                            cookies.put(cookieName, cookieParts[1].split(";")[0]);
//                        }
//                    }
                    final StringBuilder cookieBuilder = new StringBuilder();
                    appendCookie(cookieBuilder, "remixscreen_depth", cookies);
                    appendCookie(cookieBuilder, "tmr_lvid", cookies);
                    appendCookie(cookieBuilder, "tmr_lvidTS", cookies);
                    appendCookie(cookieBuilder, "remixscreen_height", cookies);
                    appendCookie(cookieBuilder, "remixscreen_width", cookies);
                    appendCookie(cookieBuilder, "remixstid", cookies);
                    appendCookie(cookieBuilder, "remixdt", cookies);
                    appendCookie(cookieBuilder, "remixlang", cookies);
                    appendCookie(cookieBuilder, "remixluas2", cookies);
                    appendCookie(cookieBuilder, "remixstlid", cookies);
                    appendCookie(cookieBuilder, "remixflash", cookies);
                    appendCookie(cookieBuilder, "sui", cookies);
                    appendCookie(cookieBuilder, "remixuas", cookies);
                    appendCookie(cookieBuilder, "s", cookies);
                    appendCookie(cookieBuilder, "sua", cookies);
                    appendCookie(cookieBuilder, "remixsts", cookies);
                    appendCookie(cookieBuilder, "p", cookies);
                    appendCookie(cookieBuilder, "remixseenads", cookies);
                    appendCookie(cookieBuilder, "remixdmgr_tmp", cookies);
                    appendCookie(cookieBuilder, "remixua", cookies);
                    appendCookie(cookieBuilder, "remixbdr", cookies);
                    appendCookie(cookieBuilder, "remixscreen_orient", cookies);
                    appendCookie(cookieBuilder, "remixscreen_dpr", cookies);
                    appendCookie(cookieBuilder, "remixgp", cookies);
                    appendCookie(cookieBuilder, "remixsuc", cookies);
                    appendCookie(cookieBuilder, "remixdmgr", cookies);
                    appendCookie(cookieBuilder, "remixrefkey", cookies);
                    appendCookie(cookieBuilder, "remixscreen_winzoom", cookies);
                    appendCookie(cookieBuilder, "remixdark_color_scheme", cookies);
                    appendCookie(cookieBuilder, "remixcolor_scheme_mode", cookies);
                    appendCookie(cookieBuilder, "remixpuad", cookies);
                    appendCookie(cookieBuilder, "remixnsid", cookies);
                    appendCookie(cookieBuilder, "remixsid", cookies);
                    appendCookie(cookieBuilder, "tmr_detect", cookies);
                    appendCookie(cookieBuilder, "tmr_reqNum", cookies);
                    vkAccount.setCookie(cookieBuilder.toString());
                    vkAccount.setAccessToken(vkService.getAccessToken(vkAccount, restTemplate));
                    dbConnection.updateVkAccCookies(vkAccount);
                    dbConnection.saveVkAccCookies(cookies, vkAccount.getId());
//                    vkService.addFriend(vkAccount, "https://vk.com/id14030702", restTemplate);
//                    vkService.like(vkAccount, "https://vk.com/wall143864226_972", restTemplate);
               //     vkService.subscribe(vkAccount, "https://vk.com/pisfu2023", restTemplate);
                    System.out.println("Updated cookies for vk acc: " + vkAccount.getId() + ", cookie: " + vkAccount.getCookie() + ", access token: " + vkAccount.getAccessToken());
                    //  Thread.sleep(20000);
                } catch (Exception e) {
                    System.out.println("Failed to refresh cookies for " + vkAccount.getId());
                    e.printStackTrace();
                }
            }
            System.out.println(new Date() + " All cookies are updated going to sleep for " + 2400000);
            Thread.sleep(2400000);
        }
    }

    private static void appendCookie(final StringBuilder cookieBuilder, final String cookieName,
                                     final Map<String, String> cookies) {
        String cookieValue = cookies.get(cookieName);
        if (cookieValue != null) {
            cookieBuilder.append(cookieName).append("=").append(cookieValue).append(";");
        }
    }

    private static HttpHeaders populateVkHeaders(final VkAccount vkAccount) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("accept", "*/*");
        headers.add("accept-language", "ru-RU,ru;q=0.9");
        headers.add("content-type", "application/x-www-form-urlencoded");
        headers.setOrigin("https://vk.com");
        headers.add("referer", "https://vk.com/");
        headers.add("sec-ch-ua", "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
        headers.add("sec-ch-ua-mobile", "?0");
        headers.add("sec-ch-ua-platform", "\"Windows\"");
        headers.add("sec-fetch-dest", "empty");
        headers.add("sec-fetch-mode", "cors");
        headers.add("sec-fetch-site", "same-site");
        headers.add("cookie", vkAccount.getCookie());
        return headers;
    }

    //remixstlid=9106082561771058040_LY9JWTvscazcl8JjJ6Fx9deWmgeTxOvzD9BC7TKSGZw;
    // remixstid=1537925247_zIWi4Z2Fr6gU4HgKJR9cMiXymS2ftmlXZAr6ZON4qKT;
    // remixscreen_width=2560;
    // remixscreen_height=1440;
    // remixscreen_dpr=1;
    // remixscreen_depth=24;
    // remixscreen_orient=1;
    // remixdt=0;
    // remixdark_color_scheme=0;
    // remixcolor_scheme_mode=auto;
    // remixgp=77adbaef3dbb0ab4aa2a92c2daea91bb;
    // tmr_lvid=f86bc042d94a7d97774cccefca424f35;
    // tmr_lvidTS=1669041386371;
    // remixua=41%7C-1%7C194%7C2912123832;
    // remixuas=N2UwZTNmZjkzMWNlYWI2ZjZlNTBmYzQy;
    // sui=591061113%2C5rYq7-OKWNb9nM6-3aE2LD1cUu8teeDf3X9435TvsH8;
    // remixsuc=1%3A;
    // remixlang=0;
    // remixseenads=2;
    // remixbdr=0;
    // sua=IMZv_jjLR6nuE-uX_KU2GrlSU-Jn7x3n4qe31TwiW7I%23591061113%5Evk1.a.lKYWwEFNPPwiErd1-vvAnotqb3BCETXhd39s6J5ynUJku1aK2b9MFYz5a1yBVl3uJtUJJWG785NhGwmdsmtQcSqTohmm4ZGNnIs0a_sKGRzqjGjKMij4KjRkEF58BY_-EGup8RvqSjTw82nmNsDxaxCMuT5a8c3IOc1F6zmeyU7Y6EDT2D1u3Z6CgADnTdFb%5E1669041735;
    // remixnsid=vk1.a.sxNQsSbYNzoVC8ZrkxB73SzOLSuOnO1dBbZFRZP5XGXRiGk8YsHxD5YJfcCyDdseHOeSKZjzBWn1p5BCUnDpDrLA3HRM4xuxOXnEyvYu1pBvaIRTtwTrd95648b03UeWjarG2PjrK4f9Ey92Xu4Be7H9cAIiHDhv71kDDGkzpA9YQFykRZDh5YuB0mcvI0vv;
    // remixpuad=R-N2t2QD0-v6crGSOfOr09Vnhn_GFGCN2I2t4JtUQVQ;
    // remixscreen_winzoom=2.33; remixlgck=5f3515712a071c31b5; tmr_reqNum=15
    private static HttpHeaders populateVkHeaders(final Map<String, String> cookies) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("accept", "*/*");
        headers.add("accept-language", "ru-RU,ru;q=0.9");
        headers.add("content-type", "application/x-www-form-urlencoded");
        headers.setOrigin("https://vk.com");
        headers.add("referer", "https://vk.com/");
        headers.add("sec-ch-ua", "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
        headers.add("sec-ch-ua-mobile", "?0");
        headers.add("sec-ch-ua-platform", "\"Windows\"");
        headers.add("sec-fetch-dest", "empty");
        headers.add("sec-fetch-mode", "cors");
        headers.add("sec-fetch-site", "same-site");
        final StringBuilder authCookies = new StringBuilder();
        appendCookie(authCookies, "remixstlid", cookies);
        appendCookie(authCookies, "remixstid", cookies);
        appendCookie(authCookies, "remixscreen_width", cookies);
        appendCookie(authCookies, "remixscreen_height", cookies);
        appendCookie(authCookies, "remixscreen_dpr", cookies);
        appendCookie(authCookies, "remixscreen_depth", cookies);
        appendCookie(authCookies, "remixscreen_orient", cookies);
        appendCookie(authCookies, "remixdt", cookies);
        appendCookie(authCookies, "remixdark_color_scheme", cookies);
        appendCookie(authCookies, "remixcolor_scheme_mode", cookies);
        appendCookie(authCookies, "remixgp", cookies);
        appendCookie(authCookies, "tmr_lvid", cookies);
        appendCookie(authCookies, "tmr_lvidTS", cookies);
        appendCookie(authCookies, "remixua", cookies);
        appendCookie(authCookies, "remixuas", cookies);
        appendCookie(authCookies, "sui", cookies);
        appendCookie(authCookies, "remixsuc", cookies);
        appendCookie(authCookies, "remixseenads", cookies);
        appendCookie(authCookies, "remixbdr", cookies);
        appendCookie(authCookies, "sua", cookies);
        appendCookie(authCookies, "remixpuad", cookies);
        appendCookie(authCookies, "remixscreen_winzoom", cookies);
        appendCookie(authCookies, "tmr_reqNum", cookies);
        headers.add("cookie", authCookies.toString());
        return headers;
    }

    //remixstlid=9106082561771058040_LY9JWTvscazcl8JjJ6Fx9deWmgeTxOvzD9BC7TKSGZw;
    // remixstid=1537925247_zIWi4Z2Fr6gU4HgKJR9cMiXymS2ftmlXZAr6ZON4qKT;
    // remixscreen_width=2560;
    // remixscreen_height=1440;
    // remixscreen_dpr=1;
    // remixscreen_depth=24;
    // remixscreen_orient=1;
    // remixdt=0;
    // remixdark_color_scheme=0;
    // remixcolor_scheme_mode=auto;
    // remixgp=77adbaef3dbb0ab4aa2a92c2daea91bb;
    // tmr_lvid=f86bc042d94a7d97774cccefca424f35;
    // tmr_lvidTS=1669041386371;
    // remixua=41%7C-1%7C194%7C2912123832;
    // remixuas=N2UwZTNmZjkzMWNlYWI2ZjZlNTBmYzQy;
    // sui=591061113%2C5rYq7-OKWNb9nM6-3aE2LD1cUu8teeDf3X9435TvsH8;
    // remixsuc=1%3A;
    // remixlang=0;
    // remixseenads=2;
    // remixbdr=0;
    // sua=IMZv_jjLR6nuE-uX_KU2GrlSU-Jn7x3n4qe31TwiW7I%23591061113%5Evk1.a.lKYWwEFNPPwiErd1-vvAnotqb3BCETXhd39s6J5ynUJku1aK2b9MFYz5a1yBVl3uJtUJJWG785NhGwmdsmtQcSqTohmm4ZGNnIs0a_sKGRzqjGjKMij4KjRkEF58BY_-EGup8RvqSjTw82nmNsDxaxCMuT5a8c3IOc1F6zmeyU7Y6EDT2D1u3Z6CgADnTdFb%5E1669041735;
    // remixnsid=vk1.a.sxNQsSbYNzoVC8ZrkxB73SzOLSuOnO1dBbZFRZP5XGXRiGk8YsHxD5YJfcCyDdseHOeSKZjzBWn1p5BCUnDpDrLA3HRM4xuxOXnEyvYu1pBvaIRTtwTrd95648b03UeWjarG2PjrK4f9Ey92Xu4Be7H9cAIiHDhv71kDDGkzpA9YQFykRZDh5YuB0mcvI0vv;
    // remixpuad=R-N2t2QD0-v6crGSOfOr09Vnhn_GFGCN2I2t4JtUQVQ;
    // remixscreen_winzoom=2.33; remixlgck=5f3515712a071c31b5; tmr_reqNum=15
    private static HttpHeaders populateVkAuthHeaders(final Map<String, String> cookies) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("accept", "*/*");
        headers.add("accept-language", "ru-RU,ru;q=0.9");
        headers.add("content-type", "application/x-www-form-urlencoded");
        headers.setOrigin("https://vk.com");
        headers.add("referer", "https://vk.com/");
        headers.add("sec-ch-ua", "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
        headers.add("sec-ch-ua-mobile", "?0");
        headers.add("sec-ch-ua-platform", "\"Windows\"");
        headers.add("sec-fetch-dest", "empty");
        headers.add("sec-fetch-mode", "cors");
        headers.add("sec-fetch-site", "same-site");
        final StringBuilder authCookies = new StringBuilder();
        appendCookie(authCookies, "remixstlid", cookies);
        appendCookie(authCookies, "remixstid", cookies);
        appendCookie(authCookies, "remixscreen_width", cookies);
        appendCookie(authCookies, "remixscreen_height", cookies);
        appendCookie(authCookies, "remixscreen_dpr", cookies);
        appendCookie(authCookies, "remixscreen_depth", cookies);
        appendCookie(authCookies, "remixscreen_orient", cookies);
        appendCookie(authCookies, "remixdt", cookies);
        appendCookie(authCookies, "remixdark_color_scheme", cookies);
        appendCookie(authCookies, "remixcolor_scheme_mode", cookies);
        appendCookie(authCookies, "remixgp", cookies);
        appendCookie(authCookies, "tmr_lvid", cookies);
        appendCookie(authCookies, "tmr_lvidTS", cookies);
        appendCookie(authCookies, "remixua", cookies);
        appendCookie(authCookies, "remixuas", cookies);
        appendCookie(authCookies, "sui", cookies);
        appendCookie(authCookies, "remixsuc", cookies);
        appendCookie(authCookies, "remixseenads", cookies);
        appendCookie(authCookies, "remixbdr", cookies);
        appendCookie(authCookies, "sua", cookies);
        appendCookie(authCookies, "remixpuad", cookies);
        appendCookie(authCookies, "remixscreen_winzoom", cookies);
        appendCookie(authCookies, "remixlgck", cookies);
        appendCookie(authCookies, "tmr_reqNum", cookies);
        headers.add("cookie", authCookies.toString());
        return headers;
    }

    //remixstlid=9106082561771058040_LY9JWTvscazcl8JjJ6Fx9deWmgeTxOvzD9BC7TKSGZw;
    // remixstid=1537925247_zIWi4Z2Fr6gU4HgKJR9cMiXymS2ftmlXZAr6ZON4qKT;
    // remixscreen_width=2560;
    // remixscreen_height=1440;
    // remixscreen_dpr=1;
    // remixscreen_depth=24;
    // remixscreen_orient=1;
    // remixdt=0;
    // remixdark_color_scheme=0;
    // remixcolor_scheme_mode=auto;
    // remixgp=77adbaef3dbb0ab4aa2a92c2daea91bb;
    // tmr_lvid=f86bc042d94a7d97774cccefca424f35;
    // tmr_lvidTS=1669041386371;
    // remixua=41%7C-1%7C194%7C2912123832;
    // remixuas=N2UwZTNmZjkzMWNlYWI2ZjZlNTBmYzQy;
    // sui=591061113%2C5rYq7-OKWNb9nM6-3aE2LD1cUu8teeDf3X9435TvsH8;
    // remixsuc=1%3A;
    // remixlang=0;
    // remixseenads=2; remixbdr=0;
    // remixscreen_winzoom=2.33;
    // sua=aQVW0f2gIbXhyEqPNt6Yomd-jJ8X6wwaUhSeC-UonQk%23591061113%5Evk1.a.lKYWwEFNPPwiErd1-vvAnotqb3BCETXhd39s6J5ynUJku1aK2b9MFYz5a1yBVl3uJtUJJWG785NhGwmdsmtQcSqTohmm4ZGNnIs0a_sKGRzqjGjKMij4KjRkEF58BY_-EGup8RvqSjTw82nmNsDxaxCMuT5a8c3IOc1F6zmeyU7Y6EDT2D1u3Z6CgADnTdFb%5E1669043123;
    // remixnsid=vk1.a.4QBkHe7qnDvBk3j4Ow4gNvL8NJ1S_Ix_EEM8d-4k3TLczsqK7kXfWfJfPcl0WJs3dLfCnHGKEs1qHvpxyiTdlJ0_2KGpE-sOVLT7JTzbbw056RxjL3txdLF6ECKjQEVPtT5tE2lxMJe5joMn1RF-hKAnKVPbgABC5BV9brPsUcqouL8-gqzD2QnZNKH-JcC4;
    // remixpuad=wXuNSIN6bsk4b2CLdQ4rwKddfqbMZXPXlfY6ePe5iHo;
    // tmr_reqNum=16;
    // remixlgck=2cf6d1fa050f8f4d09;
    // remixsts=%7B%22data%22%3A%5B%5B1669043851%2C%22web_dark_theme%22%2C%22auto%22%2C%22vkcom_light%22%2C0%5D%5D%2C%22uniqueId%22%3A528304885%7D
    private static HttpHeaders populateVkAuthGetSavedUsersHeaders(final Map<String, String> cookies) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("accept", "*/*");
        headers.add("accept-language", "ru-RU,ru;q=0.9");
        headers.add("content-type", "application/x-www-form-urlencoded");
        headers.setOrigin("https://vk.com");
        headers.add("referer", "https://vk.com/");
        headers.add("sec-ch-ua", "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
        headers.add("sec-ch-ua-mobile", "?0");
        headers.add("sec-ch-ua-platform", "\"Windows\"");
        headers.add("sec-fetch-dest", "empty");
        headers.add("sec-fetch-mode", "cors");
        headers.add("sec-fetch-site", "same-site");
        final StringBuilder authCookies = new StringBuilder();
        appendCookie(authCookies, "remixstlid", cookies);
        appendCookie(authCookies, "remixstid", cookies);
        appendCookie(authCookies, "remixscreen_width", cookies);
        appendCookie(authCookies, "remixscreen_height", cookies);
        appendCookie(authCookies, "remixscreen_dpr", cookies);
        appendCookie(authCookies, "remixscreen_depth", cookies);
        appendCookie(authCookies, "remixscreen_orient", cookies);
        appendCookie(authCookies, "remixdt", cookies);
        appendCookie(authCookies, "remixdark_color_scheme", cookies);
        appendCookie(authCookies, "remixcolor_scheme_mode", cookies);
        appendCookie(authCookies, "remixgp", cookies);
        appendCookie(authCookies, "tmr_lvid", cookies);
        appendCookie(authCookies, "tmr_lvidTS", cookies);
        appendCookie(authCookies, "remixua", cookies);
        appendCookie(authCookies, "remixuas", cookies);
        appendCookie(authCookies, "sui", cookies);
        appendCookie(authCookies, "remixsuc", cookies);
        appendCookie(authCookies, "remixlang", cookies);
        appendCookie(authCookies, "remixseenads", cookies);
        appendCookie(authCookies, "remixbdr", cookies);
        appendCookie(authCookies, "sua", cookies);
        appendCookie(authCookies, "remixlang", cookies);
        appendCookie(authCookies, "remixpuad", cookies);
        appendCookie(authCookies, "remixscreen_winzoom", cookies);
        appendCookie(authCookies, "remixlgck", cookies);
        appendCookie(authCookies, "tmr_reqNum", cookies);
        appendCookie(authCookies, "remixsts", cookies);
        headers.add("cookie", authCookies.toString());
        return headers;
    }
}
