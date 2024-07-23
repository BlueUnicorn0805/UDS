package hawaiiappbuilders.c.model;

import com.google.gson.annotations.SerializedName;

public class DriverDeliveryInfoOld {
    @SerializedName("DriverID")
    private String DriverID;

    @SerializedName("Name")
    private String Name;

    @SerializedName("DriverMLID")
    private String DriverMLID;

    @SerializedName("StreetNum")
    private String StreetNum;

    @SerializedName("Street")
    private String Street;

    // Currently Missing Data
    @SerializedName("DelID")
    private String DelID;

    @SerializedName("ToLat")
    private double ToLat;

    @SerializedName("ToLon")
    private double ToLon;

    @SerializedName("DelTime")
    private String DelTime;

    @SerializedName("FromAddress")
    private String FromAddress;

    @SerializedName("ToAddress")
    private String ToAddress;

    @SerializedName("Distance")
    private String Distance;

    public String getDriverID() { return DriverID; }
    public void setDriverID(String driverID) { DriverID = driverID; }

    public String getName() { return Name; }
    public void setName(String name) { Name = name; }

    public String getDriverMLID() { return DriverMLID; }
    public void setDriverMLID(String driverMLID) { DriverMLID = driverMLID; }

    public String getStreetNum() { return StreetNum; }
    public void setStreetNum(String streetNum) { StreetNum = streetNum; }

    public String getStreet() { return Street; }
    public void setStreet(String street) { Street = street; }

    public String getDelID() { return DelID; }
    public void setDelID(String delID) { DelID = delID; }

    public double getToLat() { return ToLat; }
    public void setToLat(double lat) { ToLat = lat; }

    public double getToLon() { return ToLon; }
    public void setToLon(double lon) { ToLon = lon; }

    public String getDelTime() { return DelTime; }
    public void setDelTime(String time) { DelTime = time; }

    public String getFromAddress() { return FromAddress; }
    public void setFromAddress(String fromAddress) { FromAddress = fromAddress; }

    public String getToAddress() { return ToAddress; }
    public void setToAddress(String toAddress) { ToAddress = toAddress; }

    public String getDistance() { return Distance; }
    public void setDistance(String distance) { Distance = distance; }
}
