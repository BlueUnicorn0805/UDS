package hawaiiappbuilders.c.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import hawaiiappbuilders.c.AppSettings;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.model.ApptUtil;
import hawaiiappbuilders.c.model.CalendarData;

public class CalApptAdapter extends RecyclerView.Adapter<CalApptAdapter.ItemViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    private int groupPosition;
    CalTimeAdapter.RecyclerViewClickListener listener;

    ArrayList<CalendarData.Data> apptList;

    CalendarData.Data selectedItem;

    public CalApptAdapter(Context context, int grpPos, CalTimeAdapter.RecyclerViewClickListener listener) {
        this.context = context;
        groupPosition = grpPos;
        this.listener = listener;
    }

    public void setGroupPosition(int groupPosition) {
        this.groupPosition = groupPosition;
    }

    public void setApptList(ArrayList<CalendarData.Data> data) {
        this.apptList = data;
    }

    public void setActiveList(CalendarData.Data data) {
        this.selectedItem = data;
    }

    @Override
    public int getItemCount() {
        if (apptList == null)
            return 0;
        return apptList.size();
    }

    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();

        if (listener != null) {
            listener.onApptClick(view, groupPosition, position, apptList.get(position));
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int position = (int) view.getTag();

        if (listener != null) {
            listener.onApptLongClick(view, groupPosition, position, apptList.get(position));
        }

        return true;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        public TextView tvContents;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvContents = itemView.findViewById(R.id.tvContents);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_appt_child_item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        CalendarData.Data calData = apptList.get(position);

        ApptUtil apptUtil = new ApptUtil(holder.itemView.getContext());

        holder.tvContents.setText(calData.getName());

        AppSettings appSettings = new AppSettings(holder.itemView.getContext());
        String shareStatus = calData.getNote();
        /*int shareMLID = 0;
        try {
            shareMLID = Integer.parseInt(shareStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        holder.tvContents.setTag(position);
        holder.tvContents.setOnClickListener(this);
        holder.tvContents.setOnLongClickListener(this);
    }
}