package hawaiiappbuilders.c;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import hawaiiappbuilders.c.adapter.ReportItemAdapter;
import hawaiiappbuilders.c.model.ReportInfo;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.VolleySingleton;


public class ActivityReportDetails extends BaseActivity implements View.OnClickListener {

    ReportInfo reportInfo;

    TextView tvInvoice;
    TextView tvDateTime;
    String dateTime;

    LinearLayout panelHeaders;
    ArrayList<String> headerTitlesList = new ArrayList<>();
    ArrayList<JSONObject> dataList = new ArrayList<>();
    RecyclerView recyclerView;
    ReportItemAdapter reportItemAdapter;

    // Store Information
    TextView tvStoreName;
    TextView tvStoreInfo1;
    TextView tvStoreInfo2;
    TextView tvStoreInfo3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_details);

        Intent intent = getIntent();
        reportInfo = intent.getParcelableExtra("report");
        if (reportInfo == null) {
            finish();
            return;
        }

        try {
            dateTime = DateUtil.toStringFormat_8(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Show Store Info
        tvStoreName = findViewById(R.id.storeName);
        tvStoreInfo1 = findViewById(R.id.storeInfo1);
        tvStoreInfo2 = findViewById(R.id.storeInfo2);
        tvStoreInfo3 = findViewById(R.id.storeInfo3);
        tvInvoice = findViewById(R.id.tvInvoice);

        tvStoreName.setText(appSettings.getCO());
        tvStoreInfo1.setText(String.format("%s %s", appSettings.getStreetNum(), appSettings.getStreet()).trim());
        tvStoreInfo2.setText(String.format("%s, %s, %s", appSettings.getCity(), appSettings.getSt(), appSettings.getZip()).trim());
        tvStoreInfo3.setText("");

        tvInvoice.setText(reportInfo.getName());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(reportInfo.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvDateTime = findViewById(R.id.orderDate);
        tvDateTime.setText(dateTime);

        panelHeaders = findViewById(R.id.panelHeaders);

        recyclerView = (RecyclerView) findViewById(R.id.rcvMenuList);
        recyclerView.setLayoutManager(new GridLayoutManager(getBaseContext(), 1));
        reportItemAdapter = new ReportItemAdapter(mContext, headerTitlesList, dataList);
        recyclerView.setAdapter(reportItemAdapter);

        findViewById(R.id.pdfView).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.btnEmail).setOnClickListener(this);

        getReportingDetails();
    }

    public void getReportingDetails() {

        if (getLocation()) {

            final Map<String, String> params = new HashMap<>();
            String baseUrl = BaseFunctions.getBaseUrl(this,
                    "CJLGet",
                    BaseFunctions.MAIN_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    mMyApp.getAndroidId());
            String extraParams = "&mode=" + "Report" +
                    "&misc=" + reportInfo.getRptID() +
                    "&industryID=" + appSettings.getIndustryid();
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

                    Log.e("Report", response);

                    if (!TextUtils.isEmpty(response)) {
                        try {
                            JSONArray jsonResponse = new JSONArray(response);

                            if (jsonResponse.get(0) instanceof JSONObject && ((JSONObject) jsonResponse.get(0)).has("msg")) {
                                try {
                                    showAlert(((JSONObject) jsonResponse.get(0)).optString("msg"));
                                    findViewById(R.id.buttons).setVisibility(View.GONE);
                                    findViewById(R.id.pdfView).setClickable(false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showAlert(e.getMessage());
                                }
                                return;
                            }

                            JSONArray jsonHeaderArray = jsonResponse.getJSONArray(1);

                            int subViewsCnt = panelHeaders.getChildCount();
                            int headerCnt = Math.min(jsonHeaderArray.length(), subViewsCnt);

                            for (int i = 0; i < subViewsCnt; i++) {
                                if (i < headerCnt) {
                                    headerTitlesList.add(jsonHeaderArray.getString(i));
                                    LinearLayout panelPro = (LinearLayout) panelHeaders.getChildAt(i);
                                    TextView tvHeaderTitle = panelPro.findViewById(R.id.tvHeaderID);
                                    tvHeaderTitle.setText(jsonHeaderArray.getString(i));
                                } else {
                                    panelHeaders.removeViewAt(headerCnt);
                                }
                            }

                            JSONArray jsonArray = jsonResponse.getJSONArray(3);

                            if (jsonArray.length() < 1) {
                                showAlert("No Data Yet");
                                return;
                            }

                            for (int i = 0; i < jsonArray.length(); i++) {
                                dataList.add(jsonArray.getJSONObject(i));
                            }

                            reportItemAdapter.notifyDataSetChanged();

                            findViewById(R.id.buttons).setVisibility(View.VISIBLE);
                            findViewById(R.id.pdfView).setClickable(true);
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
                protected Map<String, String> getParams() {
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

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.pdfView) {
            if (findViewById(R.id.buttons).getVisibility() == View.GONE)
                findViewById(R.id.buttons).setVisibility(View.VISIBLE);
            else
                findViewById(R.id.buttons).setVisibility(View.GONE);
        } else if (viewId == R.id.btnSave) {
            createPDFFromView(findViewById(R.id.pdfView),
                    "Report_" + System.currentTimeMillis() + ".pdf");
        } else if (viewId == R.id.btnEmail) {
            emailFile(findViewById(R.id.pdfView),
                    "Report_" + System.currentTimeMillis() + ".pdf");
        }
    }

    public File createPDFFromView(View view, String fileName) {
        showProgressDialog();

        Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

        int topConHeight = findViewById(R.id.topContainer).getHeight();
        int recViewHeight = recyclerView.computeVerticalScrollRange();
        int totalHeight = topConHeight + Math.max(recViewHeight, recyclerView.computeVerticalScrollExtent());

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        findViewById(R.id.topContainer).draw(canvas);
        canvas.translate(0, topConHeight);

        recyclerView.scrollTo(0, 0);
        recyclerView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        recyclerView.layout(0, topConHeight, recyclerView.getMeasuredWidth(), topConHeight + recViewHeight);

        recyclerView.draw(canvas);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas pdfCanvas = page.getCanvas();
        pdfCanvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        File pdfDoc = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        try {
            document.writeTo(new FileOutputStream(pdfDoc));
            Toast.makeText(mContext, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Could not Save!!", Toast.LENGTH_SHORT).show();
        }

// Restore the RecyclerView's state (if needed)
        if (recyclerViewState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }

        document.close();
        hideProgressDialog();

        return pdfDoc;
    }

    public void emailFile(View view, String fileName) {
        File pdfDoc = createPDFFromView(view, fileName);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        Uri uri = FileProvider.getUriForFile(mContext,
                BuildConfig.APPLICATION_ID + ".provider",
                pdfDoc);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Send email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(mContext, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
