package hawaiiappbuilders.c.messaging;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.android.volley.VolleyError;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.model.FCMTokenData;

public class FCMHelper {

    // Payload Types
    public static final int PT_Text_Message = 1; // Text
    public static final int PT_Orders = 2; // Orders
    public static final int PT_Funds_Sent_Transactions = 3; // Funds Sent - Transactions
    public static final int PT_Share_Location = 4; // Share Location
    public static final int PT_Reschedule_Appt = 5; // Reschedule Appointment
    public static final int PT_Invoice_Sent = 6; // Invoice Sent
    public static final int PT_Share_Task = 7; // Share Task
    public static final int PT_Share_My_Info = 8; // Share My Info

    public static final int PT_Consumer_Parcel_Req = 10; // Consumer Parcel Req
    public static final int PT_Driver_Parcel_Bid = 11; // Driver Parcel Bid
    public static final int PT_Driver_Food_Request = 12; // Driver Food Request
    public static final int PT_Driver_Food_Grab = 13; // Driver Food Grab

    public static final int PT_Valet_Request = 20; // Valet Request
    public static final int PT_Valet_Parked = 21; // Valet Parked
    public static final int PT_Valet_Get_Car = 22; // Valet Get Car

    public static final int PT_Send_Order = 30; // Send Order to Rest
    public static final int PT_Rest_Rec = 31; // Rest Rec
    public static final int PT_Rest_Prep = 32; // Rest Prep
    public static final int PT_Rest_Complete = 33; // Rest Complete
    public static final int PT_Rest_Del = 34; // Rest Del

    public static final int PT_Dial_Phone = 99; // Dial Phone Number and Call

    public static final int PT_VIDEO_CALL_REQUEST = 310;
    public static final int PT_VIDEO_CALL_IN = 311;
    public static final int PT_VIDEO_CALL_ACCEPT = 312;
    public static final int PT_VIDEO_CALL_DECLINE = 313;
    public static final int PT_VIDEO_CALL_SENDER_DECLINE = 314;
    public static final int PT_INCOMING_RESP_VIDEO_CALL = 318; // Log for Incoming video call


    // content title formats
    public static final String CTITLE_NEW_MESSAGE_W_NAME = "New Message from %s"; // senderName
    public static final String CTITLE_BID_ACCEPTED = "Your bid was accepted";
    public static final String CTITLE_NEW_DELIVERY = "New Delivery!";
    public static final String CTITLE_NEW_ORDER_W_ID = "New Order #%s"; // orderId
    public static final String CTITLE_PAYMENT_RECEIVED = "Payment Received!";
    public static final String CTITLE_SHARE_LOCATION = "Share Location";
    public static final String CTITLE_RESCHEDULE_APPOINTMENT_REQUEST = "Reschedule Appointment Request";
    public static final String CTITLE_NEW_INVOICE_W_ID = "New Invoice #%s"; // invoiceId
    public static final String CTITLE_NEW_TASK = "You have a new task";
    public static final String CTITLE_NEW_INCOMING_CALL = "New Incoming Call";

    // content message formats
    public static final String CTEXT_NEW_ORDER_REQUEST_W_NAME = "You have a new order request from %s"; // senderName
    public static final String CTEXT_SEND_MONEY_W_NAME = "%s has sent you money"; // senderName
    public static final String CTEXT_SENDER_SHARE_LOCATION = "%s has shared a location with you";
    public static final String CTEXT_BY_NAME = "by %s";
    public static final String CTEXT_FROM_NAME = "by %s";
    public static final String CTEXT_NEW_TASK = "%s has shared a new task with you";
    public static final String CTEXT_DIAL_PHONE_NUMBER = "%s called wanting help to see transactions";

    public static String getContentTitleWithName(String format, String name) {
        return String.format(format, name);
    }

    public static String getContentTextWithName(String format, String name) {
        return String.format(format, name);
    }

    public static String getContentTitleWithId(String format, int id) {
        return String.format(format, id);
    }

    public static String getContentTitleWithId(String format, long id) {
        return String.format(format, id);
    }

    public interface OnGetTokenListener {
        void onSuccess(String response);

        void onVolleyError(VolleyError error);

        void onEmptyResponse();

        void onFinishPopulateTokenList(ArrayList<FCMTokenData> tokenList);

        void onJsonArrayEmpty();

        void onJsonException();

        void onTokenListEmpty();
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            return null;
        }
    }

    // TODO:  Build notification view for onDuty
    public static NotificationCompat.Builder buildCustomNotificationView(Context context, String channelId, Intent intent, String contentTitle, String contentText, String senderName, String siteName, String subject, String imgURL, String email, String timesent) {
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            // Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            @SuppressLint("RemoteViewLayout") RemoteViews notificationLayout = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.layout_notification_small);
            notificationLayout.setTextViewText(R.id.text_app_name, siteName);
            notificationLayout.setTextViewText(R.id.text_title, contentTitle);
            notificationLayout.setTextViewText(R.id.text_subject, subject);
            notificationLayout.setTextViewText(R.id.text_full_message, contentText);
            notificationLayout.setTextViewText(R.id.text_user_details, senderName);
            notificationLayout.setTextViewText(R.id.text_time_sent, timesent);
            notificationLayout.setTextViewText(R.id.text_email, email);
            Bitmap bitmap = getBitmapFromURL(imgURL);
            if (bitmap != null) {
                notificationLayout.setImageViewBitmap(R.id.img, bitmap);
            }
            return new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(notificationLayout)
                    .setSound(defaultSoundUri)
                    .setPriority(100)
                    .setContentIntent(pendingIntent);
        }
        return null;
    }

    public static NotificationCompat.Builder buildNotificationView(Context context, String channelId, Intent intent, String contentTitle, String contentText) {
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            // Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            return new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle())
                    .setSound(defaultSoundUri)
                    .setPriority(100)
                    .setContentIntent(pendingIntent);
        }
        return null;
    }

    public static void showNotificationView(Context context, NotificationCompat.Builder notificationBuilder, String channelId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "seekur notification";
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // notification id should be unique
        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        // UpdateCashBroadcast.sendBroadcast(context);
    }

    private static int getNotificationIcon() {
        return R.mipmap.ic_launcher;
    }

}
