package com.rdapps.gamepad.customui;

import android.content.Context;
import com.rdapps.gamepad.web.CachingWebUtils;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class CustomUiClient {
    private static final String BASE_URL = "https://youtubeplays.github.io/JoyConDroidJS/";

    private static Retrofit retrofit = null;
    private static CustomUiService service = null;


    public static synchronized CustomUiService getService(Context context) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(CachingWebUtils.getOkHttpClient(context))
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
        }
        if (service == null) {
            service = retrofit.create(CustomUiService.class);
        }
        return service;
    }
}
