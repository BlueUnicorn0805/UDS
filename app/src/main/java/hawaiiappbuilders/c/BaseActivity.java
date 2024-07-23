/*
 * Copyright 2018 ShineStar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hawaiiappbuilders.c;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hawaiiappbuilders.c.R;

import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseActivity extends AppCompatActivity {

    Context mContext;
    MyApplication mMyApp;
    Typeface mAppFont;
    protected ProgressDialog mProgress;

    SharedPreferences mSettings;
    protected AppSettings appSettings;

    String mUserId;
    int mUserType;

    public static final String GLOBAL_SETTING = "cscs";

    protected static final int REQUEST_REGISTER = 100;
    protected static final int REQUEST_LOCATION = 200;
    protected static final int REQUEST_SIGNUP = 300;
    protected static final int REQUEST_JOB = 500;
    protected static final int REQUEST_AUTH = 600;

    // Permission Requests
    protected static final int PERMISSION_REQUEST_CODE_CAMERA = 100;
    protected static final String[] PERMISSION_REQUEST_CAMERA_STRING = {Manifest.permission.CAMERA};

    // Permission Requests
    protected static final int PERMISSION_REQUEST_CODE_GALLERY = 101;
    protected static final String[] PERMISSION_REQUEST_GALLERY_STRING = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    public static final String[] PERMISSION_REQUEST_PHONE_STRING = {Manifest.permission.CALL_PHONE};
    protected static final int PERMISSION_REQUEST_CODE_LOCATION = 102;
    protected static final String[] PERMISSION_REQUEST_LOCATION_STRING = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    protected static final int PERMISSION_REQUEST_CODE_QRSCAN = 103;
    protected static final String[] PERMISSION_REQUEST_QRSCAN_STRING = {Manifest.permission.CAMERA};

    private GestureDetector mDetector;
    private final int swipeThreshold = 100;
    private final int swipeVelocityThreshold = 100;

    protected String TAG = "AppCommon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mMyApp = (MyApplication) getApplication();

        mSettings = getPreferences(this);
        appSettings = new AppSettings(mContext);

        //TextView mTxtTitle = (TextView) findViewById(R.id.txtTitle);
        //mTxtTitle.setText(getString(R.string.mSettings));

        /*ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }*/

        mProgress = new ProgressDialog(mContext, R.style.DialogTheme);
        mProgress.setMessage(getString(R.string.loading));
        mProgress.setCancelable(false);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });

        /*if ("88c5395c86294668".equalsIgnoreCase(mMyApp.getAndroidId())) {
            DEV_MODE = true;
        } else {
            DEV_MODE = false;
        }*/

        mDetector = new GestureDetector(mContext, new MyGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*if (this.mDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);*/

        return this.mDetector.onTouchEvent(event);
    }

    public int GetPixelFromDips(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // do nothing
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // do nothing
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > swipeThreshold && Math.abs(velocityX) > swipeVelocityThreshold) {
                        /*if (diffX > 0) {
                            Toast.makeText(getApplicationContext(), "Left to Right swipe gesture", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Right to Left swipe gesture", Toast.LENGTH_SHORT).show();
                        }*/
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return true;
        }
    }

    protected boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lat = "0";
            lon = "0";
            return false;
        } else {
            return true;
        }
    }

    private static boolean DEV_MODE = false;
    private String lat = "0.0", lon = "0.0";
    public boolean getLocation() {
        if (checkLocationPermission()) {
            // In case of user Accept Location
            GpsTracker gpsTracker = new GpsTracker(mContext);
            if (gpsTracker.canGetLocation()) {
                lat = String.valueOf(gpsTracker.getLatitude());
                lon = String.valueOf(gpsTracker.getLongitude());

                // Save User Lat and Lon
                appSettings.setLastLocationLat(lat);
                appSettings.setLastLocationLon(lon);

                return true;
            } else {
                gpsTracker.showSettingsAlert();
                return false;
            }
        } else {
            // In case of unknown or Deny
            startActivity(new Intent(mContext, ActivityPermission.class));
            return false;
        }
    }

    public String getUserLat() {
        if (DEV_MODE || "86294668".equals(mMyApp.getAndroidId())) {
            return "41.797273";

        } else {
            return lat;
        }
    }

    public String getUserLon() {
        if (DEV_MODE || "86294668".equals(mMyApp.getAndroidId())) {
            return "123.4287958";
        } else {
            return lon;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(GLOBAL_SETTING, Context.MODE_PRIVATE);
    }

    public void savePreferences(String key, String value) {
        SharedPreferences sharedPreferences = getPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void setHint(CountryCodePicker countryCodePicker, EditText phoneEditText) {
        PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.createInstance(mContext);
        PhoneNumberUtil.PhoneNumberType mobile = PhoneNumberUtil.PhoneNumberType.MOBILE;
        Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.getExampleNumberForType(countryCodePicker.getSelectedCountryNameCode(), mobile);
        if (phoneNumber == null) {
            phoneEditText.setHint("");
            return;
        }
        String hint = mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        phoneEditText.setHint(hint);
    }

    public String getVersionName() {
        PackageInfo pInfo = null;
        int curVerionCode = 0;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "1.0";
        }
    }

    public String loadPreferences(String key) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(GLOBAL_SETTING, MODE_PRIVATE);
            String strSavedMemo = sharedPreferences.getString(key, "");
            return strSavedMemo;
        } catch (NullPointerException nullPointerException) {

            return null;
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showProgressDialog() {
        if (mProgress.isShowing())
            return;

        mProgress.show();
        mProgress.setContentView(R.layout.dialog_loading);
    }

    public void hideProgressDialog() {
        if(mContext== null)
            return;

        if (mProgress.isShowing() && mProgress!=null) {
            mProgress.dismiss();
        }

    }

    // Remove EditText Keyboard
    public void hideKeyboard(EditText et) {
        if (et != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) BaseActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = BaseActivity.this.getCurrentFocus();
        if (view == null) {
            view = new View(BaseActivity.this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /****** CHECK NETWORK CONNECTION *******/
    public static boolean isOnline(Context conn) {
        ConnectivityManager cm = (ConnectivityManager) conn.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /****** HANDLE NETWORK ERROR INSTANCE *******/
    public void networkErrorHandle(Context mContext, VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            //This indicates that the request has either time out or there is no connection
            showToastMessage("The request has either time out or there is no Connection.");
        } else if (error instanceof AuthFailureError) {
            // Error indicating that there was an Authentication Failure while performing the request
            showToastMessage("There was an Authentication Failure.");
        } else if (error instanceof ServerError) {
            //Indicates that the server responded with a error response
            showToastMessage("The server not responded!");
        } else if (error instanceof NetworkError) {
            //Indicates that there was network error while performing the request
            showToastMessage("There was network error, check your network!");
        } else if (error instanceof ParseError) {
            // Indicates that the server response could not be parsed
            showToastMessage("The server response could not be parsed.");
        }
    }

    public void msg(int resId) {
        String msg = getString(resId);
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setMessage(msg);
        alert.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alert.show();
    }

    public void msg(String msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        alert.setMessage(msg);
        alert.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alert.show();
    }

    public void msg(String msg, final View.OnClickListener onClickListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        alert.setMessage(msg);
        alert.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (onClickListener != null) {
                            onClickListener.onClick(null);
                        }
                    }
                });
        AlertDialog alertDialog = alert.show();
    }

    public void closeMsg(int msgId) {
        closeMsg(getString(msgId));
    }

    public void closeMsg(String msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
        alert.setMessage(msg);
        alert.setPositiveButton(getString(R.string.alert_close),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        AlertDialog alertDialog = alert.show();

    }

    boolean isInLogout = false;
    public void logout(final boolean autologout) {
        if (isInLogout == true) {
            return;
        }

        if (getLocation()) {
            isInLogout = true;

            final HashMap<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());
            String extraParams = "&mode=LogOut" + "&misc=" + "";
            baseUrl += extraParams;
            Log.e("logout", params.toString());

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }

            Log.e("logout", fullParams);

            if (!autologout) {
                showProgressDialog();
            }

            GoogleCertProvider.install(mContext);
            RequestQueue queue = Volley.newRequestQueue(mContext);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (!autologout) {
                        hideProgressDialog();
                    }

                    if (!TextUtils.isEmpty(response)) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {

                                showToastMessage(jsonObject.getString("msg"));
                            } else {
                                appSettings.clear();
                                appSettings.logOut();

                                if (!autologout) {
                                    Intent intent = new Intent(mContext, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                    finish();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    isInLogout = false;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    networkErrorHandle(mContext, error);
                    if (!autologout) {
                        hideProgressDialog();
                    }

                    isInLogout = false;
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return params;
                }
            };

            stringRequest.setShouldCache(false);
            queue.add(stringRequest);
        }
    }

    public void showToastMessage(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public void showToastMessage(int msgId) {
        Toast.makeText(mContext, msgId, Toast.LENGTH_SHORT).show();
    }

    public void showAlert(int resId) {
        String alertMsg = getString(resId);
        showAlert(alertMsg, null);
    }

    public void showAlert(int resId, View.OnClickListener clickListener) {
        String alertMsg = getString(resId);
        showAlert(alertMsg, clickListener);
    }

    public void showAlert(String message) {
        showAlert(message, null);
    }

    public void showAlert(String message, final View.OnClickListener clickListener) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_error, null);

        final AlertDialog errorDlg = new AlertDialog.Builder(mContext)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        TextView tvAlert = (TextView) dialogView.findViewById(R.id.tvAlert);
        tvAlert.setText(message);
        dialogView.findViewById(R.id.btnCloseAlert).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                errorDlg.dismiss();
                if (clickListener != null) {
                    clickListener.onClick(v);
                }
            }
        });

        errorDlg.show();
        errorDlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    //*****************************************************************
    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    protected void openLink(String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    protected String getAndroidId() {
        TelephonyManager tm =
                (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("ID", "Android ID: " + androidId);
        return androidId;
    }

    protected String getServiceProvider() {
        String telID = "";
        try {
            TelephonyManager tm =
                    (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            telID = tm.getSimOperatorName();
            if (TextUtils.isEmpty(telID)) {
                telID = tm.getNetworkOperatorName();
            }
        } catch (Exception e) {
            telID = e.getMessage();
        }
        if (TextUtils.isEmpty(telID)) {
            telID = "No Sim Info";
        }

        if (DEV_MODE) {
            return "China Telecom";
        } else {
            return telID;
        }
    }

    protected String getNetworkOperator() {
        String telID = "NetworkOper";
        try {
            TelephonyManager tm =
                    (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            telID = tm.getNetworkOperator();
        } catch (Exception e) {
            telID = e.getMessage();
        }
        if (TextUtils.isEmpty(telID)) {
            telID = "No Sim Info";
        }

        if (DEV_MODE) {
            return "46003";
        } else {
            return telID;
        }
    }

    protected boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (gps_enabled || network_enabled) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Function to show settings alert dialog
     */
    public void showLocationSettingsAlert() {
        showLocationSettingsAlert(null);
    }

    public void showLocationSettingsAlert(final View.OnClickListener listener) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext, R.style.AlertDialogTheme);

        // Setting Dialog Title
        alertDialog.setTitle("GPS Settings");

        // Setting Dialog Message
        alertDialog.setMessage("Turn by Turn directions will guide new customers to your location.\nPlease turn you Location On for this.");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                /*Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, REQUEST_LOCATION);*/

                if (listener != null) {
                    listener.onClick(null);
                }
            }
        });

        /*// on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });*/

        // Showing Alert Message
        alertDialog.show();
    }


    private static float getPixelScaleFactor(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
    }


    public static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static int pxToDp(Context context, int px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

    protected void shareApp() {
        try {
            // Uri imageUri = Uri.fromFile(imageFile);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_body));
            // shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("text/plain");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String chooserTitle = getResources().getString(R.string.share_title);
            startActivity(Intent.createChooser(shareIntent, chooserTitle));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This will be used in Android6.0(Marshmallow) or above
    public static boolean checkPermissions(Context context, String[] permissions, boolean showHintMessage, int requestCode) {

        if (permissions == null || permissions.length == 0)
            return true;

        boolean allPermissionSetted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionSetted = false;
                break;
            }
        }

        if (allPermissionSetted)
            return true;

        // Should we show an explanation?
        boolean shouldShowRequestPermissionRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                shouldShowRequestPermissionRationale = true;
                break;
            }
        }

        if (showHintMessage && shouldShowRequestPermissionRationale) {
            // Show an expanation to the user *asynchronously* -- don't
            // block
            // this thread waiting for the user's response! After the
            // user
            // sees the explanation, try again to request the
            // permission.
            String strPermissionHint = context.getString(R.string.request_permission_hint);
            Toast.makeText(context, strPermissionHint, Toast.LENGTH_SHORT).show();
        }

        ActivityCompat.requestPermissions((Activity) context, permissions, requestCode);

        return false;
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

        if (bAllGranted) {
        } else {
        }
    }

    public Uri getFileUri(File downloadedFile) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Uri fileUri = Uri.fromFile(downloadedFile);
            return fileUri;
        } else {
            Uri fileUri = FileProvider.getUriForFile(
                    getApplicationContext(),
                    getPackageName() + ".provider", // (use your app signature + ".provider" )
                    downloadedFile);
            return fileUri;
        }
    }
}
