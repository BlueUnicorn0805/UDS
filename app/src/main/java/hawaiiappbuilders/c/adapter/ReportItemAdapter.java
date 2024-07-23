package hawaiiappbuilders.c.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import hawaiiappbuilders.c.R;


public class ReportItemAdapter extends RecyclerView.Adapter<ReportItemAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    public ArrayList<String> itemHeaderList;
    public ArrayList<JSONObject> itemDataList;
    private Context ctx;

    public ReportItemAdapter(Context ctx, ArrayList<String> itemHeaderList, ArrayList<JSONObject> itemDataList) {
        this.ctx = ctx;
        inflater = LayoutInflater.from(ctx);
        this.itemHeaderList = itemHeaderList;
        this.itemDataList = itemDataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.item_data_report, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        JSONObject jsonRow = itemDataList.get(position);

        int i = 0;
        for (Iterator<String> it = jsonRow.keys(); it.hasNext(); ) {
            if (i < itemHeaderList.size()) {
                String key = it.next();
                String cellValue = jsonRow.optString(key);
                holder.tvDataField.get(i++).setText(cellValue);
            }
        }

//        for (int i = 0; i < itemHeaderList.size(); i++) {
//            String cellValue = "";
//            try {
//                if (jsonRow.has(itemHeaderList.get(i))) {
//                    cellValue = jsonRow.getString(itemHeaderList.get(i));
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            holder.tvDataField.get(i).setText(cellValue);
//        }
    }

    @Override
    public int getItemCount() {
        return itemDataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        protected ArrayList<TextView> tvDataField = new ArrayList<>();

        public MyViewHolder(View itemView) {
            super(itemView);

            LinearLayout panelLayouts = itemView.findViewById(R.id.panelData);
            int subViewCnt = panelLayouts.getChildCount();
            int headerCnt = Math.min(subViewCnt, itemHeaderList.size());

            for (int i = 0; i < subViewCnt; i++) {
                if (i < headerCnt) {
                    LinearLayout childPanel = (LinearLayout) panelLayouts.getChildAt(i);
                    tvDataField.add((TextView) childPanel.findViewById(R.id.tvValue));
                } else {
                    panelLayouts.removeViewAt(headerCnt);
                }
            }
        }
    }
}
