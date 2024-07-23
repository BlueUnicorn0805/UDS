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
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import hawaiiappbuilders.c.MainActivity;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.URLResolver;
import hawaiiappbuilders.c.adapter.DriverDelsListAdapter;
import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.model.DeliveryItem;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SenderDeliveriesListFragment extends BaseFragment implements View.OnClickListener {

    public static SenderDeliveriesListFragment newInstance(String text) {
        SenderDeliveriesListFragment mFragment = new SenderDeliveriesListFragment();
        Bundle mBundle = new Bundle();
        mBundle.putString(TEXT_FRAGMENT, text);
        mFragment.setArguments(mBundle);
        return mFragment;
    }

    ListView lvData;
    DriverDelsListAdapter senderDeliveriesListAdapter;
    ArrayList<DeliveryItem> deliveriesList = new ArrayList<>();

    TextView tabPendings;
    TextView tabPast;
    int currentTabIdx = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_senderdelslist, container, false);

        init(getArguments());

        tabPendings = (TextView) rootView.findViewById(R.id.tabPendings);
        tabPast = (TextView) rootView.findViewById(R.id.tabPast);

        tabPendings.setOnClickListener(this);
        tabPast.setOnClickListener(this);

        lvData = (ListView) rootView.findViewById(R.id.lvMyDelsData);
        senderDeliveriesListAdapter = new DriverDelsListAdapter(mContext, deliveriesList);
        lvData.setAdapter(senderDeliveriesListAdapter);
        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((MainActivity)parentActivity).showFragment(MainActivity.FRAGMENT_SENDER_DELIVERY_MAP);
            }
        });

        rootView.findViewById(R.id.btnAddNew).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectTab(0);

        getDeliveries();
    }

    private void selectTab(int tabIndex) {
        if (currentTabIdx == tabIndex) {
            return;
        }

        currentTabIdx = tabIndex;
        if (currentTabIdx == 0) {
            tabPendings.setSelected(true);
            tabPast.setSelected(false);
        } else {
            tabPendings.setSelected(false);
            tabPast.setSelected(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tabPendings) {
            selectTab(0);
        } else if(viewId == R.id.tabPast) {
            selectTab(1);
        } else if(viewId == R.id.btnAddNew) {
            //parentActivity.showFragment(MainActivity.FRAGMENT_SENDER_NEW_DELIVERY);
        }
    }

    private void getDeliveries() {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();

            jsonObject.put("userid", appSettings.getUserId());
            jsonObject.put("mlid", appSettings.getUserId());
            jsonObject.put("MLID", appSettings.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        showProgressDialog();

        RequestQueue queue = Volley.newRequestQueue(mContext);
        /*StringRequest sr = new StringRequest(Request.Method.GET, URLResolver.apiDelsBySenderID(jsonObject), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                hideProgressDialog();

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (jsonObject.getBoolean("status")) {

                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            Gson gson = new Gson();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject favDataObj = dataArray.getJSONObject(i);
                                DeliveryItem newDelInfo = gson.fromJson(favDataObj.toString(), DeliveryItem.class);
                                deliveriesList.add(newDelInfo);
                            }

                            senderDeliveriesListAdapter.notifyDataSetChanged();
                        } else {
                            showToastMessage(jsonObject.getString("message"));
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
                showToastMessage("Request Error!, Please check network.");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Accept", "application/json");
                return params;
            }
        };
        queue.add(sr);*/
    }
}
