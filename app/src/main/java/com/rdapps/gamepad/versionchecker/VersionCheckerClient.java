package com.rdapps.gamepad.versionchecker;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class VersionCheckerClient {
    private static final String BASE_URL = "https://raw.githubusercontent.com/";

    private static Retrofit retrofit = null;
    private static VersionCheckerService service = null;


    public static synchronized VersionCheckerService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
        }
        if (service == null) {
            service = retrofit.create(VersionCheckerService.class);
        }
        return service;
    }
}
