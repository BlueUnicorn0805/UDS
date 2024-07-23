package hawaiiappbuilders.c.model;

import com.google.gson.annotations.SerializedName;

public class DelDetailsInfo {

    @SerializedName("PULastestTime")
    private String DelTime;

    @SerializedName("Customer")
    private String DelCustomer;

    @SerializedName("Status")
    private String Status;

    @SerializedName("Bid")
    private String Bid;

    //@SerializedName("Rating")
    private String Rating;

    @SerializedName("Tip")
    private String Tip;

    public String getDelTime() { return DelTime; }
    public void setDelTime(String delTime) { DelTime = delTime; }

    public String getDelCustomer() { return DelCustomer; }
    public void setDelCustomer(String delCustomer) { DelCustomer = delCustomer; }

    public String getStatus() { return Status; }
    public void setStatus(String status) { Status = status; }

    public String getBid() { return Bid; }
    public void setBid(String bid) { Bid = bid; }

    public String getRating() { return Rating; }
    public void setRating(String rating) { Rating = rating; }

    public String getTip() { return Tip; }
    public void setTip(String tip) { Tip = tip; }
}
