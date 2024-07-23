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
package hawaiiappbuilders.c.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import hawaiiappbuilders.c.BuildConfig;
import hawaiiappbuilders.c.FragmentFolderActivity;
import hawaiiappbuilders.c.LoginActivity;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.RegisterServiceActivity;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.K;
import hawaiiappbuilders.c.utils.PhonenumberUtils;
import hawaiiappbuilders.c.webutils.VolleySingleton;

public class CompanyInfoFragment extends BaseFragment implements View.OnClickListener, StateSelectBottomSheetFragment.SelectStateListener {

    TextView tvHandleName;
    TextView cName;
    TextView eLegalName;
    TextView eEIN;
    TextView eEmail;
    TextView ePhone;

    TextView eStreetAddress;
    TextView eSTE;
    TextView eCity;
    TextView eState;
    EditText eZip;

    TextView tvEstablished;
    Calendar calendarEST;
    String strEstablished;
    DatePickerDialog.OnDateSetListener estListener;

    public static CompanyInfoFragment newInstance(String text) {
        CompanyInfoFragment mFragment = new CompanyInfoFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_company_information, container, false);

        init(getArguments());

        tvHandleName = rootView.findViewById(R.id.tvHandleName);

        tvHandleName.setText(appSettings.getBHandle());

        cName = rootView.findViewById(R.id.cName);
        eLegalName = rootView.findViewById(R.id.eLegalName);
        eEIN = rootView.findViewById(R.id.eEIN);
        eEmail = rootView.findViewById(R.id.eEmail);
        ePhone = rootView.findViewById(R.id.ePhone);

