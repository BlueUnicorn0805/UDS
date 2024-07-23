package hawaiiappbuilders.c;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import hawaiiappbuilders.c.R;

import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.model.User;
import hawaiiappbuilders.c.utils.BaseFunctions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG  = MyFirebaseInstanceIDService.class.getSimpleName();

    SharedPreferences settings;

    @Override
    public void onTokenRefresh() {
        settings = getApplicationContext().getSharedPreferences(BaseActivity.GLOBAL_SETTING, Context.MODE_PRIVATE);

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        settings.edit().putString("pushtoken", refreshedToken).commit();

        Log.d(TAG, "Refreshed token: " + refreshedToken);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userName = settings.getString("username", "Anonymous");
            User user = new User();
            user.setName(userName);
            user.setNotificationToken(refreshedToken);

            // Save data
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
        }

        // Save on backend db
        /*AppSettings appSettings = new AppSettings(getApplicationContext());
        MyApplication myApps = (MyApplication) getApplication();
        if (appSettings.isLoggedIn()) {
            int userId = appSettings.getUserId();

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject();
                jsonObject.put("userid", userId);
                jsonObject.put("token", refreshedToken);
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
                    //showMessage(error.getMessage());
                }
            });

            sr.setShouldCache(false);
            queue.add(sr);
        }*/
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(refreshedToken);
    }
}
