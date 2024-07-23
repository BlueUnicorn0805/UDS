package hawaiiappbuilders.c;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import hawaiiappbuilders.c.model.FavDeliveryUser;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;
import info.hoang8f.android.segmented.SegmentedGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    SegmentedGroup segmentedGroup;
    View panelUserInput;
    View btnFinger;
    RadioButton btnPassword;
    RadioButton btnBioAuth;

    EditText edtEmail;
    EditText edtPassword;

    private String pushToken;
    int retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pushToken = mSettings.getString("pushtoken", "");
        // TOKEN IS MISSING BECAUSE IT IS SAVING THE pushtoken IN A SEPARATE SHAREDPREFERENCE INSTANCE, NOT IN APPSETTINGS
        // TOKEN SHOULD BE SAVED USING APPSETTINGS
        /*FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                pushToken = instanceIdResult.getToken();
                mSettings.edit().putString("pushtoken", pushToken).commit();
            }
        });*/

        retry = 0;
        getToken();

        edtEmail = (EditText) findViewById(R.id.edtTitle);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        // Fill predefined input
        edtEmail.setText(appSettings.getLoginInput());

        //edtEmail.setText("testfreelancerbd@gmail.com");
        //edtPassword.setText("abcdEF1234##");

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.btnForgotPwd).setOnClickListener(this);
        findViewById(R.id.btnSignup).setOnClickListener(this);
        findViewById(R.id.btnZintaPay).setOnClickListener(this);

        panelUserInput = findViewById(R.id.panelLoginPassword);
        btnFinger = findViewById(R.id.btnFinger);
        btnPassword = findViewById(R.id.btnPassword);
        btnBioAuth = findViewById(R.id.btnBioAuth);

        btnPassword.setChecked(true);
        btnFinger.setVisibility(View.INVISIBLE);

        segmentedGroup = findViewById(R.id.segmentedAuth);
        segmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.btnPassword) {
                    panelUserInput.setVisibility(View.VISIBLE);
                    btnFinger.setVisibility(View.INVISIBLE);
                } else if (checkedId == R.id.btnBioAuth) {
                    panelUserInput.setVisibility(View.INVISIBLE);
                    btnFinger.setVisibility(View.VISIBLE);
                }
            }
        });

        btnFinger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBioAuth();
            }
        });
    }

    public void getToken() {
        if (retry < 100) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> token) {
                    if (token.isSuccessful()) {
                        String newToken = token.getResult();
                        // Log.e("Token3", newToken);
                        Log.e("ActivitySplash", newToken);
                        appSettings.setTokenRetry(retry);
                        appSettings.setDeviceToken(newToken);
                    } else {
                        retry++;
                        Log.e("ActivitySplash", "Retry:getToken:" + retry);
                        getToken();
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.btnBack) {
            finish();
        } else if (viewId == R.id.btnLogin) {
            loginUser();
        } else if (viewId == R.id.btnForgotPwd) {
            startActivity(new Intent(mContext, ForgotPwdActivity.class));
        } else if (viewId == R.id.btnSignup) {
            //startActivityForResult(new Intent(mContext, RegisterActivity.class), REQUEST_REGISTER);
            //startActivityForResult(new Intent(mContext, RegisterPhoneActivity.class), REQUEST_REGISTER);
            //startActivityForResult(new Intent(mContext, RegisterEmailActivity.class), REQUEST_REGISTER);


            Intent intent = new Intent(mContext, RegisterEnrollNowActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

            startActivity(intent);
        } else if (viewId == R.id.btnZintaPay) {
            Intent hintIntent = new Intent(this, AboutZintaActivity.class);
            startActivity(hintIntent);
            overridePendingTransition(R.anim.push_top_in, R.anim.push_bottom_out);
        }
    }

    private void loginUser() {

        // Go to Location Permission
        if (!checkLocationPermission()) {
            startActivity(new Intent(mContext, ActivityPermission.class));
            return;
        }

        // Get Location
        if (!getLocation()) {
            return;
        }

        hideKeyboard(edtEmail);
        hideKeyboard(edtPassword);

        final String email = edtEmail.getText().toString().trim();
        final String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError(getText(R.string.error_invalid_email));
            return;
        }

        if (!isValidEmail(email) && !"xiao".equals(email) && !"mama".equals(email)) {
            edtEmail.setError(getText(R.string.error_invalid_email));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError(getText(R.string.error_password));
            return;
        }

        startActivityForResult(new Intent(mContext, ActivityLoginAuth.class), REQUEST_AUTH);
    }

    private void login(String code, boolean isBioAuth) {
        final String email = edtEmail.getText().toString().trim();
        final String password = edtPassword.getText().toString().trim();

        String baseUrl = BaseFunctions.getBaseUrl(mContext,
                "zzzLogin",
                BaseFunctions.MAIN_FOLDER,
                getUserLat(),
                getUserLon(),
                mMyApp.getAndroidId());

        final Map<String, String> params = new HashMap<>();
        // params.put("ver", String.valueOf(URLResolver.currVer));
        String bio;
        if (isBioAuth) {
            bio = "PAST";
        } else {
            bio = "";
        }
        String extraParams = "&un=" + email +
                "&pw=" + password +
                "&token=" + appSettings.getDeviceToken() +
                "&retry=" + appSettings.getTokenRetry() +
                "&currmlid=" + "0" +
                "&userZAcode=" + code +
                "&hostMLID=" + "182" +
                "&bio=" + bio;
        baseUrl += extraParams;

        Log.e("Request", baseUrl);


        // Try to Signup here
        showProgressDialog();

        GoogleCertProvider.install(mContext);

        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();

                Log.e("login", response);

                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONArray jsonResponse = new JSONArray(response);
                        JSONObject jsonObject = jsonResponse.getJSONObject(0);
                        if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                            showAlert(jsonObject.getString("msg"));
                        } else {

                            // Save Last Login Information
                            appSettings.setLoginInput(edtEmail.getText().toString().trim());

                            // Get User Information
                            FavDeliveryUser user = new FavDeliveryUser();

                            user.setUserID(jsonObject.getInt("UserID"));
                            user.setFN(jsonObject.getString("FN"));
                            user.setLN(jsonObject.getString("LN"));
                            user.setDOB(jsonObject.getString("ESTAB"));
                            user.setPIN(jsonObject.getString("PIN"));
                            user.setWP(jsonObject.getString("WP"));
                            user.setCP(jsonObject.getString("CP"));
//                            user.setUTC(jsonObject.getString("cUTC"));
                            user.setUTC(jsonObject.getString("wUTC"));
                            user.setEmail(jsonObject.getString("wEmail"));


                            user.setStreetNum(jsonObject.getString("StreetNum"));
                            user.setStreet(jsonObject.getString("Street"));
                            user.setCity(jsonObject.getString("City"));
                            user.setSt(jsonObject.getString("St"));
                            user.setZip(jsonObject.getString("Zip"));

                            user.setGender(jsonObject.optString("gender"));
                            user.setMarital(jsonObject.optString("marital"));

                            //user.setWorkID(jsonObject.getString("WorkID"));
                            // In GF and AVA, always set 0
                            user.setWorkID(jsonObject.getString("WorkID"));

                            // Additional Fields for Emp
                            //user.setEmpId(jsonObject.getString("EmpID"));
                            //user.setDepartID(jsonObject.getString("DepartID"));
                            //user.setDepartName(jsonObject.getString("DepartName"));
                            //user.setAccessLevel(jsonObject.getString("aLev"));
                            //user.setIndustryID(jsonObject.getString("IndustryID"));

                            appSettings.putInt("LOGIN_LIMIT", 0);
                            appSettings.setUserId(user.getUserID());
                            appSettings.setCO(jsonObject.optString("Co"));
                            appSettings.setFN(user.getFN());
                            appSettings.setLN(user.getLN());
                            appSettings.setDOB(user.getDOB());
                            appSettings.setPIN(user.getPIN());

//                            appSettings.setLegalName(jsonObject.optString("LegalName"));
//                            appSettings.setEIN(jsonObject.optString("EIN"));

                            appSettings.setSte(jsonObject.optString("STE"));

                            appSettings.setWP(user.getWP());
                            appSettings.setCP(user.getCP());
                            appSettings.setUTC(user.getUTC());

                            appSettings.setEmail(user.getEmail());
                            appSettings.setStreetNum(user.getStreetNum());
                            appSettings.setStreet(user.getStreet());
                            appSettings.setCity(user.getCity());
                            appSettings.setSt(user.getSt());
                            appSettings.setZip(user.getZip());

                            appSettings.setGendar(user.getGender());
                            appSettings.setMarital(user.getMarital());

                            // Save Employer credentials
                            appSettings.setEmpId(jsonObject.getInt("EmpID"));
                            appSettings.setWorkid(jsonObject.getInt("WorkID"));
                            appSettings.setIndustryid(jsonObject.getString("IndustryID"));

                            appSettings.setWHandle(jsonObject.optString("wHandle"));

                            appSettings.setCountryCode(jsonObject.optString("wCountryCode"));

                            //appSettings.setDepartId(jsonObject.getString("DepartID"));
                            appSettings.setDepartName(jsonObject.getString("DepartName"));
                            //appSettings.setALev(jsonObject.getString("aLev"));
                            //appSettings.setIndustryid(jsonObject.getString("IndustryID"));

                            // Save user credentials
                            Gson gson = new Gson();
                            String json = gson.toJson(user);
                            appSettings.putString("USER_OBJECT", json);

                            GetMyDriverID();
                            /*// Show Profile Screen
                            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                            startActivityForResult(profileIntent, REQUEST_PROFILE);*/
                        }
                    } catch (JSONException e) {
                        showAlert("Data Error!");
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                showToastMessage(getResources().getString(R.string.error_server_response));
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                /*params.put("P1", BaseFunctions.getHackValue(mContext));
                params.put("R1", BaseFunctions.R1());
                BaseFunctions.handleHankRice(params, mContext);*/

                return params;
            }
        };

        sr.setShouldCache(false);
        queue.add(sr);
    }

    private void GetMyDriverID() {
        // Reset driver id
        appSettings.setDriverID("");

        if (getLocation()) {

            String baseUrl = BaseFunctions.getBaseUrl(mContext,
                    "CJLGet",
                    BaseFunctions.MAIN_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    mMyApp.getAndroidId());
            String extraParams = "&mode=DelGetID" +
                    "&industryID=80" + "&sellerID=0" + "&misc" + "";
            baseUrl += extraParams;
            final Map<String, String> params = new HashMap<>();

            Log.e("CJLGet", baseUrl);

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    Log.e("CJLGet", response);

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;

                            // Get Driver ID.
                            if (jsonObject.has("DriverID") && !jsonObject.isNull("DriverID")) {
                                int driverID = jsonObject.getInt("DriverID");
                                if (driverID == -77) {
                                    showToastMessage("Failed to get the Driver ID.");
                                    return;
                                }
                                appSettings.setDriverID(jsonObject.getString("DriverID"));
                            }

                            if (TextUtils.isEmpty(appSettings.getDriverID()) || "0".equals(appSettings.getDriverID())) {

                                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                                alert.setTitle("Welcome!"); // put in strings
                                alert.setMessage("We see you are already registered using OmniVers. You will continue to use that same email for both apps, and now its time to setup you up or your company.");
                                alert.setPositiveButton("Register My Company",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Go to Register
                                                Intent intent = new Intent(mContext, RegisterEnrollNowActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

                                                startActivity(intent);
                                                dialog.dismiss();
                                            }
                                        });
                                alert.setNegativeButton("Not Now",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alert.show();
                            } else {
                                // Go to Main Screen
                                appSettings.setLoggedIn();
                                startActivity(new Intent(mContext, MainActivity.class));
                                finish();
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
                    showAlert("Request Error!, Please check network.");
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
    }

    private void doBioAuth() {
        int authStatus = checkBiometricsAuthentication();
        if (authStatus == BiometricManager.BIOMETRIC_SUCCESS) {
            // Set the authentication

            if (TextUtils.isEmpty(edtEmail.getText().toString().trim())) {
                msg("Please enter the email to login with Birometric Auth.");
                return;
            }

            doBiometricsAuthentication();
        } else if (authStatus == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            //if(appSettings.getBiometricAuthUseStatus() == 1) {
            //    //doMainAction();
            //} else {
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(mContext, R.style.AlertDialogTheme);
            alertDialogBuilder.setTitle("Please confirm");
            alertDialogBuilder.setMessage("To secure data, would you use biometrics authentication?")
                    .setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();

                            //appSettings.setBiometricAuthStatus(0);

                            requestBiometricsSettings();
                        }
                    }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                            //appSettings.setBiometricAuthStatus(1);

                            //doMainAction();
                        }
                    });
            androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            //}
        } else {
            // Go to Main Screen
            //doMainAction();
            showToastMessage("Biometrics Authentication is not supported! Please use password.");
        }
    }

    private int checkBiometricsAuthentication() {

        BiometricManager biometricManager = BiometricManager.from(this);
        int biometricsAuthStatus = biometricManager.canAuthenticate(BIOMETRIC_WEAK | DEVICE_CREDENTIAL);
        switch (biometricsAuthStatus) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                Log.e("MY_APP_TAG", "Biometric can be set in the Settings.");
                break;
        }

        return biometricsAuthStatus;
    }

    private void requestBiometricsSettings() {
        try {
            final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
            enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BIOMETRIC_WEAK | DEVICE_CREDENTIAL);
            startActivityForResult(enrollIntent, REQUEST_AUTH);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
            startActivity(intent);
        }
    }

    private void doBiometricsAuthentication() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                //showToastMessage("Authentication error: " + errString);

                msg("Authentication error: " + errString, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //finish();
                    }
                });
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                showToastMessage("Authentication succeeded!");

                startActivityForResult(new Intent(mContext, ActivityLoginAuth.class), REQUEST_AUTH);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                //showToastMessage("Authentication failed, please try again.");

                msg("Authentication failed, please try again.", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //finish();
                    }
                });
            }
        });

        // Allows user to authenticate using either a Class 3 biometric or
        // their lock screen credential (PIN, pattern, or password).
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric authentication")
                .setSubtitle("Please verify authentication to use the app data.")
                // Can't call setNegativeButtonText() and
                // setAllowedAuthenticators(...|DEVICE_CREDENTIAL) at the same time.
                //.setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                .build();

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_REGISTER && resultCode == RESULT_OK) {
            edtEmail.setText(data.getStringExtra("email"));
            edtPassword.setText(data.getStringExtra("password"));
        } else if (requestCode == REQUEST_AUTH && resultCode == RESULT_OK) {
            String uuid = mMyApp.getAndroidId();
            String code = data.getStringExtra("code");

            if (panelUserInput.getVisibility() == View.VISIBLE) {
                login(code, false);
            } else {
                final String email = edtEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    edtEmail.setError(getText(R.string.error_invalid_email));
                    return;
                }

                if (!isValidEmail(email)) {
                    edtEmail.setError(getText(R.string.error_invalid_email));
                    return;
                }

                login(code, true);
            }
        }
    }
}
