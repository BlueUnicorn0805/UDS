package hawaiiappbuilders.c.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ReportInfo implements Parcelable {

    String Grp;
    String Name;
    int catID;
    int rptID;

    ArrayList<ReportInfo> childIndustryInfo = new ArrayList<>();

    public ReportInfo() {}

    protected ReportInfo(Parcel in) {
        Grp = in.readString();
        Name = in.readString();
        catID = in.readInt();
        rptID = in.readInt();

        childIndustryInfo = in.createTypedArrayList(ReportInfo.CREATOR);
    }

    public static final Creator<ReportInfo> CREATOR = new Creator<ReportInfo>() {
        @Override
        public ReportInfo createFromParcel(Parcel in) {
            return new ReportInfo(in);
        }

        @Override
        public ReportInfo[] newArray(int size) {
            return new ReportInfo[size];
        }
    };

    // Grp
    public String getGrp() { return Grp; }
    public void setGrp(String grp) { Grp = grp; }

    // Name
    public String getName() { return Name; }
    public void setName(String name) { Name = name; }

    // Cate ID
    public int getCatID() { return catID; }
    public void setCatID(int catID) { this.catID = catID; }

    // Rpt ID
    public int getRptID() { return rptID; }
    public void setRptID(int rptID) { this.rptID = rptID; }

    public ArrayList<ReportInfo> getChildIndustryInfo() { return childIndustryInfo; }
    public void setChildIndustryInfo(ArrayList<ReportInfo> childIndustryInfo) { this.childIndustryInfo = childIndustryInfo; }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Grp);
        dest.writeString(Name);
        dest.writeInt(catID);
        dest.writeInt(rptID);

        dest.writeTypedList(childIndustryInfo);
    }
}
