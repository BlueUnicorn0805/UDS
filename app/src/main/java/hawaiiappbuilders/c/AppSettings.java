package hawaiiappbuilders.c;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import java.util.ArrayList;

public class AppSettings {
    private static final String APP_SHARED_PREFS = "eappbuilder_udx_prefs";
    private SharedPreferences appSharedPrefs;
    private Editor prefsEditor;
    private static final String LOGGED_IN = "logged_in";

    private static final String DEVICE_ID_SET = "device_id_set";
    private static final String DEVICE_TOKEN = "device_token";
    private static final String PIN = "pin";
    private String USEI_ID = "userid";
    private String FN = "FN";
    private String LAT = "LAT";
    private String LNG = "LNG";

    private static final String UTC_SETTING = "UTC";

    private String NEW_DEL_ID = "New_Del_ID";


    private static final String LOGIN_INPUT = "login_input";

    public AppSettings(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }


    // ------------------------------------------ Company ---------------------------------------------------
    public String COMPANY = "COMPANY";

    public String getCompany() {
        return appSharedPrefs.getString(COMPANY, "");
    }

    public void setCompany(String value) {
        prefsEditor.putString(COMPANY, value);
        prefsEditor.commit();
    }

    public String getString(String key) {
        return appSharedPrefs.getString(key, "");
    }

    public int getInt(String key) {
        return appSharedPrefs.getInt(key, 0);
    }

    public long getLong(String key) {
        return appSharedPrefs.getLong(key, 0);
    }

    public boolean getBoolean(String key) {
        return appSharedPrefs.getBoolean(key, false);
    }

    public void putString(String key, String value) {
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }

    public void putInt(String key, int value) {
        prefsEditor.putInt(key, value);
        prefsEditor.commit();
    }

    public void putLong(String key, long value) {
        prefsEditor.putLong(key, value);
        prefsEditor.commit();
    }

    public void putBoolean(String key, boolean value) {
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }

    public void remove(String key) {
        prefsEditor.remove(key);
        prefsEditor.commit();
    }

    public void clear() {
        // prefsEditor.clear();
        // prefsEditor.commit();

        setUserId(0);
        setFN("");
        setLN("");
        setEmail("");
        setCP("");
        setStreet("");
        setCity("");
        setSt("");
        setZip("");

        logOut();
    }

    public void setLoginInput(String loginInput) {
        prefsEditor.putString(LOGIN_INPUT, loginInput);
        prefsEditor.commit();
    }

    public String getLoginInput() {
        return appSharedPrefs.getString(LOGIN_INPUT, "");
    }

    public int getUserId() {
        int userID = appSharedPrefs.getInt(USEI_ID, 0);
        /*if (TextUtils.isEmpty(userID)) {
            userID = 0;
        }*/

        return userID;
    }

    public void setUserId(int userId) {
        prefsEditor.putInt(USEI_ID, userId);
        prefsEditor.commit();
    }

    public String getFN() {
        return appSharedPrefs.getString(FN, "");
    }

    public void setFN(String userId) {
        prefsEditor.putString(FN, userId);
        prefsEditor.commit();
    }

    public void setDeviceIdSet(boolean isAdded) {
        prefsEditor.putBoolean(DEVICE_ID_SET, isAdded);
        prefsEditor.apply();
    }

    public boolean isDeviceIdSet() {
        return appSharedPrefs.getBoolean(DEVICE_ID_SET, false);
    }

    public void setLoggedIn() {
        prefsEditor.putBoolean(LOGGED_IN, true);
        prefsEditor.apply();
    }

    public boolean isLoggedIn() {
        return appSharedPrefs.getBoolean(LOGGED_IN, false);
    }

    public boolean logOut() {
        prefsEditor.putBoolean(LOGGED_IN, false);
        return prefsEditor.commit();
    }

    public void setPIN(String pin) {
        prefsEditor.putString(PIN, pin);
        prefsEditor.apply();
    }

