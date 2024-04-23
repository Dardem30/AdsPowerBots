package da;

import bo.*;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DbConnection {
    private static DbConnection INSTANCE;
    private static Connection CONNECTION_INSTANCE;

    public static DbConnection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DbConnection();
        }
        return INSTANCE;
    }

    public Connection initConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        final Properties properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "root");
        properties.setProperty("useUnicode", "true");
        properties.setProperty("characterEncoding", "cp1251");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/vktarget?useUnicode=true&serverTimezone=UTC", properties);
    }

    public List<VkSerfingAccount> getAllVkSerfingAccounts() throws Exception {
        try (final Connection connection = initConnection()) {
            final List<VkSerfingAccount> result = new ArrayList<>();
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT a.id, a.email, a.password, a.cookie, va.id, va.phone, va.login, va.password, va.cookie, va.account_id, va.vlike_account_id, va.access_token, va.userid, a.cooldown, a.xcsrftoken, va.cantaddfriends, va.cantsubscribe, va.proxy, va.port, va.proxy_username, va.proxy_password, " +
                    "va.available_subscribes, va.available_friend_requests, va.available_shares, va.browser_profile, a.balance, pa.id, pa.acc_name, " +
                    "a.balance_target, ya.id, ya.gmail, ya.phone, pva.id, pva.balance, pva.blocked " +
                    "FROM vkserfing_account a " +
                    "INNER JOIN vk_account va on a.id = va.vkserfing_account_id " +
                    "INNER JOIN payeer_account pa on pa.vk_acc_id = va.id " +
                    "INNER JOIN vw_balance_overview balance_info on balance_info.vk_id = va.id " +
                    "LEFT JOIN youtube_account ya on va.id = ya.vk_acc_id " +
                    "LEFT JOIN payup_video_account pva on pva.vk_acc_id = va.id " +
                    "WHERE 1=1 " +
                    // "AND balance_info.todays_income < 16 " +
                    // "AND va.id IN (575, 609) " +
                   // "AND va.browser_profile IN ('jbwk6gv') " +
                    "AND va.browser_profile NOT IN (SELECT code FROM not_ready_browsers) " +
                    "AND (va.unblock_applied = '0' OR va.unblock_applied is null) " +
                    "AND va.proxy is not null ORDER BY RAND()");
            // "WHERE va.browser_profile NOT IN ('jbwk6gx') AND balance_info.todays_income < 16 AND va.browser_profile NOT IN (SELECT code FROM not_ready_browsers) AND va.proxy is not null ORDER BY RAND()");
            //"WHERE va.browser_profile IN ('jbwk6gx')");
            while (resultSet.next()) {
                final VkSerfingAccount vkSerfingAccount = new VkSerfingAccount();
                final int accId = resultSet.getInt(1);
                final String accEmail = resultSet.getString(2);
                final String accPassword = resultSet.getString(3);
                final String accCookie = resultSet.getString(4);
                vkSerfingAccount.setId(accId);
                vkSerfingAccount.setEmail(accEmail);
                vkSerfingAccount.setPassword(accPassword);
                vkSerfingAccount.setCookie(accCookie);
                result.add(vkSerfingAccount);
                final int vkAccId = resultSet.getInt(5);
                if (vkAccId != 0) {
                    final VkAccount vkAccount = new VkAccount();
                    final String vkAccPhone = resultSet.getString(6);
                    final String vkAccLogin = resultSet.getString(7);
                    final String vkAccPassword = resultSet.getString(8);
                    final String vkAccCookie = resultSet.getString(9);
                    final int vkAccVTargetAccId = resultSet.getInt(10);
                    final int vkAccVLikeAccId = resultSet.getInt(11);
                    final String vkAccToken = resultSet.getString(12);
                    final String vkUserId = resultSet.getString(13);
                    vkAccount.setId(vkAccId);
                    vkAccount.setPhone(vkAccPhone);
                    vkAccount.setLogin(vkAccLogin);
                    vkAccount.setPassword(vkAccPassword);
                    vkAccount.setCookie(vkAccCookie);
                    vkAccount.setvTargetAccountId(vkAccVTargetAccId);
                    vkAccount.setAccessToken(vkAccToken);
                    vkAccount.setUserId(vkUserId);
                    if (vkAccVLikeAccId != 0) {
                        vkAccount.setvLikeAccountId(vkAccVLikeAccId);
                    }
                    vkSerfingAccount.setVkAccount(vkAccount);
                    final boolean cantAddFriends = resultSet.getBoolean(16);
                    vkAccount.setCantAddFriends(cantAddFriends);
                    final boolean cantSubscribe = resultSet.getBoolean(17);
                    vkAccount.setCantSubscribe(cantSubscribe);
                    final String proxy = resultSet.getString(18);
                    final String port = resultSet.getString(19);
                    final String proxyUsername = resultSet.getString(20);
                    final String proxyPassword = resultSet.getString(21);
                    vkAccount.setProxy(proxy);
                    vkAccount.setPort(port);
                    vkAccount.setProxyUsername(proxyUsername);
                    vkAccount.setProxyPassword(proxyPassword);
                    final Integer availableSubscribes = resultSet.getInt(22);
                    final Integer availableFriendRequests = resultSet.getInt(23);
                    final Integer availableShares = resultSet.getInt(24);
                    vkAccount.setAvailableSubscribes(availableSubscribes);
                    vkAccount.setAvailableFriendRequests(availableFriendRequests);
                    vkAccount.setAvailableShares(availableShares);
                    vkAccount.setBrowserProfile(resultSet.getString(25));
                    final PayeerAccount payeerAccount = new PayeerAccount();
                    payeerAccount.setId(resultSet.getInt(27));
                    payeerAccount.setAccName(resultSet.getString(28));
                    vkAccount.setPayeerAccount(payeerAccount);
                    int payUpVideoAccountId = resultSet.getInt(33);
                    if (payUpVideoAccountId != 0) {
                        PayUpVideoAcc payUpVideoAcc = new PayUpVideoAcc();
                        payUpVideoAcc.setId(payUpVideoAccountId);
                        payUpVideoAcc.setBalance(resultSet.getFloat(34));
                        payUpVideoAcc.setBlocked(resultSet.getBoolean(35));
                        vkAccount.setPayUpVideoAcc(payUpVideoAcc);
                    }
                }
                vkSerfingAccount.setCoolDown(resultSet.getTimestamp(14));
                vkSerfingAccount.setCsrfToken(resultSet.getString(15));
                vkSerfingAccount.setBalance(resultSet.getFloat(26));
                vkSerfingAccount.setBalanceVkTarget(resultSet.getFloat(29));
                int yaId = resultSet.getInt(30);
                if (yaId != 0) {
                    final YoutubeAccount youtubeAccount = new YoutubeAccount();
                    youtubeAccount.setId(yaId);
                    youtubeAccount.setGmail(resultSet.getString(31));
                    youtubeAccount.setPhone(resultSet.getString(32));
                    youtubeAccount.setVkAccId(vkAccId);
                    vkSerfingAccount.getVkAccount().setYoutubeAccount(youtubeAccount);
                }
            }
            return result;
        }
    }

    public List<VkSerfingAccount> getAllVkSerfingAccountsToInvoice() throws Exception {
        try (final Connection connection = initConnection()) {
            final List<VkSerfingAccount> result = new ArrayList<>();
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT a.id, a.email, a.password, a.cookie, va.id, va.phone, va.login, va.password, va.cookie, va.account_id, va.vlike_account_id, va.access_token, va.userid, a.cooldown, a.xcsrftoken, va.cantaddfriends, va.cantsubscribe, va.proxy, va.port, va.proxy_username, va.proxy_password, " +
                    "va.available_subscribes, va.available_friend_requests, va.available_shares, " +
                    "a.balance, pa.acc_name " +
                    "FROM vkserfing_account a " +
                    "INNER JOIN vk_account va on a.id = va.vkserfing_account_id " +
                    "INNER JOIN payeer_account pa on pa.vk_acc_id = va.id " +
                    "WHERE va.proxy is not null AND a.requires_verification is null AND a.invoice = '1' ORDER BY RAND()");
            while (resultSet.next()) {
                final VkSerfingAccount vkSerfingAccount = new VkSerfingAccount();
                final int accId = resultSet.getInt(1);
                final String accEmail = resultSet.getString(2);
                final String accPassword = resultSet.getString(3);
                final String accCookie = resultSet.getString(4);
                vkSerfingAccount.setId(accId);
                vkSerfingAccount.setEmail(accEmail);
                vkSerfingAccount.setPassword(accPassword);
                vkSerfingAccount.setCookie(accCookie);
                result.add(vkSerfingAccount);
                final int vkAccId = resultSet.getInt(5);
                if (vkAccId != 0) {
                    final VkAccount vkAccount = new VkAccount();
                    final String vkAccPhone = resultSet.getString(6);
                    final String vkAccLogin = resultSet.getString(7);
                    final String vkAccPassword = resultSet.getString(8);
                    final String vkAccCookie = resultSet.getString(9);
                    final int vkAccVTargetAccId = resultSet.getInt(10);
                    final int vkAccVLikeAccId = resultSet.getInt(11);
                    final String vkAccToken = resultSet.getString(12);
                    final String vkUserId = resultSet.getString(13);
                    vkAccount.setId(vkAccId);
                    vkAccount.setPhone(vkAccPhone);
                    vkAccount.setLogin(vkAccLogin);
                    vkAccount.setPassword(vkAccPassword);
                    vkAccount.setCookie(vkAccCookie);
                    vkAccount.setvTargetAccountId(vkAccVTargetAccId);
                    vkAccount.setAccessToken(vkAccToken);
                    vkAccount.setUserId(vkUserId);
                    if (vkAccVLikeAccId != 0) {
                        vkAccount.setvLikeAccountId(vkAccVLikeAccId);
                    }
                    vkSerfingAccount.setVkAccount(vkAccount);
                    final boolean cantAddFriends = resultSet.getBoolean(16);
                    vkAccount.setCantAddFriends(cantAddFriends);
                    final boolean cantSubscribe = resultSet.getBoolean(17);
                    vkAccount.setCantSubscribe(cantSubscribe);
                    final String proxy = resultSet.getString(18);
                    final String port = resultSet.getString(19);
                    final String proxyUsername = resultSet.getString(20);
                    final String proxyPassword = resultSet.getString(21);
                    vkAccount.setProxy(proxy);
                    vkAccount.setPort(port);
                    vkAccount.setProxyUsername(proxyUsername);
                    vkAccount.setProxyPassword(proxyPassword);
                    final Integer availableSubscribes = resultSet.getInt(22);
                    final Integer availableFriendRequests = resultSet.getInt(23);
                    final Integer availableShares = resultSet.getInt(24);
                    vkAccount.setAvailableSubscribes(availableSubscribes);
                    vkAccount.setAvailableFriendRequests(availableFriendRequests);
                    vkAccount.setAvailableShares(availableShares);
                }
                vkSerfingAccount.setCoolDown(resultSet.getTimestamp(14));
                vkSerfingAccount.setCsrfToken(resultSet.getString(15));
                vkSerfingAccount.setBalance(resultSet.getFloat(25));
                vkSerfingAccount.setPayeerAccName(resultSet.getString(26));
            }
            return result;
        }
    }

    public List<VkSerfingAccount> getAllNotVerifyedVkSerfingAccounts() throws Exception {
        try (final Connection connection = initConnection()) {
            final List<VkSerfingAccount> result = new ArrayList<>();
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT a.id, a.email, a.password, a.cookie, va.id, va.phone, va.login, va.password, va.cookie, va.account_id, va.vlike_account_id, va.access_token, va.userid, a.cooldown, a.xcsrftoken, va.cantaddfriends, va.cantsubscribe, va.proxy, va.port, va.proxy_username, va.proxy_password, " +
                    "va.available_subscribes, va.available_friend_requests, va.available_shares FROM vkserfing_account a INNER JOIN vk_account va on a.id = va.vkserfing_account_id WHERE va.proxy is not null AND a.requires_verification = '1' ORDER BY RAND()");
            while (resultSet.next()) {
                final VkSerfingAccount vkSerfingAccount = new VkSerfingAccount();
                final int accId = resultSet.getInt(1);
                final String accEmail = resultSet.getString(2);
                final String accPassword = resultSet.getString(3);
                final String accCookie = resultSet.getString(4);
                vkSerfingAccount.setId(accId);
                vkSerfingAccount.setEmail(accEmail);
                vkSerfingAccount.setPassword(accPassword);
                vkSerfingAccount.setCookie(accCookie);
                result.add(vkSerfingAccount);
                final int vkAccId = resultSet.getInt(5);
                if (vkAccId != 0) {
                    final VkAccount vkAccount = new VkAccount();
                    final String vkAccPhone = resultSet.getString(6);
                    final String vkAccLogin = resultSet.getString(7);
                    final String vkAccPassword = resultSet.getString(8);
                    final String vkAccCookie = resultSet.getString(9);
                    final int vkAccVTargetAccId = resultSet.getInt(10);
                    final int vkAccVLikeAccId = resultSet.getInt(11);
                    final String vkAccToken = resultSet.getString(12);
                    final String vkUserId = resultSet.getString(13);
                    vkAccount.setId(vkAccId);
                    vkAccount.setPhone(vkAccPhone);
                    vkAccount.setLogin(vkAccLogin);
                    vkAccount.setPassword(vkAccPassword);
                    vkAccount.setCookie(vkAccCookie);
                    vkAccount.setvTargetAccountId(vkAccVTargetAccId);
                    vkAccount.setAccessToken(vkAccToken);
                    vkAccount.setUserId(vkUserId);
                    if (vkAccVLikeAccId != 0) {
                        vkAccount.setvLikeAccountId(vkAccVLikeAccId);
                    }
                    vkSerfingAccount.setVkAccount(vkAccount);
                    final boolean cantAddFriends = resultSet.getBoolean(16);
                    vkAccount.setCantAddFriends(cantAddFriends);
                    final boolean cantSubscribe = resultSet.getBoolean(17);
                    vkAccount.setCantSubscribe(cantSubscribe);
                    final String proxy = resultSet.getString(18);
                    final String port = resultSet.getString(19);
                    final String proxyUsername = resultSet.getString(20);
                    final String proxyPassword = resultSet.getString(21);
                    vkAccount.setProxy(proxy);
                    vkAccount.setPort(port);
                    vkAccount.setProxyUsername(proxyUsername);
                    vkAccount.setProxyPassword(proxyPassword);
                    final Integer availableSubscribes = resultSet.getInt(22);
                    final Integer availableFriendRequests = resultSet.getInt(23);
                    final Integer availableShares = resultSet.getInt(24);
                    vkAccount.setAvailableSubscribes(availableSubscribes);
                    vkAccount.setAvailableFriendRequests(availableFriendRequests);
                    vkAccount.setAvailableShares(availableShares);
                }
                vkSerfingAccount.setCoolDown(resultSet.getTimestamp(14));
                vkSerfingAccount.setCsrfToken(resultSet.getString(15));
            }
            return result;
        }
    }

    public List<IpWebAccount> getAllIpWebAccounts() throws Exception {
        try (final Connection connection = initConnection()) {
            final List<IpWebAccount> result = new ArrayList<>();
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT a.id, a.email, a.password, a.cookie, va.id, va.phone, va.login, va.password, va.cookie, va.account_id, va.vlike_account_id, va.access_token, va.userid, va.cantaddfriends, va.cantsubscribe FROM ipweb_account a INNER JOIN vk_account va on a.id = va.ipweb_account_id");
            while (resultSet.next()) {
                final IpWebAccount ipWebAccount = new IpWebAccount();
                final int accId = resultSet.getInt(1);
                final String accEmail = resultSet.getString(2);
                final String accPassword = resultSet.getString(3);
                final String accCookie = resultSet.getString(4);
                ipWebAccount.setId(accId);
                ipWebAccount.setEmail(accEmail);
                ipWebAccount.setPassword(accPassword);
                ipWebAccount.setCookie(accCookie);
                result.add(ipWebAccount);
                final int vkAccId = resultSet.getInt(5);
                if (vkAccId != 0) {
                    final VkAccount vkAccount = new VkAccount();
                    final String vkAccPhone = resultSet.getString(6);
                    final String vkAccLogin = resultSet.getString(7);
                    final String vkAccPassword = resultSet.getString(8);
                    final String vkAccCookie = resultSet.getString(9);
                    final int vkAccVTargetAccId = resultSet.getInt(10);
                    final int vkAccVLikeAccId = resultSet.getInt(11);
                    final String vkAccToken = resultSet.getString(12);
                    final String vkUserId = resultSet.getString(13);
                    vkAccount.setId(vkAccId);
                    vkAccount.setPhone(vkAccPhone);
                    vkAccount.setLogin(vkAccLogin);
                    vkAccount.setPassword(vkAccPassword);
                    vkAccount.setCookie(vkAccCookie);
                    vkAccount.setvTargetAccountId(vkAccVTargetAccId);
                    vkAccount.setAccessToken(vkAccToken);
                    vkAccount.setUserId(vkUserId);
                    if (vkAccVLikeAccId != 0) {
                        vkAccount.setvLikeAccountId(vkAccVLikeAccId);
                    }
                    ipWebAccount.setVkAccount(vkAccount);
                    final boolean cantAddFriends = resultSet.getBoolean(14);
                    vkAccount.setCantAddFriends(cantAddFriends);
                    final boolean cantSubscribe = resultSet.getBoolean(15);
                    vkAccount.setCantSubscribe(cantSubscribe);
                }
            }
            return result;
        }
    }

    public List<VkAccount> getAllVkAccs() throws Exception {
        try (final Connection connection = initConnection()) {
            final List<VkAccount> result = new ArrayList<>();
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT id, phone, login, password, cookie, account_id, vlike_account_id, access_token, userid, va.proxy, va.port, va.proxy_username, va.proxy_password, va.vkserfing_account_id, va.browser_profile FROM vk_account va");
            while (resultSet.next()) {
                final VkAccount vkAccount = new VkAccount();
                final int vkAccId = resultSet.getInt(1);
                final String vkAccPhone = resultSet.getString(2);
                final String vkAccLogin = resultSet.getString(3);
                final String vkAccPassword = resultSet.getString(4);
                final String vkAccCookie = resultSet.getString(5);
                final int vkAccVTargetAccId = resultSet.getInt(6);
                final int vkAccVLikeAccId = resultSet.getInt(7);
                final String vkAccToken = resultSet.getString(8);
                final String vkUserId = resultSet.getString(9);
                vkAccount.setId(vkAccId);
                vkAccount.setPhone(vkAccPhone);
                vkAccount.setLogin(vkAccLogin);
                vkAccount.setPassword(vkAccPassword);
                vkAccount.setCookie(vkAccCookie);
                vkAccount.setvTargetAccountId(vkAccVTargetAccId);
                vkAccount.setAccessToken(vkAccToken);
                vkAccount.setUserId(vkUserId);
                if (vkAccVLikeAccId != 0) {
                    vkAccount.setvLikeAccountId(vkAccVLikeAccId);
                }
                final String proxy = resultSet.getString(10);
                final String port = resultSet.getString(11);
                final String proxyUsername = resultSet.getString(12);
                final String proxyPassword = resultSet.getString(13);
                vkAccount.setProxy(proxy);
                vkAccount.setPort(port);
                vkAccount.setProxyUsername(proxyUsername);
                vkAccount.setProxyPassword(proxyPassword);
                vkAccount.setVkSerfingAccId(resultSet.getInt(14));
                vkAccount.setBrowserProfile(resultSet.getString(15));
                result.add(vkAccount);
            }
            return result;
        }
    }

    public VkAccount getVkAccById(final int vkId) throws Exception {
        try (final Connection connection = initConnection()) {
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT id, phone, login, password, cookie, account_id, vlike_account_id, access_token, userid,proxy,port,proxy_username, proxy_password, browser_profile FROM vk_account va WHERE id = " + vkId);
            resultSet.next();
            final VkAccount vkAccount = new VkAccount();
            final int vkAccId = resultSet.getInt(1);
            final String vkAccPhone = resultSet.getString(2);
            final String vkAccLogin = resultSet.getString(3);
            final String vkAccPassword = resultSet.getString(4);
            final String vkAccCookie = resultSet.getString(5);
            final int vkAccVTargetAccId = resultSet.getInt(6);
            final int vkAccVLikeAccId = resultSet.getInt(7);
            final String vkAccToken = resultSet.getString(8);
            final String vkUserId = resultSet.getString(9);
            vkAccount.setId(vkAccId);
            vkAccount.setPhone(vkAccPhone);
            vkAccount.setLogin(vkAccLogin);
            vkAccount.setPassword(vkAccPassword);
            vkAccount.setCookie(vkAccCookie);
            vkAccount.setvTargetAccountId(vkAccVTargetAccId);
            vkAccount.setAccessToken(vkAccToken);
            vkAccount.setUserId(vkUserId);
            if (vkAccVLikeAccId != 0) {
                vkAccount.setvLikeAccountId(vkAccVLikeAccId);
            }
            final String proxy = resultSet.getString(10);
            final String port = resultSet.getString(11);
            final String proxyUsername = resultSet.getString(12);
            final String proxyPassword = resultSet.getString(13);
            vkAccount.setProxy(proxy);
            vkAccount.setPort(port);
            vkAccount.setProxyUsername(proxyUsername);
            vkAccount.setProxyPassword(proxyPassword);
            vkAccount.setBrowserProfile(resultSet.getString(14));
            return vkAccount;
        }
    }

    public VkAccount getVkAccByPhone(final String phone) throws Exception {
        try (final Connection connection = initConnection()) {
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT id, phone, login, password, cookie, account_id, vlike_account_id, access_token, userid,proxy,port,proxy_username, proxy_password FROM vk_account va WHERE phone = '" + phone + "'");
            if (!resultSet.next()) {
                return null;
            }
            final VkAccount vkAccount = new VkAccount();
            final int vkAccId = resultSet.getInt(1);
            final String vkAccPhone = resultSet.getString(2);
            final String vkAccLogin = resultSet.getString(3);
            final String vkAccPassword = resultSet.getString(4);
            final String vkAccCookie = resultSet.getString(5);
            final int vkAccVTargetAccId = resultSet.getInt(6);
            final int vkAccVLikeAccId = resultSet.getInt(7);
            final String vkAccToken = resultSet.getString(8);
            final String vkUserId = resultSet.getString(9);
            vkAccount.setId(vkAccId);
            vkAccount.setPhone(vkAccPhone);
            vkAccount.setLogin(vkAccLogin);
            vkAccount.setPassword(vkAccPassword);
            vkAccount.setCookie(vkAccCookie);
            vkAccount.setvTargetAccountId(vkAccVTargetAccId);
            vkAccount.setAccessToken(vkAccToken);
            vkAccount.setUserId(vkUserId);
            if (vkAccVLikeAccId != 0) {
                vkAccount.setvLikeAccountId(vkAccVLikeAccId);
            }
            final String proxy = resultSet.getString(10);
            final String port = resultSet.getString(11);
            final String proxyUsername = resultSet.getString(12);
            final String proxyPassword = resultSet.getString(13);
            vkAccount.setProxy(proxy);
            vkAccount.setPort(port);
            vkAccount.setProxyUsername(proxyUsername);
            vkAccount.setProxyPassword(proxyPassword);
            return vkAccount;
        }
    }

    public Integer saveVTargetAcc(final VTargetAccount vTargetAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            if (vTargetAccount.getId() == null) {
                final PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO account(email, password, cookie) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                saveStatement.setString(1, vTargetAccount.getEmail());
                saveStatement.setString(2, vTargetAccount.getPassword());
                if (vTargetAccount.getCookie() == null) {
                    saveStatement.setNull(3, Types.VARCHAR);
                } else {
                    saveStatement.setString(3, vTargetAccount.getCookie());
                }
                saveStatement.execute();
                ResultSet generatedKeys = saveStatement.getGeneratedKeys();
                generatedKeys.next();
                return generatedKeys.getInt(1);
            } else {
                final PreparedStatement updateStatement = connection.prepareStatement("UPDATE account SET email=?, password=?, cookie=? WHERE id=?");
                updateStatement.setString(1, vTargetAccount.getEmail());
                updateStatement.setString(2, vTargetAccount.getPassword());
                if (vTargetAccount.getCookie() == null) {
                    updateStatement.setNull(3, Types.VARCHAR);
                } else {
                    updateStatement.setString(3, vTargetAccount.getCookie());
                }
                updateStatement.setInt(4, vTargetAccount.getId());
                updateStatement.execute();
                return vTargetAccount.getId();
            }
        }
    }

    public Integer saveVkSerfingAccount(final VkSerfingAccount vkSerfingAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO vkserfing_account(email, password) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            if (vkSerfingAccount.getEmail() == null) {
                saveStatement.setNull(1, Types.VARCHAR);
            } else {
                saveStatement.setString(1, vkSerfingAccount.getEmail());
            }
            if (vkSerfingAccount.getPassword() == null) {
                saveStatement.setNull(2, Types.VARCHAR);
            } else {
                saveStatement.setString(2, vkSerfingAccount.getPassword());
            }
            saveStatement.execute();
            ResultSet generatedKeys = saveStatement.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt(1);
        }
    }

    public Integer saveVLikeAcc(final VLikeAccount vLikeAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            if (vLikeAccount.getId() == null) {
                final PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO vlike_account(email, password, cookie) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                saveStatement.setString(1, vLikeAccount.getEmail());
                saveStatement.setString(2, vLikeAccount.getPassword());
                if (vLikeAccount.getCookie() == null) {
                    saveStatement.setNull(3, Types.VARCHAR);
                } else {
                    saveStatement.setString(3, vLikeAccount.getCookie());
                }
                saveStatement.execute();
                ResultSet generatedKeys = saveStatement.getGeneratedKeys();
                generatedKeys.next();
                return generatedKeys.getInt(1);
            } else {
                final PreparedStatement updateStatement = connection.prepareStatement("UPDATE vlike_account SET email=?, password=?, cookie=? WHERE id=?");
                updateStatement.setString(1, vLikeAccount.getEmail());
                updateStatement.setString(2, vLikeAccount.getPassword());
                updateStatement.setString(3, vLikeAccount.getCookie());
                updateStatement.setInt(4, vLikeAccount.getId());
                updateStatement.execute();
                return vLikeAccount.getId();
            }
        }
    }

    public Integer saveIpWebAccount(final IpWebAccount ipWebAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            if (ipWebAccount.getId() == null) {
                final PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO ipweb_account(email, password, cookie, login) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                saveStatement.setString(1, ipWebAccount.getEmail());
                saveStatement.setString(2, ipWebAccount.getPassword());
                if (ipWebAccount.getCookie() == null) {
                    saveStatement.setNull(3, Types.VARCHAR);
                } else {
                    saveStatement.setString(3, ipWebAccount.getCookie());
                }
                saveStatement.setString(4, ipWebAccount.getLogin());
                saveStatement.execute();
                ResultSet generatedKeys = saveStatement.getGeneratedKeys();
                generatedKeys.next();
                return generatedKeys.getInt(1);
            } else {
                final PreparedStatement updateStatement = connection.prepareStatement("UPDATE ipweb_account SET email=?, password=?, cookie=?, login=? WHERE id=?");
                updateStatement.setString(1, ipWebAccount.getEmail());
                updateStatement.setString(2, ipWebAccount.getPassword());
                updateStatement.setString(3, ipWebAccount.getCookie());
                updateStatement.setString(4, ipWebAccount.getLogin());
                updateStatement.setInt(5, ipWebAccount.getId());
                updateStatement.execute();
                return ipWebAccount.getId();
            }
        }
    }

    public int saveVKAcc(final VkAccount vkAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            if (vkAccount.getId() == null) {
                final PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO vk_account(phone, login, password, cookie, account_id, vlike_account_id, access_token, userid, ipweb_account_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                saveStatement.setString(1, vkAccount.getPhone());
                saveStatement.setString(2, vkAccount.getLogin());
                saveStatement.setString(3, vkAccount.getPassword());
                saveStatement.setString(4, vkAccount.getCookie());
                saveStatement.setInt(5, vkAccount.getvTargetAccountId());
                saveStatement.setInt(6, vkAccount.getvLikeAccountId());
                saveStatement.setString(7, vkAccount.getAccessToken());
                saveStatement.setString(8, vkAccount.getUserId());
                if (vkAccount.getIpWebAccId() == null) {
                    saveStatement.setNull(9, Types.INTEGER);
                } else {
                    saveStatement.setInt(9, vkAccount.getIpWebAccId());
                }
                saveStatement.execute();
                ResultSet generatedKeys = saveStatement.getGeneratedKeys();
                generatedKeys.next();
                return generatedKeys.getInt(1);
            } else {
                final PreparedStatement updateStatement = connection.prepareStatement("UPDATE vk_account SET account_id=?, vlike_account_id=? WHERE id=?");
                updateStatement.setInt(1, vkAccount.getvTargetAccountId());
                if (vkAccount.getvLikeAccountId() == null) {
                    updateStatement.setNull(2, Types.INTEGER);

                } else {
                    updateStatement.setInt(2, vkAccount.getvLikeAccountId());
                }
                updateStatement.setInt(3, vkAccount.getId());
                updateStatement.execute();
                return vkAccount.getId();
            }
        }
    }

    public int updateVkAccCookies(final VkAccount vkAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE vk_account SET cookie=? WHERE id=?");
            updateStatement.setString(1, vkAccount.getCookie());
            updateStatement.setInt(2, vkAccount.getId());
            updateStatement.execute();
            return vkAccount.getId();
        }
    }

    public Integer saveVKAccSerfing(final VkAccount vkAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            if (vkAccount.getId() == null) {
                final PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO vk_account(phone, login, password, cookie, vkserfing_account_id, access_token, userid, proxy, proxy_username, proxy_password, port," +
                        " available_subscribes, available_friend_requests, available_shares, browser_profile, reserve_codes) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,0,0,0, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                saveStatement.setString(1, vkAccount.getPhone());
                saveStatement.setString(2, vkAccount.getLogin());
                saveStatement.setString(3, vkAccount.getPassword());
                saveStatement.setString(4, vkAccount.getCookie());
                saveStatement.setInt(5, vkAccount.getVkSerfingAccId());
                saveStatement.setString(6, vkAccount.getAccessToken());
                saveStatement.setString(7, vkAccount.getUserId());
                saveStatement.setString(8, vkAccount.getProxy());
                saveStatement.setString(9, vkAccount.getProxyUsername());
                saveStatement.setString(10, vkAccount.getProxyPassword());
                saveStatement.setString(11, vkAccount.getPort());
                saveStatement.setString(12, vkAccount.getBrowserProfile());
                if (vkAccount.getReserveCodes() == null) {
                    saveStatement.setNull(13, Types.VARCHAR);
                } else {
                    saveStatement.setString(13, vkAccount.getReserveCodes());
                }
                saveStatement.execute();
                ResultSet generatedKeys = saveStatement.getGeneratedKeys();
                generatedKeys.next();
                int id = generatedKeys.getInt(1);
                vkAccount.setId(id);
                return id;
            }
        }
        return null;
    }


    public void saveVkAccCookies(Map<String, String> cookiesToSave, int vkAccId) throws Exception {
        try (final Connection connection = initConnection()) {
            connection.createStatement().execute("DELETE FROM vk_account_cookies WHERE vk_account_id=" + vkAccId);
            for (final Map.Entry<String, String> entry : cookiesToSave.entrySet()) {
                final PreparedStatement statement = connection.prepareStatement("INSERT INTO vk_account_cookies(name, value, vk_account_id) VALUES (?, ?, ?)");
                statement.setString(1, entry.getKey());
                statement.setString(2, entry.getValue());
                statement.setInt(3, vkAccId);
                statement.execute();
            }
        }
    }

    public List<VkCookie> getVkCookies(Integer vkId) throws Exception {
        try (final Connection connection = initConnection()) {
            final List<VkCookie> result = new ArrayList<>();
            PreparedStatement statement = connection.prepareStatement("SELECT id, name, value FROM vk_account_cookies WHERE vk_account_id=?");
            statement.setInt(1, vkId);
            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                final VkCookie vkCookie = new VkCookie();
                final int cookieId = resultSet.getInt(1);
                final String cookieName = resultSet.getString(2);
                final String cookieValue = resultSet.getString(3);
                vkCookie.setId(cookieId);
                vkCookie.setName(cookieName);
                vkCookie.setValue(cookieValue);
                result.add(vkCookie);
            }
            return result;
        }
    }

    public void updateVlikeBalance(final Integer accId, final float balance) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE vkserfing_account SET balance_vlike=? WHERE id=?");
            updateBalanceStatement.setFloat(1, balance);
            updateBalanceStatement.setInt(2, accId);
            updateBalanceStatement.execute();
        }
    }

    public void updateVKTargetBalance(final Integer accId, final float balance) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE vkserfing_account SET balance_target=? WHERE id=?");
            updateBalanceStatement.setFloat(1, balance);
            updateBalanceStatement.setInt(2, accId);
            updateBalanceStatement.execute();
        }
    }

    public void updateVKSerfingBalance(final Integer vkSerfingAccId, final float balance) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE vkserfing_account SET balance=? WHERE id=?");
            updateBalanceStatement.setFloat(1, balance);
            updateBalanceStatement.setInt(2, vkSerfingAccId);
            updateBalanceStatement.execute();
        }
    }

    public void updatePayUpVideoBalance(final Integer payUpVideoAccId, final float balance) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE payup_video_account SET balance=? WHERE id=?");
            updateBalanceStatement.setFloat(1, balance);
            updateBalanceStatement.setInt(2, payUpVideoAccId);
            updateBalanceStatement.execute();
        }
    }

    public void markPayUpVideoAccAsBlocked(final Integer payUpVideoAccId) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE payup_video_account SET blocked='1' WHERE id=?");
            updateBalanceStatement.setInt(1, payUpVideoAccId);
            updateBalanceStatement.execute();
        }
    }
    public void logCaptcha(final Integer payUpVideoAccId) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE payup_video_account SET captchas=IF(captchas is null, 1, captchas + 1) WHERE id=?");
            updateBalanceStatement.setInt(1, payUpVideoAccId);
            updateBalanceStatement.execute();
        }
    }

    public void blackListLink(final String link) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement blackListStatement = connection.prepareStatement("INSERT INTO blacklist(link) VALUES (?)");
            blackListStatement.setString(1, link);
            blackListStatement.execute();
        }
    }

    public boolean isLinkBlackListed(final String link) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement blackListStatement = connection.prepareStatement("SELECT 1 FROM blacklist WHERE link=? LIMIT 1");
            blackListStatement.setString(1, link);
            return blackListStatement.executeQuery().next();
        } catch (Exception e) {
            System.out.println("Failed to check link: " + link);
            return true;
        }
    }

    public void updateIpWebBalance(final Integer ipWebAccId, final float balance) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE ipweb_account SET balance=? WHERE id=?");
            updateBalanceStatement.setFloat(1, balance);
            updateBalanceStatement.setInt(2, ipWebAccId);
            updateBalanceStatement.execute();
        }
    }

    public void setVkSerfingCooldown(Date coolDown, Integer vkSerfingAccId) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE vkserfing_account SET cooldown=?, lastprocessed=NOW() WHERE id=?");
            updateBalanceStatement.setTimestamp(1, new Timestamp(coolDown.getTime()));
            updateBalanceStatement.setInt(2, vkSerfingAccId);
            updateBalanceStatement.execute();
        }
    }

    public void setVTargetCooldown(Date coolDown, Integer vTargetAccId) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE account SET cooldown=? WHERE id=?");
            updateBalanceStatement.setTimestamp(1, new Timestamp(coolDown.getTime()));
            updateBalanceStatement.setInt(2, vTargetAccId);
            updateBalanceStatement.execute();
        }
    }

    public void setVLikeCooldown(Date coolDown, Integer vLikeAccId) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE vlike_account SET cooldown=? WHERE id=?");
            updateBalanceStatement.setTimestamp(1, new Timestamp(coolDown.getTime()));
            updateBalanceStatement.setInt(2, vLikeAccId);
            updateBalanceStatement.execute();
        }
    }

    public void vkAccountCantAddFriendsAnymore(final Integer vkAccId) throws Exception {
        System.out.println("Vk acc " + vkAccId + " can't add friends for a while...");
        try (Connection connection = initConnection()) {
            connection.createStatement().executeUpdate("UPDATE vk_account SET cantaddfriends = '1', cantaddfriendssincewhen=NOW() WHERE id=" + vkAccId);
        }
    }

    public void vkAccountCantSubscribeAnymore(final Integer vkAccId) throws Exception {
        System.out.println("Vk acc " + vkAccId + " can't add friends for a while...");
        try (Connection connection = initConnection()) {
            connection.createStatement().executeUpdate("UPDATE vk_account SET cantsubscribe = '1', cantsibscribesincewhen=NOW() WHERE id=" + vkAccId);
        }
    }

    public void saveVLikeCookies(Integer vLikeAccId, String vLikeCookies) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE vlike_account SET cookie=? WHERE id=?");
            if (vLikeCookies == null) {
                updateStatement.setNull(1, Types.VARCHAR);
            } else {
                updateStatement.setString(1, vLikeCookies);
            }
            updateStatement.setInt(2, vLikeAccId);
            updateStatement.execute();
        }
    }

    public void saveVkTargetErrorMessage(final Integer vkTargetId, final String error) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE account SET errors=? WHERE id=?");
            updateStatement.setString(1, error);
            updateStatement.setInt(2, vkTargetId);
            updateStatement.execute();
        }
    }


