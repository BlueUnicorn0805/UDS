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

import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.adapter.BidsListAdapter;
import hawaiiappbuilders.c.adapter.ReviewListAdapter;
import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.location.LocationUtil;
import hawaiiappbuilders.c.location.SharedPreferenceManager;
import hawaiiappbuilders.c.model.BidsInfoOriginal;
import hawaiiappbuilders.c.model.DeliveryItem;
import hawaiiappbuilders.c.model.ReviewInfo;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.Helper;
import hawaiiappbuilders.c.view.circularprogressindicator.CircularProgressIndicator;
import hawaiiappbuilders.c.waydirections.DirectionObject;
import hawaiiappbuilders.c.waydirections.GsonRequest;
import hawaiiappbuilders.c.waydirections.LegsObject;
import hawaiiappbuilders.c.waydirections.PolylineObject;
import hawaiiappbuilders.c.waydirections.RouteObject;
import hawaiiappbuilders.c.waydirections.StepsObject;
import hawaiiappbuilders.c.waydirections.VolleySingleton;
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
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SenderDeliveriesMapFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationUtil.LocationListener {

    public static SenderDeliveriesMapFragment newInstance(String text) {
        SenderDeliveriesMapFragment mFragment = new SenderDeliveriesMapFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    private GoogleMap mMap;
    private LocationUtil mLocationUtil;
    LatLng mUserLatLng;
    String mUserAddress;
    Marker currentPositionMarker;
    ArrayList<Marker> deliveryMarkers = new ArrayList<>();


    // Current Deliveries
    ArrayList<DeliveryItem> deliveriesList = new ArrayList<>();
    Handler mHandlerUpdateBidList;
    Runnable mRunnableUpdateBidList;
    DeliveryItem currentDeliveryItem;

    // Bid List
    TextView tvDelsInfo;
    ListView lvData;
    ArrayList<BidsInfoOriginal> bidsInfoArray = new ArrayList<>();
    BidsListAdapter bidsInfoAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_senderdelsmap, container, false);

        init(getArguments());

        tvDelsInfo = (TextView) rootView.findViewById(R.id.tvDelsInfo);
        tvDelsInfo.setText(String.format("Choose a Bid(DelID:%05d)", appSettings.getNewDelID()));

        lvData = (ListView) rootView.findViewById(R.id.lvDataList);
        bidsInfoAdapter = new BidsListAdapter(mContext, bidsInfoArray);
        lvData.setAdapter(bidsInfoAdapter);
        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int index, long l) {

                // Check green pin, this delivery already assigned to any driver
                String driverIDChoosen = bidsInfoAdapter.getDriverIDChoosen();
                if (!TextUtils.isEmpty(driverIDChoosen) && !"0".equals(bidsInfoAdapter.getDriverIDChoosen())) {
                    showAlert("Already selected driver for this delivery.");
                    return;
                }

                /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
                alertDialogBuilder.setTitle("Choose Driver");
                alertDialogBuilder.setMessage("Would you like to choose this driver on your delivery?")
                        .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        sendMessage(bidsInfoArray.get(index));
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();*/

                View dialogView = getLayoutInflater().inflate(R.layout.dialog_driver_profile, null);

                final android.app.AlertDialog inputDlg = new android.app.AlertDialog.Builder(mContext, R.style.AlertDialogTheme)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();

                CircularProgressIndicator progressCompleted = (CircularProgressIndicator) dialogView.findViewById(R.id.progressCompleted);
                CircularProgressIndicator progressOnTime = (CircularProgressIndicator) dialogView.findViewById(R.id.progressOnTime);
                CircularProgressIndicator progressRepeat = (CircularProgressIndicator) dialogView.findViewById(R.id.progressRepeat);
                progressCompleted.setProgress(new Random().nextInt(100), 100);
                progressOnTime.setProgress(new Random().nextInt(100), 100);
                progressRepeat.setProgress(new Random().nextInt(100), 100);

                ReviewListAdapter reviewListAdapter;
                ArrayList<ReviewInfo> reviewInfos = new ArrayList<>();
                ListView lvProfiles = (ListView) dialogView.findViewById(R.id.lvProfileDate);

                reviewInfos.add(new ReviewInfo("3", "Shu Lian", 5.0f, "\"Great Service, On-Time, Repeat hire again.\"", "", "2018-11-20"));
                reviewInfos.add(new ReviewInfo("4", "Anna", 5.0f, "\"Very good person. Always deliveried package in time and provided great service.\"", "", "2018-11-20"));
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
                        //sendMessage(bidsInfoArray.get(index));
                        acceptBid(bidsInfoArray.get(index));
                    }
                });

                inputDlg.show();
                inputDlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            }
        });

        // Update Bid List Process
        mHandlerUpdateBidList = new Handler();
        mRunnableUpdateBidList = new Runnable() {
            @Override
            public void run() {
                if (currentDeliveryItem != null) {
                    getDelBidsList(currentDeliveryItem, false);
                }
            }
        };

        // Init Maps
        SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        mLocationUtil = new LocationUtil(parentActivity, SharedPreferenceManager.getInstance());

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDeliveries(true);

        getDelBidsList(appSettings.getNewDelID(), false);
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
    }

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

        if (deliveriesList.isEmpty()) {
            updateCamera(mUserLatLng);
        }
    }

    private static final int INITIAL_ZOOM_LEVEL = 8;
    public void updateCamera(@Nullable LatLng latLng) {
        if (latLng == null) {
            return;
        }
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL);
        mMap.animateCamera(location, 1000, null);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        /*if(viewId == R.id.btnAddNew) {
            //parentActivity.showFragment(MainActivity.FRAGMENT_SENDER_NEW_DELIVERY);
        }*/
    }

    private void getDeliveries(final boolean bShowProgress) {

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
        String extraParams = "&mode=" + "DelsBySenderID" +
                "&driverID=" + appSettings.getDriverID() +
                "&industryID=" + "80" +
                "&sellerID=" + "0" +
                "&misc=" + appSettings.getDriverID();
        baseUrl += extraParams;

        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }

        Log.e("DelsBySenderID", fullParams);

        if (bShowProgress) {
            showProgressDialog();
        }

        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (bShowProgress) {
                    hideProgressDialog();
                }

                Log.e("DelsBySenderID", response);

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        if (jsonObject.has("status") && jsonObject.getBoolean("status") == false) {
                            showToastMessage(jsonObject.getString("msg"));
                        } else {
                            Gson gson = new Gson();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject favDataObj = jsonArray.getJSONObject(i);
                                DeliveryItem newDelInfo = gson.fromJson(favDataObj.toString(), DeliveryItem.class);
                                deliveriesList.add(newDelInfo);
                            }

                            showDeliveries();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                checkDeliveryStatus();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (bShowProgress) {
                    hideProgressDialog();
                }

                checkDeliveryStatus();
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

    private void checkDeliveryStatus() {
        if (deliveriesList.isEmpty()) {
            // Go to New Delivery
            // parentActivity.showFragment(FRAGMENT_SENDER_NEW_DELIVERY);
        }
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
                        .title("Tap"/*String.format("%s", delInfo.gettAdd())*/)
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object markerObject = marker.getTag();
        if (false && markerObject instanceof DeliveryItem) {
            final DeliveryItem deliveryInfo = (DeliveryItem) markerObject;

            // Start Find the Waypoint
            if (mPolyline != null) {
                mPolyline.remove();
            }

            // Disable the route
            /*String directionApiPath = Helper.getUrl(String.valueOf(deliveryInfo.getLat()), String.valueOf(deliveryInfo.getLon()),
                    String.valueOf(deliveryInfo.getToLat()), String.valueOf(deliveryInfo.getToLon()));
            Log.d("waypoint", "Path " + directionApiPath);
            getDirectionFromDirectionApiServer(directionApiPath);*/

            // Remove Current Callback and update current Delivery Item
            mHandlerUpdateBidList.removeCallbacks(mRunnableUpdateBidList);
            currentDeliveryItem = deliveryInfo;

            if (TextUtils.isEmpty(deliveryInfo.getDriverID()) || "0".equals(deliveryInfo.getDriverID())) {
                // Orange Pin
                getDelBidsList(deliveryInfo, true);
            } else {
                // Get Bids list
                //getDelBidsList(deliveryInfo, true);
            }
        }
        return false;
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
                        showToastMessage("Couldn't find the Waypoints!");
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

        mPolyline = map.addPolyline(options);

        /*// Wrap the Waypoints
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng point : positions) {
            boundsBuilder.include(point);
        }

        int routePadding = 200;
        LatLngBounds latLngBounds = boundsBuilder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));*/
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

    private void getDelBidsList(final DeliveryItem deliveryInfo, final boolean showProgress) {
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
        String extraParams = "&mode=" + "DelBidsList" +
                "&driverID=" + appSettings.getDriverID() +
                "&industryID=" + "80" +
                "&sellerID=" + "0" +
                "&misc=" + deliveryInfo.getDelID();
        baseUrl += extraParams;

        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }

        Log.e("DelBidsList", fullParams);

        if (showProgress) {
            showProgressDialog();
        }

        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (showProgress) {
                    hideProgressDialog();
                }

                Log.e("DelBidsList", response);

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        bidsInfoArray.clear();
                        bidsInfoAdapter.setDriverIDChoosen(deliveryInfo.getDriverID());

                        if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                            showToastMessage(jsonObject.getString("msg"));
                        } else {
                            Gson gson = new Gson();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject favDataObj = jsonArray.getJSONObject(i);
                                bidsInfoArray.add(gson.fromJson(favDataObj.toString(), BidsInfoOriginal.class));
                            }
                        }

                        bidsInfoAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (showProgress) {
                    hideProgressDialog();
                }

                checkDeliveryStatus();
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

    private void getDelBidsList(int delID, final boolean showProgress) {
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
        String extraParams = "&mode=" + "DelBidsList" +
                "&driverID=" + appSettings.getDriverID() +
                "&industryID=" + "80" +
                "&sellerID=" + "0" +
                "&misc=" + String.valueOf(delID);
        baseUrl += extraParams;

        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }

        Log.e("DelBidsList", fullParams);

        if (showProgress) {
            showProgressDialog();
        }

        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (showProgress) {
                    hideProgressDialog();
                }

                Log.e("DelBidsList", response);

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        bidsInfoArray.clear();

                        if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                            showToastMessage(jsonObject.getString("msg"));
                        } else {
                            Gson gson = new Gson();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject favDataObj = jsonArray.getJSONObject(i);
                                BidsInfoOriginal newBidInfo = gson.fromJson(favDataObj.toString(), BidsInfoOriginal.class);
                                if (!TextUtils.isEmpty(newBidInfo.getBidID()) && !"null".equals(newBidInfo.getBidID())) {
                                    bidsInfoArray.add(newBidInfo);
                                }
                            }
                        }

                        bidsInfoAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (showProgress) {
                    hideProgressDialog();
                }

                checkDeliveryStatus();
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

    private void sendMessage(final BidsInfoOriginal favUser) {

        if (currentDeliveryItem == null) {
            return;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();

            jsonObject.put("userid", appSettings.getUserId());

            String msgContent = String.format("You have bee requested from %s to %s.", currentDeliveryItem.getfAdd(), currentDeliveryItem.gettAdd());
            jsonObject.put("msg", msgContent);

            jsonObject.put("msgto", favUser.getDriverID());
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

                        sendDeliveryRequestMessage(favUser);
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

    private void acceptBid(final BidsInfoOriginal bidsInfo) {

        // Check current delivery information
        if (currentDeliveryItem == null) {
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        alertDialogBuilder.setTitle("Confirm accept");
        alertDialogBuilder.setMessage("Do you want to accept this driver?")
                .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("userid", appSettings.getUserId());

                    jsonObject.put("delid", currentDeliveryItem.getDelID());
                    jsonObject.put("driverid", bidsInfo.getDriverID());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                showProgressDialog();

                RequestQueue queue = Volley.newRequestQueue(mContext);
                /*StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelAccept(jsonObject), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressDialog();

                        if (response != null || !response.isEmpty()) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String status = jsonObject.getString("status");
                                if (jsonObject.getBoolean("status") == true*//* || jsonObject.getBoolean("status") == false*//*) {
                                    *//*showAlert("Selected driver. Enjoy your delivery.", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            getDeliveries(false);
                                            bidsInfoAdapter.setDriverIDChoosen(bidsInfo.getDriverID());
                                            sendDeliveryRequestMessage(bidsInfo);
                                        }
                                    });*//*

                                    getDeliveries(false);
                                    bidsInfoAdapter.setDriverIDChoosen(bidsInfo.getDriverID());
                                    sendDeliveryRequestMessage(bidsInfo);
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

    private void sendDeliveryRequestMessage(BidsInfoOriginal bidInfo) {
        // Check data
        if (currentDeliveryItem == null || bidInfo == null)
            return;

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();

            jsonObject.put("userid", appSettings.getUserId());
            jsonObject.put("driverid", bidInfo.getDriverID());
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
                                showAlert("Driver has no any device to contact.");
                            } else {
                                String curUserName = String.format("%s %s", appSettings.getFN(), appSettings.getLN()).trim();
                                String title = String.format("%s selected you for the delivery", curUserName);
                                String addressInfo = String.format("From %s to %s", currentDeliveryItem.getfAdd(), currentDeliveryItem.gettAdd());

                                JSONObject jAdditionalData = new JSONObject();
                                jAdditionalData.put("delID", currentDeliveryItem.getDelID());
                                jAdditionalData.put("SenderName", curUserName);
                                jAdditionalData.put("SenderID", appSettings.getUserId());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    new SendPushTask(true, tokenList, "2", title, addressInfo, jAdditionalData).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, addressInfo);
                                } else {
                                    new SendPushTask(true, tokenList, "2", title, addressInfo, jAdditionalData).execute(addressInfo);
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