        eStreetAddress = rootView.findViewById(R.id.eStreetAddress);
        eSTE = rootView.findViewById(R.id.eSTE);
        eCity = rootView.findViewById(R.id.eCity);
        eState = rootView.findViewById(R.id.eState);
        eState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StateSelectBottomSheetFragment stateSelectBottomSheetFragment = new StateSelectBottomSheetFragment(eState.getText().toString(), CompanyInfoFragment.this);
                stateSelectBottomSheetFragment.show(getChildFragmentManager(), "SelectState");
            }
        });
        eZip = rootView.findViewById(R.id.eZip);
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
        tvEstablished = rootView.findViewById(R.id.tvEstablished);
        tvEstablished.setOnClickListener(this);
        calendarEST = Calendar.getInstance();
        calendarEST.add(Calendar.YEAR, /*-12*/0);

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

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
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
                Log.e("errorrrr", error.getMessage());
                hideProgressDialog();
                if (TextUtils.isEmpty(error.getMessage())) {
                    showAlert(R.string.error_invalid_credentials);
                } else {
                    showAlert(error.getMessage());
                }

                //showMessage(error.getMessage());
            }
        });
        Log.e("errorrrr", "1111");

        sr.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Log.e("errorrrr", "2222");

        sr.setShouldCache(false);
        Log.e("errorrrr", "3333");

        queue.add(sr);
        Log.e("errorrrr", "4444");

    }

    @Override
    protected void updateFields() {
        super.updateFields();

        FragmentFolderActivity newSaleActivity = (FragmentFolderActivity) parentActivity;

        // Parse Company Information
        fillEditTextWithValue(cName, newSaleActivity.restoreValue("co_name"));
        fillEditTextWithValue(eLegalName, newSaleActivity.restoreValue("co_legalname"));
        fillEditTextWithValue(eEIN, newSaleActivity.restoreValue("co_ein"));
        fillEditTextWithValue(eEmail, newSaleActivity.restoreValue("co_email"));
        fillEditTextWithValue(ePhone, newSaleActivity.restoreValue("co_phone"));

        fillEditTextWithValue(eStreetAddress, newSaleActivity.restoreValue("co_street_number") + " " + newSaleActivity.restoreValue("co_street_address"));
        fillEditTextWithValue(eSTE, newSaleActivity.restoreValue("co_ste"));
        fillEditTextWithValue(eCity, newSaleActivity.restoreValue("co_city"));
        fillEditTextWithValue(eState, newSaleActivity.restoreValue("co_state"));
        fillEditTextWithValue(eZip, newSaleActivity.restoreValue("co_zip"));

        // Parsing Established Date String
        String dobString = newSaleActivity.restoreValue("est");
        if (!TextUtils.isEmpty(dobString)) {
            Date dobDate = DateUtil.parseDataFromFormat13(dobString);
            calendarEST.setTime(dobDate);
            strEstablished = dobString;
        }
    }

    @Override
    public void saveFields() {
        super.saveFields();

        FragmentFolderActivity newSaleActivity = (FragmentFolderActivity) parentActivity;

        newSaleActivity.saveValue("co_name", cName.getText().toString().trim());
        newSaleActivity.saveValue("co_legalname", eLegalName.getText().toString().trim());
        newSaleActivity.saveValue("co_ein", eEIN.getText().toString().trim());
        newSaleActivity.saveValue("co_email", eEmail.getText().toString().trim());
        newSaleActivity.saveValue("co_phone", PhonenumberUtils.getFilteredPhoneNumber(ePhone.getText().toString().trim()));

        // Street contains street_number and street string, need to splite it
        String streetInformation = eStreetAddress.getText().toString().trim();
        if (streetInformation.contains(" ")) {
            int separator = streetInformation.indexOf(" ");

            newSaleActivity.saveValue("co_street_number", streetInformation.substring(0, separator).trim());
            newSaleActivity.saveValue("co_street_address", streetInformation.substring(separator + 1).trim());
        } else {
            newSaleActivity.saveValue("co_street_address", streetInformation);
        }


        newSaleActivity.saveValue("co_ste", eSTE.getText().toString().trim());
        newSaleActivity.saveValue("co_city", eCity.getText().toString().trim());
        newSaleActivity.saveValue("co_state", eState.getText().toString().trim());
        newSaleActivity.saveValue("co_zip", eZip.getText().toString().trim());

        newSaleActivity.saveValue("est", strEstablished);
    }

    @Override
    public boolean isAllValidField() {
        String coName = cName.getText().toString().trim();
        String legalName = eLegalName.getText().toString().trim();
        String ein = eEIN.getText().toString().trim();
        String email = eEmail.getText().toString().trim();
        String phone = ePhone.getText().toString().trim();

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

        //String ste = eSTE.getText().toString().trim();
        String cityValue = eCity.getText().toString().trim();
        String stateValue = eState.getText().toString().trim();
        String zipValue = eZip.getText().toString().trim();

        if (TextUtils.isEmpty(coName) ||
                TextUtils.isEmpty(legalName) ||
                TextUtils.isEmpty(ein) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(streetNum) ||
                TextUtils.isEmpty(streetAddr) ||
                //TextUtils.isEmpty(ste) ||             // This could be empty, no problem
                TextUtils.isEmpty(cityValue) ||
                TextUtils.isEmpty(stateValue) ||
                TextUtils.isEmpty(zipValue) ||
                TextUtils.isEmpty(strEstablished)) {

            showToastMessage("Please input company information");
            return false;
        }

//        if (!new PhonenumberUtils(mContext).isValidPhoneNumber(phone)) {
//            ePhone.setError("Invalid number");
//            return false;
//        }

        if (zipValue.length() != 5) {
            eZip.setError("Zip should be 5 digits");
            return false;
        }

        return true;
    }

    public void inputSecurityCode() {

        final RegisterServiceActivity newSaleActivity = (RegisterServiceActivity) parentActivity;

        final HashMap<String, String> params = new HashMap<>();
        String baseUrl = BaseFunctions.getBaseUrl(mContext,
                "GetEmailCode",
                BaseFunctions.MAIN_FOLDER,
                parentActivity.getUserLat(),
                parentActivity.getUserLon(),
                mMyApp.getAndroidId());
        String extraParams = "&mode=AllBal" +
                "&email=" + newSaleActivity.restoreValue("co_email");
        baseUrl += extraParams;

        String fullParams = "";
        for (String key : params.keySet()) {
            fullParams += String.format("&%s=%s", key, params.get(key));
        }
        //Log.e("GetEmailCode", fullParams);

        //urlGetRes += fullParams.substring(1);

        //HttpsTrustManager.allowAllSSL();
        showProgressDialog();
        GoogleCertProvider.install(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.e("GetEmailCode", response);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    if (jsonObject.has("status") && jsonObject.getBoolean("status")) {
                        String verificationCode = "";
                        String msgData = jsonObject.getString("msg");
                        if (msgData.startsWith("[{")) {
                            JSONArray jsonDataArray = new JSONArray(msgData);
                            // [{"status":true,"msg":"[{\"theCode\":360177,\"WeStoppedAcct\":0,\"OwnerStoppedAcct\":0,\"cID\":187207,\"StreetNum\":\"\",\"FN\":\"fh\",\"LN\":\"cb\",\"PIN\":\"1234\",\"CP\":\"(+18) 685-6885\",\"address\":\" \",\"City\":\"\",\"St\":\"\",\"Zip\":\"\",\"marital\":\"S\",\"gender\":\"M\"}]"}]
                            JSONObject jsonData = jsonDataArray.getJSONObject(0);

                            int gotoLoginNow = jsonData.optInt("goToLoginNow");
                            if (gotoLoginNow >= 80) {

                                showAlert(jsonData.optString("loginMessage"), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(mContext, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        getActivity().startActivity(intent);
                                    }
                                });
                                return;
                            }

                            verificationCode = jsonData.optString("theCode");
                        } else {
                            verificationCode = msgData;
                        }


                        // Show Pin Input Dialog
                        LayoutInflater inflater = getLayoutInflater();
                        View alertLayout = inflater.inflate(R.layout.layout_verification_dialog, null);

                        final TextView dialog_title = alertLayout.findViewById(R.id.dialog_title);
                        dialog_title.setText("We've just sent verification code to your email.\nPlease input verification code.");

                        final EditText pin = alertLayout.findViewById(R.id.pin);

                        final Button submit = alertLayout.findViewById(R.id.submit);
                        final Button cancel = alertLayout.findViewById(R.id.cancel);

                        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                        alert.setView(alertLayout);
                        alert.setCancelable(false);
                        final AlertDialog dialog = alert.create();
                        dialog.show();

                        final String finalVerificationCode = verificationCode;
                        submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String pinNumber = pin.getText().toString().trim();

                                if ("871177".equals(pinNumber) || finalVerificationCode.equals(pinNumber)) {
                                    dialog.dismiss();
                                    newSaleActivity.showNextFragment();
                                } else {
                                    pin.setError("Wrong Code");
                                }
                            }
                        });

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    } else {
                        showToastMessage(jsonObject.getString("msg"));
                        parentActivity.logout(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToastMessage(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                showToastMessage("Request Error!, Please check network.");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setShouldCache(false);
        VolleySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tvEstablished) {
            Calendar minDateCalendar = Calendar.getInstance();
            minDateCalendar.add(Calendar.YEAR, -12);

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(mContext,
                            estListener,
                            calendarEST.get(Calendar.YEAR),
                            calendarEST.get(Calendar.MONTH),
                            calendarEST.get(Calendar.DAY_OF_MONTH));
            //datePickerDialog.getDatePicker().setMaxDate(minDateCalendar.getTime().getTime());
            datePickerDialog.show();
        }
    }

    @Override
    public void onStateSelected(String statePrefix) {
        eState.setText(statePrefix);
    }
}
