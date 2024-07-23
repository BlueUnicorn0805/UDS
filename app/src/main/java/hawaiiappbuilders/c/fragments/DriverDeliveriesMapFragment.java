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

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hawaiiappbuilders.c.BaseActivity;
import hawaiiappbuilders.c.DeliveryDetailsActivity;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.adapter.ReviewListAdapter;
import hawaiiappbuilders.c.location.Constants;
import hawaiiappbuilders.c.location.GeocodeAddressIntentService;
import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.location.LocationUtil;
import hawaiiappbuilders.c.location.SharedPreferenceManager;
import hawaiiappbuilders.c.messaging.FCMHelper;
import hawaiiappbuilders.c.messaging.TokenGetter;
import hawaiiappbuilders.c.model.DeliveryItem;
import hawaiiappbuilders.c.model.FCMTokenData;
import hawaiiappbuilders.c.model.OpenDeliveryInfo;
import hawaiiappbuilders.c.model.ReviewInfo;
import hawaiiappbuilders.c.model.User;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.Helper;
import hawaiiappbuilders.c.view.circularprogressindicator.CircularProgressIndicator;
import hawaiiappbuilders.c.waydirections.DirectionObject;
import hawaiiappbuilders.c.waydirections.GsonRequest;
import hawaiiappbuilders.c.waydirections.LegsObject;
import hawaiiappbuilders.c.waydirections.PolylineObject;
import hawaiiappbuilders.c.waydirections.RouteObject;
import hawaiiappbuilders.c.waydirections.StepsObject;
import hawaiiappbuilders.c.waydirections.VolleySingleton;

public class DriverDeliveriesMapFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationUtil.LocationListener {

    public static DriverDeliveriesMapFragment newInstance(String text) {
        DriverDeliveriesMapFragment mFragment = new DriverDeliveriesMapFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);

