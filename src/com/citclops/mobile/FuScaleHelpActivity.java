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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


public class FuScaleHelpActivity extends FragmentActivity{

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
        setContentView(R.layout.activity_fuscale_help_collection);

        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        // 
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mViewPager = (ViewPager) findViewById(R.id.fuscale_help_collection_pager);
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
           return 8;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	String retorn ="";
        	switch(position){
        	case 0:
        		retorn = ctx.getString(R.string.help_0_title);
        		break;
        	case 1:
        		retorn = ctx.getString(R.string.help_1_title);
        		break;
        	case 2:
        		retorn = ctx.getString(R.string.help_2_title);
        		break;
        	case 3:
        		retorn = ctx.getString(R.string.help_3_title);
        		break;
        	case 4:
        		retorn = ctx.getString(R.string.help_4_title);
        		break;
        	case 5:
        		retorn = ctx.getString(R.string.help_5_title);
        		break;
        	case 6:
        		retorn = ctx.getString(R.string.help_6_title);
        		break;
        	case 7:
        		retorn = ctx.getString(R.string.help_7_title);
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
        		rootView = inflater.inflate(R.layout.activity_fuscale_help_index, container, false);
        		TextView txtPrgrf1 = (TextView)rootView.findViewById(R.id.fuscale_help_0_pgrf1);
        		TextView txtPrgrf2 = (TextView)rootView.findViewById(R.id.fuscale_help_0_pgrf2);
        		TextView txtPrgrf3 = (TextView)rootView.findViewById(R.id.fuscale_help_0_pgrf3);
        		TextView txtPrgrf4 = (TextView)rootView.findViewById(R.id.fuscale_help_0_pgrf4);
        		TextView txtPrgrf5 = (TextView)rootView.findViewById(R.id.fuscale_help_0_pgrf5);
        		TextView txtPrgrf6 = (TextView)rootView.findViewById(R.id.fuscale_help_0_pgrf6);
        		TextView txtPrgrf7 = (TextView)rootView.findViewById(R.id.fuscale_help_0_pgrf7);
        		txtPrgrf1.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(1);}});
        		txtPrgrf2.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(2);}});
        		txtPrgrf3.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(3);}});
        		txtPrgrf4.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(4);}});
        		txtPrgrf5.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(5);}});
        		txtPrgrf6.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(6);}});
        		txtPrgrf7.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(7);}});
        		break;
        	case 1:
        		rootView = inflater.inflate(R.layout.activity_fuscale_help_1, container, false);
        		TextView fuscale_help_1_back = (TextView)rootView.findViewById(R.id.fuscale_help_1_back);
        		fuscale_help_1_back.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(0);}});
        		break;
        	case 2:
        		rootView = inflater.inflate(R.layout.activity_fuscale_help_2, container, false);
        		TextView fuscale_help_2_back = (TextView)rootView.findViewById(R.id.fuscale_help_2_back);
        		fuscale_help_2_back.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(0);}});
        		break;
        	case 3:
        		rootView = inflater.inflate(R.layout.activity_fuscale_help_3, container, false);
        		TextView fuscale_help_3_back = (TextView)rootView.findViewById(R.id.fuscale_help_3_back);
        		fuscale_help_3_back.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(0);}});
        		break;
        	case 4:
        		rootView = inflater.inflate(R.layout.activity_fuscale_help_4, container, false);
        		TextView fuscale_help_4_back = (TextView)rootView.findViewById(R.id.fuscale_help_4_back);
        		fuscale_help_4_back.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(0);}});
        		break;
        	case 5:
        		rootView = inflater.inflate(R.layout.activity_fuscale_help_5, container, false);
        		TextView fuscale_help_5_back = (TextView)rootView.findViewById(R.id.fuscale_help_5_back);
        		fuscale_help_5_back.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(0);}});
        		break;
        	case 6:
        		rootView = inflater.inflate(R.layout.activity_fuscale_help_6, container, false);
        		TextView fuscale_help_6_back = (TextView)rootView.findViewById(R.id.fuscale_help_6_back);
        		fuscale_help_6_back.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(0);}});
        		break;
        	case 7:
        		rootView = inflater.inflate(R.layout.activity_fuscale_help_7, container, false);
        		TextView fuscale_help_7_back = (TextView)rootView.findViewById(R.id.fuscale_help_7_back);
        		fuscale_help_7_back.setOnClickListener(new OnClickListener(){@Override public void onClick(View v) {localCopyViewPager.setCurrentItem(0);}});
        		break;
        	}
            return rootView;
        }
    }
	
}
