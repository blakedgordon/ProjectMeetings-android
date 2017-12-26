package edu.calbaptist.android.projectmeetings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private static final String TAG = "PrivacyPolicyActivity";

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        getSupportActionBar().setTitle(getString(R.string.privacy_policy));

        mWebView = findViewById(R.id.webview_privacy_policy);
        mWebView.loadUrl("http://" + getString(R.string.http_endpoint) + ":" +
                getResources().getInteger(R.integer.endpoint_port));
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }
}
