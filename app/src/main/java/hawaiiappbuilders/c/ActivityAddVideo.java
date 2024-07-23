package hawaiiappbuilders.c;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.webutils.VolleySingleton;

public class ActivityAddVideo extends BaseActivity implements View.OnClickListener {

    EditText videoId;
    EditText videoHeadline;
    EditText videoTitle;
    EditText videoMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        initView();
        initClicks();
    }

    private void initView() {
        videoId = findViewById(R.id.videoId);
        videoHeadline = findViewById(R.id.videoHeadline);
        videoTitle = findViewById(R.id.videoTitle);
        videoMessage = findViewById(R.id.videoMessage);
    }

    private void initClicks() {
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.ivLogo).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.ivLogo:
            case R.id.btnCancel:
                finish();
                break;
            case R.id.btnSave:
                callSaveAPI();
                break;
        }

    }

    private void callSaveAPI() {

        String vId = videoId.getText().toString().trim();

        if (isValidYouTubeUrl(vId)) {
            vId = extractVideoId(vId);
        } else if (vId.length() >= 11) {
            vId = vId.substring(vId.length() - 11);
        } else {
            if (TextUtils.isEmpty(vId))
                Toast.makeText(mContext, "Please add video ID/URL!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext, "Invalid video ID/URL!", Toast.LENGTH_SHORT).show();
            return;
        }

        String headline = videoHeadline.getText().toString().trim();
        if (TextUtils.isEmpty(headline)) {
            Toast.makeText(mContext, "Please add video Headline!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = videoTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(mContext, "Please add video Title!", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = videoMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(mContext, "Please add video Message!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getLocation()) {
            final HashMap<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext,
                    "CJLSet",
                    BaseFunctions.MAIN_FOLDER,
                    getUserLat(),
                    getUserLon(),
                    mMyApp.getAndroidId());

            String extraParams = "&mode=17" +
                    "&industryID=" + appSettings.getIndustryid() +
                    "&promoid=" + "0" +
                    "&Amt=" + "0" +
                    "&OrderID=" + "0" +
                    "&mins=" + "0" +
                    "&tolat=" + "0" +
                    "&tolon=" + "0" +
                    "&address=" + vId +
                    "&headline=" + headline +
                    "&Title=" + title +
                    "&msg=" + message;

            baseUrl += extraParams;

            Log.e("request", "request -> " + baseUrl);

            showProgressDialog();
            GoogleCertProvider.install(mContext);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();
                    Log.e("response", "response -> " + response);

                    try {
                        JSONArray responseArray = new JSONArray(response);
                        JSONObject responseJson = responseArray.getJSONObject(0);

                        if (responseJson.optBoolean("status")) {
                            finish();
                        } else {
                            showAlert("Try again later.", v -> finish());
                        }
                    } catch (JSONException e) {
                        Log.e("response", "exception -> " + e.getMessage());
                        finish();
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
    }

    // Method to extract video ID from YouTube URL
    public static String extractVideoId(String url) {
        String videoId = url.substring(url.length() - 11);
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2Fvideos%2F|youtu.be%2F|\\/v%2F)[^#\\&\\?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url); //url is youtube url for which you want to extract video id.
        if (matcher.find()) {
            videoId = matcher.group();
        }
        return videoId;
    }

    public static boolean isValidYouTubeUrl(String url) {
        String pattern = "^(http(s)?://)?((w){3}.)?youtu(be|.be)?(\\.com)?/.+";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(url);
        return matcher.matches();
    }
}




