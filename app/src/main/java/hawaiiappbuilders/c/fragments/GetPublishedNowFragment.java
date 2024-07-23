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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import hawaiiappbuilders.c.RegisterServiceActivity;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.webutils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class GetPublishedNowFragment extends BaseFragment implements View.OnClickListener {

    AppCompatCheckBox chkConfirm;

    public static GetPublishedNowFragment newInstance(String text) {
        GetPublishedNowFragment mFragment = new GetPublishedNowFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    FrameLayout frameLayout;
    LinearLayout mainView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_getpublishednow, container, false);

        init(getArguments());

        frameLayout = rootView.findViewById(R.id.layout_congratulations);
        mainView = rootView.findViewById(R.id.mainView);

        rootView.findViewById(R.id.btnPublish).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateFields() {
        super.updateFields();
    }

    @Override
    public void saveFields() {
        super.saveFields();
    }

    @Override
    public boolean isAllValidField() {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnPublish) {
            registerUserInfo();
        }
    }

    private void registerUserInfo() {

        RegisterServiceActivity newSaleActivity = (RegisterServiceActivity) parentActivity;

        if (parentActivity.getLocation()) {

            final Map<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrlForRegistration(mContext,
                    "AddPer",
                    BaseFunctions.MAIN_FOLDER,
                    parentActivity.getUserLat(),
                    parentActivity.getUserLon(),
                    mMyApp.getAndroidId());

            // No need to set again, but set again here:)
            String extraParams = "&email=" + newSaleActivity.restoreValue("email") +
                    "&un=" + newSaleActivity.restoreValue("email") +
                    "&pw=" + newSaleActivity.restoreValue("user_password") +
                    "&marital=" + "?" +
                    "&gender=" + "?" +
                    "&fn=" + newSaleActivity.restoreValue("firstname") +
                    "&ln=" + newSaleActivity.restoreValue("lastname") +
                    "&phone=" + newSaleActivity.restoreValue("phone") +
                    "&dob=" + newSaleActivity.restoreValue("DOB") +
                    "&street_number=" + newSaleActivity.restoreValue("street_number") +
                    "&street=" + newSaleActivity.restoreValue("street_address") +
                    "&ste=" + "?" +
                    "&city=" + newSaleActivity.restoreValue("city") +
                    "&state=" + newSaleActivity.restoreValue("state") +
                    "&zip=" + newSaleActivity.restoreValue("zip") +
                    "&pin=" + newSaleActivity.restoreValue("pincode") +
//                    "&co=" + newSaleActivity.restoreValue("co_name") +
//                    "&cid=" + "0" +
                    "&editCID=" + appSettings.getUserId() +  // In case of having cid by verifyEmail api, then use  +
//                    "&editstoreid=" + "0" +
                    "&industryID=" + "80" +
                    "&srvPrv=" + "0" +
                    "&handle=" + newSaleActivity.restoreValue("handle") +
                    "&token=" + appSettings.getDeviceToken() +
                    "&countryCode=" + appSettings.getCountryCode();

            baseUrl += extraParams;

            Log.e("Request", baseUrl);

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }
            Log.e("AddPer", fullParams);

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    Log.e("AddPerRes", response);

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;
                            String status = jsonObject.getString("status");
                            if (jsonObject.getBoolean("status")) {
                                // showToastMessage("Successfully saved owner information.");
                                int newMLID = Integer.parseInt(jsonObject.getString("msg").replace("NewOwnerID:", ""));
                                /*if (jsonObject.has("newCID")) {
                                    newMLID = jsonObject.getInt("newCID");
                                } else if (jsonObject.has("ownerID")) {
                                    newMLID = jsonObject.getInt("ownerID");
                                } else {
                                    newMLID = jsonObject.getInt("msg");
                                            //.replace("NewOwnerID:", "").trim();
                                }*/

                                // Set User ID
                                appSettings.setUserId(newMLID);

                                addBiz(newMLID);
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
                    // showToastMessage("Request Error!, Please check network.");

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

    private void addBiz(int ownerID) {
        new AlertDialog.Builder(mContext)
                .setTitle("Final Steps")
                .setMessage("Click to continue to add your business")
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                RegisterServiceActivity newSaleActivity = (RegisterServiceActivity) parentActivity;

                                appSettings.setWorkid(0);

                                final Map<String, String> params = new HashMap<>();
                                String baseUrl = BaseFunctions.getBaseUrl(mContext,
                                        "AddBiz",
                                        BaseFunctions.MAIN_FOLDER,
                                        parentActivity.getUserLat(),
                                        parentActivity.getUserLon(),
                                        mMyApp.getAndroidId());

                                String extraParams = "&email=" + newSaleActivity.restoreValue("co_email") +
                                        "&co=" + newSaleActivity.restoreValue("co_name").replace("&", "|||") +
                                        "&est=" + newSaleActivity.restoreValue("est") +
                                        "&ln=" + newSaleActivity.restoreValue("lastname") +
                                        "&fn=" + newSaleActivity.restoreValue("firstname") +
                                        "&countryCode=" + appSettings.getCountryCode() +
                                        "&legalName=" + newSaleActivity.restoreValue("co_legalname").replace("&", "|||") +
                                        "&EIN=" + newSaleActivity.restoreValue("co_ein").replace("&", "|||") +
//                                        "&cp=" + newSaleActivity.restoreValue("phone") +
                                        "&wp=" + newSaleActivity.restoreValue("co_phone") +
                                        "&street_number=" + newSaleActivity.restoreValue("co_street_number") +
                                        "&street=" + newSaleActivity.restoreValue("co_street_address") +
                                        "&ste=" + newSaleActivity.restoreValue("co_ste").replace("#", "") +
                                        "&city=" + newSaleActivity.restoreValue("co_city") +
                                        "&state=" + newSaleActivity.restoreValue("co_state") +
                                        "&zip=" + newSaleActivity.restoreValue("co_zip") +
                                        "&ownerID=" + ownerID +
                                        "&repEID=" + "0"/*appSettings.getEmpId()*/ +
                                        "&industryID=" + appSettings.getIndustryid() +
                                        "&editstoreID=" + "0" +
//                                        "&sellerID=" + "0" +
                                        "&contactMethod=" + "0" +
                                        "&DMaker=" + "0" +
                                        "&SOLD=" + "0" +
                                        "&handle=" + newSaleActivity.restoreValue("bhandle") +
                                        "&token=" + appSettings.getDeviceToken() +
                                        "&referCP=" + newSaleActivity.restoreValue("phone") +
                                        "&WelcomeMsg=" + newSaleActivity.restoreValue("welcomemsg");

                                baseUrl += extraParams;
                                Log.e("Request", baseUrl);

                                showProgressDialog();
                                RequestQueue queue = Volley.newRequestQueue(mContext);

                                //HttpsTrustManager.allowAllSSL();
                                GoogleCertProvider.install(mContext);

                                StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        hideProgressDialog();

                                        Log.e("AddBizRes", response);

                                        if (response != null || !response.isEmpty()) {
                                            try {
                                                JSONArray jsonArray = new JSONArray(response);
                                                JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;
                                                String status = jsonObject.getString("status");
                                                if (jsonObject.getBoolean("status")) {
                                                    showToastMessage("Successfully saved owner information.");

                                                    int newStoreID = 0;
                                                    if (jsonObject.has("newStoreID")) {
                                                        newStoreID = jsonObject.getInt("newStoreID");
                                                    } else if (jsonObject.has("NewStoreID")) {
                                                        newStoreID = jsonObject.getInt("NewStoreID");
                                                    }

                                                    // Set WorkID
                                                    appSettings.setWorkid(newStoreID);

                                                    if (newStoreID == 0) {
                                                        // Check newStoreID
                                                        showToastMessage("Need new StoreID");
                                                        return;
                                                    }

                                                    int empID = 0;
                                                    if (jsonObject.has("empID")) {
                                                        empID = jsonObject.getInt("empID");
                                                    } else if (jsonObject.has("EmpID")) {
                                                        empID = jsonObject.getInt("EmpID");
                                                    }

                                                    // Set WorkID
                                                    appSettings.setEmpId(empID);

                                                    if (empID == 0) {
                                                        // Check newStoreID
                                                        showToastMessage("Need empID");
                                                        return;
                                                    }

                                                    registerVideoLink();

//                                                    registerDriver(0, newSaleActivity.restoreValue("handle"));

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
                                        if (TextUtils.isEmpty(error.getMessage())) {
                                            showAlert("Request Error!, Please check network.");
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
                        })
                .show();
    }

    private void registerVideoLink() {

        RegisterServiceActivity newSaleActivity = (RegisterServiceActivity) parentActivity;
        String uLink = newSaleActivity.restoreValue("ulink");

        Log.e("test", uLink);

        if (uLink.length() <= 10) {
            mainView.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            Animation bounceAnimation = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
            frameLayout.startAnimation(bounceAnimation);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    parentActivity.finish();
                }
            }, 4000);

            return;
        }

        if (uLink.length() > 11) {
            uLink = uLink.substring(0, 11);
        }

        // CJLSet(mode=77&WorkID=newWorkID&cID=xx&empID=0&industryID=0&PromoTrackID=0&Amt=0&OrderID=0&UTC=0&mins=0&devid=xx&appname=xx&uuid=xx&lon=xx&lat=xx&address=xx&title=)
        // Calls API

        final HashMap<String, String> params = new HashMap<>();

        String baseUrl = BaseFunctions.getBaseUrl(mContext,
                "CJLSet",
                BaseFunctions.MAIN_FOLDER,
                parentActivity.getUserLat(),
                parentActivity.getUserLon(),
                mMyApp.getAndroidId());

        // params.put("UTC", String.valueOf(0/*appSettings.getCUTC()*/));
        String extraParams = "&mode=77" +
                "&empID=" + appSettings.getEmpId() +
                "&industryID=" + "80" +
                "&promoid=" + "0" +
                "&Amt=" + "0" +
                "&OrderID=" + "0" +
                "&mins=" + "0" +
                "&address=" + uLink +
                "&title=" + "" +
                "&tolat=" + "0" +
                "&tolon=" + "0";
        baseUrl += extraParams;

        Log.e("request", "request -> " + baseUrl);

        //showProgressDialog();
        GoogleCertProvider.install(mContext);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //hideProgressDialog();
                Log.e("response", "response -> " + response);

                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {

                            //showToastMessage(jsonObject.getString("msg"));
                        } else if (jsonObject.has("status") && jsonObject.getBoolean("status")) {
                            //showToastMessage("Your request was successful");

                            //parentActivity.finish();

                            mainView.setVisibility(View.GONE);
                            frameLayout.setVisibility(View.VISIBLE);
                            Animation bounceAnimation = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
                            frameLayout.startAnimation(bounceAnimation);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    parentActivity.finish();
                                }
                            }, 4000);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showToastMessage("Request Error!, Please check network.");
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
        VolleySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    private void registerDriver(int capabilitiesID, String handle) {
        // DriverAdd(int devid, long empID, int cID, int workID,int CapabilitiesID, string appname, string Lon, string Lat, string UUID, string Handle)

        /*if (!getLocation()) {
            return;
        }*/

        RegisterServiceActivity newSaleActivity = (RegisterServiceActivity) parentActivity;
        if (parentActivity.getLocation()) {
            String baseUrl = BaseFunctions.getBaseUrl(mContext,
                    "DriverAdd",
                    BaseFunctions.MAIN_FOLDER,
                    parentActivity.getUserLat(),
                    parentActivity.getUserLon(),
                    mMyApp.getAndroidId());

            final Map<String, String> params = new HashMap<>();

            // No need to set again, but set again here:)

            String extraParams =
                    "&CapabilitiesID=" + capabilitiesID +
                            "&Handle=" + handle;
            baseUrl += extraParams;

            Log.e("Request", baseUrl);

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
                                parentActivity.finish();

                                /*Intent dataIntent = parentActivity.getIntent();
                                dataIntent.putExtra("email", email);
                                dataIntent.putExtra("password", password);
                                parentActivity.setResult(RESULT_OK, dataIntent);
                                parentActivity.finish();*/
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
                    //showToastMessage("Request Error!, Please check network.");

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