//    public boolean containsRestrictedKeyword(String profileHtml) throws Exception {
//        final List<String> restrictedKeyWords = new ArrayList<>();
//        try (final Connection connection = initConnection()) {
//            final PreparedStatement statement = connection.prepareStatement("SELECT keyword FROM restricted_keywords");
//            final ResultSet resultSet = statement.executeQuery();
//            while (resultSet.next()) {
//                final String keyword = resultSet.getString(1);
//                restrictedKeyWords.add(keyword);
//            }
//        }
//        for (final String keyword : restrictedKeyWords) {
//            if (StringUtils.containsIgnoreCase(profileHtml, keyword)) {
//                System.out.println("Restriced keyword: " + keyword);
//                return true;
//            }
//        }
//        return false;
//    }

    public boolean containsRestrictedKeyword(String profileHtml) throws Exception {
        final List<String> restrictedKeyWords = new ArrayList<>();
        try (final Connection connection = initConnection()) {
            final PreparedStatement statement = connection.prepareStatement("SELECT keyword FROM restricted_keywords");
            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                final String keyword = resultSet.getString(1);
                restrictedKeyWords.add(keyword);
            }
        }
        String regex = restrictedKeyWords.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|", "(?iu)", ""));

        Pattern r = Pattern.compile(regex);

        Matcher m = r.matcher(profileHtml);
        if (m.find()) {
            System.out.println("Restriced keyword: " + m.group(0));
            return true;
        }
        return false;
    }

    public void updateVkLimitsForShares(final VkAccount vkAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE vk_account SET available_shares=? WHERE id=?");
            updateStatement.setInt(1, vkAccount.getAvailableShares());
            updateStatement.setInt(2, vkAccount.getId());
            updateStatement.execute();
        }
    }

    public void updateVkLimitsForSubscribes(final VkAccount vkAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE vk_account SET available_subscribes=? WHERE id=?");
            updateStatement.setInt(1, vkAccount.getAvailableSubscribes());
            updateStatement.setInt(2, vkAccount.getId());
            updateStatement.execute();
        }
    }

    public void updateVkLimitsForFriendRequests(final VkAccount vkAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE vk_account SET available_friend_requests=? WHERE id=?");
            updateStatement.setInt(1, vkAccount.getAvailableFriendRequests());
            updateStatement.setInt(2, vkAccount.getId());
            updateStatement.execute();
        }
    }

    public String updateAccountCookies(String currentCookies, final List<String> setCookies, final String accountTable, final int accId) throws Exception {
        // System.out.println("Going to update acc cookies for " + accountTable + ", acc id " + accId + ", cookie " + currentCookies);
        for (final String setCookie : setCookies) {
            final String[] cookieParams = setCookie.split("=");
            currentCookies = currentCookies.replaceAll(cookieParams[0] + "=(.*?);", cookieParams[0] + "=" + cookieParams[1].split(";")[0] + ";");
        }
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE " + accountTable + " SET cookie=? WHERE id=?");
            updateStatement.setString(1, currentCookies);
            updateStatement.setInt(2, accId);
            updateStatement.execute();
        }
        //   System.out.println("Updated cookies " + currentCookies);
        return currentCookies;
    }

    public void savePayeerAccount(PayeerAccount payeerAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            if (payeerAccount.getId() == 0) {
                final PreparedStatement updateBalanceStatement = connection.prepareStatement("INSERT INTO payeer_account(email, password, secret_code, acc_name, vk_acc_id, cookie, session_id,master_key) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                updateBalanceStatement.setString(1, payeerAccount.getEmail());
                updateBalanceStatement.setString(2, payeerAccount.getPassword());
                updateBalanceStatement.setString(3, payeerAccount.getSecretCode());
                updateBalanceStatement.setString(4, payeerAccount.getAccName());
                updateBalanceStatement.setInt(5, payeerAccount.getVkAccId());
                updateBalanceStatement.setString(6, payeerAccount.getCookies());
                updateBalanceStatement.setString(7, payeerAccount.getSessionId());
                updateBalanceStatement.setString(8, payeerAccount.getMasterKey());
                updateBalanceStatement.execute();
            } else {
                final PreparedStatement updateBalanceStatement = connection.prepareStatement("UPDATE payeer_account SET password=?, master_key=? WHERE id=?");
                updateBalanceStatement.setString(1, payeerAccount.getPassword());
                updateBalanceStatement.setString(2, payeerAccount.getMasterKey());
                updateBalanceStatement.setInt(3, payeerAccount.getId());
                updateBalanceStatement.executeUpdate();
            }
        }
    }

    public void updatePayeerAccount(PayeerAccount payeerAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateBalanceStatement = connection.prepareStatement("Update payeer_account SET acc_name=?, secret_code=? " +
                    " WHERE id=?");
            updateBalanceStatement.setString(1, payeerAccount.getAccName());
            updateBalanceStatement.setString(2, payeerAccount.getSecretCode());
            updateBalanceStatement.setInt(3, payeerAccount.getId());
            updateBalanceStatement.execute();
        }
    }

    public void saveCredentialsForVkSerfing(Integer vkSerfingAccId, String cookies, String vkSerfingXsrfToken) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE vkserfing_account SET cookie=?, xcsrftoken=? WHERE id=?");
            updateStatement.setString(1, cookies);
            updateStatement.setString(2, vkSerfingXsrfToken);
            updateStatement.setInt(3, vkSerfingAccId);
            updateStatement.execute();
        }
    }

    public void vkSerfingAccRequiresVerification(Integer vkSerfingAccId) throws Exception {
        System.out.println("Acc " + vkSerfingAccId + " requires verification");
        try (final Connection connection = initConnection()) {
            final PreparedStatement updateStatement = connection.prepareStatement("UPDATE vkserfing_account SET requires_verification='1' WHERE id=?");
            updateStatement.setInt(1, vkSerfingAccId);
            updateStatement.execute();
        }
    }

    public List<PayeerAccount> getPayeerAccsToInvoice() throws Exception {
        try (final Connection connection = initConnection()) {
            final List<PayeerAccount> result = new ArrayList<>();
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT pa.cookie, pa.session_id, pa.balance, " +
                    "va.proxy, va.port, va.proxy_username, va.proxy_password, va.browser_profile, pa.acc_name, pa.password, pa.id, pa.master_key " +
                    "FROM payeer_account pa INNER JOIN vk_account va on pa.vk_acc_id = va.id WHERE pa.invoice = '1'");
            while (resultSet.next()) {
                final PayeerAccount account = new PayeerAccount();
                account.setCookies(resultSet.getString(1));
                account.setSessionId(resultSet.getString(2));
                account.setBalance(resultSet.getFloat(3));
                final VkAccount vkAccount = new VkAccount();
                final String proxy = resultSet.getString(4);
                final String port = resultSet.getString(5);
                final String proxyUsername = resultSet.getString(6);
                final String proxyPassword = resultSet.getString(7);
                vkAccount.setProxy(proxy);
                vkAccount.setPort(port);
                vkAccount.setProxyUsername(proxyUsername);
                vkAccount.setProxyPassword(proxyPassword);
                vkAccount.setBrowserProfile(resultSet.getString(8));
                account.setVkAccount(vkAccount);
                account.setAccName(resultSet.getString(9));
                account.setPassword(resultSet.getString(10));
                account.setId(resultSet.getInt(11));
                account.setMasterKey(resultSet.getString(12));
                result.add(account);
            }
            return result;
        }
    }

    public List<PayeerAccount> getAllPayeerAccs() throws Exception {
        try (final Connection connection = initConnection()) {
            final List<PayeerAccount> result = new ArrayList<>();
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT pa.cookie, pa.session_id, pa.balance, " +
                    "va.proxy, va.port, va.proxy_username, va.proxy_password, va.browser_profile, pa.acc_name, pa.password, pa.id, pa.master_key, pa.secret_code " +
                    "FROM payeer_account pa INNER JOIN vk_account va on pa.vk_acc_id = va.id WHERE va.id >= 491");
            while (resultSet.next()) {
                final PayeerAccount account = new PayeerAccount();
                account.setCookies(resultSet.getString(1));
                account.setSessionId(resultSet.getString(2));
                account.setBalance(resultSet.getFloat(3));
                final VkAccount vkAccount = new VkAccount();
                final String proxy = resultSet.getString(4);
                final String port = resultSet.getString(5);
                final String proxyUsername = resultSet.getString(6);
                final String proxyPassword = resultSet.getString(7);
                vkAccount.setProxy(proxy);
                vkAccount.setPort(port);
                vkAccount.setProxyUsername(proxyUsername);
                vkAccount.setProxyPassword(proxyPassword);
                vkAccount.setBrowserProfile(resultSet.getString(8));
                account.setVkAccount(vkAccount);
                account.setAccName(resultSet.getString(9));
                account.setPassword(resultSet.getString(10));
                account.setId(resultSet.getInt(11));
                account.setMasterKey(resultSet.getString(12));
                account.setSecretCode(resultSet.getString(13));
                result.add(account);
            }
            return result;
        }
    }

    public List<PayeerAccount> getLostPayeers() throws Exception {
        try (final Connection connection = initConnection()) {
            final List<PayeerAccount> result = new ArrayList<>();
            final ResultSet resultSet = connection.createStatement().executeQuery("SELECT pa.cookie, pa.session_id, pa.balance, va.proxy, va.port, va.proxy_username, va.proxy_password," +
                    " pa.acc_name, pa.password, pa.id, pa.email FROM payeer_account pa INNER JOIN vk_account va on pa.vk_acc_id = va.id WHERE disabled = '1'");
            while (resultSet.next()) {
                final PayeerAccount account = new PayeerAccount();
                account.setCookies(resultSet.getString(1));
                account.setSessionId(resultSet.getString(2));
                account.setBalance(resultSet.getFloat(3));
                final VkAccount vkAccount = new VkAccount();
                final String proxy = resultSet.getString(4);
                final String port = resultSet.getString(5);
                final String proxyUsername = resultSet.getString(6);
                final String proxyPassword = resultSet.getString(7);
                vkAccount.setProxy(proxy);
                vkAccount.setPort(port);
                vkAccount.setProxyUsername(proxyUsername);
                vkAccount.setProxyPassword(proxyPassword);
                account.setVkAccount(vkAccount);
                result.add(account);
                account.setAccName(resultSet.getString(8));
                account.setPassword(resultSet.getString(9));
                account.setId(resultSet.getInt(10));
                account.setEmail(resultSet.getString(11));
            }
            return result;
        }
    }

    public void saveSessionForPayeer(final String cookie, final String sessionId, final Integer accId) throws Exception {
        try (final Connection connection = initConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payeer_account SET cookie=?, session_id=?, disabled = null WHERE id=?");
            preparedStatement.setString(1, cookie);
            preparedStatement.setString(2, sessionId);
            preparedStatement.setInt(3, accId);
            preparedStatement.executeUpdate();
        }

    }

    public void updatePayeerBalance(int id, float balance) throws Exception {
        try (final Connection connection = initConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payeer_account SET balance=ifnull(balance, 0) + ? WHERE id=?");
            preparedStatement.setFloat(1, balance);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        }
    }

    public void markPayeerAsInvoiced(int id, int balance) throws Exception {
        try (final Connection connection = initConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payeer_account SET balance=?, invoice = '0' WHERE id=?");
            preparedStatement.setInt(1, balance);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        }
    }

    public void vkSerfingAccIsVerified(Integer vkSerfingAccId) throws Exception {
        try (final Connection connection = initConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE vkserfing_account SET requires_verification = null WHERE id=?");
            preparedStatement.setInt(1, vkSerfingAccId);
            preparedStatement.executeUpdate();
        }
    }

    public boolean isProxyUsed(String proxy) throws Exception {
        try (final Connection connection = initConnection()) {
            return connection.createStatement().executeQuery("SELECT vk_id FROM vw_balance_overview WHERE proxy='" + proxy + "'").next();
        }
    }

    public boolean isProfileUsed(String userId) throws Exception {
        try (final Connection connection = initConnection()) {
            return connection.createStatement().executeQuery("SELECT vk.id FROM vk_account vk\n" +
                    "INNER JOIN vkserfing_account a on vk.vkserfing_account_id = a.id\n" +
                    "WHERE (vk.blocked = '0' OR vk.blocked is null) AND a.requires_verification is null AND vk.browser_profile='" + userId + "'").next();
        }
    }

    public void markVkAccAsBlocked(final Integer vkAccId, final String blocked) throws Exception {
        try (final Connection connection = initConnection()) {
            if (blocked.equals("0")) {
                connection.createStatement().executeUpdate("UPDATE vk_account SET unblock_applied='0', blocked = '" + blocked + "' WHERE id=" + vkAccId);
            } else {
                connection.createStatement().executeUpdate("UPDATE vk_account SET blocked = '" + blocked + "' WHERE id=" + vkAccId);
            }
        }
    }

    public void logAction(final String action, final String link, final Integer vkAccId, final boolean result, String responseBody) throws Exception {
        try (final Connection connection = initConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO action_log(action, link, create_time, vk_acc_id, result, response_body) VALUES (?, ?, NOW(), ?, ?, ?)");
            preparedStatement.setString(1, action);
            preparedStatement.setString(2, link);
            preparedStatement.setInt(3, vkAccId);
            preparedStatement.setBoolean(4, result);
            if (responseBody == null) {
                preparedStatement.setNull(5, Types.VARCHAR);
            } else {
                preparedStatement.setString(5, responseBody);
            }
            preparedStatement.execute();
        }
    }

    public boolean isLinkOnCooldown(final String link) {
        try {
            try (final Connection connection = initConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM action_log WHERE link=? AND create_time > DATE_SUB(NOW(), INTERVAL 1 HOUR)");
                preparedStatement.setString(1, link);
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                int usages = resultSet.getInt(1);
                System.out.println("Amount of link usage for [" + link + "] is " + usages);
                return usages > 7;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public int getAmountOfAttemptsForRegistration(final String ip) throws Exception {
        try (final Connection connection = initConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT attempts_to_registrate FROM blacklist_ips WHERE ip=?");
            preparedStatement.setString(1, ip);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }

    public boolean isSubNetBlacklisted(final String ip) throws Exception {
        try (final Connection connection = initConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM blacklist_ips_prefixes WHERE ? LIKE CONCAT(prefix, '%')");
            preparedStatement.setString(1, ip);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public boolean hadAction(Integer id, String link, String action) throws Exception {
        try (final Connection connection = initConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM action_log WHERE vk_acc_id=? AND action=? AND link=? AND result='1'");
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, action);
            preparedStatement.setString(3, link);
            return preparedStatement.executeQuery().next();
        }
    }

    public void fixateRegistrationAttempt(final String ip) throws Exception {
        try (final Connection connection = initConnection()) {
            int amountOfAttemptsForRegistration = getAmountOfAttemptsForRegistration(ip);
            PreparedStatement preparedStatement;
            if (amountOfAttemptsForRegistration == 0) {
                preparedStatement = connection.prepareStatement("INSERT INTO blacklist_ips(ip, attempts_to_registrate) VALUES (?, 1)");
                preparedStatement.setString(1, ip);
                preparedStatement.execute();
            } else {
                preparedStatement = connection.prepareStatement("UPDATE blacklist_ips SET attempts_to_registrate=attempts_to_registrate + 1 WHERE ip=?");
                preparedStatement.setString(1, ip);
                preparedStatement.executeUpdate();
            }
        }
    }

    public void logInvoice(final Integer vkId, final Float sum, final String service) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO invoice_history(vk_id, date, sum, service) VALUES (?, NOW(), ?, ?)");
            saveStatement.setInt(1, vkId);
            saveStatement.setFloat(2, sum);
            saveStatement.setString(3, service);
            saveStatement.execute();
        }
    }

    public void updateProfileInfoForVkAcc(VkAccount vkAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement saveStatement = connection.prepareStatement("UPDATE vk_account SET firstname=?, lastname=?, day_of_birth=?, month_of_birth=?, year_of_birth=?, sex=? WHERE id=?");
            saveStatement.setString(1, vkAccount.getFirstName());
            saveStatement.setString(2, vkAccount.getLastName());
            saveStatement.setInt(3, vkAccount.getDayOfBirth());
            saveStatement.setInt(4, vkAccount.getMonthOfBirth());
            saveStatement.setInt(5, vkAccount.getYearOfBirth());
            saveStatement.setString(6, vkAccount.getSex());
            saveStatement.setInt(7, vkAccount.getId());
            saveStatement.execute();
        }
    }

    public void saveYouTubeAcc(final YoutubeAccount youtubeAccount) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO youtube_account(phone, vk_acc_id, gmail) VALUES (?, ?, ?)");
            saveStatement.setString(1, youtubeAccount.getPhone());
            saveStatement.setInt(2, youtubeAccount.getVkAccId());
            saveStatement.setString(3, youtubeAccount.getGmail());
            saveStatement.execute();
        }
    }

    public Date getLatestInvoice(Integer id, String service) throws Exception {
        try (final Connection connection = initConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT date FROM invoice_history WHERE vk_id=? AND service=? ORDER BY date DESC");
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, service);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDate(1);
            }
            return null;
        }
    }

    public void markBrowserAsNotReadyYet(String browserProfile) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO not_ready_browsers(code) VALUES (?)");
            saveStatement.setString(1, browserProfile);
            saveStatement.execute();
        }
    }

    public void savePayUpVideoAcc(PayUpVideoAcc payUpVideoAcc) throws Exception {
        try (final Connection connection = initConnection()) {
            final PreparedStatement saveStatement = connection.prepareStatement("INSERT INTO payup_video_account(vk_acc_id) VALUES (?)");
            saveStatement.setInt(1, payUpVideoAcc.getVkAccount().getId());
            saveStatement.execute();
        }
    }
}