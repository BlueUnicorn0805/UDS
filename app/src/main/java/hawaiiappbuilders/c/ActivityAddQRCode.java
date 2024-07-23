package hawaiiappbuilders.c;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import hawaiiappbuilders.c.utils.BaseFunctions;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.UtilHelper;
import hawaiiappbuilders.c.view.OpenSansButton;
import hawaiiappbuilders.c.view.OpenSansEditText;
import hawaiiappbuilders.c.view.OpenSansTextView;

public class ActivityAddQRCode extends BaseActivity {

    OpenSansTextView tvQRCode;
    OpenSansEditText tvQRMessage;
    ImageView ivQR;
    OpenSansButton btnClose, btnMakeQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_qrcode);

        initViews();
        initListeners();
        callAddQRApi();
    }

    private void initListeners() {
        btnClose.setOnClickListener(v -> finish());
        btnMakeQr.setOnClickListener(v -> createQRImage());
    }

    private void initViews() {
        tvQRCode = findViewById(R.id.tvQrcode);
        tvQRMessage = findViewById(R.id.tvQRMessage);
        ivQR = findViewById(R.id.iv_qr);
        btnClose = findViewById(R.id.btnClose);
        btnMakeQr = findViewById(R.id.btnMakeQr);
    }

    private void callAddQRApi() {
        if (getLocation()) {

            final Map<String, String> params = new HashMap<>();
            String baseUrl = BaseFunctions.getBaseUrl(mContext, "CJLGet", BaseFunctions.MAIN_FOLDER, getUserLat(), getUserLon(), mMyApp.getAndroidId());
            String extraParams = "&mode=explainDriverQRcode";
            baseUrl += extraParams;

            Log.e("CJLGet", baseUrl);

            showProgressDialog();
            RequestQueue queue = Volley.newRequestQueue(mContext);

            //HttpsTrustManager.allowAllSSL();
            GoogleCertProvider.install(mContext);

            StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, response -> {
                hideProgressDialog();
                Log.e("callAddQRApi", response);

                if (response != null || !response.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;
                        String status = jsonObject.getString("msgToDriver");
                        Log.e(TAG, "onResponse: " + status);
                        tvQRCode.setText(status);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, error -> {
                hideProgressDialog();
                showAlert("Request Error!, Please check network.");
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
            queue.add(sr);
        }
    }

    private void createQRImage() {
        String label = "";
        if (tvQRMessage.getText() != null) {
            if (TextUtils.isEmpty(tvQRMessage.getText())) {
                Toast.makeText(mContext, "Please add message!", Toast.LENGTH_SHORT).show();
                return;
            }
            String mergedDataString = tvQRMessage.getText().toString().trim();
            try {
                mergedDataString = URLEncoder.encode(mergedDataString, "UTF-8");
                label = String.format("%s", mergedDataString.replaceAll("\\+", "%20"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String link = "https://omnivers.info/biz/?" +
                "appid=173" +
                "&qrid=0" +
                "&pmt=510" +
                "&semp=" + appSettings.getEmpId() +
                "&sponsorid=0" +
                "&promoid=0" +
                "&indust=" + appSettings.getIndustryid() +
                "&handle=" + appSettings.getWHandle() +
                "&prodid=0" +
                "&r2=" + UtilHelper.getR2() +
                "&msg=" + label;
        Log.e(TAG, "createQRImage:link \n " + link);
        Bitmap bitmap = generateQRCode(link);
        ivQR.setImageBitmap(bitmap);
//        ivQR.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 120, 120, false));
        ivQR.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        ivQR.setVisibility(View.VISIBLE);
    }

    private Bitmap generateQRCode(String text) {
        try {
            // Set QR code parameters
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, ivQR.getWidth(), ivQR.getWidth(), hints);

            // Convert bit matrix to bitmap
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}