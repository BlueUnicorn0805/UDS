package hawaiiappbuilders.c;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.protobuf.StringValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.messaging.FCMHelper;
import hawaiiappbuilders.c.messaging.TokenGetter;
import hawaiiappbuilders.c.model.CalendarData;
import hawaiiappbuilders.c.model.DeliveryItem;
import hawaiiappbuilders.c.model.FCMTokenData;
import hawaiiappbuilders.c.model.StartVideoModel;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.PhonenumberUtils;
import hawaiiappbuilders.c.videocalls.AgoraTokenModel;
import hawaiiappbuilders.c.videocalls.CallViewActivity;
import hawaiiappbuilders.c.videocalls.OutCallViewActivity;
import hawaiiappbuilders.c.view.OpenSansBoldTextView;
import hawaiiappbuilders.c.view.OpenSansButton;
import hawaiiappbuilders.c.view.OpenSansEditText;
import hawaiiappbuilders.c.view.OpenSansTextView;
import hawaiiappbuilders.c.webutils.VolleySingleton;

public class DriverStatusActivity extends BaseActivity implements
        View.OnClickListener {
    Spinner spinnerMessageTitle;
    String[] messageNames;
    String[] messageValues;
    String selectedMessageValue = "";
    OpenSansEditText edtPreMessage;
    OpenSansEditText edMessage;
    OpenSansBoldTextView minus;
    OpenSansBoldTextView plus;
    OpenSansBoldTextView ETA_num;
    CheckBox checkBox;
    TextView tvTripID;
    TextView tvClient;
    TextView tvClientAddr;
    TextView tvClientCSZ;
    RelativeLayout rlSendMsg;
    RelativeLayout rlVideoCall;
    ImageView ivDropped;
    ImageView ivEmergency;

    OpenSansButton btnSend;
    OpenSansButton btnClose;

    String SelectMessage;
    String agoraChannelName = "";
    String agoraToken = "";

    CalendarData.Data tripInfo;
    StartVideoModel videoModel;

    int EtaNum = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_status);

        rlSendMsg = (RelativeLayout) findViewById(R.id.rl_sendmsg);
        rlVideoCall = (RelativeLayout) findViewById(R.id.rl_videocall);
        ivDropped = (ImageView) findViewById(R.id.iv_dropped_off);
        ivEmergency = (ImageView) findViewById(R.id.iv_emergency);

        // Get the Trip Info
        Intent intent = getIntent();
        tripInfo = intent.getParcelableExtra("tripInfo");
        if (tripInfo == null) {
            finish();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTripID = findViewById(R.id.tvTripID);
        tvTripID.setText(String.format("TripID: %d", tripInfo.getTripID()));

        tvClient = findViewById(R.id.tvClient);
        tvClient.setText(String.format("Client:\n%s %s", tripInfo.getFN(), tripInfo.getLN()));
        tvClient.setOnClickListener(this);

        tvClientAddr = findViewById(R.id.tvClientAddr);
        String[] arrayString = tripInfo.getAddressPU().split(",");

        if (arrayString.length > 2)
            tvClientAddr.setText(arrayString[0] + "\n" + arrayString[1] + ", " + arrayString[2]);
        else if (arrayString.length > 1)
            tvClientAddr.setText(arrayString[0] + "\n" + arrayString[1]);
        else
            tvClientAddr.setText(arrayString[0]);

        tvClientCSZ = findViewById(R.id.tvClientCSZ);
        String csvPu = tripInfo.getCszPU().trim();
        if (csvPu.equals(",")) {
            csvPu = "";
        }
        if (TextUtils.isEmpty(csvPu)) {
            tvClientCSZ.setVisibility(View.GONE);
        } else {
            tvClientCSZ.setText(csvPu);
        }

        findViewById(R.id.rl_videocall).setOnClickListener(this);
        findViewById(R.id.rl_sendmsg).setOnClickListener(this);
        findViewById(R.id.btnDroppedOff).setOnClickListener(this);
        findViewById(R.id.btnEmergency).setOnClickListener(this);

        findViewById(R.id.btnDirection).setOnClickListener(this);
        findViewById(R.id.btnCall).setOnClickListener(this);

        findViewById(R.id.btnComplete).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);


    }

    public void MessageSend(){
        final Map<String, String> params = new HashMap<>();

        String baseUrl = BaseFunctions.getBaseUrl(mContext,
                "arrived",
                BaseFunctions.APP_FOLDER,
                getUserLat(),
                getUserLon(),
                mMyApp.getAndroidId());

        String extraParams = "&mode=" + selectedMessageValue +
                "&miscMsg=" +  (String) Objects.requireNonNull(edtPreMessage.getText()).toString() +
                "&driverID=" + appSettings.getDriverID() +
                "&tripID=" + tripInfo.getTripID()
                ;
        baseUrl += extraParams;

        Log.d("@request","" + extraParams);
        Log.d("@request Base","" + baseUrl);

        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }
        Log.e("@arrived", fullParams);

        showProgressDialog();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest sr = new StringRequest(Request.Method.GET, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.d("@@@@@@@@@@@@@@", "Success!");
                if (response != null || !response.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;

                        if (jsonObject.optBoolean("status")) {


                            finish();

                            showToastMessage("Successfully saved business information");
                        } else {
                            showAlert(jsonObject.getString("message"));
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
                showAlert("@Request Error!, Please check network.");

                //showMessage(error.getMessage());
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

    public void showPreMessage(boolean val){
        if(val){
            edtPreMessage.setText(SelectMessage + " ETA: " + EtaNum + "m");
        } else {
            edtPreMessage.setText(" ETA: " + EtaNum + "m");
        }
    }

    private void SendMessageDialog(){
        Dialog sendMessageDlg = new Dialog(DriverStatusActivity.this);
        sendMessageDlg.setContentView(R.layout.dialog_message);

//        btnSend = sendMessageDlg.findViewById(R.id.btnSend);
//
//        btnSend.setOnClickListener(view -> {
//            MessageSend();
//        });
        // Spinner
        spinnerMessageTitle = sendMessageDlg.findViewById(R.id.spinnerMessageTitle);
        edtPreMessage = sendMessageDlg.findViewById(R.id.edtPreMessage);
        edMessage = sendMessageDlg.findViewById(R.id.edMessage);
        edMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                SelectMessage = (String) Objects.requireNonNull(edMessage.getText()).toString();
                showPreMessage(checkBox.isChecked());
//                addressUpdateHandler.removeMessages(1);
//                addressUpdateHandler.sendEmptyMessageDelayed(1, 2500);
            }
        });
        minus = sendMessageDlg.findViewById(R.id.minus);
        plus = sendMessageDlg.findViewById(R.id.plus);
        ETA_num = sendMessageDlg.findViewById(R.id.ETA_num);
        checkBox = sendMessageDlg.findViewById(R.id.radioActive);
        checkBox.setOnClickListener(view -> {
//            int viewID = view.getId();
//            if (viewID == R.id.radioActive) {
//
//            }
            Log.d("@Checked", "checkedStatus:::: " + checkBox.isChecked());
//            checkBox.setChecked(!checkBox.isChecked());
            showPreMessage(checkBox.isChecked());
        });
        minus.setOnClickListener(v-> {
            Log.d("@Minus", "Minus");
            if(EtaNum <= 0){
                EtaNum =0;
                minus.setEnabled(false);
            }else {
                EtaNum--;
            }
            ETA_num.setText(String.valueOf(EtaNum));
            showPreMessage(checkBox.isChecked());
        });

        plus.setOnClickListener(v-> {

            Log.d("@Plus", "Plus");
//            if(EtaNum == 0){
                EtaNum++;
                minus.setEnabled(true);
//            }
            ETA_num.setText(String.valueOf(EtaNum));
            showPreMessage(checkBox.isChecked());
        });

        int defaultSpinnerIndex = 0;
        messageNames = getResources().getStringArray(R.array.spinner_message);
        messageValues = new String[messageNames.length];
        for (int i = 0; i < messageNames.length; i++) {
            String timezoneInfo = messageNames[i];
            String[] spliteValues = timezoneInfo.split("=");
            messageNames[i] = spliteValues[0];
            messageValues[i] = spliteValues[1];

//            if (messageValues[i].equals(appSettings.getUTC())) {
//                defaultSpinnerIndex = i;
//            }
        }
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(sendMessageDlg.getContext(), R.layout.simple_spinner_item, messageNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        spinnerMessageTitle.setAdapter(spinnerArrayAdapter);
        spinnerMessageTitle.setSelection(defaultSpinnerIndex);

        spinnerMessageTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMessageValue = messageValues[position];
                SelectMessage = messageNames[position];
                edMessage.setText(SelectMessage);
//                spinnerMessageTitle.set
                showPreMessage(checkBox.isChecked());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMessageValue = messageValues[spinnerMessageTitle.getSelectedItemPosition()];
            }
        });

        sendMessageDlg.findViewById(R.id.btnSend).setOnClickListener(v -> {
            MessageSend();
            sendMessageDlg.dismiss();
        });
        sendMessageDlg.findViewById(R.id.btnClose).setOnClickListener(v -> {
            sendMessageDlg.dismiss();
        });
        sendMessageDlg.show();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tvClient) {
            Intent intent = new Intent(mContext, ClientDetailsActivity.class);
            intent.putExtra("tripInfo", tripInfo);
            startActivity(intent);
        } else if (viewId == R.id.rl_sendmsg) {
            Log.d("@sendMessageDlg", "Come on in!!!!!!!");
            SendMessageDialog();
        } else if (viewId == R.id.rl_videocall) {
//            sendPushForOutCall();
            startVideoCallApi(tripInfo);
        } else if (viewId == R.id.btnDroppedOff) {
            ivDropped.setImageResource(R.drawable.ic_status_arrived);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);

            builder.setMessage("Are you sure you want to proceed?")
                    .setCancelable(false)
                    // Set the action buttons
                    .setPositiveButton("Yes", (dialog, id) -> {
                        setStatus(3);
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            builder.create().show();

        } else if (viewId == R.id.btnEmergency) {
            ivEmergency.setImageResource(R.drawable.ic_status_arrived);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);

            builder.setMessage("Are you sure you want to proceed?")
                    .setCancelable(false)
                    // Set the action buttons
                    .setPositiveButton("Yes", (dialog, id) -> {
                        setStatus(4);
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            builder.create().show();

        } else if (viewId == R.id.btnComplete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);

            builder.setMessage("Would you like to complete this trip?")
                    .setCancelable(false)
                    // Set the action buttons
                    .setPositiveButton("Yes", (dialog, id) -> {
                        setStatus(7);
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            builder.create().show();

        } else if (viewId == R.id.btnCancel) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);

            builder.setMessage("Would you like to cancel this trip?")
                    .setCancelable(false)
                    // Set the action buttons
                    .setPositiveButton("Yes", (dialog, id) -> {
                        setStatus(10);
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            builder.create().show();

        } else if (viewId == R.id.btnDirection) {
            String address = String.format("%s %s", tripInfo.getAddressPU(), tripInfo.getCszPU());
            if (!TextUtils.isEmpty(address)) {
                //Uri gmmIntentUri = Uri.parse(String.format("geo:%f,%f", mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%s&mode=d", address));

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    showToastMessage("Please install google map");
                }
            } else {
                showToastMessage("No Address Info");
            }
        } else if (viewId == R.id.btnCall) {
            if (!TextUtils.isEmpty(tripInfo.getCP())) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + tripInfo.getCP()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        }
    }

    private void setStatus(int mode) {
        if (getLocation()) {
            String baseUrl = BaseFunctions.getBaseUrl(mContext, "arrived", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());
            // params.put("UTC", String.valueOf(0/*appSettings.getCUTC()*/));
            String extraParams = "&mode=" + mode +
                    "&industryID=" + "80" +
                    "&promoid=" + "0" +
                    "&Amt=" + "0" +
                    "&mins=" + "0" +
                    "&OrderID=" + "0" +
                    "&tolat=" + "0" +
                    "&tolon=" + "0" +
                    "&tripID=" + tripInfo.getTripID() +
                    "&driverID=" + appSettings.getDriverID() +
                    "&miles=" + "0" +
                    "&tip=" + "0";
            baseUrl += extraParams;

            Log.e("arrived", baseUrl);

            showProgressDialog();
            GoogleCertProvider.install(mContext);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();
                    Log.e("900", response);

                    if (!TextUtils.isEmpty(response)) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                            } else if (jsonObject.has("status") && jsonObject.getBoolean("status")) {
                            }

                            showToastMessage(jsonObject.getString("msg"));

                            switch (mode) {
                                case 2:
                                case 3:
                                case 7:
                                case 10:
                                    finish();
                                    break;
                            }
                            //finish();
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
            VolleySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
        }
    }

    private void sendPushForOutCall() {

        final Map<String, String> params = new HashMap<>();

        GpsTracker gpsTracker = new GpsTracker(getApplicationContext());
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
                "&TokenMLID=" + tripInfo.getClientID();
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
                        JSONArray dataArray = new JSONArray(response);
                        String curUserName = String.format("%s %s", appSettings.getFN(), appSettings.getLN()).trim();
                        String title = String.format("Call from %s", curUserName);

                        TokenGetter tokenGetter = new TokenGetter(appSettings.getUserId(), mContext, (BaseActivity) mContext);
                        ArrayList<FCMTokenData> tokenList = tokenGetter.getTokenList(dataArray);
                        if (!tokenList.isEmpty()) {
                            JSONObject payload = new JSONObject();
                            payload.put("driverName", curUserName);
                            payload.put("driverID", appSettings.getDriverID());
                            payload.put("userID", appSettings.getUserId());
                            payload.put("sender_token", appSettings.getDeviceToken());
//                            payload.put("agora_channel_name", appSettings.getUserId() + "-" + tripInfo.getClientID());
                            payload.put("agora_channel_name", "vcall-3");
//                            payload.put("agora_token", "agora_token");
                            payload.put("agora_token", "007eJxTYND2LDzoNe9YfVLZrK3Czz6J71W631Nw7O+ijM87rrD94dVVYDBLNTZJMrdITk0xNzOxSLa0SEpMM0xKtTQ2MjA0TzNJPWvTm9YQyMjAf2YeKyMDBIL47AxlyYk5ObrGDAwAPxEiHA==");
                            payload.put("title", title);

                            Intent intent = new Intent(mContext, OutCallViewActivity.class);
                            intent.putExtra("receiver_token", tokenList.get(0).getToken());
//                            intent.putExtra("agora_channel_name", appSettings.getUserId() + "-" + tripInfo.getClientID());
                            intent.putExtra("agora_channel_name", "vcall-3");
//                            intent.putExtra("agora_token", "agora_token");
                            intent.putExtra("agora_token", "007eJxTYND2LDzoNe9YfVLZrK3Czz6J71W631Nw7O+ijM87rrD94dVVYDBLNTZJMrdITk0xNzOxSLa0SEpMM0xKtTQ2MjA0TzNJPWvTm9YQyMjAf2YeKyMDBIL47AxlyYk5ObrGDAwAPxEiHA==");
                            startActivity(intent);

                            tokenGetter.sendPushNotification(mContext, tokenList, FCMHelper.PT_VIDEO_CALL_REQUEST, payload);
                        } else {
                            showAlert("Sender has no any device to contact.");
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


    private void startVideoCallApi(CalendarData.Data contactInfo) {
        try {
            showProgressDialog();
            String baseUrl = BaseFunctions.getBaseUrl(this,
                    "StartVC",
                    BaseFunctions.APP_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    ((MyApplication) getApplication()).getAndroidId());
            try {

                agoraChannelName = appSettings.getUserId() + "" + contactInfo.getClientID();
                agoraToken = "";
                RequestQueue requestTokenQueue = Volley.newRequestQueue(this);
                String getAgoraUrl = "https://uniway-agora-server.onrender.com/rtcToken?channelName=" + agoraChannelName;
                StringRequest stringAgoraRequest = new StringRequest(Request.Method.GET, getAgoraUrl,
                        tokenResponse -> {
                            // Handle response

                            try {
                                Log.d("@GET AGOToken::: response",  "" + tokenResponse);
                                Gson tokenGson = new Gson();
                                AgoraTokenModel[] listOfTokens = tokenGson.fromJson(tokenResponse, AgoraTokenModel[].class);

                                agoraToken = listOfTokens[0].getCallToken();
                                Log.d("@GET AGOToken::: agoraToken",  "" + agoraToken);

                            } catch (Exception e) {
                                hideProgressDialog();
//                                Timber.tag("TAG").e("onApiResponseError: %s", e.getLocalizedMessage());
//                                if (dataUtil != null) {
//                                    dataUtil.setActivityName(ConnectionActivity.class.getSimpleName());
//                                    dataUtil.zzzLogIt(e, "GetAgoraToken");
//                                }
                            }
                        }, error -> {
                    hideProgressDialog();
                    Log.e(TAG, "startVideoCallGetATokenError: " + error);
//                    if (dataUtil != null) {
//                        dataUtil.zzzLogIt(error, "GetAgoraToken");
//                    }
                    // Handle error
                });

                requestTokenQueue.add(stringAgoraRequest);
            } catch (Exception ignored){
//                if (dataUtil != null) {
//                    dataUtil.setActivityName(ConnectionActivity.class.getSimpleName());
//                    dataUtil.zzzLogIt(ignored, "GetAgoraToken");
//                }
            }
            Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
            builder.appendQueryParameter("callingMLID", String.valueOf(contactInfo.getClientID()));
            builder.appendQueryParameter("VCsecurityID", "0");
            builder.appendQueryParameter("callerhandle", appSettings.getBHandle());
            builder.appendQueryParameter("callerFN", appSettings.getFN());
            builder.appendQueryParameter("callerLN", appSettings.getLN());
            builder.appendQueryParameter("agoraToken", "007eJxTYND2LDzoNe9YfVLZrK3Czz6J71W631Nw7O+ijM87rrD94dVVYDBLNTZJMrdITk0xNzOxSLa0SEpMM0xKtTQ2MjA0TzNJPWvTm9YQyMjAf2YeKyMDBIL47AxlyYk5ObrGDAwAPxEiHA==");
            builder.appendQueryParameter("agoraChannel", agoraChannelName);
            builder.appendQueryParameter("callingHandle", contactInfo.getName());
            builder.appendQueryParameter("callingFN", contactInfo.getFN());
            builder.appendQueryParameter("callingLN", contactInfo.getLN());
            String urlWithParams = builder.build().toString();
            Log.d( "@Start vc request: ", "" + urlWithParams);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWithParams,
                    response -> {
                        // Handle response
                        Log.d("@StartVideoCallApi::: response",  "" + response);
                        try {
                            Gson gson = new Gson();
                            StartVideoModel[] listOfModels = gson.fromJson(response, StartVideoModel[].class);
                            Log.d( "@StartVideoCallApi::: listofModels" , "" + gson.toJson(listOfModels));
                            hideProgressDialog();
                            if (listOfModels != null && listOfModels.length > 0) {
                                videoModel = listOfModels[0];
                                Log.e(TAG, "startVideoCallApi: callId =>  " + videoModel.getCallId());
                                if (videoModel != null && videoModel.isStatus()) {
                                    Intent intent = new Intent(mContext, OutCallViewActivity.class);
                                    intent.putExtra("callingMLID", String.valueOf(contactInfo.getClientID()));
                                    intent.putExtra("pfirst_name", String.valueOf(contactInfo.getFN()));
                                    intent.putExtra("plast_name", String.valueOf(contactInfo.getLN()));
                                    intent.putExtra("coming_from", "outgoing_screen");
                                    if (videoModel.getCallId() != null) {
                                        intent.putExtra("pcallid", String.valueOf(videoModel.getCallId()));
                                    }
                                    intent.putExtra("agora_channel_name", "vcall-3");
                                    intent.putExtra("agora_token", "007eJxTYND2LDzoNe9YfVLZrK3Czz6J71W631Nw7O+ijM87rrD94dVVYDBLNTZJMrdITk0xNzOxSLa0SEpMM0xKtTQ2MjA0TzNJPWvTm9YQyMjAf2YeKyMDBIL47AxlyYk5ObrGDAwAPxEiHA==");
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(mContext, videoModel.getMsg(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        } catch (Exception e) {
                            hideProgressDialog();
//                            Timber.tag("TAG").e("onApiResponseError: %s", e.getLocalizedMessage());
//                            if (dataUtil != null) {
//                                dataUtil.setActivityName(ConnectionActivity.class.getSimpleName());
//                                dataUtil.zzzLogIt(e, "StartVC");
//                            }
                        }
                    }, error -> {
                hideProgressDialog();
                Log.e(TAG, "startVideoCallApiError: " + error);
//                if (dataUtil != null) {
//                    dataUtil.zzzLogIt(error, "StartVC");
//                }
                // Handle error
            });

            requestQueue.add(stringRequest);
        } catch (Exception e) {
//            if (dataUtil != null) {
//                dataUtil.setActivityName(ConnectionActivity.class.getSimpleName());
//                dataUtil.zzzLogIt(e, "StartVC");
//            }
        }
    }
}
