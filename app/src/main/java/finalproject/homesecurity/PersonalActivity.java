package finalproject.homesecurity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

public class PersonalActivity extends AppCompatActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TextView toolbarTitle,signedInAs;
    private ViewPager mViewPager;
    private SharedPreferences settings,sharedPreferences;
    private android.app.FragmentManager fragmentManager;
    private CommandControlsFragment frag;
    private PlayVideoFragment videoFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_personal_toolbar);
        setSupportActionBar(toolbar);

        signedInAs = (TextView) toolbar.findViewById(R.id.toolbar_text);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        settings = getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("PhoneMode", Context.MODE_PRIVATE);
        signedInAs.setText(settings.getString("userId","Email"));
        // Create the adapter that will return a fragment for each of the
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_personal, menu);
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

    @Override
    public void onBackPressed() {
        frag = (CommandControlsFragment) getFragmentManager().findFragmentByTag("frag");
        videoFrag = (PlayVideoFragment) getFragmentManager().findFragmentByTag("video_frag");
        if(frag != null && frag.isVisible()) //remove the fragment and make the listview visible again
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(frag);
            fragmentTransaction.commit();
            toolbarTitle.setText(R.string.app_name);
            SecurityFragment.listView.setVisibility(View.VISIBLE);
        }
        else if(videoFrag != null && videoFrag.isVisible()) //remove the fragment and make the listview visible again
        {
            fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(videoFrag);
            fragmentTransaction.commit();
        }
        else //display message asking the user if they want to leave personal device mode
        {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.leavePersonalDevice)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            PersonalActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit().putString("DeviceMode", null).apply();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new SecurityFragment();
                case 1:
                    return new VideoFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECURITY";
                case 1:
                    return "VIDEOS";
            }
            return null;
        }
    }
}
