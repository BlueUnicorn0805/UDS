package hawaiiappbuilders.c.model;

import com.google.gson.annotations.SerializedName;

public class StartVideoModel {

    @SerializedName("callid")
    private Integer callID;

    @SerializedName("msg")
    private String msg;

    @SerializedName("status")
    private boolean status;

    public String getMsg(){
        return msg;
    }

    public boolean isStatus(){
        return status;
    }

    public Integer getCallId() {
        return callID;
    }

    public void setCallId(int callId) {
        this.callID = callId;
    }
}
