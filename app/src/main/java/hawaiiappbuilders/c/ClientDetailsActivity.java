package hawaiiappbuilders.c;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hawaiiappbuilders.c.model.CalendarData;
import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.DateUtil;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.webutils.VolleySingleton;

public class ClientDetailsActivity extends BaseActivity implements
        View.OnClickListener {

    TextView tvClientInfo;
    TextView tvPickupInfo;
    TextView tvDestinationInfo;

    CalendarData.Data tripInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_details);

        // Get the Trip Info
        Intent intent = getIntent();
        tripInfo = intent.getParcelableExtra("tripInfo");
        if (tripInfo == null) {
            finish();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tvClientInfo = findViewById(R.id.tvClientInfo);
        tvPickupInfo = findViewById(R.id.tvPickupInfo);
        tvDestinationInfo = findViewById(R.id.tvDestinationInfo);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FN: ").append(tripInfo.getFN()).append("\n");
        stringBuilder.append("LN: ").append(tripInfo.getLN()).append("\n");
        stringBuilder.append("Nick: ").append(tripInfo.getNick()).append("\n");
        stringBuilder.append("Seats: ").append(tripInfo.getSeats()).append("\n");
        stringBuilder.append("Weight: ").append(tripInfo.getWeight()).append("\n");
        stringBuilder.append("Age: ").append(tripInfo.getAge()).append("\n");
        stringBuilder.append("CP: ").append(tripInfo.getCP());
        tvClientInfo.setText(stringBuilder.toString());

        Log.e("testing", tripInfo.getPuTime() + "\n" + tripInfo.getApptTime());

        stringBuilder = new StringBuilder();
        stringBuilder.append("Pickup Time: ").append(DateUtil.toStringFormat_40(DateUtil.parseDataFromFormat18(tripInfo.getPuTime()))).append("\n");
        stringBuilder.append("Facility: ").append(tripInfo.getFacilityPU()).append("\n");
        stringBuilder.append("Address: ").append(tripInfo.getAddressPU()).append("\n");
        stringBuilder.append("Floor: ").append(tripInfo.getFloorPU()).append("\n");
        stringBuilder.append("Room: ").append(tripInfo.getRoomPU()).append("\n");
        stringBuilder.append("Bed: ").append(tripInfo.getBedPU()).append("\n");
        stringBuilder.append("Stairs: ").append(tripInfo.getStairsPU()).append("\n");
        stringBuilder.append("Note: ").append(tripInfo.getNotePU());
        tvPickupInfo.setText(stringBuilder.toString());

        stringBuilder = new StringBuilder();
        stringBuilder.append("Appt Time: ").append(DateUtil.toStringFormat_40(DateUtil.parseDataFromFormat18(tripInfo.getApptTime()))).append("\n");
        stringBuilder.append("Facility: ").append(tripInfo.getFacility()).append("\n");
        stringBuilder.append("Address: ").append(tripInfo.getAddress()).append("\n");
        stringBuilder.append("Floor: ").append(tripInfo.getFloor()).append("\n");
        stringBuilder.append("Room: ").append(tripInfo.getRoom()).append("\n");
        stringBuilder.append("Bed: ").append(tripInfo.getBed()).append("\n");
        stringBuilder.append("Stairs: ").append(tripInfo.getStairs()).append("\n");
        stringBuilder.append("Note: ").append(tripInfo.getNote());
        tvDestinationInfo.setText(stringBuilder.toString());

        findViewById(R.id.btnLocation).setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnLocation) {
            String address = String.format("%s %s", tripInfo.getAddressPU(), tripInfo.getCszPU());
            if (!TextUtils.isEmpty(address)) {
                //Uri gmmIntentUri = Uri.parse(String.format("geo:%f,%f", mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%s&mode=d", address));

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    showToastMessage("Please install google map");
                }
            } else {
                showToastMessage("No Address Info");
            }
        }
    }

    private void setStatus(int mode) {
        if (getLocation()) {

        }
        String baseUrl = BaseFunctions.getBaseUrl(mContext, "arrived", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());
        // params.put("UTC", String.valueOf(0/*appSettings.getCUTC()*/));
        String extraParams = "&mode=" + mode +
                "&industryID=" + "80" +
                "&promoid=" + "0" +
                "&Amt=" + "0" +
                "&mins=" + "0" +
                "&OrderID=" + "0" +
                "&tolat=" + "0" +
                "&tolon=" + "0" +
                "&tripID=" + "0" +
                "&driverID=" + appSettings.getDriverID() +
                "&miles=" + "0" +
                "&tip=" + "0";
        baseUrl += extraParams;

        Log.e("arrived", baseUrl);

        showProgressDialog();
        GoogleCertProvider.install(mContext);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.e("900", response);

                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
                        } else if (jsonObject.has("status") && jsonObject.getBoolean("status")) {
                        }

                        showToastMessage(jsonObject.getString("msg"));
                        finish();
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
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setShouldCache(false);
        VolleySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }
}
