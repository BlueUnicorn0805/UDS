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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rilixtech.widget.countrycodepicker.Country;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import hawaiiappbuilders.c.BaseActivity;
import hawaiiappbuilders.c.BuildConfig;
import hawaiiappbuilders.c.RegisterServiceActivity;
import hawaiiappbuilders.c.FragmentFolderActivity;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.K;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class OwnerInfoFragment extends BaseFragment implements OnClickListener, StateSelectBottomSheetFragment.SelectStateListener {

    public static OwnerInfoFragment newInstance(String text) {
        OwnerInfoFragment mFragment = new OwnerInfoFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    TextView tvHandle;
    TextView fName;
    TextView lName;
    TextView eMail;

    CountryCodePicker countryCodePicker;
    EditText pNumber;
    String countryCode = "";

    TextView edtPasss;
    TextView edtConfirm;

    TextView streetAddress;
    TextView city;
    TextView st;
    EditText zip;

    TextView tvDOB;
    Calendar calendarDOB;
    String strDOB;
    DatePickerDialog.OnDateSetListener dobListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_owner_information, container, false);

        init(getArguments());

        // Owner Information
        tvHandle = rootView.findViewById(R.id.tvHandle);
        fName = rootView.findViewById(R.id.fName);
        lName = rootView.findViewById(R.id.lName);
        eMail = rootView.findViewById(R.id.eMail);
        pNumber = rootView.findViewById(R.id.pNumber);
        countryCodePicker = (CountryCodePicker) rootView.findViewById(R.id.countryCodePicker);
        countryCodePicker.setCountryForPhoneCode(appSettings.getCountryCode().isEmpty() ? 1 : Integer.parseInt(appSettings.getCountryCode()));
        countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected(Country selectedCountry) {
                appSettings.setCountryCode(selectedCountry.getPhoneCode());
                ((BaseActivity) getActivity()).setHint(countryCodePicker, pNumber);
            }
        });
        appSettings.setCountryCode(countryCodePicker.getSelectedCountryCode());
        countryCode = appSettings.getCountryCode();
        ((BaseActivity) getActivity()).setHint(countryCodePicker, pNumber);

        // Show Email and Phone
        eMail.setText(appSettings.getEmail());
        pNumber.setText(appSettings.getCP());

        edtPasss = rootView.findViewById(R.id.password);
        edtConfirm = rootView.findViewById(R.id.confirm);

        streetAddress = rootView.findViewById(R.id.streetAddress);
        city = rootView.findViewById(R.id.city);
        st = rootView.findViewById(R.id.st);
        st.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                StateSelectBottomSheetFragment stateSelectBottomSheetFragment = new StateSelectBottomSheetFragment(st.getText().toString(), OwnerInfoFragment.this);
                stateSelectBottomSheetFragment.show(getChildFragmentManager(), "SelectState");
            }
        });
        zip = rootView.findViewById(R.id.zip);
        zip.addTextChangedListener(new TextWatcher() {
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
        tvDOB = rootView.findViewById(R.id.tvDOB);
        tvDOB.setOnClickListener(this);
        calendarDOB = Calendar.getInstance();
        calendarDOB.add(Calendar.YEAR, -12);
        strDOB = DateUtil.toStringFormat_13(calendarDOB.getTime());
        tvDOB.setText(strDOB);
        dobListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendarDOB.set(Calendar.YEAR, year);
                calendarDOB.set(Calendar.MONTH, monthOfYear);
                calendarDOB.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                strDOB = DateUtil.toStringFormat_13(calendarDOB.getTime());
                tvDOB.setText(strDOB);
            }
        };

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void getCityStateFromZip() {
        String zipCode = zip.getText().toString().trim();
        if (zipCode.length() != 5) {
            return;
        }

        hideKeyboard(zip);

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
                                        st.setText(shortName);
                                    } else if (types.contains("locality")) {
                                        city.setText(longName);
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

    @Override
    protected void updateFields() {
        super.updateFields();

        FragmentFolderActivity newSaleActivity = (FragmentFolderActivity) parentActivity;

        // Parse Owner Information
        fillEditTextWithValue(tvHandle, newSaleActivity.restoreValue("handle"));

        fillEditTextWithValue(fName, newSaleActivity.restoreValue("firstname"));
        fillEditTextWithValue(lName, newSaleActivity.restoreValue("lastname"));

        fillEditTextWithValue(eMail, newSaleActivity.restoreValue("email"));
        fillEditTextWithValue(pNumber, newSaleActivity.restoreValue("phone"));

        fillEditTextWithValue(edtPasss, newSaleActivity.restoreValue("user_password"));
        fillEditTextWithValue(edtConfirm, newSaleActivity.restoreValue("user_confirm"));

        fillEditTextWithValue(streetAddress, newSaleActivity.restoreValue("street_number") + " " + newSaleActivity.restoreValue("street_address"));
        fillEditTextWithValue(city, newSaleActivity.restoreValue("city"));
        fillEditTextWithValue(st, newSaleActivity.restoreValue("state"));
        fillEditTextWithValue(zip, newSaleActivity.restoreValue("zip"));

        // Parsing DOB String
        String dobString = newSaleActivity.restoreValue("DOB");
        if (!TextUtils.isEmpty(dobString)) {
            Date dobDate = DateUtil.parseDataFromFormat13(dobString);
            calendarDOB.setTime(dobDate);
            strDOB = dobString;
        }
    }

    @Override
    public void saveFields() {
        super.saveFields();

        FragmentFolderActivity newSaleActivity = (FragmentFolderActivity) parentActivity;

        newSaleActivity.saveValue("firstname", fName.getText().toString().trim());
        newSaleActivity.saveValue("lastname", lName.getText().toString().trim());
        newSaleActivity.saveValue("email", eMail.getText().toString().trim());
        newSaleActivity.saveValue("phone", pNumber.getText().toString().trim());

        newSaleActivity.saveValue("user_password", edtPasss.getText().toString());
        newSaleActivity.saveValue("user_confirm", edtPasss.getText().toString());

        // Street contains street_number and street string, need to splite it
        String streetInformation = streetAddress.getText().toString().trim();
        if (streetInformation.contains(" ")) {
            int separator = streetInformation.indexOf(" ");

            newSaleActivity.saveValue("street_number", streetInformation.substring(0, separator).trim());
            newSaleActivity.saveValue("street_address", streetInformation.substring(separator + 1).trim());
        } else {
            newSaleActivity.saveValue("street_address", streetInformation);
        }

        newSaleActivity.saveValue("city", city.getText().toString().trim());
        newSaleActivity.saveValue("state", st.getText().toString().trim());
        newSaleActivity.saveValue("zip", zip.getText().toString().trim());

        newSaleActivity.saveValue("DOB", strDOB);
    }

    @Override
    public boolean isAllValidField() {
        String firstName = fName.getText().toString().trim();
        String lastName = lName.getText().toString().trim();
        String email = eMail.getText().toString().trim();
        String phone = pNumber.getText().toString().trim();
        String passVal = edtPasss.getText().toString().trim();
        String confirmVal = edtConfirm.getText().toString().trim();

        String streetNum = "";
        String streetAddr = "";
        String streetInformation = streetAddress.getText().toString().trim();
        if (streetInformation.contains(" ")) {
            int separator = streetInformation.indexOf(" ");

            streetNum = streetInformation.substring(0, separator).trim();
            streetAddr = streetInformation.substring(separator + 1).trim();
        } else {
            streetAddr = streetInformation;
        }

        String cityValue = city.getText().toString().trim();
        String stateValue = st.getText().toString().trim();
        String zipValue = zip.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) ||
                TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(passVal) ||
                TextUtils.isEmpty(confirmVal) ||
                TextUtils.isEmpty(streetNum) ||
                TextUtils.isEmpty(streetAddr) ||
                TextUtils.isEmpty(cityValue) ||
                TextUtils.isEmpty(stateValue) ||
                TextUtils.isEmpty(zipValue) ||
                TextUtils.isEmpty(strDOB)) {

            showToastMessage("Please input Owner information");

            return false;
        }

        if (!passVal.equals(confirmVal)) {
            showToastMessage("Password mismatch!");
            return false;
        }

        if (zipValue.length() != 5) {
            zip.setError("Zip should be 5 digits");
            return false;
        }

        return true;
    }

    public void checkPINCode() {

        final RegisterServiceActivity newSaleActivity = (RegisterServiceActivity) parentActivity;

        // Show Pin Input Dialog
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_alert_dialog, null);
        TextView dialogTitle = alertLayout.findViewById(R.id.dialog_title);
        TextView subTitle = alertLayout.findViewById(R.id.dialog_sub_title);
        dialogTitle.setText("Create Your Security PIN");
        subTitle.setText("(Used for purchases)");
        subTitle.setVisibility(View.VISIBLE);
        final EditText pin = alertLayout.findViewById(R.id.pin);
        final EditText cpin = alertLayout.findViewById(R.id.pin_confirm);
        final Button submit = alertLayout.findViewById(R.id.pin_submit);
        final Button cancel = alertLayout.findViewById(R.id.pin_cancel);
        cancel.setVisibility(View.GONE);
        submit.setText(R.string.ok);

        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.show();
        submit.setOnClickListener(new OnClickListener() {
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
                    dialog.dismiss();
                    newSaleActivity.saveValue("pincode", pinNumber);

                    newSaleActivity.showNextFragment();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tvDOB) {
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

    @Override
    public void onStateSelected(String statePrefix) {
        st.setText(statePrefix);
    }
}
