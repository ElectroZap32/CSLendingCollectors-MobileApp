package com.cslending.zc.collapp.collections;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.cslending.zc.collapp.R;

public class Worksheet extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worksheet);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        SharedPreferences ret = getApplicationContext().getSharedPreferences("ColInfo", Context.MODE_PRIVATE);
        final String send_col_id = ret.getString(getResources().getString(R.string.collector),"");

        String url = "https://cslending-zc.com/csl-modules/webview-worksheet.php?coll_id=" + send_col_id;

        WebView webview = findViewById(R.id.myWebView);
        webview.getSettings().setJavaScriptEnabled(true);
        if (cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl(url);
    }
}