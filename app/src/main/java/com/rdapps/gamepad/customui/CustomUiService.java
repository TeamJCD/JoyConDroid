package com.rdapps.gamepad.customui;

import com.rdapps.gamepad.model.CustomUiItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface CustomUiService {

    @GET("index.html")
    Call<List<CustomUiItem>> getCustomUis();
}
