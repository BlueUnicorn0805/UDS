package hawaiiappbuilders.c;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.K;

public class SplashActivity extends BaseActivity {

    private long showStartTime;
    private final static long DELAY_TIME = 1500;
    private boolean isRunning;

    ImageView ivLogo;
    TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String eky = K.eKy("117731");
        Log.e("DevID", eky);
        eky = K.eKy("92823");
        Log.e("R2", eky);

        String diverID = appSettings.getDriverID();

        ivLogo = (ImageView) findViewById(R.id.ivLogo);
        tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvVersion.setText(getVersionName());
        startAnimation();
        startSplash();
    }

    private void startAnimation() {
        ivLogo.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ivLogo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        AnimatorSet mAnimatorSet = new AnimatorSet();
                        mAnimatorSet.playTogether(ObjectAnimator.ofFloat(ivLogo, "alpha", 0, 1, 1, 1),
                                ObjectAnimator.ofFloat(ivLogo, "scaleX", 0.3f, 1.05f, 0.9f, 1),
                                ObjectAnimator.ofFloat(ivLogo, "scaleY", 0.3f, 1.05f, 0.9f, 1));
                        mAnimatorSet.setDuration(1500);
                        mAnimatorSet.start();
                    }
                });
        tvVersion.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        tvVersion.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        AnimatorSet mAnimatorSet = new AnimatorSet();
                        mAnimatorSet.playTogether(ObjectAnimator.ofFloat(tvVersion, "alpha", 0, 1, 1, 1),
                                ObjectAnimator.ofFloat(tvVersion, "scaleX", 0.3f, 1.05f, 0.9f, 1),
                                ObjectAnimator.ofFloat(tvVersion, "scaleY", 0.3f, 1.05f, 0.9f, 1));
                        mAnimatorSet.setDuration(1500);
                        mAnimatorSet.start();
                    }
                });

    }

    private void startSplash() {

        showStartTime = System.currentTimeMillis();
        isRunning = true;

        Thread background = new Thread() {
            public void run() {
                try {
                    // Delay Time
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - showStartTime < DELAY_TIME) {
                        try {
                            // Delay for DELAY_TIME
                            Thread.sleep(showStartTime + DELAY_TIME
                                    - currentTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                } catch (Exception e) {
                    return;
                } finally {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            doFinish();
                        }
                    });
                }
            }
        };

        background.start();
    }

    private void doFinish() {
        if (this.isRunning) {

            // Check Permissions
            //if (checkPermissions(mContext, PERMISSION_REQUEST_LOCATION_STRING, true, PERMISSION_REQUEST_CODE_LOCATION)) {
            // We don't ask permission here.
            checkToken();
            //}
        }
    }

    private void checkToken() {

        // We don't ask permission here.
        //mMyApp.updatedLocation();

        if (appSettings.isLoggedIn()) {
            startActivity(new Intent(mContext, MainActivity.class));
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            finish();
        } else {
            // Call SecurityCk before login
            AppSettings appSettings = new AppSettings(mContext);
            final Map<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "securityCk", BaseFunctions.MAIN_FOLDER, appSettings.getLastLocationLat(), appSettings.getLastLocationLon(), mMyApp.getAndroidId());
            String extraParams = "&mode=1" +
                    "&WeMightNeedRefreshTokenLaterButNotInAppsNow=" + appSettings.getDeviceToken();
            baseUrl += extraParams;

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }

            Log.e("securityCk", fullParams);

            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("securityCk", response);

                    if (!TextUtils.isEmpty(response)) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return params;
                }
            };

            sr.setRetryPolicy(new DefaultRetryPolicy(
                    25000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            sr.setShouldCache(false);
            queue.add(sr);

            //startActivity(new Intent(mContext, ActivityLogin.class));
            startActivity(new Intent(mContext, ZAUHowItWorksActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (this.isRunning) {
            this.isRunning = false;
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check All Permission was granted
        boolean bAllGranted = true;
        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                bAllGranted = false;
                break;
            }
        }

        if (bAllGranted) {
            checkToken();
        } else {
            showAlert(R.string.request_permission_hint);
        }
    }
}
