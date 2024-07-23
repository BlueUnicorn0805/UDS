package hawaiiappbuilders.c.videocalls;

import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.ACTION_VIDEO_CALL;
import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.VC_TYPE;
import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.VC_TYPE_RECEIVER_DECLINE;
import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.VC_TYPE_SENDER_DECLINE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import hawaiiappbuilders.c.BaseActivity;
import hawaiiappbuilders.c.MyApplication;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.messaging.FCMHelper;
import hawaiiappbuilders.c.messaging.TokenGetter;
import hawaiiappbuilders.c.model.FCMTokenData;
import hawaiiappbuilders.c.model.WaitCallModel;
import hawaiiappbuilders.c.utils.AgoraVCKit;
import hawaiiappbuilders.c.utils.BaseFunctions;

public class IncomingCallViewActivity extends BaseActivity {

    private static final int PERMISSION_REQ_ID = 22;
    private boolean isTurnonVideo = true;
    private String agoraChannelName = "";
    private String agoraToken = "";
    private String senderToken = "";
    private String firstName = "";
    private String lastName = "";
    private String callID;
    private Ringtone ringtone;

    public static final int iStartedTheCall = 0;
    private Handler handler;
    private boolean isDecline = false;

    private CountDownTimer countDownTimer;
    private long secondsRemaining;
    private boolean isCancelAPi = false;

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

                    if (vcType.equals(VC_TYPE_SENDER_DECLINE)) {
                        finish();
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_view);
        AgoraVCKit.getInstance().init(this);

        Intent intent = getIntent();
        callID = intent.getStringExtra("pcallid");
        Log.d("@IncomingActivity", "Received callId: " + callID);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtone.play();

        if (checkPermissions()) {
            AgoraVCKit.getInstance().setLocalPreview(findViewById(R.id.local_video_view_container), this);
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);
        }

        findViewById(R.id.btnReject).setOnClickListener(v -> cancelCall(BaseFunctions.CALL_DECLINED));
        findViewById(R.id.btnAccept).setOnClickListener(v -> acceptCall());
        findViewById(R.id.btnCamera).setOnClickListener(v -> changeCamera());

        if(getIntent().hasExtra("pfirst_name")) {
            firstName = getIntent().getStringExtra("pfirst_name");
        }
        if(getIntent().hasExtra("plast_name")) {
            lastName = getIntent().getStringExtra("plast_name");
        }
        if(getIntent().hasExtra("agora_token")) {
            agoraToken = getIntent().getStringExtra("agora_token");
            Log.e("Uniway-IncomingCallView", "agora_token: " + agoraToken);
        }
        if(getIntent().hasExtra("agora_channel_name")) {
            agoraChannelName = getIntent().getStringExtra("agora_channel_name");
            Log.e("Uniway-IncomingCallView", "agora_channel_name: " + agoraChannelName);
        }
        if(getIntent().hasExtra("sender_token")) {
            senderToken = getIntent().getStringExtra("sender_token");
            Log.e("Uniway-IncomingCallView", "sender_token: " + senderToken);
        }

