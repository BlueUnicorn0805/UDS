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

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.adapter.TransferAdapter;
import hawaiiappbuilders.c.model.TransferRequest;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.GoogleCertProvider;

public class XTabRequestFunds extends BaseFragment implements View.OnClickListener {

    // Transfer
    TextView tvTrasnAvailableFunds;
    RadioGroup radioGroupTransMode;
    EditText edtAmountToTransfer;
    RecyclerView rvTransfer;

    AppCompatCheckBox chkExpress;

    float avaBalance = 0;
    ArrayList<TransferRequest> transferRequests = new ArrayList<>();
    TransferAdapter transferAdapter;

    public static XTabRequestFunds newInstance(String text) {
        XTabRequestFunds mFragment = new XTabRequestFunds();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);

        return mFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_xtabrequestfunds, container, false);

        init(savedInstanceState);
        initLayout(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get AVA Balance
        getCashData();

        // Get Past Request
        getPastRequest();
    }

    private void initLayout(View parentView) {
        // ------------------------------ Transfer -------------------------------
        tvTrasnAvailableFunds = parentView.findViewById(R.id.tvTrasnAvailableFunds);
        radioGroupTransMode = parentView.findViewById(R.id.radioGroupTransMode);

        edtAmountToTransfer = parentView.findViewById(R.id.edtAmountToTransfer);

        // ------------------------- Past Request -----------------------
        rvTransfer = parentView.findViewById(R.id.lvTransfer);
        rvTransfer.setHasFixedSize(true);
        rvTransfer.setLayoutManager(new LinearLayoutManager(mContext));

        transferAdapter = new TransferAdapter(mContext, transferRequests);
        rvTransfer.setAdapter(transferAdapter);

        parentView.findViewById(R.id.btnSubmitYourRequest).setOnClickListener(this);

        chkExpress = parentView.findViewById(R.id.chkExpress);
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        if (viewID == R.id.btnSubmitYourRequest) {
            // TODO: Ask for PIN
            showPinDlg();
        }
    }

    private void showPinDlg() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_alert_dialog, null);
        final TextView title = alertLayout.findViewById(R.id.dialog_title);
        title.setText("PIN is required to pay now");
        final EditText pin = alertLayout.findViewById(R.id.pin);
        final EditText cpin = alertLayout.findViewById(R.id.pin_confirm);
        final ImageView grey_line = alertLayout.findViewById(R.id.grey_line);
        cpin.setVisibility(View.GONE);
        grey_line.setVisibility(View.GONE);

        final Button submit = alertLayout.findViewById(R.id.pin_submit);
        submit.setText("Continue");

        final Button cancel = alertLayout.findViewById(R.id.pin_cancel);
        cancel.setText("Cancel");

        final androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        alert.setView(alertLayout);
        alert.setCancelable(true);
        final androidx.appcompat.app.AlertDialog dialog = alert.create();
        dialog.show();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pinNumber = pin.getText().toString().trim();
                boolean pinTrue = false;
               //hideKeyboard();
                // TODO:  Change pin requirement to 4
                if (pinNumber.isEmpty() || pinNumber.length() < 2) {
                    pin.setError("PIN must be 4 - 10 characters long");
                    pinTrue = false;
                } else {
                    pinTrue = true;
                }

                String userPin = appSettings.getPIN().trim();
                if (pinTrue && userPin.equalsIgnoreCase(pinNumber)) {
                    dialog.dismiss();

                    performBankRequest();
                } else {
                    showToastMessage("Wrong PIN");
                    dialog.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void getCashData() {
        if (parentActivity.getLocation()) {
            final HashMap<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, parentActivity.getUserLat(), parentActivity.getUserLon(), mMyApp.getAndroidId());
            String extraParams = "&mode=AllBal" +
                    "&misc=" + appSettings.getUserId();
            baseUrl += extraParams;
            Log.e("avaBal", params.toString());

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }

            Log.e("avaBal", fullParams);

            // String urlGetRes = URLResolver.getHashCode() + "CJLGet";
            //urlGetRes += fullParams.substring(1);

            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {

                            showToastMessage(jsonObject.getString("msg"));
                            parentActivity.logout(false);
                        } else {
                            String instaCash = jsonObject.getString("instaCash");
                            String instaSaving = jsonObject.getString("instaSavings");

                            try {
                                float fInstaCash = Float.parseFloat(instaCash);
                                instaCash = String.format("%.02f", fInstaCash);
                                avaBalance = fInstaCash;
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                            try {
                                float fInstaSaving = Float.parseFloat(instaSaving);
                                instaSaving = String.format("%.02f", fInstaSaving);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                            tvTrasnAvailableFunds.setText(String.format("Available Balance\n$ %.02f", avaBalance/* * 0.8*/));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToastMessage(e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    parentActivity.networkErrorHandle(mContext, error);
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

            queue.add(stringRequest);
        }
    }


    // --------------- API Transfer Request -----------------
    private void getPastRequest() {

        // Calls API
        if (parentActivity.getLocation()) {
            final HashMap<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, parentActivity.getUserLat(), parentActivity.getUserLon(), mMyApp.getAndroidId());
            String extraParams = "&mode=ListBankReq" +
                    "&sellerID=" + appSettings.getWorkid();
            baseUrl += extraParams;
            Log.e("ListBankRq", params.toString());

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }

            Log.e("ListBankRq", fullParams);

            RequestQueue queue = Volley.newRequestQueue(mContext);

            GoogleCertProvider.install(mContext);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    if (!TextUtils.isEmpty(response)) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {

                                //showToastMessage(jsonObject.getString("msg"));
                            } else {
                                transferRequests.clear();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject data = jsonArray.getJSONObject(i);
                                    TransferRequest transInfo = new TransferRequest();

                                    if (data.has("ID") && !data.isNull("ID")) {
                                        transInfo.setID(data.getString("ID"));
                                    } else {
                                        transInfo.setID("");
                                    }

                                    if (data.has("amt") && !data.isNull("amt")) {
                                        transInfo.setAmt(data.getString("amt"));
                                    } else {
                                        transInfo.setAmt("0");
                                    }

                                    if (data.has("TxRegID") && !data.isNull("TxRegID")) {
                                        transInfo.setTxRegID(data.getString("TxRegID"));
                                    } else {
                                        transInfo.setTxRegID("0");
                                    }

                                    if (data.has("OrderID") && !data.isNull("OrderID")) {
                                        transInfo.setOrderID(data.getString("OrderID"));
                                    } else {
                                        transInfo.setOrderID("0");
                                    }

                                    if (data.has("CreateDate") && !data.isNull("CreateDate")) {
                                        transInfo.setCreateDate(data.getString("CreateDate"));
                                    } else {
                                        transInfo.setCreateDate("0");
                                    }

                                    if (data.has("Msg") && !data.isNull("Msg")) {
                                        transInfo.setStatus(data.getString("Msg"));
                                    } else if (data.has("Status") && !data.isNull("Status")) {
                                        transInfo.setStatus(data.getString("Status"));
                                    } else {
                                        transInfo.setStatus("0");
                                    }

                                    if (data.has("acct") && !data.isNull("acct")) {
                                        transInfo.setAcct(data.getString("acct"));
                                    } else {
                                        transInfo.setAcct("0");
                                    }

                                    transferRequests.add(transInfo);

                                }

                                transferAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    parentActivity.networkErrorHandle(mContext, error);
                    hideProgressDialog();
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
            queue.add(stringRequest);
        }
    }


    // --------------- API Transfer Request -----------------
    private void performBankRequest() {
        String strValTransfer = edtAmountToTransfer.getText().toString().trim();
        float amountToTransfer = 0;
        try {
            amountToTransfer = Float.parseFloat(strValTransfer);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (amountToTransfer == 0) {
            showToastMessage("Please input correct amount to transfer");
            return;
        }

        boolean bSendToYourBank = radioGroupTransMode.getCheckedRadioButtonId() == R.id.radioSendToBank;
        if (bSendToYourBank && amountToTransfer > avaBalance/* * 0.8*/) {
            showToastMessage("Please input available amount");
            return;
        }

        // Calls API
        if (parentActivity.getLocation()) {
            final HashMap<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "BankRq", BaseFunctions.MAIN_FOLDER, parentActivity.getUserLat(), parentActivity.getUserLon(), mMyApp.getAndroidId());

            String mode;
            if (bSendToYourBank) {
                mode = "ToBank";
            } else {
                mode = "FromBank";
            }

            String express;
            if (chkExpress.isChecked()) {
                express = "1";
            } else {
                express = "0";
            }

            String extraParams = "&mode=" + mode +
                    "&Amt=" + strValTransfer +
                    "&Note=" + "" +
                    "&express=" + express +
                    "&phoneTime=" + DateUtil.toStringFormat_12(new Date()) +
                    "&token=" + appSettings.getDeviceToken();
            baseUrl += extraParams;

            if (bSendToYourBank) {
                Log.e("ToBank", params.toString());
            } else {
                Log.e("FromBank", params.toString());
            }

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }

            Log.e("BankRq", fullParams);

            showProgressDialog();

            RequestQueue queue = Volley.newRequestQueue(mContext);
            GoogleCertProvider.install(mContext);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    Log.e("BankRq", response);

                    hideProgressDialog();

                    if (!TextUtils.isEmpty(response)) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {

                                showToastMessage(jsonObject.getString("msg"));
                            } else if (jsonObject.has("status") && jsonObject.getBoolean("status")) {
                                showAlert("Your request was successful", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Goto HP
                                        parentActivity.finish();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    parentActivity.networkErrorHandle(mContext, error);
                    hideProgressDialog();
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
            queue.add(stringRequest);
        }
    }
}
