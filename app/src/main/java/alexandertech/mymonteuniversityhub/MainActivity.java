package alexandertech.mymonteuniversityhub;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private DrawerLayout drawerLayout;
    private String[] pageTitle = {"myPlanner", "News", "Parking"};
    private String studyRooms = "http://library2.csumb.edu/mrbs/mobilenow.php";
    public static String MYPREFERENCE = "myPref";
    private String food = "https://csumb.sodexomyway.com/smgmenu/display/csu-monterey%20bay%20dining%20common%20-%20resident%20dining";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
            tabLayout.addTab(tabLayout.newTab().setText(pageTitle[i]));
        }

        //set gravity for tab bar
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //handling navigation view item event
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        viewPager = (ViewPager)findViewById(R.id.view_pager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), MainActivity.this);
        viewPager.setAdapter(pagerAdapter);


        //setting Tab layout (number of Tabs = number of ViewPager pages)
        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Fragment frag = pagerAdapter.fragments[position];
                if(frag != null && frag instanceof MapsFragment){
                    Toast.makeText(MainActivity.this, "On page Selected", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
       // viewPager.setOffscreenPageLimit(3);
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

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
                    return new myPlanner();
                case 1:
                    return new News();
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

    @Override
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



    /*
    Method for the navigation Drawer that takes in the id from the navigation drawer
    and based on the view, an action will be performed.
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.DinningCommonsItem) {
           // viewPager.setCurrentItem(0);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Title here");

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
            //Menu item for dining commons
            //Refer to variable food to change it's refrence
            Uri uri = Uri.parse(food);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            viewPager.setCurrentItem(0);
        } else if (id == R.id.LibraryStudyRooms) {
            Uri uri = Uri.parse(studyRooms);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
           // viewPager.setCurrentItem(1);
        } else if (id == R.id.MapYourRoute) {
            viewPager.setCurrentItem(2);
        } else if (id == R.id.CampusPD) {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "18316550268"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);

        } else if (id == R.id.close) {
            finish();
        }
        else if (id == R.id.Login){
            Intent intent = new Intent(MainActivity.this, oktaLogin.class);
            startActivity(intent);
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