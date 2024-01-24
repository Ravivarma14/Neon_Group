package com.example.neon_group_task;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.jetbrains.annotations.NotNull;

//imports for webview

public class MainActivity extends AppCompatActivity {

    private WebView googleWebView;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private int count=0;
    private int c=0;
    private Dialog progressDialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checking for Internet
        if(!isConnectedToInternet()){
            displayDialog(true,"");
        }
        else {
            //show loading dialog
            displayLoading();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setup();
                }
            },2000);

        }

    }

    //to show Interstitial Ad on 10 clicks
    public void showInterstitialAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(MainActivity.this);
        } else {
           Toast.makeText(getApplicationContext(), "The interstitial ad wasn't ready yet.",Toast.LENGTH_SHORT).show();
       }
    }

    //when click on back
    @Override
    public void onBackPressed() {
        if(googleWebView.canGoBack()){
            googleWebView.goBack();
        }
        else{
            super.onBackPressed();
        }
    }


    //class for google web view client
    public class googleWebViewClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if(progressDialog!=null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.equals("https://www.google.com")){
                view.loadUrl(url);
            }
            return true;
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            displayDialog(false, (String) error.getDescription());
            super.onReceivedError(view, request, error);
        }
    }

    //check for internet connectivity to show dialog
    public boolean isConnectedToInternet(){
        ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
    if(networkInfo!=null){
        if(networkInfo.isConnected())
            return true;
        else
            return false;
    }
    else{
        return false;
    }
    }

    // dialog for internet unavailable and error while loading google
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public void displayDialog(boolean isNoInternet, String error){

        Dialog dialog=new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_ui));
        dialog.setCancelable(false);
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        //when error occurred while loading google website
        if(!isNoInternet){
            dialog.findViewById(R.id.openSettings).setVisibility(View.GONE);
            dialog.findViewById(R.id.refresh).setVisibility(View.GONE);
            dialog.findViewById(R.id.retry).setVisibility(View.VISIBLE);
            TextView title=(TextView) dialog.findViewById(R.id.title);
            title.setText("Error Occured");
            TextView message=(TextView) dialog.findViewById(R.id.message);
            message.setText("Your Internet Connection May not be active Or " + error);
            dialog.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isConnectedToInternet()) {
                        dialog.dismiss();
                        setup();
                    }
                }
            });
        }

        else {
            dialog.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isConnectedToInternet()) {
                        dialog.dismiss();
                        setup();
                    }
                }
            });


            dialog.findViewById(R.id.openSettings).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //to open network settings
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
        }

        dialog.show();
    }

    //loading dialog before loading website
    public void displayLoading(){
        progressDialog=new Dialog(MainActivity.this,R.style.MyAlertDialogStyle);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_ui));
        progressDialog.setCancelable(false);

        progressDialog.show();

    }

    public void setup(){
        //code for google webview
        googleWebView = findViewById(R.id.webView);
        googleWebView.setWebViewClient(new googleWebViewClient());
        googleWebView.loadUrl("https://www.google.com/");
        WebSettings settings = googleWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        //code for admob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //load the ad
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //interstitial ad

        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NotNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NotNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                    }
                });

        findViewById(R.id.webView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                count++;
                c++;
                if (c == 3) {
                    count--;
                    c = 0;
                }
                if (count == 10) {
                    count = 0;
                    showInterstitialAd();
                }
                return false;
            }
        });
    }

}