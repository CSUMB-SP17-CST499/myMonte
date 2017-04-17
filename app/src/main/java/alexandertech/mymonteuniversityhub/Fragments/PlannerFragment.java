package alexandertech.mymonteuniversityhub.Fragments;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import alexandertech.mymonteuniversityhub.Adapters.TaskAdapter;
import alexandertech.mymonteuniversityhub.Classes.EventDecorator;
import alexandertech.mymonteuniversityhub.Classes.LiteDBHelper;
import alexandertech.mymonteuniversityhub.Classes.MyFirebaseInstanceIdService;
import alexandertech.mymonteuniversityhub.Classes.Task;
import alexandertech.mymonteuniversityhub.Interfaces.TaskItemClickListener;
import alexandertech.mymonteuniversityhub.R;

import static alexandertech.mymonteuniversityhub.Activities.MainActivity.sharedPrefs;

/**
 * Title: PlannerFragment
 * Authors: Joseph Molina, Anthony Symkowick
 * Date: 2/17/2017
 * Description: This file instantiates and connects the objects necessary for the PlannerFragment activity, including
 * a Calendar and a TaskList. The calendar will show students at-a-glance info about upcoming events via a custom Dialog.
 * The TaskList will be a dynamic set of Cards filled with user-defined TODO items.
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

        calendarView.state().edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit();
        calendarView.addDecorator(assignmentDot);

        //Instantiate FAB
        FloatingActionButton addTaskFab = (FloatingActionButton) v.findViewById(R.id.fab);
        addTaskFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    launchAddTaskDialog();
                } catch(Exception e) {
                    //TODO: handle exception
                }
            }
        });


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

        /**
         * TaskList Sync
         */

        try {
            tasks = requestTasksFromServer(); //Initialize TaskList with tasks from server
        } catch (Exception e) {
            tasks = new ArrayList<>(); //Make a brand new empty list if not found on server
            Log.d("TasksFromServer", "request failed...PlannerFrag:158");
        }

        taskAdapter = new TaskAdapter(getContext(), tasks, new TaskItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                try {
                    launchViewTaskDialog(tasks.get(position));
                } catch(IOException e) {

                }
            }
        }); //Connect ArrayList to the Adapter
        recList.setAdapter(taskAdapter); //Connect RecyclerView to the Adapter
        //So, it goes RecyclerView -> Adapter <- TaskArrayList :)

        //This helps the BottomSheetDialog handle keyboard input without hiding the Date & Time Buttons
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void launchAddTaskDialog() throws ExecutionException,InterruptedException {
        final BottomSheetDialog addTaskDialog = new BottomSheetDialog(getActivity());
        final View addTaskLayout = getActivity().getLayoutInflater().inflate(R.layout.bottomsheetdialog_addtask, null);
        addTaskDialog.setContentView(addTaskLayout);
        addTaskDialog.show();

        final Calendar todayDate = Calendar.getInstance();
        Toast.makeText(getContext(), todayDate.toString(), Toast.LENGTH_LONG);
        final Calendar selectedDate = Calendar.getInstance(); //Initialize to today's date

        final Button btnSave = (Button) addTaskLayout.findViewById(R.id.btnSaveTask);
        ImageButton btnDueDate = (ImageButton) addTaskLayout.findViewById(R.id.btnDueDate);
        ImageButton btnDueTime = (ImageButton) addTaskLayout.findViewById(R.id.btnDueTime);
        final EditText taskEditText = (EditText) addTaskLayout.findViewById(R.id.addTaskContent);

        //Start Handle Disable Empty Task Uploads
        btnSave.setEnabled(false);
        taskEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().length() > 0)
                {
                    btnSave.setEnabled(true);
                }
            }
        });
        //End Handle Disable Empty Task Uploads


        btnDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(year, month, dayOfMonth);
                        TextView dueDateText = (TextView) addTaskLayout.findViewById(R.id.dueDateText);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM d");

                        dueDateText.setText(simpleDateFormat.format(selectedDate.getTime()));
                    }
                }, todayDate.get(Calendar.YEAR), todayDate.get(Calendar.MONTH), todayDate.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.getDatePicker().setMinDate(todayDate.getTimeInMillis()); //Tasks can't be set in the past
                datePickerDialog.show();
            }
        });


        btnDueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDate.set(Calendar.MINUTE, minute);
                        TextView dueTimeText = (TextView) addTaskLayout.findViewById(R.id.dueTimeText);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");

                        dueTimeText.setText(simpleDateFormat.format(selectedDate.getTime()));
                    }
                }, todayDate.get(Calendar.HOUR_OF_DAY), todayDate.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDBHelper = new LiteDBHelper(getContext());
                sharedPreferences = getActivity().getSharedPreferences("MontePrefs", Context.MODE_PRIVATE);
                userFName = sharedPrefs.getString("First Name", "Monte"); //SharedPreferences retrieval takes Key and DefaultValue as parameters
                userLName = sharedPrefs.getString("Last Name", "Otter");
                userEmail = sharedPrefs.getString("Email", "monte@ottermail.com");
                userID = sharedPrefs.getString("ID", "12345");
                final String taskTitle = taskEditText.getText().toString();
                final String selectedDateString = Long.toString(selectedDate.getTime().getTime()); //gotta convert from cal to date to Unixtime
                SimpleDateFormat prettyDueDate = new SimpleDateFormat("MMM d, h:mm a");

                RunnableFuture f = new FutureTask(new Callable() {
                    public Integer call() {
                        try {
                            int taskID = liteDBHelper.insertTask(taskTitle, userID, selectedDateString, firebaseInstance.getFirebaseAndroidID());
                            Log.d("Timestamp Accuracy", selectedDateString);
                            return taskID;
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //If above failed
                        return 666;
                    }
                });

                new Thread(f).start();

                int taskID = 0;
                try {
                     taskID = (Integer) f.get();
                } catch(Exception e) {
                    Log.d("TaskIntConversionError", "oops int");
                }

                Task t = new Task(taskTitle, selectedDate, taskID);
                tasks.add(t);
                taskAdapter.notifyDataSetChanged();
                addTaskDialog.closeOptionsMenu();
                addTaskDialog.dismiss();
                Snackbar.make(getView(), "Saved \"" + taskTitle + "\" for " + prettyDueDate.format(selectedDate.getTime()), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void launchViewTaskDialog(final Task taskSelected) throws IOException {
        final Task t = taskSelected;
        final BottomSheetDialog viewTaskDialog = new BottomSheetDialog(getActivity());
        final View viewTaskLayout = getActivity().getLayoutInflater().inflate(R.layout.bottomsheetdialog_deletetask, null); //re-using this layout, tweaking into a View-Only version

        /**
         * Populate the bottomsheetdialog with specific task data.
         * This includes Title TextView, Date TextView, Time TextView, and a Delete button.
         */

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d"); //Formatter for Date (May 4)
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");//Formatter for Time (6:00pm)

        Log.d("DueDateTextView", taskSelected.getDueDate().toString());
        TextView title = (TextView) viewTaskLayout.findViewById(R.id.txtTaskName); //Title of task
        TextView date = (TextView) viewTaskLayout.findViewById(R.id.dueDateText); //Date of task
        TextView time = (TextView) viewTaskLayout.findViewById(R.id.dueTimeText); //Time of task

        title.setText(taskSelected.getName());
        date.setText(dateFormat.format(taskSelected.getDueDate().getTime()));
        time.setText(timeFormat.format(taskSelected.getDueDate().getTime()));


        Button delete = (Button) viewTaskLayout.findViewById(R.id.btnDeleteTask);

        viewTaskDialog.setContentView(viewTaskLayout);
        viewTaskDialog.show();

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    deleteTask(t);
                    tasks.remove(t);
                    viewTaskDialog.dismiss();
                    Snackbar.make(getView(), "Deleted task!", Snackbar.LENGTH_SHORT).show();
                } catch(IOException e) {
                    viewTaskDialog.dismiss();
                    Snackbar.make(getView(), "Oops, internal server error!", Snackbar.LENGTH_SHORT).show();
                    Log.d("DeleteError", e.toString());
                }
            }
        });

    }

    public ArrayList<Task> requestTasksFromServer() throws IOException,ExecutionException,InterruptedException {
        final LiteDBHelper liteDBHelper = new LiteDBHelper(getContext());
        sharedPreferences = getActivity().getSharedPreferences("MontePrefs", Context.MODE_PRIVATE);
        userID = sharedPrefs.getString("ID", "12345");
        ArrayList<Task> result;

        //RunnableFuture allows arraylist to be populated in a separate thread, and then returned
        RunnableFuture f = new FutureTask(new Callable() {
            public ArrayList<Task> call() throws IOException{
                ArrayList<Task> t = liteDBHelper.getTasksFromServer(userID);
                return t;
            }
        });

        new Thread(f).start();
        result = (ArrayList) f.get();
        return result;
    }

    public void deleteTask(final Task t) throws IOException {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final LiteDBHelper liteDBHelper = new LiteDBHelper(getContext());
                    sharedPreferences = getActivity().getSharedPreferences("MontePrefs", Context.MODE_PRIVATE);
                    userID = sharedPrefs.getString("ID", "12345");
                    Log.d("Object task id", Integer.toString(t.getId()));
                    liteDBHelper.deleteTask(userID, t.getId());
                    return;

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        taskAdapter.notifyDataSetChanged();

    }

}