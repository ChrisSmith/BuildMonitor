package org.collegelabs.buildmonitor.buildmonitor2.tc;

import android.util.Base64;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import org.collegelabs.buildmonitor.buildmonitor2.BuildConfig;
import org.collegelabs.buildmonitor.buildmonitor2.storage.Database;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observable;

import java.util.concurrent.TimeUnit;

public class ServiceHelper {

    public static Observable<TeamCityService> getService(Database db){
        return db.GetCredentials()
            .map(ServiceHelper::getService);
    }

    public static TeamCityService getService(Credentials credentials){
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

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(getEndpoint(credentials))
                .setClient(new OkClient(client))
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setRequestInterceptor(new JsonHeaderInterceptor())
                .setConverter(new GsonConverter(gson));

        if(!credentials.isGuest){
            String userAndPassword = credentials.username + ":" + credentials.password;
            final String authHeader = "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);
            builder.setRequestInterceptor(new BasicAuthInterceptor(authHeader));
        }

        return builder.build().create(TeamCityService.class);
    }

    public static String getEndpoint(Credentials credentials){
        HttpUrl.Builder endpoint = HttpUrl.parse(credentials.server).newBuilder();

        if(credentials.isGuest){
            endpoint.addPathSegment("guestAuth");
        }

        return endpoint.addPathSegment("app")
                .addPathSegment("rest")
                .build()
                .toString();
    }
}
