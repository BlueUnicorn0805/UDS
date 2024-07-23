package hawaiiappbuilders.c.utils;

import android.content.Context;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class AgoraVCKit {

    // Fill in the App ID obtained from the Agora Console
    private String AGORA_APP_ID = "6e34b78ced7648c98baf1be932017f4e";

    private RtcEngine mRtcEngine;

    private static AgoraVCKit instance;

    public static AgoraVCKit getInstance() {
        if (instance == null) {
            instance = new AgoraVCKit();
        }
        return instance;
    }

    public void init(Context context){
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = context;
            config.mAppId = AGORA_APP_ID;
            mRtcEngine = RtcEngine.create(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy(){
        if (mRtcEngine != null) {
            mRtcEngine.stopPreview();
            mRtcEngine.leaveChannel();
            mRtcEngine = null;
            RtcEngine.destroy();
        }
    }

    public void setEventHandler(IRtcEngineEventHandler mRtcEventHandler){
        mRtcEngine.removeHandler(mRtcEventHandler);
        mRtcEngine.addHandler(mRtcEventHandler);
    }

    public void stopPreview(){
        mRtcEngine.stopPreview();
    }

    public void setLocalPreview(FrameLayout localViewContainer, Context context){
        mRtcEngine.enableVideo();
        mRtcEngine.startPreview();
        SurfaceView surfaceView = new SurfaceView(context);
        localViewContainer.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    public void setupRemoteVideo(int uid, FrameLayout remoteContainer, Context context) {
        SurfaceView surfaceView = new SurfaceView (context);
        surfaceView.setZOrderMediaOverlay(true);
        remoteContainer.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }


    public void joinChannel(String token, String channelName){
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION;
        mRtcEngine.joinChannel(token, channelName, 0, options);
    }

    public void muteAudio(boolean val){
        mRtcEngine.muteLocalAudioStream(val);
    }

    public void switchLocalVideo(){
        mRtcEngine.switchCamera();
    }

    public void switchRemoteVideo(boolean val){
        mRtcEngine.muteAllRemoteVideoStreams(val);
    }
}
