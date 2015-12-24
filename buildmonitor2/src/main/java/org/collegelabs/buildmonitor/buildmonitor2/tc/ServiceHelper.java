package org.collegelabs.buildmonitor.buildmonitor2.tc;

import android.util.Base64;
import com.facebook.stetho.okhttp.BuildConfig;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

import java.util.concurrent.TimeUnit;

public class ServiceHelper {

    public static TeamCityService getService(Credentials credentials){
        String userAndPassword = credentials.username + ":" + credentials.password;
        final String authHeader = "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyyMMdd'T'HHmmssZ")
                .create();

        final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
        final int READ_TIMEOUT_MILLIS = 60 * 1000; // 60s

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        client.setReadTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        if(BuildConfig.DEBUG){
            client.networkInterceptors().add(new StethoInterceptor());
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(credentials.server)
                .setClient(new OkClient(client))
                .setRequestInterceptor(new BasicAuthInterceptor(authHeader))
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setConverter(new GsonConverter(gson))
                .build();

        return restAdapter.create(TeamCityService.class);
    }
}
