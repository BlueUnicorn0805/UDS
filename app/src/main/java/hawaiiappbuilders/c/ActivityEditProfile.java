package hawaiiappbuilders.c;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import hawaiiappbuilders.c.fragments.StateSelectBottomSheetFragment;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.K;
import hawaiiappbuilders.c.utils.PhonenumberUtils;
import hawaiiappbuilders.c.view.OpenSansTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityEditProfile extends BaseActivity implements View.OnClickListener, StateSelectBottomSheetFragment.SelectStateListener {

    Spinner spinnerTimeZone;
    String[] timezoneNames;
    String[] timezoneValues;
    String selectedUTC = "";

    EditText cName;
    EditText eLegalName;
    EditText eEIN;
    EditText eEmail;
    EditText ePhone;

    EditText eStreetAddress;
    EditText eSTE;
    EditText eCity;
    TextView eState;
    EditText eZip;

    TextView tvEstablished;
    Calendar calendarEST;
    String strEstablished;
    OpenSansTextView tvHandleName;

    DatePickerDialog.OnDateSetListener estListener;

    String TAG = "editprofile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        findViewById(R.id.ivLogo).setOnClickListener(this);


        // Spinner
        spinnerTimeZone = findViewById(R.id.spinnerTimeZone);
        tvHandleName = findViewById(R.id.tvHandleName);
        int defaultSpinnerIndex = 0;
        timezoneNames = getResources().getStringArray(R.array.spinner_timezone);
        timezoneValues = new String[timezoneNames.length];
        for (int i = 0; i < timezoneNames.length; i++) {
            String timezoneInfo = timezoneNames[i];
            String[] spliteValues = timezoneInfo.split("=");
            timezoneNames[i] = spliteValues[0];
            timezoneValues[i] = spliteValues[1];

            if (timezoneValues[i].equals(appSettings.getUTC())) {
                defaultSpinnerIndex = i;
            }
        }
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, R.layout.simple_spinner_item, timezoneNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        spinnerTimeZone.setAdapter(spinnerArrayAdapter);
        spinnerTimeZone.setSelection(defaultSpinnerIndex);

        spinnerTimeZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUTC = timezoneValues[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUTC = timezoneValues[spinnerTimeZone.getSelectedItemPosition()];
            }
        });

        cName = findViewById(R.id.cName);
        cName.setText(appSettings.getCO());

//        eLegalName and eEIN needs to set, need data in login api response,
//        set them in appSettings new fns will be needed then need to handle here

        eLegalName = findViewById(R.id.eLegalName);
        eLegalName.setText(appSettings.getLegalName());

        eEIN = findViewById(R.id.eEIN);
