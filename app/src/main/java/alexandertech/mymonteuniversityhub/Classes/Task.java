package alexandertech.mymonteuniversityhub.Classes;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by aymswick on 2/18/17.
 * This is the task class. Tasks have 3 attributes; a Name, a Due Date, and a Course.
 */

public class Task {
    protected String name;
    protected Calendar dueDate;
    private String course; // TODO: Replace this with a course class later

    public Task(String name) {
        this.name = name;
    }

    public Task(String name, Calendar dueDate) {
        this.name = name;
        this.dueDate = dueDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }


    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
