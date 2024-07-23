package hawaiiappbuilders.c.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hawaiiappbuilders.c.R;
import hawaiiappbuilders.c.model.Transaction;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ItemViewHolder> {

    Context context;
    ArrayList<Transaction> mTransactions;

    public TransactionAdapter(Context context, ArrayList<Transaction> mTransactions) {
        this.context = context;
        this.mTransactions = mTransactions;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public View itemTransaction;
        public TextView mTransactionEmail, mTransactionAmount, mTransactionDate, mTransactionId, mTransactionStatus;
        public TextView mTransactionMemo;
        public ItemViewHolder(View itemView) {
            super(itemView);
            itemTransaction = itemView.findViewById(R.id.itemTransaction);
            mTransactionEmail = itemView.findViewById(R.id.transaction_email);
            mTransactionAmount = itemView.findViewById(R.id.transaction_amount);
            mTransactionDate = itemView.findViewById(R.id.transaction_date);
            mTransactionId = itemView.findViewById(R.id.transaction_id);
            mTransactionStatus = itemView.findViewById(R.id.transaction_status);
            mTransactionMemo = itemView.findViewById(R.id.tvMemo);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item_row, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Transaction transaction = mTransactions.get(position);
        holder.mTransactionEmail.setText(transaction.getName().replace(", To", "\nTo"));

        double amt = 0;
        try {
            amt = Double.parseDouble(transaction.getAmt());
        } catch (Exception e) {
            e.printStackTrace();
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        String currency = format.format(amt);
        holder.mTransactionAmount.setText(currency/*"$".concat(String.format("%.02f", amt))*/);

        int orderID = 0;
        try {
            orderID = Integer.parseInt(transaction.getTxID());
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.mTransactionDate.setText(convertDate(transaction.getItemDate()));
        holder.mTransactionId.setText("OrderID : ".concat(String.format("%03d", orderID)));
    }

    @Override
    public int getItemCount() {
        return mTransactions.size();
    }

    private String convertDate(String dateString) {
        try {
            //create SimpleDateFormat object with source string date format
            SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy/MM/dd");

            //parse the string into Date object
            Date date = sdfSource.parse(dateString);

            //create SimpleDateFormat object with desired date format
            SimpleDateFormat sdfDestination = new SimpleDateFormat("MM-dd-yyyy");

            //parse the date into another format
            dateString = sdfDestination.format(date);
        } catch (ParseException pe) {
            System.out.println("Parse Exception : " + pe);
        }
        return dateString;
    }
}