        return mFragment;
    }

    User mCurrentUserInfo;
    boolean isFullScreen = true;

    TextView btnStatus;
    boolean userStatusIsActive = true;

    CheckBox chkSlammed;

    FrameLayout panelMap;

    BottomSheetBehavior sheetBehavior;

    EditText edtFromAddr;
    EditText edtToAddr;
    EditText edtDistance;
    EditText edtDistanceFromMe;

    Button btnDetails;
    Button btnAction;
    boolean actionIsForBid; // True: Bid Delivery, False : Complete

    private GoogleMap mMap;
    private LocationUtil mLocationUtil;

    Handler addressUpdateHandler;
    AddressResultReceiver mResultReceiver;
    double toLatitude;
    double toLongitude;
    LatLng mUserLatLng;
    String mUserAddress;

    ArrayList<OpenDeliveryInfo> deliveriesInfoInMap = new ArrayList<>();
    ArrayList<DeliveryItem> deliveriesInfoByDriver = new ArrayList<>();

    ArrayList<Marker> markersInfoInMap = new ArrayList<>();
    ArrayList<Marker> markersInfoByDriver = new ArrayList<>();

    private static final int DELIVERY_ACTION_BID = 1;
    private static final int DELIVERY_ACTION_COMPLETE = 2;
    int curDeliveryAction = 0;

    private static final int INITIAL_ZOOM_LEVEL = 8;

    TextView tabOpen;
    TextView tabPickups;
    TextView tabDeliveries;
    int currentTabIdx = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_driver_map, container, false);

        init(getArguments());

        // Init UI
        panelMap = (FrameLayout) rootView.findViewById(R.id.panelMap);

        // Make full screen at the first
        isFullScreen = true;

        // Driver Status Button
        btnStatus = (TextView) rootView.findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(this);

        chkSlammed = rootView.findViewById(R.id.chkSlammed);
        chkSlammed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateDutyAndSlammedStatus();
            }
        });

        // Delivery Informations
        LinearLayout bottom_sheet = rootView.findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);

        edtFromAddr = (EditText) rootView.findViewById(R.id.edtFromAddr);
        edtFromAddr.setKeyListener(null);
        edtFromAddr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    //Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    //        Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
                    //startActivity(intent);

                    LatLng latLng = (LatLng) view.getTag();
                    showDirection(latLng);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        edtToAddr = (EditText) rootView.findViewById(R.id.edtToAddr);
        edtToAddr.setKeyListener(null);
        edtToAddr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                addressUpdateHandler.removeMessages(0);
                addressUpdateHandler.sendEmptyMessageDelayed(0, 1500);
            }
        });

        edtToAddr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                LatLng latLng = (LatLng) view.getTag();
                showDirection(latLng);
            }
        });

        mResultReceiver = new AddressResultReceiver(null);
        addressUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == 0) {
                    String strToAddr = edtToAddr.getText().toString();
                    if (!TextUtils.isEmpty(strToAddr)) {
                        Intent intent = new Intent(mContext, GeocodeAddressIntentService.class);
                        intent.putExtra(Constants.RECEIVER, mResultReceiver);
                        intent.putExtra(Constants.FETCH_TYPE_EXTRA, Constants.USE_ADDRESS_NAME);
                        intent.putExtra(Constants.LOCATION_NAME_DATA_EXTRA, strToAddr);
                        parentActivity.startService(intent);
                    }
                }
            }
        };

        edtDistance = (EditText) rootView.findViewById(R.id.edtDistance);
        edtDistance.setKeyListener(null);

        edtDistanceFromMe = (EditText) rootView.findViewById(R.id.edtDistanceFromMe);
        edtDistanceFromMe.setKeyListener(null);

        btnDetails = (Button) rootView.findViewById(R.id.btnDetails);
        btnDetails.setOnClickListener(this);

        btnAction = (Button) rootView.findViewById(R.id.btnAction);
        btnAction.setOnClickListener(this);

        rootView.findViewById(R.id.btnScreen).setOnClickListener(this);


        tabOpen = (TextView) rootView.findViewById(R.id.tabOpen);
        tabPickups = (TextView) rootView.findViewById(R.id.tabPickups);
        tabDeliveries = (TextView) rootView.findViewById(R.id.tabDeliveries);
        tabOpen.setOnClickListener(this);
        tabOpen.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                loadData();
                return false; // Not consumed and call click listener too
            }
        });
        tabPickups.setOnClickListener(this);
        tabDeliveries.setOnClickListener(this);

        SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mLocationUtil = new LocationUtil(parentActivity, SharedPreferenceManager.getInstance());

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectTab(0);
    }

    private void selectTab(int tabIndex) {
        if (currentTabIdx == tabIndex) {
            return;
        }

        currentTabIdx = tabIndex;
        if (currentTabIdx == 0) {
            tabOpen.setSelected(true);
            tabPickups.setSelected(false);
            tabDeliveries.setSelected(false);
        } else if (currentTabIdx == 1) {
            tabOpen.setSelected(false);
            tabPickups.setSelected(true);
            tabDeliveries.setSelected(false);
        } else {
            tabOpen.setSelected(false);
            tabPickups.setSelected(false);
            tabDeliveries.setSelected(true);
        }

        showDeliveriesInMap();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationUtil != null) {
            mLocationUtil.onDestroy();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Disable the Toolbar in the map
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

            }
        });

        if (checkPermissions(mContext, PERMISSION_REQUEST_LOCATION_STRING, false, PERMISSION_REQUEST_CODE_LOCATION)) {
            try {
                //mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        mMap.setOnMarkerClickListener(this);

        mLocationUtil.fetchApproximateLocation(this);
        mLocationUtil.fetchPreciseLocation(this);

        loadData();
    }

    private void loadData() {
        btnAction.setTag(null);
        btnAction.setVisibility(View.GONE);

        // Make full screen.
        isFullScreen = true;

        getDeliveriesInArea();
        getDeliveriesByDriver();


        /*
        Row	MsgID	Msg	Internal	CreateDate
        1	8199	Complete	null	2018/11/23 10:20:09 AM
        2	8190	Driver Complete	null	2018/11/23 10:20:09 AM
        3	8180	Other Location	null	2018/11/23 11:07:03 AM
        4	8174	Neighbor accepted	null	2018/11/23 11:07:03 AM
        5	8172	At Front Door	null	2018/11/23 11:07:03 AM
        6	8170	Signed for	null	2018/11/23 11:07:03 AM
        7	8160	Picked Up	null	2019/1/10 11:59:43 AM
        8	8155	In Route to pickup package	null	2018/11/25 6:38:00 AM
        9	8150	Sender Chose Driver	null	2018/11/23 10:22:25 AM
        8199 is complete
        C
        you might what to copy and keep this for reference

        MsgID	Msg
        8199	Complete
        8190	Driver Complete
        8180	Other Location
        8174	Neighbor accepted
        8172	At Front Door
        8170	Signed for
        8160	Picked Up
        8155	In Route to pickup package
        8150	Sender Chose Driver
        */
    }

    private void getDeliveriesInArea() {

        // Remove Old Data
        if (!markersInfoInMap.isEmpty()) {
            for (Marker marker : markersInfoInMap) {
                marker.remove();
            }
            markersInfoInMap.clear();
        }
        deliveriesInfoInMap.clear();

        final Map<String, String> params = new HashMap<>();
        GpsTracker gpsTracker = new GpsTracker(parentActivity.getApplicationContext());
        String lat, lon;
        if (gpsTracker.canGetLocation()) {
            lon = String.valueOf(gpsTracker.getLongitude());
            lat = String.valueOf(gpsTracker.getLatitude());
        } else {
            lon = "";
            lat = "";
        }
        String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, lat, lon, mMyApp.getAndroidId());
        String extraParams = "&mode=" + "NewDelsInArea" +
                "&industryID=" + "80" +
                "&sellerID=" + "0" +
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
                            showToastMessage(jsonObject.getString("msg"));
                            //parentActivity.logout(false);
                        } else {
                            deliveriesInfoInMap.clear();
                            Gson gson = new Gson();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject favDataObj = jsonArray.getJSONObject(i);
                                Log.e("JSON", favDataObj.toString());
                                try {
                                    deliveriesInfoInMap.add(gson.fromJson(favDataObj.toString(), OpenDeliveryInfo.class));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            Log.e("opendels", String.format("There is(are) %d items of open dels", deliveriesInfoInMap.size()));
                        }

                        showDeliveriesInMap();

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

    private void getDeliveriesByDriver() {
        if (!markersInfoByDriver.isEmpty()) {
            for (Marker marker : markersInfoByDriver) {
                marker.remove();
            }
            markersInfoByDriver.clear();
        }
        deliveriesInfoByDriver.clear();

        final Map<String, String> params = new HashMap<>();
        GpsTracker gpsTracker = new GpsTracker(parentActivity.getApplicationContext());
        String lat, lon;
        if (gpsTracker.canGetLocation()) {
            lon = String.valueOf(gpsTracker.getLongitude());
            lat = String.valueOf(gpsTracker.getLatitude());
        } else {
            lon = "";
            lat = "";
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

                    if (jsonObject.has("status") && jsonObject.getBoolean("status") == false) {
                        showToastMessage(jsonObject.getString("msg"));
                        //parentActivity.logout(false);
                    } else {
                        deliveriesInfoByDriver.clear();

                        Gson gson = new Gson();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject favDataObj = jsonArray.getJSONObject(i);
                            DeliveryItem newDelInfo = gson.fromJson(favDataObj.toString(), DeliveryItem.class);

                            if (newDelInfo != null) {
                                deliveriesInfoByDriver.add(newDelInfo);
                            }
                        }

                        Log.e("mydels", String.format("There is(are) %d items of driver dels", deliveriesInfoByDriver.size()));
                    }

                    showDeliveriesInMap();
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

    private void updateDutyAndSlammedStatus() {
        String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLSet", BaseFunctions.MAIN_FOLDER, parentActivity.getUserLat(), parentActivity.getUserLon(), mMyApp.getAndroidId());
        // params.put("UTC", String.valueOf(0/*appSettings.getCUTC()*/));
        String extraParams = "&mode=900" +
                "&industryID=" + "80" +
                "&promoid=" + "0" +
                "&Amt=" + "0" +
                "&OrderID=" + "0" +
                "&mins=" + "0" +
                "&address=" + "" +
                "&title=" + "" +
                "&tolat=" + "0" +
                "&tolon=" + "0" +
                "&onduty=" + (userStatusIsActive ? 1 : 0) +
                "&slammed=" + (chkSlammed.isChecked() ? 1 : 0);
        baseUrl += extraParams;

        Log.e("900", baseUrl);

        //showProgressDialog();
        GoogleCertProvider.install(mContext);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //hideProgressDialog();
                Log.e("900", response);

                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                        } else if (jsonObject.has("status") && jsonObject.getBoolean("status")) {
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showToastMessage("Request Error!, Please check network.");
                hideProgressDialog();
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setShouldCache(false);
        hawaiiappbuilders.c.webutils.VolleySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    synchronized private void showDeliveriesInMap() {
        if (mMap == null)
            return;

        hideDetailLayout();

        // Start Find the Waypoint
        if (mPolyline != null) {
            mPolyline.remove();
            mPolyline = null;
        }

        // Remove markers
        if (!markersInfoInMap.isEmpty()) {
            for (Marker marker : markersInfoInMap) {
                marker.remove();
            }
            markersInfoInMap.clear();
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        int boundsCounter = 0;

        if (currentTabIdx == 0) {
            // Show Open Deliveries
            if (deliveriesInfoInMap != null && deliveriesInfoInMap.size() > 0) {
                for (OpenDeliveryInfo delInfo : deliveriesInfoInMap) {
                    //LatLng latLng = new LatLng(delInfo.getToLat(), delInfo.getToLon());
                    LatLng latLng = new LatLng(delInfo.getLat(), delInfo.getLon());
                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                            .title(delInfo.getfAdd()/*String.format("%s to %s", delInfo.getfAdd(), delInfo.gettAdd())*/)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(delInfo.getStatusID() == 8077 ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_ORANGE)));
                    marker.setTag(delInfo);

                    markersInfoInMap.add(marker);

                    boundsBuilder.include(latLng);
                    boundsCounter++;
                }
            }
        } else if (currentTabIdx == 1) {
            // Show My Deliveries
            if (deliveriesInfoByDriver != null) {
                for (DeliveryItem delInfo : deliveriesInfoByDriver) {
                    if (delInfo.getToLat() != 0 && delInfo.getToLat() != 0) {
                        LatLng latLng = new LatLng(delInfo.getLat(), delInfo.getLon());
                        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                                .title(String.format("%s", delInfo.getfAdd()))
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        marker.setTag(delInfo);

                        markersInfoByDriver.add(marker);

                        boundsBuilder.include(latLng);
                        boundsCounter++;
                    }
                }
            }
        }

        try {
            // Show Bounds
            /*if (boundsCounter > 0) {
                LatLngBounds latLngBounds = boundsBuilder.build();
                if (boundsCounter == 1) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 14.0f));
                } else {
                    int routePadding = 200;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
                }
            }*/
            if (false && currentTabIdx == 0 && deliveriesInfoInMap.size() > 0) {
                LatLng latLng = new LatLng(deliveriesInfoInMap.get(0).getLat(), deliveriesInfoInMap.get(0).getLon());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));
            } else if (false && currentTabIdx == 1 && deliveriesInfoByDriver.size() > 0) {
                LatLng latLng = new LatLng(deliveriesInfoByDriver.get(0).getLat(), deliveriesInfoByDriver.get(0).getLon());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));
            } else {
                LatLng latLng = new LatLng(39.742043, -104.991531);
                if (parentActivity.getLocation()) {
                    double lat = Double.parseDouble(parentActivity.getUserLat());
                    double lon = Double.parseDouble(parentActivity.getUserLon());
                    latLng = new LatLng(lat, lon);
                }

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.0f));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnScreen) {
            if (viewId == R.id.btnScreen) {
                if (!isFullScreen) {
                    hideDetailLayout();
                } else {
                    showDetailLayout();
                }
            }
        } else if (viewId == R.id.btnAction) {
            if (actionIsForBid) {
                OpenDeliveryInfo deliveryInfo = (OpenDeliveryInfo) btnAction.getTag();
                if (deliveryInfo.getStatusID() == 8077) {
                    acceptBid(deliveryInfo);
                } else {
                    confirmBidOnDelivery(deliveryInfo);
                }
            } else {
                DeliveryItem deliveryInfo = (DeliveryItem) btnAction.getTag();

                if (deliveryInfo.isPickuped()) {
                    completeAndRateDelivery(deliveryInfo);
                } else {
                    markAsPickupDelivery(deliveryInfo);
                }
            }
        } else if (viewId == R.id.btnStatus) {
            if (userStatusIsActive) {
                userStatusIsActive = false;
                btnStatus.setText("Off Duty");
                btnStatus.setBackgroundResource(R.color.user_status_off);

                // Remove path
                if (mPolyline != null) {
                    mPolyline.remove();
                    mPolyline = null;
                }

                // Remove all Markers
                if (!markersInfoInMap.isEmpty()) {
                    for (Marker marker : markersInfoInMap) {
                        marker.remove();
                    }
                    markersInfoInMap.clear();
                }

                if (!markersInfoByDriver.isEmpty()) {
                    for (Marker marker : markersInfoByDriver) {
                        marker.remove();
                    }
                    markersInfoByDriver.clear();
                }
            } else {
                userStatusIsActive = true;
                btnStatus.setText("On Duty");
                btnStatus.setBackgroundResource(R.color.user_status_on);

                loadData();
            }

            updateDutyAndSlammedStatus();
        } else if (viewId == R.id.tabOpen) {
            selectTab(0);
        } else if (viewId == R.id.tabPickups) {
            selectTab(1);
        } else if (viewId == R.id.tabDeliveries) {
            selectTab(2);
        } else if (viewId == R.id.btnDetails) {
            //startActivity(new Intent(mContext, ActivityDelsDetails.class));
            showDeliveryDetails(btnDetails.getTag());
        }
    }

    public void awakeOnDuty() {
        if (!userStatusIsActive) {
            userStatusIsActive = true;
            btnStatus.setText("On Duty");
            btnStatus.setBackgroundResource(R.color.user_status_on);
            loadData();
        } else {
            loadData();
        }
    }

    private void showDetailLayout() {
        /*ViewGroup.LayoutParams scrollContentsParam = panelScrollContents.getLayoutParams();
        scrollContentsParam.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        panelScrollContents.setLayoutParams(scrollContentsParam);*/

        ViewGroup.LayoutParams mapContentsParam = panelMap.getLayoutParams();
        mapContentsParam.height = dpToPx(mContext, 300);
        panelMap.setLayoutParams(mapContentsParam);

        isFullScreen = false;
    }

    private void hideDetailLayout() {
        /*ViewGroup.LayoutParams scrollContentsParam = panelScrollContents.getLayoutParams();
        scrollContentsParam.height = LinearLayout.LayoutParams.MATCH_PARENT;
        panelScrollContents.setLayoutParams(scrollContentsParam);*/

        /*ViewGroup.LayoutParams mapContentsParam = panelMap.getLayoutParams();
        mapContentsParam.height = LinearLayout.LayoutParams.MATCH_PARENT;
        panelMap.setLayoutParams(mapContentsParam);*/

        isFullScreen = true;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != currentPositionMarker) {
            currentActiveMarker = marker;
        }

        GpsTracker gpsTracker = new GpsTracker(mContext);
        LatLng curLatLng = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());

        // Show the delivery info
        isFullScreen = false;

        Object markerObject = marker.getTag();
        if (markerObject instanceof OpenDeliveryInfo) {
            final OpenDeliveryInfo deliveryInfo = (OpenDeliveryInfo) markerObject;

            if (deliveryInfo.getStatusID() == 8077) {
                acceptBid(deliveryInfo);
                return false;
            }

            btnDetails.setTag(deliveryInfo);

            edtFromAddr.setText(deliveryInfo.getfAdd() + " " + deliveryInfo.getfCSZ());
            edtToAddr.setText(deliveryInfo.gettAdd() + " " + deliveryInfo.gettCSZ());

            LatLng fromLatLng = new LatLng(deliveryInfo.getLat(), deliveryInfo.getLon());
            LatLng toLatLng = new LatLng(deliveryInfo.getToLat(), deliveryInfo.getToLon());

            edtFromAddr.setTag(fromLatLng);
            edtToAddr.setTag(toLatLng);

            edtDistance.setText(String.format("%.2f Mile(s)", getDistanceMiles(fromLatLng, toLatLng)));
            edtDistanceFromMe.setText(String.format("%.2f Mile(s)", getDistanceMiles(curLatLng, fromLatLng)));

            btnAction.setVisibility(View.VISIBLE);
            btnAction.setTag(deliveryInfo);

            btnAction.setText(/*deliveryInfo.getStatusID() == 8077 ? "Accept" : */"Bid on Delivery");
            actionIsForBid = true;

            // Start Find the Waypoint
            if (mPolyline != null) {
                mPolyline.remove();
                mPolyline = null;
            }

            String directionApiPath = Helper.getUrl(String.valueOf(fromLatLng.latitude), String.valueOf(fromLatLng.longitude),
                    String.valueOf(toLatLng.latitude), String.valueOf(toLatLng.longitude));
            Log.d("waypoint", "Path " + directionApiPath);
            getDirectionFromDirectionApiServer(directionApiPath);

            /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
            alertDialogBuilder.setTitle("Confirm bid");
            alertDialogBuilder.setMessage("Would you like to bid this delivery?")
                    .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    inputBidInfo(deliveryInfo);
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            Window window = alertDialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.windowAnimations = R.style.DialogAnimation;
            window.setAttributes(wlp);
            alertDialog.show();*/

            // We call this using button action
            // confirmBidOnDelivery(deliveryInfo);

            // Show Delivery Details
            // showDeliveryDetails(markerObject);
        } else if (markerObject instanceof DeliveryItem) {
            DeliveryItem deliveryInfo = (DeliveryItem) markerObject;

            btnDetails.setTag(deliveryInfo);

            // edtFromAddr.setText(deliveryInfo.getFromAddress());
            // edtToAddr.setText(deliveryInfo.getDelToAdd());

            edtFromAddr.setText(deliveryInfo.getfAdd() + " " + deliveryInfo.getfCSZ());
            edtToAddr.setText(deliveryInfo.gettAdd() + " " + deliveryInfo.gettCSZ());

            LatLng fromLatLng = new LatLng(deliveryInfo.getLat(), deliveryInfo.getLon());
            LatLng toLatLng = new LatLng(deliveryInfo.getToLat(), deliveryInfo.getToLon());

            edtFromAddr.setTag(fromLatLng);
            edtToAddr.setTag(toLatLng);

            edtDistance.setText(String.format("%.2f Mile(s)", getDistanceMiles(fromLatLng, toLatLng)));
            edtDistanceFromMe.setText(String.format("%.2f Mile(s)", getDistanceMiles(curLatLng, fromLatLng)));

            btnAction.setVisibility(View.VISIBLE);
            btnAction.setTag(deliveryInfo);

            if (deliveryInfo.isPickuped()) {
                btnAction.setText("Complete Delivery");
            } else {
                btnAction.setText("Mark as Picked Up");
            }
            actionIsForBid = false;

            /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
            alertDialogBuilder.setTitle("Confirm delivery");
            alertDialogBuilder.setMessage("Is Delivery Complete?")
                    .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            Window window = alertDialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.windowAnimations = R.style.DialogAnimation;
            window.setAttributes(wlp);
            alertDialog.show();*/

            // We call this function using the button action
            // completeAndRateDelivery(deliveryInfo);

            // Show Delivery Details
            // showDeliveryDetails(markerObject);
        }

        // Open Behavior
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            // Close Behavior
            //sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        return false;
    }

    private void showDeliveryDetails(Object deliveryDetails) {

        Intent intent = new Intent(mContext, DeliveryDetailsActivity.class);
        intent.putExtra("delivery_info", (Parcelable) deliveryDetails);
        startActivity(intent);
    }

    private void confirmBidOnDelivery(final OpenDeliveryInfo deliveryInfo) {
        if (deliveryInfo == null)
            return;

        final Map<String, String> params = new HashMap<>();

        GpsTracker gpsTracker = new GpsTracker(parentActivity.getApplicationContext());
        String lat, lon;
        if (gpsTracker.canGetLocation()) {
            lon = String.valueOf(gpsTracker.getLongitude());
            lat = String.valueOf(gpsTracker.getLatitude());
        } else {
            lon = "";
            lat = "";
        }
        String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGetRating", BaseFunctions.MAIN_FOLDER, lat, lon, mMyApp.getAndroidId());
        String extraParams = "&delID=" + deliveryInfo.getDelID() +
                "&RatingOfMLID=" + deliveryInfo.getMLID();
        baseUrl += extraParams;
        // params.put("driverID", appSettings.getDriverID());

        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }

        Log.e("CJLGetRating", fullParams);

        showProgressDialog();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        //HttpsTrustManager.allowAllSSL();
        GoogleCertProvider.install(mContext);

        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();

                Log.e("CJLGetRating", response);

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;

                        int completed = jsonObject.getInt("completed");
                        int onTime = jsonObject.getInt("onTime");

                        int repeat = 0;
                        if(jsonObject.has("repeat")) {
                            repeat = jsonObject.getInt("repeat");
                        }
                        float stars = (float) jsonObject.getDouble("stars");
                        String fn = jsonObject.getString("FN");

                        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sender_profile, null);

                        final android.app.AlertDialog inputDlg = new android.app.AlertDialog.Builder(mContext, R.style.AlertDialogTheme)
                                .setView(dialogView)
                                .setCancelable(false)
                                .create();

                        CircularProgressIndicator progressCompleted = (CircularProgressIndicator) dialogView.findViewById(R.id.progressCompleted);
                        CircularProgressIndicator progressOnTime = (CircularProgressIndicator) dialogView.findViewById(R.id.progressOnTime);
                        CircularProgressIndicator progressRepeat = (CircularProgressIndicator) dialogView.findViewById(R.id.progressRepeat);

                        progressCompleted.setProgress(completed, 100);
                        progressOnTime.setProgress(onTime, 100);
                        progressRepeat.setProgress(repeat, 100);

                        ReviewListAdapter reviewListAdapter;
                        ArrayList<ReviewInfo> reviewInfos = new ArrayList<>();
                        ListView lvProfiles = (ListView) dialogView.findViewById(R.id.lvProfileDate);

                        // reviewInfos.add(new ReviewInfo("1", "Xian G", 5.0f, "\"Great Client, Want to be hired again.\"", "", "2018-11-20"));
                        // reviewInfos.add(new ReviewInfo("2", "Xiao M", 5.0f, "\"Very good client. I want to server for him again.\"", "", "2018-11-21"));
                        reviewInfos.add(new ReviewInfo("4", fn, stars, "\"Very good person. Always deliveried package in time and provided great service.\"", "", "2018-11-20"));

                        reviewListAdapter = new ReviewListAdapter(mContext, reviewInfos);
                        lvProfiles.setAdapter(reviewListAdapter);

                        dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                inputDlg.dismiss();
                            }
                        });
                        dialogView.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                inputDlg.dismiss();
                                inputBidInfo(deliveryInfo);
                            }
                        });

                        inputDlg.show();
                        inputDlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
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

    private void completeAndRateDelivery(final DeliveryItem deliveryInfo) {

        if (deliveryInfo == null)
            return;

        final JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("userid", appSettings.getUserId());
            jsonObject.put("delid", deliveryInfo.getDelID());

            jsonObject.put("statusid", "8170");
            jsonObject.put("rate", "0");
            jsonObject.put("q1", "1");
            jsonObject.put("q2", "1");

            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating_from_driver, null);

            final android.app.AlertDialog inputDlg = new android.app.AlertDialog.Builder(mContext, R.style.AlertDialogTheme)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            // Questions
            final RadioButton radioSignedFor = (RadioButton) dialogView.findViewById(R.id.radioSignedFor);
            final RadioButton radioAtFrontDoor = (RadioButton) dialogView.findViewById(R.id.radioAtFrontDoor);
            final RadioButton radioNeighborAccepted = (RadioButton) dialogView.findViewById(R.id.radioNeighborAccepted);
            final RadioButton radioOther = (RadioButton) dialogView.findViewById(R.id.radioOther);

            RadioGroup groupOnTime = (RadioGroup) dialogView.findViewById(R.id.groupOnTime);
            RadioGroup groupComplete = (RadioGroup) dialogView.findViewById(R.id.groupComplete);

            View.OnClickListener statusButtonClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int viewID = view.getId();
                    try {
                        if (viewID == R.id.radioSignedFor && radioSignedFor.isChecked()) {
                            jsonObject.put("statusid", "8170");

                            radioSignedFor.setChecked(true);
                            radioAtFrontDoor.setChecked(false);
                            radioNeighborAccepted.setChecked(false);
                            radioOther.setChecked(false);
                        } else if (viewID == R.id.radioAtFrontDoor) {
                            jsonObject.put("statusid", "8172");

                            radioSignedFor.setChecked(false);
                            radioAtFrontDoor.setChecked(true);
                            radioNeighborAccepted.setChecked(false);
                            radioOther.setChecked(false);
                        } else if (viewID == R.id.radioNeighborAccepted) {
                            jsonObject.put("statusid", "8174");

                            radioSignedFor.setChecked(false);
                            radioAtFrontDoor.setChecked(false);
                            radioNeighborAccepted.setChecked(true);
                            radioOther.setChecked(false);
                        } else if (viewID == R.id.radioOther) {
                            jsonObject.put("statusid", "8180");

                            radioSignedFor.setChecked(false);
                            radioAtFrontDoor.setChecked(false);
                            radioNeighborAccepted.setChecked(false);
                            radioOther.setChecked(true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            radioSignedFor.setOnClickListener(statusButtonClickListener);
            radioAtFrontDoor.setOnClickListener(statusButtonClickListener);
            radioNeighborAccepted.setOnClickListener(statusButtonClickListener);
            radioOther.setOnClickListener(statusButtonClickListener);

            groupOnTime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    try {
                        if (i == R.id.btnOnTimeYes) {
                            jsonObject.put("q1", "1");
                        } else if (i == R.id.btnOnTimeNo) {
                            jsonObject.put("q1", "0");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            groupComplete.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    try {
                        if (i == R.id.btnCompleteYes) {
                            jsonObject.put("q2", "1");
                        } else if (i == R.id.btnCompleteNo) {
                            jsonObject.put("q2", "0");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Ratings
            final RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
            final TextView tvErrorRate = (TextView) dialogView.findViewById(R.id.tvErrorRate);
            tvErrorRate.setVisibility(View.GONE);

            // Button Actions
            dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    inputDlg.dismiss();
                }
            });
            dialogView.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    // Check current rating
                    if (ratingBar.getRating() == 0) {
                        tvErrorRate.setVisibility(View.VISIBLE);
                        return;
                    }

                    try {
                        jsonObject.put("rate", ratingBar.getRating());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    inputDlg.dismiss();

                    // Request Rating Api
                    showProgressDialog();
                    RequestQueue queue = Volley.newRequestQueue(mContext);
                 /*   StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelStatusRate(jsonObject), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            hideProgressDialog();

                            if (response != null || !response.isEmpty()) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String status = jsonObject.getString("status");
                                    if (jsonObject.getBoolean("status")) {
                                        showAlert("Completed delivery! Good job.", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                notifySenderCompleteStatus(deliveryInfo);
                                                loadData();
                                            }
                                        });
                                    } else {
                                        showToastMessage(jsonObject.getString("message"));
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
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/x-www-form-urlencoded");
                            params.put("Accept", "application/json");
                            return params;
                        }
                    };
                    queue.add(sr);*/
                }
            });

            inputDlg.show();
            inputDlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void markAsPickupDelivery(final DeliveryItem deliveryInfo) {

        if (deliveryInfo == null)
            return;

        final JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("userid", appSettings.getUserId());
            jsonObject.put("delid", deliveryInfo.getDelID());

            jsonObject.put("statusid", "8160");

            // Request Rating Api
            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);
          /*  StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelStatusUpdate(jsonObject), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (jsonObject.getBoolean("status")) {
                                deliveryInfo.setPickuped(true);
                                btnAction.setTag(deliveryInfo);
                                btnAction.setText("Complete Delivery");
                            } else {
                                showToastMessage(jsonObject.getString("message"));
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
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    params.put("Accept", "application/json");
                    return params;
                }
            };
            queue.add(sr);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifySenderCompleteStatus(final DeliveryItem deliveryInfo) {

        final Map<String, String> params = new HashMap<>();

        GpsTracker gpsTracker = new GpsTracker(parentActivity.getApplicationContext());
        String lat, lon;
        if (gpsTracker.canGetLocation()) {
            lon = String.valueOf(gpsTracker.getLongitude());
            lat = String.valueOf(gpsTracker.getLatitude());
        } else if (mMyApp.curLocation != null) {
            lon = String.valueOf(mMyApp.curLocation.getLongitude());
            lat = String.valueOf(mMyApp.curLocation.getLatitude());
        } else {
            lon = "";
            lat = "";
        }
        String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGetToken", BaseFunctions.MAIN_FOLDER, lat, lon, mMyApp.getAndroidId());
        String extraParams = "&mode=getLastLoggedin" +
                "&TokenMLID=" + deliveryInfo.getDriverID();
        baseUrl += extraParams;
        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }
        Log.e("CJLGetToken", fullParams);

        showProgressDialog();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        //HttpsTrustManager.allowAllSSL();
        GoogleCertProvider.install(mContext);

        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e("CJLGetToken", params.toString());

                hideProgressDialog();
                if (response != null || !response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (jsonObject.getBoolean("status")) {

                            JSONArray dataArray = jsonObject.getJSONArray("data");

                            String curUserName = String.format("%s %s", appSettings.getFN(), appSettings.getLN()).trim();
                            String title = String.format("%s completed your delivery", curUserName);
                            String addressInfo = String.format("To %s, From %s", deliveryInfo.gettAdd(), deliveryInfo.getfAdd());

                            TokenGetter tokenGetter = new TokenGetter(appSettings.getUserId(), mContext, (BaseActivity) mContext);
                            ArrayList<FCMTokenData> tokenList = tokenGetter.getTokenList(dataArray);
                            if (!tokenList.isEmpty()) {
                                JSONObject payload = new JSONObject();
                                payload.put("delID", deliveryInfo.getDelID());
                                payload.put("driverName", curUserName);
                                payload.put("driverID", appSettings.getDriverID());
                                payload.put("userID", appSettings.getUserId());
                                payload.put("title", title);
                                payload.put("message", addressInfo);
                                tokenGetter.sendPushNotification(mContext, tokenList, FCMHelper.PT_Funds_Sent_Transactions, payload);
                            } else {
                                showAlert("Sender has no any device to contact.");
                            }
                        } else {
                            showToastMessage(jsonObject.getString("message"));
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

    private void getDirectionFromDirectionApiServer(String url) {
        GsonRequest<DirectionObject> serverRequest = new GsonRequest<DirectionObject>(
                Request.Method.GET,
                url,
                DirectionObject.class,
                createRequestSuccessListener(),
                createRequestErrorListener());
        serverRequest.setRetryPolicy(new DefaultRetryPolicy(
                Helper.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(parentActivity.getApplicationContext()).addToRequestQueue(serverRequest);
    }

    private Response.Listener<DirectionObject> createRequestSuccessListener() {
        return new Response.Listener<DirectionObject>() {
            @Override
            public void onResponse(DirectionObject response) {
                try {
                    Log.d("JSON Response", response.toString());
                    if (response.getStatus().equals("OK")) {
                        List<LatLng> mDirections = getDirectionPolylines(response.getRoutes());
                        drawRouteOnMap(mMap, mDirections);
                    } else {
                        //showToastMessage("Couldn't find the Waypoints!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ;
        };
    }

    private List<LatLng> getDirectionPolylines(List<RouteObject> routes) {
        List<LatLng> directionList = new ArrayList<LatLng>();
        for (RouteObject route : routes) {
            List<LegsObject> legs = route.getLegs();
            for (LegsObject leg : legs) {
                List<StepsObject> steps = leg.getSteps();
                for (StepsObject step : steps) {
                    PolylineObject polyline = step.getPolyline();
                    String points = polyline.getPoints();
                    List<LatLng> singlePolyline = decodePoly(points);
                    for (LatLng direction : singlePolyline) {
                        directionList.add(direction);
                    }
                }
            }
        }
        return directionList;
    }

    private Response.ErrorListener createRequestErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
    }

    Polyline mPolyline;

    private void drawRouteOnMap(GoogleMap map, List<LatLng> positions) {
        PolylineOptions options = new PolylineOptions().width(8).color(Color.BLUE).geodesic(true);
        options.addAll(positions);

        // Remove old polyline
        if (mPolyline != null) {
            mPolyline.remove();
            mPolyline = null;
        }

        mPolyline = map.addPolyline(options);

        // Wrap the Waypoints
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng point : positions) {
            boundsBuilder.include(point);
        }

        int routePadding = 200;
        LatLngBounds latLngBounds = boundsBuilder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void inputBidInfo(final OpenDeliveryInfo deliveryInfo) {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_input_bid_info, null);

        final android.app.AlertDialog inputDlg = new android.app.AlertDialog.Builder(mContext, R.style.AlertDialogTheme)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        final EditText edtPrice = (EditText) dialogView.findViewById(R.id.edtPrice);
        final EditText edtETA = (EditText) dialogView.findViewById(R.id.edtETA);
        dialogView.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String price = edtPrice.getText().toString().trim();
                final String eta = edtETA.getText().toString().trim();

                /*InputMethodManager imm = (InputMethodManager)edtPrice.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                imm = (InputMethodManager)edtETA.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);*/

                hideKeyboard(edtPrice);
                hideKeyboard(edtETA);

                if (TextUtils.isEmpty(price)) {
                    edtPrice.setError("Invalid input");
                    edtPrice.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(eta)) {
                    edtETA.setError("Invalid input");
                    edtETA.requestFocus();
                    return;
                }

                inputDlg.dismiss();

                final Map<String, String> params = new HashMap<>();

                GpsTracker gpsTracker = new GpsTracker(parentActivity.getApplicationContext());
                String lat, lon;
                if (gpsTracker.canGetLocation()) {
                    lon = String.valueOf(gpsTracker.getLongitude());
                    lat = String.valueOf(gpsTracker.getLatitude());
                } else {
                    lon = "";
                    lat = "";
                }
                String baseUrl = BaseFunctions.getBaseUrl(mContext, "DelBidAdd", BaseFunctions.MAIN_FOLDER, lat, lon, mMyApp.getAndroidId());
                String extraParams = "&driverID=" + appSettings.getDriverID() +
                        "&delID=" + deliveryInfo.getDelID() +
                        "&ETAmins=" + eta +
                        "&bidAmt=" + price;
                baseUrl += extraParams;
                String fullParams = "";
                for (String key : params.keySet()) {
                    fullParams += String.format("&%s=%s", key, params.get(key));
                }

                Log.e("DelBidAdd", fullParams);

                showProgressDialog();
                RequestQueue queue = Volley.newRequestQueue(mContext);

                //HttpsTrustManager.allowAllSSL();
                GoogleCertProvider.install(mContext);

                StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();

                        Log.e("DelBidAdd", response);

                        if (response != null || !response.isEmpty()) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;

                                if (jsonObject.getBoolean("status")) {

                                    showToastMessage("Bid has been offered");

                                    notifySenderBidStatus(deliveryInfo);
                                    loadData();
                                } else {
                                    showToastMessage(jsonObject.getString("msg"));
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
        });
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                inputDlg.dismiss();
            }
        });

        inputDlg.show();
        inputDlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void notifySenderBidStatus(final OpenDeliveryInfo deliveryInfo) {

        final Map<String, String> params = new HashMap<>();

        GpsTracker gpsTracker = new GpsTracker(parentActivity.getApplicationContext());
        String lat, lon;
        if (gpsTracker.canGetLocation()) {
            lon = String.valueOf(gpsTracker.getLongitude());
            lat = String.valueOf(gpsTracker.getLatitude());
        } else if (mMyApp.curLocation != null) {
            lon = String.valueOf(mMyApp.curLocation.getLongitude());
            lat = String.valueOf(mMyApp.curLocation.getLatitude());
        } else {
            lon = "";
            lat = "";
        }
        String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGetToken", BaseFunctions.MAIN_FOLDER, lat, lon, mMyApp.getAndroidId());
        String extraParams = "&mode=getLastLoggedin" +
                "&TokenMLID=" + deliveryInfo.getMLID();
        baseUrl += extraParams;
        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }
        Log.e("CJLGetToken", fullParams);

        showProgressDialog();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        //HttpsTrustManager.allowAllSSL();
        GoogleCertProvider.install(mContext);

        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e("CJLGetToken", response);

                hideProgressDialog();
                try {
                    JSONArray dataArray = new JSONArray(response);
                    String curUserName = String.format("%s %s", appSettings.getFN(), appSettings.getLN()).trim();
                    String title = String.format("%s just bid your delivery", curUserName);
                    String addressInfo = String.format("To %s, From %s", deliveryInfo.gettAdd(), deliveryInfo.getfAdd());

                    JSONObject jAdditionalData = new JSONObject();
                    jAdditionalData.put("delID", deliveryInfo.getDelID());
                    jAdditionalData.put("driverName", curUserName);
                    jAdditionalData.put("driverID", appSettings.getDriverID());
                    jAdditionalData.put("userID", appSettings.getUserId());

                    TokenGetter tokenGetter = new TokenGetter(appSettings.getUserId(), mContext, (BaseActivity) mContext);
                    ArrayList<FCMTokenData> tokenList = tokenGetter.getTokenList(dataArray);
                    if (!tokenList.isEmpty()) {
                        JSONObject payload = new JSONObject();
                        payload.put("delID", deliveryInfo.getDelID());
                        payload.put("driverName", curUserName);
                        payload.put("driverID", appSettings.getDriverID());
                        payload.put("userID", appSettings.getUserId());
                        payload.put("title", title);
                        payload.put("message", addressInfo);
                        tokenGetter.sendPushNotification(mContext, tokenList, FCMHelper.PT_Share_Location, payload);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();

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

    private void acceptBid(final OpenDeliveryInfo deliveryInfo) {

        // Check current delivery information
        /*if (currentDeliveryItem == null) {
            return;
        }*/

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        alertDialogBuilder.setTitle("Confirm accept");
        alertDialogBuilder.setMessage("Do you want to accept this delivery?")
                .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                        final Map<String, String> params = new HashMap<>();

                        String baseUrl = BaseFunctions.getBaseUrl(mContext, "BidAccept", BaseFunctions.MAIN_FOLDER, String.valueOf(mUserLatLng.latitude), String.valueOf(mUserLatLng.longitude), mMyApp.getAndroidId());
                        String extraParams = "&DelID=" + deliveryInfo.getDelID() +
                                "&DriverID=" + appSettings.getDriverID() +
                                "&acceptTime=" + DateUtil.toStringFormat_7(new Date());
                        baseUrl += extraParams;

                        String fullParams = "";
                        for (String key : params.keySet()) {
                            fullParams += String.format("&%s=%s", key, params.get(key));
                        }
                        Log.e("BidAccept", fullParams);

                        showProgressDialog();

                        RequestQueue queue = Volley.newRequestQueue(mContext);
                        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.e("BidAccept", response);

                                hideProgressDialog();

                                if (response != null || !response.isEmpty()) {
                                    try {
                                        JSONArray jsonArray = new JSONArray(response);
                                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                                        if (jsonObject.optBoolean("status")) {
                                            //sendDeliveryRequestMessage(bidsInfo);
                                            JSONArray dataArray = jsonObject.getJSONArray("token");
                                            // Xiao UPDel
                                            // tokenList.add("dAyya7gPd5o:APA91bHoizSGBl0PNm3DpIbgBmGH14r81-FmVup5xMCTYJ1xw_4PePS88U8SCUdwtW9E62EanKzWNt3tp5ylW1Lb5FdW7G2N5NLgaQ4FLpj5aEFDXrVKpBrZ7xN-oMY63oFHMHMfGAeo");
                                            String title = appSettings.getFN() + " " + appSettings.getLN();
                                            TokenGetter tokenGetter = new TokenGetter(appSettings.getUserId(), mContext, (BaseActivity) mContext);
                                            ArrayList<FCMTokenData> tokenList = tokenGetter.getTokenList(dataArray);
                                            if (!tokenList.isEmpty()) {
                                                JSONObject payload = new JSONObject();
                                                payload.put("DELID", deliveryInfo.getDelID());
                                                payload.put("SenderCP", appSettings.getCP());
                                                payload.put("SenderID", appSettings.getUserId());
                                                payload.put("title", title);
                                                payload.put("message", "Your bid was accepted");
                                                tokenGetter.sendPushNotification(mContext, tokenList, FCMHelper.PT_Text_Message, payload);
                                            }
                                            selectTab(1);
                                            loadData();

                                        } else {
                                            showToastMessage(jsonObject.getString("message"));
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
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() {
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("Content-Type", "application/x-www-form-urlencoded");
                                params.put("Accept", "application/json");
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
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.windowAnimations = R.style.DialogAnimation;
        window.setAttributes(wlp);
        alertDialog.show();
    }

    Marker currentPositionMarker;
    Marker currentActiveMarker;
    private ArrayList<Marker> bidderMarkers = new ArrayList<>();

    @Override
    public void onLocationReceived(@NonNull LatLng location, @NonNull String addressString, Address address) {
        mUserLatLng = new LatLng(location.latitude, location.longitude);
        mUserAddress = addressString;

        // Remove Old marker
        if (currentPositionMarker != null) {
            currentPositionMarker.remove();
        }

        currentPositionMarker = mMap.addMarker(new MarkerOptions().position(mUserLatLng)
                .title(getString(R.string.you_are_here))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_location_pin)));

        if (deliveriesInfoInMap.isEmpty() && deliveriesInfoByDriver.isEmpty()) {
            updateCamera(location);
        }
    }

    public void updateCamera(@Nullable LatLng latLng) {
        if (latLng == null) {
            return;
        }
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL);
        mMap.animateCamera(location, 1000, null);
    }

    public void removeOldMarkers(ArrayList<Marker> markers) {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == Constants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(Constants.RESULT_ADDRESS);
                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toLatitude = address.getLatitude();
                        toLongitude = address.getLongitude();
                        //updateDistance();
                    }
                });
            }
        }
    }

    private void updateDistance() {
        // Check Target Location
        if (toLatitude == 0 && toLatitude == 0) {
            return;
        }

        // Check Start Location
        if (mUserLatLng == null) {
            return;
        }

        final double MILES_PER_KILO = 0.621371;

        double distance = calculationByDistance(mUserLatLng.latitude, mUserLatLng.longitude, toLatitude, toLongitude);
        edtDistance.setText(String.format("%.1f Mile", distance * MILES_PER_KILO));
    }

    private double getDistanceMiles(LatLng fro, LatLng to) {
        final double MILES_PER_KILO = 0.621371;

        double distance = calculationByDistance(fro.latitude, fro.longitude, to.latitude, to.longitude);
        return distance * MILES_PER_KILO;
    }

    // Haversine Distance Calulator
    public double calculationByDistance(double initialLat, double initialLong,
                                        double finalLat, double finalLong) {
        int R = 6371; // km (Earth radius)
        double dLat = toRadians(finalLat - initialLat);
        double dLon = toRadians(finalLong - initialLong);
        initialLat = toRadians(initialLat);
        finalLat = toRadians(finalLat);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(initialLat) * Math.cos(finalLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public double toRadians(double deg) {
        return deg * (Math.PI / 180);
    }

    private void showDirection(LatLng latLng) {
        if (latLng == null)
            return;

        String urlString = String.format("http://maps.google.com/maps?daddr=%f,%f", latLng.latitude, latLng.longitude);
        Log.e("direction", urlString);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(urlString));
        if (isPackageExisted("com.google.android.apps.maps")) {
            intent.setPackage("com.google.android.apps.maps");
        }
        startActivity(intent);
    }

    private boolean isPackageExisted(String targetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = parentActivity.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationUtil.onPermissionResult(requestCode, permissions, grantResults);

        // Check All Permission was granted
        boolean bAllGranted = true;
        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                bAllGranted = false;
                break;
            }
        }

        if (bAllGranted && requestCode == PERMISSION_REQUEST_CODE_LOCATION) {
            // Enable Current location pick
            try {
                //mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(R.string.request_permission_hint);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
