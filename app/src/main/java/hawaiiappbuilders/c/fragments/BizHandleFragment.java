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

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.RegisterServiceActivity;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.GoogleCertProvider;

public class BizHandleFragment extends BaseFragment implements View.OnClickListener {

    public static BizHandleFragment newInstance(String text) {
        BizHandleFragment mFragment = new BizHandleFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    EditText edtBizHandle;
    Handler mCheckNameHandler;

    Button btnCheckName;
    Button btnNext;

    TextView textResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_biz_handle, container, false);

        init(getArguments());

        textResult = rootView.findViewById(R.id.textResult);
        edtBizHandle = (EditText) rootView.findViewById(R.id.edtUsername);
        mCheckNameHandler = new Handler(getActivity().getMainLooper()) {
            @Override
            public void handleMessage(@NonNull android.os.Message msg) {
                super.handleMessage(msg);

                checkUserName();
            }
        };

        String blockCharacterSet = "@'{}\"\\/~#^|$%&*!;";
        InputFilter filter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };
        edtBizHandle.setFilters(new InputFilter[]{filter});
        edtBizHandle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String userName = edtBizHandle.getText().toString().trim();
                if (userName.length() > 4) {
                    //mCheckNameHandler.removeMessages(0);
                    //mCheckNameHandler.sendEmptyMessageDelayed(0, 3000);
                    btnCheckName.setVisibility(View.VISIBLE);
                } else {
                    btnCheckName.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnNext = rootView.findViewById(R.id.btnNext);
        btnNext.setVisibility(View.GONE);

        btnCheckName = rootView.findViewById(R.id.btnCheckName);
        rootView.findViewById(R.id.btnCheckName).setOnClickListener(this);
        btnCheckName.setVisibility(View.GONE);

        rootView.findViewById(R.id.btnNext).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void checkUserName() {

        String userName = edtBizHandle.getText().toString().trim().replace("@", "");

        edtBizHandle.setText(userName);
        //final String theLastCP = edtPayNotes.getText().toString().trim();

        if (TextUtils.isEmpty(userName)) {
            showToastMessage("Please input handle.");
            return;
        }

        if (userName.contains(" ")) {
            showToastMessage("Space is not allowed.");
            return;
        }

        if (parentActivity.getLocation()) {
            HashMap<String, String> params = new HashMap<>();
            String baseUrl = BaseFunctions.getBaseUrlForRegistration(getActivity(),
                    "cjlGet",
                    BaseFunctions.MAIN_FOLDER,
                    parentActivity.getUserLat(),
                    parentActivity.getUserLon(),
                    mMyApp.getAndroidId());
            String extraParams =
                    "&mode=" + "testUN" +
                            "&phoneTime=" + DateUtil.toStringFormat_12(new Date()) +
                            "&misc=" + userName +
                            "&bizHandle=" + edtBizHandle.getText().toString() +
                            "&countrycode=" + appSettings.getCountryCode();
            baseUrl += extraParams;
            Log.e("Request", baseUrl);

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            String finalBaseUrl = baseUrl;
            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    Log.e("testUN", response);

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray baseJsonArray = new JSONArray(response);
                            if (baseJsonArray.length() > 0) {
                                JSONObject statusObject = baseJsonArray.getJSONObject(0);
                                if (statusObject.has("status") && !statusObject.optBoolean("status")) {
                                    textResult.setVisibility(View.VISIBLE);
                                    showToastMessage("Good choice! That name is available");
                                    btnNext.setVisibility(View.VISIBLE);
                                    btnCheckName.setVisibility(View.GONE);
                                } else {
                                    textResult.setVisibility(View.GONE);
                                    showToastMessage(statusObject.optString("msg"));
                                    hideKeyboard(edtBizHandle);
                                    btnCheckName.setVisibility(View.VISIBLE);
                                }
                            } else {
                                showAlert("Not able to contact the Attendee using Notifications.\n" +
                                        "You might want to call them.");
                                textResult.setVisibility(View.GONE);
                            }
                        } catch (JSONException jsonException) {
                            textResult.setVisibility(View.GONE);
                            showToastMessage("Sorry! That name is not available");
                            hideKeyboard(edtBizHandle);
                            btnCheckName.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            textResult.setVisibility(View.GONE);
                            showAlert(e.getMessage());
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideProgressDialog();
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
    public void onClick(View view) {
        int viewid = view.getId();
        if (viewid == R.id.btnNext) {
            String userName = edtBizHandle.getText().toString().trim();
            appSettings.setBHandle(userName);

            RegisterServiceActivity newSaleActivity = (RegisterServiceActivity) parentActivity;
            newSaleActivity.saveValue("bhandle", userName);

            newSaleActivity.showNextFragment();
        } else if (viewid == R.id.btnCheckName) {
            checkUserName();
        }
    }
}
