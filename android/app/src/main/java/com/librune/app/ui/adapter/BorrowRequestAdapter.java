package com.librune.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.librune.app.R;
import com.librune.app.model.BorrowDetail;
import com.librune.app.model.BorrowRequest;

import java.util.ArrayList;
import java.util.List;

public class BorrowRequestAdapter extends RecyclerView.Adapter<BorrowRequestAdapter.BorrowViewHolder> {

    public interface OnCancelListener {
        void onCancel(BorrowRequest request);
    }

    private final OnCancelListener cancelListener;
    private List<BorrowRequest> requests = new ArrayList<>();

    public BorrowRequestAdapter(OnCancelListener cancelListener) {
        this.cancelListener = cancelListener;
    }

    public void setRequests(List<BorrowRequest> requests) {
        this.requests = requests != null ? requests : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BorrowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_borrow_request, parent, false);
        return new BorrowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowViewHolder holder, int position) {
        BorrowRequest req = requests.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvId.setText("Phiếu #" + req.getId());
        String status = req.getStatus() != null ? req.getStatus() : "";
        holder.tvStatus.setText(status);

        int colorRes = R.color.librune_muted;
        if ("PENDING".equals(status)) colorRes = R.color.librune_warning;
        else if ("APPROVED".equals(status)) colorRes = R.color.librune_accent;
        else if ("RETURNED".equals(status)) colorRes = R.color.librune_success;
        else if ("REJECTED".equals(status) || "CANCELLED".equals(status)) colorRes = R.color.librune_danger;
        holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, colorRes));

        StringBuilder booksText = new StringBuilder();
        if (req.getDetails() != null) {
            for (int i = 0; i < req.getDetails().size(); i++) {
                BorrowDetail d = req.getDetails().get(i);
                if (i > 0) booksText.append("\n");
                String title = d.getBook() != null ? d.getBook().getTitle() : "Sách";
                booksText.append("• ").append(title).append(" (x").append(d.getQuantity()).append(")");
            }
        }
        holder.tvBooks.setText(booksText.toString());

        String dates = "Ngày gửi: " + formatIso(req.getRequestDate());
        if (req.getDueDate() != null) {
            dates += " | Hạn trả: " + formatIso(req.getDueDate());
        }
        holder.tvDates.setText(dates);

        if ("PENDING".equals(status)) {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> cancelListener.onCancel(req));
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }
    }

    private String formatIso(String iso) {
        if (iso == null) return "-";
        return iso.length() >= 10 ? iso.substring(0, 10) : iso;
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class BorrowViewHolder extends RecyclerView.ViewHolder {
        final TextView tvId;
        final TextView tvStatus;
        final TextView tvBooks;
        final TextView tvDates;
        final Button btnCancel;

        BorrowViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvBorrowId);
            tvStatus = itemView.findViewById(R.id.tvBorrowStatus);
            tvBooks = itemView.findViewById(R.id.tvBorrowBooks);
            tvDates = itemView.findViewById(R.id.tvBorrowDates);
            btnCancel = itemView.findViewById(R.id.btnCancelBorrow);
        }
    }
}
