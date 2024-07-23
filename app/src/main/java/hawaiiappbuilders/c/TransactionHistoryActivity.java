package hawaiiappbuilders.c;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import hawaiiappbuilders.c.adapter.TransactionAdapter;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.model.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransactionHistoryActivity extends BaseActivity implements View.OnClickListener {

    Context context;
    ArrayList<Transaction> mTransactions;
    TransactionAdapter adapter;
    RecyclerView mTransactionList;

    TextView mEmptyList;
    String TAG = "ActivityTransaction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        mTransactions = new ArrayList<>();
        mTransactionList = findViewById(R.id.transaction_recyclerView);
        mEmptyList = findViewById(R.id.emptyList);

        findViewById(R.id.tx_btn_dashboard).setOnClickListener(this);

        if(getIntent().getExtras() != null) {
            String phone = getIntent().getExtras().getString("phone");
            if(!phone.isEmpty()) {
                if (checkPermissions(this, PERMISSION_REQUEST_PHONE_STRING, false, 109)) {
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                    phoneIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    phoneIntent.setData(Uri.parse(String.format("tel:%s", phone)));
                    startActivity(phoneIntent);
                }
            }
        }

        getTransactionInfo();
    }

    private void getTransactionInfo() {
        Log.e(TAG, "getLocation: ");
        if (getLocation()) {

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());

            String userLat = getUserLat();
            String userLon = getUserLon();

            final HashMap<String, String> params = new HashMap<>();
            //params.put("cID", appSettings.getUserId());

            String extraParams = "&mode=" + "TXs" +
                    "&sellerID=" + "0" +
                    "&misc=" + appSettings.getUserId();

            baseUrl += extraParams;

            Log.e("avaTXs", params.toString());

            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }

            Log.e("avaTXs", fullParams);

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    Log.e("Tx", response);

                    if (response != null || !response.isEmpty()) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;

                            if(jsonObject.get("status") instanceof Integer) {
                                if(jsonObject.getInt("status") == 1) {
                                    mTransactionList.setVisibility(View.VISIBLE);
                                    mEmptyList.setVisibility(View.GONE);

                                    Log.e(TAG, "onSuccess: " + jsonArray.toString());
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject data = jsonArray.getJSONObject(i);
                                        Transaction transaction = new Transaction();

                                        transaction.setName(data.getString("Name"));
                                        transaction.setAmt(data.getString("Amt"));
                                        transaction.setItemDate(data.getString("ItemDate"));
                                        transaction.setTxID(data.getString("TxID"));
                                        transaction.setToID(data.getString("toID"));
                                        transaction.setNote(data.getString("Note"));
                                        mTransactions.add(transaction);
                                    }

                                    setUpRecyclerView(mTransactions);
                                }
                            }  else {
                                showToastMessage(jsonObject.getString("msg"));
                                mTransactionList.setVisibility(View.GONE);
                                mEmptyList.setVisibility(View.VISIBLE);
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
                    mTransactionList.setVisibility(View.GONE);
                    mEmptyList.setVisibility(View.VISIBLE);

                    showToastMessage("Request Error!, Please check network.");
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

    private void setUpRecyclerView(ArrayList<Transaction> mTransactions) {
        mTransactionList.setHasFixedSize(true);
        mTransactionList.setLayoutManager(new LinearLayoutManager(context));

        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(getResources().getDrawable(R.drawable.divider));

        mTransactionList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new TransactionAdapter(context, mTransactions);
        mTransactionList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tx_btn_dashboard:
                finish();
                break;
        }
    }
}
