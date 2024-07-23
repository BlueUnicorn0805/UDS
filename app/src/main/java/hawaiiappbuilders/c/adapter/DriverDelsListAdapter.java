package hawaiiappbuilders.c.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.model.DeliveryItem;

import java.util.List;

public class DriverDelsListAdapter extends BaseAdapter {

    private Context mContext;
    private List<DeliveryItem> mDataList;

    public DriverDelsListAdapter(Context mContext, List<DeliveryItem> dataList) {
        this.mContext = mContext;
        this.mDataList = dataList;
    }

    @Override
    public int getCount() {
        if (mDataList == null)
            return 0;
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_driver_deliver,
                    null);
        }

        // Get Item Data
        DeliveryItem itemInfo = (DeliveryItem) getItem(position);

        // Init Controls
        TextView txtFromAddr = (TextView) convertView.findViewById(R.id.txtFromAddr);
        TextView txtToAddr = (TextView) convertView.findViewById(R.id.txtToAddr);
        TextView txtDistance = (TextView) convertView.findViewById(R.id.txtDistance);

        txtFromAddr.setText(String.format("From : %s", itemInfo.getfAdd()));
        txtToAddr.setText(String.format("To : %s", itemInfo.gettAdd()));
        txtDistance.setText(String.format("Distance : %.2f Mile(s)", getDistanceMiles(itemInfo.getLat(), itemInfo.getLon(), itemInfo.getToLat(), itemInfo.getToLon())));

        return convertView;
    }

    private double getDistanceMiles(double initialLat, double initialLong,
                                    double finalLat, double finalLong) {
        final double MILES_PER_KILO = 0.621371;

        double distance = calculationByDistance(initialLat, initialLong, finalLat, finalLong);
        return distance * MILES_PER_KILO;
    }

    // Haversine Distance Calulator
    public double calculationByDistance(double initialLat, double initialLong,
                                        double finalLat, double finalLong) {
        int R = 6371; // km (Earth radius)
        double dLat = toRadians(finalLat - initialLat);
        double dLon = toRadians(finalLong - initialLong);
        initialLat = toRadians(initialLat);
        finalLat = toRadians(finalLat);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(initialLat) * Math.cos(finalLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public double toRadians(double deg) {
        return deg * (Math.PI / 180);
    }
}


