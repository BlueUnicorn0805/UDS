package hawaiiappbuilders.c.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;

import hawaiiappbuilders.c.AppSettings;
import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.model.ApptUtil;
import hawaiiappbuilders.c.model.CalendarData;
import hawaiiappbuilders.c.utils.DateUtil;

public class CalTimeAgendaAdapter extends RecyclerView.Adapter<CalTimeAgendaAdapter.ItemViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    RecyclerViewClickListener listener;

    ArrayList<CalendarData.Data> mapData;
    private String TAG = CalTimeAgendaAdapter.class.getSimpleName();

    public CalTimeAgendaAdapter(Context context, RecyclerViewClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        int clickItemID = (int) view.getTag();

        if (listener != null) {
            listener.onClick(view, clickItemID);
        }

        notifyDataSetChanged();
    }

    @Override
    public boolean onLongClick(View view) {
        int clickItemID = (int) view.getTag();

        if (listener != null) {
            listener.onClick(view, clickItemID);
        }

        return true;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout mMainLinearLayout;
        public TextView tvTime;
        public TextView tvContents;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mMainLinearLayout = itemView.findViewById(R.id.main_ll);
            tvTime = itemView.findViewById(R.id.tvTime);

            tvContents = itemView.findViewById(R.id.tvContents);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_appt_timeline_item_agenda, parent, false);
        return new ItemViewHolder(v);
    }

    private String getTimeTitle(int position) {
        int hour = position + 1;

        String timeTile = String.format("%d %s",
                (hour <= 12) ? hour : hour - 12,
                (hour < 12) ? "AM" : "PM");
        return timeTile;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        CalendarData.Data itemData = mapData.get(position);
        Date date = DateUtil.parseDataFromFormat12(itemData.getRoomPU().replace("T", " "));
        //String dateString = String.format("%s%s\n%s", date.getDate(), getDayOfMonthSuffix(date.getDate()), DateUtil.toStringFormat_10(date));
        String dateString = String.format("%s\n%s", DateUtil.dateToString(date, "EEE dd"), DateUtil.toStringFormat_10(date));

        holder.tvTime.setText(dateString);
        holder.tvContents.setText(itemData.getStairsPU());

        holder.mMainLinearLayout.setTag(position);
        holder.mMainLinearLayout.setOnClickListener(this);


        AppSettings appSettings = new AppSettings(holder.itemView.getContext());
        ApptUtil apptUtil = new ApptUtil(holder.itemView.getContext());

        holder.tvContents.setText(itemData.getName());
    }

    private String getDayOfMonthSuffix(int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    @Override
    public int getItemCount() {
        if (mapData == null) return 0;
        return mapData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    public interface RecyclerViewClickListener {
        void onClick(View view, int position);

        void onApptClick(View view, int groupPos, int position, CalendarData.Data calData);

        void onApptLongClick(View view, int groupPos, int position, CalendarData.Data calData);
    }

    public void notifyData(ArrayList<CalendarData.Data> mapData) {
        this.mapData = mapData;
        notifyDataSetChanged();
    }
}