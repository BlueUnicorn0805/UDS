package hawaiiappbuilders.c;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
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

import hawaiiappbuilders.c.adapter.CalTimeAdapter;
import hawaiiappbuilders.c.fragments.BaseFragment;
import hawaiiappbuilders.c.fragments.DriverDeliveriesListFragment;
import hawaiiappbuilders.c.fragments.DriverDeliveriesMapFragment;
import hawaiiappbuilders.c.fragments.DriverDeliveryInfoFragment;
import hawaiiappbuilders.c.fragments.HomeFragment;
import hawaiiappbuilders.c.fragments.SenderDeliveriesListFragment;
import hawaiiappbuilders.c.fragments.SenderDeliveriesMapFragment;
import hawaiiappbuilders.c.fragments.SenderNewDeliveryFragment;
import hawaiiappbuilders.c.model.CalendarData;
import hawaiiappbuilders.c.model.DeliveryItem;
import hawaiiappbuilders.c.model.User;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.DownloadFileFromURL;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.K;
import hawaiiappbuilders.c.webutils.VolleySingleton;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements
        View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    Toolbar toolbar;
    TextView toolbarTitle;

    NavigationView navigationViewCalendar;

    BaseFragment currentFragment;

    User mCurrentUserInfo;

    public static final int FRAGMENT_HOME = 1;
    public static final int FRAGMENT_SENDER_DELIVERY_LIST = 2;
    public static final int FRAGMENT_SENDER_DELIVERY_MAP = 3;
    public static final int FRAGMENT_SENDER_NEW_DELIVERY = 4;
    public static final int FRAGMENT_DRIVER_DELIVERY_LIST = 5;
    public static final int FRAGMENT_DRIVER_DELIVERY_MAP = 6;
    public static final int FRAGMENT_DRIVER_DELIVERY = 7;

    // Firebase Data Fetch
    private FirebaseAuth mFireBaseAuth;
    private FirebaseDatabase mFireBaseDataBase;
    private ValueEventListener mUserDetailsValueEventListener;
    private ValueEventListener mDriverListValueEventListener;
    private ValueEventListener mBidderListValueEventListener;

    private DatabaseReference mCurrentUserDetailsDBReference;
    private DatabaseReference mDriversListDBReference;
    private DatabaseReference mBidderListDBReference;

    View panelCal;
    TextView tvMonth;
    String[] monthTitleString;
    private Calendar calSelectedDate = Calendar.getInstance();
    private final List<TextView> dayLabelTextViews = new ArrayList<>();
    private final List<TextView> dateTextViews = new ArrayList<>();
    private final List<ImageView> selectedImageViews = new ArrayList<>();
    View panelWeekDays;
    CalendarView calView;
    RecyclerView recyclerAppts;
    CalTimeAdapter calAdapter;
    private ArrayList<CalendarData.Data> mCalendarDataList = new ArrayList<>();
    private ArrayList<String> mCalendarDataMonths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);

        findViewById(R.id.btnSetStatus).setOnClickListener(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(Gravity.RIGHT)) {
                    drawer.closeDrawer(Gravity.RIGHT);
                } else {
                    drawer.openDrawer(Gravity.RIGHT);
                }
            }
        });

        // Remove All push
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();

        // Check User status
        if (!appSettings.isLoggedIn()) {
            startActivity(new Intent(mContext, LoginActivity.class));
            finish();
            return;
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        int menuItemSize = navigationView.getMenu().size();
        for (int i = 0; i < menuItemSize; i++) {
            navigationView.getMenu().getItem(i).setCheckable(false);
        }

        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        TextView tvVersion = headerView.findViewById(R.id.tvVersion);
        tvVersion.setText(getVersionName());

        TextView tvSlogan = headerView.findViewById(R.id.tvSlogan);
        tvSlogan.setText(getString(R.string.app_name));

        TextView tvUserName = headerView.findViewById(R.id.tvUserName);
        tvUserName.setText(appSettings.getFN() + " " + appSettings.getLN());

        try {
            registerReceiver(updateStatusReceiver, new IntentFilter("com.ugo.updatedeliverystatus"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set Left navigation
        navigationViewCalendar = (NavigationView) findViewById(R.id.nav_view_left);
        if (navigationViewCalendar != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navigationViewCalendar.getLayoutParams();
            params.width = metrics.widthPixels;
            navigationViewCalendar.setLayoutParams(params);
        }
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (drawerView == navigationViewCalendar) {
                    getApptData(true);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });


        // Panel Calc
        panelCal = findViewById(R.id.panelCal);

        monthTitleString = getResources().getStringArray(R.array.array_months);

        tvMonth = findViewById(R.id.tvMonth);

        // set current month
        Calendar calendar = DateUtil.getCurrentDate();
        Date currentDate = new Date(calendar.getTimeInMillis());
        calSelectedDate.setTime(currentDate);

        tvMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideCalc(30);
            }
        });

        week();
        dateOfWeek();
        updateWeeklyCalendar();

        findViewById(R.id.btnCalNav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });

        findViewById(R.id.btnCalSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(mContext, ActivitySearchMeet.class);
                intent.putExtra("calc_list", mCalendarDataList);
                startActivityForResult(intent, REQUEST_SEARCH_CONTACT);*/
            }
        });

        findViewById(R.id.btnCalToday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calSelectedDate = Calendar.getInstance();

                getApptData(false);

                updateWeeklyCalendar();

                showHideCalc(7);
            }
        });

        panelWeekDays = findViewById(R.id.panelWeekDays);
        calView = findViewById(R.id.calView);
        calView.setFirstDayOfWeek(Calendar.MONDAY);
        calView.setVisibility(View.GONE);
        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int monthOfYear, int dayOfMonth) {

                calSelectedDate.set(Calendar.YEAR, year);
                calSelectedDate.set(Calendar.MONTH, monthOfYear);
                calSelectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateWeeklyCalendar();
                showHideCalc(7);

                getApptData(false);
            }
        });

        findViewById(R.id.btnPrevDay).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                calSelectedDate.add(Calendar.DAY_OF_YEAR, -7);

                updateWeeklyCalendar();
                showHideCalc(7);

                calView.setDate(calSelectedDate.getTimeInMillis());

                getApptData(false);
            }
        });
        findViewById(R.id.btnNextDay).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                calSelectedDate.add(Calendar.DAY_OF_YEAR, 7);

                updateWeeklyCalendar();
                showHideCalc(7);

                calView.setDate(calSelectedDate.getTimeInMillis());

                getApptData(false);
            }
        });

        recyclerAppts = findViewById(R.id.recyclerAppts);
        // todo: InvalidSetHasFixedSize
        // recyclerAppts.setHasFixedSize(true);
        recyclerAppts.setLayoutManager(new LinearLayoutManager(mContext));
        calAdapter = new CalTimeAdapter(mContext,
                new CalTimeAdapter.RecyclerViewClickListener() {
                    @Override
                    public void onTimeClick(View view, int position) {
                        calSelectedDate.set(Calendar.HOUR_OF_DAY, position + 1);
                        calSelectedDate.set(Calendar.MINUTE, 0);


                    }

                    @Override
                    public void onTimelineClick(View view, int position) {
                        calSelectedDate.set(Calendar.HOUR_OF_DAY, position + 1);
                        calSelectedDate.set(Calendar.MINUTE, 0);

                    }

                    @Override
                    public void onApptClick(View view, int groupPos, int position, CalendarData.Data calData) {
                        /*for (int i = 0; i < contactModels.size(); i++) {
                            if (contactModels.get(i).getId() == calData.getLdbID()) {
                                setSelectedContactIndex(i, contactModels.get(i));
                                break;
                            }
                        }*/

                        Intent intent = new Intent(mContext, DriverStatusActivity.class);
                        intent.putExtra("tripInfo", calData);
                        startActivity(intent);
                    }

                    @Override
                    public void onApptLongClick(View view, int groupPos, int position, CalendarData.Data calData) {

                    }
                });
        recyclerAppts.setAdapter(calAdapter);

        // Check push and show relevant page
        checkPushAction(getIntent());

        // Check App Update
        // Start App Updates Daily Checker
        /*final String todayString = DateUtil.toStringFormat_13(new Date());
        if (!appSettings.getLastUpdateDate().equalsIgnoreCase(todayString)) {
            new CheckAppUpdateHelper(MainActivity.this, new CheckAppUpdateHelper.SearchRestaurantCallback() {
                @Override
                public void onFailed(String message) {
                    //showToastMessage(message);
                }

                @Override
                public void onSuccess(boolean haveUpdates) {
                    if (haveUpdates) {
                        // Check Permissions
                        if (checkPermissions(mContext, PERMISSION_REQUEST_GALLERY_STRING, true, PERMISSION_REQUEST_CODE_GALLERY)) {
                            downloadNewBuild();
                        }
                    }

                    appSettings.setLastUpdateDate(todayString);
                }
            }).execute();
        }*/
    }

    private void downloadNewBuild() {
        // Trigger App download Task Here
        File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        new DownloadFileFromURL(mContext, downloadFolder, "appiFare").execute("http://ilikeava.com/171/iFare.apk");
    }

    private void setTitle(String title) {
        toolbarTitle.setText(title);
    }

    BroadcastReceiver updateStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", 0);

            Log.e("update", String.format("Received update message. Type is %d", type));
            if (type == 1 || type == 2) {
                if (currentFragment != null && currentFragment instanceof DriverDeliveriesMapFragment) {
                    ((DriverDeliveriesMapFragment) currentFragment).awakeOnDuty();
                    Log.e("update", "Update Delivery Status --------------------------");
                }
            } else if (type == 4) {
                showFragment(FRAGMENT_SENDER_DELIVERY_MAP);
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        checkPushAction(intent);
    }

    private void checkPushAction(Intent intent) {

        if (intent == null) {
            showRelevantFragment();
            return;
        }

        // Check push and determine the first screen
        String pushType = intent.getStringExtra("pushtype");
        Log.e("Extra", "PMT:" + pushType);

        if (!TextUtils.isEmpty(pushType)) {
            showFragment(FRAGMENT_HOME);

            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            if (pushType.equals("1")) {
                // Driver received the bid accept message

                String dlgMsg = String.format("%s\n%s\n", title, message);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
                alertDialogBuilder.setTitle("Your bid was accepted");
                alertDialogBuilder.setMessage(dlgMsg)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                showFragment(FRAGMENT_SENDER_DELIVERY_MAP);
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else if (pushType.equals("2")) {
                // Driver received delivery request message

                /*String dlgMsg = String.format("%s\n%s\n\nDo you want to accept delivery offer?", title, message);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
                alertDialogBuilder.setTitle("Accept Delivery");
                alertDialogBuilder.setMessage(dlgMsg)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                showRelevantFragment();
                            }
                        }).
                        setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                showRelevantFragment();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();*/

                showRelevantFragment();
            } else if (pushType.equals("3")) {

                // Sender received the complete delivery message
                final JSONObject jsonObject = new JSONObject();

                try {
                    String extraData = intent.getStringExtra("pushData");
                    JSONObject jExtraData = new JSONObject(extraData);
                    String delID = jExtraData.getString("delID");

                    jsonObject.put("userid", appSettings.getUserId());
                    jsonObject.put("delid", delID);

                    jsonObject.put("statusid", "8199");
                    jsonObject.put("rate", "0");
                    jsonObject.put("q1", "1");
                    jsonObject.put("q2", "1");
                    jsonObject.put("q3", "1");

                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating_from_sender, null);

                    final android.app.AlertDialog inputDlg = new android.app.AlertDialog.Builder(mContext, R.style.AlertDialogTheme)
                            .setView(dialogView)
                            .setCancelable(false)
                            .create();

                    TextView tvTitle = (TextView) dialogView.findViewById(R.id.tvCompleteTitle);
                    TextView tvMessage = (TextView) dialogView.findViewById(R.id.tvCompleteMessage);
                    tvTitle.setText(title);
                    tvMessage.setText(message);

                    // Questions
                    RadioGroup groupOnTime = (RadioGroup) dialogView.findViewById(R.id.groupOnTime);
                    RadioGroup groupComplete = (RadioGroup) dialogView.findViewById(R.id.groupComplete);
                    RadioGroup groupAddFav = (RadioGroup) dialogView.findViewById(R.id.groupAddFav);

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

                    groupAddFav.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            try {
                                if (i == R.id.btnFavYes) {
                                    jsonObject.put("q3", "1");
                                } else if (i == R.id.btnFavNo) {
                                    jsonObject.put("q3", "0");
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

                    final EditText edtTips = (EditText) dialogView.findViewById(R.id.edtTips);

                    // Button Actions
                    dialogView.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            // Check current rating
                            if (ratingBar.getRating() == 0) {
                                tvErrorRate.setVisibility(View.VISIBLE);
                                return;
                            }

                            float tipsValue = 0;
                            try {
                                tipsValue = Float.parseFloat(edtTips.getText().toString());
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                            try {
                                jsonObject.put("rate", ratingBar.getRating());
                                jsonObject.put("tip", (int) tipsValue);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            inputDlg.dismiss();

                            // Request Rating Api
                            showProgressDialog();
                            RequestQueue queue = Volley.newRequestQueue(mContext);
                            /*StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelStatusRateDriv(jsonObject), new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    hideProgressDialog();

                                    if (response != null || !response.isEmpty()) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            String status = jsonObject.getString("status");
                                            if (jsonObject.getBoolean("status")) {
                                                *//*showAlert("Completed delivery! Good job.", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        showFragment(FRAGMENT_SENDER_DELIVERY_MAP);
                                                    }
                                                });*//*
                                                showFragment(FRAGMENT_SENDER_DELIVERY_MAP);
                                            } else {
                                                showToastMessage(jsonObject.getString("message"));
                                                showRelevantFragment();
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

                                    showRelevantFragment();
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (pushType.equals("4")) {
                /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
                alertDialogBuilder.setTitle(title);
                alertDialogBuilder.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                showFragment(FRAGMENT_SENDER_DELIVERY_MAP);
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();*/

                showFragment(FRAGMENT_SENDER_DELIVERY_MAP);
            } else {
                showRelevantFragment();
            }
        } else {
            showRelevantFragment();
        }
    }

    private void showRelevantFragment() {

        showFragment(FRAGMENT_HOME);

        //showFragment(FRAGMENT_DRIVER_DELIVERY_MAP);

        /*if (!TextUtils.isEmpty(appSettings.getDriverID())) {
            showFragment(FRAGMENT_DRIVER_DELIVERY_MAP);
        } else {
            // Original Logic
            // showFragment(FRAGMENT_SENDER_DELIVERY_MAP);

            showFragment(FRAGMENT_SENDER_NEW_DELIVERY);

            //// Start new Delivery Activity
            //Intent newDeliveryIntent = new Intent(mContext, NewDeliveryActivity.class);
            //newDeliveryIntent.putStringArrayListExtra("fav_tokens", new ArrayList<String>());
            //startActivityForResult(newDeliveryIntent, 0);
        }*/
    }

    public void showFragment(int fragmentID) {

        // Create fragment and give it an argument specifying the article it should show
        BaseFragment newFragment = null;
        String title = getString(R.string.app_name);
        if (fragmentID == FRAGMENT_HOME) {
            if (currentFragment != null && currentFragment instanceof HomeFragment) {
                return;
            }

            newFragment = HomeFragment.newInstance("HOME");
            title = "Dashboard";
        } else if (fragmentID == FRAGMENT_SENDER_DELIVERY_LIST) {
            if (currentFragment != null && currentFragment instanceof SenderDeliveriesListFragment) {
                return;
            }

            newFragment = SenderDeliveriesListFragment.newInstance("Sender_Delivery_List");
            title = "Sender Delivery List";
        } else if (fragmentID == FRAGMENT_SENDER_DELIVERY_MAP) {
            if (currentFragment != null && currentFragment instanceof SenderDeliveriesMapFragment) {
                return;
            }

            newFragment = SenderDeliveriesMapFragment.newInstance("Sender_Delivery_Map");
            title = "Sender Deliveries In Map";
        } else if (fragmentID == FRAGMENT_SENDER_NEW_DELIVERY) {
            if (currentFragment != null && currentFragment instanceof SenderNewDeliveryFragment) {
                return;
            }

            newFragment = SenderNewDeliveryFragment.newInstance("Sender_New_Delivery");
            title = "Enroll Now";   //""New Delivery";
        } else if (fragmentID == FRAGMENT_DRIVER_DELIVERY_LIST) {
            if (currentFragment != null && currentFragment instanceof DriverDeliveriesListFragment) {
                return;
            }

            newFragment = DriverDeliveriesListFragment.newInstance("Driver_Delivery_List");
            title = "Driver Deliveries";
        } else if (fragmentID == FRAGMENT_DRIVER_DELIVERY_MAP) {
            if (currentFragment != null && currentFragment instanceof DriverDeliveriesMapFragment) {
                return;
            }

            newFragment = DriverDeliveriesMapFragment.newInstance("Driver_Delivery_Map");
            title = "Deliveries";
        }

        if (newFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
            currentFragment = newFragment;

            setTitle(title);
        }
    }

    public void showDriverDeliveryInfo(DeliveryItem driverItem) {
        BaseFragment newFragment = DriverDeliveryInfoFragment.newInstance("DriverDelivery", driverItem);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
        currentFragment = newFragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(updateStatusReceiver);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.btnSetStatus) {
            startActivity(new Intent(mContext, DriverStatusActivity.class));
        }
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

            String curUserName = mCurrentUserInfo.getName();

            for (String device : deviceList) {
                try {
                    try {
                        // Prepare JSON containing the FCM message content. What to send and where to send.
                        JSONObject jGcmData = new JSONObject();
                        JSONObject jData = new JSONObject();

                        if (isDriver) {
                            String message = getString(R.string.format_push_for_drivers);
                            message = message.replace("1", curUserName);
                            jData.put("message", message);
                        } else {
                            String message = getString(R.string.format_push_for_bidders);
                            message = message.replace("1", curUserName);
                            jData.put("message", message);
                        }

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

        // Check All Permission was granted
        boolean bAllGranted = true;
        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                bAllGranted = false;
                break;
            }
        }

        if (bAllGranted && requestCode == PERMISSION_REQUEST_CODE_LOCATION) {
        } else {
            showAlert(R.string.request_permission_hint);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (currentFragment != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            logoutUser();
        } else if (id == R.id.nav_home) {
            showFragment(FRAGMENT_HOME);
        } else if (id == R.id.nav_nemt_schedule) {
            drawer.openDrawer(navigationViewCalendar);
        } else if (id == R.id.nav_taxi_schedule) {
            drawer.openDrawer(navigationViewCalendar);
        } else if (id == R.id.nav_notifications) {
            startActivity(new Intent(mContext, NotificationsActivity.class));
        } else if (id == R.id.nav_driverdeliveries) {
            // --- Driver Options -------------------------------------------------
            // Driver Deliveries with map
            if (TextUtils.isEmpty(appSettings.getDriverID())) {
                showAlert(R.string.error_approve_as_driver);
            } else {
                showFragment(FRAGMENT_DRIVER_DELIVERY_MAP);
            }
        } else if (id == R.id.nav_listofdrvdeliveries) {
            // List of Driver Deliveries
            if (TextUtils.isEmpty(appSettings.getDriverID())) {
                showAlert(R.string.error_approve_as_driver);
            } else {
                showFragment(FRAGMENT_DRIVER_DELIVERY_LIST);
            }
        } else if (id == R.id.nav_makenewdelivery) {
            // --- Sender Options -------------------------------------------------
            showFragment(FRAGMENT_SENDER_NEW_DELIVERY);
        } else if (id == R.id.nav_existings) {
            showFragment(FRAGMENT_SENDER_DELIVERY_MAP);
        } else if (id == R.id.nav_listofsenderdeliveries) {
            showFragment(FRAGMENT_SENDER_DELIVERY_LIST);
        } else if (id == R.id.nav_deposit) {
            startActivity(new Intent(mContext, EarnedActivity.class));
        } else if (id == R.id.nav_transactions) {
            startActivity(new Intent(mContext, TransactionHistoryActivity.class));
        } else if (id == R.id.nav_profile) {
            //startActivity(new Intent(mContext, ProfileActivity.class));
            startActivity(new Intent(mContext, ActivityEditProfile.class));
        } else if (id == R.id.nav_video) {
            //startActivity(new Intent(mContext, ProfileActivity.class));
            startActivity(new Intent(mContext, ActivityAddVideo.class));
        } else if (id == R.id.nav_password) {
            startActivity(new Intent(mContext, ActivityChangePassword.class));
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_send) {
            startActivity(new Intent(mContext, SupportActivity.class));
        } else if (id == R.id.nav_reports) {
            startActivity(new Intent(mContext, ActivityReport.class));
        } else if (id == R.id.nav_qr) {
            startActivity(new Intent(mContext, ActivityAddQRCode.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.END);
        return true;
    }

    private void logoutUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        alertDialogBuilder.setTitle("Confirm logout");
        alertDialogBuilder.setMessage("Would you like to logout?")
                .setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                        logout(false);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    boolean isFinish = false;

    class FinishTimer extends CountDownTimer {
        public FinishTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            isFinish = true;
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            isFinish = false;
        }
    }

    private void week() {
        View view = (View) findViewById(R.id.symbolOfWeek);
        TextView mon = (TextView) view.findViewById(R.id.mon);
        TextView tue = (TextView) view.findViewById(R.id.tue);
        TextView wed = (TextView) view.findViewById(R.id.wed);
        TextView thu = (TextView) view.findViewById(R.id.thu);
        TextView fri = (TextView) view.findViewById(R.id.fri);
        TextView sat = (TextView) view.findViewById(R.id.sat);
        TextView sun = (TextView) view.findViewById(R.id.sun);

        dayLabelTextViews.add(mon);
        dayLabelTextViews.add(tue);
        dayLabelTextViews.add(wed);
        dayLabelTextViews.add(thu);
        dayLabelTextViews.add(fri);
        dayLabelTextViews.add(sat);
        dayLabelTextViews.add(sun);

        // Set Font Size
        for (TextView textView : dayLabelTextViews) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            textView.setTextColor(Color.BLACK);
        }

        ImageView monSelected = (ImageView) view.findViewById(R.id.monSelected);
        ImageView tueSelected = (ImageView) view.findViewById(R.id.tueSelected);
        ImageView wedSelected = (ImageView) view.findViewById(R.id.wedSelected);
        ImageView thuSelected = (ImageView) view.findViewById(R.id.thuSelected);
        ImageView friSelected = (ImageView) view.findViewById(R.id.friSelected);
        ImageView satSelected = (ImageView) view.findViewById(R.id.satSelected);
        ImageView sunSelected = (ImageView) view.findViewById(R.id.sunSelected);

        monSelected.setVisibility(View.GONE);
        tueSelected.setVisibility(View.GONE);
        wedSelected.setVisibility(View.GONE);
        thuSelected.setVisibility(View.GONE);
        friSelected.setVisibility(View.GONE);
        satSelected.setVisibility(View.GONE);
        sunSelected.setVisibility(View.GONE);
    }

    private void dateOfWeek() {
        View view = (View) findViewById(R.id.dateOfWeek);

        TextView mon = (TextView) view.findViewById(R.id.mon);
        TextView tue = (TextView) view.findViewById(R.id.tue);
        TextView wed = (TextView) view.findViewById(R.id.wed);
        TextView thu = (TextView) view.findViewById(R.id.thu);
        TextView fri = (TextView) view.findViewById(R.id.fri);
        TextView sat = (TextView) view.findViewById(R.id.sat);
        TextView sun = (TextView) view.findViewById(R.id.sun);

        dateTextViews.add(mon);
        dateTextViews.add(tue);
        dateTextViews.add(wed);
        dateTextViews.add(thu);
        dateTextViews.add(fri);
        dateTextViews.add(sat);
        dateTextViews.add(sun);

        ImageView monSelected = (ImageView) view.findViewById(R.id.monSelected);
        ImageView tueSelected = (ImageView) view.findViewById(R.id.tueSelected);
        ImageView wedSelected = (ImageView) view.findViewById(R.id.wedSelected);
        ImageView thuSelected = (ImageView) view.findViewById(R.id.thuSelected);
        ImageView friSelected = (ImageView) view.findViewById(R.id.friSelected);
        ImageView satSelected = (ImageView) view.findViewById(R.id.satSelected);
        ImageView sunSelected = (ImageView) view.findViewById(R.id.sunSelected);

        monSelected.setVisibility(View.INVISIBLE);
        tueSelected.setVisibility(View.INVISIBLE);
        wedSelected.setVisibility(View.INVISIBLE);
        thuSelected.setVisibility(View.INVISIBLE);
        friSelected.setVisibility(View.INVISIBLE);
        satSelected.setVisibility(View.INVISIBLE);
        sunSelected.setVisibility(View.INVISIBLE);


        selectedImageViews.add(monSelected);
        selectedImageViews.add(tueSelected);
        selectedImageViews.add(wedSelected);
        selectedImageViews.add(thuSelected);
        selectedImageViews.add(friSelected);
        selectedImageViews.add(satSelected);
        selectedImageViews.add(sunSelected);
    }

    private void showHideCalc(int showDays) {
        Calendar today = Calendar.getInstance();
        int todayIndex = 0/*today.get(Calendar.DAY_OF_WEEK) - today.getFirstDayOfWeek()*/;

        if (showDays == 1) {
            calView.setVisibility(View.GONE);
            panelWeekDays.setVisibility(View.VISIBLE);


            for (int i = 0; i < dateTextViews.size(); i++) {
                if (i == todayIndex) {
                    dayLabelTextViews.get(i).setVisibility(View.VISIBLE);
                    dateTextViews.get(i).setVisibility(View.VISIBLE);
                    selectedImageViews.get(i).setVisibility(View.VISIBLE);

                    dateTextViews.get(i).setTextColor(getResources().getColor(R.color.white));
                    selectedImageViews.get(i).setVisibility(View.VISIBLE);
                    dateTextViews.get(i).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.selector_date_circle_white);
                } else {
                    dayLabelTextViews.get(i).setVisibility(View.INVISIBLE);
                    dateTextViews.get(i).setVisibility(View.INVISIBLE);
                    selectedImageViews.get(i).setVisibility(View.INVISIBLE);

                    dateTextViews.get(i).setTextColor(getResources().getColor(R.color.app_grey_dark));
                    selectedImageViews.get(i).setVisibility(View.INVISIBLE);
                    dateTextViews.get(i).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.rectangle);
                }
            }
        } else if (showDays == 7) {
            calView.setVisibility(View.GONE);
            panelWeekDays.setVisibility(View.VISIBLE);


            for (int i = 0; i < dateTextViews.size(); i++) {
                dayLabelTextViews.get(i).setVisibility(View.VISIBLE);
                dateTextViews.get(i).setVisibility(View.VISIBLE);
            }
        } else if (showDays == 30) {
            calView.setVisibility(View.VISIBLE);
            panelWeekDays.setVisibility(View.GONE);
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);

    }

    private void updateWeeklyCalendar() {
        // Show month
        tvMonth.setText(monthTitleString[calSelectedDate.get(Calendar.MONTH) + 1]);

        // Update Weekly Calendar
        for (int i = 0; i < dateTextViews.size(); i++) {
            dayLabelTextViews.get(i).setVisibility(View.VISIBLE);
            dateTextViews.get(i).setVisibility(View.VISIBLE);

            dateTextViews.get(i).setText(getWeekRange(calSelectedDate, i));
            dateTextViews.get(i).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.rectangle);

            Calendar calItemDate = getWeekRangeCalendar(calSelectedDate, i);
            if (isSameDay(calItemDate, calSelectedDate)) {

                dateTextViews.get(i).setTextColor(getResources().getColor(R.color.white));
                selectedImageViews.get(i).setVisibility(View.VISIBLE);
                dateTextViews.get(i).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.selector_date_circle_white);
            } else {
                dateTextViews.get(i).setTextColor(getResources().getColor(R.color.app_grey_dark));
                selectedImageViews.get(i).setVisibility(View.INVISIBLE);
                dateTextViews.get(i).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.rectangle);
            }

            dateTextViews.get(i).setTag(i);
            dateTextViews.get(i).setOnClickListener(weeklyCalendarItemClickListener);
        }
    }

    View.OnClickListener weeklyCalendarItemClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            int dayIndexInWeek = (int) view.getTag();
            paintSelected(calSelectedDate, dateTextViews, selectedImageViews, 7, dayIndexInWeek);
            calSelectedDate = getWeekRangeCalendar(calSelectedDate, dayIndexInWeek);

            getApptData(false);

            calView.setDate(calSelectedDate.getTimeInMillis());
        }
    };

    private void paintSelected(Calendar calendar, List<TextView> textView, List<ImageView> imageView, int size, int selected) {
        for (int i = 0; i < size; i++) {
            //textView.get(i).setText(getWeekRange(calendar, i));
            textView.get(i).setTextColor(getResources().getColor(R.color.app_grey_dark));
            textView.get(i).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.rectangle);
            imageView.get(i).setVisibility(View.INVISIBLE);

            if (i == selected) {
                textView.get(i).setTextColor(getResources().getColor(R.color.white));
                imageView.get(i).setVisibility(View.VISIBLE);
                textView.get(i).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.selector_date_circle_white);
            }
        }
    }

    private String getWeekRange(Calendar calendar, int addNext) {
        Calendar first = (Calendar) calendar.clone();
        first.add(Calendar.DAY_OF_WEEK,
                first.getFirstDayOfWeek() - first.get(Calendar.DAY_OF_WEEK));
        Calendar last = (Calendar) first.clone();

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            last.add(Calendar.DAY_OF_YEAR, addNext - 7 + 1);
        } else {
            last.add(Calendar.DAY_OF_YEAR, addNext + 1);
        }

        SimpleDateFormat df = new SimpleDateFormat("d");
        return df.format(last.getTime());

       /* Calendar first = (Calendar) calendar.clone();
        first.set(Calendar.DAY_OF_WEEK,
                (addNext + 2) % 7);
        SimpleDateFormat df = new SimpleDateFormat("d");
        return df.format(first.getTime());*/
    }

    private Calendar getWeekRangeCalendar(Calendar calendar, int addNext) {
        Calendar first = (Calendar) calendar.clone();
        first.add(Calendar.DAY_OF_WEEK,
                first.getFirstDayOfWeek() - first.get(Calendar.DAY_OF_WEEK));
        Calendar last = (Calendar) first.clone();

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            last.add(Calendar.DAY_OF_YEAR, addNext - 7 + 1);
        } else {
            last.add(Calendar.DAY_OF_YEAR, addNext + 1);
        }

        return last;

        /*Calendar first = (Calendar) calendar.clone();
        first.set(Calendar.DAY_OF_WEEK,
                (addNext + 2) % 7);
        return first;*/
    }

    private void getApptData(final boolean updateData) {

        if (updateData) {
            mCalendarDataMonths.clear();
        }

        final String monthString = DateUtil.dateToString(calSelectedDate.getTime(), "yyyy-MM");
        // Already loaded month date, then doesn't reload month date
        if (mCalendarDataMonths.contains(monthString)) {
            updateItemList();
            return;
        }

        // Load new month date
        Calendar calMonthStart = (Calendar) calSelectedDate.clone();
        calMonthStart.set(Calendar.DAY_OF_MONTH, calMonthStart.getActualMinimum(Calendar.DAY_OF_MONTH));

        Calendar calMonthEnd = (Calendar) calSelectedDate.clone();
        calMonthEnd.set(Calendar.DAY_OF_MONTH, calMonthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));

        // Get From Server
        if (getLocation()) {
            HashMap<String, String> params = new HashMap<>();
            String baseUrl = BaseFunctions.getBaseUrl(this,
                    "appGet",
                    BaseFunctions.APP_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    mMyApp.getAndroidId());
            String extraParams =
                    "&mode=" + "myTripsWeekly" +
                            "&driverID=" + appSettings.getDriverID() +
                            "&tripID=" + 1 +
                            "&industryID=" + 487 +
                            "&DateStart=" + DateUtil.toStringFormat_12(calMonthStart.getTime()) +
                            "&DateEnd=" + DateUtil.toStringFormat_12(calMonthEnd.getTime());
            baseUrl += extraParams;
            Log.e("myTripsWeekly", baseUrl);

            GoogleCertProvider.install(mContext);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //hideProgressDialog();

                    Log.e("myTripsWeekly", "onSuccess: " + response);

                    try {

                        JSONArray responseArray = new JSONArray(response);

                        JSONObject responseObject = responseArray.getJSONObject(0);
                        if (responseObject.has("status") && !responseObject.getBoolean("status")) {
                            //showAlert(responseObject.getString("msg"));
                            msg(responseObject.getString("msg"));
                        } else {
                            if (updateData) {
                                mCalendarDataList.clear();
                            }
                            Gson gson = new Gson();
                            for (int i = 0; i < responseArray.length(); i++) {
                                CalendarData.Data newItem = gson.fromJson(responseArray.getString(i), CalendarData.Data.class);

                                if (!mCalendarDataList.contains(newItem)) {
                                    mCalendarDataList.add(newItem);
                                }
                            }

                            CalendarData.setCalendarDataList(mCalendarDataList);

                            updateItemList();

                            mCalendarDataMonths.add(monthString);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    networkErrorHandle(mContext, error);
                    //hideProgressDialog();
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
            VolleySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
        }
    }

    private void updateItemList() {
        //[{"CALID":1,"CALOrderID":0,"CALAmt":0.00,"CALSetByID":1434741,"Title":"Good ","ApptStartTime":"2021-08-26T21:00:00","ApptEndTime":"2021-08-26T21:30:00","apptLat":0.0000000000,"apptLon":0.0000000000,"address":"","meetingID":0,"noteID":0,"BuyerID":0,"Co":"","DetailID":0,"DetailStatusID":0,"Qty":0,"Name":"","Price":0.00,"TotPrice":0.00},{"CALID":4,"CALOrderID":0,"CALAmt":0.00,"CALSetByID":1434741,"Title":"Meet for work ","ApptStartTime":"2021-08-27T07:00:00","ApptEndTime":"2021-08-27T07:30:00","apptLat":0.0000000000,"apptLon":0.0000000000,"address":"Df","meetingID":0,"noteID":0,"BuyerID":0,"Co":"","DetailID":0,"DetailStatusID":0,"Qty":0,"Name":"","Price":0.00,"TotPrice":0.00}]

        Map<String, ArrayList<CalendarData.Data>> mapData = new HashMap<>();
        for (CalendarData.Data item : mCalendarDataList) {
            Date dateItem = DateUtil.parseDataFromFormat12(item.getPuTime().replace("T", " "));
            Calendar calItem = Calendar.getInstance();
            calItem.setTime(dateItem);

            if (isSameDay(calSelectedDate, calItem)) {
                String hourInfo = DateUtil.toStringFormat_30(dateItem).toUpperCase();

                ArrayList<CalendarData.Data> hourItems = mapData.get(hourInfo);

                if (hourItems == null) {
                    hourItems = new ArrayList<>();
                    mapData.put(hourInfo, hourItems);
                }

                hourItems.add(item);
            }
        }

        calAdapter.notifyData(mapData);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            if (!isFinish) {

                showToastMessage(R.string.finish_message);
                FinishTimer timer = new FinishTimer(2000, 1);
                timer.start();
            } else {
                finish();
            }
        }
    }
}
