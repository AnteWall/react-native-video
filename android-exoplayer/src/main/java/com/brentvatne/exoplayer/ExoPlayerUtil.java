package com.brentvatne.exoplayer;
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Application;
import android.util.Base64;
import android.util.Log;

import com.brentvatne.exoplayer.util.DefaultDrmService;
import com.brentvatne.exoplayer.util.StringConverterModule;
import com.brentvatne.exoplayer.util.Utils;
import com.brentvatne.exoplayer.util.WidevineDrmLicense;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;
import retrofit.mime.TypedByteArray;

/**
 * Utility methods for the demo application.
 */
public class ExoPlayerUtil {
    public static final int CONNECT_TIMEOUT = 120;
    public static final int DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final int READ_TIMEOUT = 120;
    public final static String NONE_LANGUAGE_CODE = "none";

    private final CookieManager defaultCookieManager;
    private DefaultDrmService defaultDrmService;
    //private final Application application;

    public ExoPlayerUtil(DefaultDrmService defaultDrmService /*, Application application*/) {
        this.defaultDrmService = defaultDrmService;
        //this.application = application;

        this.defaultCookieManager = new CookieManager();
        this.defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    public byte[] executePostProvisioning(String url, byte[] data, Map<String, String> requestProperties, boolean licenseKeyRequest)
            throws Exception {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(data != null);
            urlConnection.setDoInput(true);
            if (requestProperties != null) {
                for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
                    urlConnection.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
                }
            }
            // Write the request body, if there is one.
            if (data != null) {
                OutputStream out = urlConnection.getOutputStream();
                try {
                    out.write(data);
                } finally {
                    out.close();
                }
            }
            // Read and return the response body.
            InputStream inputStream = urlConnection.getInputStream();
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte scratch[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(scratch)) != -1) {
                    byteArrayOutputStream.write(scratch, 0, bytesRead);
                }
                return byteArrayOutputStream.toByteArray();
            } finally {
                inputStream.close();
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }


    public byte[] executePostLicenseRequest(String url, byte[] data, Map<String, String> requestProperties, boolean licenseKeyRequest)
            throws Exception {

        // create adapter
        RestAdapter adapter;
        if (url.contains("https")) {
            adapter = new RestAdapter.Builder().setClient(provideClient(createOkHttpClient(/*application*/)))
                    .setEndpoint(Endpoints.newFixedEndpoint("https://"))
                    .setConverter(new JacksonConverter(provideObjectMapper()))
                    .build();
        } else {
            adapter = new RestAdapter.Builder().setClient(provideClient(createOkHttpClient(/*application*/)))
                    .setEndpoint(Endpoints.newFixedEndpoint("http://"))
                    .setConverter(new JacksonConverter(provideObjectMapper()))
                    .build();
        }
        this.defaultDrmService = adapter.create(DefaultDrmService.class);

        // remove https and http
        url = Utils.removeScheme(url);


        TypedByteArray typedByteArray = new TypedByteArray("*/*", data);
        try {
            if (licenseKeyRequest) {
                WidevineDrmLicense widevineDrmLicense = this.defaultDrmService.getDrmLicense(url, typedByteArray);
                Log.d("Drm: ", widevineDrmLicense.toString());
                return Base64.decode(widevineDrmLicense.getLicense(), Base64.DEFAULT);
            } else {
                return this.defaultDrmService.sendData(url, typedByteArray);
            }
        } catch (Exception e) {
            Log.e("PostLicense", Log.getStackTraceString(e));
            return null;
        }
    }

    public void setDefaultCookieManager() {
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != this.defaultCookieManager) {
            CookieHandler.setDefault(this.defaultCookieManager);
        }
    }

    ObjectMapper provideObjectMapper() {
        ObjectMapper jacksonObjectMapper = new ObjectMapper();
        jacksonObjectMapper.setPropertyNamingStrategy(new PropertyNamingStrategy.PascalCaseStrategy());
        jacksonObjectMapper.registerModule(new StringConverterModule());

        // This way it won't complain if we don't have every part of the json object defined
        jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return jacksonObjectMapper;
    }

    Client provideClient(OkHttpClient client) {
        client.setConnectTimeout(30, TimeUnit.SECONDS);
        return new OkClient(client);
    }

    // TODO CHANGE HTTPCLIENT TO V3 we have both 2 and 3 now
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
            Log.e("Failed install cache", e);
        }*/

        return client;
    }

}