/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package hawaiiappbuilders.c.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

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

import java.util.HashMap;
import java.util.Map;

import hawaiiappbuilders.c.ActivityBank;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.GoogleCertProvider;

public class XTabEnterAcct extends BaseFragment implements View.OnClickListener {


    // Bank Information
    View panelBankInfo;
    TextView tvAccount;
    TextView tvAccountConfirm;
    TextView tvRouting;
    TextView tvBankName;

    WebView webViewTerms;

    CheckBox chkAgreeSubmit;
    int bankID = 0;
    //91901024

    View panelVerify;
    EditText tvVerifyAmount;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_xtabenteracct, container, false);

        init(savedInstanceState);
        initLayout(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initLayout(View parentView) {

        // ---------------------------- About the Bank ---------------------------
        panelBankInfo = parentView.findViewById(R.id.panelBankInfo);
        tvAccount = parentView.findViewById(R.id.tvAccount);
        tvAccountConfirm = parentView.findViewById(R.id.tvAccountConfirm);

        tvRouting = parentView.findViewById(R.id.tvRouting);
        tvRouting.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String routing = tvRouting.getText().toString().trim();
                if (routing.length() > 8) {
                    getRoutingCodes(routing);
                }
            }
        });

        // UI Bank Information
        tvBankName = parentView.findViewById(R.id.tvBankName);

        webViewTerms = parentView.findViewById(R.id.webViewTerms);
        WebSettings webSettings = webViewTerms.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webViewTerms.setWebChromeClient(new WebChromeClient() {
            public void onReceivedTitle(WebView view, String title) {
            }
        });
        webViewTerms.setBackgroundColor(Color.WHITE);
        webViewTerms.loadUrl(String.format("file:///android_asset/techs/payment_tc.html"));
        webViewTerms.requestFocus();

        chkAgreeSubmit = parentView.findViewById(R.id.chkAgreeSubmit);
        chkAgreeSubmit.setChecked(true);
        chkAgreeSubmit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    showAlert("Without selecting “I Agree” we will not be able to complete your purchase.");
                }
            }
        });
        parentView.findViewById(R.id.btnSave).setOnClickListener(this);

        // Verify Panel
        panelVerify = parentView.findViewById(R.id.panelVerify);
        panelVerify.setVisibility(View.GONE);
        tvVerifyAmount = parentView.findViewById(R.id.tvVerifyAmount);
        parentView.findViewById(R.id.btnSubmit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        if (viewID == R.id.btnSave) {
            saveBankInformation();
        } else if (viewID == R.id.btnSubmit) {
            hideKeyboard(tvVerifyAmount);
            String amt = tvVerifyAmount.getText().toString().trim();
            if (TextUtils.isEmpty(amt)) {
                showToastMessage("Please enter amount!");
            } else {
                getVerifyStatus(Float.parseFloat(amt), true);
            }
        }
    }

    public void getVerifyStatus(float amt, boolean showResponse) {
        if (parentActivity.getLocation()) {
            String userLat = parentActivity.getUserLat();
            String userLon = parentActivity.getUserLon();

            final Map<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "verifyBank", BaseFunctions.MAIN_FOLDER, parentActivity.getUserLat(), parentActivity.getUserLon(), mMyApp.getAndroidId());
            String extraParams = "&amt=" + amt;
            baseUrl += extraParams;
            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }
            Log.e("verifyBank", fullParams);

            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    Log.e("verifyBank", response);

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray dataArray = new JSONArray(response);

                            JSONObject resultJson = dataArray.getJSONObject(0);
                            int needsVerified = resultJson.optInt("status");
                            if (showResponse) {
                                showAlert(resultJson.optString("msg"), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        parentActivity.finish();
                                    }
                                });
                            } else {
                                if (needsVerified == 30) {
                                    showToastMessage("You have already completed this.");
                                    ((ActivityBank) parentActivity).showTab(0);

                                    appSettings.setTransMoneyStatus(ActivityBank.TRANSFER_VERIFIED);
                                } else if (needsVerified == 20) {
                                    appSettings.setTransMoneyStatus(ActivityBank.TRANSFER_NEEDS_VERIFY);

                                    panelBankInfo.setVisibility(View.GONE);
                                    panelVerify.setVisibility(View.VISIBLE);
                                } else if (needsVerified == 0) {
                                    appSettings.setTransMoneyStatus(ActivityBank.TRANSFER_NOT_SETUP_ACCT);
                                } else if (needsVerified == 25) {
                                    showAlert("Once deposit made, you will be able to enter that amount.", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            parentActivity.finish();
                                        }
                                    });
                                } else if (needsVerified == 35) {
                                    showAlert("Not usable.", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            parentActivity.finish();
                                        }
                                    });
                                }
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
                    //hideProgressDialog();
                    if (TextUtils.isEmpty(error.getMessage())) {
                        showAlert(R.string.error_invalid_credentials);
                    } else {
                        showAlert(error.getMessage());
                    }
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

    synchronized private void getRoutingCodes(String routing) {

        if (routing.length() > 9) {
            showAlert("Your routing number should always be 9 digits.");
            return;
        }

        if (parentActivity.getLocation()) {
            String userLat = parentActivity.getUserLat();
            String userLon = parentActivity.getUserLon();

            final Map<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "MasterBankByRt", BaseFunctions.MAIN_FOLDER, parentActivity.getUserLat(), parentActivity.getUserLon(), mMyApp.getAndroidId());
            String extraParams = "&RT=" + routing;
            baseUrl += extraParams;

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }
            Log.e("RT", fullParams);

            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    Log.e("RT", response);

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray dataArray = new JSONArray(response);
                            JSONObject resultJson = dataArray.getJSONObject(0);
                            if (resultJson.has("status") && resultJson.getBoolean("status") == false) {
                                showAlert(resultJson.getString("msg"));
                                return;
                            }

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataObj = dataArray.getJSONObject(i);
                                bankID = dataObj.getInt("ID");
                                tvBankName.setText(dataObj.getString("BankName"));

                                String city = dataObj.getString("BankCity");
                                String state = dataObj.getString("BankState");
                                String zip = dataObj.getString("BankZip");

                                break;
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
                    //hideProgressDialog();
                    showToastMessage("Request Error!, Please check network.");
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

    private void saveBankInformation() {
        // Send Card Information to the server

        String accountName = tvAccount.getText().toString().trim();
        String accountConfirm = tvAccountConfirm.getText().toString().trim();

        if (bankID == 0) {
            showToastMessage("No Bank information");
            return;
        }

        if (TextUtils.isEmpty(accountName) /*|| accountName.length() < 8 || accountName.length() > 12*/) {
            //showToastMessage("Account number is generally 8 ~ 12 digits");
            showToastMessage("Please enter account number.");
            return;
        }

        if (!accountName.equals(accountConfirm)) {
            showToastMessage("Account number mismatch!");
            return;
        }

        // Check user agreement
        if (!chkAgreeSubmit.isChecked()) {
            showAlert("Without selecting “I Agree” we will not be able to complete your purchase.");
            return;
        }

        if (parentActivity.getLocation()) {
            String userLat = parentActivity.getUserLat();
            String userLon = parentActivity.getUserLon();

            final HashMap<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "AddAcct", BaseFunctions.MAIN_FOLDER, parentActivity.getUserLat(), parentActivity.getUserLon(), mMyApp.getAndroidId());
            String extraParams = "&misc=" + "" +
                    "&acctOwnerID=" + appSettings.getUserId() +
                    "&NOC=" + "0" +
                    "&Acct=" + accountName +
                    "&bankID=" + String.valueOf(bankID) +
                    "&cvv=" + "0" +
                    "&exp4=" + "0" +
                    "&zip=" + "0" +
                    "&Nick=" + "0";
            baseUrl += extraParams;

            Log.e("addCC", params.toString());

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }

            Log.e("addCC", fullParams);

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;
                            if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                                String msg = jsonObject.getString("msg");
                                if (msg.contains("duplicate")) {
                                    showToastMessage("Duplicates not allowed");
                                } else {
                                    showToastMessage(msg);
                                }
                            } else {
                                String newNickId = "0";
                                if (jsonObject.has("NickID") && !jsonObject.isNull("NickID")) {
                                    newNickId = jsonObject.getString("NickID");
                                }

                                showAlert("Saved successfully!", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Goto HP
                                        parentActivity.finish();
                                    }
                                });
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
