package get.saga;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;

import get.saga.ui.SlidingTabLayout;


public class MainActivity extends ActionBarActivity {

    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ViewPager pager;
        final ViewPagerAdapter adapter;
        SlidingTabLayout tabs;

        File folder = new File(Environment.getExternalStorageDirectory() + "/Saga");
        if (!folder.exists()) {
            folder.mkdir();
        }

        try{
            // Get tracker.
            Tracker t = ((ApplicationWrapper) getApplication()).getTracker(
                    ApplicationWrapper.TrackerName.APP_TRACKER);
            // Set screen name.
            t.setScreenName("MainActivity");
            // Send a screen view.
            t.send(new HitBuilders.ScreenViewBuilder().build());
        }catch(Exception e){
            //just as a protective measure
        }


        setContentView(R.layout.activity_main);
        String title = "";
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mToolbar != null) {
                setSupportActionBar(mToolbar);
                getSupportActionBar().setTitle(title);
                mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
                //getSupportActionBar().setDisplayShowTitleEnabled(false);
                //ImageView toolbarImageView = (ImageView) mToolbar.findViewById(R.id.timageview);
                //TextView toolbarTextView = (TextView) mToolbar.findViewById(R.id.ttextview);
                //toolbarImageView.setImageResource(R.drawable.untitled);
                //toolbarTextView.setText("Saga");
            }
        }

        adapter =  new ViewPagerAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorAccent);
            }
        });
        tabs.setViewPager(pager);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_feedback) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"prempal.42@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Saga Feedback");
            try {
                i.putExtra(Intent.EXTRA_TEXT   , "Model - " + Build.MODEL + "\nAndroid Version - " + Build.VERSION.RELEASE
                        + "\nApp Version - " + getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName + "\n_ _ _ _ _ _ _ _ _ _ _ _ _\n");
            }
            catch(PackageManager.NameNotFoundException e){
                e.printStackTrace();
            }

            try {
                startActivity(Intent.createChooser(i, "Choose email client..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
        else if(id == R.id.action_invite) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, "Hey! Check out this amazing app - Saga. \nhttp://getsa.ga/apk ");
            try {
                startActivity(Intent.createChooser(i, "Choose..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No application available to invite friends.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        CharSequence TAB_TITLES[]={"DOWNLOAD","LIBRARY"};
        int NUM_TAB =2;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if(position == 0)
            {
                DownloadFragment dF = new DownloadFragment();
                return dF;
            }
            else
            {
                LibraryFragment lF = new LibraryFragment();
                return lF;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_TITLES[position];
        }

        @Override
        public int getCount() {
            return NUM_TAB;
        }
    }

}
