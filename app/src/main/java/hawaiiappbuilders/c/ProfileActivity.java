package hawaiiappbuilders.c;

import android.os.Bundle;
import android.os.Handler;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import hawaiiappbuilders.c.R;

import hawaiiappbuilders.c.adapter.ReviewListAdapter;
import hawaiiappbuilders.c.model.ReviewInfo;
import hawaiiappbuilders.c.view.circularprogressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Random;

public class ProfileActivity extends BaseActivity implements View.OnClickListener {

    TextView tabDriver;
    TextView tabSender;
    int currentTabIdx = -1;

    NestedScrollView scrollView;

    CircularProgressIndicator progressCompleted;
    CircularProgressIndicator progressOnTime;
    CircularProgressIndicator progressRepeat;

    ListView lvDataList;
    ReviewListAdapter reviewListAdapter;
    ArrayList<ReviewInfo> reviewInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        TextView tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText(String.format("%s %s", appSettings.getFN(), appSettings.getLN()).trim());

        scrollView = findViewById(R.id.scrollView);

        progressCompleted = findViewById(R.id.progressCompleted);
        progressOnTime = findViewById(R.id.progressOnTime);
        progressRepeat = findViewById(R.id.progressRepeat);

        lvDataList = findViewById(R.id.lvDataList);
        reviewInfos.add(new ReviewInfo("1", "Xian G", 5.0f, "\"Great Service, On-Time, Repeat hire again.\"", "", "2018-11-20"));
        reviewInfos.add(new ReviewInfo("2", "Xiao M", 5.0f, "\"Very good person. Always deliveried package in time and provided great service.\"", "", "2018-11-20"));
        reviewInfos.add(new ReviewInfo("3", "Shu Lian", 5.0f, "\"Great Service, On-Time, Repeat hire again.\"", "", "2018-11-20"));
        reviewInfos.add(new ReviewInfo("4", "Anna", 5.0f, "\"Very good person. Always deliveried package in time and provided great service.\"", "", "2018-11-20"));
        reviewInfos.add(new ReviewInfo("5", "Jack P", 5.0f, "\"Great Service, On-Time, Repeat hire again.\"", "", "2018-11-20"));
        reviewInfos.add(new ReviewInfo("6", "Chun T", 5.0f, "\"Very good person. Always deliveried package in time and provided great service.\"", "", "2018-11-20"));
        reviewInfos.add(new ReviewInfo("7", "Ronald L", 5.0f, "\"Great Service, On-Time, Repeat hire again.\"", "", "2018-11-20"));

        reviewListAdapter = new ReviewListAdapter(mContext, reviewInfos);
        lvDataList.setAdapter(reviewListAdapter);

        // Tab Actions
        tabDriver = (TextView) findViewById(R.id.tabDriver);
        tabSender = (TextView) findViewById(R.id.tabSender);

        tabDriver.setOnClickListener(this);
        tabSender.setOnClickListener(this);

        selectTab(0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.pageScroll(View.FOCUS_UP);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        }, 100);
    }

    private void selectTab(int tabIndex) {
        if (currentTabIdx == tabIndex) {
            return;
        }

        currentTabIdx = tabIndex;
        if (currentTabIdx == 0) {
            tabDriver.setSelected(true);
            tabSender.setSelected(false);
        } else {
            tabDriver.setSelected(false);
            tabSender.setSelected(true);
        }

        progressCompleted.setProgress(new Random().nextInt(100), 100);
        progressOnTime.setProgress(new Random().nextInt(100), 100);
        progressRepeat.setProgress(new Random().nextInt(100), 100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //listener for home
        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tabDriver) {
            selectTab(0);
        } else if (viewId == R.id.tabSender) {
            selectTab(1);
        }
    }
}
