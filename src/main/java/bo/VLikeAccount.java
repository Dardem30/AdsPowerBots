package bo;

import java.util.Date;

public class VLikeAccount {
    private Integer id;
    private String email;
    private String cookie;
    private String password;
    private String payeerAccName;
    private VkAccount vkAccount;
    private YoutubeAccount youtubeAccount;
    private Date coolDown;
    private Float balance;
    private boolean coolDownIsSet;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public VkAccount getVkAccount() {
        return vkAccount;
    }

    public void setVkAccount(VkAccount vkAccount) {
        this.vkAccount = vkAccount;
    }

    public YoutubeAccount getYoutubeAccount() {
        return youtubeAccount;
    }

    public void setYoutubeAccount(YoutubeAccount youtubeAccount) {
        this.youtubeAccount = youtubeAccount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public Date getCoolDown() {
        return coolDown;
    }

    public void setCoolDown(Date coolDown) {
        this.coolDown = coolDown;
    }

    public boolean isCoolDownIsSet() {
        return coolDownIsSet;
    }

    public void setCoolDownIsSet(boolean coolDownIsSet) {
        this.coolDownIsSet = coolDownIsSet;
    }

    public String getPayeerAccName() {
        return payeerAccName;
    }

    public void setPayeerAccName(String payeerAccName) {
        this.payeerAccName = payeerAccName;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }
}
