package service;

import bo.VkAccount;
import da.DbConnection;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import util.IConstants;
import util.ProxyCustomizer;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static util.SeleniumUtils.findElement;
import static util.SeleniumUtils.waitUntilElementWillBePresent;

public class VkService {
    private static final DbConnection dbConnection = DbConnection.getInstance();
    private static VkService INSTANCE;

    public static VkService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VkService();
        }
        return INSTANCE;
    }

    public String getHtml(final VkAccount vkAccount, final String link, final boolean allowRestrictedWords, final RestTemplate restTemplate) throws Exception {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        final HttpHeaders headers = new HttpHeaders();
        headers.add("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        headers.add("accept-language", "en-US,en;q=0.9,ru-RU;q=0.8,ru;q=0.7");
        headers.add("cache-control", "max-age=0");
        headers.add("sec-ch-ua", "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
        headers.add("sec-ch-ua-mobile", "?0");
        headers.add("sec-ch-ua-platform", "\"Windows\"");
        headers.add("sec-fetch-dest", "document");
        headers.add("sec-fetch-mode", "navigate");
        headers.add("sec-fetch-site", "same-origin");
        headers.add("sec-fetch-user", "?1");
        headers.add("upgrade-insecure-requests", "1");
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 6.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
        headers.add("cookie", vkAccount.getCookie());
        try {
            final String profileHtml = restTemplate.exchange(link, HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
            if (allowRestrictedWords) {
                if (profileHtml.contains("<div class=\"HiddenPostBlank__header\">Запись удалена</div>")
                        || profileHtml.contains("<title>Запись удалена</title>")
                        || profileHtml.contains("<div class=\"HiddenPostBlank__header\">Post deleted</div>")
                        || profileHtml.contains("<title>Post deleted</title>")
                        || profileHtml.contains("<div class=\"message_page_title\">Ошибка</div>")
                        || profileHtml.contains("<div class=\"message_page_title\">Error</div>")
                ) {
                    System.out.println(link + " is added to blacklist");
                    DbConnection.getInstance().blackListLink(link);
                    return null;
                }
            } else if (DbConnection.getInstance().containsRestrictedKeyword(profileHtml)) {
                System.out.println(link + " is added to blacklist");
                DbConnection.getInstance().blackListLink(link);
                return null;
            }
            return profileHtml;
        } catch (ResourceAccessException e) {
            vkAccount.setProxyUnauthorized(true);
            throw e;
        }

    }

    public boolean like(final VkAccount vkAccount, final String link, final RestTemplate restTemplate, final String profileHtml) throws Exception {
        if (link.contains("z=photo")) {
            return likePhoto(vkAccount, "https://vk.com/" + link.split("z=")[1], restTemplate, profileHtml);
        }
        System.out.println(new Date() + " vk acc is going to like: " + link);
        if (!checkHtml(profileHtml, true, link)) {
            return false;
        }
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", vkAccount.getCookie());
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
        String hash = null;
        final Matcher publicHashMatcher = Pattern.compile("data-reaction-hash=\"(.*?)\"", Pattern.DOTALL).matcher(profileHtml);
        if (publicHashMatcher.find()) {
            hash = publicHashMatcher.group(1);

        }
        String[] linkParts = link.split("/");
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("act", "a_set_reaction");
        body.add("al", "1");
        body.add("from", "wall_one");
        body.add("hash", hash);
        body.add("object", linkParts[linkParts.length - 1].split("\\?")[0]);
        body.add("reaction_id", "0");
        body.add("wall", "2");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        final ResponseEntity<String> response = restTemplate.exchange("https://vk.com/like.php?act=a_set_reaction",
                HttpMethod.POST, request, String.class);
        final boolean result = response.getBody().toString().contains("\"payload\":[0,");
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " pressed like for " + link + " vk response " + result);
        dbConnection.logAction("like", link, vkAccount.getId(), result, null);
        return result;

    }

    public boolean vote(final VkAccount vkAccount, final String link, final RestTemplate restTemplate, final Integer optionNumber, String profileHtml) throws Exception {
        try {
            if (dbConnection.hadAction(vkAccount.getId(), link, "vote")) {
                System.out.println(new Date() + " vk acc " + vkAccount.getId() + " already voted for " + link);
                return true;
            }
            System.out.println(new Date() + " vk acc is going to vote: " + link);
            if (!checkHtml(profileHtml, true, link)) {
                return false;
            }
            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("cookie", vkAccount.getCookie());
            httpHeaders.set("content-type", "application/x-www-form-urlencoded");
            String hash = null;
            final Matcher publicHashMatcher = Pattern.compile("data-hash=\"(.*?)\"", Pattern.DOTALL).matcher(profileHtml);
            while (publicHashMatcher.find()) {
                hash = publicHashMatcher.group(1);
            }
            String votingId = null;
            Matcher votingIdMatcher = Pattern.compile("media_voting\\s_media_voting(.*?)\\s", Pattern.DOTALL).matcher(profileHtml);
            if (votingIdMatcher.find()) {
                votingId = votingIdMatcher.group(1);
            }
            if (StringUtils.isEmpty(hash)) {
                System.out.println(link + " is added to blacklist");
                DbConnection.getInstance().blackListLink(link);
                return false;
            }
            String optionId = null;
            if (votingId == null) {
                final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
                final Matcher voteBoxIdMatcher = Pattern.compile("data-id=\"(.*?)\"", Pattern.DOTALL).matcher(profileHtml);
                if (voteBoxIdMatcher.find()) {
                    body.add("act", "get_layout");
                    body.add("al", "1");
                    body.add("for_box", "true");
                    body.add("ref", "wall");
                    body.add("voting_id", voteBoxIdMatcher.group(1));
                    ResponseEntity<String> boxLayoutResponse = restTemplate.exchange("https://vk.com/al_voting.php?act=get_layout", HttpMethod.POST, new HttpEntity<>(body, httpHeaders), String.class);
                    profileHtml = boxLayoutResponse.getBody().split("\"Опрос\",\"")[1].split("\"]")[0];
                    final Matcher hashMatcher = Pattern.compile("data-hash=\"(.*?)\"", Pattern.DOTALL).matcher(profileHtml.replace("\\", ""));
                    while (hashMatcher.find()) {
                        hash = hashMatcher.group(1);
                    }
                    if (StringUtils.isEmpty(hash)) {
                        System.out.println(link + " is added to blacklist");
                        DbConnection.getInstance().blackListLink(link);
                        return false;
                    }
                    votingIdMatcher = Pattern.compile("media_voting _media_voting(.*?) ", Pattern.DOTALL).matcher(profileHtml);
                    if (votingIdMatcher.find()) {
                        votingId = votingIdMatcher.group(1);
                    }
                    final Matcher optionIdMatcher = Pattern.compile("media_voting_option_wrap _media_voting_option(.*?)\"", Pattern.DOTALL).matcher(profileHtml);
                    int index = 1;
                    do {
                        optionIdMatcher.find();
                        optionId = optionIdMatcher.group(1);
                        index++;
                    } while (index <= optionNumber);
                    optionId = optionId.replace("\\", "");
                }
            } else {
                final Matcher optionIdMatcher = Pattern.compile("<div class=\"media_voting_option_wrap\\s_media_voting_option(.*?)\\s", Pattern.DOTALL).matcher(profileHtml);
                int index = 1;
                do {
                    optionIdMatcher.find();
                    optionId = optionIdMatcher.group(1);
                    index++;
                } while (index <= optionNumber);
            }
            final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            body.add("act", "vote");
            body.add("al", "1");
            body.add("hash", hash);
            String[] linkParts = link.split("/");
            body.add("material", linkParts[linkParts.length - 1].split("\\?")[0]);
            body.add("option_ids", optionId);
            body.add("ref", "wall");
            body.add("voting_id", votingId);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
            final ResponseEntity<String> response = restTemplate.exchange("https://vk.com/al_voting.php?act=vote",
                    HttpMethod.POST, request, String.class);
            String responseBody = response.getBody();
//            if (responseBody.contains("опрос<span class=\"media_voting_separator\">&sdot;</span>завершён") || responseBody.contains("Ошибка доступа")) {
//                System.out.println(link + " is added to blacklist");
//                DbConnection.getInstance().blackListLink(link);
//                return false;
//            }
            final boolean result = responseBody.contains("\"payload\":[0,");
            System.out.println(new Date() + " vk acc " + vkAccount.getId() + " voted for " + link + " vk response " + result);
            dbConnection.logAction("vote", link, vkAccount.getId(), result, null);
            if (!result) {
                dbConnection.blackListLink(link);
            }
            return result;
        } catch (Exception e) {
            System.out.println(link + " is added to blacklist");
            DbConnection.getInstance().blackListLink(link);
            return false;

        }
    }

    public boolean likeReply(final VkAccount vkAccount, final String link, RestTemplate restTemplate, final String profileHtml) throws Exception {
        System.out.println(new Date() + " vk acc is going to like reply: " + link);
        if (!checkHtml(profileHtml, true, link)) {
            return false;
        }
        String[] linkParts = link.split("/");
        final String object;
        if (link.contains("_r")) {
            String[] linkIds = link.split("wall-")[1].split("_");
            object = "wall_reply-" + linkIds[0] + "_" + linkIds[2].replace("r", "");
        } else {
            if (!link.contains("-")) {
                object = "wall_reply" + linkParts[linkParts.length - 1].split("\\?")[0].split("_")[0].replace("wall", "") + "_" + link.split("reply=")[1];
            } else {
                object = "wall_reply-" + linkParts[linkParts.length - 1].split("\\?")[0].split("-")[1].split("_")[0] + "_" + link.split("reply=")[1];
            }
        }
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", vkAccount.getCookie());
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
        String hash = null;
        final Matcher publicHashMatcher = Pattern.compile("Likes.toggle\\(this, event, '" + object + "', '(.*?)'\\)").matcher(profileHtml);
        if (publicHashMatcher.find()) {
            hash = publicHashMatcher.group(1);
        }
        if (hash == null) {
            System.out.println(link + " is added to blacklist");
            DbConnection.getInstance().blackListLink(link);
            return false;
        }
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("act", "a_do_like");
        body.add("al", "1");
        body.add("from", "wall_one");
        body.add("from_widget", "0");
        body.add("hash", hash);
        body.add("object", object);
        body.add("wall", "2");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        ResponseEntity response;
        try {
            response = restTemplate.exchange("https://vk.com/like.php?act=a_set_reaction",
                    HttpMethod.POST, request, String.class);
        } catch (ResourceAccessException e) {
            restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
            response = restTemplate.exchange("https://vk.com/like.php?act=a_set_reaction",
                    HttpMethod.POST, request, String.class);
        }
        boolean result = response.getBody().toString().contains("\"payload\":[0,");
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " pressed like for reply " + link + " vk response " + result);
        dbConnection.logAction("likeReply", link, vkAccount.getId(), result, null);
        return result;
    }

    public boolean comment(final VkAccount vkAccount, String link, final String message, final RestTemplate restTemplate, final String profileHtml) throws Exception {
        if (!checkHtml(profileHtml, true, link)) {
            return false;
        }
        if (link.contains("?w=")) {
            link = "https://vk.com/" + link.split("w=")[1];
        }
        System.out.println(new Date() + " vk acc is going to comment: " + link);
        if (dbConnection.isLinkOnCooldown(link)) {
            System.out.println("Link to comment [" + link + "] is on cooldown!!!! not going to happen");
            return true;
        }
        String[] linkParts = link.split("/");
        final String object = linkParts[linkParts.length - 1].split("\\?")[0].replace("wall", "");
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", vkAccount.getCookie());
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
        String hash = null;
        Matcher publicHashMatcher = Pattern.compile("id=\"post_hash-(.*?)\" value=\"(.*?)\"").matcher(profileHtml);
        if (publicHashMatcher.find()) {
            hash = publicHashMatcher.group(2);
        }
        if (hash == null) {
            publicHashMatcher = Pattern.compile("\"post_hash\":\"(.*?)\",", Pattern.DOTALL).matcher(profileHtml);
            if (publicHashMatcher.find()) {
                hash = publicHashMatcher.group(1);
            }
        }
        if (hash == null) {
            System.out.println(link + " is added to blacklist");
            DbConnection.getInstance().blackListLink(link);
            return false;
        }
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("Message", message);
        body.add("act", "post");
        body.add("al", "1");
        body.add("from", "");
        body.add("hash", hash);
        body.add("need_last", "0");
        body.add("only_new", "0");
        body.add("order", "desc");
        body.add("ref", "wall_one");
        body.add("reply_to", object);
        body.add("reply_to_msg", "");
        body.add("reply_to_user", "0");
        body.add("timestamp", String.valueOf(new Date().getTime()));
        //body.add("top_replies", "167880,167878");
        body.add("type", "full");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        final ResponseEntity response = restTemplate.exchange("https://vk.com/al_wall.php?act=post",
                HttpMethod.POST, request, String.class);
        final boolean result = response.getBody().toString().contains("\"payload\":[0,");
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " commented " + link + " vk response " + result);
        dbConnection.logAction("comment", link, vkAccount.getId(), result, null);
        return result;
    }

    public boolean showPost(final VkAccount vkAccount, final String link, RestTemplate restTemplate, final String profileHtml) throws Exception {
        System.out.println(new Date() + " vk acc is going to view: " + link);
        if (!checkHtml(profileHtml, true, link)) {
            return false;
        }
        String hash = null;
        final Matcher publicHashMatcher = Pattern.compile("post_view_hash=\"(.*?)\"", Pattern.DOTALL).matcher(profileHtml);
        if (publicHashMatcher.find()) {
            hash = publicHashMatcher.group(1);
        }
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", vkAccount.getCookie());
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
        httpHeaders.set("referer", link);
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        final String[] linkParts = link.split("/");
        final String object;
        if (link.contains("?w=")) {
            object = link.split("\\?w=")[1].replace("wall", "");
        } else {
            object = linkParts[linkParts.length - 1].split("\\?")[0].replace("wall", "");
        }
        body.add("act", "seen");
        body.add("al", "1");
        String[] objectParts = object.split("_");
        body.add("data", objectParts[0] + "_u" + objectParts[1] + ":-1::na");
        body.add("meta", object + ":830:1329");
        body.add("hash", hash);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        ResponseEntity response;
        try {
            response = restTemplate.exchange("https://vk.com/al_page.php?act=seen",
                    HttpMethod.POST, request, String.class);
        } catch (ResourceAccessException e) {
            restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
            response = restTemplate.exchange("https://vk.com/al_page.php?act=seen",
                    HttpMethod.POST, request, String.class);
        }
        final boolean result = response.getBody().toString().contains("\"payload\":[0,");
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " viewed " + link + " vk response " + result);
        dbConnection.logAction("showPost", link, vkAccount.getId(), result, null);
        return result;
    }

    public boolean showVideo(final VkAccount vkAccount, final String link, final RestTemplate restTemplate, final String profileHtml) throws Exception {
        System.out.println(new Date() + " vk acc is going to view video: " + link);
        if (!checkHtml(profileHtml, true, link)) {
            return false;
        }
        String hash = null;
        final Matcher publicHashMatcher = Pattern.compile("\"view_hash\":\"(.*?)\"", Pattern.DOTALL).matcher(profileHtml);
        if (publicHashMatcher.find()) {
            hash = publicHashMatcher.group(1);
        }
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", vkAccount.getCookie());
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
        httpHeaders.set("referer", link);
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        final String object;
        if (link.contains("z=")) {
            object = link.split("z=")[1];
        } else {
            final String[] linkParts = link.split("/");
            object = linkParts[linkParts.length - 1].split("\\?")[0];
        }
        body.add("al", "1");
        body.add("curr_res", "2");
        body.add("hash", hash);
        body.add("max_res", "4");
        body.add("module", "community_videos");
        body.add("oid", "-134699547");
        body.add("player", "html5");
        body.add("vid", object.split("_")[1]);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        final ResponseEntity response = restTemplate.exchange("https://vk.com/al_video.php?act=inc_view_counter",
                HttpMethod.POST, request, String.class);
        final boolean result = response.getBody().toString().contains("\"payload\":[0,");
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " viewed video " + link + " vk response " + result);
        dbConnection.logAction("showVideo", link, vkAccount.getId(), result, null);
        return result;
    }

    public boolean likeVideo(final VkAccount vkAccount, final String link, final RestTemplate restTemplate, final String profileHtml) throws Exception {
        System.out.println(new Date() + " vk acc is going to like video: " + link);
        if (!checkHtml(profileHtml, true, link)) {
            return false;
        }
        String hash = null;
        final Matcher publicHashMatcher = Pattern.compile("like_hash&quot;:&quot;(.*?)&quot;", Pattern.DOTALL).matcher(profileHtml);
        if (publicHashMatcher.find()) {
            hash = publicHashMatcher.group(1);
        }
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", vkAccount.getCookie());
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
        httpHeaders.set("referer", link);
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        final String object;
        if (link.contains("z=")) {
            object = link.split("z=")[1].split("%")[0];
        } else {
            final String[] linkParts = link.split("/");
            object = linkParts[linkParts.length - 1].split("\\?")[0].split("%")[0];
        }
        body.add("act", "a_do_like");
        body.add("al", "1");
        body.add("from", "videoview");
        body.add("from_widget", "0");
        body.add("object", object);
        body.add("wall", "2");
        body.add("hash", hash);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        final ResponseEntity response = restTemplate.exchange("https://vk.com/like.php?act=a_do_like",
                HttpMethod.POST, request, String.class);
        final boolean result = response.getBody().toString().contains("\"payload\":[0,");
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " liked video " + link + " vk response " + result);
        dbConnection.logAction("likeVideo", link, vkAccount.getId(), result, null);
        return result;

    }

    public boolean likePhoto(final VkAccount vkAccount, final String link, final RestTemplate restTemplate, final String profileHtml) throws Exception {
        System.out.println(new Date() + " vk acc is going to like photo: " + link);
        if (!checkHtml(profileHtml, true, link)) {
            return false;
        }
        String[] linkParts = link.split("/");
        final String object = linkParts[linkParts.length - 1].split("\\?")[0];
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", vkAccount.getCookie());
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
        String hash = null;
        //"hash":"c647c6880a8f91aca1"
        final Matcher publicHashMatcher = Pattern.compile("Likes\\.toggle\\(this, event, '" + object + "', '(.*?)'\\)", Pattern.DOTALL).matcher(profileHtml);
        if (publicHashMatcher.find()) {
            hash = publicHashMatcher.group(1);
        }
        System.out.println(hash);
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("act", "a_do_like");
        body.add("al", "1");
        body.add("from", "photo_viewer");
        body.add("from_widget", "0");
        body.add("hash", hash);
        body.add("object", object);
        body.add("wall", "2");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        final ResponseEntity response = restTemplate.exchange("https://vk.com/like.php?act=a_do_like",
                HttpMethod.POST, request, String.class);
        final boolean result = response.getBody().toString().contains("\"payload\":[0,");
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " pressed like for photo " + link + " vk response " + result);
        dbConnection.logAction("likePhoto", link, vkAccount.getId(), result, null);
        return result;
    }

    private boolean checkHtml(final String profileHtml, final boolean allowRestrictedWords, final String link) throws Exception {
        if (allowRestrictedWords) {
            if (profileHtml == null) {
                System.out.println("Provided null html for link " + link);
            }
            if (profileHtml.contains("<div class=\"HiddenPostBlank__header\">Запись удалена</div>")
                    || profileHtml.contains("<title>Запись удалена</title>")
                    || profileHtml.contains("<div class=\"HiddenPostBlank__header\">Post deleted</div>")
                    || profileHtml.contains("<title>Post deleted</title>")
                    || profileHtml.contains("<div class=\"message_page_title\">Ошибка</div>")
                    || profileHtml.contains("<div class=\"message_page_title\">Error</div>")
            ) {
                return false;
            }
        } else if (DbConnection.getInstance().containsRestrictedKeyword(profileHtml)) {
            System.out.println(link + " is added to blacklist");
            DbConnection.getInstance().blackListLink(link);
            return false;
        }
        return true;
    }

    public boolean likeClip(final VkAccount vkAccount, final String link, final RestTemplate restTemplate, final String profileHtml) throws Exception {
        System.out.println(new Date() + " vk acc is going to like clip: " + link);
        if (!checkHtml(profileHtml, true, link)) {
            return false;
        }
//        String[] linkParts = link.split("/");
//        final String object = linkParts[linkParts.length - 1].split("\\?")[0];
        final String object = link.split("z=")[1];
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", vkAccount.getCookie());
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
        String hash = null;
        //onclick="Likes.toggle(this, event, 'clip647743113_456239120', '4ab01b6a14349dc62f');"
        final Matcher publicHashMatcher = Pattern.compile("'" + object + "', '(.*?)'", Pattern.DOTALL).matcher(profileHtml);
        publicHashMatcher.find();
        hash = publicHashMatcher.group(1);
        System.out.println(hash);
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("act", "a_do_like");
        body.add("al", "1");
        body.add("from", "videoview");
        body.add("from_widget", "0");
        body.add("hash", hash);
        body.add("object", object);
        body.add("wall", "2");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        final ResponseEntity response = restTemplate.exchange("https://vk.com/like.php?act=a_do_like",
                HttpMethod.POST, request, String.class);
        boolean result = response.getBody().toString().contains("\"payload\":[0,");
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " pressed like for clip " + link + " vk response " + result);
        dbConnection.logAction("likeClip", link, vkAccount.getId(), result, null);
        return result;
    }

    public boolean share(final VkAccount vkAccount, final String link, final RestTemplate restTemplate, final String profileHtml) throws Exception {
        System.out.println(new Date() + " vk acc is going to share: " + link);
        if (link.contains("photo") || link.contains("clip")) {
            return false;
        }
        if (!checkHtml(profileHtml, false, link)) {
            return false;
        }
        if (dbConnection.isLinkOnCooldown(link)) {
            System.out.println("Link to share [" + link + "] is on cooldown!!!! not going to happen");
            return true;
        }
        String[] linkParts = link.split("/");
        final String object = linkParts[linkParts.length - 1].split("\\?")[0];
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", vkAccount.getCookie());
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
        final MultiValueMap<String, String> bodyForBoxHashRequest = new LinkedMultiValueMap<String, String>();
        bodyForBoxHashRequest.add("act", "publish_box");
        bodyForBoxHashRequest.add("al", "1");
        bodyForBoxHashRequest.add("from_widget", "0");
        bodyForBoxHashRequest.add("object", object);
        final String publishBoxBody = restTemplate.exchange("https://vk.com/like.php?act=publish_box", HttpMethod.POST, new HttpEntity<>(bodyForBoxHashRequest, httpHeaders), String.class).getBody();
        String hash = null;
        final Matcher publicHashMatcher = Pattern.compile("shHash:\\s'(.*)',").matcher(publishBoxBody);
        if (publicHashMatcher.find()) {
            hash = publicHashMatcher.group().split("'")[1];
        }
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("act", "a_do_publish");
        body.add("al", "1");
        body.add("close_comments", "0");
        body.add("friends_only", "0");
        body.add("from", "box");
        body.add("hash", hash);
        body.add("list", "");
        body.add("mark_as_ads", "0");
        body.add("mute_notifications", "0");
        body.add("object", object);
        body.add("ret_data", "1");
        body.add("to", "0");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        final ResponseEntity response = restTemplate.exchange("https://vk.com/like.php?act=a_do_publish",
                HttpMethod.POST, request, String.class);
        boolean result = response.getBody().toString().contains("\"payload\":[0,");
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " shared " + link + " vk response " + result);
        dbConnection.logAction("share", link, vkAccount.getId(), result, null);
        if (!result) {
            dbConnection.blackListLink(link);
        }
        return result;
    }

    //    public boolean recommend(VkAccount vkAccount, String link, RestTemplate restTemplate) throws Exception {
