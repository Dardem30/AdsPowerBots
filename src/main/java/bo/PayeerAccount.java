package bo;

public class PayeerAccount {
    private int id;
    private String email;
    private String accName;
    private String secretCode;
    private String password;
    private String cookies;
    private String sessionId;
    private String masterKey;
    private Integer vkAccId;
    private Float balance;
    private VkAccount vkAccount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getVkAccId() {
        return vkAccId;
    }

    public void setVkAccId(Integer vkAccId) {
        this.vkAccId = vkAccId;
    }

    public VkAccount getVkAccount() {
        return vkAccount;
    }

    public void setVkAccount(VkAccount vkAccount) {
        this.vkAccount = vkAccount;
    }

    public String getCookies() {
        return cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    public String getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }
}
