package com.akansh.mythoughts;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private int exit=0;
    private ValueCallback<Uri[]> mfilePathCallback;
    private int PICK_CODE=1;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { WebView.enableSlowWholeDocumentDraw(); }
        setContentView(R.layout.activity_main);
        webView=findViewById(R.id.web);
        registerForContextMenu(webView);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setAppCachePath("/data/data" + this.getPackageName() + "/cache");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.addJavascriptInterface(new WebAppInterface(this),"Android");
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                AlertDialog dialog=new AlertDialog.Builder(view.getContext()).
                        setTitle("Information").
                        setMessage(message).
                        setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();
                dialog.show();
                result.confirm();
                return true;
            }
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                changeP(newProgress);
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mfilePathCallback=filePathCallback;
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent,PICK_CODE);
                return true;
            }
        });

        File f=new File("/data/data/"+getPackageName()+"/"+Constants.NEW_DIR+"/index.html");
        if(!f.exists()) {
            ContentFetcher contentFetcher=new ContentFetcher(getPackageName());
            contentFetcher.downloadListeners=new ContentFetcher.DownloadListeners() {
                @Override
                public void onDownloadCompeted(boolean status) {
                    progress.cancel();
                    if(status) {
                        webView.loadUrl("file:///data/data/"+getPackageName()+"/"+Constants.NEW_DIR+"/index.html");
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Error!");
                        builder.setMessage("Something went wrong!\nPlease try again later");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                finishAffinity();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.setCancelable(false);
                        alert.setCanceledOnTouchOutside(false);
                        alert.show();
                    }
                }

                @Override
                public void onDownloadStarted() {
                    progress=new ProgressDialog( MainActivity.this);
                    progress.setMessage("Preparing App For First Use\nPlease Wait...");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(true);
                    progress.setProgress(0);
                    progress.setCancelable(false);
                    progress.setCanceledOnTouchOutside(false);
                    progress.show();
                }
            };
            contentFetcher.execute();
        }else{
            webView.loadUrl("file:///data/data/"+getPackageName()+"/"+Constants.NEW_DIR+"/index.html");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri result = data.getData();
            Uri[] resultArr = new Uri[1];
            resultArr[0] = result;
            mfilePathCallback.onReceiveValue(resultArr);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void changeP(int progress) {  //Change Webpage Progress
        if(progress<=100) {
            try {
                final ProgressBar pBar;
                pBar=findViewById(R.id.progressBar);
                if(pBar.getVisibility()==ProgressBar.GONE) {
                    pBar.setVisibility(View.VISIBLE);
                }
                pBar.setProgress(progress);
                if(progress==100) {
                    pBar.setVisibility(View.GONE);
                }
            }catch (Exception e) {
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            if(webView.getVisibility()== View.VISIBLE) {
                webView.goBack();
            }
        }else{
            exit();
        }
    }

    public void exit() {  //Function For Double Tap Exit
        if(exit==0) {
            Toast.makeText(this,"Press One More Time To Exit!",Toast.LENGTH_LONG).show();
            Timer myTimer=new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    exit=0;
                }
            },2000);
            exit++;
        }
        if(exit==1) {
            exit++;
        }else if(exit==2) {
            finishAffinity();
        }
    }

    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) {
            mContext = c;
        }
        @JavascriptInterface
        public void share(String quote) {
            Toast.makeText(mContext,"Sharing Quote...",Toast.LENGTH_LONG).show();
            String shareBody = quote+"\nQuote Shared Via MyThoughts App";
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sharing Quote...");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            mContext.startActivity(Intent.createChooser(sharingIntent, "Sharing Via MyThoughts"));
        }
    }

}
