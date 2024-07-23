package hawaiiappbuilders.c.videocalls;

import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.ACTION_VIDEO_CALL;
import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.VC_TYPE;
import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.VC_TYPE_ACCEPT;
import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.VC_TYPE_RECEIVER_DECLINE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;

import hawaiiappbuilders.c.BaseActivity;
import hawaiiappbuilders.c.MyApplication;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.messaging.AppFirebaseMessagingService;
import hawaiiappbuilders.c.messaging.FCMHelper;
import hawaiiappbuilders.c.messaging.TokenGetter;
import hawaiiappbuilders.c.model.FCMTokenData;
import hawaiiappbuilders.c.model.WaitCallModel;
import hawaiiappbuilders.c.utils.AgoraVCKit;
import hawaiiappbuilders.c.utils.BaseFunctions;

public class OutCallViewActivity extends BaseActivity {

    private static final int PERMISSION_REQ_ID = 22;

    private boolean isTurnonVideo = true;

    private String receiverToken = "";
    private String agoraToken = "";
    private String agoraChannelName = "";

    private String callID;
    private String ClientID;
    private String firstName;
    private String lastName;
    private String comingFrom;
    private boolean isDecline = false;
    private Handler handler;

    public static final int iStartedTheCall = 1;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String vcType = "";
                    if(intent.hasExtra(VC_TYPE)){
                        vcType = intent.getStringExtra(VC_TYPE);
                    }

                    if (vcType.equals(VC_TYPE_RECEIVER_DECLINE)) {
                        finish();
                    } else if(vcType.equals(VC_TYPE_ACCEPT)){
                        Intent newIntent = new Intent(OutCallViewActivity.this, CallViewActivity.class);
                        newIntent.putExtra("receiver_token", receiverToken);
                        newIntent.putExtra("agora_token", agoraToken);
                        newIntent.putExtra("agora_channel_name", agoraChannelName);
                        newIntent.putExtra("is_receiver", false);
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(newIntent);
                        finish();
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_call_view);

        Intent intent = getIntent();
        callID = intent.getStringExtra("pcallid");
        ClientID = intent.getStringExtra("callingMLID");
        firstName = intent.getStringExtra("pfirst_name");
        lastName = intent.getStringExtra("plast_name");
        comingFrom = intent.getStringExtra("coming_from");

//        agoraToken = intent.getStringExtra("agora_token");
//        agoraChannelName = appSettings.getUserId() + "" + ClientID;

        AgoraVCKit.getInstance().init(this);

        if (checkPermissions()) {
            AgoraVCKit.getInstance().setLocalPreview(findViewById(R.id.local_video_view_container), this);
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);
        }
        findViewById(R.id.btnReject).setOnClickListener(v -> {
            cancelCallApi(BaseFunctions.CALL_CANCELED_HUNG_UP);
        });
        findViewById(R.id.btnCamera).setOnClickListener(v -> changeCamera());

