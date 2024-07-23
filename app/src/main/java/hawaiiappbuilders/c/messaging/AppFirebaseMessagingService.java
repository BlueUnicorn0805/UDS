package hawaiiappbuilders.c.messaging;

import static hawaiiappbuilders.c.messaging.FCMHelper.CTEXT_SEND_MONEY_W_NAME;
import static hawaiiappbuilders.c.messaging.FCMHelper.CTITLE_NEW_MESSAGE_W_NAME;
import static hawaiiappbuilders.c.messaging.FCMHelper.CTITLE_PAYMENT_RECEIVED;
import static hawaiiappbuilders.c.messaging.FCMHelper.showNotificationView;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import hawaiiappbuilders.c.AppSettings;
import hawaiiappbuilders.c.BaseActivity;
import hawaiiappbuilders.c.MyApplication;
import hawaiiappbuilders.c.R;

import hawaiiappbuilders.c.TransactionHistoryActivity;
import hawaiiappbuilders.c.URLResolver;
import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.messaging.FCMHelper;
import hawaiiappbuilders.c.model.User;
import hawaiiappbuilders.c.videocalls.CallViewActivity;
import hawaiiappbuilders.c.videocalls.IncomingCallViewActivity;
import hawaiiappbuilders.c.videocalls.OutCallViewActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    private static int notId = 0;

    SharedPreferences settings;

    private static String lastMessageStamp = "";
    private static int lastMessageType = 0;

    private final String TAG = "FBMsg";

    private static boolean isAppInBackground = true;

    public static void setAppInBackground(boolean isBackground) {
        isAppInBackground = isBackground;
    }

    public static final String ACTION_VIDEO_CALL = "ACTION_VIDEO_CALL";
    public static final String VC_TYPE = "VC_TYPE";
    public static final String VC_TYPE_RECEIVER_DECLINE = "VC_TYPE_RECEIVER_DECLINE";
    public static final String VC_TYPE_SENDER_DECLINE = "VC_TYPE_SENDER_DECLINE";
    public static final String VC_TYPE_ACCEPT = "VC_TYPE_ACCEPT";


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        settings = getApplicationContext().getSharedPreferences(BaseActivity.GLOBAL_SETTING, Context.MODE_PRIVATE);
        settings.edit().putString("pushtoken", token).commit();

        // Save on Firebase db
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userName = settings.getString("username", "Anonymous");
            User user = new User();
            user.setName(userName);
            user.setNotificationToken(token);

            // Save data
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
        }

        // Save on backend db
        /*AppSettings appSettings = new AppSettings(getApplicationContext());
        MyApplication myApps = (MyApplication) getApplication();
        if (appSettings.isLoggedIn()) {
            int userId = appSettings.getUserId();
            String deviceId = myApps.getAndroidId();

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject();
                jsonObject.put("devid", "1434741");
                jsonObject.put("appid", "ugoAndroid");
                GpsTracker gpsTracker = new GpsTracker(getApplicationContext());
                if (gpsTracker.canGetLocation()) {
                    jsonObject.put("lon", String.valueOf(gpsTracker.getLongitude()));
                    jsonObject.put("lat", String.valueOf(gpsTracker.getLatitude()));
                } else {
                    jsonObject.put("lon", appSettings.getDeviceLng());
                    jsonObject.put("lat", appSettings.getDeviceLat());
                }
                jsonObject.put("uuid", deviceId);

                jsonObject.put("userid", userId);
                jsonObject.put("token", token);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String urlUpdateToken = URLResolver.apiUpdateToken(jsonObject);
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            StringRequest sr = new StringRequest(Request.Method.GET, urlUpdateToken, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {


                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (jsonObject.getBoolean("status")) {
                            } else {
                                Log.e(TAG, jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (TextUtils.isEmpty(error.getMessage())) {
                        Log.e(TAG, getString(R.string.error_invalid_credentials));
                    } else {
                        Log.e(TAG, error.getMessage());
                    }
                }
            });

            sr.setShouldCache(false);
            queue.add(sr);
        }*/
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("From", remoteMessage.getFrom());

        /*RemoteMessage.Notification notification = remoteMessage.getNotification();
        String pushTitle = remoteMessage.getNotification().getTitle();
        String pushBody = remoteMessage.getNotification().getBody();*/

        if (remoteMessage.getData() == null)
            return;

        // String appTarget = remoteMessage.getData().get("appid");
        try {
            showNotification(remoteMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*private void showNotification(RemoteMessage remoteMessage) {
        Log.e("UPD_PM", remoteMessage.toString());

        Map<String, String> dataMap = remoteMessage.getData();
        String title = dataMap.get("title");
        String messageBody = dataMap.get("message");
        String pushType = dataMap.get("pushtype");
        String pushData = dataMap.get("extra");
        String pushTimeStamp = dataMap.get("timestamp");

        Log.e("dup", "------------------------------------------------------------------------");
        Log.e("dup", "OldType " + lastMessageType);
        Log.e("dup", "OldStamp" + lastMessageStamp);

        Log.e("dup", "NewType " + pushType);
        Log.e("dup", "NewStamp" + pushTimeStamp);
        Log.e("dup", "------------------------------------------------------------------------");

        if (pushType.equals(lastMessageType) && pushTimeStamp.equals(lastMessageStamp)) {
            // Ignore repeated message
            return;
        }

        lastMessageType = pushType;
        lastMessageStamp = pushTimeStamp;

        // Show push notification
        Intent intent = null;

        // Notify
        if ("1".equals(pushType)) {// Sender made new delivery and send push to the favourite drivers
            intent = new Intent("eAppBuilder1957.c.bid_accept_ACTIVITY");
            intent.putExtra("title", title);
            intent.putExtra("message", messageBody);
            intent.putExtra("pushType", pushType);
            intent.putExtra("payloads", pushData);

            Intent dutyUpdateIntent = new Intent("com.ugo.updatedeliverystatus");
            dutyUpdateIntent.putExtra("type", 1);
            sendBroadcast(dutyUpdateIntent);

            Log.e("push", "1. Got fav push!!!------------------");
        } else if ("2".equals(pushType)) {// Sender accept your bid and selected you as a deliverer
            Intent dutyUpdateIntent = new Intent("com.ugo.updatedeliverystatus");
            dutyUpdateIntent.putExtra("type", 2);
            sendBroadcast(dutyUpdateIntent);
            Log.e("push", "2. Got accept push!!!------------------");
        } else if ("4".equals(pushType)) {// Driver bid on open delivery and send push to sender
            Intent dutyUpdateIntent = new Intent("com.ugo.updatedeliverystatus");
            dutyUpdateIntent.putExtra("type", 4);
            sendBroadcast(dutyUpdateIntent);
            Log.e("push", "4. Got bid push!!!------------------");
        }

        if (intent != null) {
            // Show push notification
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP*//* | Intent.FLAG_ACTIVITY_NEW_TASK*//*);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title*//*getString(R.string.title_notification)*//*)
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setOngoing(true)
                            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            if (notificationManager != null) {

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId,
                            "UPX Notification",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(notId++*//* ID of notification *//*, notificationBuilder.build());
            }
        }
    }*/

    /*private void showNotification(RemoteMessage remoteMessage) {
        Log.e("UPD_PM", remoteMessage.toString());

        Map<String, String> dataMap = remoteMessage.getData();
        Set<String> keys = dataMap.keySet();
        Iterator<String> setIterator = keys.iterator();
        String keysVal = "";
        while (setIterator.hasNext()) {
            keysVal = keysVal + ", " + setIterator.next();
        }
        Log.e("PMkeys", keysVal);
        Log.e("PMvals", dataMap.toString());

        // Retrieve the following from data
        String siteName = dataMap.get("siteName");
        String title = dataMap.get("title");
        String imgURL = dataMap.get("imgURL");
        String subject = dataMap.get("subject");
        String email = dataMap.get("email");
        String fn = dataMap.get("fn");
        String ln = dataMap.get("ln");
        String company = dataMap.get("co");
        String timesent = dataMap.get("timesent"); // Server sent it
        String timestamp = dataMap.get("timestamp"); // TODO:  Do we need to receive this instead of timesent?
        String messageBody = dataMap.get("message");
        String senderName = dataMap.get("name");

        String name = "Fr: " + senderName;
        if (dataMap.get("payloadtype") != null) {
            int payloadtype = Integer.parseInt(dataMap.get("payloadtype"));

            if (payloadtype == lastMessageType && timestamp.equals(lastMessageStamp)) {
                // Ignore repeated message
                return;
            }

            Log.e("dup", "------------------------------------------------------------------------");
            Log.e("dup", "OldType " + lastMessageType);
            Log.e("dup", "OldStamp" + lastMessageStamp);

            Log.e("dup", "NewType " + payloadtype);
            Log.e("dup", "NewStamp" + timestamp);
            Log.e("dup", "------------------------------------------------------------------------");

            if (payloadtype == lastMessageType && timestamp.equals(lastMessageStamp)) {
                // Ignore repeated message
                return;
            }

            lastMessageType = payloadtype;
            lastMessageStamp = timestamp;

            boolean showPushNoti = true;
            Intent intent = null;

            // String appId = jsonObject.getString("appid");
            switch (payloadtype) {
                case 1:
                    String payloads = dataMap.get("payloads");  // TODO: rename extra to payloads?
                    // Receives update delivery status
                    // Show Driver Deliveries Map page
                    intent = new Intent("eAppBuilder1957.c.bid_accept_ACTIVITY");
                    intent.putExtra("title", title);
                    intent.putExtra("message", messageBody);
                    intent.putExtra("pushType", payloadtype);
                    intent.putExtra("payloads", payloads);

                    Intent dutyUpdateIntent = new Intent("com.ugo.updatedeliverystatus");
                    dutyUpdateIntent.putExtra("type", 1);
                    sendBroadcast(dutyUpdateIntent);

                    Log.e("push", "1. Got fav push!!!------------------");
                    break;
                case 2:
                    // Receives update delivery status
                    // Show Driver Deliveries Map page
                    intent = new Intent("com.ugo.updatedeliverystatus");
                    intent.putExtra("type", 2);
                    sendBroadcast(intent);
                    Log.e("push", "2. Got accept push!!!------------------");
                    break;
                case 4:
                    // Show Sender Deliveries Map page
                    intent = new Intent("com.ugo.updatedeliverystatus");
                    intent.putExtra("type", 4);
                    sendBroadcast(intent);
                    Log.e("push", "4. Got bid push!!!------------------");
                    break;
            }

            if (intent != null) {
                // Show push notification
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                String channelId = getString(R.string.default_notification_channel_id);
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(title)
                                .setContentText(messageBody)
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setOngoing(true)
                                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                                .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


                if (notificationManager != null) {

                    // Since android Oreo notification channel is needed.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(channelId,
                                "UPX Notification",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        notificationManager.createNotificationChannel(channel);
                    }
                    notificationManager.notify(notId++, notificationBuilder.build());
                }
            }
        }
    }*/

    Map<String, Object> result = new HashMap<>();
    boolean isJson(Object obj) {
        try {
            if (obj instanceof String) {
                JSONObject jsonObject = new JSONObject(String.valueOf(obj));
            }
        } catch (JSONException e) {
            return false;
        }
        return true;
    }
    public Map<String, Object> extractDataMap(JSONObject jsonObject) throws JSONException {
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String key = it.next();
            Object ob = jsonObject.get(key);
            if (ob instanceof String) {
                if (isJson(ob)) {
                    JSONObject j = new JSONObject(jsonObject.get(key).toString());
                    result.put(key, j);
                } else {
                    result.put(key, ob);
                }
            } else {
                result.put(key, ob);
            }
        }
        return result;
    }

    /*@RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("WrongConstant")
    private void showNotification(RemoteMessage remoteMessage) throws JSONException {
        Map<String, String> dataMap = remoteMessage.getData();
        // result.clear();
        // Map<String, Object> result = extractDataMap(new JSONObject(remoteMessage.getData()));
        // Retrieve the following from data
        String email = dataMap.get("email");
        String fn = dataMap.get("fn");
        String ln = dataMap.get("ln");
        String company = dataMap.get("co");
        String messageBody = dataMap.get("message");
        String senderName = dataMap.get("name");

        boolean showCustomNotificationView = false;
        if (dataMap.get("payloadtype") != null) {
            int payloadtype = Integer.parseInt(dataMap.get("payloadtype"));
            boolean showPushNoti = true;
            Intent intent = null;
            String title = "";
            try {
                // String appId = jsonObject.getString("appid");
                String payloads = "";
                switch (payloadtype) {
                    case FCMHelper.PT_Text_Message: // Text Message
                        // payloads = dataMap.get("payloads");
                        title = String.format("New message from %s", senderName);
                        String senderID = dataMap.get("SenderID");
                        intent = new Intent("eAppBuilder1957.c.bid_accept_ACTIVITY");
                        intent.putExtra("title", title);
                        intent.putExtra("message", messageBody);
                        intent.putExtra("payloadtype", payloadtype);
                        // intent.putExtra("payloads", payloads);
                        break;
                    case FCMHelper.PT_Orders: // Sender made new delivery and send push to the favourite drivers
                        title = String.format("Order update from %s", senderName);
                        String orderData = dataMap.get("payloads");
                        JSONObject jsonObject = new JSONObject(orderData);
                        int orderId = jsonObject.getInt("orderID");
                        int statusId = jsonObject.getInt("statusID");
                        intent = new Intent("com.ugo.updatedeliverystatus");
                        intent.putExtra("type", payloadtype);
                        sendBroadcast(intent);
                        // send local message
                        *//*Intent localMsg = new Intent("eAppBuilder1957.d.newmessage");
                        localMsg.putExtra("from", title);
                        localMsg.putExtra("message", messageBody);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(localMsg);*//*
                        break;
                    case FCMHelper.PT_Funds_Sent_Transactions: // Sender made new delivery and send push to the favourite drivers
                        title = String.format("%s sent you money", senderName);
                        intent = new Intent(this, TransactionHistoryActivity.class);
                        break;
                    case FCMHelper.PT_Share_Location:
                        title = String.format("Location from %s", senderName);
                        if (dataMap.get("payloads") != null) {
                            payloads = dataMap.get("payloads");
                            if(payloads != null) {
                                JSONObject locationData = new JSONObject(payloads);
                                locationData.getString("title");
                                title = locationData.getString("title");
                                Double latData = locationData.getDouble("lat");
                                Double lonData = locationData.getDouble("lon");
                                intent = new Intent("com.ugo.updatedeliverystatus");
                                intent.putExtra("type", payloadtype);
                                intent.putExtra("SenderName", senderName);
                                intent.putExtra("lat", latData);
                                intent.putExtra("lon", lonData);
                                intent.putExtra("zoom", dataMap.get("zoom"));
                                sendBroadcast(intent);
                            }
                        }
                        break;
                    case FCMHelper.PT_Reschedule_Appt:
                        break;
                    case FCMHelper.PT_Invoice_Sent:
                        title = String.format("New Invoice from %s", senderName);
                        if (dataMap.get("payloads") != null) {
                            payloads = dataMap.get("payloads");
                            if(payloads != null) {
                                JSONObject invoiceData = new JSONObject(payloads);
                                intent = new Intent("eAppBuilder1957.c.bid_accept_ACTIVITY");
                                long invoiceOrderId = invoiceData.getLong("orderId");
                                intent.putExtra("orderId", invoiceOrderId);
                                intent.putExtra("title", title);
                                intent.putExtra("message", messageBody);
                                intent.putExtra("payloadtype", payloadtype);
                            }
                        }
                        break;
                    case FCMHelper.PT_Share_Task:
                        title = String.format("%s shared with you a new task", senderName);
                        if (dataMap.get("payloads") != null) {
                            // String data = dataMap.get("payloads");
                            intent = new Intent("eAppBuilder1957.c.bid_accept_ACTIVITY");

                        }
                        break;
                    case FCMHelper.PT_Consumer_Parcel_Req:
                        break;
                    case FCMHelper.PT_Driver_Parcel_Bid:
                        break;
                    case FCMHelper.PT_Driver_Food_Request:
                        break;
                    case FCMHelper.PT_Driver_Food_Grab:
                        break;
                    case FCMHelper.PT_Valet_Request:
                        break;
                    case FCMHelper.PT_Valet_Parked:
                        break;
                    case FCMHelper.PT_Valet_Get_Car:
                        break;
                    case FCMHelper.PT_Send_Order:
                        break;
                    case FCMHelper.PT_Rest_Rec:
                        break;
                    case FCMHelper.PT_Rest_Prep:
                        break;
                    case FCMHelper.PT_Rest_Complete:
                        break;
                    case FCMHelper.PT_Rest_Del:
                        break;
                    case FCMHelper.PT_Share_My_Info:
                        break;
                    case FCMHelper.PT_Dial_Phone:
                        title = String.format("%s is requesting you to dial a phone number", senderName);
                        if (dataMap.get("payloads") != null) {
                            String phoneData = dataMap.get("payloads");
                            String phoneNumber = (new JSONObject(phoneData)).getString("phone");
                            intent = new Intent("hawaiiappbuilders.c.dial_phone");
                            intent.putExtra("phone", phoneNumber);
                        }
                        break;
                    default:
                        return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (showPushNoti) {
                if (payloadtype == 1) {
                    showCustomNotificationView = true;
                }

                FCMHelper.showNotificationView(this, showCustomNotificationView, intent, dataMap, title);
            }
        }
    }*/

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("WrongConstant")
    private void showNotification(RemoteMessage remoteMessage) throws JSONException {
        Map<String, String> dataMap = remoteMessage.getData();
        // result.clear();
        // Map<String, Object> result = extractDataMap(new JSONObject(remoteMessage.getData()));
        // Retrieve the following from data

        String email = dataMap.get("email");
        String fn = dataMap.get("fn");
        String ln = dataMap.get("ln");
        String company = dataMap.get("co");
        String messageBody = dataMap.get("message");
        String senderName = dataMap.get("name");

        Log.e("UNIWAY_FIREBASE", dataMap.toString());

        if (dataMap.get("payloadtype") != null) {
            // TODO: contentTitles and contentText should be defined here.
            int payloadType = Integer.parseInt(Objects.requireNonNull(dataMap.get("payloadtype")));
            boolean showPushNoti = true;
            Intent intent = null;
            String contentTitle = "";
            String contentText = "";
            try {
                // By default, retrieve the contentText from the message top-level data.
                // Note that message should always be returned from the server
                contentText = dataMap.get("message");
                // Check if a message has been sent from payloads object
                // payloads will vary per payloadType.  Could be a null value
                String payloadsString = dataMap.get("payloads");
                JSONObject payloadData = null;
                try {
                    if (payloadsString != null && !payloadsString.isEmpty()) {
                        JSONObject payloadObject = new JSONObject(payloadsString);
                        if (payloadObject.has("title")) {
                            // this will be the initial value for contentTitle
                            // contentTitle will be updated inside the switch code below ...
                            // if payloadObject.title is empty or null
                            contentTitle = payloadObject.getString("title");
                        }
                        if (payloadObject.has("message")) {
                            // override current contentText value with new value from the payloadObject
                            contentText = payloadObject.getString("message");
                        }
                        payloadData = new JSONObject(payloadsString);
                    }else{
                        payloadData = new JSONObject(dataMap);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                switch (payloadType) {
                    case FCMHelper.PT_VIDEO_CALL_ACCEPT:
                        intent = new Intent(this, CallViewActivity.class);
                        intent.setAction("STOP_RINGTONE_ACTION");
                        intent.putExtra("receiver_token", payloadData.getString("receiver_token"));
                        intent.putExtra("agora_token", payloadData.getString("agora_token"));
                        intent.putExtra("agora_channel_name", payloadData.getString("agora_channel_name"));
                        intent.putExtra("is_receiver", false);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        Log.d("@PT_Video_Message",isAppInBackground ? "background" : "foreground");
                        if (!isAppInBackground) {
                            Intent vcAcceptIntent = new Intent(ACTION_VIDEO_CALL);
                            vcAcceptIntent.putExtra(VC_TYPE, VC_TYPE_ACCEPT);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(vcAcceptIntent);
                        } else {
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                            Ringtone r = RingtoneManager.getRingtone(this.getApplicationContext(), notification);
                            r.play();
                        }
                        Log.d("@PT_Video_ACCEPT","OK");
                        break;
                    case FCMHelper.PT_VIDEO_CALL_REQUEST:
                        intent = new Intent(this, IncomingCallViewActivity.class);
                        intent.setAction("STOP_RINGTONE_ACTION");
                        intent.putExtra("pfirst_name", payloadData.has("pfirst_name") ? payloadData.getString("pfirst_name") : "");
                        intent.putExtra("plast_name", payloadData.has("plast_name") ? payloadData.getString("plast_name") : "");
//                        intent.putExtra("sender_token", payloadData.getString("sender_token"));
                        intent.putExtra("agora_token", payloadData.has("agoraToken") ? payloadData.getString("agoraToken") : "");
//                        intent.putExtra("agora_channel_name", payloadData.getString("agora_channel_name"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        Log.d("@PT_Video_Message",isAppInBackground ? "background" : "foreground");
                        if (payloadData.has("callid")) {
                            Log.d("@COming!!!!!!!!!!!!!!!!!!!!","");
                            intent.putExtra("pcallid", payloadData.getString("callid"));
                        }

                        if (!isAppInBackground) {
                            this.startActivity(intent);
                        } else {
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                            Ringtone r = RingtoneManager.getRingtone(this.getApplicationContext(), notification);
                            r.play();
                        }
                        if(!(payloadData ==null)){
//                            String statusID = bundle.getString("callid");
//                            Intent localMsg = new Intent(ACTION_VIDEO_CALL);
//                            localMsg.putExtra("callId", statusID);
//                            LocalBroadcastManager.getInstance(context).sendBroadcast(localMsg);
                        }
                        Log.d("@PT_Video_Message","HAHAHAHAH!!!!!!!!!!!!!!");
                        break;
                    case FCMHelper.PT_VIDEO_CALL_DECLINE:
                        Intent localMsg = new Intent(ACTION_VIDEO_CALL);
                        localMsg.putExtra(VC_TYPE, VC_TYPE_RECEIVER_DECLINE);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(localMsg);
                        break;
                    case FCMHelper.PT_VIDEO_CALL_SENDER_DECLINE:
                        Intent vcSenderDeclineIntent = new Intent(ACTION_VIDEO_CALL);
                        vcSenderDeclineIntent.putExtra(VC_TYPE, VC_TYPE_SENDER_DECLINE);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(vcSenderDeclineIntent);
                        break;
                    case FCMHelper.PT_Text_Message: // Text Message
                        contentTitle = FCMHelper.getContentTitleWithName(CTITLE_NEW_MESSAGE_W_NAME, senderName);
                        // Navigate to PushMsgActivity
                        intent = new Intent("PushMessageActivity.VIEW");
                        intent.putExtra("title", contentTitle);
                        intent.putExtra("message", messageBody);
                        intent.putExtra("siteName", dataMap.get("siteName"));
                        intent.putExtra("timesent", dataMap.get("timesent"));
                        break;
                    case FCMHelper.PT_Orders:
                        break;
                    case FCMHelper.PT_Funds_Sent_Transactions:
                        contentTitle = CTITLE_PAYMENT_RECEIVED;
                        contentText = String.format(CTEXT_SEND_MONEY_W_NAME, senderName);
                        intent = new Intent(this, TransactionHistoryActivity.class);
                        break;
                    case FCMHelper.PT_Share_Location:
                        break;
                    case FCMHelper.PT_Reschedule_Appt:
                        break;
                    case FCMHelper.PT_Invoice_Sent:
                        break;
                    case FCMHelper.PT_Share_Task:
                        break;
                    case FCMHelper.PT_Consumer_Parcel_Req:
                        break;
                    case FCMHelper.PT_Driver_Parcel_Bid:
                        break;
                    case FCMHelper.PT_Driver_Food_Request:
                        break;
                    case FCMHelper.PT_Driver_Food_Grab:
                        break;
                    case FCMHelper.PT_Valet_Request:
                        break;
                    case FCMHelper.PT_Valet_Parked:
                        break;
                    case FCMHelper.PT_Valet_Get_Car:
                        break;
                    case FCMHelper.PT_Send_Order:
                        break;
                    case FCMHelper.PT_Rest_Rec:
                        break;
                    case FCMHelper.PT_Rest_Prep:
                        break;
                    case FCMHelper.PT_Rest_Complete:
                        break;
                    case FCMHelper.PT_Rest_Del:
                        break;
                    case FCMHelper.PT_Share_My_Info:
                        break;
                    case FCMHelper.PT_Dial_Phone:
                        break;
                    default:
                        return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String channelId = this.getString(R.string.default_notification_channel_id);
            if (showPushNoti) {

                if (intent == null) {
                    return;
                }

                NotificationCompat.Builder notificationBuilder = FCMHelper.buildNotificationView(this, channelId, intent, contentTitle, contentText);
                if (payloadType == 1) {
                    // Get details from the Contact Form
                    String siteName = dataMap.get("siteName"); // From Contact Form
                    String subject = dataMap.get("subject"); // From Contact Form
                    String imgURL = dataMap.get("imgURL");
                    String timeSent = dataMap.get("timesent");

                    notificationBuilder = FCMHelper.buildCustomNotificationView(this, channelId, intent, senderName, contentText, contentTitle, siteName, subject, imgURL, email, timeSent);
                }
                showNotificationView(this, notificationBuilder, channelId);
            }
        }
    }

}
