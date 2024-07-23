package hawaiiappbuilders.c;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import hawaiiappbuilders.c.adapter.DelsDetailsAdapter;
import hawaiiappbuilders.c.model.DelDetailsInfo;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityDelsDetails extends BaseActivity {

    RecyclerView listDels;
    DelsDetailsAdapter adapter;
    ArrayList<DelDetailsInfo> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delsdetails);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        initViews();
        getData();
    }

    private void initViews() {
        listDels = findViewById(R.id.listDels);
        listDels.setHasFixedSize(true);
        listDels.setLayoutManager(new LinearLayoutManager(mContext));

        adapter = new DelsDetailsAdapter(mContext, dataList);
        listDels.setAdapter(adapter);
    }

    private void getData() {

        getLocation();

        final Map<String, String> params = new HashMap<>();

        String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());
        String extraParams = "&mode=tripDetails" + "&misc=all" +
                "&driverid=" + appSettings.getDriverID();
        baseUrl += extraParams;

        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }

        Log.e("tripDetails", fullParams);

        showProgressDialog();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        //HttpsTrustManager.allowAllSSL();
        GoogleCertProvider.install(mContext);

        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e("tripDetails", response);

                hideProgressDialog();

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;

                    if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                        showToastMessage(jsonObject.getString("msg"));
                    } else {
                        dataList.clear();
                        Gson gson = new Gson();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject favDataObj = jsonArray.getJSONObject(i);
                            Log.e("JSON", favDataObj.toString());
                            try {
                                dataList.add(gson.fromJson(favDataObj.toString(), DelDetailsInfo.class));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

        sr.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        sr.setShouldCache(false);
        queue.add(sr);
    }
}