//        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(ACTION_VIDEO_CALL));

        isTurnonVideo = true;

        handler = new Handler();
        startCountDownTimer();
        handler.postDelayed(this::callWaitingVideoCallApi, 3000);
    }

    private void changeCamera() {
        isTurnonVideo = !isTurnonVideo;
        if(isTurnonVideo){
            ((AppCompatImageView)findViewById(R.id.ivCam)).setImageResource(R.drawable.round_videocam_24);
            AgoraVCKit.getInstance().setLocalPreview(findViewById(R.id.local_video_view_container), this);
        }else{
            ((AppCompatImageView)findViewById(R.id.ivCam)).setImageResource(R.drawable.round_videocam_off_24);
            AgoraVCKit.getInstance().stopPreview();
            ((FrameLayout)findViewById(R.id.local_video_view_container)).removeAllViews();
        }
    }

    private String[] getRequiredPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
        }
    }

    private boolean checkPermissions() {
        for (String permission : getRequiredPermissions()) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkPermissions()) {
            AgoraVCKit.getInstance().setLocalPreview(findViewById(R.id.local_video_view_container), this);
        }
    }

    private CountDownTimer countDownTimer;
    private long secondsRemaining;
    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(3000, 1000) {
            @SuppressLint("LogNotTimber")
            @Override
            public void onTick(long millisUntilFinished) {
                secondsRemaining = millisUntilFinished / 1000;
                Log.d(TAG, "onTick: " + secondsRemaining);
            }

            @Override
            public void onFinish() {
                handler.postDelayed(() -> callWaitingVideoCallApi(), 1000);
                countDownTimer.start();
            }
        }.start();
    }


    private void removeHandler() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler.removeCallbacks(this::callWaitingVideoCallApi);
        }
    }

    private void closeScreen() {
        handler.postDelayed(() -> {
            finish();
        }, 1500);
    }

    private void cancelCallApi(int status) {
        try {
//            showProgressDialog();
            isDecline = true;
            String baseUrl = BaseFunctions.getBaseUrl(this,
                    "setVCstatus",
                    BaseFunctions.APP_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    ((MyApplication) getApplication()).getAndroidId());
            Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
            builder.appendQueryParameter("VCsecurityID", String.valueOf(0));
            builder.appendQueryParameter("statusID", String.valueOf(status));
            builder.appendQueryParameter("callID", callID);
            String urlWithParams = builder.build().toString();
            Log.d( "@Cancel_api_request: ", "" + urlWithParams);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWithParams,
                    response -> {
                        // Handle response
                        Log.d("@setVCStatus Response::: ", "" + response);
                        try {
                            Gson gson = new Gson();
                            WaitCallModel[] listOfModels = gson.fromJson(response, WaitCallModel[].class);
                            if (listOfModels != null && listOfModels.length > 0) {
                                WaitCallModel model = listOfModels[0];
//                                hideProgressDialog();
                                if (model.getCallStatus() == BaseFunctions.CALL_DECLINED || model.getCallStatus() == BaseFunctions.CALL_CANCELED_HUNG_UP) {
                                    closeScreen();
                                    if (countDownTimer != null) countDownTimer.cancel();
                                    removeHandler();
                                    Toast.makeText(this, model.getMsg(), Toast.LENGTH_SHORT).show();
                                    Log.d("@Cancel_Call_HangUP::: ", "" + response);
                                    finish();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, error -> {
//                hideProgressDialog();
                Log.e(TAG, "ApiError: " + error);
                // Handle error
            });

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callWaitingVideoCallApi() {
        try {
            String baseUrl = BaseFunctions.getBaseUrl(this,
                    "waitingOnCall",
                    BaseFunctions.APP_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    ((MyApplication) getApplication()).getAndroidId());
            Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
            builder.appendQueryParameter("VCsecurityID", String.valueOf(0));
            builder.appendQueryParameter("callID", callID);
            builder.appendQueryParameter("iStartedTheCall", String.valueOf(iStartedTheCall));
//            builder.appendQueryParameter("callerhandle", appSettings.getHandle());
            String urlWithParams = builder.build().toString();
            Log.d(TAG, "waitingOnCall Request:" + urlWithParams);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWithParams,
                    response -> {
                        Log.d("@WaitingOnCall Response:%s", "" + response);
                        // Handle response
                        try {
                            Gson gson = new Gson();
                            WaitCallModel[] listOfModels = gson.fromJson(response, WaitCallModel[].class);
                            if (listOfModels != null && listOfModels.length > 0) {
                                WaitCallModel model = listOfModels[0];
                                if (handler == null) {
//                                    handler = new Handler();
                                }

                                Log.e(TAG, "callWaitingVideoCallApi: " + model.getCallStatus());
                                if (model.getCallStatus() <= 1360) {
//                                    handler.postDelayed(this::callWaitingVideoCallApi, 3000);
                                } else if (model.getCallStatus() == 1400) {
//                                    isDecline = true;
                                    Log.e(TAG, "goToVideoCallActivity: ");
                                    if (countDownTimer != null) countDownTimer.cancel();
                                    removeHandler();
                                    Intent intent = new Intent(this, CallViewActivity.class);
                                    intent.putExtra("pcallid", String.valueOf(callID));
                                    intent.putExtra("coming_from", "incoming_screen");
                                    intent.putExtra("pfirst_name", firstName);
                                    intent.putExtra("plast_name", lastName);
                                    intent.putExtra("calling_ip", model.getCallerIp());
                                    intent.putExtra("agora_token", agoraToken);
                                    intent.putExtra("agora_channel_name", agoraChannelName);
                                    intent.putExtra("iStartedTheCall", iStartedTheCall);

                                    Log.d("@OUTCALL_AgoraToken", ""+agoraToken);
                                    startActivity(intent);
                                    closeScreen();
                                    finish();
                                } else if (model.getCallStatus() > 1360 && model.getCallStatus() < 1400) {
                                    isDecline = true;
                                    if (countDownTimer != null) countDownTimer.cancel();
                                    removeHandler();
                                    Toast.makeText(this, model.getMsg(), Toast.LENGTH_LONG).show();
                                    closeScreen();
                                    finish();
                                }
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "waitingOnCall Exception:%s" + e.getLocalizedMessage());
                        }
                    }, error -> {
                Log.e(TAG, "waitingOnCall error: " + error.getMessage());
            });

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeHandler();
        if (!isDecline && !isFinishing()) {
            cancelCallApi(BaseFunctions.CALL_DECLINED);
        }

    }
}