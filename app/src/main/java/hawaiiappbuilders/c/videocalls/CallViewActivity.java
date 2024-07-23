package hawaiiappbuilders.c.videocalls;

import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.ACTION_VIDEO_CALL;
import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.VC_TYPE;
import static hawaiiappbuilders.c.messaging.AppFirebaseMessagingService.VC_TYPE_SENDER_DECLINE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import hawaiiappbuilders.c.BaseActivity;
import hawaiiappbuilders.c.MyApplication;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.messaging.FCMHelper;
import hawaiiappbuilders.c.messaging.TokenGetter;
import hawaiiappbuilders.c.model.FCMTokenData;
import hawaiiappbuilders.c.model.WaitCallModel;
import hawaiiappbuilders.c.utils.AgoraVCKit;
import hawaiiappbuilders.c.utils.BaseFunctions;
import io.agora.rtc2.IRtcEngineEventHandler;

public class CallViewActivity extends BaseActivity {

    private String agoraChannelName = "vcall-3";
//    private String agoraChannelName = "";
    private String agoraToken = "007eJxTYBCojFn76PPeRN2Z3PM+sB1zNX2vo1N/dkrlvrTDlhfL1A8rMJilGpskmVskp6aYm5lYJFtaJCWmGSalWhobGRiap5mkVvv3pzUEMjKoeDozMjJAIIjPzlCWnJiTo2vMwAAAxp0f7w==";
//    private String agoraToken = "";

    private static final int PERMISSION_REQ_ID = 22;

    private String senderToken = "";
    private String receiverToken = "";

    private boolean isReceiver = true;

    private CardView btnSwitchLocalCamera;
    private CardView btnDisableLocalCamera;
    private CardView btnSwitchRemoteCamera;
    private AppCompatImageView btnMic;
    private CardView btnCancelCall;

    private CountDownTimer countDownTimer, callTimeTimer;
    private Handler handler;
    private long secondsRemaining;

    private long callTime = 0;

    private String callID;
    public static int iStartedTheCall;
    private String firstName;
    private String lastName;
    private boolean isDecline = false;
    private String cameraIp;
    private String comingFrom;
    private String callingIp;

    private boolean isTurnonVideo = true;
    private boolean isTurnonRemoteVideo = true;
    private boolean isTurnonSwitchCam = true;
    private boolean isTurnonMic = true;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_view);

        Intent intent = getIntent();
        callID = intent.getStringExtra("pcallid");
        firstName = intent.getStringExtra("pfirst_name");
        lastName = intent.getStringExtra("plast_name");
        comingFrom = intent.getStringExtra("coming_from");
        iStartedTheCall = intent.getIntExtra("iStartedTheCall", 9999);
        isTurnonVideo = intent.getBooleanExtra("user_camera_statue",true);
