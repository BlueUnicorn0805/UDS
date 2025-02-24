package hawaiiappbuilders.c;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SelectTimezoneActivity extends BaseActivity implements
        View.OnClickListener {

    Spinner spinnerTimeZone;
    String[] timezoneNames;
    String[] timezoneValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecttimezone);

        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        // Spinner
        spinnerTimeZone = findViewById(R.id.spinnerTimeZone);
        int defaultSpinnerIndex = 0;

        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        int localTimezone = (int) TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        double minDiff = Integer.MAX_VALUE;

        spinnerTimeZone = findViewById(R.id.spinnerTimeZone);
        timezoneNames = getResources().getStringArray(R.array.spinner_timezone);
        timezoneValues = new String[timezoneNames.length];
        for (int i = 0; i < timezoneNames.length; i++) {
            String timezoneInfo = timezoneNames[i];
            String[] spliteValues = timezoneInfo.split("=");
            timezoneNames[i] = spliteValues[0];
            timezoneValues[i] = spliteValues[1];

//            if (Math.abs(timezoneValues[i] - localTimezone) < minDiff) {
//                defaultSpinnerIndex = i;
//                minDiff = Math.abs(timezoneValues[i] - localTimezone);
//            }
            if (timezoneValues[i].equals(appSettings.getUTC())) {
                defaultSpinnerIndex = i;
            }
        }

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, R.layout.layout_spinner_timezone,
                timezoneNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTimeZone.setAdapter(spinnerArrayAdapter);
        spinnerTimeZone.setSelection(defaultSpinnerIndex);

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnBack) {
            finish();
        } else if (viewId == R.id.btnSave) {
            appSettings.setUTC(timezoneValues[spinnerTimeZone.getSelectedItemPosition()]);

            startActivity(new Intent(mContext, RegisterEmailActivity.class));
            finish();
        }
    }
}
