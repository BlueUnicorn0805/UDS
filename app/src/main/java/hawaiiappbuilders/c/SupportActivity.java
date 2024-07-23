package hawaiiappbuilders.c;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;

import android.view.View;
import android.widget.EditText;

public class SupportActivity extends BaseActivity implements View.OnClickListener {

    EditText edtTitle;
    EditText edtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Init UI Elements
        edtTitle = (EditText) findViewById(R.id.edtTitle);
        edtMessage = (EditText) findViewById(R.id.edtMessage);

        findViewById(R.id.btnSubmit).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnSubmit) {
            sendFeedBack();
        }
    }

    private void sendFeedBack() {

        hideKeyboard(edtTitle);
        hideKeyboard(edtMessage);

        String strTitle = edtTitle.getText().toString().trim();
        String strMessage = edtMessage.getText().toString().trim();

        Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
        if (intent == null) {
            intent = new Intent(Intent.ACTION_SENDTO);
        }
        //Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"Support@UpDelivery.org"});
        intent.putExtra(Intent.EXTRA_SUBJECT, strTitle);
        intent.putExtra(Intent.EXTRA_TEXT, strMessage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            showToastMessage("Please install Email App to use function");
        }
    }
}