//        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(ACTION_VIDEO_CALL));

        handler = new Handler();
        startCountDownTimer();
        handler.postDelayed(this::callWaitingVideoCallApi, 1000);
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
        try {
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
                handler.removeCallbacks(this::callWaitingVideoCallApi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeScreen() {
        handler.postDelayed(this::finish, 1500);
    }

    private void stopRingTone() {
        try {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
        } catch (Exception e) {
            Log.d(TAG, "stopRingTone: " + e.getLocalizedMessage());
        }
    }

    private void callWaitingVideoCallApi() {
        try {
            Log.d("@CallID :::","" + callID);
            String baseUrl = BaseFunctions.getBaseUrl(this,
                    "waitingOnCall",
                    BaseFunctions.APP_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    ((MyApplication) getApplication()).getAndroidId());
            Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
            builder.appendQueryParameter("VCsecurityID", String.valueOf(0));
            builder.appendQueryParameter("callID", !TextUtils.isEmpty(callID) ? callID : "2");
            builder.appendQueryParameter("iStartedTheCall", String.valueOf(iStartedTheCall));
//            builder.appendQueryParameter("callerhandle", appSettings.getHandle());
            String urlWithParams = builder.build().toString();
            Log.d(TAG, "waitingOnCall Request:" + urlWithParams);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWithParams,
                    response -> {
                        Log.d( "@WaitingOnCall Response:%s", "" + response);
                        // Handle response
                        try {
                            Gson gson = new Gson();
                            WaitCallModel[] listOfModels = gson.fromJson(response, WaitCallModel[].class);
                            if (listOfModels != null && listOfModels.length > 0) {
                                WaitCallModel model = listOfModels[0];
                                if (handler == null) {
                                    handler = new Handler();
                                }
                                if (model.getCallStatus() > 1360 && model.getCallStatus() < 1400) {
                                    Toast.makeText(this, model.getMsg(), Toast.LENGTH_LONG).show();
                                    if (countDownTimer != null) countDownTimer.cancel();
                                    removeHandler();
                                    closeScreen();
                                    finish();
                                }
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "waitingOnCall Exception:%s" + e.getLocalizedMessage());
                        }
                    }, error -> {
                Log.e(TAG, "waitingOnCall error:%s" + error);
                finish();
                // Handle error
            });

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelCall(int status) {
        try {
            Log.e( "@CancelCall: first ", "" + (status == BaseFunctions.CALL_DECLINED));
            if (status == BaseFunctions.CALL_DECLINED) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                removeHandler();
                if (!isFinishing()) {
                    finish();
                }
            }
            showProgressDialog();
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
            Log.d(TAG, "setVCStatus request: " + urlWithParams);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWithParams,
                    response -> {
                        // Handle response
                        Log.d( "@setVCStatus Response: ","" + response);
                        isCancelAPi = true;
                        try {
                            Gson gson = new Gson();
                            JsonArray json = gson.fromJson(response, JsonArray.class);
                            Toast.makeText(this, json.get(0).getAsJsonObject().get("msg").getAsString(), Toast.LENGTH_SHORT).show();
                            WaitCallModel[] listOfModels = gson.fromJson(response, WaitCallModel[].class);
                            if (listOfModels != null && listOfModels.length > 0) {
                                WaitCallModel model = listOfModels[0];
                                hideProgressDialog();
                                Toast.makeText(this, model.getMsg(), Toast.LENGTH_SHORT).show();

                                switch (model.getCallStatus()) {
                                    case 1375:
                                        if (countDownTimer != null) {
                                            countDownTimer.cancel();
                                        }
                                        removeHandler();
                                        stopRingTone();
                                        if (!isFinishing()) {
                                            finish();
                                        }
                                        break;
                                    case 1400:
                                        if (countDownTimer != null) {
                                            countDownTimer.cancel();
                                        }
                                        stopRingTone();
                                        removeHandler();
                                        goToVideoCallActivity(model);
                                        finish();
                                        break;
                                    case 1360:
                                        break;
                                }
                            } else {
                                isCancelAPi = false;
                                hideProgressDialog();
                            }
                        } catch (Exception e) {
                            hideProgressDialog();
                            isCancelAPi = false;
                            e.printStackTrace();
                        }
                    }, error -> {
                hideProgressDialog();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "ApiError: " + error);
                // Handle error
            });

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void acceptCall() {
        try {
            removeHandler();
            String baseUrl = BaseFunctions.getBaseUrl(this,
                    "acceptVC",
                    BaseFunctions.APP_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    ((MyApplication) getApplication()).getAndroidId());

            Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
            builder.appendQueryParameter("VCsecurityID", String.valueOf(0));
            builder.appendQueryParameter("callID", callID);
//            builder.appendQueryParameter("callID", "41");
            String urlWithParams = builder.build().toString();
            Log.d("@AcceptVC request::: urlWithParams", "" + urlWithParams);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWithParams,
                    response -> {
                        Log.d("@AcceptVC Response::: response", "" + response);
                        isCancelAPi = true;
                        Gson gson = new Gson();
                        WaitCallModel[] listOfModels = gson.fromJson(response, WaitCallModel[].class);
                        Log.e(TAG, "acceptCall: " + listOfModels);
                        if (listOfModels != null && listOfModels.length > 0) {
                            WaitCallModel model = listOfModels[0];
                            Log.e(TAG, "acceptCall: " + gson.toJson(model));
                            if (model.getCallStatus() > 1360 && model.getCallStatus() < 1400) {
                                removeHandler();
                                stopRingTone();
                                Toast.makeText(this, model.getMsg(), Toast.LENGTH_SHORT).show();
                                cancelCall(model.getCallStatus());
                            } else if (model.getCallStatus() == 1400) {
                                Log.e(TAG, "goToAnswerCallActivity: ");

                                Intent intent = new Intent(this, CallViewActivity.class);
                                intent.putExtra("pcallid", String.valueOf(callID));
                                intent.putExtra("coming_from", "incoming_screen");
                                intent.putExtra("pfirst_name", model.getCallerFn());
                                intent.putExtra("plast_name", model.getCallerLn());
                                intent.putExtra("calling_ip", model.getCallerIp());
                                intent.putExtra("iStartedTheCall", model.getCallerIp());
                                intent.putExtra("agora_token", model.getAgoraToken());
                                intent.putExtra("agora_channel_name", model.getAgoraChannel());
//                            intent.putExtra(Constants.KEY_USER_Agora_Channel, agoraChannelName);
//                            intent.putExtra(Constants.KEY_USER_Agora_Token, agoraToken);
//                            intent.putExtra(Constants.KEY_USER_Agora_Id, agoraAppId);
                                intent.putExtra("user_camera_statue", isTurnonVideo);
                                startActivity(intent);
//                            sendPushResponseVCall();
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                }
                                removeHandler();
                                stopRingTone();
                                finish();
                            }
                        }
                    }, error -> {
            });

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            isCancelAPi = false;
            e.printStackTrace();
        }
    }

    private void goToVideoCallActivity(WaitCallModel model) {
        Log.e(TAG, "goToVideoCallActivity: ");

        Intent intent = new Intent(this, OutCallViewActivity.class);
        intent.putExtra("pcallid", String.valueOf(callID));
        intent.putExtra("coming_from", "incoming_screen");
        intent.putExtra("pfirst_name", firstName);
        intent.putExtra("plast_name", lastName);
        intent.putExtra("calling_ip", model.getCallerIp());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        removeHandler();
        stopRingTone();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (!isCancelAPi && !isDecline && !isFinishing()) {
            cancelCall(BaseFunctions.CALL_DECLINED);
        }
        super.onDestroy();
    }
}