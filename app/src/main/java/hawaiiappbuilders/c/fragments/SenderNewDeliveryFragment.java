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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import hawaiiappbuilders.c.MainActivity;
import hawaiiappbuilders.c.NewDeliveryActivity;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.adapter.BidsListAdapter;
import hawaiiappbuilders.c.location.Constants;
import hawaiiappbuilders.c.location.GeocodeAddressIntentService;
import hawaiiappbuilders.c.location.LocationUtil;
import hawaiiappbuilders.c.location.SharedPreferenceManager;
import hawaiiappbuilders.c.model.BidsInfoOriginal;
import hawaiiappbuilders.c.model.DeliveryItem;
import hawaiiappbuilders.c.model.FavDeliveryUser;
import hawaiiappbuilders.c.model.User;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static hawaiiappbuilders.c.MainActivity.FRAGMENT_SENDER_DELIVERY_MAP;

public class SenderNewDeliveryFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationUtil.LocationListener {

    public static SenderNewDeliveryFragment newInstance(String text) {
        SenderNewDeliveryFragment mFragment = new SenderNewDeliveryFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);

        return mFragment;
    }

    User mCurrentUserInfo;

    boolean isFullScreen;
    FrameLayout panelMap;
    ScrollView scrollView;

    EditText edtFromAddr;
    EditText edtToAddr;

    Button btnSendRequest;

    Spinner spinnerFavs;
    ArrayList<FavDeliveryUser> favsInfoArray = new ArrayList<>();
    ArrayAdapter<FavDeliveryUser> spinnerArrayAdapter;

    View panelBidContents;
    ListView lvDataList;
    ArrayList<BidsInfoOriginal> bidsInfoArray = new ArrayList<>();
    BidsListAdapter bidsInfoAdapter;
    String newDeliveryId;

    Handler mHandlerUpdateBidList;
    Runnable mRunnableUpdateBidList;

    private GoogleMap mMap;
    private LocationUtil mLocationUtil;

    Handler addressUpdateHandler;
    AddressResultReceiver mResultReceiver;
    double toLatitude;
    double toLongitude;
    LatLng mUserLatLng;
    String mUserAddress;

    ArrayList<DeliveryItem> deliveriesList = new ArrayList<>();
    ArrayList<Marker> deliveryMarkers = new ArrayList<>();

    private static final int INITIAL_ZOOM_LEVEL = 14;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_sendernewdelivery, container, false);

        init(getArguments());

        // Init UI
        panelMap = (FrameLayout) rootView.findViewById(R.id.panelMap);
        scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);

        edtFromAddr = (EditText) rootView.findViewById(R.id.edtFromAddr);
        edtToAddr = (EditText) rootView.findViewById(R.id.edtToAddr);
        edtToAddr.requestFocus();

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

        spinnerFavs = (Spinner) rootView.findViewById(R.id.spinnerFavs);
        spinnerArrayAdapter = new ArrayAdapter<FavDeliveryUser>(mContext, android.R.layout.simple_spinner_item, favsInfoArray); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        spinnerFavs.setAdapter(spinnerArrayAdapter);
        spinnerFavs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {

                    if (TextUtils.isEmpty(newDeliveryId)) {
                        showAlert(R.string.error_make_delivery_first);
                        return;
                    }

                    final String fromAddr = edtFromAddr.getText().toString().trim();
                    final String toAddr = edtToAddr.getText().toString().trim();
                    final String addressInfo = String.format("From %s to %s", fromAddr, toAddr);

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject();

                        jsonObject.put("userid", appSettings.getUserId());
                        jsonObject.put("driverid", favsInfoArray.get(i).getDriverID());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    showProgressDialog();

                    RequestQueue queue = Volley.newRequestQueue(mContext);
                    /*StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelGetDriverTokensByDriverID(jsonObject), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            hideProgressDialog();

                            if (response != null || !response.isEmpty()) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String status = jsonObject.getString("status");
                                    if (jsonObject.getBoolean("status")) {

                                        ArrayList<String> tokenList = new ArrayList<>();
                                        JSONArray dataArray = jsonObject.getJSONArray("data");
                                        for (int i = 0; i < dataArray.length(); i++) {
                                            JSONObject tokenObj = dataArray.getJSONObject(i);
                                            if (tokenObj.has("Token") && !tokenObj.isNull("Token")) {
                                                // Send push directly
                                                tokenList.add(tokenObj.getString("Token"));
                                            }
                                        }

                                        if (tokenList.isEmpty()) {
                                            showAlert("Driver has no any device to notify.");
                                        } else {

                                            String curUserName = String.format("%s %s", appSettings.getFN(), appSettings.getLN()).trim();
                                            String title = String.format("%s invited you for new delivery", curUserName);

                                            JSONObject jAdditionalData = new JSONObject();
                                            jAdditionalData.put("delID", newDeliveryId);
                                            jAdditionalData.put("SenderName", curUserName);
                                            jAdditionalData.put("SenderID", appSettings.getUserId());

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                new SendPushTask(true, tokenList, "1", title, addressInfo, jAdditionalData).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, addressInfo);
                                            } else {
                                                new SendPushTask(true, tokenList, "1", title, addressInfo, jAdditionalData).execute(addressInfo);
                                            }
                                        }
                                    } else {
                                        hideProgressDialog();
                                        showToastMessage(jsonObject.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    hideProgressDialog();
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Update Bid List Process
        mHandlerUpdateBidList = new Handler();
        mRunnableUpdateBidList = new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(newDeliveryId)) {
                    getDelBidsList(newDeliveryId, false);
                }
            }
        };

        panelBidContents = rootView.findViewById(R.id.panelBidContents);
        lvDataList = (ListView) rootView.findViewById(R.id.lvDataList);
        bidsInfoAdapter = new BidsListAdapter(mContext, bidsInfoArray);
        /*bidsInfoArray.add(new BidsInfoOriginal("Chuck", 77, 1, "16:30"));
        bidsInfoArray.add(new BidsInfoOriginal("Xaim", 100, 9, "12:30"));
        bidsInfoArray.add(new BidsInfoOriginal("XianG", 70, 5, "15:30"));
        bidsInfoArray.add(new BidsInfoOriginal("ShuiLian", 50, 7, "10:30"));
        bidsInfoArray.add(new BidsInfoOriginal("Xiang", 50, 10, "11:30"));*/
        lvDataList.setAdapter(bidsInfoAdapter);
        lvDataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                StringBuilder bidsInfo = new StringBuilder();
                bidsInfo.append("Bid Information\n");
                bidsInfo.append("Name : " + bidsInfoArray.get(i).getDFName() + "\n");
                bidsInfo.append("Amount : $" + bidsInfoArray.get(i).getBidAmount() + "\n");
                bidsInfo.append("Pickup Time : " + bidsInfoArray.get(i).getPickTime());

                //showAlert(bidsInfo.toString());

                //acceptBid(bidsInfoArray.get(i));
            }
        });

        btnSendRequest = (Button) rootView.findViewById(R.id.btnSendRequest);
        rootView.findViewById(R.id.btnScreen).setOnClickListener(this);
        rootView.findViewById(R.id.btnSendRequest).setOnClickListener(this);

        panelBidContents.setVisibility(View.GONE);

        SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mLocationUtil = new LocationUtil(parentActivity, SharedPreferenceManager.getInstance());

        rootView.findViewById(R.id.btnNewDel).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //getFavList();

        // Disable this logic
        //getFavTokenList();
        //getSenderDeliveryList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationUtil != null) {
            mLocationUtil.onDestroy();
        }

        if (mHandlerUpdateBidList != null) {
            mHandlerUpdateBidList.removeCallbacks(mRunnableUpdateBidList);
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

        //getDelBidsList();
    }


    // ------- Get All favorite tokens --------------------------------------------------------------------------------------
    ArrayList<String> favDriversTokenList = new ArrayList<>();
    private void getFavTokenList() {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();

            jsonObject.put("userid", appSettings.getUserId());
            jsonObject.put("senderid", appSettings.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        showProgressDialog();

        RequestQueue queue = Volley.newRequestQueue(mContext);
        /*StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelTokensNameBySenderID(jsonObject), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (jsonObject.getBoolean("status")) {

                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject favDataObj = dataArray.getJSONObject(i);
                                String token = favDataObj.getString("Token");
                                String firstName = favDataObj.getString("FN");
                                if (!TextUtils.isEmpty(token)) {
                                    favDriversTokenList.add(token);
                                }
                            }
                        } else {
                            //showToastMessage(jsonObject.getString("message"));
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

    /*private void getFavList() {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("devid", "1434741");
            jsonObject.put("appid", "ugoAndroid");
            GpsTracker gpsTracker = new GpsTracker(parentActivity.getApplicationContext());
            if (gpsTracker.canGetLocation()) {
                jsonObject.put("lon", String.valueOf(gpsTracker.getLongitude()));
                jsonObject.put("lat", String.valueOf(gpsTracker.getLatitude()));
            } else {
                jsonObject.put("lon", "0.0");
                jsonObject.put("lat", "0.0");
            }

            jsonObject.put("uuid", mMyApp.getAndroidId());
            jsonObject.put("userid", appSettings.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        showProgressDialog();

        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelFavsGet(jsonObject), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (jsonObject.getBoolean("status")) {

                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject favDataObj = dataArray.getJSONObject(i);
                                FavDeliveryUser newFavUser = new FavDeliveryUser();
                                newFavUser.setDriverID(favDataObj.getString("DriverID"));
                                newFavUser.setDriverMLID(favDataObj.getString("DriverMLID"));
                                //newFavUser.setToken(favDataObj.getString("Token"));

                                String favUserName = favDataObj.getString("Name");
                                String[] names = favUserName.split("\\s+");
                                if (names.length == 2) {
                                    newFavUser.setFN(names[0]);
                                    newFavUser.setLN(names[1]);
                                } else {
                                    newFavUser.setFN(favUserName);
                                    newFavUser.setLN("");
                                }

                                favsInfoArray.add(newFavUser);
                            }
                            if (favsInfoArray.size() > 0) {
                                FavDeliveryUser selectFavTitle = new FavDeliveryUser();
                                selectFavTitle.setFN("Choose Favorite");
                                selectFavTitle.setLN("");
                                favsInfoArray.add(0, selectFavTitle);
                            }

                            spinnerArrayAdapter.notifyDataSetChanged();

                        } else {
                            hideProgressDialog();
                            showToastMessage(jsonObject.getString("message"));
                        }
                    } catch (JSONException e) {
                        hideProgressDialog();
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
        queue.add(sr);
    }*/

    private void getSenderDeliveryList() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();

            jsonObject.put("userid", appSettings.getUserId());
            jsonObject.put("mlid", appSettings.getUserId());
            jsonObject.put("MLID", appSettings.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(mContext);
        /*StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelsBySenderID(jsonObject), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (jsonObject.getBoolean("status")) {

                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            Gson gson = new Gson();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject favDataObj = dataArray.getJSONObject(i);
                                DeliveryItem newDelInfo = gson.fromJson(favDataObj.toString(), DeliveryItem.class);
                                deliveriesList.add(newDelInfo);
                            }

                            showDeliveries();
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

    private void showDeliveries() {
        if (mMap == null)
            return;

        if (deliveriesList == null || deliveriesList.isEmpty())
            return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        int boundsCounter = 0;
        if (!deliveryMarkers.isEmpty()) {
            for (Marker marker : deliveryMarkers) {
                marker.remove();
            }
            deliveryMarkers.clear();
        }

        for (DeliveryItem delInfo : deliveriesList) {
            LatLng latLng = new LatLng(delInfo.getToLat(), delInfo.getToLon());

            if (TextUtils.isEmpty(delInfo.getDriverID()) || "0".equals(delInfo.getDriverID())) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                        .title("New"/*String.format("%s", delInfo.gettAdd())*/)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                marker.setTag(delInfo);
                marker.showInfoWindow();

                deliveryMarkers.add(marker);
                boundsCounter++;
            } else {
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                        .title(String.format("%s", delInfo.gettAdd()))
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                marker.setTag(delInfo);

                deliveryMarkers.add(marker);
                boundsCounter++;
            }

            boundsBuilder.include(latLng);
        }

        LatLngBounds latLngBounds = boundsBuilder.build();
        if (boundsCounter == 1) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 14.0f));

        } else {
            int routePadding = 200;
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
        }
    }

    private void sendMessage(FavDeliveryUser favUser) {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();

            jsonObject.put("userid", appSettings.getUserId());
            jsonObject.put("msg", "You have been requested!");
            jsonObject.put("msgto", favUser.getDriverMLID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        showProgressDialog();

        RequestQueue queue = Volley.newRequestQueue(mContext);
        /*StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiBeep(jsonObject), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (jsonObject.getBoolean("status")) {
                            showToastMessage(jsonObject.getString("message"));
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

    private void getDelBidsList(String delID, final boolean showProgress) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();

            jsonObject.put("userid", appSettings.getUserId());
            jsonObject.put("delid", delID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (showProgress) {
            showProgressDialog();
        }

        RequestQueue queue = Volley.newRequestQueue(mContext);
        /*StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelBidsList(jsonObject), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (showProgress) {
                    hideProgressDialog();
                }

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (jsonObject.getBoolean("status")) {

                            bidsInfoArray.clear();
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            Gson gson = new Gson();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject favDataObj = dataArray.getJSONObject(i);
                                bidsInfoArray.add(gson.fromJson(favDataObj.toString(), BidsInfoOriginal.class));
                            }
                            bidsInfoAdapter.notifyDataSetChanged();
                        } else {
                            showToastMessage(jsonObject.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mHandlerUpdateBidList.postDelayed(mRunnableUpdateBidList, 60 * 1000); // Get the bid list cycle
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (showProgress) {
                    hideProgressDialog();
                }
                showToastMessage("Request Error!, Please check network.");

                mHandlerUpdateBidList.postDelayed(mRunnableUpdateBidList, 60 * 1000); // Get the bid list cycle
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

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnScreen) {
            if (viewId == R.id.btnScreen) {
                if (!isFullScreen) {
                /*ViewGroup.LayoutParams scrollContentsParam = panelScrollContents.getLayoutParams();
                scrollContentsParam.height = LinearLayout.LayoutParams.MATCH_PARENT;
                panelScrollContents.setLayoutParams(scrollContentsParam);*/

                    ViewGroup.LayoutParams mapContentsParam = panelMap.getLayoutParams();
                    mapContentsParam.height = LinearLayout.LayoutParams.MATCH_PARENT;
                    panelMap.setLayoutParams(mapContentsParam);

                } else {
                /*ViewGroup.LayoutParams scrollContentsParam = panelScrollContents.getLayoutParams();
                scrollContentsParam.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                panelScrollContents.setLayoutParams(scrollContentsParam);*/

                    ViewGroup.LayoutParams mapContentsParam = panelMap.getLayoutParams();
                    mapContentsParam.height = dpToPx(mContext, 270);
                    panelMap.setLayoutParams(mapContentsParam);
                }

                isFullScreen = !isFullScreen;
            }
        } else if (viewId == R.id.btnSendRequest) {
            makeNewDelivery();
        } else if(viewId == R.id.btnNewDel) {
            // Start new Delivery Activity
            Intent newDeliveryIntent = new Intent(mContext, NewDeliveryActivity.class);
            newDeliveryIntent.putStringArrayListExtra("fav_tokens", favDriversTokenList);
            startActivityForResult(newDeliveryIntent, REQUEST_NEW_DELIVERY);
        }
    }

    private void makeNewDelivery() {
        final String toAddress = edtToAddr.getText().toString().trim();

        if (TextUtils.isEmpty(toAddress)) {
            edtToAddr.setError(getString(R.string.error_input_hint));
            showToastMessage(R.string.error_input_destination);
            return;
        }

        if (TextUtils.isEmpty(mUserAddress) || (toLatitude == 0 && toLongitude == 0)) {
            showToastMessage(R.string.error_wait_while_getting_address);
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delivery_details, null);

        final android.app.AlertDialog inputDlg = new android.app.AlertDialog.Builder(mContext, R.style.AlertDialogTheme)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        final EditText edtPhone = (EditText) dialogView.findViewById(R.id.edtPhone);
        final EditText edtName = (EditText) dialogView.findViewById(R.id.edtName);
        final EditText edtAddress = (EditText) dialogView.findViewById(R.id.edtAddress);
        final EditText edtApartment = (EditText) dialogView.findViewById(R.id.edtApartment);
        final EditText edtFloor = (EditText) dialogView.findViewById(R.id.edtFloor);
        final EditText edtCityStateZip = (EditText) dialogView.findViewById(R.id.edtCityStateZip);
        final EditText edtInstructions = (EditText) dialogView.findViewById(R.id.edtInstructions);

        final EditText edtPackageSize = (EditText) dialogView.findViewById(R.id.edtPackageSize);
        final EditText edtWeight = (EditText) dialogView.findViewById(R.id.edtWeight);
        final EditText edtQTY = (EditText) dialogView.findViewById(R.id.edtQTY);

        // Set first values
        if (!TextUtils.isEmpty(appSettings.getWP())) {
            edtPhone.setText(appSettings.getWP());
        } else if(!TextUtils.isEmpty(appSettings.getCP())) {
            edtPhone.setText(appSettings.getCP());
        }
        edtName.setText(appSettings.getFN());
        edtAddress.setText(mUserAddress);
        edtCityStateZip.setText(String.format("%s, %s", appSettings.getCity(), appSettings.getZip()));

        // Set saved values
        String deliveryInformationString = appSettings.getDeliveryInfo();
        try {
            JSONObject jsonDelivery = new JSONObject(deliveryInformationString);
            edtPhone.setText(jsonDelivery.getString("Phone"));
            edtName.setText(jsonDelivery.getString("Name"));
            edtAddress.setText(jsonDelivery.getString("Address"));
            edtApartment.setText(jsonDelivery.getString("Apartment"));
            edtFloor.setText(jsonDelivery.getString("Floor"));
            edtCityStateZip.setText(jsonDelivery.getString("CityStateZip"));
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        dialogView.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String phone = edtPhone.getText().toString().trim();
                final String name = edtName.getText().toString().trim();
                final String address = edtAddress.getText().toString().trim();
                final String apartment = edtApartment.getText().toString().trim();
                final String floor = edtFloor.getText().toString().trim();
                final String cityStateZip = edtCityStateZip.getText().toString().trim();
                final String instructions = edtInstructions.getText().toString().trim();
                final String packageSize = edtPackageSize.getText().toString().trim();
                final String weight = edtWeight.getText().toString().trim();
                final String qty = edtQTY.getText().toString().trim();

                /*InputMethodManager imm = (InputMethodManager)edtPrice.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                imm = (InputMethodManager)edtETA.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);*/

                hideKeyboard(edtPhone);
                hideKeyboard(edtName);
                hideKeyboard(edtAddress);
                hideKeyboard(edtApartment);
                hideKeyboard(edtFloor);
                hideKeyboard(edtCityStateZip);
                hideKeyboard(edtInstructions);
                hideKeyboard(edtPackageSize);
                hideKeyboard(edtWeight);
                hideKeyboard(edtQTY);

                if (TextUtils.isEmpty(phone)) {
                    edtPhone.setError("Invalid input");
                    edtPhone.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(name)) {
                    edtName.setError("Invalid input");
                    edtName.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(address)) {
                    edtAddress.setError("Invalid input");
                    edtAddress.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(apartment)) {
                    edtApartment.setError("Invalid input");
                    edtApartment.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(floor)) {
                    edtFloor.setError("Invalid input");
                    edtFloor.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(cityStateZip)) {
                    edtCityStateZip.setError("Invalid input");
                    edtCityStateZip.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(instructions)) {
                    edtInstructions.setError("Invalid input");
                    edtInstructions.requestFocus();
                    return;
                }

                int valuePackageSize = 0;
                try {
                    valuePackageSize = Integer.parseInt(packageSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (valuePackageSize <= 0) {
                    edtPackageSize.setError("Invalid input");
                    edtPackageSize.requestFocus();
                    return;
                }

                int valueWeight = 0;
                try {
                    valueWeight = Integer.parseInt(weight);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (valueWeight <= 0) {
                    edtWeight.setError("Invalid input");
                    edtWeight.requestFocus();
                    return;
                }

                int valueQTY = 0;
                try {
                    valueQTY = Integer.parseInt(qty);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (valueQTY <= 0) {
                    edtQTY.setError("Invalid input");
                    edtQTY.requestFocus();
                    return;
                }

                inputDlg.dismiss();

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("userid", appSettings.getUserId());

                    jsonObject.put("pickuplocaltime", "2018/10/20 11:00");
                    jsonObject.put("maxlocaltime", "2018/10/20 14:30");

                    jsonObject.put("capabilitiesid", "44");
                    jsonObject.put("statusid", "0");

                    jsonObject.put("fromadd", mUserAddress);
                    jsonObject.put("deltoadd", toAddress);

                    jsonObject.put("paksize", packageSize);
                    jsonObject.put("pakwgt", weight);

                    jsonObject.put("tolon", String.valueOf(toLongitude));
                    jsonObject.put("tolat", String.valueOf(toLatitude));

                    jsonObject.put("instruct", instructions);
                    jsonObject.put("ph", phone);
                    jsonObject.put("apt", apartment);
                    jsonObject.put("floor", floor);
                    jsonObject.put("qty", qty);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Save the information for next use
                // Set saved values
                String deliveryInformationString = appSettings.getDeliveryInfo();
                try {
                    JSONObject jsonDelivery = new JSONObject();

                    jsonDelivery.put("Phone", phone);
                    jsonDelivery.put("Name", name);
                    jsonDelivery.put("Address", address);
                    jsonDelivery.put("Apartment", apartment);
                    jsonDelivery.put("Floor", floor);
                    jsonDelivery.put("CityStateZip", cityStateZip);
                    appSettings.setDeliveryInfo(jsonDelivery.toString());
                } catch (JSONException e1) {
                    e1.printStackTrace();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

                showProgressDialog();

                /*RequestQueue queue = Volley.newRequestQueue(mContext);
                StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelAdd(jsonObject), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();

                        if (response != null || !response.isEmpty()) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String status = jsonObject.getString("status");
                                if (jsonObject.getBoolean("status") == true*//* || jsonObject.getBoolean("status") == false*//*) {
                                    //{"status":true,"NewID=":"21"}

                                    edtToAddr.setText("");
                                    getSenderDeliveryList();

                                    // Get new Del ID
                                    newDeliveryId = jsonObject.getString("NewID=");

                                    //showAlert("Successfully added new delivery.");

                            *//*appSettings.addMyDelivery(newDeliveryId);

                            btnSendRequest.setEnabled(false);

                            // Get Del Bids
                            panelBidContents.setVisibility(View.VISIBLE);*//*
                                    //getDelBidsList(newDeliveryId, true);

                                    // Send push to all Favorite driver devices
                                    if (!favDriversTokenList.isEmpty()) {
                                        String curUserName = String.format("%s %s", appSettings.getFN(), appSettings.getLN()).trim();
                                        String title = String.format("%s invited you for new delivery", curUserName);
                                        String addressInfo = String.format("From %s to %s", mUserAddress, toAddress);

                                        JSONObject jAdditionalData = new JSONObject();
                                        try {
                                            jAdditionalData.put("delID", newDeliveryId);
                                            jAdditionalData.put("SenderName", curUserName);
                                            jAdditionalData.put("SenderID", appSettings.getUserId());

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                new SendPushTask(true, favDriversTokenList, "1", title, addressInfo, jAdditionalData).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, addressInfo);
                                            } else {
                                                new SendPushTask(true, favDriversTokenList, "1", title, addressInfo, jAdditionalData).execute(addressInfo);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
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
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                inputDlg.dismiss();
            }
        });

        inputDlg.show();
        inputDlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
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
        updateCamera(location);

        edtFromAddr.setText(addressString);
        updateDistance();
    }

    public void updateCamera(@Nullable LatLng latLng) {
        if (latLng == null) {
            return;
        }
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL);
        mMap.animateCamera(location, 2000, null);
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
                        updateDistance();
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
        //edtDistance.setText(String.format("%.1f Km", distance));
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
        mLocationUtil.onResolutionResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_DELIVERY && resultCode == Activity.RESULT_OK) {
            //getSenderDeliveryList();
            ((MainActivity)parentActivity).showFragment(FRAGMENT_SENDER_DELIVERY_MAP);
        }
    }
}
