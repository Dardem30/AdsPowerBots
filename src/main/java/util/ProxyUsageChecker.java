package util;

import da.DbConnection;

import java.io.File;
import java.nio.file.Files;

public class ProxyUsageChecker {
    public static void main(String[] args) throws Exception {
        final DbConnection dbConnection = DbConnection.getInstance();
        final String content = new String(Files.readAllBytes(new File("forge/check_proxy_usage.txt").toPath())).replaceAll("\r", "");
        for (final String proxy: content.split("\n")) {
            if (!dbConnection.isProxyUsed(proxy)) {
                System.out.println(proxy);
            }
        }

    }
}
