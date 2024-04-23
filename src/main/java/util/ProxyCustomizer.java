package util;

import bo.VkAccount;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyCustomizer {
    public static RestTemplate buildRestTemplate(final VkAccount vkAccount) {

        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        final String authUser = vkAccount.getProxyUsername();
        final String authPassword = vkAccount.getProxyPassword();
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authUser, authPassword.toCharArray());
                    }
                }
        );

        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);

        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(vkAccount.getProxy(),
                Integer.parseInt(vkAccount.getPort())));
        requestFactory.setProxy(proxy);

        return new RestTemplate(requestFactory);
    }
    public static RestTemplate buildRestTemplate(final VkAccount vkAccount, final Proxy.Type proxyType) {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        final String authUser = vkAccount.getProxyUsername();
        final String authPassword = vkAccount.getProxyPassword();
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authUser, authPassword.toCharArray());
                    }
                }
        );

        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);

        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        final Proxy proxy = new Proxy(proxyType, new InetSocketAddress(vkAccount.getProxy(),
                Integer.parseInt(vkAccount.getPort())));
        requestFactory.setProxy(proxy);

        return new RestTemplate(requestFactory);
    }

    public static String executeRequest(final VkAccount vkAccount, final HttpUriRequest request) throws Exception {
        final NTCredentials ntCreds = new NTCredentials(vkAccount.getProxyUsername() + ":" + vkAccount.getProxyPassword());
        final BasicResponseHandler responseHandler = new BasicResponseHandler();

        final int port = Integer.parseInt(vkAccount.getPort());
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(vkAccount.getProxy(), port), ntCreds);
        final HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        clientBuilder.useSystemProperties();
        clientBuilder.setProxy(new HttpHost(vkAccount.getProxy(), port));
        clientBuilder.setDefaultCredentialsProvider(credsProvider);
        clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        final CloseableHttpClient client = clientBuilder.build();

        return responseHandler.handleResponse(client.execute(request));
    }
    public static CloseableHttpResponse executeRequestForObject(final VkAccount vkAccount, final HttpUriRequest request) throws Exception {
        final NTCredentials ntCreds = new NTCredentials(vkAccount.getProxyUsername() + ":" + vkAccount.getProxyPassword());

        final int port = Integer.parseInt(vkAccount.getPort());
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(vkAccount.getProxy(), port), ntCreds);
        final HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        clientBuilder.useSystemProperties();
        clientBuilder.setProxy(new HttpHost(vkAccount.getProxy(), port));
        clientBuilder.setDefaultCredentialsProvider(credsProvider);
        clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        final CloseableHttpClient client = clientBuilder.build();

        return client.execute(request);
    }
}