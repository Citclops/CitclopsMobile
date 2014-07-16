package com.citclops.mobile;

import com.citclops.models.SettingsData;
import com.citclops.util.MessageDialog;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;

public class MainPreferencesActivity extends Activity {

	// Control first run of Activity
	Context _this;											// Local Context
	boolean firstExecution = true;							// Variable First Run
	Spinner spnProfiles;									// Selector of Profiles
	ToggleButton toggleButtonSecchiDisk;					// Button for Ask Secchi Disk
	ToggleButton toggleButtonPlasticScale;					// Button for Ask Plastic Fu Scale	
	ToggleButton toggleButtonDataUpload;					// Button for Data Upload	
	EditText txtMailSurvey;									// Text To enter the email for the Survey
	int indexProfileSelected = -1;							// Saved Selected Index on Enter Activity
	boolean bAskSecchiDiskSelected;							// Saved Ask Secchi Disk on Enter Activity
	boolean bAskPlasticScaleSelected;						// Saved Ask Plastic Scale on Enter Activity
	boolean bDataUploadSelected;							// Saved Data Upload
	String mailSurveySelected = "";							// Email Survey on Enter Activity
	SettingsData settingsData;								// Data Settings
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_preferences);
		
		// Local values
		_this = this;
		
		// Get Widget References
		spnProfiles = (Spinner)findViewById(R.idMainPreferences.spnProfiles);
		toggleButtonSecchiDisk = (ToggleButton)findViewById(R.idMainPreferences.toggleButtonSecchiDisk);
		toggleButtonPlasticScale = (ToggleButton)findViewById(R.idMainPreferences.toggleButtonPlasticScale);
		toggleButtonDataUpload = (ToggleButton)findViewById(R.idMainPreferences.toggleButtonUploadInfo);
		txtMailSurvey = (EditText)findViewById(R.idMainPreferences.txtMailSurvey);
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		if (firstExecution){
			firstExecution = false;
			
			// Load Data
			settingsData = new SettingsData(this);
			settingsData.LoadData();
			
			// Assign profiles
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, settingsData.getDescriptionProfiles());		
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    spnProfiles.setAdapter(adapter);

		    // Select active profile
    		if (settingsData.IsProfileInformed()){    		
    			// Get Profile
    			indexProfileSelected = settingsData.getSelectedProfile();
    			spnProfiles.setSelection(indexProfileSelected);
    		}		
    		
    		// Select values for the preferences questions
    		bAskSecchiDiskSelected = settingsData.getAskSecchiDiskPreference();
    		bAskPlasticScaleSelected = settingsData.getAskPlasticScalePreference();
    		bDataUploadSelected = settingsData.getDataUploadPreference();
    		mailSurveySelected = settingsData.getMailAddressSurvey().trim();
    		toggleButtonSecchiDisk.setChecked(bAskSecchiDiskSelected);
    		toggleButtonPlasticScale.setChecked(bAskPlasticScaleSelected);
    		toggleButtonDataUpload.setChecked(bDataUploadSelected);
    		
    		txtMailSurvey.setText(mailSurveySelected);
		}
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);		
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if ((spnProfiles.getSelectedItemPosition() != indexProfileSelected) ||
		    	(bAskSecchiDiskSelected != toggleButtonSecchiDisk.isChecked()) ||		    	
		    	(bDataUploadSelected != toggleButtonDataUpload.isChecked()) ||
		    	(!mailSurveySelected.equals(txtMailSurvey.getText().toString().trim())) ||
		    	(bAskPlasticScaleSelected != toggleButtonPlasticScale.isChecked())) {
				// Value of profile has changed. Ask for save values
				DialogInterface.OnClickListener listenerAsk_response_Yes = new DialogInterface.OnClickListener() {				
    				public void onClick(DialogInterface dialog, int which) {
    					if (MessageDialog.bMessagedActivated){
    						MessageDialog.bMessagedActivated = false;    						
    						// Save the changes
    						settingsData.setProfileByIndex(spnProfiles.getSelectedItemPosition());
    						settingsData.setAskQuestionsPreferences(toggleButtonSecchiDisk.isChecked(), toggleButtonPlasticScale.isChecked());
    						settingsData.setCommPreferences(toggleButtonDataUpload.isChecked());    						
    						// Exit Activity
    						finish();    						
    					}
    				}
    			};
    			DialogInterface.OnClickListener listenerAsk_response_No = new DialogInterface.OnClickListener() {				
    				public void onClick(DialogInterface dialog, int which) {
    					if (MessageDialog.bMessagedActivated){
    						MessageDialog.bMessagedActivated = false;
    						
    						// Exit Activity
    						finish();    						
    					}
    				}
    			};
				MessageDialog.ShowDialogYesNo(
		   				_this, 
		   				getResources().getText(R.string.app_name).toString(),
		   				getResources().getText(R.string.main_preferences_exit_ask_save_changes).toString(),
		   				getResources().getText(R.string.main_preferences_exit_save_yes).toString(),
		   				getResources().getText(R.string.main_preferences_exit_save_no).toString(),
		   				listenerAsk_response_Yes, 
		   				listenerAsk_response_No, 
		   				false);
			} else {
				// Exit Activity
				finish();    													
			}
		} 
	    return false;
	}
}
