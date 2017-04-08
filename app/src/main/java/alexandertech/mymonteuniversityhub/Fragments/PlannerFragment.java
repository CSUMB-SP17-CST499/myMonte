package alexandertech.mymonteuniversityhub.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import alexandertech.mymonteuniversityhub.Adapters.TaskAdapter;
import alexandertech.mymonteuniversityhub.Classes.EventDecorator;
import alexandertech.mymonteuniversityhub.Classes.LiteDBHelper;
import alexandertech.mymonteuniversityhub.Classes.MyFirebaseInstanceIdService;
import alexandertech.mymonteuniversityhub.Classes.Task;
import alexandertech.mymonteuniversityhub.R;

import static alexandertech.mymonteuniversityhub.Activities.MainActivity.sharedPrefs;

/**
 * A simple {@link Fragment} subclass.
 */

public class PlannerFragment extends Fragment {

    private TaskAdapter taskAdapter;
    private CardView mCardView;
    private EventDecorator assignmentDot;
    private SharedPreferences sharedPreferences;
    private MyFirebaseInstanceIdService firebaseInstance;
    private LiteDBHelper liteDBHelper;
    private String userEmail = "";
    private String userFName = "";
    private String userLName = "";
    private String userID = "";
    private List<Task> tasks;

    View v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    /**
     * Title: PlannerFragment
     * Authors: Joseph Molina, Anthony Symkowick
     * Date: 2/17/2017
     * Description: This file instantiates and connects the objects necessary for the PlannerFragment activity, including
     * a Calendar and a TaskList. The calendar will show students at-a-glance info about upcoming events via a custom Dialog.
     * The TaskList will be a dynamic set of Cards filled with user-defined TODO items.
     */
        firebaseInstance = new MyFirebaseInstanceIdService();

        // XML Layout is inflated for fragment_planner
        v = inflater.inflate(R.layout.fragment_planner, container, false);
        View taskview = inflater.inflate(R.layout.tasklist, container, false);

        //Set Up CalendarView
        MaterialCalendarView calendarView = (MaterialCalendarView) v.findViewById(R.id.calendarView);


        //debug
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);

        ArrayList<CalendarDay> dates = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            CalendarDay day = CalendarDay.from(calendar);
            dates.add(day);
            calendar.add(Calendar.DATE, 5);
        }

        assignmentDot = new EventDecorator(R.color.csumb_blue, dates);
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                //TODO: Ask the MonteApi to return the relevant assignments for the selected date
            }
        });

        calendarView.setTopbarVisible(false);
        calendarView.state().edit()
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        calendarView.addDecorator(assignmentDot);

        //Instantiate FAB
        FloatingActionButton addTaskFab = (FloatingActionButton) v.findViewById(R.id.fab);
        addTaskFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddTaskDialog();
            }
        });

        //Setup CardView Behavior
        mCardView = (CardView) taskview.findViewById(R.id.taskCard);
        //TODO: Setup custom onclicklistener for touch and delete


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

            tasks = new ArrayList<>(); //Create a test List of Tasks
//            Date d = new Date();
//            for(int i = 0; i < 10; i++)
//            {
//                Task t = new Task(" " + i, d);
//                tasks.add(t);
//            }
            taskAdapter = new TaskAdapter(tasks);
            recList.setAdapter(taskAdapter);

        return v;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void launchAddTaskDialog() {
        final BottomSheetDialog addTaskDialog = new BottomSheetDialog(getActivity());
        final View addTaskLayout = getActivity().getLayoutInflater().inflate(R.layout.bottomsheetdialog_addtask, null);
        addTaskDialog.setContentView(addTaskLayout);
        addTaskDialog.show();

        Button save = (Button) addTaskLayout.findViewById(R.id.btnSaveTask);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDBHelper = new LiteDBHelper(getContext());
                sharedPreferences = getActivity().getSharedPreferences("MontePrefs", Context.MODE_PRIVATE);
                userFName = sharedPrefs.getString("First Name", "Monte"); //SharedPreferences retrieval takes Key and DefaultValue as parameters
                userLName = sharedPrefs.getString("Last Name", "Otter");
                userEmail = sharedPrefs.getString("Email", "monte@ottermail.com");
                userID = sharedPrefs.getString("ID", "12345");


                EditText taskEditText = (EditText) addTaskLayout.findViewById(R.id.addTaskContent);
                final String taskTitle = taskEditText.getText().toString();
                Date testDate = new Date(2017, 5, 1);
                testDate.getTime();

                final String testDateString = Long.toString(testDate.getTime());
                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            liteDBHelper.insertTask(taskTitle, userID, testDateString, firebaseInstance.getFirebaseAndroidID());
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                Task t = new Task(taskTitle, testDate);
                tasks.add(t);
                addTaskDialog.closeOptionsMenu();
            }
        });



    }
}