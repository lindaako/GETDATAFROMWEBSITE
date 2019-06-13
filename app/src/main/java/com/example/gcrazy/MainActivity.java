package com.example.gcrazy;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
{
    int line_counter = 0;
    int Availability = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView =  findViewById(R.id.webView);
        TextView contentView =  findViewById(R.id.contentView);


        WebSettings webSettings = webView.getSettings();

        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        try {
            Method m = WebSettings.class.getMethod("setMixedContentMode", int.class);
            if ( m == null )
            {
                Log.e("WebSettings", "Error getting setMixedContentMode method");
            }
            else {
                m.invoke(webSettings, 2); // 2 = MIXED_CONTENT_COMPATIBILITY_MODE
                Log.i("WebSettings", "Successfully set MIXED_CONTENT_COMPATIBILITY_MODE");
            }
        }
        catch (Exception ex) {
            Log.e("WebSettings", "Error calling setMixedContentMode: " + ex.getMessage(), ex);
        }


        class MyJavaScriptInterface
        {
            private TextView contentView;

            public MyJavaScriptInterface(TextView aContentView)
            {
                contentView = aContentView;
            }

            @SuppressWarnings("unused")
            @JavascriptInterface
            public void processContent(String aContent)
            {
                final String content = aContent;
                contentView.post(new Runnable()
                {
                    public void run()
                    {


                        contentView.setText(content);
                        System.out.print(content);

                        BufferedReader br = new BufferedReader(new StringReader(content));

                        String result[] = new String[500];
                        String line = "";


                        while (true)
                        {
                            try
                            {
                                if (!((line = br.readLine()) != null))
                                    break;
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }


                            result[line_counter] = line;
                            line_counter++;
                        }

                        //Toast.makeText(MainActivity.this,result[62],Toast.LENGTH_LONG).show();

                        for(int cc = 0; cc < line_counter; cc++)
                        {

                            if (result[cc].contains("Status\tAvailable"))
                            {
                               // Toast.makeText(MainActivity.this, "The index of status is = " + result[cc], Toast.LENGTH_LONG).show();
                                Availability++;
                            }

                            else

                                {

                            }
                        }

                        Toast.makeText(MainActivity.this, Availability + " books available", Toast.LENGTH_LONG).show();

                    }
                });
            }

            @SuppressWarnings("unused")
            @JavascriptInterface
            public void processHTML(String html)
            {
                //Html extract here

                final String content = html;
                contentView.post(new Runnable()
                {
                    public void run()
                    {
                        contentView.setText(content);
                        System.out.println(content);


                    }
                });

            }
        }



        webView.addJavascriptInterface(new MyJavaScriptInterface(contentView), "INTERFACE");
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url)
            {
                try
                {
                    synchronized(this)
                    {
                        wait(500);
                    }
                }

                catch(InterruptedException ex)
                {

                }

                    view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");

            }
        });

        webView.clearCache(true);
        webView.loadUrl("https://pyxis.knu.ac.kr/en/#/search/detail/4500295");

    }
}
