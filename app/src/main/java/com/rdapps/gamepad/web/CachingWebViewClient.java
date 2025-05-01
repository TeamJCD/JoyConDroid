package com.rdapps.gamepad.web;

import static com.rdapps.gamepad.log.JoyConLog.log;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CachingWebViewClient extends WebViewClient {
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        log("CONTENT", String.format(Locale.ROOT, "Error fetching %s: %d %s",
                request.getUrl(), error.getErrorCode(), error.getDescription()));

        view.loadUrl("file:///android_asset/error.html");
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        try {
            Request okRequest = new Request.Builder()
                    .url(request.getUrl().toString())
                    .build();

            Response okResponse = CachingWebUtils.getOkHttpClient(view.getContext())
                    .newCall(okRequest).execute();
            ResponseBody body = okResponse.body();
            if (body == null || (!okResponse.isSuccessful() && okResponse.code() != 504)) {
                return super.shouldInterceptRequest(view, request);
            }

            MediaType mediaType = body.contentType();

            String mimeType = Optional.ofNullable(mediaType)
                    .map(contentType -> contentType.type() + "/" + contentType.subtype())
                    .orElse("text/plain");

            String encoding = Optional.ofNullable(mediaType)
                    .map(MediaType::charset)
                    .map(Charset::name)
                    .orElse("UTF-8");

            Map<String, String> responseHeaders = new HashMap<>();
            for (String name : okResponse.headers().names()) {
                responseHeaders.put(name, okResponse.header(name));
            }

            InputStream stream = body.byteStream();

            int statusCode = okResponse.code();
            String reasonPhrase = okResponse.message();

            if (reasonPhrase.trim().isEmpty()) {
                reasonPhrase = getDefaultReasonPhrase(statusCode);
            }

            return new WebResourceResponse(mimeType, encoding, statusCode, reasonPhrase,
                    responseHeaders, stream);
        } catch (IOException e) {
            return super.shouldInterceptRequest(view, request);
        }
    }

    private boolean isNetworkAvailable(Context context) {
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

    private String getDefaultReasonPhrase(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 403 -> "Forbidden";
            case 500 -> "Internal Server Error";
            case 304 -> "Not Modified";
            default -> "Unknown";
        };
    }
}
