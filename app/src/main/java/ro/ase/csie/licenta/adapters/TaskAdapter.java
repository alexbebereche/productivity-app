package ro.ase.csie.licenta.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import ro.ase.csie.licenta.R;
import ro.ase.csie.licenta.classes.entities.Task;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskHolder> {
    private OnItemClickListener listener;

    public TaskAdapter() {
        super(DIFF_CALLBACK);
    }
    public int position; // added

    private static final DiffUtil.ItemCallback<Task>  DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getPriority() == newItem.getPriority() &&
                     oldItem.getNoOfPomodoros() == newItem.getNoOfPomodoros();  //here was the bug!!!
        }
    };

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        Task crtTask = getItem(position);
        holder.tvTaskName.setText(crtTask.getName());
        holder.tvPriority.setText(String.valueOf(crtTask.getPriority()));

        if(crtTask.hasTimer()){
            holder.tvPomodorosLeft.setText(crtTask.getNoOfPomodoros() * 25 + " minutes left");
        }
        else{
            holder.tvPomodorosLeft.setText("No timer set");
        }

        this.position = position;
    }

    public Task getTaskAtPosition(int position) {
        return getItem(position);
    }

    class TaskHolder extends RecyclerView.ViewHolder {
        private TextView tvTaskName;
        private TextView tvPriority;
        private TextView tvPomodorosLeft;


        public TaskHolder(@NonNull View itemView) {
            super(itemView);

            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvPomodorosLeft = itemView.findViewById(R.id.tvPomodorosLeft);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });
        }


    }

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


}
