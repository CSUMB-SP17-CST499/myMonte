package alexandertech.mymonteuniversityhub.Adapters;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import alexandertech.mymonteuniversityhub.Activities.MainActivity;
import alexandertech.mymonteuniversityhub.Classes.Task;
import alexandertech.mymonteuniversityhub.Fragments.PlannerFragment;
import alexandertech.mymonteuniversityhub.Interfaces.TaskItemClickListener;
import alexandertech.mymonteuniversityhub.R;

/**
 * Created by aymswick on 2/18/17.
 * http://www.binpress.com/tutorial/android-l-recyclerview-and-cardview-tutorial/156
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> implements View.OnClickListener {


    private List<Task> taskList; // A list of the current tasks
    Context mContext;
    TaskItemClickListener listener;


    public TaskAdapter(Context c, List<Task> taskList, TaskItemClickListener tl) {
        this.mContext = c;
        this.taskList = taskList;
        this.listener = tl;
    }

    public void onClick(View v) {

    }

    // These 3 methods must be overridden for the recycling cards to be populated and counted.

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder taskViewHolder, int position) {
        //Binds a task to a specific view (in this case, a GoogleNow-style card)
        Task t = taskList.get(position);
        System.out.println(taskViewHolder);
        taskViewHolder.taskName.setText(t.getName());
        taskViewHolder.id = t.getId();
        return;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.tasklist, parent, false);

        final TaskViewHolder mViewHolder = new TaskViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, mViewHolder.getPosition()); //TODO: Get position is deprecated may be causing the bug.
            }
        });

        return mViewHolder;
    }

    /**
     * Created by aymswick on 2/18/17.
     * The TaskViewHolder tells the adapter which layout files to inject dynamic Tasks into.
     * This tutorial helped a lot. http://www.binpress.com/tutorial/android-l-recyclerview-and-cardview-tutorial/156
     */

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        protected TextView taskName;
        private int id;
        private Calendar dueDate;
        private CardView cardView;
        // TODO: add the rest of the task attributes (due date, course)

        public TaskViewHolder(View view){
            super(view);

            cardView = (CardView) view.findViewById(R.id.taskCard);
            taskName = (TextView) view.findViewById(R.id.txtTaskName);
        }

        public void bindTaskInfo(TextView t){
            this.taskName.setText(t.getText());
        }

    }


}
