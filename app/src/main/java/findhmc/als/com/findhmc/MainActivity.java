package findhmc.als.com.findhmc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private WebView wb;
	private InterstitialAd interstitial;
    private Timer myTimer;
    private Boolean AppStatus=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Locate the Banner Ad in activity_main.xml
		AdView adView = (AdView) this.findViewById(R.id.adView);

		// Request for Ads
		AdRequest adRequest = new AdRequest.Builder()

				// Add a test device to show Test Ads
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("abc")
				.build();

		// Load ads into Banner Ads
		adView.loadAd(adRequest);

        wb = (WebView) findViewById(R.id.webView1);
        wb.getSettings().setJavaScriptEnabled(true);
        wb.getSettings().setLoadWithOverviewMode(true);
        wb.getSettings().setUseWideViewPort(true);
        wb.getSettings().setBuiltInZoomControls(true);
        wb.getSettings().setJavaScriptEnabled(true);
        wb.getSettings().setDomStorageEnabled(true);
        wb.setWebViewClient(new HelloWebViewClient());
        wb.loadUrl("http://www.findhmc.co.uk/mobilehome/");

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Loading...");
        progress.show();

        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                progress.cancel();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 5000);


        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, 60000);

    }

    private void TimerMethod()
    {
        this.runOnUiThread(Timer_Tick);
    }


    private Runnable Timer_Tick = new Runnable() {

        public void run() {

            if (AppStatus) {

                interstitial = new InterstitialAd(MainActivity.this);
                // Insert the Ad Unit ID
                //interstitial.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
                interstitial.setAdUnitId("ca-app-pub-2797390334436497/4028870168");

                // Request for Ads
                AdRequest adRequest = new AdRequest.Builder()

                        // Add a test device to show Test Ads
                        .build();


                // Load ads into Interstitial Ads
                interstitial.loadAd(adRequest);

                // Prepare an Interstitial Ad Listener
                interstitial.setAdListener(new AdListener() {
                    public void onAdLoaded() {
                        // Call displayInterstitial() function
                        displayInterstitial();
                    }
                });
            }
        }

    };


    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

	public void displayInterstitial() {
		// If Ads are loaded, show Interstitial else show nothing.
		if (interstitial.isLoaded()) {
			interstitial.show();
		}
	}


    @Override
    protected void onPause() {
        AppStatus = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        AppStatus = true;
        super.onResume();
    }



}