//        System.out.println(new Date() + " vk acc is going to recommend: " + link);
//        final String profileHtml = getHtml(vkAccount, link);
//        if (profileHtml.contains("<h1 class=\"page_name\">Займ")
//                || profileHtml.contains("<h1 class=\"page_name\">ЗАЙМ")
//                || profileHtml.contains("<h1 class=\"page_name\">займ")
//        ) {
//            System.out.println("Займбот хуйни: " + link);
//            DbConnection.getInstance().blackListLink(link);
//            return false;
//        }
//        final HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
//        httpHeaders.set("cookie", vkAccount.getCookie());
//        String groupId = null;
//        String hash = null;
//        final Matcher groupMatcher = Pattern.compile("(?<=\\bonclick=\")[^\"]*").matcher(profileHtml);
//        while (groupMatcher.find()) {
//            String group = groupMatcher.group();
//            if (group.contains("Groups.enter")) {
//                //Groups.enter(this, 82277766, &#39;63ec2acc7abe0e9d16&#39;, &#39;_&#39;)
//                String[] inviteParams = group.split(",");
//                groupId = inviteParams[1].trim();
//                hash = inviteParams[2].trim().replaceAll("&#39;", "");
//                break;
//            }
//        }
//        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
//        final String url;
//        if (groupId == null) {
//            final Matcher publicGroupIdMatcher = Pattern.compile("\"public_id\":(\\d+)").matcher(profileHtml);
//            if (publicGroupIdMatcher.find()) {
//                groupId = publicGroupIdMatcher.group().split(":")[1];
//            }
//            body.add("act", "a_enter");
//            body.add("al", "1");
//            body.add("pid", groupId);
//            body.add("ref", "community_page");
//            url = "https://vk.com/al_public.php?act=a_enter";
//        } else {
//            body.add("act", "group_like_toggle");
//            body.add("al", "1");
//            body.add("group_id", groupId);
//            body.add("is_like", "1");
//            body.add("ref", "community_page");
//            url = "https://vk.com/al_groups.php?act=group_like_toggle";
//        }
//        if (hash == null) {
//            final Matcher publicHashMatcher = Pattern.compile("\"enterHash\":(\\\".*\\\")").matcher(profileHtml);
//            if (publicHashMatcher.find()) {
//                hash = publicHashMatcher.group().split(":")[1].split(",")[0].replaceAll("\"", "");
//            }
//        }
//        body.add("hash", hash);
//
//        final ResponseEntity response = restTemplate.exchange(url,
//                HttpMethod.POST, new HttpEntity<>(body, httpHeaders), String.class);
//        Object responseBody = response.getBody();
//        if (responseBody != null && responseBody.toString().contains("\"payload\":[\"2\"")) {
//            DbConnection.getInstance().vkAccountCantSubscribeAnymore(vkAccount.getId());
//            return false;
//        }
//        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " recommended for " + link + " vk response " + responseBody);
//        return true;
//    }
    public boolean subscribe(final VkAccount vkAccount, final String link, RestTemplate restTemplate, final ChromeDriver driver, final String profileHtml) throws Exception {

        if (dbConnection.hadAction(vkAccount.getId(), link, "subscribe")) {
            System.out.println(new Date() + " vk acc " + vkAccount.getId() + " already subscribed for " + link);
            return true;
        }
        System.out.println(new Date() + " vk acc is going to subscribe: " + link);
        if (!checkHtml(profileHtml, false, link)) {
            return false;
        }
        if (dbConnection.isLinkOnCooldown(link)) {
            System.out.println("Link to subscribe [" + link + "] is on cooldown!!!! not going to happen");
            return true;
        }
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
//        final StringBuilder cookies = new StringBuilder();
//        for (Cookie cookie : driver.manage().getCookies()) {
//            cookies.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
//        }
//        vkAccount.setCookie(cookies.toString());
        httpHeaders.set("cookie", vkAccount.getCookie());
        String groupId = null;
        String hash = null;
        final Matcher groupMatcher = Pattern.compile("(?<=\\bonclick=\")[^\"]*").matcher(profileHtml);
        while (groupMatcher.find()) {
            String group = groupMatcher.group();
            if (group.contains("Groups.enter") || group.contains("Groups.leave")) {
                //Groups.enter(this, 82277766, &#39;63ec2acc7abe0e9d16&#39;, &#39;_&#39;)
                String[] inviteParams = group.split(",");
                groupId = inviteParams[1].trim();
                hash = inviteParams[2].trim().replaceAll("'", "");
                break;
            }
        }
        //     data-trackcode="
//        final Matcher trackCodeMatcher = Pattern.compile("data-trackcode=\"(.*?)\"", Pattern.DOTALL).matcher(profileHtml);
//        trackCodeMatcher.find();
//        final String trackCode = trackCodeMatcher.group(1);
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        final String url;
        final Boolean isPublic;
        if (groupId == null) {
            final Matcher publicGroupIdMatcher = Pattern.compile("\"public_id\":(\\d+)").matcher(profileHtml);
            if (publicGroupIdMatcher.find()) {
                groupId = publicGroupIdMatcher.group().split(":")[1];
            }
            body.add("act", "a_enter");
            body.add("al", "1");
            body.add("pid", groupId);
            body.add("ref", "community_page");
            //     body.add("trackcode", trackCode);
            url = "https://vk.com/al_public.php?act=a_enter";
            isPublic = true;
        } else {
            body.add("act", "enter");
            body.add("context", "_");
            body.add("al", "1");
            body.add("gid", groupId);
            body.add("ref", "community_page");
            //   body.add("trackcode", trackCode);
            url = "https://vk.com/al_groups.php?act=enter";
            isPublic = false;
        }
        if (hash == null) {
            final Matcher publicHashMatcher = Pattern.compile("\"enterHash\":(\\\".*\\\")").matcher(profileHtml);
            if (publicHashMatcher.find()) {
                hash = publicHashMatcher.group().split(":")[1].split(",")[0].replaceAll("\"", "");
            }
        }
        body.add("hash", hash);
        ResponseEntity response;
        try {
            response = restTemplate.exchange(url,
                    HttpMethod.POST, new HttpEntity<>(body, httpHeaders), String.class);
        } catch (ResourceAccessException e) {
            restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
            response = restTemplate.exchange(url,
                    HttpMethod.POST, new HttpEntity<>(body, httpHeaders), String.class);
        }

        Object responseBody = response.getBody();
        if (responseBody != null && responseBody.toString().contains("\"payload\":[\"2\"")) {
            DbConnection.getInstance().vkAccountCantSubscribeAnymore(vkAccount.getId());
            return false;
        }
        boolean result = responseBody.toString().contains("\"payload\":[0,");
        dbConnection.logAction("subscribe", link, vkAccount.getId(), result, !result ? responseBody.toString().substring(0, responseBody.toString().length() > 30000 ? 30000 : responseBody.toString().length() - 1) : null);
        if (responseBody != null && (responseBody.toString().contains("\"payload\":[\"8\",[\"\\\"Ошибка доступа\\\"\"") || responseBody.toString().contains("\"payload\":[\"5\""))) {
            // System.out.println(profileHtml);
            System.out.println(new Date() + " vk acc " + vkAccount.getId() + " already subscribed for " + link);
            return true;
//            System.out.println(new Date() + " vk acc " + vkAccount.getId() + " going to resubscribe for " + link);
//            if (isPublic) {
//                body.add("act", "a_leave");
//                restTemplate.exchange("https://vk.com/al_public.php?act=a_leave",
//                        HttpMethod.POST, new HttpEntity<>(body, httpHeaders), String.class);
//                body.add("act", "a_enter");
//            } else {
//
//                body.add("act", "leave");
//                restTemplate.exchange("https://vk.com/al_groups.php?act=leave",
//                        HttpMethod.POST, new HttpEntity<>(body, httpHeaders), String.class);
//                body.add("gid", groupId);
//            }
//            final ResponseEntity<String> resubscribeCall = restTemplate.exchange(url,
//                    HttpMethod.POST, new HttpEntity<>(body, httpHeaders), String.class);
//            result = resubscribeCall.getBody().toString().contains("\"payload\":[0,");
//            System.out.println(new Date() + " vk acc " + vkAccount.getId() + " resubscribed for " + link + " vk response " + result);
//            if (!result) {
//                vkAccount.setFailedSubscriptionsInRow(3);
//            }
        } else {
            System.out.println(new Date() + " vk acc " + vkAccount.getId() + " subscribed for " + link + " vk response " + result);
        }
//        if (!result) {
//            vkAccount.incrementFailedSub();
//            if (vkAccount.getFailedSubscriptionsInRow() > 2 && driver != null) {
//                System.out.println("Going to refresh acc cookies " + vkAccount.getId());
//                refreshCookie(driver, vkAccount, vkAccount.getRestTemplate(), true);
//                vkAccount.setFailedSubscriptionsInRow(0);
//            }
//        } else {
//            vkAccount.setFailedSubscriptionsInRow(0);
//        }
        return result;
    }

    public boolean addFriend(final VkAccount vkAccount, String link, final RestTemplate restTemplate, final String profileHtml) throws Exception {
        if (link.contains("http:")) {
            link = link.replace("http", "https");
        }
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " is going to add friend: " + link);
        if (dbConnection.isLinkOnCooldown(link)) {
            System.out.println("Link to add friend [" + link + "] is on cooldown!!!! not going to happen");
            return true;
        }
        if (!checkHtml(profileHtml, false, link)) {
            return false;
        }
        //   vkAccount.setAccessToken(getAccessToken(vkAccount, restTemplate));
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", vkAccount.getCookie());
        httpHeaders.set("content-type", "application/x-www-form-urlencoded");
        String userId = null;
        Matcher userIdMatcher = Pattern.compile("data-exec=\"\\{&quot;ProfileWrapper\\/init&quot;:\\{&quot;ownerId&quot;:(.*?),").matcher(profileHtml);
        if (userIdMatcher.find()) {
            userId = userIdMatcher.group().split("=")[3].split(" ")[0].replaceAll("\"", "");
        }
        if (userId == null) {
            userIdMatcher = Pattern.compile("<a class=\"author\" href=\".*\" data-from-id=(\\\".*\\\")").matcher(profileHtml);
            if (userIdMatcher.find()) {
                userId = userIdMatcher.group().split("=")[3].split(" ")[0].replaceAll("\"", "");
            }
            if (userId == null) {
                userIdMatcher = Pattern.compile("<a href=\"/wall(.*?)\"").matcher(profileHtml);
                if (userIdMatcher.find()) {
                    userId = userIdMatcher.group(1);
                }
            }
        }
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        if (userId != null) {
            userId = userId.split("\\?")[0].replaceAll("[^\\d]", "");
        }
        System.out.println(userId);
        body.add("user_id", userId);
        body.add("access_token", vkAccount.getAccessToken());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        final ResponseEntity response = restTemplate.exchange("https://api.vk.com/method/friends.add?v=5.191&client_id=6287487",
                HttpMethod.POST, request, String.class);
        Object responseBody = response.getBody();
        if (responseBody != null && responseBody.toString().contains("Flood control")) {
            vkAccount.setCantAddFriends(true);
            DbConnection.getInstance().vkAccountCantAddFriendsAnymore(vkAccount.getId());
            return false;
        }
        boolean result = responseBody.equals("{\"response\":1}");
        System.out.println(new Date() + " vk acc " + vkAccount.getId() + " added friend " + link + " vk response " + result);
        dbConnection.logAction("addFriend", link, vkAccount.getId(), result, null);
        return true;
    }

    public String getAccessToken(final VkAccount vkAccount, final RestTemplate restTemplate) throws Exception {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        final HttpHeaders headers = new HttpHeaders();
        headers.add("accept", "*/*");
        headers.add("accept-language", "ru-RU,ru;q=0.9");
        headers.add("content-type", "application/x-www-form-urlencoded");
        headers.setOrigin("https://vk.com");
        headers.add("origin", "https://vk.com");
        headers.add("referer", "https://vk.com/");
        headers.add("sec-ch-ua", "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
        headers.add("sec-ch-ua-mobile", "?0");
        headers.add("sec-ch-ua-platform", "\"Windows\"");
        headers.add("sec-fetch-dest", "empty");
        headers.add("sec-fetch-mode", "cors");
        headers.add("sec-fetch-site", "same-site");
        headers.add("cookie", vkAccount.getCookie());
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("version", "1");
        params.add("app_id", IConstants.VK_CLIENT_ID);
        params.add("access_token", "");
        ResponseEntity<String> exchange = restTemplate.exchange("https://login.vk.com/?act=web_token", HttpMethod.POST,
                new HttpEntity<>(params, headers), String.class);
        final String strResponse = exchange.getBody();
        return strResponse.split("access_token\":\"")[1].split("\"")[0];
    }

    public boolean refreshCookie(final ChromeDriver driver, final VkAccount vkAccount, final RestTemplate restTemplate, boolean inNewWindow) throws Exception {
        if (inNewWindow) {
            driver.switchTo().newWindow(WindowType.TAB);
        }
        try {
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
            driver.get("https://vk.com/");
//            try {
//                final String blockMessage = waitUntilElementWillBePresent(driver, "div[@id='login_blocked_wrap']").getAttribute("innerText");
//                if (blockMessage.contains("To restore access, please confirm your phone number and create a new password.")
//                || blockMessage.contains("Чтобы восстановить доступ, подтвердите номер телефона и придумайте новый пароль")) {
//                    WebElement recoveryButton = waitUntilElementWillBePresent(driver, "div[@onclick='Login.showUnblockForm()']");
//                    recoveryButton.click();
//                    WebElement changePhone = waitUntilElementWillBePresent(driver, "a[@class='_change_phone_button']");
//                    changePhone.click();
//                    WebElement newPhoneField = waitUntilElementWillBePresent(driver, "div[@id='join_phone_prefixed']/div[@class='prefix_input_field']/input");
//                    FiveSimService fiveSimService = FiveSimService.getInstance();
//                    final FiveSimPurchaseResponse fiveSimPurchaseResponse = fiveSimService.purchaseCallReset();
//
//                    SmsActivate smsActivate = SmsActivate.getInstance();
//                    SmsActivateResponse activationInfo = smsActivate.purchaseVkActivation();
//                    newPhoneField.sendKeys(activationInfo.getPhoneNumber());
//                    waitUntilElementWithTextWillBePresent(driver, "span", "Позвонить").click();
//                    try {
//                        final WebElement errorMessage = waitUntilElementWillBePresent(driver, "div[@class='msg submit_error error']");
//                    } catch (Exception ignored) {
//
//                    }
//                    smsActivate.waitForSmsCode(activationInfo);
//                    Thread.sleep(100000000);
//                }
//                return false;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            try {
                final String blockMessage = waitUntilElementWillBePresent(driver, "div[@id='login_blocked_wrap']").getAttribute("innerText");
                DbConnection.getInstance().markVkAccAsBlocked(vkAccount.getId(), "1");
                System.out.println("Vk acc " + vkAccount.getId() + " is blocked for " + blockMessage);
                return false;
            } catch (TimeoutException ignored) {

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                waitUntilElementWillBePresent(driver, "input[@name='login']").sendKeys(vkAccount.getPhone());
                System.out.println("Going to login into vk acc " + vkAccount.getId());
                findElement(driver, "button[@type='submit']").click();
                Thread.sleep(5000);
                try {
                    findElement(driver, "input[@type='password']").sendKeys(vkAccount.getPassword());
                } catch (Exception e) {
                    findElement(driver, "button[@type='button']/span").click();
                    waitUntilElementWillBePresent(driver, "input[@type='password']").sendKeys(vkAccount.getPassword());
                }
                findElement(driver, "button[@type='submit']").click();
            } catch (Exception ignored) {

            }
            new WebDriverWait(driver, Duration.ofSeconds(45)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@id='top_profile_link']")));
            DbConnection.getInstance().markVkAccAsBlocked(vkAccount.getId(), "0");
            if (true) {
                return true;
            }
            Thread.sleep(3000);
            final LogEntries vkLogs = driver.manage().logs().get("performance");
            String vkCookies = null;
            for (final LogEntry entry : vkLogs) {
                final JSONObject json = new JSONObject(entry.getMessage());
                final JSONObject message = json.getJSONObject("message");
                final String method = message.getString("method");

                if ("Network.responseReceived".equals(method)) {
                    final JSONObject params = message.getJSONObject("params");

                    final JSONObject response = params.getJSONObject("response");
                    final String messageUrl = response.getString("url");

                    if (messageUrl.contains("https://login.vk.com/?act=web_token")) {
                        final String requestId = params.getString("requestId");
                        for (final LogEntry reqEntry : vkLogs) {
                            final JSONObject reqJson = new JSONObject(reqEntry.getMessage());
                            final JSONObject reqMessage = reqJson.getJSONObject("message");
                            final JSONObject reqParams = reqMessage.getJSONObject("params");
                            try {
                                final String reqId = reqParams.getString("requestId");
                                if (requestId.equals(reqId)) {
                                    final JSONObject headers = reqParams.getJSONObject("headers");
                                    try {
                                        vkCookies = (String) headers.get("cookie");
                                        break;
                                    } catch (Exception e) {
                                        vkCookies = (String) headers.get("Cookie");
                                        break;
                                    }
                                }
                            } catch (JSONException ignored) {
                            }
                        }
                        break;
                    }
                }
            }
            for (final LogEntry entry : vkLogs) {
                final JSONObject json = new JSONObject(entry.getMessage());
                final JSONObject message = json.getJSONObject("message");
                final String method = message.getString("method");

                if ("Network.responseReceived".equals(method)) {
                    final JSONObject params = message.getJSONObject("params");

                    final JSONObject response = params.getJSONObject("response");
                    final String messageUrl = response.getString("url");

                    if (messageUrl.contains("https://vk.com/feed")) {
                        final String requestId = params.getString("requestId");
                        for (final LogEntry reqEntry : vkLogs) {
                            final JSONObject reqJson = new JSONObject(reqEntry.getMessage());
                            final JSONObject reqMessage = reqJson.getJSONObject("message");
                            final JSONObject reqParams = reqMessage.getJSONObject("params");
                            try {
                                final String reqId = reqParams.getString("requestId");
                                if (requestId.equals(reqId)) {
                                    final JSONObject headers = reqParams.getJSONObject("headers");
                                    try {
                                        vkCookies += ";" + headers.get("cookie");
                                        break;
                                    } catch (Exception e) {
                                        vkCookies += ";" + headers.get("Cookie");
                                        break;
                                    }
                                }
                            } catch (JSONException ignored) {
                            }
                        }
                        break;
                    }
                }
            }
            //  System.out.println(vkCookies);
            final Map<String, String> cookiesToSave = new HashMap<>();
            for (final String cookieRow : vkCookies.split(";")) {
                final String[] cookieProperties = cookieRow.trim().split("=");
                try {
                    final String cookieName = cookieProperties[0];
                    final String cookieValue = cookieProperties[1];
                    cookiesToSave.put(cookieName, cookieValue);
                } catch (Exception e) {
                    //    System.out.println(cookieRow);
                }
            }
//            final String authTokenLocalStorageItem = (String) driver.executeScript("" +
//                    "const items = { ...localStorage };" +
//                    "        for (let key in items) {" +
//                    "            if (key.indexOf('web_token:login:auth') !=-1) {" +
//                    "                return items[key];" +
//                    "            }" +
//                    "        }");
            //   final JSONObject authTokenLocalStorageItemJson = new JSONObject(authTokenLocalStorageItem);
            //   final Integer userId = authTokenLocalStorageItemJson.getInt("user_id");
            //  final String accessToken = authTokenLocalStorageItemJson.getString("access_token");
            vkAccount.setCookie(vkCookies);
            // vkAccount.setAccessToken(accessToken);
            //   vkAccount.setUserId(String.valueOf(userId));
            DbConnection dbConnection = DbConnection.getInstance();
            dbConnection.saveVkAccCookies(cookiesToSave, vkAccount.getId());
            DbConnection.getInstance().updateVkAccCookies(vkAccount);
//            final Map<String, String> cookies = new HashMap<>();
//            for (final VkCookie cookie : dbConnection.getVkCookies(vkAccount.getId())) {
//                cookies.put(cookie.getName(), cookie.getValue());
//            }
//            final ResponseEntity<String> loginPage = restTemplate.exchange("https://vk.com/", HttpMethod.GET, new HttpEntity<>(populateVkHeaders(cookies)), String.class);
//            for (final String header : loginPage.getHeaders().get("Set-Cookie")) {
//                if (!header.contains("DELETED")) {
//                    String[] cookieParts = header.split("=");
//                    cookies.put(cookieParts[0], cookieParts[1].split(";")[0]);
//                }
//            }
//            final MultiValueMap<String, String> vkAuthConnectSavedUserRequestUrlParameters = new LinkedMultiValueMap<String, String>();
//            vkAuthConnectSavedUserRequestUrlParameters.add("version", "1");
//            vkAuthConnectSavedUserRequestUrlParameters.add("app_id", IConstants.VK_CLIENT_ID);
//            vkAuthConnectSavedUserRequestUrlParameters.add("access_token", "");
//            final String vkSavedUserResponse = restTemplate.exchange("https://login.vk.com/?act=connect_get_saved_users", HttpMethod.POST,
//                    new HttpEntity<>(vkAuthConnectSavedUserRequestUrlParameters, populateVkAuthGetSavedUsersHeaders(cookies)), String.class).getBody();
//            final String authHash = vkSavedUserResponse.split("\"auth_hash\":\"")[1].split("\"")[0];
//            final MultiValueMap<String, String> vkAuthRequestUrlParameters = new LinkedMultiValueMap<String, String>();
//            vkAuthRequestUrlParameters.add("user_id", vkAccount.getUserId());
//            vkAuthRequestUrlParameters.add("auth_hash", authHash);
//            vkAuthRequestUrlParameters.add("from", "carousel");
//            vkAuthRequestUrlParameters.add("version", "1");
//            vkAuthRequestUrlParameters.add("app_id", IConstants.VK_CLIENT_ID);
//            vkAuthRequestUrlParameters.add("access_token", "");
//            final ResponseEntity<String> vkAuthRequest = restTemplate.exchange("https://login.vk.com/?act=connect_auth_saved_users", HttpMethod.POST,
//                    new HttpEntity<>(vkAuthRequestUrlParameters, populateVkAuthHeaders(cookies)), String.class);
//            for (final String header : vkAuthRequest.getHeaders().get("Set-Cookie")) {
//                if (!header.contains("DELETED")) {
//                    String[] cookieParts = header.split("=");
//                    String cookieName = cookieParts[0];
//                    if (cookieName.equals("Array")) {
//                        continue;
//                    }
//                    cookies.put(cookieName, cookieParts[1].split(";")[0]);
//                }
//            }
//            final StringBuilder cookieBuilder = new StringBuilder();
//            appendCookie(cookieBuilder, "remixscreen_depth", cookies);
//            appendCookie(cookieBuilder, "tmr_lvid", cookies);
//            appendCookie(cookieBuilder, "tmr_lvidTS", cookies);
//            appendCookie(cookieBuilder, "remixscreen_height", cookies);
//            appendCookie(cookieBuilder, "remixscreen_width", cookies);
//            appendCookie(cookieBuilder, "remixstid", cookies);
//            appendCookie(cookieBuilder, "remixdt", cookies);
//            appendCookie(cookieBuilder, "remixlang", cookies);
//            appendCookie(cookieBuilder, "remixluas2", cookies);
//            appendCookie(cookieBuilder, "remixstlid", cookies);
//            appendCookie(cookieBuilder, "remixflash", cookies);
//            appendCookie(cookieBuilder, "sui", cookies);
//            appendCookie(cookieBuilder, "remixuas", cookies);
//            appendCookie(cookieBuilder, "s", cookies);
//            appendCookie(cookieBuilder, "sua", cookies);
//            appendCookie(cookieBuilder, "remixsts", cookies);
//            appendCookie(cookieBuilder, "p", cookies);
//            appendCookie(cookieBuilder, "remixseenads", cookies);
//            appendCookie(cookieBuilder, "remixdmgr_tmp", cookies);
//            appendCookie(cookieBuilder, "remixua", cookies);
//            appendCookie(cookieBuilder, "remixbdr", cookies);
//            appendCookie(cookieBuilder, "remixscreen_orient", cookies);
//            appendCookie(cookieBuilder, "remixscreen_dpr", cookies);
//            appendCookie(cookieBuilder, "remixgp", cookies);
//            appendCookie(cookieBuilder, "remixsuc", cookies);
//            appendCookie(cookieBuilder, "remixdmgr", cookies);
//            appendCookie(cookieBuilder, "remixrefkey", cookies);
//            appendCookie(cookieBuilder, "remixscreen_winzoom", cookies);
//            appendCookie(cookieBuilder, "remixdark_color_scheme", cookies);
//            appendCookie(cookieBuilder, "remixcolor_scheme_mode", cookies);
//            appendCookie(cookieBuilder, "remixpuad", cookies);
//            appendCookie(cookieBuilder, "remixnsid", cookies);
//            appendCookie(cookieBuilder, "remixsid", cookies);
//            appendCookie(cookieBuilder, "tmr_detect", cookies);
//            appendCookie(cookieBuilder, "tmr_reqNum", cookies);
//            vkAccount.setCookie(cookieBuilder.toString());
//            vkAccount.setAccessToken(getAccessToken(vkAccount, restTemplate));
//            dbConnection.updateVkAccCookies(vkAccount);
//            dbConnection.saveVkAccCookies(cookies, vkAccount.getId());
            System.out.println("Updated cookies for acc: " + vkAccount.getId() + " " + vkAccount.getCookie());
            if (inNewWindow) {
                driver.close();
                driver.switchTo().window(new ArrayList<>(driver.getWindowHandles()).get(0));
            }
        } catch (ResourceAccessException e) {
            if (inNewWindow) {
                driver.close();
                driver.switchTo().window(new ArrayList<>(driver.getWindowHandles()).get(0));
            }
            System.out.println("Problems with proxy going to retry to refresh cookies for acc " + vkAccount.getId());
            refreshCookie(driver, vkAccount, restTemplate, inNewWindow);
        }
        return true;
    }

    private static void appendCookie(final StringBuilder cookieBuilder, final String cookieName,
                                     final Map<String, String> cookies) {
        String cookieValue = cookies.get(cookieName);
        if (cookieValue != null) {
            cookieBuilder.append(cookieName).append("=").append(cookieValue).append(";");
        }
    }

//    public static String collectUsefulContent(final ChromeDriver driver) {
//        final StringBuilder result = new StringBuilder();
//        result.append(findElement(driver, "div[@id='page_body']").getAttribute("innerHTML"));
//        result.append(findElement(driver, "div[@id='page_header_cont']").getAttribute("innerHTML"));
//        for (WebElement script : driver.findElements(By.xpath("//body/script"))) {
//            final String scriptContent = script.getAttribute("textContent");
//            if (scriptContent.contains("addTemplates({")) {
//                result.append(scriptContent);
//                break;
//            }
//        }
////        return result.toString();
//return driver.findElement(By.xpath("//body")).getAttribute("innerHTML");
//    }
}
