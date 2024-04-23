package bo;

import org.springframework.web.client.RestTemplate;

public class VkAccount {
    private Integer id;
    private String phone;
    private String login;
    private String firstName;
    private String lastName;
    private String password;
    private String cookie;
    private String accessToken;
    private Integer vTargetAccountId;
    private Integer vLikeAccountId;
    private Integer vkSerfingAccId;
    private Integer ipWebAccId;
    private String userId;
    private String proxy;
    private String port;
    private String reserveCodes;
    private String proxyUsername;
    private String proxyPassword;
    private String browserProfile;
    private boolean cantAddFriends;
    private boolean cantSubscribe;
    private Integer availableSubscribes;
    private Integer availableSubscribesForVlike;
    private Integer availableFriendRequests;
    private Integer availableShares;
    private Integer dayOfBirth;
    private Integer monthOfBirth;
    private Integer yearOfBirth;
    private String sex;
    private boolean proxyUnauthorized;
    private boolean skipVotes;
    private PayeerAccount payeerAccount;
    private YoutubeAccount youtubeAccount;
    private RestTemplate restTemplate;
    private VkSerfingAccount vkSerfingAccount;
    private PayUpVideoAcc payUpVideoAcc;
    private int failedSubscriptionsInRow = 0;


    public VkSerfingAccount getVkSerfingAccount() {
        return vkSerfingAccount;
    }

    public void setVkSerfingAccount(VkSerfingAccount vkSerfingAccount) {
        this.vkSerfingAccount = vkSerfingAccount;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PayeerAccount getPayeerAccount() {
        return payeerAccount;
    }

    public void setPayeerAccount(PayeerAccount payeerAccount) {
        this.payeerAccount = payeerAccount;
    }

    public boolean isProxyUnauthorized() {
        return proxyUnauthorized;
    }

    public void setProxyUnauthorized(boolean proxyUnauthorized) {
        this.proxyUnauthorized = proxyUnauthorized;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public Integer getvTargetAccountId() {
        return vTargetAccountId;
    }

    public void setvTargetAccountId(Integer vTargetAccountId) {
        this.vTargetAccountId = vTargetAccountId;
    }

    public Integer getvLikeAccountId() {
        return vLikeAccountId;
    }

    public void setvLikeAccountId(Integer vLikeAccountId) {
        this.vLikeAccountId = vLikeAccountId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getIpWebAccId() {
        return ipWebAccId;
    }

    public void setIpWebAccId(Integer ipWebAccId) {
        this.ipWebAccId = ipWebAccId;
    }

    public boolean isCantAddFriends() {
        return cantAddFriends;
    }

    public void setCantAddFriends(boolean cantAddFriends) {
        this.cantAddFriends = cantAddFriends;
    }

    public boolean isCantSubscribe() {
        if (cantSubscribe) {
            System.out.println("Vk acc " + getId() + " cant subscribe for a while");
        }
        return cantSubscribe;
    }

    public void setCantSubscribe(boolean cantSubscribe) {
        this.cantSubscribe = cantSubscribe;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public Integer getVkSerfingAccId() {
        return vkSerfingAccId;
    }

    public void setVkSerfingAccId(Integer vkSerfingAccId) {
        this.vkSerfingAccId = vkSerfingAccId;
    }

    public Integer getAvailableSubscribes() {
        return availableSubscribes;
    }

    public void setAvailableSubscribes(Integer availableSubscribes) {
        this.availableSubscribes = availableSubscribes;
    }

    public Integer getAvailableFriendRequests() {
        return availableFriendRequests;
    }

    public void setAvailableFriendRequests(Integer availableFriendRequests) {
        this.availableFriendRequests = availableFriendRequests;
    }

    public Integer getAvailableShares() {
        return availableShares;
    }

    public void setAvailableShares(Integer availableShares) {
        this.availableShares = availableShares;
    }

    public boolean isLimitForFriendRequestsIsOver() {
        if (availableFriendRequests == null) {
            System.out.println("limit for friend requests is not set for acc " + id);
            return true;
        }
        return availableFriendRequests <= 0;
    }
    public boolean isLimitForSubscribtions() {
        if (availableSubscribes == null) {
            System.out.println("limit for subscribes is not set for acc " + id);
            return true;
        }
        return availableSubscribes <= 0;
    }
    public boolean isLimitForSubscribtionsForVlike() {
        if (availableSubscribesForVlike == null) {
            System.out.println("limit for subscribes for vlike is not set for acc " + id);
            return true;
        }
        return availableSubscribesForVlike <= 0;
    }

    public Integer getAvailableSubscribesForVlike() {
        return availableSubscribesForVlike;
    }

    public void setAvailableSubscribesForVlike(Integer availableSubscribesForVlike) {
        this.availableSubscribesForVlike = availableSubscribesForVlike;
    }

    public boolean isLimitForShares() {
        if (availableShares == null) {
            System.out.println("limit for shares is not set for acc " + id);
            return true;
        }
        return availableShares <= 0;
    }

    public String getBrowserProfile() {
        return browserProfile;
    }

    public void setBrowserProfile(String browserProfile) {
        this.browserProfile = browserProfile;
    }

    public int getFailedSubscriptionsInRow() {
        return failedSubscriptionsInRow;
    }

    public void setFailedSubscriptionsInRow(int failedSubscriptionsInRow) {
        this.failedSubscriptionsInRow = failedSubscriptionsInRow;
    }
    public void incrementFailedSub() {
        failedSubscriptionsInRow++;
    }

    public String getReserveCodes() {
        return reserveCodes;
    }

    public void setReserveCodes(String reserveCodes) {
        this.reserveCodes = reserveCodes;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public YoutubeAccount getYoutubeAccount() {
        return youtubeAccount;
    }

    public void setYoutubeAccount(YoutubeAccount youtubeAccount) {
        this.youtubeAccount = youtubeAccount;
    }

    public Integer getDayOfBirth() {
        return dayOfBirth;
    }

    public void setDayOfBirth(Integer dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }

    public Integer getMonthOfBirth() {
        return monthOfBirth;
    }

    public void setMonthOfBirth(Integer monthOfBirth) {
        this.monthOfBirth = monthOfBirth;
    }

    public Integer getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Integer yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public PayUpVideoAcc getPayUpVideoAcc() {
        return payUpVideoAcc;
    }

    public void setPayUpVideoAcc(PayUpVideoAcc payUpVideoAcc) {
        payUpVideoAcc.setVkAccount(this);
        this.payUpVideoAcc = payUpVideoAcc;
    }

    public boolean isSkipVotes() {
        return skipVotes;
    }

    public void setSkipVotes(boolean skipVotes) {
        this.skipVotes = skipVotes;
    }
}
