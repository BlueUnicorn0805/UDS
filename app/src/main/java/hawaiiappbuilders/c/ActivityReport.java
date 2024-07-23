package hawaiiappbuilders.c;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

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
import java.util.HashMap;
import java.util.Map;

import hawaiiappbuilders.c.adapter.ReportAdapter;
import hawaiiappbuilders.c.model.ReportInfo;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.VolleySingleton;


public class ActivityReport extends BaseActivity {

    ArrayList<ReportInfo> menuList = new ArrayList<>();
    HashMap<String, ReportInfo> menuHashList = new HashMap<>();
    ExpandableListView lvData;
    ReportAdapter reportAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_list);

        findViewById(R.id.tbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lvData = (ExpandableListView) findViewById(R.id.lvData);
        reportAdapter = new ReportAdapter(mContext, menuList, new ReportAdapter.ItemSelectListener() {
            @Override
            public void onItemSelected(int groupPosition, int childPosition) {
                ReportInfo reportInfo = menuList.get(groupPosition).getChildIndustryInfo().get(childPosition);
                Log.e(TAG, "onItemSelected: " + reportInfo);
                Intent intent = new Intent(mContext, ActivityReportDetails.class);
                intent.putExtra("report", reportInfo);
                startActivity(intent);
            }
        });

        lvData.setAdapter(reportAdapter);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            lvData.setIndicatorBounds(width - GetPixelFromDips(45), width - GetPixelFromDips(15));
        } else {
            lvData.setIndicatorBoundsRelative(width - GetPixelFromDips(45), width - GetPixelFromDips(15));
        }

        getReportings();
    }

    public void getReportings() {

        if (getLocation()) {

            final Map<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(this,
                    "CJLGet",
                    BaseFunctions.MAIN_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    mMyApp.getAndroidId());
            String extraParams = "&mode=" + "ListReports" +
                    "&misc=" + "700000" +
                    "&industry=" + "123";

            baseUrl += extraParams;

            Log.e("request", "request-> " + baseUrl);

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    Log.e("ListReports", response);

                    if (!TextUtils.isEmpty(response)) {
                        try {

                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject itemObj = jsonArray.getJSONObject(i);

                                String Grp = itemObj.getString("Grp");
                                String Name = itemObj.getString("Name");
                                int catID = itemObj.getInt("catID");
                                int rptID = itemObj.getInt("rptID");

                                ReportInfo grpInfo = menuHashList.get(Grp);
                                if (grpInfo == null) {
                                    grpInfo = new ReportInfo();
                                    grpInfo.setGrp(Grp);
                                    menuHashList.put(Grp, grpInfo);
                                    menuList.add(grpInfo);

                                    ReportInfo newItemInfo = new ReportInfo();
                                    newItemInfo.setGrp(Grp);
                                    newItemInfo.setName(Name);
                                    newItemInfo.setCatID(catID);
                                    newItemInfo.setRptID(rptID);

                                    grpInfo.getChildIndustryInfo().add(newItemInfo);
                                } else {
                                    ReportInfo newItemInfo = new ReportInfo();
                                    newItemInfo.setGrp(Grp);
                                    newItemInfo.setName(Name);
                                    newItemInfo.setCatID(catID);
                                    newItemInfo.setRptID(rptID);

                                    grpInfo.getChildIndustryInfo().add(newItemInfo);
                                }
                            }
                            reportAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showAlert(e.getMessage());
                        }
                    } else {
                        showAlert("Server Error");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideProgressDialog();
                    if (TextUtils.isEmpty(error.getMessage())) {
                        showAlert("Server error!");
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
            VolleySingleton.setTimeout(sr);
            queue.add(sr);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
