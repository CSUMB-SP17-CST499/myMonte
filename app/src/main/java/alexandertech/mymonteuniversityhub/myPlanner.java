package alexandertech.mymonteuniversityhub;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class myPlanner extends Fragment {

    private TaskAdapter taskAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    /**
     * Title: myPlanner
     * Authors: Joseph Molina, Anthony Symkowick
     * Date: 2/17/2017
     * Description: This file instantiates and connects the objects necessary for the myPlanner activity, including
     * a Calendar and a TaskList. The calendar will show students at-a-glance info about upcoming events via a custom Dialog.
     * The TaskList will be a dynamic set of Cards filled with user-defined TODO items.
     */

        // XML Layout is inflated for fragment_myplanner
        View v = inflater.inflate(R.layout.fragment_myplanner, container, false);

        /*
         * Before we return the inflated view, we will instantiate a RecyclerView object and reference the xml element.
         * The RecyclerView will serve as the manager for dynamic Tasks in the TaskList.
         * For info about the container and/or how fragments work compared to activities,
         * see https://developer.android.com/guide/components/fragments.html#Creating
         */

        RecyclerView recList = (RecyclerView) v.findViewById(R.id.cardList); // Connect RecyclerView (cardList) and set its layout manager

            recList.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recList.setLayoutManager(llm);

            //Dummy Data for tasks to display in the recycler view
            List<Task> tl = new ArrayList<>(); //Create a test List of Tasks
            Date d = new Date();
            for(int i = 0; i < 10; i++)
            {
                Task t = new Task(" " + i, d);
                tl.add(t);
            }
            taskAdapter = new TaskAdapter(tl);
            recList.setAdapter(taskAdapter);

        return v;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}