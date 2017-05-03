package alexandertech.mymonteuniversityhub.Classes;

/**
 * Created by aymswick on 5/3/17.
 */

public class Assignment {
    private String name;
    private String course;
    private String duedate;


    public Assignment(String name, String course, String duedate) {
        this.name = name;
        this.course = course;
        this.duedate = duedate;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDuedate() {
        return duedate;
    }

    public void setDuedate(String duedate) {
        this.duedate = duedate;
    }
}
