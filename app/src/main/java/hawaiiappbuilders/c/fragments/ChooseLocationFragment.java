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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import hawaiiappbuilders.c.ActivityPermission;
import hawaiiappbuilders.c.FragmentFolderActivity;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.location.GpsTracker;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class ChooseLocationFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback {

    public static ChooseLocationFragment newInstance(String text) {
        ChooseLocationFragment mFragment = new ChooseLocationFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    private GoogleMap mMap;
    TextView tvLat;
    TextView tvLot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_choose_location, container, false);

        init(getArguments());

        tvLat = rootView.findViewById(R.id.tvLat);
        tvLot = rootView.findViewById(R.id.tvLot);

        SupportMapFragment mMapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
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
    public void onClick(View view) {
        int viewId = view.getId();
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
//        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//            @Override
//            public void onMyLocationChange(Location location) {
//
//            }
//        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                tvLat.setText(String.format("%f", mMap.getCameraPosition().target.latitude));
                tvLot.setText(String.format("%f", mMap.getCameraPosition().target.longitude));
            }
        });

        getMyLocation();
    }

    private void getMyLocation() {
        //checkPermissions(mContext, PERMISSION_REQUEST_LOCATION_STRING, false, PERMISSION_REQUEST_CODE_LOCATION)
        if (isPermissionsAllowed(mContext, PERMISSION_REQUEST_LOCATION_STRING)) {
            showCurrentPosition();
        } else {
            /*parentActivity.showLocationSettingsAlert(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkPermissions(mContext, PERMISSION_REQUEST_LOCATION_STRING, false, PERMISSION_REQUEST_CODE_LOCATION);
                }
            });*/

            // Go to Permission Screen
            startActivityForResult(new Intent(mContext, ActivityPermission.class), PERMISSION_REQUEST_CODE_LOCATION);
        }
    }

    private void showCurrentPosition() {
        if (mMap == null) {
            return;
        }

        try {
            //mMap.setMyLocationEnabled(true);

            GpsTracker gpsTracker = new GpsTracker(mContext);
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();

            if (latitude == 0 && longitude == 0) {
                gpsTracker.showSettingsAlert();
                return;
            }

            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16);
            mMap.animateCamera(location, 1000, null);

            //mMap.getUiSettings().setScrollGesturesEnabled(false);
            //mMap.getUiSettings().setZoomGesturesEnabled(false);
            //mMap.getUiSettings().setZoomControlsEnabled(false);

            tvLat.setText(String.format("%f", mMap.getCameraPosition().target.latitude));
            tvLot.setText(String.format("%f", mMap.getCameraPosition().target.longitude));

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateFields() {

        FragmentFolderActivity newSaleActivity = (FragmentFolderActivity) parentActivity;
        String strLat = newSaleActivity.restoreValue("Lat");
        String strLon = newSaleActivity.restoreValue("Lon");
        if (mMap != null && !TextUtils.isEmpty(strLat) && !TextUtils.isEmpty(strLon)) {
            double latitude = Double.parseDouble(strLat);
            double longitude = Double.parseDouble(strLon);

            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16);
            mMap.animateCamera(location, 1000, null);
        }
    }

    @Override
    public void saveFields() {
        super.saveFields();

        FragmentFolderActivity newSaleActivity = (FragmentFolderActivity) parentActivity;

        if (mMap != null) {
            newSaleActivity.saveValue("Lat", String.valueOf(mMap.getCameraPosition().target.latitude));
            newSaleActivity.saveValue("Lon", String.valueOf(mMap.getCameraPosition().target.longitude));
        } else if (parentActivity.getLocation()) {
            newSaleActivity.saveValue("Lat", parentActivity.getUserLat());
            newSaleActivity.saveValue("Lon", parentActivity.getUserLon());
        } else {
            newSaleActivity.saveValue("Lat", "0");
            newSaleActivity.saveValue("Lon", "0");
        }
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

        if (bAllGranted && requestCode == PERMISSION_REQUEST_CODE_LOCATION) {
            // Enable Current location pick
            showCurrentPosition();
        } else {
            parentActivity.showLocationSettingsAlert();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        getMyLocation();
    }
}
