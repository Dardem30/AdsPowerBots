package bo;

import java.util.Date;

public class VkSerfingAccount {
    private Integer id;
    private String email;
    private String cookie;
    private String password;
    private String csrfToken;
    private VkAccount vkAccount;
    private Date coolDown;
    private boolean coolDownIsSet;
    private Float balance;
    private Float balanceVkTarget;
    private String payeerAccName;
    private int missClicks;
    private int vkserfingFailsInRow = 0;

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
        vkAccount.setVkSerfingAccount(this);
        this.vkAccount = vkAccount;
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

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public boolean isCoolDownIsSet() {
        return coolDownIsSet;
    }

    public void setCoolDownIsSet(boolean coolDownIsSet) {
        this.coolDownIsSet = coolDownIsSet;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    public String getPayeerAccName() {
        return payeerAccName;
    }

    public void setPayeerAccName(String payeerAccName) {
        this.payeerAccName = payeerAccName;
    }

    public int getMissClicks() {
        return missClicks;
    }

    public void setMissClicks(int missClicks) {
        this.missClicks = missClicks;
    }

    public Float getBalanceVkTarget() {
        return balanceVkTarget;
    }

    public void setBalanceVkTarget(Float balanceVkTarget) {
        this.balanceVkTarget = balanceVkTarget;
    }

    public int getVkserfingFailsInRow() {
        return vkserfingFailsInRow;
    }

    public void setVkserfingFailsInRow(int vkserfingFailsInRow) {
        this.vkserfingFailsInRow = vkserfingFailsInRow;
    }
    public void incrementVkSerfingFailsInRow() {
        vkserfingFailsInRow++;
    }

}
