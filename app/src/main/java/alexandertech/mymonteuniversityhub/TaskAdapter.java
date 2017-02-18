package alexandertech.mymonteuniversityhub;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by aymswick on 2/18/17.
 */

public class TaskAdapter extends RecyclerView.Adapter {


    private List<Task> taskList; // A list of the current tasks

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }


    // These 3 methods must be overridden for the recycling cards to be populated and counted.

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.fragment_myplanner, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder taskViewHolder, int position) {
        Task t = taskList.get(position);
        taskViewHolder.taskName.setText(t.name);
    }

}
