package alexandertech.mymonteuniversityhub.Interfaces;

import android.view.View;

/**
 * Created by aymswick on 4/14/17. Handles the onClick event for an item in our TaskList.
 */

public interface TaskItemClickListener {
    public void onItemClick(View v, int position);
}
