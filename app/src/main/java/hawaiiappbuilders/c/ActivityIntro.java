package hawaiiappbuilders.c;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;

import android.util.Log;
import android.widget.VideoView;

import hawaiiappbuilders.c.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class ActivityIntro extends BaseActivity {

    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introvideo);

        // appSettings.setUserId("1");
        // appSettings.setLoggedIn();
        // appSettings.logOut();
        // Firebase
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> token) {
                if(token.isSuccessful()) {
                    String newToken = token.getResult();
                    // Log.e("Token3", newToken);
                    Log.e("ActivitySplash", newToken);
                    appSettings.setDeviceToken(newToken);
                }
            }
        });


        final VideoView videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setAlpha(0);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/raw/intro");
        videoView.setVideoURI(video);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        videoView.setAlpha(1);
                    }
                }, 200);
            }
        });

        isRunning = true;
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isRunning) {
                            return;
                        }

                        checkToken();
                    }
                }, 0);
            }
        });
    }

    private void checkToken() {

        mMyApp.updatedLocation();

        if (appSettings.isLoggedIn()) {
            startActivity(new Intent(mContext, MainActivity.class));
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            finish();
        } else {
            startActivity(new Intent(mContext, LoginActivity.class));
            //startActivity(new Intent(mContext, MainActivity.class));
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (this.isRunning) {
            this.isRunning = false;
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check All Permission was granted
        boolean bAllGranted = true;
        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                bAllGranted = false;
                break;
            }
        }

        if (bAllGranted) {
            checkToken();
        } else {
            showAlert(R.string.request_permission_hint);
        }
    }
}

// https://developer.squareup.com/docs/in-app-payments-sdk/what-it-does