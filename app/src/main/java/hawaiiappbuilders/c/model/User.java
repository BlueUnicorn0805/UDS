package hawaiiappbuilders.c.model;

import java.io.Serializable;

/**
 * Created by GeeksEra on 4/26/2018.
 */

public class User implements Serializable {

    private String UserId;
    private String Name;
    private String NotificationToken;
    private String Address;
    private Double Latitude;
    private Double Longitude;


    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getNotificationToken() {
        return NotificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        NotificationToken = notificationToken;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }
}
