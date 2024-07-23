package hawaiiappbuilders.c;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityChangePassword extends BaseActivity implements View.OnClickListener {

    EditText edtOriginal;
    EditText edtPwd;
    EditText edtConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepwd);

        edtOriginal = (EditText) findViewById(R.id.edtOriginal);
        edtPwd = (EditText) findViewById(R.id.edtPwd);
        edtConfirm = (EditText) findViewById(R.id.edtConfirm);

        //edtEmail.setText("testfreelancerbd@gmail.com");
        //edtPassword.setText("abcdEF1234##");

        findViewById(R.id.btnSubmit).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.btnBack) {
            finish();
        } else if (viewId == R.id.btnSubmit) {
            //loginUserWithNST();
            loginUserWithGF();
        }
    }

    private void loginUserWithGF() {

        hideKeyboard(edtOriginal);
        hideKeyboard(edtPwd);
        hideKeyboard(edtConfirm);

        // Get Location
        if (!getLocation()) {
            return;
        }

        final String original = edtOriginal.getText().toString().trim();
        final String password = edtPwd.getText().toString().trim();
        final String confirm = edtConfirm.getText().toString().trim();

        /* if (TextUtils.isEmpty(original)) {
            edtOriginal.setError("Please input original password");
            return;
        }
        */
        if (TextUtils.isEmpty(password)) {
            edtPwd.setError(getText(R.string.error_password));
            return;
        }

        if (password.length() < 5) {
            edtPwd.setError(getText(R.string.error_invalid_password));
            return;
        }

        if (password.contains("#")) {
            edtPwd.setError("Password couldn't accept # symbol");
            return;
        }

        /*if (!password.equals(confirm)) {
            edtConfirm.setError(getText(R.string.error_password_not_match));
            return;
        }*/

        if (getLocation()) {
            showProgressDialog();

            final HashMap<String, String> params = new HashMap<>();

            String baseUrl = BaseFunctions.getBaseUrl(mContext, "resetPW", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());
            String extraParams =
                    "&industryID=" + appSettings.getIndustryid() +
                    "&currentPW=" + original +
                    "&newPW=" + password +
                    "&misc=" + "" +
                    "&sellerID=" + appSettings.getWorkid();
            baseUrl += extraParams;

            //Log.e("resetPW", params.toString());
            String fullParams = "";
            for (String key : params.keySet()) {
                fullParams += String.format("&%s=%s", key, params.get(key));
            }
            //Log.e("resetPW", fullParams);

            GoogleCertProvider.install(mContext);
            RequestQueue queue = Volley.newRequestQueue(mContext);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();

                    JSONArray responseArry = null;
                    try {
                        responseArry = new JSONArray(response);
                        JSONObject responseObj = responseArry.getJSONObject(0);

                        String msg = "Your PW has been updated.";
                        if (responseObj.has("msg")) {
                            msg = responseObj.getString("msg");
                        }

                        showAlert(msg, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    25000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            stringRequest.setShouldCache(false);
            stringRequest.setShouldCache(false);
            queue.add(stringRequest);

        }
    }
}
