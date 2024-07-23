package hawaiiappbuilders.c;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.GoogleCertProvider;


public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    EditText edtFN;
    EditText edtLN;
    EditText edtPhone;
    EditText edtEmail;
    EditText edtPassword;
    EditText edtConfirm;

    private static final int GENDER_MALE = 1;
    private static final int GENDER_FEMALE = 2;
    int genderOption = GENDER_MALE;

    private static final int MARITAL_MARRIED = 1;
    private static final int MARITAL_SINGLE = 2;
    private static final int MARITAL_DEVORCED = 3;
    int maritalOption = MARITAL_SINGLE;

    EditText edtDOB;
    EditText edtStreetNum;
    EditText edtStreet;
    EditText edtSte;
    EditText edtState;
    EditText edtCity;
    EditText edtZip;

    private String PIN = "";

    private String extraParams;

    String strDOB = "";
    Calendar calendarDOB;
    DatePickerDialog.OnDateSetListener dobListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent intent = getIntent();
        String countryCode = intent.getStringExtra("phoneCode");
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String email = intent.getStringExtra("email");

        edtFN = (EditText) findViewById(R.id.edtFirstName);
        edtLN = (EditText) findViewById(R.id.edtLastName);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        edtEmail = (EditText) findViewById(R.id.edtTitle);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtConfirm = (EditText) findViewById(R.id.edtConfirm);

        edtPhone.setText(String.format("%s%s", countryCode, phoneNumber));
        edtEmail.setText(email);

        ((RadioGroup) findViewById(R.id.groupGender)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radioMale) {
                    genderOption = GENDER_MALE;
                } else if (i == R.id.radioFemale) {
                    genderOption = GENDER_FEMALE;
                }
            }
        });

        ((RadioGroup) findViewById(R.id.groupMarital)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radioMarried) {
                    maritalOption = MARITAL_MARRIED;
                } else if (i == R.id.radioSingle) {
                    maritalOption = MARITAL_SINGLE;
                } else if (i == R.id.radioDivorced) {
                    maritalOption = MARITAL_DEVORCED;
                }
            }
        });

        edtDOB = (EditText) findViewById(R.id.edtDOB);
        edtDOB.setKeyListener(null);
        edtDOB.setOnClickListener(this);
        calendarDOB = Calendar.getInstance();
        calendarDOB.add(Calendar.YEAR, -12);
        strDOB = DateUtil.toStringFormat_14(calendarDOB.getTime());
        edtDOB.setText(strDOB);
        dobListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendarDOB.set(Calendar.YEAR, year);
                calendarDOB.set(Calendar.MONTH, monthOfYear);
                calendarDOB.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                strDOB = DateUtil.toStringFormat_14(calendarDOB.getTime());
                edtDOB.setText(strDOB);
            }
        };

        edtStreetNum = (EditText) findViewById(R.id.edtStreetNum);
        edtStreet = (EditText) findViewById(R.id.edtStreet);
        edtSte = (EditText) findViewById(R.id.edtSte);
        edtState = (EditText) findViewById(R.id.edtState);
        edtCity = (EditText) findViewById(R.id.edtCity);
        edtZip = (EditText) findViewById(R.id.edtZip);

        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.btnBack) {
            finish();
        } else if (viewId == R.id.btnRegister) {

            registerUser();
        } else if (viewId == R.id.edtDOB) {
            Calendar minDateCalendar = Calendar.getInstance();
            minDateCalendar.add(Calendar.YEAR, -12);

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(mContext,
                            dobListener,
                            calendarDOB.get(Calendar.YEAR),
                            calendarDOB.get(Calendar.MONTH),
                            calendarDOB.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMaxDate(minDateCalendar.getTime().getTime());
            datePickerDialog.show();
        }
    }

    private void registerUser() {

        // Go to Location Permission
        if (checkLocationPermission() == false) {
            startActivity(new Intent(mContext, ActivityPermission.class));
            return;
        }

        getLocation();

        hideKeyboard(edtFN);
        hideKeyboard(edtLN);
        hideKeyboard(edtEmail);
        hideKeyboard(edtPhone);
        hideKeyboard(edtPassword);
        hideKeyboard(edtConfirm);

        hideKeyboard(edtStreetNum);
        hideKeyboard(edtStreet);
        hideKeyboard(edtSte);
        hideKeyboard(edtCity);
        hideKeyboard(edtState);
        hideKeyboard(edtZip);

        final String firstName = edtFN.getText().toString().trim();
        final String lastName = edtLN.getText().toString().trim();
        final String email = edtEmail.getText().toString().trim();
        final String phone = edtPhone.getText().toString().trim();
        final String password = edtPassword.getText().toString().trim();
        final String confirm = edtConfirm.getText().toString().trim();

        final String streetnum = edtStreetNum.getText().toString().trim();
        final String street = edtStreet.getText().toString().trim();
        final String ste = edtSte.getText().toString().trim();
        final String city = edtCity.getText().toString().trim();
        final String state = edtState.getText().toString().trim();
        final String zip = edtZip.getText().toString().trim();

        // Check Names
        if (TextUtils.isEmpty(firstName)) {
            edtFN.setError(getText(R.string.error_invalid_userame));
            showAlert(R.string.error_invalid_userame);
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            edtLN.setError(getText(R.string.error_invalid_userame));
            showAlert(R.string.error_invalid_userame);
            return;
        }

        // Check mail
        if (!isValidEmail(email)) {
            edtEmail.setError(getText(R.string.error_invalid_email));
            showAlert(R.string.error_invalid_email);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError(getText(R.string.error_password));
            showAlert(R.string.error_password);
            return;
        }

        if (password.length() < 5) {
            edtPassword.setError(getText(R.string.error_invalid_password));
            showAlert(R.string.error_invalid_password);
            return;
        }

        /*if (!isValidPassword(password)) {
            showAlert(R.string.error_invalid_password);
            return;
        }*/

        if (!password.equals(confirm)) {
            edtConfirm.setError(getText(R.string.error_password_not_match));
            showAlert(R.string.error_password_not_match);
            return;
        }

        /*// Check Address
        if (TextUtils.isEmpty(streetnum)) {
            edtStreetNum.setError(getText(R.string.error_address));
            showAlert(R.string.error_address);
            return;
        }

        if (TextUtils.isEmpty(street)) {
            edtStreet.setError(getText(R.string.error_address));
            showAlert(R.string.error_address);
            return;
        }

        if (TextUtils.isEmpty(city)) {
            edtCity.setError(getText(R.string.error_address));
            showAlert(R.string.error_address);
            return;
        }

        if (TextUtils.isEmpty(state)) {
            edtState.setError(getText(R.string.error_address));
            showAlert(R.string.error_address);
            return;
        }*/

        if (TextUtils.isEmpty(zip)) {
            edtZip.setError(getText(R.string.error_address));
            showAlert(R.string.error_address);
            return;
        }

        String marital = "";
        if (maritalOption == MARITAL_MARRIED) {
            marital = "M";
        } else if (maritalOption == MARITAL_SINGLE) {
            marital = "S";
        } else if (maritalOption == MARITAL_DEVORCED) {
            marital = "D";
        }
        String gender = "";
        if (genderOption == GENDER_MALE) {
            gender = "M";
        } else if (genderOption == GENDER_FEMALE) {
            gender = "F";
        }
        String stateValue = state.length() > 0 ? state.substring(0, 2) : state;
        extraParams = "&marital=" + marital +
                "&gender=" + gender +
                "&un=" + email +
                "&pw=" + password +
                "&fn=" + firstName +
                "&ln=" + lastName +
                "&phone=" + phone +
                "&dob=" + strDOB +
                "&industryID=" + "0" +
                "&street_number=" + streetnum +
                "&street=" + street +
                "&ste=" + ste +
                "&city=" + city +
                "&state=" + stateValue +
                "&zip=" + zip +
                "&editCID=" + appSettings.getUserId() +
                "&editstoreid=" + 0 +
                "&srvPrv=" + appSettings.getALev();

        // Show Pin Input Dialog
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_alert_dialog, null);
        final EditText pin = alertLayout.findViewById(R.id.pin);
        final EditText cpin = alertLayout.findViewById(R.id.pin_confirm);
        final Button submit = alertLayout.findViewById(R.id.pin_submit);
        final Button cancel = alertLayout.findViewById(R.id.pin_cancel);
        cancel.setVisibility(View.GONE);
        submit.setText("Create PIN");

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.show();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pinNumber = pin.getText().toString().trim();
                String confirmPinNumber = cpin.getText().toString().trim();
                boolean pinTrue = false;
                boolean cpinTrue = false;
                boolean cTrue = false;

                if (confirmPinNumber.isEmpty() || confirmPinNumber.length() < 4) {
                    cpin.setError("Please, confirm your pin");
                    cpinTrue = false;
                } else {
                    cpinTrue = true;
                }

                if (pinNumber.isEmpty() || pinNumber.length() < 4) {
                    pin.setError("PIN must be 4 - 10 characters long");
                    pinTrue = false;
                } else {
                    pinTrue = true;
                }

                if (cpinTrue && pinTrue && pinNumber.equalsIgnoreCase(confirmPinNumber)) {
                    cTrue = true;
                } else {
                    cpin.setText("");
                    cpin.setError("Please, confirm your pin");
                    cTrue = false;
                }

                if (cTrue) {
                    PIN = pinNumber;
                    extraParams += "&pin=" + PIN;

                    dialog.dismiss();

                    hideKeyboard();

                    // verifyPhone(params);
                    callRegister(extraParams);
                }
            }
        });
    }

    private void callRegister(String extraParams) {

        if (getLocation()) {

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "AddPer", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());
            baseUrl += extraParams;
            final String email = edtEmail.getText().toString().trim();
            final String password = edtPassword.getText().toString().trim();

            hideKeyboard();

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    Log.e("AddPer", response);

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;
                            String status = jsonObject.getString("status");
                            if (jsonObject.getBoolean("status")) {
                                String msg = jsonObject.getString("msg");
                                //[{"status":true,"msg":"Got here again or existing email so we will use this ID:186148"}]
                                String keywordID = "ID:";
                                int indexID = msg.indexOf(keywordID);
                                String userID = "";
                                if (indexID > 0) {
                                    userID = msg.substring(indexID + keywordID.length());
                                    registerDriver(userID, email, password);
                                } else {
                                    showAlert("Couldn't get new User ID");
                                }
                            } else {
                                showAlert(jsonObject.getString("msg"));
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
                    showToastMessage("Request Error!, Please check network.");

                    //showMessage(error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return new HashMap<>();
                }
            };

            sr.setShouldCache(false);
            queue.add(sr);
        }
    }

    private void registerDriver(String userID, final String email, final String password) {
        // DriverAdd(int devid, long empID, int cID, int workID,int CapabilitiesID, string appname, string Lon, string Lat, string UUID, string Handle)

        /*if (!getLocation()) {
            return;
        }*/

        if (getLocation()) {
            String baseUrl = BaseFunctions.getBaseUrl(mContext, "DriverAdd", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());


            final Map<String, String> params = new HashMap<>();

            // No need to set again, but set again here:)

            String extraParams = "&CapabilitiesID=0" +
                    "&Handle=" + email;
            baseUrl += extraParams;

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }
            Log.e("DriverAdd", fullParams);

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    Log.e("DriverAdd", response);

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;
                            String status = jsonObject.getString("status");
                            if (jsonObject.getBoolean("status")) {

                                showToastMessage("Register Success!");

                                Intent dataIntent = getIntent();
                                dataIntent.putExtra("email", email);
                                dataIntent.putExtra("password", password);
                                setResult(RESULT_OK, dataIntent);
                                finish();
                            } else {
                                showAlert(jsonObject.getString("msg"));
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
                    showToastMessage("Request Error!, Please check network.");

                    //showMessage(error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return params;
                }
            };

            sr.setShouldCache(false);
            queue.add(sr);
        }
    }
}
