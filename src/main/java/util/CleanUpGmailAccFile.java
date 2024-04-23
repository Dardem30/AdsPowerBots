package util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;

public class CleanUpGmailAccFile {
    public static void main(String[] args) throws Exception {
        String content = new String(Files.readAllBytes(new File("gmailAccsToCleanUp.json").toPath()));
        final JSONArray json = new JSONArray(content);
        for (final Object o: json) {
            final JSONObject account = (JSONObject) o;
            System.out.println(account.getString("account").replaceAll(":", "|"));
        }
    }
}
