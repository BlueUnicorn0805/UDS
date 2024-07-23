package hawaiiappbuilders.c;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

public class ActivityShowQR extends BaseActivity implements View.OnClickListener {

    TextView tvName;
    TextView tvOrderId;
    ImageView qr_code_niv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showqrcode);

        initViews();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvName.setText(String.format("%s %s", appSettings.getFN(), appSettings.getLN()));

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderId.setText("OrderId: " + 0);

        findViewById(R.id.btnClose).setOnClickListener(this);
        findViewById(R.id.btnOnSite).setOnClickListener(this);

        qr_code_niv = findViewById(R.id.qr_code_niv);

//        https://omnivers.info/biz/?pmt=57&semp=455&fn=1&ln=1&handle=founder&orderid=0&wp=1&
//        msg=This%20is%20my%20Realtime%20Driver%20Identification
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("pmt", 57);
            jsonObject.put("semp", appSettings.getEmpId());
            jsonObject.put("fn", 1);
            jsonObject.put("ln", 1);
//            jsonObject.put("handle", appSettings.getBHandle());
            jsonObject.put("orderid", 0);
            jsonObject.put("wp", appSettings.getWP());
            jsonObject.put("msg", "This is my Realtime Driver Identification");

//            jsonObject.put("DriverID", appSettings.getDriverID());
//            jsonObject.put("co", appSettings.getCO());
//            jsonObject.put("cp", appSettings.getCP());
//            jsonObject.put("title", appSettings.getDepartName());
//            jsonObject.put("workID", appSettings.getWorkid());
//            jsonObject.put("mode", appSettings.getDriverID());

            getQRImage(jsonObject, qr_code_niv);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getQRImage(JSONObject jsonObject, ImageView qrCodePhoneImageView) {
        String myUrl = "";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https").authority("omnivers.info").appendPath("biz");
        Iterator<String> iter = jsonObject.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            String value = jsonObject.optString(key);
            builder.appendQueryParameter(key, value);
        }

        // Prevent reencode UTF8
        myUrl = builder.build().toString();

        Log.e("QRCODE", myUrl);


        Log.e("QRCODE", jsonObject.toString());
        Log.e("QRCODE", myUrl);

        Bitmap bm = generateQRCode(myUrl);
        qrCodePhoneImageView.setVisibility(View.VISIBLE);
        qrCodePhoneImageView.setImageBitmap(bm);
    }

    private Bitmap generateQRCode(String text) {
        try {
            // Set QR code parameters
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Log.e("QRCODE", text);
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300, hints);

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


    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btnClose) {
            finish();
        } else if (viewId == R.id.btnOnSite) {
        }
    }
}