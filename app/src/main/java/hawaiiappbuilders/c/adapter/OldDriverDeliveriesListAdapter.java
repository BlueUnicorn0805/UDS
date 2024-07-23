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

public class OldDriverDeliveriesListAdapter extends BaseAdapter {

    private Context mContext;
    private List<DeliveryItem> mDataList;

    public OldDriverDeliveriesListAdapter(Context mContext, List<DeliveryItem> dataList) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_deliver_data,
                    null);
        }

        // Get Item Data
        DeliveryItem itemInfo = (DeliveryItem) getItem(position);

        // Init Controls
        TextView txtNo = (TextView) convertView.findViewById(R.id.txtNo);
        TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
        TextView txtAddress = (TextView) convertView.findViewById(R.id.txtAddress);
        TextView txtPickupTime = (TextView) convertView.findViewById(R.id.txtPickupTime);

        txtNo.setText(String.format("%d", position + 1));
        txtName.setText(itemInfo.getUserName());
        txtAddress.setText(String.format("%s %s", itemInfo.getStreetNum(), itemInfo.getStreet()));
        txtPickupTime.setText(itemInfo.gettAdd());

        return convertView;
    }
}


