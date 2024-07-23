package hawaiiappbuilders.c;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class FragmentFolderActivity extends BaseActivity {

    // Sales Information
    JSONObject mSaleInformation = new JSONObject();
    JSONObject mCustomMsgInformation = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add essential fields
        saveCustomValue("rcvadd0", 0);
        saveCustomValue("rcvadd1", 0);
        saveCustomValue("rcvadd2", 0);
        saveCustomValue("rcvadd3", 0);
        saveCustomValue("rcvadd4", 0);
        saveCustomValue("rcvadd5", 0);
    }

    // Save Sale Information Field
    public void saveValue(String key, String value) {
        try {
            mSaleInformation.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Save Sale Information Field
    public void saveCustomValue(String key, String value) {
        try {
            mCustomMsgInformation.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveCustomValue(String key, int value) {
        try {
            mCustomMsgInformation.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Iterator<String> restoreCustomKeySet() {
        return mCustomMsgInformation.keys();
    }
    public String restoreCustomValueAsString(String key) {
        String value = "";
        if (mCustomMsgInformation.has(key)) {
            try {
                value = mCustomMsgInformation.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public int restoreCustomValueAsInt(String key) {
        int value = 0;
        if (mCustomMsgInformation.has(key)) {
            try {
                value = mCustomMsgInformation.getInt(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    // Save Sale Information Field as Boolean
    public void saveValue(String key, boolean value) {
        try {
            mSaleInformation.put(key, value ? 1 : 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Retrieve Sale Information Field
    public String restoreValue(String key) {
        String value = "";
        if (mSaleInformation.has(key)) {
            try {
                value = mSaleInformation.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
     }

    public String restoreValueWithDefaultZero(String key) {
        String value = "0";
        if (mSaleInformation.has(key)) {
            try {
                value = mSaleInformation.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    // Retrieve Sale Information Field as Boolean
    public boolean restoreValueBoolean(String key) {
        int value = 0;
        if (mSaleInformation.has(key)) {
            try {
                value = mSaleInformation.getInt(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value > 0;
    }

    public int restoreValueInt(String key) {
        int value = 0;
        if (mSaleInformation.has(key)) {
            try {
                value = mSaleInformation.getInt(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