// ---------------------------------------------------------------------------------------------------------------------------------
//        agoraToken = intent.getStringExtra("agora_token");
//        agoraChannelName = intent.getStringExtra("agora_channel_name");

        AgoraVCKit.getInstance().init(this);
        AgoraVCKit.getInstance().setEventHandler(new IRtcEngineEventHandler() {
            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                super.onJoinChannelSuccess(channel, uid, elapsed);
                runOnUiThread(() -> {
//                    Log.e("InCallActivity", "Join channel success");
                    Toast.makeText(CallViewActivity.this, "Join channel success", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onUserJoined(int uid, int elapsed) {
                Log.e("InCallActivity", "uid: " + uid);
                runOnUiThread(() -> {
                    // ?? uid ?,????????
                    FrameLayout remoteContainer = findViewById(R.id.remote_video_view_container);
                    AgoraVCKit.getInstance().setupRemoteVideo(uid, remoteContainer, CallViewActivity.this);
                });
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                super.onUserOffline(uid, reason);
                runOnUiThread(() -> {
//                    Toast.makeText(CallViewActivity.this, "User offline: " + uid, Toast.LENGTH_SHORT).show();
                });
            }
        });

        if (checkPermissions()) {
            AgoraVCKit.getInstance().setLocalPreview(findViewById(R.id.local_video_view_container), this);
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);
        }

        findViewById(R.id.btnReject).setOnClickListener(v -> {
            cancelCallApi(1380);
        });

        btnCancelCall = findViewById(R.id.btnReject);
        btnDisableLocalCamera = findViewById(R.id.btnCamera);
        btnSwitchLocalCamera = findViewById(R.id.btnSwitchCamera);
        btnSwitchRemoteCamera = findViewById(R.id.btnSwitchRemoteCamera);
        btnMic = findViewById(R.id.ivMic);

        btnDisableLocalCamera.setOnClickListener(v -> changeCamera());
        btnSwitchLocalCamera.setOnClickListener(v -> switchLocalCamera());
        btnSwitchRemoteCamera.setOnClickListener(v -> switchRemoteCamera());
        btnMic.setOnClickListener(v -> changeMic());

        btnCancelCall.setOnClickListener(v -> cancelCallApi(1380));

        findViewById(R.id.btnDollar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog sendMoneyDlg = new Dialog(CallViewActivity.this);
                sendMoneyDlg.setContentView(R.layout.layout_alert_dollar_send_dialog);
                sendMoneyDlg.findViewById(R.id.btn_send).setOnClickListener(v -> {
                    sendMoneyDlg.dismiss();
                });
                sendMoneyDlg.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
                    sendMoneyDlg.dismiss();
                });
                sendMoneyDlg.show();
            }
        });

        ((TextView)findViewById(R.id.text_current_time)).setText(currentTime());
        ((TextView)findViewById(R.id.text_found_name)).setText(firstName + " " + lastName);

        if(!agoraToken.isEmpty() && !agoraChannelName.isEmpty()){
            AgoraVCKit.getInstance().joinChannel(agoraToken, agoraChannelName);
        }

        startCountDownTimer();
    }

    private void startCountDownTimer() {
        if (handler == null) {
            handler = new Handler();
        }
        countDownTimer = new CountDownTimer(5000, 1000) {
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

        callTimeTimer = new CountDownTimer(1000, 1000) {
            @SuppressLint("LogNotTimber")
            @Override
            public void onTick(long millisUntilFinished) {
                callTime += 1;
                ((TextView)findViewById(R.id.text_calling_time)).setText(convertSECtoHMS(callTime));
            }

            @Override
            public void onFinish() {
                handler.postDelayed(() -> callWaitingVideoCallApi(), 1000);
                callTimeTimer.start();
            }
        }.start();
    }

//    private void changeCamera() {
//        isTurnonVideo = !isTurnonVideo;
//        if(isTurnonVideo){
//            ((AppCompatImageView)findViewById(R.id.ivCam)).setImageResource(R.drawable.round_videocam_24);
//            AgoraVCKit.getInstance().setLocalPreview(findViewById(R.id.local_video_view_container), this);
//        }else{
//            ((AppCompatImageView)findViewById(R.id.ivCam)).setImageResource(R.drawable.round_videocam_off_24);
//            AgoraVCKit.getInstance().stopPreview();
//            ((FrameLayout)findViewById(R.id.local_video_view_container)).removeAllViews();
//        }
//    }
    private void changeCamera() {
        isTurnonVideo = !isTurnonVideo;
        HiddenCamera(isTurnonVideo);
//        if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
//            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
//        } else {
//            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
//        }
//        startCamera();
    }
    private void changeMic() {
        isTurnonMic = !isTurnonMic;

        if(isTurnonMic){
            ((AppCompatImageView)findViewById(R.id.ivMic)).setImageResource(R.drawable.baseline_mic_24);
            AgoraVCKit.getInstance().muteAudio(true);
//            setAgoraLocalPreview(findViewById(R.id.local_video_view_container));
        }else{
            ((AppCompatImageView)findViewById(R.id.ivMic)).setImageResource(R.drawable.baseline_mic_off_24);
            AgoraVCKit.getInstance().muteAudio(false);
        }
    }

    private void HiddenCamera(boolean isTurnonVideo) {
        if(isTurnonVideo){
            ((AppCompatImageView)findViewById(R.id.ivCam)).setImageResource(R.drawable.baseline_video_camera_front_24);
//            setAgoraLocalPreview(findViewById(R.id.local_video_view_container));
            findViewById(R.id.local_video_view_container).setVisibility(View.VISIBLE);
        }else{
            ((AppCompatImageView)findViewById(R.id.ivCam)).setImageResource(R.drawable.round_videocam_off_24);
            findViewById(R.id.local_video_view_container).setVisibility(View.GONE);
        }
    }

    private void switchLocalCamera() {
        isTurnonSwitchCam = !isTurnonSwitchCam;
        AgoraVCKit.getInstance().switchLocalVideo();
        if(isTurnonSwitchCam){
//            ((AppCompatImageView)findViewById(R.id.ivSwitchCam)).setImageResource(R.drawable.baseline_video_camera_front_24);
//            setAgoraLocalPreview(findViewById(R.id.local_video_view_container));

        }else{
//            ((AppCompatImageView)findViewById(R.id.ivSwitchCam)).setImageResource(R.drawable.baseline_video_camera_back_24);

        }
    }
    private void switchRemoteCamera() {
        isTurnonRemoteVideo = !isTurnonRemoteVideo;
        if(isTurnonRemoteVideo){
            ((AppCompatImageView)findViewById(R.id.ivswitchRemoteCam)).setImageResource(R.drawable.baseline_switch_remote_video_24);
//            setAgoraLocalPreview(findViewById(R.id.local_video_view_container));
            AgoraVCKit.getInstance().switchRemoteVideo(true);
            ((FrameLayout)findViewById(R.id.remote_video_view_container)).setVisibility(View.VISIBLE);
        }else{
            ((AppCompatImageView)findViewById(R.id.ivswitchRemoteCam)).setImageResource(R.drawable.round_videocam_off_24);
//            findViewById(R.id.remote_video_view_container).setVisibility(View.GONE);
            AgoraVCKit.getInstance().switchRemoteVideo(false);
            ((FrameLayout)findViewById(R.id.remote_video_view_container)).setVisibility(View.INVISIBLE);
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
            AgoraVCKit.getInstance().joinChannel(agoraToken, agoraChannelName);
        }
    }

    private void cancelCallApi(int status) {
        try {
//            showProgressDialog();
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
            Log.d(TAG, "cancel_api_request: " + urlWithParams);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWithParams,
                    response -> {
                        // Handle response
                        Log.d(TAG, "setVCStatus Response: " + response);
                        try {
                            Gson gson = new Gson();
                            WaitCallModel[] listOfModels = gson.fromJson(response, WaitCallModel[].class);
                            if (listOfModels != null && listOfModels.length > 0) {
                                WaitCallModel model = listOfModels[0];
//                                hideProgressDialog();
                                if (model.getCallStatus() == BaseFunctions.CALL_DECLINED || model.getCallStatus() == BaseFunctions.CALL_CANCELED_HUNG_UP) {
                                    Log.d("@InCallsetVCStatus Response: ","" + response);
                                    isDecline = true;
                                    if (countDownTimer != null) countDownTimer.cancel();
                                    handler.removeCallbacksAndMessages(null);
                                    handler.removeCallbacks(this::callWaitingVideoCallApi);
                                    Toast.makeText(this, model.getMsg(), Toast.LENGTH_SHORT).show();
                                    Log.d("@@@InCallsetVCStatus Response: ","" + response);
                                    finish();
                                }
                            } else {
//                                hideProgressDialog();
                            }
                        } catch (Exception e) {
//                            hideProgressDialog();
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
            String urlWithParams = builder.build().toString();
            Log.d(TAG, "waitingOnCall Request:" + urlWithParams);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWithParams,
                    response -> {
                        Log.d( "@InCallWaitingOnCall Response:%s","" + response);
                        // Handle response
                        try {
                            Gson gson = new Gson();
                            WaitCallModel[] listOfModels = gson.fromJson(response, WaitCallModel[].class);
                            if (listOfModels != null && listOfModels.length > 0) {
                                WaitCallModel model = listOfModels[0];
                                if (handler == null) {
                                    handler = new Handler();
                                }
                                if (model.getCallStatus() < 1400) {
                                    if (countDownTimer != null) countDownTimer.cancel();
                                    handler.removeCallbacksAndMessages(null);
                                    handler.removeCallbacks(this::callWaitingVideoCallApi);
                                    Toast.makeText(this, model.getMsg(), Toast.LENGTH_LONG).show();
                                    finish();
                                    closeScreen();
                                } else {
                                    if (model.getCallStatus() != 1400) {
                                        Toast.makeText(this, model.getMsg(), Toast.LENGTH_LONG).show();
                                    }
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

    private void closeScreen() {
        handler.postDelayed(() -> {
            finish();
        }, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (callTimeTimer != null) {
            callTimeTimer.cancel();
        }
        if (handler != null) {
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
                handler.removeCallbacks(this::callWaitingVideoCallApi);
            }
        }
//        if (!isDecline && !isFinishing()) {
            cancelCallApi(BaseFunctions.CALL_DECLINED);
//        }

        AgoraVCKit.getInstance().destroy();
    }

    public String currentTime(){
        SimpleDateFormat df = new SimpleDateFormat("H:mm a");
        return df.format(new Date());
    }
    public String convertSECtoHMS(long secondsCount) {
        //Calculate the seconds to display:
        long seconds = secondsCount %60;
        secondsCount -= seconds;
        //Calculate the minutes:
        long minutesCount = secondsCount / 60;
        long minutes = minutesCount % 60;
        minutesCount -= minutes;
        //Calculate the hours:
        long hoursCount = minutesCount / 60;
        //Build the String
        String result = "";
        if(hoursCount > 0){
            result += hoursCount + "h ";
        }

        if(minutes > 0){
            result += minutes + "m ";
        }

        result += seconds + "s";
        return result;
    }
}