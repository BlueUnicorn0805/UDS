package hawaiiappbuilders.c;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import hawaiiappbuilders.c.R;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.AppCompatRadioButton;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import hawaiiappbuilders.c.adapter.BidsListAdapter;
import hawaiiappbuilders.c.adapter.OpenDeliveriesListAdapter;
import hawaiiappbuilders.c.location.Constants;
import hawaiiappbuilders.c.location.GeocodeAddressIntentService;
import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.location.LocationUtil;
import hawaiiappbuilders.c.location.SharedPreferenceManager;
import hawaiiappbuilders.c.messaging.FCMHelper;
import hawaiiappbuilders.c.messaging.TokenGetter;
import hawaiiappbuilders.c.model.BidsInfoOriginal;
import hawaiiappbuilders.c.model.FCMTokenData;
import hawaiiappbuilders.c.model.OpenDeliveryInfo;
import hawaiiappbuilders.c.model.FavDeliveryUser;
import hawaiiappbuilders.c.model.User;
import hawaiiappbuilders.c.utils.K;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OriginalMapsActivity extends BaseActivity implements OnMapReadyCallback,
        LocationUtil.LocationListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private LocationUtil mLocationUtil;

    private static final int INITIAL_ZOOM_LEVEL = 14;

    boolean isFullScreen;

    boolean userIsSender;

    ScrollView scrollView;
    LinearLayout panelScrollContents;
    FrameLayout panelMap;

    TextView btnStatus;
    boolean userStatusIsActive;

    View panelDistance;

    EditText edtFromAddr;

    EditText edtToAddr;
    Handler addressUpdateHandler;
    AddressResultReceiver mResultReceiver;
    double toLatitude;
    double toLongitude;

    EditText edtDistance;

    View panelUser1;
    View panelUser2;
    RadioGroup radioGroup;
    AppCompatRadioButton radioUser1;
    AppCompatRadioButton radioUser2;

    View panelBottomUser2;

    Spinner spinnerFavs;
    ArrayList<FavDeliveryUser> fabsInfoArray = new ArrayList<>();
    ArrayAdapter<FavDeliveryUser> spinnerArrayAdapter;

    TextView tvUserCategory;
    TextView headerName;
    TextView headerAmount;
    TextView headerTime;

    ListView lvDataList;
    ArrayList<BidsInfoOriginal> bidsInfoArray = new ArrayList<>();
    BidsListAdapter bidsInfoAdapter;

    ArrayList<OpenDeliveryInfo> openDeliveriesInfoArray = new ArrayList<>();
    OpenDeliveriesListAdapter openDeliveriesInfoAdapter;

    ArrayList<OpenDeliveryInfo> deliveriesInfoInMap = new ArrayList<>();

    LatLng mUserLatLng;
    String mUserAddress;

    User mCurrentUserInfo;

    // Firebase Data Fetch
    private FirebaseAuth mFireBaseAuth;
    private FirebaseDatabase mFireBaseDataBase;
    private ValueEventListener mUserDetailsValueEventListener;
    private ValueEventListener mDriverListValueEventListener;
    private ValueEventListener mBidderListValueEventListener;

    private DatabaseReference mCurrentUserDetailsDBReference;
    private DatabaseReference mDriversListDBReference;
    private DatabaseReference mBidderListDBReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();*/

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        int menuItemSize = navigationView.getMenu().size();
        for (int i = 0; i < menuItemSize; i++) {
            navigationView.getMenu().getItem(i).setCheckable(false);
        }

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserName);
        tvUserName.setText(appSettings.getFN() + " " + appSettings.getLN());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocationUtil = new LocationUtil(this, SharedPreferenceManager.getInstance());

        userIsSender = true;

        btnStatus = findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(this);
        userStatusIsActive = true;

        scrollView = findViewById(R.id.scrollView);
        panelScrollContents = (LinearLayout) findViewById(R.id.panelScrollContents);
        panelMap = (FrameLayout) findViewById(R.id.panelMap);

        panelDistance = findViewById(R.id.panelDistance);

        // User Input and auto filled
        edtFromAddr = findViewById(R.id.edtFromAddr);
        edtFromAddr.setKeyListener(null);

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
                        startService(intent);
                    }
                }
            }
        };

        edtToAddr = findViewById(R.id.edtToAddr);
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

        edtDistance = findViewById(R.id.edtDistance);
        edtDistance.setKeyListener(null);

        panelUser1 = findViewById(R.id.panelUser1);
        panelUser2 = findViewById(R.id.panelUser2);

        panelBottomUser2 = findViewById(R.id.panelBottomUser2);

        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radioUser1) {
                    userIsSender = true;

                    panelDistance.setVisibility(View.GONE);

                    panelBottomUser2.setVisibility(View.VISIBLE);

                    panelUser1.setVisibility(View.VISIBLE);
                    panelUser2.setVisibility(View.GONE);

                    if (!TextUtils.isEmpty(mUserAddress)) {
                        edtFromAddr.setText(mUserAddress);
                    }

                    // Change Hint
                    edtToAddr.setHint("123 Main St, Des Moines, IA 50309");

                    // Update User List
                    tvUserCategory.setText("Bids");
                    headerName.setText("Name");
                    headerAmount.setVisibility(View.VISIBLE);
                    headerAmount.setText("Bid");
                    headerTime.setText("Pickup Time");
                    lvDataList.setAdapter(bidsInfoAdapter);
                } else if (i == R.id.radioUser2) {
                    userIsSender = false;

                    panelDistance.setVisibility(View.VISIBLE);

                    panelBottomUser2.setVisibility(View.VISIBLE);

                    panelUser1.setVisibility(View.GONE);
                    panelUser2.setVisibility(View.VISIBLE);

                    edtFromAddr.setText("");

                    // Change Hint
                    edtToAddr.setHint("");

                    // Update User List
                    tvUserCategory.setText("Summary of all Deliveries");
                    headerName.setText("Name");
                    headerAmount.setVisibility(View.GONE);
                    headerAmount.setText("Address");
                    headerTime.setText("Ready Time");
                    lvDataList.setAdapter(openDeliveriesInfoAdapter);
                }
            }
        });

        tvUserCategory = (TextView) findViewById(R.id.tvUserCategory);
        headerName = (TextView) findViewById(R.id.headerName);
        headerAmount = (TextView) findViewById(R.id.headerAmount);
        headerTime = (TextView) findViewById(R.id.headerTime);

        lvDataList = findViewById(R.id.lvDataList);
        bidsInfoAdapter = new BidsListAdapter(mContext, bidsInfoArray);
        openDeliveriesInfoAdapter = new OpenDeliveriesListAdapter(mContext, openDeliveriesInfoArray);

        bidsInfoArray.add(new BidsInfoOriginal("Chuck", 77, 1, "16:30"));
        bidsInfoArray.add(new BidsInfoOriginal("Xaim", 100, 9, "12:30"));
        bidsInfoArray.add(new BidsInfoOriginal("XianG", 70, 5, "15:30"));
        bidsInfoArray.add(new BidsInfoOriginal("ShuiLian", 50, 7, "10:30"));
        bidsInfoArray.add(new BidsInfoOriginal("Xiang", 50, 10, "11:30"));
        lvDataList.setAdapter(bidsInfoAdapter);
        lvDataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (userIsSender) {
                    StringBuilder bidsInfo = new StringBuilder();
                    bidsInfo.append("Bid Information\n");
                    bidsInfo.append("Name : " + bidsInfoArray.get(i).getDFName() + "\n");
                    bidsInfo.append("Amount : $" + bidsInfoArray.get(i).getBidAmount() + "\n");
                    bidsInfo.append("Pickup Time : " + bidsInfoArray.get(i).getPickTime());

                    showAlert(bidsInfo.toString());
                } else {
                    StringBuilder deliveryInfo = new StringBuilder();
                    deliveryInfo.append("Delivery Information\n");
                    deliveryInfo.append("Name : " + openDeliveriesInfoArray.get(i).getUserName() + "\n");
                    deliveryInfo.append("Delivery Time : " + openDeliveriesInfoArray.get(i).getPickTime());

                    showAlert(deliveryInfo.toString());
                }
            }
        });

        openDeliveriesInfoArray.add(new OpenDeliveryInfo("SandWitch", 70, "123 Long Street", "16:30"));
        openDeliveriesInfoArray.add(new OpenDeliveryInfo("HotDog", 80, "12 Short Street", "16:30"));
        openDeliveriesInfoArray.add(new OpenDeliveryInfo("Bread", 90, "10 Middle Street", "16:30"));
        openDeliveriesInfoArray.add(new OpenDeliveryInfo("Milk", 100, "5 Beautiful Street", "16:30"));
        openDeliveriesInfoArray.add(new OpenDeliveryInfo("Rice", 120, "3 Urgly Street:)", "16:30"));

        findViewById(R.id.btnScreen).setOnClickListener(this);
        findViewById(R.id.btnSendRequest).setOnClickListener(this);
        findViewById(R.id.btnSendBid).setOnClickListener(this);

        spinnerFavs = findViewById(R.id.spinnerFavs);
        spinnerArrayAdapter = new ArrayAdapter<FavDeliveryUser>(mContext, android.R.layout.simple_spinner_item, fabsInfoArray); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        spinnerFavs.setAdapter(spinnerArrayAdapter);

        // setRealtimeDataBaseListener();
        // getFavList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove Callback
        mLocationUtil.onDestroy();
        removeOldMarkers(driverMarkers);
        removeOldMarkers(bidderMarkers);
    }

    private void setRealtimeDataBaseListener() {
        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseDataBase = FirebaseDatabase.getInstance();

        mCurrentUserDetailsDBReference = mFireBaseDataBase.getReference()
                .child("users")
                .child(mFireBaseAuth.getUid());

        mDriversListDBReference = mFireBaseDataBase.getReference()
                .child("users");

        mBidderListDBReference = mFireBaseDataBase.getReference()
                .child("drivers");

        mUserDetailsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCurrentUserInfo = dataSnapshot.getValue(User.class);
                }
                mDriversListDBReference.addValueEventListener(
                        mDriverListValueEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mDriversListDBReference.addValueEventListener(
                        mDriverListValueEventListener);
            }
        };

        mDriverListValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<User> receivers = new ArrayList<>();

                for (DataSnapshot receiver : dataSnapshot.getChildren()) {
                    if (receiver.exists()) {
                        User request = receiver.getValue(User.class);
                        if (request != null) {
                            receivers.add(request);
                        }
                    }
                }
                addDriverMarkers(receivers);
                //mBidderListDBReference.addValueEventListener(mBidderListValueEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //mBidderListDBReference.addValueEventListener(mBidderListValueEventListener);
            }
        };

        mBidderListValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<UserInfo> donors = new ArrayList<>();
                for (DataSnapshot donor : dataSnapshot.getChildren()) {
                    if (donor.exists()) {
                        UserInfo request = donor.getValue(UserInfo.class);
                        if (request != null)
                            donors.add(request);
                    }
                }
                //mView.addDriverMarkers(donors);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mCurrentUserDetailsDBReference.addListenerForSingleValueEvent(mUserDetailsValueEventListener);
    }

    private void getFavList() {

        /*JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("devid", "1434741");
            jsonObject.put("appid", "ugoAndroid");
            GpsTracker gpsTracker = new GpsTracker(getApplicationContext());
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
                                newFavUser.setUserID(favDataObj.getInt("DriverID"));
                                String favUserName = favDataObj.getString("Name");
                                String[] names = favUserName.split("\\s+");
                                if (names.length == 2) {
                                    newFavUser.setFN(names[0]);
                                    newFavUser.setLN(names[1]);
                                } else {
                                    newFavUser.setFN(favUserName);
                                    newFavUser.setLN("");
                                }

                                fabsInfoArray.add(newFavUser);
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
        queue.add(sr);*/
    }

    private void getPinList() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("userid", appSettings.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        showProgressDialog();

        RequestQueue queue = Volley.newRequestQueue(mContext);
        /*StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelsInArea(jsonObject), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (jsonObject.getBoolean("status")) {

                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            Gson gson = new Gson();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject favDataObj = dataArray.getJSONObject(i);
                                deliveriesInfoInMap.add(gson.fromJson(favDataObj.toString(), OpenDeliveryInfo.class));
                            }

                            showDeliveriesInArea(deliveriesInfoInMap);
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

    private void showDeliveriesInArea(ArrayList<OpenDeliveryInfo> delsListInfo) {
        if (mMap == null)
            return;

        if (delsListInfo == null || delsListInfo.isEmpty())
            return;

        for (OpenDeliveryInfo delInfo : delsListInfo) {
            LatLng latLng = new LatLng(delInfo.getLat(), delInfo.getLon());
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(String.format("%s to %s", delInfo.getfAdd(), delInfo.gettAdd()))
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            marker.setTag(delInfo);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
            /*try{
                mMap.setMyLocationEnabled(true);
            } catch(SecurityException e) {
                e.printStackTrace();
            }*/
        }
        mMap.setOnMarkerClickListener(this);

        mLocationUtil.fetchApproximateLocation(this);
        mLocationUtil.fetchPreciseLocation(this);

        getPinList();
    }

    Marker currentPositionMarker;
    Marker currentActiveMarker;
    private ArrayList<Marker> driverMarkers = new ArrayList<>();
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
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location)));
        updateCamera(location);

        if (userIsSender) {
            edtFromAddr.setText(addressString);
            updateDistance();
        }

        /*User userInfo = new User();
        userInfo.setName(mSettings.getString("username", "User"));
        userInfo.setNotificationToken(mSettings.getString("pushtoken", "pushtoken"));
        userInfo.setAddress(mUserAddress);
        userInfo.setLatitude(mUserLatLng.latitude);
        userInfo.setLongitude(mUserLatLng.longitude);
        userInfo.setUserId(mFireBaseAuth.getUid());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userInfo);*/

    }


    public void updateCamera(@Nullable LatLng latLng) {
        if (latLng == null) {
            return;
        }
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL);
        mMap.animateCamera(location, 2000, null);
    }

    public void addMarker(@NonNull User request, boolean isDriver) {

        if (request == null)
            return;

        if (request.getLatitude() == null || request.getLongitude() == null)
            return;

        LatLng latLng = new LatLng(request.getLatitude(), request.getLongitude());
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                .title(String.format("%s (%s)", request.getName(), "You have choosen this driver"))
                .icon(BitmapDescriptorFactory
                        .defaultMarker(isDriver ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED)));
        marker.setTag(request);

        if (isDriver)
            driverMarkers.add(marker);
        else bidderMarkers.add(marker);
    }

    public void addDriverMarkers(ArrayList<User> drivers) {

        currentActiveMarker = null;

        removeOldMarkers(driverMarkers);
        for (User driver : drivers) {
            // Skip my information
            if (mCurrentUserInfo != null && mCurrentUserInfo.getUserId().equals(driver.getUserId()))
                continue;

            addMarker(driver, true);
        }
    }

    public void removeOldMarkers(ArrayList<Marker> markers) {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != currentPositionMarker) {
            currentActiveMarker = marker;
        }
        return false;
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
        } else if (viewId == R.id.btnSendBid || viewId == R.id.btnSendRequest) {
            // Send Push
            if (currentActiveMarker == null) {
                return;
            }

            User userInfo = (User) currentActiveMarker.getTag();
            if (userInfo == null || TextUtils.isEmpty(userInfo.getNotificationToken())) {
                return;
            }

            ArrayList<FCMTokenData> tokenList = new ArrayList<>();
            tokenList.add(new FCMTokenData(userInfo.getNotificationToken(), FCMTokenData.OS_UNKNOWN));

            String curUserName = mCurrentUserInfo.getName();
            String message = getString(R.string.format_push_for_drivers);
            message = message.replace("1", curUserName);

            TokenGetter tokenGetter = new TokenGetter(appSettings.getUserId(), mContext, (BaseActivity) mContext);
            if (!tokenList.isEmpty()) {
                JSONObject payload = new JSONObject();
                try {
                    payload.put("message", message);
                    tokenGetter.sendPushNotification(mContext, tokenList, FCMHelper.PT_Text_Message, payload);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (viewId == R.id.btnScreen) {
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
        edtDistance.setText(String.format("%.1f Mi", distance * MILES_PER_KILO));
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

    public class SendPushTask extends AsyncTask<String, Void, String> {

        boolean isDriver;
        ArrayList<String> deviceList;

        public SendPushTask(boolean isDriver, ArrayList<String> deviceIdList) {
            this.isDriver = isDriver;
            this.deviceList = deviceIdList;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(String result) {
            hideProgressDialog();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(String result) {
            super.onCancelled(result);
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";



            for (String device : deviceList) {
                try {
                    try {
                        // Prepare JSON containing the FCM message content. What to send and where to send.
                        JSONObject jGcmData = new JSONObject();
                        JSONObject jData = new JSONObject();

                        // Where to send GCM message.
                        jGcmData.put("to", device);

                        // What to send in GCM message.
                        jGcmData.put("data", jData);

                        // Create connection to send GCM Message request.
                        URL url = new URL("https://fcm.googleapis.com/fcm/send");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestProperty("Authorization", "key=" + K.gKy(BuildConfig.PM));
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);

                        // Send FCM message content.
                        OutputStream outputStream = conn.getOutputStream();
                        outputStream.write(jGcmData.toString().getBytes());

                        // Read FCM response.
                        InputStream inputStream = conn.getInputStream();
                        String resp = IOUtils.toString(inputStream, "UTF-8");
                        System.out.println(resp);
                        System.out.println("Check your device/emulator for notification or logcat for " +
                                "confirmation of the receipt of the GCM message.");
                    } catch (IOException e) {
                        System.out.println("Unable to send GCM message.");
                        System.out.println("Please ensure that API_KEY has been replaced by the server " +
                                "API key, and that the device's registration token is correct (if specified).");
                        e.printStackTrace();
                    }


                    /*String strEmail = params[0];
                    String strPassword = params[1];

                    URL url = new URL("http://192.168.1.55/token");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("grant_type", "password")
                            .appendQueryParameter("username", strEmail)
                            .appendQueryParameter("Password", strPassword);
                    String query = builder.build().getEncodedQuery();

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();

                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String line;
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = reader.readLine()) != null) {
                            response += line;
                        }
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return response;
        }
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
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(R.string.request_permission_hint);
        }
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == Constants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(Constants.RESULT_ADDRESS);
                runOnUiThread(new Runnable() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLocationUtil.onResolutionResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {

        } else if (id == R.id.nav_makenewdelivery) {
            // Handle the camera action
        } else if (id == R.id.nav_driverdeliveries) {

        } else if (id == R.id.nav_deposit) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.END);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}
