package alexandertech.mymonteuniversityhub.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import alexandertech.mymonteuniversityhub.Classes.LiteDBHelper;
import alexandertech.mymonteuniversityhub.Classes.MyFirebaseInstanceIdService;
import alexandertech.mymonteuniversityhub.Fragments.MapsFragment;
import alexandertech.mymonteuniversityhub.Fragments.PlannerFragment;
import alexandertech.mymonteuniversityhub.Fragments.StudyRoomsFragment;
import alexandertech.mymonteuniversityhub.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static final String MY_PREFS_NAME = "MontePrefs";
    public static SharedPreferences sharedPrefs;
    public static SharedPreferences.Editor prefs;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private DrawerLayout drawerLayout;
    private String[] pageTitle = {"myPlanner", "Study Rooms", "Parking"};
    private String newsPage = "https://csumb.edu/news";
    private String reportIssue = "https://docs.google.com" +
            "/forms/d/e/1FAIpQLSczSktOIv7Dusil6OiikwsOMhM1Yq3" +
            "oWjwIoFBU3YQnOR0bwg/viewform?usp=sf_link";
    private String food = "https://csumb.sodexomyway.com/" +
            "smgmenu/display/csu-monterey%20bay%20dining%20common%20-%20resident%20dining";
    private String userEmail = "";
    private String userFName = "";
    private String userLName = "";
    private String userID = "";
    private String SESSION_ID = "";
    private int[] tabIcons = {
            R.mipmap.planner_icon,
            R.mipmap.ic_local_library_black_24dp,
            R.mipmap.ic_directions_car_black_24dp
    };

    NetworkInfo networkInfo;
    HashMap<String, String> buildingMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpPrefs();
        setUpNavigationDrawer();
        setUpTabViewPager();
        showWelcomeMessage();
        setUpBuildingMap();
    }

    public void setUpPrefs(){
        sharedPrefs = getSharedPreferences(MY_PREFS_NAME,Context.MODE_PRIVATE);
        prefs = sharedPrefs.edit();
        gatherUserInfoFromSharedPreferences();
        prefs.apply();
        System.out.println("User ID: " + userID);
    }

    public void setUpNavigationDrawer(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        setSupportActionBar(toolbar);
        //create default navigation drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void setUpTabViewPager(){
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

        //setting Tab layout (number of Tabs = number of ViewPager pages)
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

    }

    public void showWelcomeMessage(){
        Snackbar.make(findViewById(android.R.id.content), "Welcome, "
                + userFName + "!", Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.BLUE)
                .show();
    }


    public void setUpBuildingMap(){
        buildingMap.put("Administration Building(1)","36.653364, -121.798278");
        buildingMap.put("Alumni and Visitors Center(97)","36.654635, -121.801792");
        buildingMap.put("Aquatic Center(100)","36.651590, -121.807439");
        buildingMap.put("Asilomar Hall(203)","36.653273, -121.796321");
        buildingMap.put("Avocet Hall(208)","36.653490, -121.799627");
        buildingMap.put("Beach Hall(21)","36.652818, -121.799203");
        buildingMap.put("Chapman Science Academic Center(53)","36.653692, -121.794803");
        buildingMap.put("Child Care Center(91)","36.654235, -121.806792");
        buildingMap.put("Cinematic Arts and Technology(27)","36.652076, -121.793929");
        buildingMap.put("Coast Hall(45)","36.650911, -121.793096");
        buildingMap.put("Cypress Hall(202)","36.653830, -121.795826");
        buildingMap.put("Dining Commons(16)","36.654398, -121.798917");
        buildingMap.put("Dunes Hall(10)","36.653869, -121.800673");
        buildingMap.put("Facilities Services and Operations(37)","36.649167, -121.787747");
        buildingMap.put("Field House(902)","36.649703, -121.805912");
        buildingMap.put("Field Office(902C)", "36.649703, -121.805912");
        buildingMap.put("Freeman field(STADIUM)","36.650983, -121.805012");
        buildingMap.put("Gavilan Hall(201)","36.654621, -121.792968");
        buildingMap.put("Green Hall(58)","36.652091, -121.790566");
        buildingMap.put("Harbor Hall(46)","36.651312, -121.793062");
        buildingMap.put("Health and Wellness Services(80)","36.655772, -121.803121");
        buildingMap.put("Heron Hall(18)","36.654277, -121.799739");
        buildingMap.put("IT Services(43)","36.649533, -121.793127");
        buildingMap.put("Joel and Dena Gambord Business and Information Technology Building(506)","36.652593, -121.797327");
        buildingMap.put("Manzanita Hall(205)","36.653466, -121.797020");
        buildingMap.put("Meeting House(98)","36.653444, -121.801516");
        buildingMap.put("Mountain Hall(84)","36.655724, -121.806348");
        buildingMap.put("Music Hall(30)","36.648099, -121.794442");
        buildingMap.put("Ocean Hall(86)","36.655713, -121.807124");
        buildingMap.put("Otter Express(14)","36.654239, -121.798185");
        buildingMap.put("Otter Soccer Complex(SOCCER)","36.649808, -121.808161");
        buildingMap.put("Otter Sports Center(90)","36.654608, -121.808206");
        buildingMap.put("Pacific Hall(44)","36.650241, -121.793155");
        buildingMap.put("Reading Center(59)","36.652713, -121.790520");
        buildingMap.put("Science Instructional Lab Annex (50)","36.652811, -121.793809");
        buildingMap.put("Softball field(SOFTBALL)","36.648767, -121.805215");
        buildingMap.put("Strawberry Apartments(301)","36.655669, -121.800360");
        buildingMap.put("Student Center(12)", "36.654429, -121.797398");
        buildingMap.put("Student Services(47)", "Student Services(47)");
        buildingMap.put("Surf Hall(6)","36.653685, -121.797351");
        buildingMap.put("Tanimura and Antle Family Memorial Library(508)", "36.652583, -121.796194");
        buildingMap.put("University Center(29)","36.650260, -121.794185");
        buildingMap.put("Watershed Institute(42)","36.650004, -121.792673");
        buildingMap.put("Willet Hall(204)", "36.653982, -121.796404");
        buildingMap.put("World Languages and Cultures-North(49)", "36.652760, -121.792603");
        buildingMap.put("World Languages and Cultures-South(48)", "36.652347, -121.792686");
        buildingMap.put("World Theater(28)", "36.650861, -121.793879");
        buildingMap.put("Yarrow Hall(206)", "36.653505, -121.797649");
    }

    class PagerAdapter extends FragmentPagerAdapter{

        String tabTitles[] = new String[]{"myPlanner", "Study Rooms", "Parking"};
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
                        return new StudyRoomsFragment();
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
    /*
    Method for the navigation Drawer that takes in the id from the navigation drawer
    and based on the view, an action will be performed.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.grades){
            if(hasInternetConnection()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                WebView wv = new WebView(this);
                //url for the web api to get the users grades.
                wv.loadUrl("https://monteapp.me/moodle/monteapi/getGrades.php?id=" + userID);
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
            }else{
                displaySnackbar();
            }
        }

        if (id == R.id.DinningCommonsItem) {
            if (hasInternetConnection()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("DC Food");
                WebView wv = new WebView(this);
                WebSettings webSettings = wv.getSettings();
                webSettings.setJavaScriptEnabled(true);
                wv.loadUrl("https://csumb.sodexomyway.com/smgmenu/display/csu-monterey%" +
                        "20bay%20dining%20common%20-%20resident%20dining");
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
            }else{
                displaySnackbar();
            }

        } else if (id == R.id.CampusNews) {
            if (hasInternetConnection()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                WebView wv = new WebView(this);
                WebSettings webSettings = wv.getSettings();
                webSettings.setJavaScriptEnabled(true);
                wv.loadUrl(newsPage);
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
            }else{
                displaySnackbar();
            }
        } else if (id == R.id.MapYourRoute) {
            onMapYourRoute();

        } else if (id == R.id.CampusPD) {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "18316550268"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }else if (id == R.id.reportIssue){
            Snackbar.make(findViewById(android.R.id.content), "Sorry, this feature is broken" , Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.BLUE)
                    .show();

            /*if (hasInternetConnection()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Report an Issue");
                WebView wv = new WebView(this);
                WebSettings webSettings = wv.getSettings();
                webSettings.setJavaScriptEnabled(true);
                wv.loadUrl(reportIssue);
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });
                alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
            else{
                displaySnackbar();
            }
*/
        }else if (id == R.id.logout) {
            if (hasInternetConnection()) {
                final LiteDBHelper dbFlush = new LiteDBHelper(getApplicationContext());
                final MyFirebaseInstanceIdService firebaseID = new MyFirebaseInstanceIdService();
                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dbFlush.clearSessionFromRemoteDB(firebaseID.getFirebaseAndroidID());
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent redirectToSpalsh = new Intent(MainActivity.this, LoginActivity.class);
                        redirectToSpalsh.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(redirectToSpalsh);
                    }
                });
                thread.start();

                finish();
            }else{
                displaySnackbar();
            }
            }

        else if (id == R.id.wowMenu) {
            if (hasInternetConnection()) {
                Uri uri = Uri.parse("https://drive.google.com/viewerng/viewer" +
                        "?embedded=true&url=www.wowcafe.com/menus/monterey_bay_9.7.16.pdf");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
            else{
                displaySnackbar();
            }
        }
        else if (id == R.id.close) {
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    public void onMapYourRoute(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        mBuilder.setTitle("Which building do you need to go?");

        final Spinner mSpinner = (Spinner) mView.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item,getResources()
                .getStringArray(R.array.buildings));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        mBuilder.setPositiveButton("Get Directions", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!mSpinner.getSelectedItem().toString().
                        equalsIgnoreCase("Please choose a building…")){
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?daddr=" +
                                    buildingMap.get(mSpinner.getSelectedItem().toString())));
                    startActivity(intent);

                    dialog.dismiss();
                }
            }
        });
        mBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
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

    /**
     * Method to get all userdata from SharedPreferences.
     * This data is instantiated during the LoginActivity and is also referenced
     * during the SplashScreen Activity
     */
    public void gatherUserInfoFromSharedPreferences() {
        sharedPrefs = getSharedPreferences("MontePrefs",Context.MODE_PRIVATE);
        userFName = sharedPrefs.getString("First Name", "Monte");
        //SharedPreferences retrieval takes Key and DefaultValue as parameters
        userLName = sharedPrefs.getString("Last Name", "Otter");
        userEmail = sharedPrefs.getString("Email", "monte@ottermail.com");
        userID = sharedPrefs.getString("ID", "12345");
        Log.d("SharedPrefs", "!!!!email at MainActivity " + userEmail + " !!!!");
        Log.d("SharedPrefs", "!!!!userID at MainActivity " + userID + " !!!!");
        SESSION_ID = sharedPrefs.getString("SessionKey", "sessionkeyerror");
    }
    public boolean hasInternetConnection(){

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();

    }

    public void displaySnackbar(){
        Snackbar.make(findViewById(android.R.id.content),
                "No internet connection",
                Snackbar.LENGTH_LONG).show();
    }

}