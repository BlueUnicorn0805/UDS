package hawaiiappbuilders.c.utils;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import hawaiiappbuilders.c.AppSettings;
import hawaiiappbuilders.c.BuildConfig;
import hawaiiappbuilders.c.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class BaseFunctions {

    public static final int MAIN_FOLDER = 1;
    public static final int SEARCH_FOLDER = 2;
    public static final int SIGN_UP_FOLDER = 3;
    public static final int ORDER_FOLDER = 4;
    public static final int APP_FOLDER = 5;
    public static final int MAIL_FOLDER = 6;
    public static final int CALL_ACCEPTED = 1400;
    public static final int CALL_DECLINED = 1375;
    public static final int CALL_WAITING = 1355;
    public static final int CALL_CANCELED_HUNG_UP = 1380;
    public static final int CALL_DELIVERED = 1360;
    public static final int NEW_CALL = 1350;
    public static final int CALL_ERROR = 1361;
    public static final int NEED_TO_SETUP = 1365;
    public static final int CALL_BLOCKED = 1370;
    public static final int CALL_ON_HOLD = 1499;
    public static final int CALL_MUTE = 1450;
    public static final int CALL_NO_ANSWER = 1385;

    public static String getBaseUrl(Context context, String apiName, int folderId, String lat, String lon, String uuid) {
        final int currVer = 50922;
        int R1 = currVer;

        byte[] folder;
        switch (folderId) {
            case 1:
                folder = new byte[]{0x6d, 0x61, 0x69, 0x6e, 0x2f}; // mainFolder
                break;
            case 2:
                folder = new byte[]{0x73, 0x65, 0x61, 0x72, 0x63, 0x68, 0x2f}; // searchFolder
                break;
            case 3:
                folder = new byte[]{0x73, 0x69, 0x67, 0x6e, 0x75, 0x70, 0x2f}; //signupFolder
                break;
            case 4:
                folder = new byte[]{0x6f, 0x72, 0x64, 0x65, 0x72, 0x2f}; // orderFolder
                break;
            case 5:
                folder = new byte[]{0x61, 0x70, 0x70, 0x2f}; // appFolder
                break;
            case 6:
                folder = new byte[]{0x6d, 0x61, 0x69, 0x6c, 0x2f}; // mailFolder
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + folderId);
        }

        byte[] baseUrl = {0x68, 0x74, 0x74, 0x70, 0x73, 0x3a, 0x2f, 0x2f, 0x67, 0x65, 0x74, 0x66, 0x6f, 0x6f, 0x64, 0x2e, 0x61, 0x7a, 0x75, 0x72, 0x65, 0x77, 0x65, 0x62, 0x73, 0x69, 0x74, 0x65, 0x73, 0x2e, 0x6e, 0x65, 0x74, 0x2f};

        // D1
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.hack_date));
        String dateString = dateFormat.format(date);
        String[] hacks = dateString.split("-");
        String D1 = String.valueOf(Integer.parseInt(hacks[2]));

        String H1 = BuildConfig.H1;

        // P1
        int year = Integer.parseInt(hacks[0]);
        int month = Integer.parseInt(hacks[1]);
        int day = Integer.parseInt(hacks[2]);
        int hour = Integer.parseInt(hacks[3]);
        int hackBase = 0, p1 = 0;

        hackBase = day * day * hour;
        p1 = hackBase + year + month + day + hour;
        String P1 = String.valueOf(p1);

        AppSettings appSettings = new AppSettings(context);

        String baseUrlString = "";
        String folderString = "";
        baseUrlString = new String(baseUrl, StandardCharsets.UTF_8);
        folderString = new String(folder, StandardCharsets.UTF_8);

        String rtn = baseUrlString + folderString + apiName + "?" +
                "P1=" + P1 +
                "&R1=" + R1 +
                "&R2=" + Integer.parseInt(UtilHelper.getR2()) +
                "&D1=" + D1 +
                "&H1=" + H1 +
                "&devid=" + UtilHelper.getDevId() +
                "&appname=" + ourAppname() +
                "&utc=" + appSettings.getUTC() +
                "&cid=" + appSettings.getUserId() +
                "&workid=" + appSettings.getWorkid() +
                "&empid=" + appSettings.getEmpId() +
                "&lon=" + lon +
                "&lat=" + lat +
                "&uuid=" + uuid;

        return rtn;
    }

    public static String getBaseUrlForRegistration(Context context, String apiName, int folderId, String lat, String lon, String uuid) {
        final int currVer = 50922;
        int R1 = currVer;

        byte[] folder;
        switch (folderId) {
            case 1:
                folder = new byte[]{0x6d, 0x61, 0x69, 0x6e, 0x2f}; // mainFolder
                break;
            case 2:
                folder = new byte[]{0x73, 0x65, 0x61, 0x72, 0x63, 0x68, 0x2f}; // searchFolder
                break;
            case 3:
                folder = new byte[]{0x73, 0x69, 0x67, 0x6e, 0x75, 0x70, 0x2f}; //signupFolder
                break;
            case 4:
                folder = new byte[]{0x6f, 0x72, 0x64, 0x65, 0x72, 0x2f}; // orderFolder
                break;
            case 5:
                folder = new byte[]{0x61, 0x70, 0x70, 0x2f}; // appFolder
                break;
            case 6:
                folder = new byte[]{0x6d, 0x61, 0x69, 0x6c, 0x2f}; // mailFolder
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + folderId);
        }

        byte[] baseUrl = {0x68, 0x74, 0x74, 0x70, 0x73, 0x3a, 0x2f, 0x2f, 0x67, 0x65, 0x74, 0x66, 0x6f, 0x6f, 0x64, 0x2e, 0x61, 0x7a, 0x75, 0x72, 0x65, 0x77, 0x65, 0x62, 0x73, 0x69, 0x74, 0x65, 0x73, 0x2e, 0x6e, 0x65, 0x74, 0x2f};

        // D1
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.hack_date));
        String dateString = dateFormat.format(date);
        String[] hacks = dateString.split("-");
        String D1 = String.valueOf(Integer.parseInt(hacks[2]));

        String H1 = BuildConfig.H1;

        // P1
        int year = Integer.parseInt(hacks[0]);
        int month = Integer.parseInt(hacks[1]);
        int day = Integer.parseInt(hacks[2]);
        int hour = Integer.parseInt(hacks[3]);
        int hackBase = 0, p1 = 0;

        hackBase = day * day * hour;
        p1 = hackBase + year + month + day + hour;
        String P1 = String.valueOf(p1);

        AppSettings appSettings = new AppSettings(context);

        String baseUrlString = "";
        String folderString = "";
        baseUrlString = new String(baseUrl, StandardCharsets.UTF_8);
        folderString = new String(folder, StandardCharsets.UTF_8);

        String rtn = baseUrlString + folderString + apiName + "?" +
                "P1=" + P1 +
                "&R1=" + R1 +
                "&R2=" + Integer.parseInt(UtilHelper.getR2()) +
                "&D1=" + D1 +
                "&H1=" + H1 +
                "&devid=" + UtilHelper.getDevId() +
                "&appname=" + ourAppname() +
                "&utc=" + appSettings.getUTC() +
                "&cid=" + appSettings.getUserId() +
                "&workid=" + appSettings.getWorkid() +
                "&empid=" + appSettings.getEmpId() +
                "&lon=" + lon +
                "&lat=" + lat +
                "&uuid=" + uuid;

        return rtn;
    }

    public static String getBaseData(JSONObject dataParameters, Context context, String apiName, int folderId, String lat, String lon, String uuid) {
        final int currVer = 50922;
        int R1 = currVer;

        byte[] folder;
        switch (folderId) {
            case 1:
                folder = new byte[]{0x6d, 0x61, 0x69, 0x6e, 0x2f}; // mainFolder
                break;
            case 2:
                folder = new byte[]{0x73, 0x65, 0x61, 0x72, 0x63, 0x68, 0x2f}; // searchFolder
                break;
            case 3:
                folder = new byte[]{0x73, 0x69, 0x67, 0x6e, 0x75, 0x70, 0x2f}; //signupFolder
                break;
            case 4:
                folder = new byte[]{0x6f, 0x72, 0x64, 0x65, 0x72, 0x2f}; // orderFolder
                break;
            case 5:
                folder = new byte[]{0x61, 0x70, 0x70, 0x2f}; // appFolder
                break;
            case 6:
                folder = new byte[]{0x6d, 0x61, 0x69, 0x6c, 0x2f}; // mailFolder
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + folderId);
        }

        byte[] baseUrl = {0x68, 0x74, 0x74, 0x70, 0x73, 0x3a, 0x2f, 0x2f, 0x67, 0x65, 0x74, 0x66, 0x6f, 0x6f, 0x64, 0x2e, 0x61, 0x7a, 0x75, 0x72, 0x65, 0x77, 0x65, 0x62, 0x73, 0x69, 0x74, 0x65, 0x73, 0x2e, 0x6e, 0x65, 0x74, 0x2f};

        // D1
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.hack_date));
        String dateString = dateFormat.format(date);
        String[] hacks = dateString.split("-");
        String D1 = String.valueOf(Integer.parseInt(hacks[2]));

        String H1 = BuildConfig.H1;

        // P1
        int year = Integer.parseInt(hacks[0]);
        int month = Integer.parseInt(hacks[1]);
        int day = Integer.parseInt(hacks[2]);
        int hour = Integer.parseInt(hacks[3]);
        int hackBase = 0, p1 = 0;

        hackBase = day * day * hour;
        p1 = hackBase + year + month + day + hour;
        String P1 = String.valueOf(p1);

        AppSettings appSettings = new AppSettings(context);

        String baseUrlString = "";
        String folderString = "";
        baseUrlString = new String(baseUrl, StandardCharsets.UTF_8);
        folderString = new String(folder, StandardCharsets.UTF_8);

        // Create base parameters json object
        JSONObject baseParametersObject = new JSONObject();
        try {
            baseParametersObject.put("P1", P1);
            baseParametersObject.put("R1", R1);
            baseParametersObject.put("R2", Integer.parseInt(UtilHelper.getR2()));
            baseParametersObject.put("D1", D1);
            baseParametersObject.put("H1", H1);
            baseParametersObject.put("devid", UtilHelper.getDevId());
            baseParametersObject.put("appname", ourAppname());
            baseParametersObject.put("utc", appSettings.getUTC());
            baseParametersObject.put("cid", appSettings.getUserId());
            baseParametersObject.put("workid", appSettings.getWorkid());
            baseParametersObject.put("empid", appSettings.getEmpId());
            baseParametersObject.put("lon", lon);
            baseParametersObject.put("lat", lat);
            baseParametersObject.put("uuid", uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject mergedData = new JSONObject();
        Iterator<String> keys = baseParametersObject.keys(); // iterate keys from json object
        while (keys.hasNext()) {
            String key = keys.next();
            Object nestedObject;
            try {
                nestedObject = baseParametersObject.get(key);
                mergedData.put(key, nestedObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Iterator<String> dataKeys = dataParameters.keys(); // iterate keys from json object
        while (dataKeys.hasNext()) {
            String key = dataKeys.next();
            Object nestedObject;
            try {
                nestedObject = dataParameters.get(key);
                mergedData.put(key, nestedObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String rtn = "";
        String mergedDataString = mergedData.toString().trim();
        try {
            mergedDataString = URLEncoder.encode(mergedDataString, "UTF-8");
            String jsonData = String.format("data=%s", mergedDataString.replaceAll("\\+", "%20"));
            rtn = baseUrlString + folderString + apiName + "?" +
                    jsonData;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return rtn;
    }

    private static String ourAppname() {
        return "UDSANDROID";
    }

}
