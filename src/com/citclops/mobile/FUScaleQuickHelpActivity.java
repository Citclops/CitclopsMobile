package com.citclops.mobile;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class FUScaleQuickHelpActivity extends FragmentActivity{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments representing
     * each object in a collection. We use a {@link android.support.v4.app.FragmentStatePagerAdapter}
     * derivative, which will destroy and re-create fragments as needed, saving and restoring their
     * state in the process. This is important to conserve memory and is a best practice when
     * allowing navigation between objects in a potentially large collection.
     */
	HelpPagesCollectionPagerAdapter mHelpPagesCollectionPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will display the object collection.
     */
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuscale_quick_help_collection);
        
        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        // 
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mViewPager = (ViewPager) findViewById(R.id.fuscale_quick_help_collection_pager);
        mHelpPagesCollectionPagerAdapter = new HelpPagesCollectionPagerAdapter(getSupportFragmentManager(),this, mViewPager);

        // Set up action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager, attaching the adapter.
        mViewPager.setAdapter(mHelpPagesCollectionPagerAdapter);        
    }

    @SuppressWarnings("deprecation")
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, MainMenuActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class HelpPagesCollectionPagerAdapter extends FragmentStatePagerAdapter {
    	Context ctx = null;
    	ViewPager localViewPager;
        public HelpPagesCollectionPagerAdapter(FragmentManager fm, Context context, ViewPager pViewPager) {
            super(fm);
            ctx = context;
            localViewPager = pViewPager;
        }
        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new HelpPageObjectFragment();
            ((HelpPageObjectFragment)fragment).localCopyViewPager = localViewPager;
            Bundle args = new Bundle();
            args.putInt(HelpPageObjectFragment.ARG_OBJECT, i); // Our object is just an integer :-P
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
           return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	String retorn ="";
        	switch(position){
	        	case 0:
	        		retorn = ctx.getString(R.string.quick_help_0_title);
	        		break;
	        	case 1:
	        		retorn = ctx.getString(R.string.quick_help_1_title);
	        		break;
	        	case 2:
	        		retorn = ctx.getString(R.string.quick_help_2_title);
	        		break;
        	}
            return retorn;
            
        }
    }
    /**
     * A class for display one page of the help
     */
    public static class HelpPageObjectFragment extends Fragment {

        public static final String ARG_OBJECT = "object";
        public ViewPager localCopyViewPager = null;
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	Bundle args = getArguments();
        	int position = args.getInt(ARG_OBJECT);
        	View rootView = null;
        	switch(position){
	        	case 0:
	        		rootView = inflater.inflate(R.layout.activity_fuscale_quick_help_1, container, false);
	        		break;
	        	case 1:
	        		rootView = inflater.inflate(R.layout.activity_fuscale_quick_help_2, container, false);
	        		break;
	        	case 2:
	        		rootView = inflater.inflate(R.layout.activity_fuscale_quick_help_3, container, false);
	        		break;        	
        	}
            return rootView;
        }
    }
}
