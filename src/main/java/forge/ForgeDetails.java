package forge;

class ForgeDetails {
    private String vkPhone;
    private String vkPassword;
    private String facebookUrl;
    private String email;
    private String emailPassword;
    private String proxy;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private String browserProfile;
    private String sex;

    public String getVkPhone() {
        return vkPhone;
    }

    public void setVkPhone(String vkPhone) {
        this.vkPhone = vkPhone;
    }

    public String getVkPassword() {
        return vkPassword;
    }

    public void setVkPassword(String vkPassword) {
        this.vkPassword = vkPassword;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
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

    public String getBrowserProfile() {
        return browserProfile;
    }

    public void setBrowserProfile(String browserProfile) {
        this.browserProfile = browserProfile;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "ForgeDetails{" +
                "vkPhone='" + vkPhone + '\'' +
                ", vkPassword='" + vkPassword + '\'' +
                ", facebookUrl='" + facebookUrl + '\'' +
                ", email='" + email + '\'' +
                ", emailPassword='" + emailPassword + '\'' +
                ", proxy='" + proxy + '\'' +
                ", proxyPort='" + proxyPort + '\'' +
                ", proxyUsername='" + proxyUsername + '\'' +
                ", proxyPassword='" + proxyPassword + '\'' +
                ", browserProfile='" + browserProfile + '\'' +
                '}';
    }
}
