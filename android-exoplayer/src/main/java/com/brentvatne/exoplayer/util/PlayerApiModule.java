package com.brentvatne.exoplayer.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.squareup.okhttp.OkHttpClient;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;

public class PlayerApiModule {

    public static final int DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final int CONNECT_TIMEOUT = 120;
    public static final int READ_TIMEOUT = 120;
    public RestAdapter restAdapter = null;
    public PlayerApiModule() {
        this.restAdapter = poviesHttpRestAdapter(provideObjectMapper(), provideClient(provideOkHttpClient()));
    }

    OkHttpClient provideOkHttpClient() {
        OkHttpClient client = createOkHttpClient();
        client.setConnectTimeout(30, TimeUnit.SECONDS);
        return client;
    }

    Client provideClient(OkHttpClient client) {
        client.setConnectTimeout(30, TimeUnit.SECONDS);
        return new OkClient(client);
    }

    ObjectMapper provideObjectMapper() {
        ObjectMapper jacksonObjectMapper = new ObjectMapper();
        jacksonObjectMapper.setPropertyNamingStrategy(new PropertyNamingStrategy.PascalCaseStrategy());
        jacksonObjectMapper.registerModule(new StringConverterModule());

        // This way it won't complain if we don't have every part of the json object defined
        jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return jacksonObjectMapper;
    }

    RestAdapter poviesHttpRestAdapter(ObjectMapper jacksonObjectMapper,
                                      Client client) {

        return new RestAdapter.Builder().setClient(client)
                .setEndpoint(Endpoints.newFixedEndpoint("http://"))
                .setConverter(new JacksonConverter(jacksonObjectMapper))
                .build();
    }

    public DefaultDrmService provideDrmService() {
        return this.restAdapter.create(DefaultDrmService.class);
    }

    private static OkHttpClient createOkHttpClient(/*Application app*/) {
        OkHttpClient client = new OkHttpClient();

        client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS); // connect timeout
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS);    // socket timeout

        // Install an HTTP cache in the application cache directory.
        /*try {
            File cacheDir = new File(app.getCacheDir(), "http");
            Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
            client.setCache(cache);
        } catch (IOException e) {
            Log.d("Unable to install disk cache.", e);
        }*/

        return client;
    }

    private static SSLSocketFactory createBadSslSocketFactory() {
        try {
            // Construct SSLSocketFactory that accepts any cert.
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager permissive = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            context.init(null, new TrustManager[] { permissive }, null);
            return context.getSocketFactory();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

}