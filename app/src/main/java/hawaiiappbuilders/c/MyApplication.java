package hawaiiappbuilders.c;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hawaiiappbuilders.c.location.GpsTracker;
import hawaiiappbuilders.c.messaging.AppFirebaseMessagingService;
import hawaiiappbuilders.c.utils.GoogleCertProvider;
import hawaiiappbuilders.c.utils.BaseFunctions;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks, LocationListener, LifecycleObserver {

	private static MyApplication INSTANCE;
	public static MyApplication getInstance() {
		return INSTANCE;
	}

	private LocationManager locationManager;
	public Location curLocation;
	public static boolean bHasGPS = false;

	private AppSettings appSettings;

	// 1 second
	public static final int GPS_MIN_TIME = 1000;
	// 1 meter
	public static final int GPS_MIN_DISTANCE = 1;

	@Override
	public void onCreate() {
		super.onCreate();

		INSTANCE = this;
		appSettings = new AppSettings(getApplicationContext());

		registerActivityLifecycleCallbacks(this);
		ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
	}

	public String getAndroidId() {
		TelephonyManager tm =
				(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
		Log.d("ID", "Android ID: " + androidId);
		return androidId;
	}

	public void updatedLocation() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}

		if (locationManager == null) {
			locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE, this);
				curLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}

			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE, this);
				Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (loc != null)
					curLocation = loc;
			}

			if (curLocation != null) {
				appSettings.setDeviceLat(String.valueOf(curLocation.getLatitude()));
				appSettings.setDeviceLng(String.valueOf(curLocation.getLongitude()));
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (!location.hasAccuracy())
			return;

		if (location.getProvider().equals(LocationManager.GPS_PROVIDER))
			bHasGPS = true;

		if (bHasGPS && location.getProvider().equals(LocationManager.NETWORK_PROVIDER))
			return;

		curLocation = location;
		appSettings.setDeviceLat(String.valueOf(curLocation.getLatitude()));
		appSettings.setDeviceLng(String.valueOf(curLocation.getLongitude()));
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {

	}

	@Override
	public void onProviderEnabled(String s) {

	}

	@Override
	public void onProviderDisabled(String s) {

	}

	public void checkAppStatus() {

		AppSettings appSettings = new AppSettings(this);

		if (!appSettings.isLoggedIn()) return;

		final Map<String, String> params = new HashMap<>();

		GpsTracker gpsTracker = new GpsTracker(getApplicationContext());
		String lat, lon;
		if(gpsTracker.canGetLocation()) {
			lat = String.valueOf(gpsTracker.getLatitude());
			lon = String.valueOf(gpsTracker.getLongitude());
		} else {
			lat = "0.0";
			lon = "0.0";
		}

		String baseUrl = BaseFunctions.getBaseUrl(this, "securityCk", BaseFunctions.MAIN_FOLDER, lat, lon, getAndroidId());
		String extraParams = "&mode=1" +
				"&WeMightNeedRefreshTokenLaterButNotInAppsNow=" + appSettings.getDeviceToken();;
		baseUrl += extraParams;
		String fullParams = "";
		for (String key : params.keySet()) {
			fullParams += String.format("&%s=%s", key, params.get(key));
		}

		Log.e("securityCk", fullParams);

		RequestQueue queue = Volley.newRequestQueue(this);

		//HttpsTrustManager.allowAllSSL();
		GoogleCertProvider.install(this);

		StringRequest sr = new StringRequest(Request.Method.POST, baseUrl, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.e("securityCk", response);

				if (!TextUtils.isEmpty(response)) {
					try {
						JSONArray jsonArray = new JSONArray(response);
						JSONObject jsonObject = jsonArray.getJSONObject(0)/*new JSONObject(response)*/;
						if (jsonObject.has("status") && !jsonObject.getBoolean("status")) {
							appSettings.clear();
							appSettings.logOut();

							Intent intent = new Intent(MyApplication.this, LoginActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(intent);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				return params;
			}
		};

		sr.setRetryPolicy(new DefaultRetryPolicy(
				25000,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		sr.setShouldCache(false);
		queue.add(sr);
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_STOP)
	void onAppBackgrounded() {
		Log.e("Awww", "App in background");
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_START)
	void onAppForegrounded() {
		Log.e("Awww", "App in foreground");

		checkAppStatus();
	}


	@Override
	public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

	}

	@Override
	public void onActivityStarted(Activity activity) {
		AppFirebaseMessagingService.setAppInBackground(false);
	}

	@Override
	public void onActivityResumed(Activity activity) {
		AppFirebaseMessagingService.setAppInBackground(false);

	}

	@Override
	public void onActivityPaused(Activity activity) {
		AppFirebaseMessagingService.setAppInBackground(true);
	}

	@Override
	public void onActivityStopped(@NonNull Activity activity) {

	}

	@Override
	public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

	}

	@Override
	public void onActivityDestroyed(@NonNull Activity activity) {

	}
}
