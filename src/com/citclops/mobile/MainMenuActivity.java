package com.citclops.mobile;

import com.citclops.models.SettingsData;
import com.citclops.util.AnimationUtils;
import com.citclops.util.LocationUtils;
import com.citclops.util.MessageDialog;
import com.citclops.util.SendFUDataServiceManager;
import com.citclops.widgets.TileMenuWidget;
import com.citclops.widgets.TileMenuWidget.OnClickInterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenuActivity extends Activity {
	// Local Copy
	Context _this;															// Context

	private static boolean bFirstExecution = true;							// Necessari to execute codi only on first execution

	private TileMenuWidget tileMenuFuScale = null;							// Tile Of Menu
	private TileMenuWidget tileMenuCitclopsData = null;						// Tile Of Citclops Data
	private TileMenuWidget tileMenuPreferences = null;						// Tile Of Preferences
	private TileMenuWidget tileMenuHelp = null;								// Tile Of Help
	
	private int LOCATION_REQUEST_CODE_FUSCALE = 1;							// Request code for Activity Result
	
	private TextView lblVersion;											// TextView of Label Version
	private FrameLayout frmLayoutAbout;										// Layout of About App
	private FrameLayout frmLayoutROOT;										// Layout Root of Main Menu

	// A nimations to show About Layout
	private Animation inFromRightAnimationAbout = AnimationUtils.inFromRightAnimation(250);
	private Animation outToLeftAnimationAbout = AnimationUtils.outToLeftAnimation(250);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try{
			// Show Working Dialog
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main_menu);

	        // Init creation var
	        bFirstExecution = true;

			// Local Copy
			_this = this;

			// Get Screen Metrics
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			
			// Get Reference to TextView of Version
			try{
				lblVersion = (TextView)findViewById(R.idAbout.lblVersion);
				lblVersion.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
			} catch (Exception e){
				// Error in get vesion number. Don't show it			
			}
			
			// Reference to Layout of About and ROOT
			frmLayoutROOT = (FrameLayout)findViewById(R.idMainMenu.frmLayoutROOT);
			frmLayoutAbout = (FrameLayout)findViewById(R.idAbout.frmLayoutAbout);
			frmLayoutAbout.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					// Amaguem el About
					hideAbout();
					return false;
				}				
			});
			
			// Get Widget References Tile Fu Scale			
			tileMenuFuScale = (TileMenuWidget)findViewById(R.idMenu.tileMenuFuScale);
			tileMenuFuScale.setReverseText(getString(R.string.menu_activity_fuscale));			
			tileMenuFuScale.setBackGroundImage(BitmapFactory.decodeResource(getResources(), R.drawable.fuscale_icon));
			tileMenuFuScale.setIntervalMilliseconds(2500);
			tileMenuFuScale.setReversedBackgroundColor(Color.argb(60, 00, 00, 00));
			tileMenuFuScale.setReversedForegroundColor(Color.argb(255, 255, 255, 255));
			tileMenuFuScale.setReversedTextSize(10 * metrics.scaledDensity);
			tileMenuFuScale.setOnClick(new OnClickInterface(){
				@Override
				public void OnClick(View tile) {
					startFuScaleActivityExt();
				}				
			});
			
			// Get Widget References Tile Citclops Data			
			tileMenuCitclopsData = (TileMenuWidget)findViewById(R.idMenu.tileMenuCitclopsData);
			tileMenuCitclopsData.setReverseText(getString(R.string.menu_activity_citlops_data));			
			tileMenuCitclopsData.setBackGroundImage(BitmapFactory.decodeResource(getResources(), R.drawable.citclopsdata_icon));
			tileMenuCitclopsData.setIntervalMilliseconds(3000);
			tileMenuCitclopsData.setReversedBackgroundColor(Color.argb(60, 00, 00, 00));
			tileMenuCitclopsData.setReversedForegroundColor(Color.argb(255, 255, 255, 255));
			tileMenuCitclopsData.setReversedTextSize(12 * metrics.scaledDensity);
			tileMenuCitclopsData.setAlpha(0.4f);
			tileMenuCitclopsData.setEnabled(false);
			
			// Get Widget References Tile Citclops Buoy			
			tileMenuPreferences = (TileMenuWidget)findViewById(R.idMenu.tileMenuPreferences);
			tileMenuPreferences.setReverseText(getString(R.string.menu_activity_preferences));			
			tileMenuPreferences.setBackGroundImage(BitmapFactory.decodeResource(getResources(), R.drawable.preferences_icon));
			tileMenuPreferences.setIntervalMilliseconds(4000);
			tileMenuPreferences.setReversedBackgroundColor(Color.argb(60, 00, 00, 00));
			tileMenuPreferences.setReversedForegroundColor(Color.argb(255, 255, 255, 255));
			tileMenuPreferences.setReversedTextSize(11 * metrics.scaledDensity);
			tileMenuPreferences.setOnClick(new OnClickInterface(){
				@Override
				public void OnClick(View tile) {
					Intent intent = new Intent(_this, MainPreferencesActivity.class);
					startActivity(intent);
				}				
			});
			
			// Get Widget References Tile Citclops Help			
			tileMenuHelp = (TileMenuWidget)findViewById(R.idMenu.tileMenuHelp);
			tileMenuHelp.setReverseText(getString(R.string.menu_activity_help));			
			tileMenuHelp.setBackGroundImage(BitmapFactory.decodeResource(getResources(), R.drawable.help_icon));
			tileMenuHelp.setIntervalMilliseconds(4000);
			tileMenuHelp.setReversedBackgroundColor(Color.argb(60, 00, 00, 00));
			tileMenuHelp.setReversedForegroundColor(Color.argb(255, 255, 255, 255));
			tileMenuHelp.setReversedTextSize(11 * metrics.scaledDensity);
			tileMenuHelp.setOnClick(new OnClickInterface(){
				@Override
				public void OnClick(View tile) {
					Intent intent = new Intent(_this, FuScaleHelpActivity.class);
					startActivity(intent);
				}				
			});
		} catch (Exception e) {
			// Error in Load Activity
			Toast.makeText(_this, getString(R.string.menu_activity_error_load_gen) + e.getMessage(), Toast.LENGTH_LONG).show();
		}finally{

		}
	}		
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			// If keyBack is Pressed
			// 1-. If About is showing, hide the panel
			// 2-. If not, return to menu Activity
			if (frmLayoutAbout.getVisibility() == FrameLayout.VISIBLE ){
				hideAbout();
			} else {
				// Return to Menu Activity
				finish();			
			}
		}
	    return false;
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		if (bFirstExecution){
			// To execute only on first execution
			bFirstExecution = false;

			// Instance profiles object
			final SettingsData settingsData = new SettingsData(this);
			settingsData.LoadData();
			    		
    		// Select profile if is not set
    		if (!settingsData.IsProfileInformed()){    			
    			// Accept Profile Selection
        		DialogInterface.OnClickListener listenerOK = new DialogInterface.OnClickListener() {
    	    	    public void onClick(DialogInterface dialog, int which) {
    					if (MessageDialog.bMessagedActivated){
    						// Disable Message
    						MessageDialog.bMessagedActivated = false;
    						
    						// Save the profile selected
    						settingsData.setProfileByIndex(MessageDialog.SelectedIndex);
    					}
    	    	    }
        		};    			
    			// Show profiles menu
    			MessageDialog.InputSelectorDialog(
    					_this, 
    					getResources().getText(R.string.menu_activity_profile_title).toString(), 
    					getResources().getText(R.string.menu_activity_profile_msg_ok).toString(), 
    					null, 
    					listenerOK, 
    					null, 
    					settingsData.getDescriptionProfiles());    			
    		} else {
    			if (!settingsData.getDataUploadPreference()){
        			// Warning about Data Upload Off
    				MessageDialog.ShowDialog(_this, getResources().getText(R.string.app_name).toString(), getString(R.string.main_dataupload_off), "OK", null, true);
    			}
    		}
		}
		
		
		// Starts sending information
		SendFUDataServiceManager.StartSend(MainMenuActivity.this, (Activity)_this);

		// Start the tiles		
		if (tileMenuFuScale.isEnabled())		tileMenuFuScale.start();
		if (tileMenuCitclopsData.isEnabled())	tileMenuCitclopsData.start();
		if (tileMenuPreferences.isEnabled())	tileMenuPreferences.start();
		if (tileMenuHelp.isEnabled())			tileMenuHelp.start();	
	}
	@Override
	protected void onPause() {
		super.onPause();
		if (tileMenuFuScale.isEnabled())		tileMenuFuScale.stop();
		if (tileMenuCitclopsData.isEnabled())	tileMenuCitclopsData.stop();
		if (tileMenuPreferences.isEnabled())	tileMenuPreferences.stop();
		if (tileMenuHelp.isEnabled())			tileMenuHelp.stop();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.idMenuMain.MenuAbout:
			showAbout();
			break;
		}
		return true;
	} 		
	private void showAbout(){
		frmLayoutROOT.setEnabled(false);
		tileMenuFuScale.setClickable(false);
		tileMenuCitclopsData.setClickable(false);
		tileMenuPreferences.setClickable(false);
		tileMenuHelp.setClickable(false);
		frmLayoutAbout.setVisibility(FrameLayout.VISIBLE);		
		frmLayoutAbout.startAnimation(inFromRightAnimationAbout);
	}
	private void hideAbout(){
		frmLayoutROOT.setEnabled(true);
		tileMenuFuScale.setClickable(true);
		tileMenuCitclopsData.setClickable(true);
		tileMenuPreferences.setClickable(true);
		tileMenuHelp.setClickable(true);
		frmLayoutAbout.setVisibility(FrameLayout.GONE);
		frmLayoutAbout.startAnimation(outToLeftAnimationAbout);
	}	
	

	private void startFuScaleActivityExt(){
		LocationUtils mLocationUtils = null;											// GPS Positioning
		try{
			// Start Listening
   			mLocationUtils = new LocationUtils(_this);
			mLocationUtils.StartListen();			
			
			// Check for location services
			if (mLocationUtils.isLocationEnabled()){
				// Load FuScale Actvity
				Intent intent = new Intent(_this, FUScaleActivityExt.class);
				startActivity(intent);
			} else {
				 AlertDialog.Builder builder = new AlertDialog.Builder(_this);
			     builder.setTitle(com.citclops.mobile.R.string.main_gps_not_found_title);  // GPS not found
			     builder.setMessage(com.citclops.mobile.R.string.main_gps_not_found_message); // Want to enable?
			     builder.setPositiveButton(com.citclops.mobile.R.string.main_gps_not_found_Yes, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialogInterface, int i) {
		            	((Activity)_this).startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_REQUEST_CODE_FUSCALE);
		            }
			     });
			     builder.setNegativeButton(com.citclops.mobile.R.string.main_gps_not_found_No, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialogInterface, int i) {
			            	MessageDialog.ShowDialog(_this, getResources().getText(R.string.app_name).toString(), getString(R.string.main_gps_not_found_Mandatory), "OK", null, true);
			            }
				     });
			     builder.create().show();
			}
		} catch (Exception e){
			// Error in Load Activity
			Toast.makeText(_this, getString(R.string.menu_activity_error_load_gen) + e.getMessage(), Toast.LENGTH_LONG).show();
		} finally{
			// Stop Listening Location Changes
			if (mLocationUtils != null) mLocationUtils.StopListen();
			mLocationUtils = null;
		}	
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == LOCATION_REQUEST_CODE_FUSCALE){
			startFuScaleActivityExt();
		}
	}
}
