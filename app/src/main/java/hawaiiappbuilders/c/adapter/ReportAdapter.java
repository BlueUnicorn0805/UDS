package hawaiiappbuilders.c.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.List;

import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.model.ReportInfo;


public class ReportAdapter extends BaseExpandableListAdapter implements View.OnClickListener {

    public interface ItemSelectListener {
        void onItemSelected(int groupPosition, int childPosition);
    }

    private Context mContext;
    private List<ReportInfo> mDataList;
    private ItemSelectListener mListener;

    public ReportAdapter(Context mContext, List<ReportInfo> dataList, ItemSelectListener listener) {
        this.mContext = mContext;
        this.mDataList = dataList;
        this.mListener = listener;
    }

    @Override
    public int getGroupCount() {
        if (mDataList == null)
            return 0;
        return mDataList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (mDataList.get(groupPosition).getChildIndustryInfo() == null) {
            return 0;
        } else {
            return mDataList.get(groupPosition).getChildIndustryInfo().size();
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mDataList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mDataList.get(groupPosition).getChildIndustryInfo().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_industries_header,
                    null);
        }

        // Get Item Data
        ReportInfo currentItem = (ReportInfo) getGroup(groupPosition);

        TextView tvIndustryName = convertView.findViewById(R.id.tvIndustryName);

        tvIndustryName.setText(currentItem.getGrp());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_industries_header,
                    null);
        }

        // Get Item Data
        ReportInfo currentItem = (ReportInfo) getChild(groupPosition, childPosition);

        CardView backView = convertView.findViewById(R.id.itemBack);
        TextView tvIndustryName = convertView.findViewById(R.id.tvIndustryName);
        tvIndustryName.setText(currentItem.getName());

        backView.setTag(new ItemIndicator(groupPosition, childPosition));
        backView.setOnClickListener(this);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public void onClick(View v) {
        ItemIndicator itemIndicator = (ItemIndicator) v.getTag();
        if (itemIndicator != null && mListener != null) {
            mListener.onItemSelected(itemIndicator.groupPosition, itemIndicator.childPosition);
        }
    }

    public class ItemIndicator {
        public int groupPosition;
        public int childPosition;

        public ItemIndicator(int groupPosition, int childPosition) {
            this.groupPosition = groupPosition;
            this.childPosition = childPosition;
        }
    }
}


