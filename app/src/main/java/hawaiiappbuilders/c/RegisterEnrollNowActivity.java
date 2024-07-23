package hawaiiappbuilders.c;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import hawaiiappbuilders.c.R;

public class RegisterEnrollNowActivity extends BaseActivity implements View.OnClickListener {

    TextView tvFN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_enrollnow);

        tvFN = findViewById(R.id.tvFN);
        tvFN.setText(appSettings.getFN());

        findViewById(R.id.btnEnrollNow).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewid = view.getId();
        if (viewid == R.id.btnEnrollNow) {
            startRegister();
        } else if (viewid == R.id.btnBack) {
            finish();
        }
    }

    private  void startRegister() {
        Intent intent = new Intent(mContext, RegisterCustomerBaseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish();
    }
}
