package hawaiiappbuilders.c.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.model.DelDetailsInfo;

import java.util.ArrayList;

public class DelsDetailsAdapter extends RecyclerView.Adapter<DelsDetailsAdapter.ItemViewHolder> {

    Context context;
    ArrayList<DelDetailsInfo> mTransactions;

    public DelsDetailsAdapter(Context context, ArrayList<DelDetailsInfo> mTransactions) {
        this.context = context;
        this.mTransactions = mTransactions;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        public TextView tvDelTime, tvDelCustomer, tvDelStatus, tvDelBid, tvDelRating, tvDelTip;

        public ItemViewHolder(View itemView) {
            super(itemView);

            tvDelTime = itemView.findViewById(R.id.tvDelTime);
            tvDelCustomer = itemView.findViewById(R.id.tvDelCustomer);
            tvDelStatus = itemView.findViewById(R.id.tvDelStatus);
            tvDelBid = itemView.findViewById(R.id.tvDelBid);
            tvDelRating = itemView.findViewById(R.id.tvDelRating);
            tvDelTip = itemView.findViewById(R.id.tvDelTip);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dels_detail, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        DelDetailsInfo itemData = mTransactions.get(position);
        holder.tvDelTime.setText(itemData.getDelTime());
        holder.tvDelCustomer.setText(itemData.getDelCustomer());
        holder.tvDelStatus.setText(itemData.getStatus());
        holder.tvDelBid.setText("$" + itemData.getBid());
        holder.tvDelRating.setText(itemData.getRating());
        holder.tvDelRating.setVisibility(View.GONE);
        holder.tvDelTip.setText("$" + itemData.getTip());
    }

    @Override
    public int getItemCount() {
        return mTransactions.size();
    }
}
