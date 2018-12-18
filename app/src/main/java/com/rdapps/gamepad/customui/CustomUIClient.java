package com.rdapps.gamepad.customui;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class CustomUIClient {
    private static final String BASE_URL = "http://joycondroid.youtubeplays.com";

    private static Retrofit retrofit = null;
    private static CustomUIService service = null;


    public static synchronized CustomUIService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
        }
        if (service == null) {
            service = retrofit.create(CustomUIService.class);
        }
        return service;
    }
}
