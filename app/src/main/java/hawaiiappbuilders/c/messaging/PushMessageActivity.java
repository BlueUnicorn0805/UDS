package hawaiiappbuilders.c.messaging;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import hawaiiappbuilders.c.BaseActivity;
import hawaiiappbuilders.c.R;

public class PushMessageActivity extends BaseActivity {

    TextView tvParams;
    TextView tvFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pushmsg);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Message");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tvParams = findViewById(R.id.tvParams);
        tvFrom = findViewById(R.id.tvFrom);

        Intent intent = getIntent();
        if (getIntent().getExtras() != null) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            String siteName = intent.getStringExtra("siteName");
            String timesent = intent.getStringExtra("timesent");
            tvParams.setText(timesent);
            tvFrom.setText(HtmlCompat.fromHtml("SiteName: " + siteName + "<br>Title: " + title + "<br><br>Message: " + message, HtmlCompat.FROM_HTML_MODE_COMPACT));
        }

    }
}
