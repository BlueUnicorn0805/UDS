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
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.model.DeliveryItem;
import hawaiiappbuilders.c.utils.Helper;
import hawaiiappbuilders.c.waydirections.DirectionObject;
import hawaiiappbuilders.c.waydirections.GsonRequest;
import hawaiiappbuilders.c.waydirections.LegsObject;
import hawaiiappbuilders.c.waydirections.PolylineObject;
import hawaiiappbuilders.c.waydirections.RouteObject;
import hawaiiappbuilders.c.waydirections.StepsObject;
import hawaiiappbuilders.c.waydirections.VolleySingleton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class DriverDeliveryInfoFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static DriverDeliveryInfoFragment newInstance(String text, DeliveryItem deliveryItem) {
        DriverDeliveryInfoFragment mFragment = new DriverDeliveryInfoFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mBundle.putParcelable(DATA_FRAGMENT, deliveryItem);

        mFragment.setArguments(mBundle);

        return mFragment;
    }

    DeliveryItem driverDeliveryItem;

    TextView btnStatus;
    boolean userStatusIsActive = true;

    private GoogleMap mMap;

    private static final int INITIAL_ZOOM_LEVEL = 14;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_driverdelivery, container, false);

        init(getArguments());

        driverDeliveryItem = getArguments().getParcelable(DATA_FRAGMENT);

        btnStatus = (TextView) rootView.findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(this);

        rootView.findViewById(R.id.btnScreen).setOnClickListener(this);

        SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Disable the Toolbar in the map
        // mMap.getUiSettings().setMapToolbarEnabled(false);

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

        if (driverDeliveryItem != null) {
            // Add Mark
            LatLng latLng = new LatLng(driverDeliveryItem.getToLat(), driverDeliveryItem.getToLon());
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(String.format("%s %s", driverDeliveryItem.getStreetNum(), driverDeliveryItem.getStreet()))
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            marker.setTag(driverDeliveryItem);

            // Show Waypoint
            LatLng fromLatLng = new LatLng(driverDeliveryItem.getLat(), driverDeliveryItem.getLon());
            LatLng toLatLng = new LatLng(driverDeliveryItem.getToLat(), driverDeliveryItem.getToLon());

            // Start Find the Waypoint
            if (mPolyline != null) {
                mPolyline.remove();
            }

            String directionApiPath = Helper.getUrl(String.valueOf(fromLatLng.latitude), String.valueOf(fromLatLng.longitude),
                    String.valueOf(toLatLng.latitude), String.valueOf(toLatLng.longitude));
            Log.d("waypoint", "Path " + directionApiPath);
            getDirectionFromDirectionApiServer(directionApiPath);
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnStatus) {
            if (userStatusIsActive) {
                userStatusIsActive = false;
                btnStatus.setText("Off Duty");
                btnStatus.setBackgroundResource(R.color.user_status_off);
            } else {
                userStatusIsActive = true;
                btnStatus.setText("On Duty");
                btnStatus.setBackgroundResource(R.color.user_status_on);
            }
        } else if (viewId == R.id.btnConfirmComplete) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
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
            alertDialog.show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Object markerObject = marker.getTag();
        if (markerObject instanceof DeliveryItem) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
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
            alertDialog.show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