    public String getPIN() {
        return appSharedPrefs.getString(PIN, "");
    }

    public void setDeviceToken(String deviceToken) {
        prefsEditor.putString(DEVICE_TOKEN, deviceToken);
        prefsEditor.apply();
    }

    private static final String GET_TOKEN_RETRY = "getTokenRetry";

    public int getTokenRetry() {
        return appSharedPrefs.getInt(GET_TOKEN_RETRY, 0);
    }

    public void setTokenRetry(int retry) {
        prefsEditor.putInt(GET_TOKEN_RETRY, retry);
        prefsEditor.commit();
    }

    public String getDeviceLat() {
        return appSharedPrefs.getString(LAT, "0.0");
    }

    public String getDeviceLng() {
        return appSharedPrefs.getString(LNG, "0.0");
    }

    public void setDeviceLat(String deviceLat) {
        prefsEditor.putString(LAT, deviceLat);
        prefsEditor.apply();
    }

    public void setDeviceLng(String deviceLng) {
        prefsEditor.putString(LNG, deviceLng);
        prefsEditor.apply();
    }

    public String getDeviceToken() {
        return appSharedPrefs.getString(DEVICE_TOKEN, "");
    }


    private static final String IS_CLOCKED_IN = "is_p_in";
    private static final String IN_TIME = "in_time";
    private static final String LUNCH_IN_TIME = "lunch_in_time";
    private static final String LUNCH_END_TIME = "lunch_end_time";
    private static final String REMAINING_OUT_TIME = "r_o_time";
    private static final String IS_HAVING_LUNCH = "is_h_lunch";
    private static final String IS_LUNCH_TIME_OVER = "is_lunch_t_ovver";

    public void setInTime(String inTime) {
        prefsEditor.putString(IN_TIME, inTime);
        prefsEditor.commit();
    }

    public String getInTime() {
        return appSharedPrefs.getString(IN_TIME, "");
    }

    public void setLunchInTime(String inTime) {
        prefsEditor.putString(LUNCH_IN_TIME, inTime);
        prefsEditor.commit();
    }

    public String getLunchInTime() {
        return appSharedPrefs.getString(LUNCH_IN_TIME, "");
    }

    public void setLunchEndTime(String inTime) {
        prefsEditor.putString(LUNCH_END_TIME, inTime);
        prefsEditor.commit();
    }

    public String getLunchEndTime() {
        return appSharedPrefs.getString(LUNCH_END_TIME, "");
    }

    public void setClockedIn() {
        prefsEditor.putBoolean(IS_CLOCKED_IN, true);
        prefsEditor.apply();
    }

    public void setClockedOut() {
        prefsEditor.putBoolean(IS_CLOCKED_IN, false);
        prefsEditor.apply();
    }

    public void setIsHavingLunch() {
        prefsEditor.putBoolean(IS_HAVING_LUNCH, true);
        prefsEditor.apply();
    }

    public void setCompletedHavingLunch() {
        prefsEditor.putBoolean(IS_HAVING_LUNCH, false);
        prefsEditor.apply();
    }

    public boolean isHavingLunch() {
        return appSharedPrefs.getBoolean(IS_HAVING_LUNCH, false);
    }

    public boolean isClockedIn() {
        return appSharedPrefs.getBoolean(IS_CLOCKED_IN, false);
    }

    public void setRemainingTime(long remainingTime) {
        prefsEditor.putLong(REMAINING_OUT_TIME, remainingTime);
        prefsEditor.commit();
    }

    public void resetRemainingTime() {
        prefsEditor.putLong(REMAINING_OUT_TIME, 30 * 60 * 1000);
        prefsEditor.commit();
    }

    public long getRemainingTime() {
        return appSharedPrefs.getLong(REMAINING_OUT_TIME, 30 * 60 * 1000);
    }

