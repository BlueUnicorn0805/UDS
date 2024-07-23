package hawaiiappbuilders.c.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CalendarData {
    @SerializedName("data")
    List<Data> calendarData;

    public List<Data> getCalendarData() {
        return calendarData;
    }

    public static class   Data implements Parcelable {

        @SerializedName("tripID")
        int tripID;

        @SerializedName("clientID")
        int clientID;

        @SerializedName("oneWay")
        int oneWay;

        @SerializedName("Seats")
        int Seats;

        @SerializedName("Age")
        int Age;

        @SerializedName("FN")
        String FN;

        @SerializedName("LN")
        String LN;

        @SerializedName("Nick")
        String Nick;

        @SerializedName("CP")
        String CP;

        @SerializedName("weight")
        double weight;

        @SerializedName("puTime")
        String puTime;

        @SerializedName("apptTime")
        String apptTime;

        @SerializedName("WillCall")
        String WillCall;

        @SerializedName("milesPU")
        String milesPU;

        @SerializedName("FacilityPU")
        String FacilityPU;

        @SerializedName("addressPU")
        String addressPU;

        @SerializedName("cszPU")
        String cszPU;

        @SerializedName("FloorPU")
        String FloorPU;

        @SerializedName("RoomPU")
        String RoomPU;

        @SerializedName("BedPU")
        String BedPU;

        @SerializedName("StairsPU")
        String StairsPU;

        @SerializedName("NotePU")
        String NotePU;

        @SerializedName("miles")
        String miles;

        @SerializedName("Facility")
        String Facility;

        @SerializedName("address")
        String address;

        @SerializedName("Floor")
        String Floor;

        @SerializedName("Room")
        String Room;

        @SerializedName("Bed")
        String Bed;

        @SerializedName("Stairs")
        String Stairs;

        @SerializedName("Note")
        String Note;

        @SerializedName("Mode")
        String Mode;

        @SerializedName("Status")
        String Status;

        @SerializedName("Vehicle")
        String Vehicle;

        @SerializedName("Gender")
        String Gender;

        @SerializedName("LonPU")
        String LonPU;

        @SerializedName("LatPU")
        String LatPU;

        @SerializedName("Lon")
        String Lon;

        @SerializedName("Lat")
        String Lat;

        protected Data(Parcel in) {
            tripID = in.readInt();
            clientID = in.readInt();
            oneWay = in.readInt();

            Seats = in.readInt();
            Age = in.readInt();
            FN = in.readString();
            LN = in.readString();
            Nick = in.readString();
            CP = in.readString();
            weight = in.readDouble();
            puTime = in.readString();
            apptTime = in.readString();
            WillCall = in.readString();

            milesPU = in.readString();
            FacilityPU = in.readString();
            addressPU = in.readString();
            cszPU = in.readString();
            FloorPU = in.readString();
            RoomPU = in.readString();
            BedPU = in.readString();
            StairsPU = in.readString();
            NotePU = in.readString();

            miles = in.readString();
            Facility = in.readString();
            address = in.readString();
            Floor = in.readString();
            Room = in.readString();
            Bed = in.readString();
            Stairs = in.readString();
            Note = in.readString();

            Mode = in.readString();
            Status = in.readString();
            Vehicle = in.readString();
            Gender = in.readString();
            LonPU = in.readString();
            LatPU = in.readString();
            Lon = in.readString();
            Lat = in.readString();
        }

        public static final Creator<Data> CREATOR = new Creator<Data>() {
            @Override
            public Data createFromParcel(Parcel in) {
                return new Data(in);
            }

            @Override
            public Data[] newArray(int size) {
                return new Data[size];
            }
        };

        public int getTripID() {
            return tripID;
        }

        public void setTripID(int tripID) {
            this.tripID = tripID;
        }

        public int getClientID() {
            return clientID;
        }

        public void setClientID(int clientID) {
            this.clientID = clientID;
        }

        public int getOneWay() { return oneWay; }
        public void setOneWay(int oneWay) { this.oneWay = oneWay; }

        public int getSeats() { return Seats; }
        public void setSeats(int seats) { this.Seats = seats; }

        public int getAge() { return Age; }
        public void setAge(int age) { this.Age = age; }

        public String getFN() { return FN; }
        public void setFN(String FN) { this.FN = FN;}

        public String getNick() {
            return Nick;
        }
        public void setNick(String nick) {
            this.Nick = nick;
        }

        public String getCP() { return CP;}
        public void setCP(String value) { this.CP = value; }

        public double getWeight() {
            return weight;
        }
        public void setWeight(double weight) {
            this.weight = weight;
        }

        public String getPuTime() { return puTime; }
        public void setPuTime(String puTime) { this.puTime = puTime; }

        public String getApptTime() { return apptTime; }
        public void setApptTime(String apptTime) { this.apptTime = apptTime; }

        public String getWillCall() { return WillCall; }
        public void setWillCall(String willCall) { this.WillCall = willCall; }

        public String getMilesPU() { return milesPU; }
        public void setMilesPU(String milesPU) { this.milesPU = milesPU; }

        public String getFacilityPU() { return FacilityPU; }
        public void setFacilityPU(String facilityPU) { this.FacilityPU = facilityPU; }

        public String getFloorPU() { return FloorPU; }
        public void setFloorPU(String floorPU) { this.FloorPU = floorPU; }
        public int getStatusId() {
            int statusId = 0;
            try {
                statusId = Integer.parseInt(FloorPU);
            } catch (Exception e){}
            return statusId;
        }

        public String getRoomPU() { return RoomPU; }
        public void setRoomPU(String roomPU) { this.RoomPU = roomPU; }

        public String getBedPU() { return BedPU; }
        public void setBedPU(String bedPU) { this.BedPU = bedPU; }

        public String getStairsPU() {
            return StairsPU;
        }
        public void setStairsPU(String stairsPU) {
            this.StairsPU = stairsPU;
        }

        public String getFacility() { return Facility; }
        public void setFacility(String facility) { this.Facility = facility; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        //---------------------------- Not used now ----------------------------
        public String getLN() {
            return LN;
        }
        public void setLN(String LN) {
            this.LN = LN;
        }

        public String getMiles() { return miles; }
        public void setMiles(String miles) { this.miles = miles; }

        public String getAddressPU() { return addressPU; }
        public void setAddressPU(String addressPU) { this.addressPU = addressPU; }

        public String getCszPU() { return cszPU; }
        public void setCszPU(String cszPU) { this.cszPU = cszPU; }

        public String getNotePU() { return NotePU; }
        public void setNotePU(String notePU) { this.NotePU = notePU; }

        public String getFloor() { return Floor; }
        public void setFloor(String floor) { this.Floor = floor; }

        public String getRoom() { return Room; }
        public void setRoom(String room) { this.Room = room; }

        public String getBed() { return Bed; }
        public void setBed(String bed) { this.Bed = bed; }

        public String getStairs() {
            return Stairs;
        }

        public void setStairs(String stairs) {
            this.Stairs = stairs;
        }

        public String getNote() { return Note; }
        public void setNote(String note) { this.Note = note; }

        public String getMode() {
            return Mode;
        }
        public void setMode(String mode) {
            Mode = mode;
        }

        public String getStatus() { return Status; }
        public void setStatus(String status) { Status = status; }

        public String getVehicle() { return Vehicle; }
        public void setVehicle(String vehicle) { Vehicle = vehicle; }

        public String getGender() { return Gender; }
        public void setGender(String gender) { Gender = gender; }

        public String getLonPU() { return LonPU; }
        public void setLonPU(String lonPU) { LonPU = lonPU; }

        public String getLatPU() { return LatPU; }
        public void setLatPU(String latPU) { LatPU = latPU; }

        public String getLon() { return Lon; }
        public void setLon(String lon) { Lon = lon; }

        public String getLat() { return Lat; }
        public void setLat(String lat) { Lat = lat;}

        public String getName() {
            String name = FN + " " + LN;
            return name.trim();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(tripID);
            parcel.writeInt(clientID);
            parcel.writeInt(oneWay);

            parcel.writeInt(Seats);
            parcel.writeInt(Age);
            parcel.writeString(FN);
            parcel.writeString(LN);
            parcel.writeString(Nick);
            parcel.writeString(CP);
            parcel.writeDouble(weight);
            parcel.writeString(puTime);
            parcel.writeString(apptTime);
            parcel.writeString(WillCall);

            parcel.writeString(milesPU);
            parcel.writeString(FacilityPU);
            parcel.writeString(addressPU);
            parcel.writeString(cszPU);
            parcel.writeString(FloorPU);
            parcel.writeString(RoomPU);
            parcel.writeString(BedPU);
            parcel.writeString(StairsPU);
            parcel.writeString(NotePU);

            parcel.writeString(miles);
            parcel.writeString(Facility);
            parcel.writeString(address);
            parcel.writeString(Floor);
            parcel.writeString(Room);
            parcel.writeString(Bed);
            parcel.writeString(Stairs);
            parcel.writeString(Note);

            parcel.writeString(Mode);
            parcel.writeString(Status);
            parcel.writeString(Vehicle);
            parcel.writeString(Gender);
            parcel.writeString(LonPU);
            parcel.writeString(LatPU);
            parcel.writeString(Lon);
            parcel.writeString(Lat);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Data data = (Data) o;

            return tripID == data.tripID;
        }

        @Override
        public int hashCode() {
            return Age != 0 ? (int) Age : 0;
        }

        //-----------------------------------------------------------------------

    }

    private static List<Data> calendarDataList = new ArrayList<>();

    public static List<Data> getCalendarDataList() {
        return calendarDataList;
    }

    public static void setCalendarDataList(List<Data> calendarDataList) {
        CalendarData.calendarDataList.clear();
        CalendarData.calendarDataList.addAll(calendarDataList);
    }

    public static void resetCalendarDataList() {
        CalendarData.calendarDataList.clear();
    }
}
