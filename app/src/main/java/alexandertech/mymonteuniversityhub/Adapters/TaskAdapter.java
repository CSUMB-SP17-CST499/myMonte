package alexandertech.mymonteuniversityhub.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import alexandertech.mymonteuniversityhub.Classes.Task;
import alexandertech.mymonteuniversityhub.R;

/**
 * Created by aymswick on 2/18/17.
 * http://www.binpress.com/tutorial/android-l-recyclerview-and-cardview-tutorial/156
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {


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
    public void onBindViewHolder(TaskViewHolder taskViewHolder, int position) {
        Task t = taskList.get(position);
        System.out.println(taskViewHolder);
        taskViewHolder.taskName.setText(t.getName());

        return;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.tasklist, parent, false);
        return new TaskViewHolder(itemView);
    }

    /**
     * Created by aymswick on 2/18/17.
     * The TaskViewHolder tells the adapter which layout files to inject dynamic Tasks into.
     * This tutorial helped a lot. http://www.binpress.com/tutorial/android-l-recyclerview-and-cardview-tutorial/156
     */

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        protected TextView taskName;
        // TODO: add the rest of the task attributes (due date, course)

        public TaskViewHolder(View view){
            super(view);
            taskName = (TextView) view.findViewById(R.id.txtTaskName);
        }

        public void bindTaskInfo(TextView t){
            this.taskName.setText(t.getText());
        }

    }

}