    public void setLunchTimeOver(boolean isLunchTimeOver) {
        prefsEditor.putBoolean(IS_LUNCH_TIME_OVER, isLunchTimeOver);
        prefsEditor.apply();
    }

    public boolean isLunchTimeOver() {
        return appSharedPrefs.getBoolean(IS_LUNCH_TIME_OVER, false);
    }

    private String CO = "CO";
    private String LN = "LN";
    private String zip = "zip";
    private String Street = "Street";
    private String StreetNum = "StreetNum";
    private String City = "City";
    private String WP = "WP";
    private String CP = "CP";
    private String COUNTRYCODE = "CONTRYCODE";

    private static final String EMPID = "empid";
    private static final String WORKID = "workID";
    private static final String INDUSTRYID = "industryid";

    private static final String LEVID = "levid";

    private String DOB = "DOB";
    private String STE = "STE";

    private String St = "St";
    private String Email = "Email";
    private String Deliveries = "Deliveries";
    private String DRIVER_ID = "driverid";

    private String DELIVERY_INFO = "delivery_info";

    private String GENDAR = "Gendar";
    private String MARITAL = "Marital";

    private String LegalName = "LegalName";
    private String EIN = "EIN";

    private String LAST_UPDATE = "LastUpdate_Date";

    public String getCO() {
        return appSharedPrefs.getString(CO, "");
    }

    public void setCO(String co) {
        prefsEditor.putString(CO, co);
        prefsEditor.commit();
    }

    public String getLN() {
        return appSharedPrefs.getString(LN, "");
    }

    public void setLN(String userId) {
        prefsEditor.putString(LN, userId);
        prefsEditor.commit();
    }

    public String getZip() {
        return appSharedPrefs.getString(zip, "");
    }

    public void setZip(String _zip) {
        prefsEditor.putString(zip, _zip);
        prefsEditor.commit();
    }

    public String getStreet() {
        return appSharedPrefs.getString(Street, "");
    }

    public void setStreet(String street) {
        prefsEditor.putString(Street, street);
        prefsEditor.commit();
    }

    public String getStreetNum() {
        return appSharedPrefs.getString(StreetNum, "");
    }

    public void setStreetNum(String streetNum) {
        prefsEditor.putString(StreetNum, streetNum);
        prefsEditor.commit();
    }

    public String getCity() {
        return appSharedPrefs.getString(City, "");
    }

    public void setCity(String city) {
        prefsEditor.putString(City, city);
        prefsEditor.commit();
    }

    public String getWP() {
        return appSharedPrefs.getString(WP, "");
    }

    public void setWP(String wp) {
        prefsEditor.putString(WP, wp);
        prefsEditor.commit();
    }

    public String getCP() {
        return appSharedPrefs.getString(CP, "");
    }

    public void setCP(String cp) {
        prefsEditor.putString(CP, cp);
        prefsEditor.commit();
    }

    public String getCountryCode() {
        return appSharedPrefs.getString(COUNTRYCODE, "");
    }

    public void setCountryCode(String cc) {
        prefsEditor.putString(COUNTRYCODE, cc);
        prefsEditor.commit();
    }

    public String getSt() {
        return appSharedPrefs.getString(St, "");
    }

    public void setSt(String _st) {
        prefsEditor.putString(St, _st);
        prefsEditor.commit();
    }

    public String getEmail() {
        return appSharedPrefs.getString(Email, "");
    }

    public void setEmail(String _Email) {
        prefsEditor.putString(Email, _Email);
        prefsEditor.commit();
    }

    public String getDriverID() {
        return appSharedPrefs.getString(DRIVER_ID, "");
    }

    public void setDriverID(String _driverId) {
        prefsEditor.putString(DRIVER_ID, _driverId);
        prefsEditor.commit();
    }

    public String getDeliveryInfo() {
        return appSharedPrefs.getString(DELIVERY_INFO, "");
    }

