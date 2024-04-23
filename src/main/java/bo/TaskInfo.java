package bo;

import org.openqa.selenium.chrome.ChromeDriver;

public class TaskInfo {
    private String profileHtml;
    private String comment;
    private String type;
    private String intermediateLink;
    private Integer voteOption;
    private Long executeTime;
    private String link;
    private Integer vkAccId;
    private ChromeDriver driver;
    private VkSerfingAccount account;

    public ChromeDriver getDriver() {
        return driver;
    }

    public void setDriver(ChromeDriver driver) {
        this.driver = driver;
    }

    public VkSerfingAccount getAccount() {
        return account;
    }

    public void setAccount(VkSerfingAccount account) {
        this.account = account;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Integer getVkAccId() {
        return vkAccId;
    }

    public void setVkAccId(Integer vkAccId) {
        this.vkAccId = vkAccId;
    }

    public Long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Long executeTime) {
        this.executeTime = executeTime;
    }

    public String getProfileHtml() {
        return profileHtml;
    }

    public void setProfileHtml(String profileHtml) {
        this.profileHtml = profileHtml;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getVoteOption() {
        return voteOption;
    }

    public void setVoteOption(Integer voteOption) {
        this.voteOption = voteOption;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIntermediateLink() {
        return intermediateLink;
    }

    public void setIntermediateLink(String intermediateLink) {
        this.intermediateLink = intermediateLink;
    }
}
