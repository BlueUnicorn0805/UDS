package hawaiiappbuilders.c;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import hawaiiappbuilders.c.R;

public class RegisterFreeListingActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freelisting);

        findViewById(R.id.btnStart).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewid = view.getId();
        if (viewid == R.id.btnStart) {
            startRegister();
        } else if (viewid == R.id.btnBack) {
            finish();
        }
    }

    private void startRegister() {
        /*Intent intent = new Intent(mContext, RegisterServiceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        startActivity(intent);
        finish();*/


        Intent intent = new Intent(mContext, SelectLanguageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        startActivity(intent);
        finish();
    }
}
