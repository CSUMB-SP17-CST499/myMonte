package alexandertech.mymonteuniversityhub;

/**
 * Created by josephmolina on 2/16/17.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/*
This class is an Adapter for the ViewPager which will determine which tab to display.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position){
        if(position == 0){
            return new myPlanner();
        }
        else if(position == 1){
            return new News();
        }
        else{
            return new MapsFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}