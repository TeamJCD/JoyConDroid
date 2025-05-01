package com.rdapps.gamepad.web;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@UtilityClass
public class CachingWebUtils {
    private static final int CACHE_MAX_AGE = 60 * 60 * 24 * 7; // 7 days

    private OkHttpClient okHttpClient;

    public OkHttpClient getOkHttpClient(Context context) {
        if (okHttpClient == null) {
            File cacheDir = new File(context.getCacheDir(), "http_cache");
            int cacheSize = 100 * 1024 * 1024; // 100MB

            Cache cache = new Cache(cacheDir, cacheSize);

            okHttpClient = new OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(chain -> {
                        Request request = chain.request();

                        if (!isNetworkAvailable(context)) {
                            request = request.newBuilder()
                                    .cacheControl(new CacheControl.Builder()
                                            .onlyIfCached()
                                            .maxStale(CACHE_MAX_AGE, TimeUnit.SECONDS)
                                            .build())
                                    .build();
                        }

                        return chain.proceed(request);
                    })
                    .build();
        }

        return okHttpClient;
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                ContextCompat.getSystemService(context, ConnectivityManager.class);

        if (connectivityManager == null) {
            return false;
        }

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }

        NetworkCapabilities capabilities = connectivityManager
                .getNetworkCapabilities(activeNetwork);

        return capabilities != null
                && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
    }
}
