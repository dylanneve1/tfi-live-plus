package com.tfigo.app;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.GeolocationPermissions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private int statusBarHeight = 0;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setupSystemBars();

        webView = new WebView(this);
        setContentView(webView);

        // Listen for window insets to get status bar height
        ViewCompat.setOnApplyWindowInsetsListener(webView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            statusBarHeight = insets.top;
            int navBarHeight = insets.bottom;

            // Inject CSS variable with actual status bar height into the page
            webView.evaluateJavascript(
                "document.documentElement.style.setProperty('--status-bar-height', '" + statusBarHeight + "px');" +
                "document.documentElement.style.setProperty('--nav-bar-height', '" + navBarHeight + "px');",
                null
            );

            return WindowInsetsCompat.CONSUMED;
        });

        // Configure WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString() + " TFIGo/1.0");

        // Allow viewport meta tag to work
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Re-inject insets after page load
                if (statusBarHeight > 0) {
                    view.evaluateJavascript(
                        "document.documentElement.style.setProperty('--status-bar-height', '" + statusBarHeight + "px');",
                        null
                    );
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                    GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        // Set background color to match theme
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            webView.setBackgroundColor(Color.parseColor("#1a1c19"));
        } else {
            webView.setBackgroundColor(Color.parseColor("#fcfdf6"));
        }

        // Load the app
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    private void setupSystemBars() {
        Window window = getWindow();
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDark = nightMode == Configuration.UI_MODE_NIGHT_YES;

        // Make status bar and nav bar transparent
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        // Set icon colors based on theme
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(!isDark);
        controller.setAppearanceLightNavigationBars(!isDark);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
