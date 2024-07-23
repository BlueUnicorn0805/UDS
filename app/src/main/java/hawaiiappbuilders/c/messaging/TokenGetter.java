package hawaiiappbuilders.c.messaging;

import static hawaiiappbuilders.c.messaging.FCMHelper.PT_Text_Message;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hawaiiappbuilders.c.MyApplication;
import hawaiiappbuilders.c.AppSettings;
import hawaiiappbuilders.c.BaseActivity;
import hawaiiappbuilders.c.model.FCMTokenData;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.GoogleCertProvider;

public class TokenGetter {

    FCMHelper.OnGetTokenListener callBack;
    Context context;
    BaseActivity parentActivity;
    String lon;
    String lat;

    int tokenMlid;

    public TokenGetter(int tokenMlid, Context context, BaseActivity parentActivity, String lat, String lon) {
        // initializing the callback object from the constructor
        this.callBack = null;
        this.context = context;
        this.parentActivity = parentActivity;
        this.lon = lon;
        this.lat = lat;
        this.tokenMlid = tokenMlid;
    }

    public TokenGetter(int tokenMlid, Context context, BaseActivity parentActivity) {
        // initializing the callback object from the constructor
        this.callBack = null;
        this.context = context;
        this.parentActivity = parentActivity;
        if (parentActivity.getLocation()) {
            this.lon = parentActivity.getUserLon();
            this.lat = parentActivity.getUserLat();
        }
        this.tokenMlid = tokenMlid;
    }

    public void getToken(int payloadType, JSONObject payloadsData, FCMHelper.OnGetTokenListener callBack) {
        this.callBack = callBack;
        if (parentActivity.getLocation()) {
            MyApplication application = (MyApplication) parentActivity.getApplication();
            cjlGetToken(payloadType, payloadsData, context, parentActivity, tokenMlid, lat, lon, application.getAndroidId());
        }
    }

    public void cjlGetToken(int payloadType, JSONObject payloadsData, Context context, BaseActivity parentActivity, int tokenMlid, String lat, String lon, String androidId) {
        HashMap<String, String> params = new HashMap<>();
        String baseUrl = BaseFunctions.getBaseUrl(context,
                "CJLGetToken",
                BaseFunctions.MAIN_FOLDER,
                lat,
                lon,
                androidId);


        String mode = "0";
        // storeowner, pos
        if (payloadType == PT_Text_Message) {
            mode = "7";
        }

        String extraParams =
                "&mode=" + mode +
                        "&TokenMLID=" + tokenMlid;
        baseUrl += extraParams;
        Log.e("Request", baseUrl);

        parentActivity.showProgressDialog();
        RequestQueue queue = Volley.newRequestQueue(context);
        // HttpsTrustManager.allowAllSSL();
        GoogleCertProvider.install(context);
        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parentActivity.hideProgressDialog();
                Log.e("CJLGetToken", response);
                prepareDataForPush(response, payloadsData, payloadType);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.onVolleyError(error);
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

    private void prepareDataForPush(String response, JSONObject payloadsData, int payloadType) {
        if (response != null && response.length() > 15) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                if (jsonArray.length() == 0) {
                    callBack.onJsonArrayEmpty();
                } else {
                    ArrayList<FCMTokenData> tokenList = new ArrayList<>();
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                        parentActivity.showToastMessage(jsonObject.optString("msg"));
                    } else {
                        callBack.onSuccess(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonData = jsonArray.getJSONObject(i);
                            String token = jsonData.getString("Token");
                            int osType = jsonData.optInt("OS");
                            tokenList.add(new FCMTokenData(token, osType));
                        }
                        callBack.onFinishPopulateTokenList(tokenList);
                    }

                    if (!tokenList.isEmpty()) {
                        /*"data": {
                            "payloadtype": payloadType,
                            "SenderID": "",
                            "name": "",
                            "fn": "",
                            "ln": "",
                            "co": "",
                            "email": "",
                            "siteName":  "",
                            "imgURL"
                            "payloads": {
                                // payloadType specific data
                            },
                             "message": "" // will be used as the notification contentText
                        }*/
                        sendPushNotification(context, tokenList, payloadType, payloadsData);
                    } else {
                        callBack.onTokenListEmpty();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callBack.onJsonException();
            }
        } else {
            callBack.onEmptyResponse();
        }
    }

    public ArrayList<FCMTokenData> getTokenList(JSONArray jsonArray) {
        ArrayList<FCMTokenData> tokenList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                String token = jsonData.getString("Token");
//                int osType = jsonData.optInt("OS");
//                tokenList.add(new FCMTokenData(token, osType));
                tokenList.add(new FCMTokenData(token, FCMTokenData.OS_ANDROID));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tokenList;
    }

    public void sendPushNotification(Context context, ArrayList<FCMTokenData> tokenList, int payloadType, JSONObject payloadsData) {
        AppSettings appSettings = new AppSettings(context);
        try {
            JSONObject fcmData = new JSONObject();
            String name = appSettings.getFN() + " " + appSettings.getLN();
            fcmData.put("payloadtype", payloadType);
            fcmData.put("SenderID", appSettings.getUserId());
            fcmData.put("email", appSettings.getEmail());
            fcmData.put("fn", appSettings.getFN());
            fcmData.put("ln", appSettings.getLN());
            fcmData.put("name", name);
            fcmData.put("co", appSettings.getCompany());
            if (payloadsData != null) {
                fcmData.put("payloads", payloadsData);
            }
            new SendPushTask(payloadType, tokenList, fcmData).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.onJsonException();
        }
    }

}
