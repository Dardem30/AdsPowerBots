package util;

import bo.CleanUpEntry;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CleanUpManager {
    public static void main(String[] args) throws Exception{
        String content = new String(Files.readAllBytes(new File("gmailAccsToCleanUp.json").toPath()));
       content = content.replaceAll("\r", "");
        List<CleanUpEntry> entryList = new ArrayList<>();
        for (final String row : content.split("\n")) {
            final String[] info = row.split("\\|");
            CleanUpEntry cleanUpEntry = new CleanUpEntry();
            cleanUpEntry.setEmail(info[0]);
            cleanUpEntry.setPasswordEmail(info[1]);
            entryList.add(cleanUpEntry);
        }
        content = new String(Files.readAllBytes(new File("vkAccsToCleanUp.txt").toPath()));
        content = content.replaceAll("\r", "");
        String[] split = content.split("\n");
        for (int index = 0;index < split.length;index++) {
            final String row = split[index];
            final String[] info = row.split("\\|");
            CleanUpEntry cleanUpEntry = entryList.get(index);
            cleanUpEntry.setPhone(info[0]);
            cleanUpEntry.setVkPassword(info[1]);
        }
        for (final CleanUpEntry entry: entryList) {
            System.out.println(entry.getEmail() + "|" + entry.getPasswordEmail() + "|" + entry.getPhone() + "|" + entry.getVkPassword());
        }
    }
}
