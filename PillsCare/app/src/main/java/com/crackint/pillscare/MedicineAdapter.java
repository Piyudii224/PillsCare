package com.crackint.pillscare;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crackint.pillscare.R;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    public interface MedicineClickListener {
        void onEdit(Medicine medicine);
        void onDelete(Medicine medicine);
    }

    private List<Medicine> medicineList;
    private MedicineClickListener listener;

    public MedicineAdapter(List<Medicine> medicineList, MedicineClickListener listener) {
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);
        holder.bind(medicine);
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvDays, tvFrequency;
        ImageButton btnEdit, btnDelete;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Medicine medicine) {
            tvName.setText(medicine.getName());
            tvTime.setText("Time: " + TextUtils.join(", ", medicine.getTimes()));
            tvDays.setText("Days: " + medicine.getDays());
            tvFrequency.setText("Frequency: " + medicine.getFrequency());

            btnEdit.setOnClickListener(v -> listener.onEdit(medicine));
            btnDelete.setOnClickListener(v -> listener.onDelete(medicine));
        }
    }
}
