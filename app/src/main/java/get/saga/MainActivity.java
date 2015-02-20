package get.saga;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import get.saga.ui.SlidingTabLayout;


public class MainActivity extends ActionBarActivity {

    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ViewPager pager;
        ViewPagerAdapter adapter;
        SlidingTabLayout tabs;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String title = "Saga - Free Music";
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
        if (id == R.id.action_settings) {
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
