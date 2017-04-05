package alexandertech.mymonteuniversityhub.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import alexandertech.mymonteuniversityhub.Classes.LiteDBHelper;
import alexandertech.mymonteuniversityhub.Fragments.MapsFragment;
import alexandertech.mymonteuniversityhub.Fragments.NewsFragment;
import alexandertech.mymonteuniversityhub.Fragments.PlannerFragment;
import alexandertech.mymonteuniversityhub.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static final String MY_PREFS_NAME = "MyPrefsFile";
    public static SharedPreferences sharedPrefs;
    public static SharedPreferences.Editor prefs;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private DrawerLayout drawerLayout;
    private String[] pageTitle = {"myPlanner", "News", "Parking"};
    private String studyRooms = "http://library2.csumb.edu/mrbs/mobilenow.php";
    public static String MYPREFERENCE = "myPref";
    private String food = "https://csumb.sodexomyway.com/smgmenu/display/csu-monterey%20bay%20dining%20common%20-%20resident%20dining";
    private String userEmail = "";
    private String userFName = "";
    private String userLname = "";
    private String userID = "";
    private String SESSION_ID = "";
    private int[] tabIcons = {
            R.mipmap.planner_icon,
            R.mipmap.ic_school_black_24dp,
            R.mipmap.ic_directions_car_black_24dp
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = getSharedPreferences(MY_PREFS_NAME,Context.MODE_PRIVATE);
        prefs = sharedPrefs.edit();


        Bundle extras = getIntent().getExtras();
        userEmail = extras.getString("Email");
        userFName = extras.getString("First Name");
        userLname = extras.getString("Last Name");
        userID = extras.getString("ID");
        SESSION_ID = extras.getString("SessionKey");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        setSupportActionBar(toolbar);

        //create default navigation drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < 3; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(pageTitle[i]).setIcon(tabIcons[i]));
        }


        //set gravity for tab bar
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //handling navigation view item event
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), MainActivity.this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(1);

        //setting the initial welcome message from when the user logs in
        Snackbar.make(findViewById(android.R.id.content), "Welcome, " + userFName + "!", Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.BLUE)
                .show();

        //setting Tab layout (number of Tabs = number of ViewPager pages)
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

    }

    private void setupTabIcons() {

    }


    class PagerAdapter extends FragmentPagerAdapter{

        String tabTitles[] = new String[]{"myPlanner", "News", "Parking"};
        public Fragment[] fragments = new Fragment[tabTitles.length];
        Context context;

        public PagerAdapter(FragmentManager fm, Context context){
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount(){
            return tabTitles.length;
        }

        @Override
        public Fragment getItem(int position){
            switch (position){
                case 0:
                    return new PlannerFragment();
                case 1:
                    return new NewsFragment();
                case 2:
                    return new MapsFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position){
            //Generate title based on item position
            return tabTitles[position];
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position){
            Fragment createdFragment = (Fragment)super.instantiateItem(container,position);
            fragments[position] = createdFragment;
            return createdFragment;
        }

    }

   /* @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],int[] grantResults){

        if(requestCode == MapsFragment.MY_PERMISSIONS_REQUEST_LOCATION){
            MapsFragment mapFragment = (MapsFragment) pagerAdapter.fragments[2];
            if(mapFragment != null){
                mapFragment.onRequestPermissionsResult(requestCode,permissions,grantResults);
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    */



    /*
    Method for the navigation Drawer that takes in the id from the navigation drawer
    and based on the view, an action will be performed.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.grades){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            WebView wv = new WebView(this);
            //url for the web api to get the users grades.
            wv.loadUrl("https://monteapp.me/moodle/monteapi/getGrades.php?id="+userID);
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                        }
            });

            alert.setView(wv);
            alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }

        if (id == R.id.DinningCommonsItem) {
           // viewPager.setCurrentItem(0);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("DC Food");

            WebView wv = new WebView(this);
            WebSettings webSettings = wv.getSettings();
            webSettings.setJavaScriptEnabled(true);
            wv.loadUrl("https://csumb.sodexomyway.com/smgmenu/display/csu-monterey%20bay%20dining%20common%20-%20resident%20dining");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();

        } else if (id == R.id.LibraryStudyRooms) {
            //Uri uri = Uri.parse(studyRooms);
            //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            //startActivity(intent);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Study room reserve");

            WebView wv = new WebView(this);
            WebSettings webSettings = wv.getSettings();
            webSettings.setJavaScriptEnabled(true);
            wv.loadUrl(studyRooms);
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alert.show();

           // viewPager.setCurrentItem(1);
        } else if (id == R.id.MapYourRoute) {
            viewPager.setCurrentItem(2);
        } else if (id == R.id.CampusPD) {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "18316550268"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }else if (id == R.id.logout){
            LiteDBHelper dbFlush = new LiteDBHelper(getApplicationContext());
            if(dbFlush.logout(SESSION_ID)){
                Intent redirectToLogin = new Intent (MainActivity.this, LoginActivity.class);
                redirectToLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(redirectToLogin);
            }
            else {
                Snackbar.make(findViewById(android.R.id.content), "There was an error, please uninstall the app to clear  the account!" , Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED)
                        .show();
            }

        } else if (id == R.id.close) {
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        assert drawerLayout != null;
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}