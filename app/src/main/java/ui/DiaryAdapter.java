package ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.profilemanagement.R;
import com.google.android.material.card.MaterialCardView;

import model.DiaryEntry;
import java.util.ArrayList;
import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {
    private List<DiaryEntry> diaryEntries = new ArrayList<>();
    private final OnViewClickListener viewListener;

    public interface OnViewClickListener {
        void onViewClick(DiaryEntry entry);
    }

    public DiaryAdapter(OnViewClickListener viewListener) {
        this.viewListener = viewListener;
    }

    public void setDiaryEntries(List<DiaryEntry> entries) {
        this.diaryEntries = entries;
        notifyDataSetChanged();
    }

    @Override
    public DiaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary_entry, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DiaryViewHolder holder, int position) {
        DiaryEntry entry = diaryEntries.get(position);
        holder.titleText.setText(entry.title);
        holder.contentText.setText(entry.content);
        holder.dateText.setText(entry.updated_at);
        holder.editButton.setOnClickListener(v -> viewListener.onViewClick(entry));
    }

    @Override
    public int getItemCount() {
        return diaryEntries.size();
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, contentText, dateText;
        MaterialCardView editButton;

        DiaryViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.title);
            contentText = itemView.findViewById(R.id.content);
            dateText = itemView.findViewById(R.id.date);
            editButton = itemView.findViewById(R.id.diary_entry);
        }
    }
}