package hawaiiappbuilders.c;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EarnedActivity extends BaseActivity implements View.OnClickListener {

    NestedScrollView scrollView;

    TextView valueCurrentBalance;
    TextView valuePayouts;
    TextView valueDeliveries;
    TextView valueTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earned);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        valueCurrentBalance = findViewById(R.id.valueCurrentBalance);
        valuePayouts = findViewById(R.id.valuePayouts);
        valueDeliveries = findViewById(R.id.valueDeliveries);
        valueTips = findViewById(R.id.valueTips);

        findViewById(R.id.btnViewDetails).setOnClickListener(this);
        findViewById(R.id.btnDetails).setOnClickListener(this);

        scrollView = findViewById(R.id.scrollView);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.pageScroll(View.FOCUS_UP);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        }, 100);

        getBalance();
        getData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //listener for home
        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnViewDetails) {
            startActivity(new Intent(mContext, TransactionHistoryActivity.class));
        } else if (viewId == R.id.btnDetails) {
            startActivity(new Intent(mContext, ActivityBank.class));
        }
    }

    private void getBalance() {
        if (getLocation()) {
            final HashMap<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());
            String extraParams = "&mode=AllBal" + "&misc=" + appSettings.getUserId();
            baseUrl += extraParams;
            Log.e("avaBal", params.toString());

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }

            Log.e("avaBal", fullParams);

            //urlGetRes += fullParams.substring(1);

            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {

                            showToastMessage(jsonObject.getString("msg"));
                            logout(false);
                        } else {
                            String instaCash = jsonObject.getString("instaCash");
                            String instaSaving = jsonObject.getString("instaSavings");

                            try {
                                float fInstaCash = Float.parseFloat(instaCash);
                                instaCash = String.format("%.02f", fInstaCash);
                                valueCurrentBalance.setText(String.format("$ %.02f", fInstaCash));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                            try {
                                float fInstaSaving = Float.parseFloat(instaSaving);
                                instaSaving = String.format("%.02f", fInstaSaving);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToastMessage(e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    networkErrorHandle(mContext, error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    25000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            stringRequest.setShouldCache(false);

            queue.add(stringRequest);
        }
    }

    private void getData() {
        if (getLocation()) {
            final HashMap<String, String> params = new HashMap<>();
            String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());
            String extraParams = "&mode=" + "delPaidOuts" +
                    "&sellerID=" + "0" +
                    "&misc=" + appSettings.getUserId();
            baseUrl += extraParams;
            Log.e("avaTXs", params.toString());

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }

            Log.e("delPaidOuts", fullParams);

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    Log.e("delPaidOuts", response);

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;
                            if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                                showToastMessage(jsonObject.getString("msg"));
                                logout(false);
                            } else {
                                valuePayouts.setText(String.format("$%.1f", (float) jsonObject.optDouble("Paid")));
                                valueDeliveries.setText(String.format("$%.1f", (float) jsonObject.optDouble("Dels")));
                                valueTips.setText(String.format("$%.1f", (float) jsonObject.optDouble("Tips")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showAlert(e.getMessage());
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideProgressDialog();

                    showAlert("Request Error!, Please check network.");
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return params;
                }
            };

            sr.setShouldCache(false);
            queue.add(sr);
        }
    }
}
