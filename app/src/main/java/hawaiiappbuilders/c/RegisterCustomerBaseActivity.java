package hawaiiappbuilders.c;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import hawaiiappbuilders.c.R;

public class RegisterCustomerBaseActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_base);

        findViewById(R.id.btnUnderstand).setOnClickListener(this);
        findViewById(R.id.ivLogo).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewid = view.getId();
        if (viewid == R.id.btnUnderstand) {
            startRegister();
        } else if (viewid == R.id.ivLogo) {
            finish();
        }
    }

    private  void startRegister() {
        Intent intent = new Intent(mContext, RegisterFreeListingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        startActivity(intent);
        finish();
    }
}