//        eEIN.setText(appSettings.getEIN());

        eEmail = findViewById(R.id.eEmail);
        eEmail.setText(appSettings.getEmail());

        ePhone = findViewById(R.id.ePhone);
        ePhone.setText(appSettings.getWP());

        eStreetAddress = findViewById(R.id.eStreetAddress);
        eStreetAddress.setText(String.format("%s %s", appSettings.getStreetNum(), appSettings.getStreet()).trim());

        eSTE = findViewById(R.id.eSTE);
        eSTE.setText(appSettings.getSte());
        tvHandleName.setText(appSettings.getWHandle());

        eCity = findViewById(R.id.eCity);
        eCity.setText(appSettings.getCity());

        eState = findViewById(R.id.eState);
        eState.setText(appSettings.getSt());
        eState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StateSelectBottomSheetFragment stateSelectBottomSheetFragment = new StateSelectBottomSheetFragment(eState.getText().toString(), ActivityEditProfile.this);
                stateSelectBottomSheetFragment.show(getSupportFragmentManager(), "SelectState");
            }
        });
        eZip = findViewById(R.id.eZip);
        eZip.setText(appSettings.getZip());
        eZip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                getCityStateFromZip();
            }
        });

        // DOB
        tvEstablished = findViewById(R.id.tvEstablished);
        tvEstablished.setOnClickListener(this);

        Date dobDate = DateUtil.parseDataFromFormat39(appSettings.getDOB());
        calendarEST = Calendar.getInstance();
        calendarEST.setTime(dobDate);
        strEstablished = DateUtil.toStringFormat_13(calendarEST.getTime());
        tvEstablished.setText(strEstablished);
        estListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendarEST.set(Calendar.YEAR, year);
                calendarEST.set(Calendar.MONTH, monthOfYear);
                calendarEST.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                strEstablished = DateUtil.toStringFormat_13(calendarEST.getTime());
                tvEstablished.setText(strEstablished);
            }
        };

        // Button Actions
        findViewById(R.id.setup_qrcode).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.ivLogo) {
            finish();
        } else if (viewId == R.id.btnSave) {
            saveFields();
        } else if (viewId == R.id.tvEstablished) {
            Calendar minDateCalendar = Calendar.getInstance();
            minDateCalendar.add(Calendar.YEAR, -12);

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(mContext,
                            estListener,
                            calendarEST.get(Calendar.YEAR),
                            calendarEST.get(Calendar.MONTH),
                            calendarEST.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMaxDate(minDateCalendar.getTime().getTime());
            datePickerDialog.show();
        } else if (viewId == R.id.setup_qrcode) {
            // Intent qrintent = new Intent(mContext, QRCodeSetupActivity.class);
            // startActivity(qrintent);
        }
    }

    private void getCityStateFromZip() {
        String zipCode = eZip.getText().toString().trim();
        if (zipCode.length() != 5) {
            return;
        }

        hideKeyboard(eZip);

        showProgressDialog();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        //HttpsTrustManager.allowAllSSL();
        GoogleCertProvider.install(mContext);

        String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&region=us&key=%s", zipCode, K.gKy(BuildConfig.G));
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();

                Log.e("CityState", response);
                try {
                    JSONObject returnJSON = new JSONObject(response);
                    if (returnJSON.has("status") && "OK".equals(returnJSON.getString("status"))) {
                        // Save current zipCode
                        JSONObject jsonZipLocationInfo = returnJSON;
                        if (jsonZipLocationInfo.has("results")) {
                            JSONArray jsonArrayResult = jsonZipLocationInfo.getJSONArray("results");
                            if (jsonArrayResult.length() > 0) {
                                JSONObject jsonAddrObj = jsonArrayResult.getJSONObject(0);

                                // Get City and State
                                JSONArray jsonAddressComponentArray = jsonAddrObj.getJSONArray("address_components");
                                for (int i = 0; i < jsonAddressComponentArray.length(); i++) {
                                    JSONObject jsonAddrComponent = jsonAddressComponentArray.getJSONObject(i);
                                    String longName = jsonAddrComponent.getString("long_name");
                                    String shortName = jsonAddrComponent.getString("short_name");
                                    String types = jsonAddrComponent.getString("types");

                                    if (types.contains("administrative_area_level_1")) {
                                        // This means state info
                                        eState.setText(shortName);
                                    } else if (types.contains("locality")) {
                                        eCity.setText(longName);
                                    }
                                }

                                // Get Location
                                JSONObject jsonGeometryObj = jsonAddrObj.getJSONObject("geometry");
                                JSONObject jsonLocationObj = jsonGeometryObj.getJSONObject("location");
                            }
                        }
                    } else {
                        showToastMessage("Couldn't get address information");
                    }
                } catch (JSONException e) {
                    // Error
                    showToastMessage("Couldn't get address information");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                if (TextUtils.isEmpty(error.getMessage())) {
                    showAlert(R.string.error_invalid_credentials);
                } else {
                    showAlert(error.getMessage());
                }

                //showMessage(error.getMessage());
            }
        });

        sr.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        sr.setShouldCache(false);
        queue.add(sr);
    }

    private void saveFields() {
        final String coName = cName.getText().toString().trim();
        String valLegalName = eLegalName.getText().toString().trim();
        String valEIN = eEIN.getText().toString().trim();
        final String email = eEmail.getText().toString().trim();
        final String phone = ePhone.getText().toString().trim();

        String streetNum = "";
        String streetAddr = "";
        String streetInformation = eStreetAddress.getText().toString().trim();
        if (streetInformation.contains(" ")) {
            int separator = streetInformation.indexOf(" ");

            streetNum = streetInformation.substring(0, separator).trim();
            streetAddr = streetInformation.substring(separator + 1).trim();
        } else {
            streetAddr = streetInformation;
        }

        final String ste = eSTE.getText().toString().trim();

        final String cityValue = eCity.getText().toString().trim();
        final String stateValue = eState.getText().toString().trim();
        final String zipValue = eZip.getText().toString().trim();

        if (TextUtils.isEmpty(coName) ||
//                TextUtils.isEmpty(valLegalName) ||
//                TextUtils.isEmpty(valEIN) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(streetNum) ||
                TextUtils.isEmpty(streetAddr) ||
                //TextUtils.isEmpty(ste) ||             // This could be empty, no problem
                TextUtils.isEmpty(cityValue) ||
                TextUtils.isEmpty(stateValue) ||
                TextUtils.isEmpty(zipValue) ||
                TextUtils.isEmpty(strEstablished)) {

            showToastMessage("Please input all fields");
            return;
        }

//        if (!new PhonenumberUtils(mContext).isValidPhoneNumber(phone)) {
//            ePhone.setError("Invalid number");
//            return;
//        }

        if (zipValue.length() != 5) {
            eZip.setError("Zip should be 5 digits");
            return;
        }

        if (valEIN.contains("***")) {
            valEIN = appSettings.getEIN();
        }

        if (getLocation()) {
            final Map<String, String> params = new HashMap<>();

            appSettings.setUTC(selectedUTC);

            String baseUrl = BaseFunctions.getBaseUrl(mContext,
                    "AddBiz",
                    BaseFunctions.MAIN_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    mMyApp.getAndroidId());
            baseUrl += "&srvPrv=-1" + "&email=" + email;

            String extraParams = "&co=" + coName +
                    "&legalName=" + valLegalName +
                    "&EIN=" + valEIN +
                    "&est=" + strEstablished +
                    "&ln=" + appSettings.getLN() +
                    "&ln=" + appSettings.getLN() +
                    "&fn=" + appSettings.getFN() +
                    "&wp=" + PhonenumberUtils.getFilteredPhoneNumber(phone) +
                    "&street_number=" + streetNum +
                    "&street=" + streetAddr +
                    "&ste=" + ste.replace("#", "") +
                    "&city=" + cityValue +
                    "&state=" + stateValue +
                    "&zip=" + zipValue +
                    "&UPDATEDutc=" + selectedUTC +
                    "&ownerID=" + "0" +
                    "&repEID=" + appSettings.getEmpId() +
                    "&industryID=" +/* "-1"*/appSettings.getIndustryid() +
                    "&editstoreID=" + appSettings.getWorkid() +
                    "&contactMethod=" + "0" +
                    "&DMaker=" + "0" +
                    "&SOLD=" + "0";
            baseUrl += extraParams;

            Log.e("request", baseUrl);

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }
            Log.e("AddBiz", fullParams);

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            final String finalStreetAddr = streetAddr;
            final String finalStreetNum = streetNum;
            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;

                            if (jsonObject.optBoolean("status")) {

                                appSettings.setCO(coName);
//                                appSettings.setLegalName(valLegalName);
//                                appSettings.setEIN(valEIN);
                                appSettings.setEmail(email);
                                appSettings.setWP(PhonenumberUtils.getFilteredPhoneNumber(phone));

                                appSettings.setStreet(finalStreetAddr);
                                appSettings.setStreetNum(finalStreetNum);

                                appSettings.setSte(ste);
                                appSettings.setCity(cityValue);
                                appSettings.setSt(stateValue);
                                appSettings.setZip(zipValue);

                                appSettings.setDOB(DateUtil.toStringFormat_39(calendarEST.getTime()));

                                /*showAlert("Successfully saved owner information.", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        finish();
                                    }
                                });*/

                                finish();

                                showToastMessage("Successfully saved business information");
                            } else {
                                showAlert(jsonObject.getString("message"));
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
                    showAlert("Request Error!, Please check network.");

                    //showMessage(error.getMessage());
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

    @Override
    public void onStateSelected(String statePrefix) {
        eState.setText(statePrefix);
    }
}
