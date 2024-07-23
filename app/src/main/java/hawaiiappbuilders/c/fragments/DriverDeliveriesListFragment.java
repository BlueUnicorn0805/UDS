/*
 * Copyright 2018 ShineStar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hawaiiappbuilders.c.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import hawaiiappbuilders.c.MainActivity;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.adapter.DriverDelsListAdapter;
import hawaiiappbuilders.c.adapter.OpenDeliveriesListAdapter;
import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.model.DeliveryItem;
import hawaiiappbuilders.c.model.OpenDeliveryInfo;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DriverDeliveriesListFragment extends BaseFragment implements View.OnClickListener {

    public static DriverDeliveriesListFragment newInstance(String text) {
        DriverDeliveriesListFragment mFragment = new DriverDeliveriesListFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    ListView lvOpenDels;
    OpenDeliveriesListAdapter openDeliveriesListAdapter;
    ArrayList<OpenDeliveryInfo> deliveriesOpen = new ArrayList<>();

    ListView lvMyDelsData;
    DriverDelsListAdapter driverDeliveriesListAdapter;
    ArrayList<DeliveryItem> deliveriesInfoByDriver = new ArrayList<>();

    TextView tabOpens;
    TextView tabPendings;
    TextView tabPast;
    int currentTabIdx = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_driverdelslist, container, false);

        init(getArguments());

        tabOpens = (TextView) rootView.findViewById(R.id.tabOpens);
        tabPendings = (TextView) rootView.findViewById(R.id.tabPendings);
        tabPast = (TextView) rootView.findViewById(R.id.tabPast);

        tabOpens.setOnClickListener(this);
        tabPendings.setOnClickListener(this);
        tabPast.setOnClickListener(this);

        rootView.findViewById(R.id.btnAddNew).setOnClickListener(this);

        lvOpenDels = (ListView) rootView.findViewById(R.id.lvOpenDelsData);
        openDeliveriesListAdapter = new OpenDeliveriesListAdapter(mContext, deliveriesOpen);
        lvOpenDels.setAdapter(openDeliveriesListAdapter);
        lvOpenDels.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //((MainActivity)parentActivity).showFragment(MainActivity.FRAGMENT_DRIVER_DELIVERY_MAP);
            }
        });

        lvMyDelsData = (ListView) rootView.findViewById(R.id.lvMyDelsData);
        driverDeliveriesListAdapter = new DriverDelsListAdapter(mContext, deliveriesInfoByDriver);
        lvMyDelsData.setAdapter(driverDeliveriesListAdapter);
        lvMyDelsData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((MainActivity)parentActivity).showFragment(MainActivity.FRAGMENT_DRIVER_DELIVERY_MAP);
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectTab(0);

        getOpenDelivery();
        getDriverDelivery();
    }

    private void selectTab(int tabIndex) {
        if (currentTabIdx == tabIndex) {
            return;
        }

        currentTabIdx = tabIndex;
        if (currentTabIdx == 0) {
            tabOpens.setSelected(true);
            tabPendings.setSelected(false);
            tabPast.setSelected(false);

            lvOpenDels.setVisibility(View.VISIBLE);
            lvMyDelsData.setVisibility(View.GONE);
        } else if (currentTabIdx == 1) {
            tabOpens.setSelected(false);
            tabPendings.setSelected(true);
            tabPast.setSelected(false);

            lvOpenDels.setVisibility(View.GONE);
            lvMyDelsData.setVisibility(View.VISIBLE);
        } else {
            tabOpens.setSelected(false);
            tabPendings.setSelected(false);
            tabPast.setSelected(true);

            lvOpenDels.setVisibility(View.GONE);
            lvMyDelsData.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tabOpens) {
            selectTab(0);
        } else if (viewId == R.id.tabPendings) {
            selectTab(1);
        } else if (viewId == R.id.tabPast) {
            selectTab(2);
        } else if (viewId == R.id.btnAddNew) {
            ((MainActivity)parentActivity).showFragment(MainActivity.FRAGMENT_DRIVER_DELIVERY_MAP);
        }
    }

    private void getOpenDelivery() {

        final Map<String, String> params = new HashMap<>();

        GpsTracker gpsTracker = new GpsTracker(parentActivity.getApplicationContext());
        String lat, lon;
        if(gpsTracker.canGetLocation()) {
            lat = String.valueOf(gpsTracker.getLatitude());
            lon = String.valueOf(gpsTracker.getLongitude());
        } else {
            lat = "0.0";
            lon = "0.0";
        }

        String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, lat, lon, mMyApp.getAndroidId());
        String extraParams = "&mode=" + "NewDelsInArea" +
                "&industryID=" + "80" +
                "&sellerID=" + 0 +
                "&misc=" + appSettings.getDriverID();
        baseUrl += extraParams;

        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }

        Log.e("DelsInArea", fullParams);

        showProgressDialog();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        //HttpsTrustManager.allowAllSSL();
        GoogleCertProvider.install(mContext);

        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();

                Log.e("DelsInArea", response);

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;

                        if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                            showToastMessage(jsonObject.getString("message"));
                        } else {
                            deliveriesOpen.clear();
                            Gson gson = new Gson();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject favDataObj = jsonArray.getJSONObject(i);
                                Log.e("JSON", favDataObj.toString());
                                try {
                                    deliveriesOpen.add(gson.fromJson(favDataObj.toString(), OpenDeliveryInfo.class));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            openDeliveriesListAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                showToastMessage("Request Error!, Please check network.");
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

    private void getDriverDelivery() {
        final Map<String, String> params = new HashMap<>();

        GpsTracker gpsTracker = new GpsTracker(parentActivity.getApplicationContext());
        String lat, lon;
        if(gpsTracker.canGetLocation()) {
            lat = String.valueOf(gpsTracker.getLatitude());
            lon = String.valueOf(gpsTracker.getLongitude());
        } else {
            lat = "0.0";
            lon = "0.0";
        }

        String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, lat, lon, mMyApp.getAndroidId());
        String extraParams = "&mode=" + "DelsByDriverID" +
                "&driverID=" + appSettings.getDriverID() +
                "&industryID=" + "80" +
                "&sellerID=" + "0" +
                "&misc=" + appSettings.getDriverID();
        baseUrl += extraParams;

        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }

        Log.e("DelsByDriverID", fullParams);

        showProgressDialog();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        //HttpsTrustManager.allowAllSSL();
        GoogleCertProvider.install(mContext);

        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();

                Log.e("DelsByDriverID", response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;

                    if (jsonObject.has("status")) {
                        showToastMessage(jsonObject.getString("msg"));
                    } else {
                        deliveriesInfoByDriver.clear();

                        Gson gson = new Gson();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject favDataObj = jsonArray.getJSONObject(i);
                            DeliveryItem newDelInfo = gson.fromJson(favDataObj.toString(), DeliveryItem.class);

                            deliveriesInfoByDriver.add(newDelInfo);
                        }
                        Log.e("mydels", String.format("There is(are) %d items of driver dels", deliveriesInfoByDriver.size()));

                        driverDeliveriesListAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                showToastMessage("Request Error!, Please check network.");
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