    public void setDeliveryInfo(String _deliveryInfo) {
        prefsEditor.putString(DELIVERY_INFO, _deliveryInfo);
        prefsEditor.commit();
    }

    public ArrayList<String> getMyDeliveries() {

        String deliveriesArchives = appSharedPrefs.getString(Deliveries, "");
        if (TextUtils.isEmpty(deliveriesArchives)) {
            return null;
        } else {
            String[] deliveriIDs = deliveriesArchives.split("_");
            if (deliveriIDs != null && deliveriIDs.length > 0) {
                ArrayList<String> deliveriIDList = new ArrayList<>();
                for (String delID : deliveriIDs) {
                    deliveriIDList.add(delID);
                }
                return deliveriIDList;
            } else {
                return null;
            }
        }
    }

    public void addMyDelivery(String newDel) {
        String deliveries = appSharedPrefs.getString(Deliveries, "");
        deliveries += "_" + newDel;

        prefsEditor.putString(Deliveries, deliveries);
        prefsEditor.commit();
    }

    public String getDOB() {
        return appSharedPrefs.getString(DOB, "");
    }

    public void setDOB(String phone) {
        prefsEditor.putString(DOB, phone);
        prefsEditor.commit();
    }

    public String getSte() {
        return appSharedPrefs.getString(STE, "");
    }

    public void setSte(String _st) {
        prefsEditor.putString(STE, _st);
        prefsEditor.commit();
    }

    // CUTC
    public void setUTC(String cutc) {
        prefsEditor.putString(UTC_SETTING, cutc);
        prefsEditor.apply();
    }

    public String getUTC() {
        String utcValue = appSharedPrefs.getString(UTC_SETTING, "0");
        if (TextUtils.isEmpty(utcValue)) {
            utcValue = "60";
        }

        /*if (TextUtils.isEmpty(utcValue)) {
            Calendar mCalendar = new GregorianCalendar();
            TimeZone mTimeZone = mCalendar.getTimeZone();
            int mGMTOffset = mTimeZone.getRawOffset();
            utcValue = String.valueOf(TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS));
        }*/

        return utcValue;
    }

    // New Delivery ID
    public void setNewDelID(int val) {
        prefsEditor.putInt(NEW_DEL_ID, val);
        prefsEditor.apply();
    }

    public int getNewDelID() {
        return appSharedPrefs.getInt(NEW_DEL_ID, 0);
    }

    public String getGendar() {
        return appSharedPrefs.getString(GENDAR, "");
    }

    public void setGendar(String gendar) {
        prefsEditor.putString(GENDAR, gendar);
        prefsEditor.commit();
    }

    public String getMarital() {
        return appSharedPrefs.getString(MARITAL, "");
    }

    public void setMarital(String marital) {
        prefsEditor.putString(MARITAL, marital);
        prefsEditor.commit();
    }

    // EmpId
    public void setEmpId(int empId) {
        prefsEditor.putInt(EMPID, empId);
        prefsEditor.apply();
    }

    public int getEmpId() {
        return appSharedPrefs.getInt(EMPID, 0);
    }

    // WORKID
    public void setWorkid(int workId) {
        prefsEditor.putInt(WORKID, workId);
        prefsEditor.apply();
    }

    public int getWorkid() {
        return appSharedPrefs.getInt(WORKID, 0);
    }

    // LevId
    public void setALev(String levId) {
        prefsEditor.putString(LEVID, levId);
        prefsEditor.apply();
    }

    public String getALev() {
        return appSharedPrefs.getString(LEVID, "");
    }

    // IndustryID
    public void setIndustryid(String industryid) {
        prefsEditor.putString(INDUSTRYID, industryid);
        prefsEditor.apply();
    }

    public String getIndustryid() {
        return appSharedPrefs.getString(INDUSTRYID, "");
    }

    // Save Last Update Date
    public void setLastUpdateDate(String dumpData) {
        prefsEditor.putString(LAST_UPDATE, dumpData);
        prefsEditor.commit();
    }

