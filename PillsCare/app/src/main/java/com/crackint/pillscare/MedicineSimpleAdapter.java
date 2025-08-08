package com.crackint.pillscare;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicineSimpleAdapter extends RecyclerView.Adapter<MedicineSimpleAdapter.ViewHolder> {

    private final List<Medicine> medicines;

    public MedicineSimpleAdapter(List<Medicine> medicines) {
        this.medicines = medicines;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemlayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);
        holder.tvName.setText(medicine.getName());
        holder.tvTime.setText("Time: " + TextUtils.join(", ", medicine.getTimes()));
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_med_name);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
