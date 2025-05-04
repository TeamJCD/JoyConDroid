package com.rdapps.gamepad.versionchecker;

import retrofit2.Call;
import retrofit2.http.GET;

public interface VersionCheckerService {

    @GET("TeamJCD/JoyConDroid/main/version.json")
    Call<Version> getVersion();
}