    // Restore Update Date
    public String getLastUpdateDate() {
        return appSharedPrefs.getString(LAST_UPDATE, "");
    }

    // -1   : Deny
    // 0    : Unknow
    // 1    : Accepted
    private String LOCATIONPERMISSION = "LOCATIONPERMISSION";

    public int getLocationPermission() {
        return appSharedPrefs.getInt(LOCATIONPERMISSION, 0);
    }

    public void setLocationPermission(int value) {
        prefsEditor.putInt(LOCATIONPERMISSION, value);
        prefsEditor.commit();
    }

    // Last Update Date
    public String LAST_LOCATION_LAT = "LASTLOCATIONLAT";
    public String LAST_LOCATION_LON = "LASTLOCATIONLON";

    // Last Location
    public void setLastLocationLat(String value) {
        prefsEditor.putString(LAST_LOCATION_LAT, value);
        prefsEditor.commit();
    }

    public String getLastLocationLat() {
        return appSharedPrefs.getString(LAST_LOCATION_LAT, "0");
    }

    public void setLastLocationLon(String value) {
        prefsEditor.putString(LAST_LOCATION_LON, value);
        prefsEditor.commit();
    }

    public String getLastLocationLon() {
        return appSharedPrefs.getString(LAST_LOCATION_LON, "0");
    }

    // 0= not seen anything and not setup
    // 1= not set account & routing
    // 2= needs to verify
    // 3= they are verified so stay on tab(1)
    private String TRANSMONEY_STATUS = "TRANSMONEY_STATUS";

    public int getTransMoneyStatus() {
        return appSharedPrefs.getInt(TRANSMONEY_STATUS, 0);
    }

    public void setTransMoneyStatus(int value) {
        prefsEditor.putInt(TRANSMONEY_STATUS, value);
        prefsEditor.commit();
    }

    public static final String COUNTRY_LANG_ID = "countryLangID";

    public int getCountryLangId() {
        return appSharedPrefs.getInt(COUNTRY_LANG_ID, 0);
    }

    public void setCountryLangId(int countryLangId) {
        prefsEditor.putInt(COUNTRY_LANG_ID, countryLangId);
        prefsEditor.commit();
    }

    public static final String COUNTRY_LANG_CODE = "countryLangCode";

    public String getCountryLangCode() {
        return appSharedPrefs.getString(COUNTRY_LANG_CODE, "");
    }

    public void setCountryLangCode(String countryLangCode) {
        prefsEditor.putString(COUNTRY_LANG_CODE, countryLangCode);
        prefsEditor.commit();
    }

    // WHANDLE
    private static final String WHANDLE = "whandle";
    private static final String BHANDLE = "bhandle";

    public void setWHandle(String val) {
        prefsEditor.putString(WHANDLE, val);
        prefsEditor.apply();
    }

    public String getWHandle() {
        return appSharedPrefs.getString(WHANDLE, "");
    }


    public void setBHandle(String val) {
        prefsEditor.putString(BHANDLE, val);
        prefsEditor.apply();
    }

    public String getBHandle() {
        return appSharedPrefs.getString(BHANDLE, "");
    }

    private static final String DEPARTMENT = "department_name";

    public void setDepartName(String departName) {
        prefsEditor.putString(DEPARTMENT, departName);
        prefsEditor.apply();
    }

    public String getDepartName() {
        return appSharedPrefs.getString(DEPARTMENT, "");
    }

    public void setLegalName(String legalName) {
        prefsEditor.putString(LegalName, legalName);
        prefsEditor.apply();
    }

    public String getLegalName() {
        return appSharedPrefs.getString(LegalName, "");
    }

    public void setEIN(String EIN) {
        prefsEditor.putString(EIN, EIN);
        prefsEditor.apply();
    }

    public String getEIN() {
        return appSharedPrefs.getString(EIN, "");
    }
}