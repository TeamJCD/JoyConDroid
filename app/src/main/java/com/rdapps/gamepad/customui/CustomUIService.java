package com.rdapps.gamepad.customui;

import com.rdapps.gamepad.model.CustomUIItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CustomUIService {

    @GET("/")
    Call<List<CustomUIItem>> getCustomUIs();
}
