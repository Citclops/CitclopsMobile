package com.citclops.mobile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.citclops.cameralibrary.CameraCallback;
import com.citclops.cameralibrary.CitclopsCameraLibrary;
import com.citclops.cameralibrary.CameraConstants.FOCUS_MODE;
import com.citclops.cameralibrary.CameraConstants.SCENE;
import com.citclops.cameralibrary.CameraConstants.WHITE_BALANCE;
import com.citclops.models.FUBarValues;
import com.citclops.models.FUScaleMetadata;
import com.citclops.models.SettingsData;
import com.citclops.sun.AzimuthZenithAngle;
import com.citclops.sun.PSA;
import com.citclops.util.BrightnessUtility;
import com.citclops.util.DataConstants;
import com.citclops.util.DispositiuFisic;
import com.citclops.util.FUScaleSensors;
import com.citclops.util.FileUtils;
import com.citclops.util.LocationUtils;
import com.citclops.util.MessageDialog;
import com.citclops.util.SendFUDataServiceManager;
import com.citclops.util.ZipUtility;
import com.citclops.widgets.CustomBorderDrawable;
import com.citclops.widgets.NumericKeyPadWidget;
import com.citclops.widgets.kImageButton;
import com.citclops.widgets.kTransparentWorkingDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class FUScaleActivityExt extends Activity {
	
	/* - - - - -  C o n s t a n t s  - - - - */
	private final int METERS_ACCURACY_GOOD_POSITION = 300;					// Accuracy meters to consider good a position 	
	private final float QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED = 0.30f;		// Opacity for the images that are not answered
	
	/* - - - - -  E n u m s  - - - - - - - - */
	private enum ACTIVITY_STATE {											// Activity States
		LOCATION_NOT_ENABLED,
		SECCHI_DISK_ASK, 
		SECCHI_DISK_DEPTH, 
//		SUN_POSITION, 
//		SHOW_PANEL_SUN,
		TAKE_PHOTO, 
		SELECT_RECT, 
		SELECT_FUSCALE_VALUE, 
		PLASTIC_FUSCALE_ASK, 
		PLASTIC_FUSCALE_VALUE, 
		QUESTIONARY,
		APP_SURVEY_ASK,
		APP_SURVEY,
		FEEDBACK};		

	private enum CLOUD_FRACTION {											// Cloud Fraction
		NOT_DEFINED, 
		CLEAR, 
		FEW, 
		SCATERED,
		BROKEN, 
		OVERCAST};		

	private enum RAINING {													// Raining
		NOT_DEFINED, 
		YES, 
		NO};
		
	private enum SEE_BOTTOM {												// See the bottom
		NOT_DEFINED, 
		YES, 
		NO};		
		
	Context _this;															// Actual Context Reference
	boolean firstExecution = true;											// Variable First Run
	DisplayMetrics metrics;													// Metrics for screen density

	ACTIVITY_STATE activityState = ACTIVITY_STATE.SECCHI_DISK_ASK;			// Activity Status
	CLOUD_FRACTION cloudFractionValue = CLOUD_FRACTION.NOT_DEFINED;			// Cloud Fraction
	RAINING rainingValue = RAINING.NOT_DEFINED;								// Raining
	SEE_BOTTOM seeBottomValue = SEE_BOTTOM.NOT_DEFINED;						// See Bottom
			
	FUBarValues dataFuScaleBars;											// Values of FU Scale Bars
	Integer FUValue = null;													// Value of Bar Selected								
	FUScaleMetadata metadata;												// Class to obtain and save data

	private kTransparentWorkingDialog workingDialog;						// Working Dialog
	BrightnessUtility brightnessUtility = null;								// Brightness Utility

	private List<WHITE_BALANCE> supportedWhiteBalances;						// Camera Parameters White Balances
	private String [] supportedStringWhiteBalance;							// Camera Parameters White Balances
	private List<FOCUS_MODE> supportedFocusMode;							// Camera Parameters Focus Mode
	private String [] supportedStringFocusMode;								// Camera Parameters Focus Mode
	private List<SCENE> supportedScene;										// Camera Parameters Suported Scene
	private String [] supportedStringScene;									// Camera Parameters Suported Scene

	FUScaleSensors fuScaleSensors = null;									// Sensors to get device orientation
	LocationUtils mLocationUtils;											// GPS Positioning
	double bestLatLocation = Double.MIN_VALUE;								// Best Location Latitude
	double bestLongLocation = Double.MIN_VALUE;								// Best Location Longitude
	private ProgressDialog progressPositionDialog;							// A ProgressDialog object while Locations is Updated
	
	private boolean bLocationCanceled = false;								// To get if user has cancelled Location and Sun position
	
	private static final int PERIOD = 2000;									// Reaction time to double click go back
	private long lastBackPressedTime;										// Last touch to control double go back

	CitclopsCameraLibrary cameraLib = null;									// Library of the camera

	FrameLayout fuScalePanelArrow;											// Panel Of Point the Sun
	FrameLayout previewContainer;											// Container preview of the camera
	kImageButton btQuickHelp;												// Quick Help Button
	TextView txtPitchValue;													// Value of pitch position
	kImageButton btObjetivo;												// Button to Take Photo
	LinearLayout containerTakePhotoWidgets;									// Linear Layout Container take Photo
	LinearLayout containerPitchWidgets;										// Linear Layout Container Pitch widgets
	TextView txtPreviewInstructionsTop;										// Text of the top instructions in preview Panel
	FrameLayout containerWorkPhoto;											// Container to work with taken photo
	LinearLayout containerWorkPhotoSquare;									// Container of square selection	
	ImageView imgWorkPhoto;													// View to show taken photo
	Bitmap bmpPhotoTaken;													// Bitmap of photo taken
	Bitmap bmpCroppedPhoto;													// Bitmap of cropped photo	
	FrameLayout fuscaleSquare11;											// FrameLayout Square Selection Position 1, 1
	FrameLayout fuscaleSquare12;											// FrameLayout Square Selection Position 1, 2
	FrameLayout fuscaleSquare13;											// FrameLayout Square Selection Position 1, 3
	FrameLayout fuscaleSquare21;											// FrameLayout Square Selection Position 2, 1
	FrameLayout fuscaleSquare22;											// FrameLayout Square Selection Position 2, 2
	FrameLayout fuscaleSquare23;											// FrameLayout Square Selection Position 2, 3
	FrameLayout fuscaleSquare31;											// FrameLayout Square Selection Position 3, 1
	FrameLayout fuscaleSquare32;											// FrameLayout Square Selection Position 3, 2
	FrameLayout fuscaleSquare33;											// FrameLayout Square Selection Position 3, 3
	TextView fuscaleSquareInstructions;										// Text of the Square Panel Instructions
	kImageButton btSelectSquareOK;											// Button to select square photo zone
	FrameLayout containerSelectValue;										// Container for select FU Value
	ImageView imgSelectValue;												// Cropped Image to Select Fu Value 
	LinearLayout panelFuScaleContainer;										// FuScale container panel
	RelativeLayout marcoFuScaleContainer;									// FU Scale Container
	SeekBar marcoFuScaleSeekBar;											// SeekBar of FU Bar
    FrameLayout fuscaleConatinerSelectFuvalueText;							// FrameLayout Container Select FuValue Instructions    
	TextView fuscaleSelectValueInstructions;								// Text of the Select FU Value Instructions
	kImageButton btSelectFUValueOK;											// Button to select FU Value 
	ScrollView containerQuestions;											// Container of questions panel
	LinearLayout panelQuestionCloudClear;									// Cloud Clear Container
	LinearLayout panelQuestionCloudFew;										// Cloud Few Container
	LinearLayout panelQuestionCloudScattered;								// Cloud Scattered Container
	LinearLayout panelQuestionCloudBroken;									// Cloud Broken Container
	LinearLayout panelQuestionCloudOvercast;								// Cloud Overcast Container	
	LinearLayout panelQuestionRainingYes;									// Raining Yes Container
	LinearLayout panelQuestionRainingNo;									// Raining No Container
	LinearLayout panelQuestionBottomYes;									// Bottom Yes Container
	LinearLayout panelQuestionBottomNo;										// Bottom No Container
	ImageView imageQuestionCloudClear;										// Image Cloud Clear Container
	ImageView imageQuestionCloudFew;										// Image Cloud Few Container
	ImageView imageQuestionCloudScattered;									// Image Cloud Scattered Container
	ImageView imageQuestionCloudBroken;										// Image Cloud Broken Container
	ImageView imageQuestionCloudOvercast;									// Image Cloud Overcast Container	
	ImageView imageQuestionRainingYes;										// Image Raining Yes Container
	ImageView imageQuestionRainingNo;										// Image Raining No Container
	ImageView imageQuestionBottomYes;										// Image Bottom Yes Container
	ImageView imageQuestionBottomNo;										// Image Bottom No Container
	Button btSend;															// Button of send info
	CheckBox chkPlaying;													// CheckBox of Playing	
	ScrollView containerAppSurvey;											// Container of app survey
	RadioGroup appsurveyEasytouseRadiogroup;								// Radio Group Easy To Use
	RadioButton appsurveyEasytouseYes;										// Radio Easy To Use Yes
	RadioButton appsurveyEasytouseNo;										// Radio Easy To Use No
	RadioGroup appsurveyMatchBarsRadiogroup;								// Radio Group Match Bars
	RadioButton appsurveyMatchBarsYes;										// Radio Match Bars Yes
	RadioButton appsurveyMatchBarsNo;										// Radio Match Bars No
	RadioGroup appsurveyLookWaterRadiogroup;								// Radio Group Look Water
	RadioButton appsurveyLookWaterWater;									// Radio Look Water Water
	RadioButton appsurveyLookWaterPhoto;									// Radio Look Water Photo
	RadioGroup appsurveyDesignRadiogroup;									// Radio Group Design
	RadioButton appsurveyDesignYes;											// Radio Match Design
	RadioButton appsurveyDesignNo;											// Radio Match Design
	RadioGroup appsurveySecchiRadiogroup;									// Radio Group Secchi
	RadioButton appsurveySecchiWith;										// Radio Secchi With
	RadioButton appsurveySecchiWithout;										// Radio Secchi Without
	RadioButton appsurveySecchiDidnotuseit;									// Redio Secchi Did not use it
	EditText editSuggestion;												// EditText of Suggestion
	Button appsurveyBack;													// Back Button
	Button appsurveySend;													// Send Button
	ScrollView scrollFeedback;												// Scroll Container Feedback
	TextView txtFeedback_text2;												// TextView Text 2, FU Value
	TextView txtFeedBackDescValue1;											// TextView Value option 1
	TextView txtFeedBackDescValue2;											// TextView Value option 2
	TextView txtFeedBackDescValue3;											// TextView Value option 3
	TextView txtFeedBackDescValue4;											// TextView Value option 4
	TextView txtFeedBackDescValue5;											// TextView Value option 5
	ScrollView scrollSecchiDiskDepth;										// Scroll Container for Secchi Disk Depth	
	NumericKeyPadWidget keyPadDiskDepth;									// KeyPad Secchi Disk Depth
	ScrollView scrollPlasticFuValue;										// Scroll Container for Plastic FU Value	
	NumericKeyPadWidget keyPadPlasticFuValue;								// KeyPad Plastic FU Value
		
	private boolean [] estatsBarres;										// Array status bars 
	final int colorFonsBar = Color.TRANSPARENT;								// Back Color of the Bar.
	
	// For Touch FU Scale /Touch and Tap
	float TapCurrentX = 0.0f;												// Variable X tap
	float TapCurrentY = 0.0f;												// Variable Y tap
	private boolean IsTap = false;											// Boolean to know if it is a tap
	private int xDelta;														// Move X							
	private int yDelta;														// Move Y
	private int NUM_PIXELS_MOVE = 15;										// Number of pixels to consider if it is a MOVE and not a TAP

	private String sProfile = "";											// Citizen's Profile description
	private boolean askForSecchiDisk = true;								// Ask if user has Secchi Disk
	private boolean askForPlasticScale = true;								// Ask if user has a Plastic Scale	
	private boolean bUserHasSecchiDisk = false;								// Gets if user has a Secchi Disk	
	private float fSecchiDiskDepth = 0.0f;									// Estimated Depth of Secchi Disk
	private boolean bUserHasPlasticFUScale = false;							// Gets if user has a Plastic Fu Scale Value
	private int iPlasticFuValue = 0;										// Plastic FU Value	
	
	// Folders to store data and images
	String LastPictureNameMetadata;											// Last name of the image
	private String baseFullPath;											// Whole Path where the photos will be saved
	private String baseFullPathTemp;										// Whole Path Temp where the photos will be saved
	private File TemporalDirectory;											// Temp directory
	private File TmpDirectory;												// FU Scale Photo and Data directory

	/* - - - - - - - - - - - - - - - - - - - - - - */
	/* - - - - -  C o n s t r u c t o r  - - - - - */
	/* - - - - - - - - - - - - - - - - - - - - - - */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try{
			// Show Working Dialog
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_fuscale_ext);
			
			// Get the profile string
			SettingsData prf = new SettingsData(this);
			prf.LoadData();
			sProfile = prf.getSelectedProfileCode();			
			askForSecchiDisk = prf.getAskSecchiDiskPreference();
			askForPlasticScale = prf.getAskPlasticScalePreference();	

			// Get Screen Density
			metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			// Local Copy
			_this = this;
		} catch (Exception e) {
			// Error in Load Activity
			Toast.makeText(_this, getString(R.string.main_error_load_gen) + e.getMessage(), Toast.LENGTH_LONG).show();
		}finally{

		}
	}
	/* - - - - - - - - - - - - - - - - - - - - - */
	/* - - - - -   O v e r r i d e s   - - - - - */
	/* - - - - - - - - - - - - - - - - - - - - - */
	@Override
	protected void onResume() {
		try {
			// Call super
			super.onResume();

			// Show Working Dialog
			if (workingDialog == null) workingDialog = kTransparentWorkingDialog.show(FUScaleActivityExt.this, null, null);

			// Update to Max Brightness
			if (brightnessUtility == null) brightnessUtility = new BrightnessUtility(this.getWindow(), this);
			brightnessUtility.MaxBrightness();

			if (firstExecution){
				firstExecution = false;

				// In first execution, the state is Secchi Ask
				activityState = ACTIVITY_STATE.SECCHI_DISK_ASK;
				
				// In first execution, Starts sending information
				SendFUDataServiceManager.StartSend(FUScaleActivityExt.this, (Activity)_this);

				// Executem la creació del activity
				(new OnCreateActivityTask()).execute();    				
			} else {
				// Instantiate the camera
				if (!cameraLib.openCamera()){
					// Failed to open the camera.
					Toast.makeText(_this, getString(R.string.main_error_opencamera), Toast.LENGTH_LONG).show();
				} 
				
				// Init Camera Values
				initCamera();

				// Register the sensor
				if (fuScaleSensors.Register() == 0){
					// There's no sensors
					Toast.makeText(_this, getString(R.string.main_error_no_sensors), Toast.LENGTH_LONG).show();				
				}
				
				// Start Listening Location Changes
				mLocationUtils.StartListen();		
				
				// Dismiss Working Dialog
				if (workingDialog != null) {
					workingDialog.dismiss();
					workingDialog = null;
				}				
			}
		} catch (Exception e) {
			// Error in Load Activity
			Toast.makeText(_this, getString(R.string.main_error_load_gen) + e.getMessage(), Toast.LENGTH_LONG).show();
		}finally{
			
		}
	}
	@Override
	protected void onPause() {
		try{
			// Call super
			super.onPause();
			
			// Restore Brightness
			if (brightnessUtility == null) brightnessUtility = new BrightnessUtility(this.getWindow(), this);
			brightnessUtility.restoreBrightness();

			// Release camera Preview
			if(cameraLib != null) cameraLib.releaseCameraAndPreview();
			
			// UnRegister the sensor
			if (fuScaleSensors != null) fuScaleSensors.UnRegister();

			// Stop Listening Location Changes
			if (mLocationUtils != null) mLocationUtils.StopListen();

		} catch (Exception e){
			// We can't do nothing. Only show message
			Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		} finally{
			
		}
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);		
		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
			// Landscape Orientation
			orientationLandscape();
		} else{
			// Portrait Orientation
			orietationPortrait();
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
//		case R.id.MnuPreferences:
//			Intent intent = new Intent(this, FUScalePreferencesActivity.class);
//			startActivity(intent);
//			break;
		case R.id.MnuCamera:
			selectFocusWhiteScene();
			break;
		}
		return true;
	} 
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		// Enable menu only in FuScale State
		boolean bEnabled = false;
		if (activityState == ACTIVITY_STATE.TAKE_PHOTO) bEnabled = true;
		for (int i = 0; i < menu.size(); i++){
			menu.getItem(i).setEnabled(bEnabled);	
		}
	    return true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			switch (activityState){
				case LOCATION_NOT_ENABLED:
					// Can't go back
					break;				
				case SECCHI_DISK_ASK:
					// Can't go back
					break;
				case SECCHI_DISK_DEPTH:
					// Back to Ask Secchi Disk
					switchToStat(ACTIVITY_STATE.SECCHI_DISK_ASK);
					break;
//				case SUN_POSITION:
//					// Can't go back
//					break;
//				case SHOW_PANEL_SUN:
//					// Back to Ask Secchi Disk
//					switchToStat(ACTIVITY_STATE.SECCHI_DISK_ASK);
//					break;
				case TAKE_PHOTO:
					switch (event.getAction()) {
					case KeyEvent.ACTION_DOWN:
						if (event.getDownTime() - lastBackPressedTime < PERIOD) {
							// Exit Activity
							exitActivity();
						} else {
							Toast.makeText(getApplicationContext(), getString(R.string.main_back_to_exit), Toast.LENGTH_SHORT).show();
							lastBackPressedTime = event.getEventTime();
						}
						return true;
					}
					break; 
				case SELECT_RECT:
					// Back to take Photo
					switchToStat(ACTIVITY_STATE.TAKE_PHOTO);
					break; 
				case SELECT_FUSCALE_VALUE:
					// Back to take Photo
					switchToStat(ACTIVITY_STATE.TAKE_PHOTO);
					break; 
				case PLASTIC_FUSCALE_ASK:
					// Can't go back
					break;
				case PLASTIC_FUSCALE_VALUE:
					// Back to Ask Plastic Fu
					switchToStat(ACTIVITY_STATE.PLASTIC_FUSCALE_ASK);
					break;
				case QUESTIONARY:
					// Back to take Photo
					switchToStat(ACTIVITY_STATE.TAKE_PHOTO);
					break; 
				case APP_SURVEY_ASK:
					// Can't go back
					break;
				case APP_SURVEY:
					// Go Directly to Feedback
					switchToStat(ACTIVITY_STATE.FEEDBACK);
					break; 
				case FEEDBACK:
					// Back to Main Menu
					exitActivity();
					break; 
			}
		}
	    return false;
	}
	/* - - - - - - - - - - - - - - - - - -- - - - - - - - - - - - - - - - - - */
	/* - -  C r e a t e     a n d      R e s u m e      T a s k s   - - - - - */
	/* - - - - - - - - - - - - - - - - - -- - - - - - - - - - - - - - - - - - */
	/*
	 * Exit the Activity 
	 */
	private void exitActivity(){
		// Recycle Bitmaps of possible other photos			
		if (bmpCroppedPhoto != null) {
			bmpCroppedPhoto.recycle();
			bmpCroppedPhoto = null;
		}
		if (bmpPhotoTaken != null) {
			bmpPhotoTaken.recycle();
			bmpPhotoTaken = null;
		}

		// Restore Brightness
		try{if (brightnessUtility != null) brightnessUtility.restoreBrightness();
		} catch(Exception e){}

		// Release camera Preview
		if(cameraLib != null) {
//			cameraLib.setCallback(null);
//			if (previewContainer != null) previewContainer.removeView(cameraLib.getSurfacePreview());
			cameraLib.releaseCameraAndPreview();
		}
		cameraLib = null;
		
		// UnRegister the sensor
		if (fuScaleSensors != null) fuScaleSensors.UnRegister();
		fuScaleSensors = null;

		// Stop Listening Location Changes
		if (mLocationUtils != null) mLocationUtils.StopListen();
		mLocationUtils = null;

		
		// Exit Activity
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				finish();				
			}			
		}, 50);		
	}	
    /**
	* @ Background Task to Create Activity 
	*/	
    class OnCreateActivityTask extends AsyncTask<String, Integer, Boolean> {
    	String msgError = "";
    	boolean bError = false;
		@Override
		protected Boolean doInBackground(String... arg0) {
			try{
    			// Create Sensor Manager
    			fuScaleSensors = new FUScaleSensors((Activity)_this, mSensorEventListener);

    			// Create Manager For Location		 
    			mLocationUtils = new LocationUtils(_this);
    			mLocationUtils.setNewBestLocationListener(newBestLocListener);
    				
    			// Start Listening and create Camera Library
    			publishProgress(50);

    			// Create FU Values Bars
    			dataFuScaleBars = new FUBarValues(_this);

    			// Create object for Metadata
    			metadata = new FUScaleMetadata(_this);

				// Get Path to store photos
				String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DataConstants.baseFolder;				
				baseFullPath = basePath + "/" + DataConstants.FUScale_FolderPhotoName + "/";
				baseFullPathTemp = baseFullPath + "/" + DataConstants.FUScale_FolderTempPhotoName + "/";
				
				// Define and create folders
				TmpDirectory = new File(baseFullPath);
				TemporalDirectory = new File(baseFullPathTemp);		
				FileUtils.DeleteDirectory(TemporalDirectory);
				if (!TmpDirectory.exists()) TmpDirectory.mkdirs();
				if (!TemporalDirectory.exists()) TemporalDirectory.mkdirs();
    			
    		} catch (Exception e){
    			bError = true;
    			msgError = e.getMessage();
    		} finally{
    			
    		}
    		return bError;
		}
		@Override
		protected void onProgressUpdate(Integer... msg) {
			if (msg[0] == 50) {
				// Start Listening Location Changes
				mLocationUtils.StartListen();
				
				// Init Camera library
				cameraLib = new CitclopsCameraLibrary(_this);
				cameraLib.setCallback(cameraCallback);				
			}
		}
		@Override 
		protected void onPostExecute(Boolean result) {
			try{
				if (bError) throw new Exception(msgError);
				
				// References to Photo Container and it's children
		    	previewContainer = (FrameLayout)findViewById(R.id.fuscale_previewcontainer);
		    	btQuickHelp = (kImageButton)findViewById(R.id.fuscale_quickHelpButton);
		    	txtPitchValue = (TextView)findViewById(R.id.fuscale_txtpitchvalue);
		    	btObjetivo = (kImageButton)findViewById(R.id.fuscale_btobjetivo);
		    	btObjetivo.setOnClickListener(mListenerTakePhoto);
		    	containerTakePhotoWidgets = (LinearLayout)findViewById(R.id.fuscale_container_teakephoto_widgets);
		    	containerPitchWidgets = (LinearLayout)findViewById(R.id.fuscale_containerpitchwidgets);
		    	txtPreviewInstructionsTop = (TextView)findViewById(R.id.fuscale_preview_instructionstop);
		    	containerWorkPhoto = (FrameLayout)findViewById(R.id.fuscale_work_photo_container);
		    	containerWorkPhotoSquare = (LinearLayout)findViewById(R.id.fuscale_work_photo_square_container);
		    	imgWorkPhoto = (ImageView)findViewById(R.id.fuscale_work_photo_image);		    			    	
		    	fuscaleSquare11 = (FrameLayout)findViewById(R.id.fuscale_square_11);		    	
		    	fuscaleSquare12 = (FrameLayout)findViewById(R.id.fuscale_square_12);
		    	fuscaleSquare13 = (FrameLayout)findViewById(R.id.fuscale_square_13);
		    	fuscaleSquare21 = (FrameLayout)findViewById(R.id.fuscale_square_21);
		    	fuscaleSquare22 = (FrameLayout)findViewById(R.id.fuscale_square_22);
		    	fuscaleSquare23 = (FrameLayout)findViewById(R.id.fuscale_square_23);
		    	fuscaleSquare31 = (FrameLayout)findViewById(R.id.fuscale_square_31);
		    	fuscaleSquare32 = (FrameLayout)findViewById(R.id.fuscale_square_32);
		    	fuscaleSquare33 = (FrameLayout)findViewById(R.id.fuscale_square_33);
		    	fuscaleSquareInstructions = (TextView)findViewById(R.id.fuscale_square_instructions);
		    	btSelectSquareOK = (kImageButton)findViewById(R.id.fuscale_select_square_ok_button);
		    	containerSelectValue = (FrameLayout)findViewById(R.id.fuscale_container_select_value);
		    	imgSelectValue = (ImageView)findViewById(R.id.fuscale_select_fuvalue_image);
				marcoFuScaleContainer = (RelativeLayout) findViewById(R.id.fuscale_marcoFuScaleContainer);					
				marcoFuScaleSeekBar = (SeekBar) findViewById(R.id.fuscale_marcoFuScaleSeekBar);	
				fuscaleSelectValueInstructions= (TextView)findViewById(R.id.fuscale_selectvalue_instructions);				
			    fuscaleConatinerSelectFuvalueText = (FrameLayout)findViewById(R.id.fuscale_conatiner_select_fuvalue_text);
				btSelectFUValueOK = (kImageButton)findViewById(R.id.fuscale_selectvalue_ok_button);
				containerQuestions = (ScrollView)findViewById(R.id.fuscale_container_questions);		    						    
				panelQuestionCloudClear = (LinearLayout)findViewById(R.id.fuscale_questions_cloud_clear);
				panelQuestionCloudFew = (LinearLayout)findViewById(R.id.fuscale_questions_cloud_few);
				panelQuestionCloudScattered = (LinearLayout)findViewById(R.id.fuscale_questions_cloud_scattered);
				panelQuestionCloudBroken = (LinearLayout)findViewById(R.id.fuscale_questions_cloud_broken);
				panelQuestionCloudOvercast = (LinearLayout)findViewById(R.id.fuscale_questions_cloud_overcast);				
				panelQuestionRainingYes = (LinearLayout)findViewById(R.id.fuscale_questions_raining_yes);
				panelQuestionRainingNo = (LinearLayout)findViewById(R.id.fuscale_questions_raining_no);				
				panelQuestionBottomYes = (LinearLayout)findViewById(R.id.fuscale_questions_bottom_yes);
				panelQuestionBottomNo = (LinearLayout)findViewById(R.id.fuscale_questions_bottom_no);
				imageQuestionCloudClear = (ImageView)findViewById(R.id.fuscale_image_cloud_clear);
				imageQuestionCloudFew = (ImageView)findViewById(R.id.fuscale_image_cloud_few);
				imageQuestionCloudScattered = (ImageView)findViewById(R.id.fuscale_image_cloud_scattered);
				imageQuestionCloudBroken = (ImageView)findViewById(R.id.fuscale_image_cloud_broken);
				imageQuestionCloudOvercast = (ImageView)findViewById(R.id.fuscale_image_cloud_overcast);
				imageQuestionRainingYes = (ImageView)findViewById(R.id.fuscale_image_raining_yes);
				imageQuestionRainingNo = (ImageView)findViewById(R.id.fuscale_image_raining_no);				
				imageQuestionBottomYes = (ImageView)findViewById(R.id.fuscale_image_bottom_yes);
				imageQuestionBottomNo = (ImageView)findViewById(R.id.fuscale_image_bottom_no);
				btSend = (Button)findViewById(R.id.fuscale_btSend);
				chkPlaying = (CheckBox)findViewById(R.id.fuscale_chkPlaying);
				
				containerAppSurvey = (ScrollView)findViewById(R.id.fuscale_container_appsurvey);				
				appsurveyEasytouseRadiogroup = (RadioGroup)findViewById(R.id.radio_appsurvey_easytouse_radiogroup);
				appsurveyEasytouseYes = (RadioButton)findViewById(R.id.radio_appsurvey_easytouse_yes);
				appsurveyEasytouseNo = (RadioButton)findViewById(R.id.radio_appsurvey_easytouse_no);				
				appsurveyMatchBarsRadiogroup = (RadioGroup)findViewById(R.id.radio_appsurvey_matchbars_radiogroup);
				appsurveyMatchBarsYes = (RadioButton)findViewById(R.id.radio_appsurvey_matchbars_yes);
				appsurveyMatchBarsNo = (RadioButton)findViewById(R.id.radio_appsurvey_matchbars_no);
				appsurveyLookWaterRadiogroup = (RadioGroup)findViewById(R.id.radio_appsurvey_lookwater_radiogroup);
				appsurveyLookWaterWater = (RadioButton )findViewById(R.id.radio_appsurvey_lookwater_water);
				appsurveyLookWaterPhoto = (RadioButton )findViewById(R.id.radio_appsurvey_lookwater_photo);
				appsurveyDesignRadiogroup = (RadioGroup)findViewById(R.id.radio_appsurvey_design_radiogroup);
				appsurveyDesignYes = (RadioButton )findViewById(R.id.radio_appsurvey_design_yes);
				appsurveyDesignNo = (RadioButton )findViewById(R.id.radio_appsurvey_design_no);
				appsurveySecchiRadiogroup = (RadioGroup)findViewById(R.id.radio_appsurvey_secchi_radiogroup);
				appsurveySecchiWith = (RadioButton )findViewById(R.id.radio_appsurvey_secchi_with);
				appsurveySecchiWithout = (RadioButton )findViewById(R.id.radio_appsurvey_secchi_without);				
				appsurveySecchiDidnotuseit = (RadioButton )findViewById(R.id.radio_appsurvey_secchi_didnotuseit);
				editSuggestion = (EditText)findViewById(R.id.edittext_appsurvey_suggestion);				
				appsurveyBack = (Button)findViewById(R.id.appsurvey_btBack);
				appsurveySend = (Button)findViewById(R.id.appsurvey_btSend);
				scrollFeedback = (ScrollView)findViewById(R.id.fuscale_scrollFeedback);
				txtFeedback_text2 = (TextView)findViewById(R.id.fuscale_txtFeedback_text2);
				txtFeedBackDescValue1 = (TextView)findViewById(R.id.fuscale_txtFeedBackDescValue1);
				txtFeedBackDescValue2 = (TextView)findViewById(R.id.fuscale_txtFeedBackDescValue2);
				txtFeedBackDescValue3 = (TextView)findViewById(R.id.fuscale_txtFeedBackDescValue3);
				txtFeedBackDescValue4 = (TextView)findViewById(R.id.fuscale_txtFeedBackDescValue4);
				txtFeedBackDescValue5 = (TextView)findViewById(R.id.fuscale_txtFeedBackDescValue5);
				scrollSecchiDiskDepth = (ScrollView)findViewById(R.id.fuscale_scrollSecchiDiskDepth);
				keyPadDiskDepth = (NumericKeyPadWidget)findViewById(R.id.fuscale_keyPadDiskDepth);				
				scrollPlasticFuValue = (ScrollView)findViewById(R.id.fuscale_scroll_PlasticFUValue);	
				keyPadPlasticFuValue = (NumericKeyPadWidget)findViewById(R.id.fuscale_keyPadPlasticFuValue);
								
		    	// Set the Listeners for the photo rectangle
				fuscaleSquare22.setOnTouchListener(touchSquareSelection);
		    	
		    	// Set Listener to Select Square Button
		    	btSelectSquareOK.setOnClickListener(listenerSelectSquareButton);

		    	// Set Listener to Select Fu Value Button
		    	btSelectFUValueOK.setOnClickListener(listenerSelectFuValueButton);
		    	
		    	// Set Listener to Quick Help Button
		    	btQuickHelp.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						if (workingDialog == null) workingDialog = kTransparentWorkingDialog.show(FUScaleActivityExt.this, null, null);
						Intent intent = new Intent(_this, FUScaleQuickHelpActivity.class);
						startActivity(intent);
					}		    		
		    	});
		    			    	
		    	// Set Listener for the seek bar
		    	marcoFuScaleSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) panelFuScaleContainer.getLayoutParams();
						layoutParams.setMargins(-progress, layoutParams.topMargin, 0, 0);
						panelFuScaleContainer.setLayoutParams(layoutParams);
					}
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {}		    		
		    	});
		    	
		    	// Set Listener for NumericKeyPad for SecchiDiskDepth. Define for float values
		    	keyPadDiskDepth.setOnlyInteger(false);
		    	keyPadDiskDepth.setNewValueListener(listener_secchi_disk_depth_keypad_value);

		    	// Set Listener for NumericKeyPad for Plastic FU Value. Define for Integer values
		    	keyPadPlasticFuValue.setOnlyInteger(true);
		    	keyPadPlasticFuValue.setNewValueListener(listener_plastic_fu_keypad_value);

				// Instantiate the camera
				if (!cameraLib.openCamera()){
					// Failed to open the camera.
					Toast.makeText(_this, getString(R.string.main_error_opencamera), Toast.LENGTH_LONG).show();
				}
				
				// Init Camera Values
				initCamera();

				// Register the sensor
				if (fuScaleSensors.Register() == 0){
					// There's no sensors
					Toast.makeText(_this, getString(R.string.main_error_no_sensors), Toast.LENGTH_LONG).show();				
				}			
							
				// Apply Actual Orientation
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
					orientationLandscape();
				} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
					orietationPortrait();			
				}
		    	
				if (askForSecchiDisk){					
					// Switch to Ask for a Secchi Disk Stat
					switchToStat(ACTIVITY_STATE.SECCHI_DISK_ASK);
				} else {
					// Don't ask for Secchi Disk
					bUserHasSecchiDisk = false;
					fSecchiDiskDepth = 0;
					switchToStat(ACTIVITY_STATE.TAKE_PHOTO);
				}
			} catch (Exception e){
				// Generic Error
				Toast.makeText(_this, getString(R.string.main_error_load_gen) + e.getMessage(), Toast.LENGTH_LONG).show();				
			} finally{
				// Dismiss Working Dialog
				if (workingDialog != null) {
					workingDialog.dismiss();
					workingDialog = null;
				}								
			}
		}    	
    }
	/* - - - - - - - - - - - - - - - - - - - - - - - - - */
	/* - - - -  A c t i v i t y     S t a t e  - - - - - */
	/* - - - - - - - - - - - - - - - - - - - - - - - - - */
    /*
     * Change to Activity State indicated
     */
    private void switchToStat(ACTIVITY_STATE newState){    	
    	try{
    		switch (newState){
				case LOCATION_NOT_ENABLED:
					state_LOCATION_NOT_ENABLED();
					break;				
		    	case SECCHI_DISK_ASK:
		    		state_SECCHI_DISK_ASK();
		    		break; 
		    	case SECCHI_DISK_DEPTH:
		    		state_SECCHI_DISK_DEPTH();
		    		break; 
//		    	case SUN_POSITION:
//		    		state_SUN_POSITION();
//		    		break; 
//		    	case SHOW_PANEL_SUN:
//		    		state_SHOW_PANEL_SUN();
//		    		break;	
		    	case TAKE_PHOTO:
		    		state_TAKE_PHOTO();
		    		break; 
		    	case SELECT_RECT:
		    		state_SELECT_RECT();
		    		break; 
		    	case SELECT_FUSCALE_VALUE:
		    		state_SELECT_FUSCALE_VALUE();
		    		break; 
		    	case PLASTIC_FUSCALE_ASK:
		    		state_PLASTIC_FUSCALE_ASK();
		    		break; 
		    	case PLASTIC_FUSCALE_VALUE:
		    		state_PLASTIC_FUSCALE_VALUE();
		    		break; 
		    	case QUESTIONARY:
		    		state_QUESTIONARY();
		    		break;		
		    	case APP_SURVEY_ASK:
		    		state_APP_SURVEY_ASK();
		    		break;		
		    	case APP_SURVEY:
		    		state_APP_SURVEY();
		    		break;		
		    	case FEEDBACK:
		    		state_FEEDBACK();
		    		break;		
	    	}	    	
	    	// Save the new state
	    	activityState = newState;    		
		} catch (Exception e){
			// Generic Error
			Toast.makeText(_this, getString(R.string.main_error_load_gen) + e.getMessage(), Toast.LENGTH_LONG).show();				
		} finally{
    		
    	}
    }
    /*
     * Listener Response YES on Secchi Disk Question 
     */
	DialogInterface.OnClickListener listener_secchidisk_ask_response_yes = new DialogInterface.OnClickListener() {				
		public void onClick(DialogInterface dialog, int which) {
			// User has a Secchi Disk. We will Ask for the estimated depth
			bUserHasSecchiDisk = true;
			
			// Switch to Ask for a Secchi Disk Depth
			switchToStat(ACTIVITY_STATE.SECCHI_DISK_DEPTH);
		}
	};
	/*
     * Listener Response NO on Secchi Disk Question 
	 */
	DialogInterface.OnClickListener listener_secchidisk_ask_response_no= new DialogInterface.OnClickListener() {				
		public void onClick(DialogInterface dialog, int which) {
			// User don't have a Secchi Disk
			bUserHasSecchiDisk = false;
			fSecchiDiskDepth = 0;
			
			// Switch to Ask for a Secchi Disk Depth
			//switchToStat(ACTIVITY_STATE.SUN_POSITION);
			switchToStat(ACTIVITY_STATE.TAKE_PHOTO);
		}
	};	
	/*
	 * State  LOCATION_NOT_ENABLED
	 */
	private void state_LOCATION_NOT_ENABLED(){
    	// Unlock screen rotation
    	unlockScreenRotation();

    	// Show Message
    	MessageDialog.ShowDialog(_this, getResources().getText(R.string.app_name).toString(), getString(R.string.main_location_not_set_Mandatory), "OK", listener_location_not_enabled, true);
	}	
    /*
     * Listener Accept State Not Location Enabled 
     */
	DialogInterface.OnClickListener listener_location_not_enabled = new DialogInterface.OnClickListener() {				
		public void onClick(DialogInterface dialog, int which) {
			// Exit
			exitActivity();
		}
	};
    /*
     * State SECCHI_DISK_ASK
     */
    private void state_SECCHI_DISK_ASK(){
    	// Unlock screen rotation
    	unlockScreenRotation();
    	
    	// Hide Panels
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.GONE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.GONE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.GONE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.GONE);
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.GONE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.GONE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.GONE);
    	    	    	
    	// Ask for a Secchi Disk
		MessageDialog.ShowDialogYesNo(
   				_this, 
   				getResources().getText(R.string.app_name).toString(),
   				getResources().getText(R.string.main_secchidisk_ask_question).toString(),
   				getResources().getText(R.string.main_secchidisk_ask_yes).toString(),
   				getResources().getText(R.string.main_secchidisk_ask_no).toString(),
   				listener_secchidisk_ask_response_yes, 
   				listener_secchidisk_ask_response_no, 
   				false);
    }   
    /*
     * Listener for plastic fu value  from keypad  
     */
    NumericKeyPadWidget.newValueListener listener_plastic_fu_keypad_value = new NumericKeyPadWidget.newValueListener() {
		@Override
		public void newValue(float newValue) {
			if ((newValue < 1) || (newValue > 21)){
				// Bad Value
				DialogInterface.OnClickListener listener_plastic_fuscale_bad_format = new DialogInterface.OnClickListener() {				
					public void onClick(DialogInterface dialog, int which) {
						// Bad Depth format. Input again
						switchToStat(ACTIVITY_STATE.PLASTIC_FUSCALE_VALUE);
					}
				};
				// Bad Range. Inform and ask again
				MessageDialog.ShowDialog(
						_this, 
		   				getResources().getText(R.string.app_name).toString(),
		    			getResources().getText(R.string.main_panel_questions_plastic_fuscale_ask_value_bad_format).toString(),
		    			getResources().getText(R.string.main_panel_questions_plastic_fuscale_ask_ok).toString(),
		    			listener_plastic_fuscale_bad_format, 
						true);				
			} else {
				// Get Value
				iPlasticFuValue = (int)newValue;

				// Go to Sun Position
				switchToStat(ACTIVITY_STATE.QUESTIONARY);							
			}
		}    	
    };        
    /*
     * Listener for enter secchi disk estimated depth from keypad  
     */
    NumericKeyPadWidget.newValueListener listener_secchi_disk_depth_keypad_value = new NumericKeyPadWidget.newValueListener() {
		@Override
		public void newValue(float newValue) {
			fSecchiDiskDepth = newValue;

			// Go to Sun Position
			//switchToStat(ACTIVITY_STATE.SUN_POSITION);
			switchToStat(ACTIVITY_STATE.TAKE_PHOTO);
		}    	
    };    
    /*
     * State SECCHI_DISK_DEPTH
     */
    private void state_SECCHI_DISK_DEPTH(){
    	// Unlock screen rotation
    	unlockScreenRotation();
    	
    	// Init KeyPadValue
    	keyPadDiskDepth.InitValue();    	

    	// Hide Panels
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.GONE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.GONE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.GONE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.GONE);    	
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.GONE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.VISIBLE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.GONE);
    }
    /*
     * State TAKE_PHOTO
     */
    private void state_TAKE_PHOTO(){    	
    	// Lock To Landscape
    	lockScreenRotationPortrait();

    	// Delete Temp Directory
		FileUtils.DeleteDirectory(TemporalDirectory);

		// Init Camera Values
		try{
			initCamera();
		}catch(Exception e){
			// Error in Camera Initializatin
			Toast.makeText(_this, getString(R.string.main_panelfuscale_error_init_camera) + e.getMessage(), Toast.LENGTH_LONG).show();
		}
				
    	// Update instructions text, depending on Secchi Disk
    	if (bUserHasSecchiDisk) txtPreviewInstructionsTop.setText(getString(R.string.main_panelfuscale_instructions1_secchi));
    	else txtPreviewInstructionsTop.setText(getString(R.string.main_panelfuscale_instructions1));
    	
    	// Show Panels
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.VISIBLE);
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.GONE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.GONE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.GONE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.GONE);    	
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.GONE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.GONE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.GONE);
    	
    	// Wait until the location has accuracy
    	if (mLocationUtils.isLocationEnabled()){
    		// Get if device is located
    		if ((bestLatLocation <= Double.MIN_VALUE) || (bestLongLocation <= Double.MIN_VALUE)){
    			// Device is not located. Location Is Enabled. Show progress bar for location
        		progressPositionDialog = new ProgressDialog(_this);  
        		progressPositionDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
        		progressPositionDialog.setTitle(getString(R.string.main_panelsquare_location_dialog_loading));  
        		progressPositionDialog.setMessage(getString(R.string.main_panelsquare_location_dialog_wait));  
        		progressPositionDialog.setCancelable(false);	//This dialog can't be canceled by pressing the back key  
        		progressPositionDialog.setIndeterminate(true);  
        		progressPositionDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.main_panelsquare_location_dialog_cancel), new DialogInterface.OnClickListener(){
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					// User cancelled Automatic Sun Position. Get Manual Sun Position
    					bLocationCanceled = true;
    					mLocationUtils.StopListen();
    					switchToStat(ACTIVITY_STATE.LOCATION_NOT_ENABLED);
    				}				
    			});
        		progressPositionDialog.show();  
    		}
		} else {
			// Location is not enabled. Show Panel for manual sun position
			switchToStat(ACTIVITY_STATE.LOCATION_NOT_ENABLED);
		}

    }
    /*
     * State SELECT_RECT
     */
    private void state_SELECT_RECT(){
    	// Lock To Landscape
    	lockScreenRotationPortrait();

    	// Save metadata on take picture moment
		fuScaleSensors.setSunAzimuthDegreesManual((int)calculateSunPosition(mLocationUtils.getLatitude(), mLocationUtils.getLongitude()));
		metadata.setAzimuth(fuScaleSensors.getAzimuthDegrees());
		metadata.setLatitude(mLocationUtils.getLatitude());
		metadata.setLongitude(mLocationUtils.getLongitude());
		metadata.setAccuracy(mLocationUtils.getAccuracy());
		metadata.setPitch(fuScaleSensors.getPitchDegrees());
		metadata.setCameraParameters(cameraLib.getCameraParameters());
    	
		// Show Image in ImageView				
    	if (bmpPhotoTaken != null){
			// Update Screen Size
    		getWindowManager().getDefaultDisplay().getMetrics(metrics);

    		// Get factor to scale
    		double fx = Math.min((double)metrics.widthPixels / (double)bmpPhotoTaken.getWidth(), (double)metrics.heightPixels / (double)bmpPhotoTaken.getHeight());  

    		// Set Image widget size		
    		imgWorkPhoto.getLayoutParams().width = 	(int)((double)bmpPhotoTaken.getWidth() * fx);
    		imgWorkPhoto.getLayoutParams().height = (int)((double)bmpPhotoTaken.getHeight() * fx);
    	} else {
    		imgWorkPhoto.getLayoutParams().width = 	LayoutParams.MATCH_PARENT;
    		imgWorkPhoto.getLayoutParams().height = LayoutParams.MATCH_PARENT;
    	}
		imgWorkPhoto.setImageBitmap(bmpPhotoTaken);
		
    	// Init move touch values
    	FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) containerWorkPhotoSquare.getLayoutParams();
    	_xDeltaSquareSelection = 0;
    	_yDeltaSquareSelection = 0;
    	layoutParams.leftMargin = 0;
    	layoutParams.topMargin = 0;
    	containerWorkPhotoSquare.setLayoutParams(layoutParams);
    	marcoFuScaleSeekBar.setProgress(0);
    	
    	// Init background    	
    	fuscaleSquare11.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_not_selected));		    	
    	fuscaleSquare12.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_not_selected));
    	fuscaleSquare13.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_not_selected));
    	fuscaleSquare21.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_not_selected));
		fuscaleSquare22.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_selected));
    	fuscaleSquare23.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_not_selected));
    	fuscaleSquare31.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_not_selected));
    	fuscaleSquare32.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_not_selected));
    	fuscaleSquare33.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_not_selected));
    	    	
    	// Update Text Instructions
    	if (bUserHasSecchiDisk) fuscaleSquareInstructions.setText(getString(R.string.main_panelsquare_instructions_secchi));
    	else fuscaleSquareInstructions.setText(getString(R.string.main_panelsquare_instructions_no_secchi));
    	
    	// Show panels
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.VISIBLE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.GONE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.GONE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.GONE);
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.GONE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.GONE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.GONE);    	
    }
    /*
     * State SELECT_FUSCALE_VALUE
     */
    private void state_SELECT_FUSCALE_VALUE(){
    	// Lock To Landscape
    	lockScreenRotationPortrait();
    	
		// Create Fu Scale Bars    	
		createFuScaleBar();
		
		// Disable Select Button
		btSelectFUValueOK.setEnabled(false);
								
    	// Show Panels
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.GONE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.VISIBLE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.GONE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.GONE);    	
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.GONE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.GONE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.GONE);
    }
    /*
     * Listener Response YES on Plastic Fu Scale Question 
     */
	DialogInterface.OnClickListener listener_plastic_fuscale_ask_response_yes = new DialogInterface.OnClickListener() {				
		public void onClick(DialogInterface dialog, int which) {
			// User has a Plastic Fu Scale. We will Ask for the Fu Value
			bUserHasPlasticFUScale = true;
			
			// Switch to Ask for a Secchi Disk Depth
			switchToStat(ACTIVITY_STATE.PLASTIC_FUSCALE_VALUE);
		}
	};
	/*
     * Listener Response NO on Plastic Fu Scale Question 
	 */
	DialogInterface.OnClickListener listener_plastic_fuscale_ask_response_no= new DialogInterface.OnClickListener() {				
		public void onClick(DialogInterface dialog, int which) {
			// User don't have a Plastic Fu Scale
			bUserHasPlasticFUScale = false;
			
			// Switch to Ask for a Secchi Disk Depth
			switchToStat(ACTIVITY_STATE.QUESTIONARY);			
		}
	};
    /*
     * State PLASTIC_FUSCALE_ASK
     */
    private void state_PLASTIC_FUSCALE_ASK(){
    	// Unlock screen rotation
    	unlockScreenRotation();

    	// Init Panel Questions
    	clearPanelQuestions();

    	// Show Panels
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.GONE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.GONE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.VISIBLE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.GONE);    	
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.GONE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.GONE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.GONE);
    	    	
    	// Ask for a Plastic FU Scale 
		MessageDialog.ShowDialogYesNo(
   				_this, 
   				getResources().getText(R.string.app_name).toString(),
   				getResources().getText(R.string.main_panel_questions_plastic_fuscale_ask_question).toString(),   				
   				getResources().getText(R.string.main_panel_questions_plastic_fuscale_ask_yes).toString(),
   				getResources().getText(R.string.main_panel_questions_plastic_fuscale_ask_no).toString(),   				
   				listener_plastic_fuscale_ask_response_yes, 
   				listener_plastic_fuscale_ask_response_no, 
   				false);
    }
    /*
     * State PLASTIC_FUSCALE_VALUE
     */
    private void state_PLASTIC_FUSCALE_VALUE(){
    	// Unlock screen rotation
    	unlockScreenRotation();

    	// Init KeyPadValue
    	keyPadPlasticFuValue.InitValue();    	
    	
    	// Show Panels
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.GONE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.GONE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.VISIBLE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.GONE);    	
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.GONE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.GONE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.VISIBLE);    	
    }
    /*
     * State QUESTIONARY
     */
    private void state_QUESTIONARY(){
    	// Unlock screen rotation
    	unlockScreenRotation();

    	// Init Panel Questions
    	clearPanelQuestions();
    	
    	// Show Panels
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.GONE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.GONE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.VISIBLE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.GONE);    	
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.GONE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.GONE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.GONE);
    }
    /*
     * State APP_SURVEY_ASK
     */
    private void state_APP_SURVEY_ASK(){
    	// Unlock screen rotation
    	unlockScreenRotation();

    	// Init Panel AppSurvey
    	clearPanelAppSurvey();

    	// Show Panels
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.GONE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.GONE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.GONE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.VISIBLE);    	
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.GONE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.GONE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.GONE);
    	    	
    	// Ask for a Plastic FU Scale 
		MessageDialog.ShowDialogYesNo(
   				_this, 
   				getResources().getText(R.string.app_name).toString(),
   				getResources().getText(R.string.main_panel_questions_app_survey_ask_question).toString(),   				
   				getResources().getText(R.string.main_panel_questions_app_survey_ask_yes).toString(),
   				getResources().getText(R.string.main_panel_questions_app_survey_ask_no).toString(),   				
   				listener_app_survey_ask_response_yes, 
   				listener_app_survey_ask_response_no, 
   				false);
    }
    /*
     * State APP_SURVEY
     */
    private void state_APP_SURVEY(){
    	// Unlock screen rotation
    	unlockScreenRotation();

    	// Init Panel AppSurvey
    	clearPanelAppSurvey();

    	// Show Panels
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.GONE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.GONE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.GONE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.VISIBLE);    	
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.GONE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.GONE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.GONE);    	
    }
    /*
     * Listener Response YES on App Survey 
     */
	DialogInterface.OnClickListener listener_app_survey_ask_response_yes = new DialogInterface.OnClickListener() {				
		public void onClick(DialogInterface dialog, int which) {
			// Switch to Ask for a Secchi Disk Depth
			switchToStat(ACTIVITY_STATE.APP_SURVEY);
		}
	};
	/*
     * Listener Response NO on App Survey 
	 */
	DialogInterface.OnClickListener listener_app_survey_ask_response_no= new DialogInterface.OnClickListener() {				
		public void onClick(DialogInterface dialog, int which) {
			// Switch to Ask for a Secchi Disk Depth
			switchToStat(ACTIVITY_STATE.FEEDBACK);			
		}
	};
    /*
     * State FEEDBACK
     */
    private void state_FEEDBACK(){
    	// Unlock screen rotation
    	unlockScreenRotation();
		
		// Start Sending Information
		SendFUDataServiceManager.StartSend(FUScaleActivityExt.this, (Activity)_this);
		
    	// Update the UI of Feedback Panel
    	updateFeedBackUI();
    	
    	// Show Panels
    	if (fuScalePanelArrow != null) fuScalePanelArrow.setVisibility(FrameLayout.GONE);
    	if (previewContainer != null) previewContainer.setVisibility(FrameLayout.GONE);
    	if (containerWorkPhoto != null) containerWorkPhoto.setVisibility(FrameLayout.GONE);
    	if (containerSelectValue != null) containerSelectValue.setVisibility(FrameLayout.GONE);
    	if (containerQuestions != null) containerQuestions.setVisibility(FrameLayout.GONE);
    	if (containerAppSurvey != null) containerAppSurvey.setVisibility(FrameLayout.GONE);    	
    	if (scrollFeedback != null) scrollFeedback.setVisibility(FrameLayout.VISIBLE);
    	if (scrollSecchiDiskDepth != null) scrollSecchiDiskDepth.setVisibility(FrameLayout.GONE);
    	if (scrollPlasticFuValue != null) scrollPlasticFuValue.setVisibility(FrameLayout.GONE);
    }
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/* - - - -  S c r e e n     O r i e n t a t i o n  - - - - - */
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
    /*
     * Configure Layout for Portrait Configuration
     */
    private void orietationPortrait(){
    	try{
        	// General container
        	containerTakePhotoWidgets.setOrientation(LinearLayout.HORIZONTAL);
        	FrameLayout.LayoutParams lContainerTakePhotoWidgetsParms = (FrameLayout.LayoutParams)containerTakePhotoWidgets.getLayoutParams();
        	lContainerTakePhotoWidgetsParms.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        	containerTakePhotoWidgets.setLayoutParams(lContainerTakePhotoWidgetsParms);
        	
        	// Azimuth View
        	((LinearLayout.LayoutParams)btQuickHelp.getLayoutParams()).topMargin = (int)(0.0f * metrics.density);
        	((LinearLayout.LayoutParams)btQuickHelp.getLayoutParams()).leftMargin = (int)(15.0f * metrics.density);
        	((LinearLayout.LayoutParams)btQuickHelp.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;
        	
        	// Pitch Container
        	((LinearLayout.LayoutParams)containerPitchWidgets.getLayoutParams()).topMargin = (int)(0.0f * metrics.density);
        	((LinearLayout.LayoutParams)containerPitchWidgets.getLayoutParams()).leftMargin = (int)(15.0f * metrics.density);
        	
        	// Take Photo Button
        	((LinearLayout.LayoutParams)btObjetivo.getLayoutParams()).topMargin = (int)(0.0f * metrics.density);
        	((LinearLayout.LayoutParams)btObjetivo.getLayoutParams()).leftMargin = (int)(20.0f * metrics.density);
        	((LinearLayout.LayoutParams)btObjetivo.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;
    	
    		// Update Picture Size
    		getWindowManager().getDefaultDisplay().getMetrics(metrics);		
    		cameraLib.setPictureSizeFitScreen(metrics.widthPixels, metrics.heightPixels);

        	// Sets best preview size
        	int widthContainer = 0;
        	int heightcontainer = 0;
        	if ((previewContainer.getWidth() == 0) || (previewContainer.getHeight() == 0)){
        		widthContainer = metrics.widthPixels;
        		heightcontainer = metrics.heightPixels;    		
        	} else {
        		widthContainer = previewContainer.getWidth();
        		heightcontainer = previewContainer.getHeight();  
        		if (widthContainer > heightcontainer){
        			int exchg = widthContainer;
        			widthContainer = heightcontainer;
        			heightcontainer = exchg;
        		}
        	}
        	
        	// Update squares for take better photo zone.
        	// Portrait means three files with 2 squares for each        	
        	int h = metrics.heightPixels - getStatusBarHeight() - getTitleBarHeight();  
        	fuscaleSquare11.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare11.getLayoutParams().height = h / 3;
        	fuscaleSquare12.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare12.getLayoutParams().height = h / 3;
        	fuscaleSquare13.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare13.getLayoutParams().height = h / 3;        	
        	fuscaleSquare21.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare21.getLayoutParams().height = h / 3;
        	fuscaleSquare22.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare22.getLayoutParams().height = h / 3;
        	fuscaleSquare23.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare23.getLayoutParams().height = h / 3;
        	fuscaleSquare31.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare31.getLayoutParams().height = h / 3;
        	fuscaleSquare32.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare32.getLayoutParams().height = h / 3;
        	fuscaleSquare33.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare33.getLayoutParams().height = h / 3;
        	
        	// Set Visibility
        	fuscaleSquare11.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare12.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare13.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare21.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare22.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare23.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare31.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare32.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare33.setVisibility(FrameLayout.VISIBLE);
        	
    		// Reposition Fu Scale Bars
        	if ((panelFuScaleContainer != null) && (imgSelectValue != null)){
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) panelFuScaleContainer.getLayoutParams();
				layoutParams.setMargins(layoutParams.leftMargin, 140, 0, 0);
        	}
    	} catch (Exception e){
			// Error in Load Activity
			Toast.makeText(_this, getString(R.string.main_error_load_gen) + e.getMessage(), Toast.LENGTH_LONG).show();
    	} finally{
    		
    	}
    }
    /*
     * Configure Layout for Landscape Configuration
     */
    private void orientationLandscape(){
    	try{    					    		
        	// General container
        	containerTakePhotoWidgets.setOrientation(LinearLayout.VERTICAL);
        	FrameLayout.LayoutParams lContainerTakePhotoWidgetsParms = (FrameLayout.LayoutParams)containerTakePhotoWidgets.getLayoutParams();
        	lContainerTakePhotoWidgetsParms.gravity = Gravity.CENTER_VERTICAL| Gravity.RIGHT;
        	containerTakePhotoWidgets.setLayoutParams(lContainerTakePhotoWidgetsParms);

        	// Azimuth View
        	((LinearLayout.LayoutParams)btQuickHelp.getLayoutParams()).topMargin = (int)(15.0f * metrics.density);
        	((LinearLayout.LayoutParams)btQuickHelp.getLayoutParams()).leftMargin = (int)(0.0f * metrics.density);
        	((LinearLayout.LayoutParams)btQuickHelp.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;

        	// Pitch Container
        	((LinearLayout.LayoutParams)containerPitchWidgets.getLayoutParams()).topMargin = (int)(15.0f * metrics.density);
        	((LinearLayout.LayoutParams)containerPitchWidgets.getLayoutParams()).leftMargin = (int)(0.0f * metrics.density);

        	// Take Photo Button
        	((LinearLayout.LayoutParams)btObjetivo.getLayoutParams()).topMargin = (int)(20.0f * metrics.density);
        	((LinearLayout.LayoutParams)btObjetivo.getLayoutParams()).leftMargin = (int)(0.0f * metrics.density);
        	((LinearLayout.LayoutParams)btObjetivo.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;

    		// Update Picture Size
    		getWindowManager().getDefaultDisplay().getMetrics(metrics);		
    		cameraLib.setPictureSizeFitScreen(metrics.widthPixels, metrics.heightPixels);
    		
        	// Sets best preview size
        	int widthContainer = 0;
        	int heightcontainer = 0;
        	if ((previewContainer.getWidth() == 0) || (previewContainer.getHeight() == 0)){
        		widthContainer = metrics.widthPixels;
        		heightcontainer = metrics.heightPixels;    		
        	} else {
        		widthContainer = previewContainer.getWidth();
        		heightcontainer = previewContainer.getHeight();    		    		
        		if (heightcontainer > widthContainer){
        			int exchg = widthContainer;
        			widthContainer = heightcontainer;
        			heightcontainer = exchg;
        		}
        	}
        	
        	// Update squares for take better photo zone.
        	// Landscape means two files with 3 squares for each        
        	int h = metrics.heightPixels - getStatusBarHeight() - getTitleBarHeight();  
        	fuscaleSquare11.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare11.getLayoutParams().height = h / 3;
        	fuscaleSquare12.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare12.getLayoutParams().height = h / 3;
        	fuscaleSquare13.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare13.getLayoutParams().height = h / 3;
        	fuscaleSquare21.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare21.getLayoutParams().height = h / 3;
        	fuscaleSquare22.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare22.getLayoutParams().height = h / 3;
        	fuscaleSquare23.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare23.getLayoutParams().height = h / 3;
        	fuscaleSquare31.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare31.getLayoutParams().height = h / 3;
        	fuscaleSquare32.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare32.getLayoutParams().height = h / 3;
        	fuscaleSquare33.getLayoutParams().width = metrics.widthPixels / 3;
        	fuscaleSquare33.getLayoutParams().height = h / 3;
        	        	        	
        	// Set Visibility
        	fuscaleSquare11.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare12.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare13.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare21.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare22.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare23.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare31.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare32.setVisibility(FrameLayout.VISIBLE);
        	fuscaleSquare33.setVisibility(FrameLayout.VISIBLE);
        	
        	if (panelFuScaleContainer != null){
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) panelFuScaleContainer.getLayoutParams();
				layoutParams.setMargins(layoutParams.leftMargin, 140, 0, 0);
        	}
    	} catch (Exception e){
			// Error in Load Activity
			Toast.makeText(_this, getString(R.string.main_error_load_gen) + e.getMessage(), Toast.LENGTH_LONG).show();
    	} finally{
    		
    	}
    }    
    public int getStatusBarHeight() {
        Rect r = new Rect();
        Window w = getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);
        return r.top;
    }     
    public int getTitleBarHeight() {
        int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return (viewTop - getStatusBarHeight());
    }
    /*
     * Lock the screen rotation
     */
    private void lockScreenRotationPortrait(){
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }    
    /*
     * Lock the screen rotation
     */
    public void lockScreenRotation(){
    	int actualScreenOrientation = getRequestedOrientation();
    	if (_this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
    		if (actualScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
    			  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    		}
    	} else if (_this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
      		if (actualScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
  			  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      		}
    	}
    }
    /*
     * Unlock screen rotation
     */
    private void unlockScreenRotation(){    	
    	int actualScreenOrientation = getRequestedOrientation();
    	if (actualScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED){
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    	}
    }
	/* - - - - - - - - - - - - - - - - - - - */
	/* - - - -  L i s t e n e r s  - - - - - */
	/* - - - - - - - - - - - - - - - - - - - */
	/*
	 * Listener of Best Location Changed
	 */
    LocationUtils.newBestLocationListener newBestLocListener = new LocationUtils.newBestLocationListener() {		
		@Override
		public void newBestLocation(Location currentBestLocation) {
			if (!bLocationCanceled) {
				if (mLocationUtils != null){
					if (mLocationUtils.isLocationEnabled()){
						// if location has accuracy, store them
						if (currentBestLocation.hasAccuracy()){
							if (Math.abs(currentBestLocation.getAccuracy()) < METERS_ACCURACY_GOOD_POSITION){
								bestLatLocation = currentBestLocation.getLatitude();
								bestLongLocation = currentBestLocation.getLongitude();								
								// We have accuracy. If we're waiting, cancel dialog
								if (progressPositionDialog != null){
									progressPositionDialog.cancel();
									switchToStat(ACTIVITY_STATE.TAKE_PHOTO);
									progressPositionDialog = null;
								}
							} else {
								bestLatLocation = Double.MIN_VALUE;
								bestLongLocation = Double.MIN_VALUE;
							}
						}
					}						
				}
				
					
//					// Gets the new azimuth from location
//					int newAzimuthValue = (int)calculateSunPosition(mLocationUtils.getLatitude(), mLocationUtils.getLongitude());
//										
//					if (Math.abs(newAzimuthValue - fuScaleSensors.getAzimuthDegrees()) > AZIMUTH_DEGREE_TO_CHANGE_VALUE){
//						fuScaleSensors.setSunAzimuthDegreesManual(newAzimuthValue);
//					}
//					
//					// Cancel progress on Loading if location has accuracy
//					if (progressPositionSunDialog != null){
//						if (currentBestLocation.hasAccuracy()){
//							if (Math.abs(currentBestLocation.getAccuracy()) < METERS_ACCURACY_GOOD_POSITION){
//								if (progressPositionSunDialog != null){
//									progressPositionSunDialog.cancel();
//									switchToStat(ACTIVITY_STATE.TAKE_PHOTO);
//									progressPositionSunDialog = null;
//								}
//							}
//						}
//					}
//					if (progressPositionSunDialog != null){
//						// If not cancelled, check autocancel
//						if (((System.nanoTime() - startTProgressTime) / 1000000000) >  SECONDS_PROGRESS_AUTOCANCEL){
//							progressPositionSunDialog.cancel();
//							if (activityState == ACTIVITY_STATE.SUN_POSITION) switchToStat(ACTIVITY_STATE.SHOW_PANEL_SUN);
//							progressPositionSunDialog = null;
//						}
//					}
					
					
					
				
			}
		}
	}; 
	/*
	 * Listener of Sensor Values Changed
	 */
	SensorEventListener mSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (activityState == ACTIVITY_STATE.TAKE_PHOTO) {
				// Set the pinch value
				if (fuScaleSensors != null) txtPitchValue.setText(String.format("%2s", fuScaleSensors.getPitchDegrees() + "").replace(' ', '0'));
								
//				// get if Positioned
//				boolean bIsPositioned = false;
//				if (fuScaleSensors.getNumberOfSensors() == 0) bIsPositioned = true;
//				else {
//					if 	((fuScaleSensors.getPitchDegrees() <= (BEST_PITCH_DEGREE + MARGIN_BEST_PITCH_DEGREE)) &&
//						(fuScaleSensors.getPitchDegrees() >= (BEST_PITCH_DEGREE - MARGIN_BEST_PITCH_DEGREE))){
//						bIsPositioned = true;
//					}
//				}
//				
//				// Update Button Take Photo status
//				//if (azimuthView.IsPositioned()){
//				if (bIsPositioned){
//					// Enable Photo Button
//					btObjetivo.setEnabled(true);
//				} else {
//					// Disable Photo Button
//					btObjetivo.setEnabled(false);
//				}
			}
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};
	/*
	 * Listener of button to take Photo
	 */
	OnClickListener mListenerTakePhoto = new OnClickListener() {			
		@Override
		public void onClick(View v) {
			try{
//				// Disable button
//				btObjetivo.setEnabled(false);
//				
				// Take the photo
				onClickTakePhoto(v);
			} catch (Exception e){
				Toast.makeText(_this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	};	
	/*
	 * Listener of button to select Square Photo
	 */
	OnClickListener listenerSelectSquareButton = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			try{
				// Recycle Bitmaps of possible other photos			
				if (bmpCroppedPhoto != null) {
					bmpCroppedPhoto.recycle();
					bmpCroppedPhoto = null;
				}
								
				// Get the cropped Bitmap 
//				int imgCroppedX = ((fuscaleSquare22.getWidth() + containerWorkPhotoSquare.getLeft()) * bmpPhotoTaken.getWidth()) / imgWorkPhoto.getWidth();
//				int imgCroppedY = ((fuscaleSquare22.getHeight() + containerWorkPhotoSquare.getTop()) * bmpPhotoTaken.getHeight()) / imgWorkPhoto.getHeight();
//				int imgCroppedWidth = (fuscaleSquare22.getWidth() * bmpPhotoTaken.getWidth()) / imgWorkPhoto.getWidth();
//				int imgCroppedHeight = (fuscaleSquare22.getHeight() * bmpPhotoTaken.getHeight()) / imgWorkPhoto.getHeight();
				
				int imgCroppedX = fuscaleSquare22.getWidth() + containerWorkPhotoSquare.getLeft();
				int imgCroppedY = fuscaleSquare22.getHeight() + containerWorkPhotoSquare.getTop() + getTitleBarHeight();
				int imgCroppedWidth = fuscaleSquare22.getWidth();
				int imgCroppedHeight = fuscaleSquare22.getHeight();

				
				
				if (imgCroppedX < 0) imgCroppedX = 0;
				if (imgCroppedY < 0) imgCroppedY = 0;
				if (imgCroppedWidth > bmpPhotoTaken.getWidth()) imgCroppedWidth = bmpPhotoTaken.getWidth(); 
				if (imgCroppedHeight > bmpPhotoTaken.getHeight()) imgCroppedHeight = bmpPhotoTaken.getHeight(); 				
				bmpCroppedPhoto = Bitmap.createBitmap(bmpPhotoTaken, imgCroppedX, imgCroppedY, imgCroppedWidth, imgCroppedHeight);

				// Recycle the big photo
				if (bmpPhotoTaken != null) {
					bmpPhotoTaken.recycle();
					bmpPhotoTaken = null;
				}

				// Adjust the ImageView Size to Cropped Image Size, and assign photo
				double factorFromScreenDimensions = 0.80d;
				double fxImgCropped = Math.min(((double)metrics.heightPixels * factorFromScreenDimensions) / (double)bmpCroppedPhoto.getHeight(), ((double)metrics.widthPixels * factorFromScreenDimensions) / (double)bmpCroppedPhoto.getWidth())  ;
				if (fxImgCropped < 1.0d) fxImgCropped = 1.0d;
				imgSelectValue.getLayoutParams().width = (int)((double)bmpCroppedPhoto.getWidth() * fxImgCropped);
				imgSelectValue.getLayoutParams().height = (int)((double)bmpCroppedPhoto.getHeight() * fxImgCropped);
//				imgSelectValue.getLayoutParams().width = LayoutParams.MATCH_PARENT;
//				imgSelectValue.getLayoutParams().height = LayoutParams.MATCH_PARENT;
//				imgSelectValue.getLayoutParams().width = bmpCroppedPhoto.getWidth();
//				imgSelectValue.getLayoutParams().height = bmpCroppedPhoto.getHeight();
//				imgSelectValue.setScaleType(ScaleType.FIT_CENTER);
				((FrameLayout.LayoutParams)imgSelectValue.getLayoutParams()).gravity = Gravity.CENTER;
				
				imgSelectValue.setImageBitmap(bmpCroppedPhoto);
								
				// Save the cropped photo
				String filePathJPG = baseFullPathTemp + LastPictureNameMetadata + ".jpg";
				FileOutputStream outStream = new FileOutputStream(filePathJPG);            
				bmpCroppedPhoto.compress(CompressFormat.JPEG, 90, outStream);
				outStream.flush();
				outStream.close();

				// Change the Activity Stat
				switchToStat(ACTIVITY_STATE.SELECT_FUSCALE_VALUE);
			} catch (Exception e){
				// Show error. Don't get next state
				Toast.makeText(_this, getString(R.string.main_panelsquare_load_photo_error) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
			} finally{
				
			}			
		}		    		
	};
	/*
	 * Listener of button to Select FU Value
	 */
	OnClickListener listenerSelectFuValueButton = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			try {
				if (askForPlasticScale){
					// Ask For Plastic Fu Scale
					switchToStat(ACTIVITY_STATE.PLASTIC_FUSCALE_ASK);
				} else {
					// Don't ask for plastic Fu Scale
					bUserHasPlasticFUScale = false;
					switchToStat(ACTIVITY_STATE.QUESTIONARY);			
				}
			} catch (Exception e){
				// Show error. Don't get next state
				Toast.makeText(_this, getString(R.string.main_select_fuvalue_general_error) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
			} finally{
				
			}			
		}		    		
	};
	/*
	 * Tap to See Bottom Panel
	 */
	public void onQuestionBottomClick(View view) throws Exception{
		try {
			// Create custom border
			CustomBorderDrawable c = new CustomBorderDrawable(new RectShape());
			c.setStrokeColour(Color.rgb(76, 164, 188));
			c.setFillColour(Color.WHITE);
			c.setStrokeWidth(6);
												
			// Update Layouts UI
			if (view.equals(panelQuestionBottomYes)){
				seeBottomValue = SEE_BOTTOM.YES;
						    	
		    	// Update Backgrounds
		    	panelQuestionBottomYes.setBackgroundDrawable(c);
		    	panelQuestionBottomNo.setBackgroundColor(Color.TRANSPARENT);
		    	
				// Update Image Opacity
		    	imageQuestionBottomYes.setAlpha(1.0f);
		    	imageQuestionBottomNo.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
			} else if (view.equals(panelQuestionBottomNo)){
				seeBottomValue = SEE_BOTTOM.NO;
						    	
		    	// Update Backgrounds
		    	panelQuestionBottomYes.setBackgroundColor(Color.TRANSPARENT);
		    	panelQuestionBottomNo.setBackgroundDrawable(c);
		    	
				// Update Image Opacity
		    	imageQuestionBottomYes.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
		    	imageQuestionBottomNo.setAlpha(1.0f);
			} 
			
			// Update Status Buttons
			updateButtonsQuestionsUI();

		} catch (Exception e){
			// Show error. Don't get next state
			Toast.makeText(_this, getString(R.string.menu_activity_error_load_gen) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
		} finally{
			
		}			
	}	
	/*
	 * Tap to Raining Panel
	 */
	public void onQuestionRainingClick(View view) throws Exception{
		try {
			// Create custom border
			CustomBorderDrawable c = new CustomBorderDrawable(new RectShape());
			c.setStrokeColour(Color.rgb(76, 164, 188));
			c.setFillColour(Color.WHITE);
			c.setStrokeWidth(6);
									
			// Update Layouts UI
			if (view.equals(panelQuestionRainingYes)){
		    	rainingValue = RAINING.YES;
		    	
		    	// Update Backgrounds
				panelQuestionRainingYes.setBackgroundDrawable(c);
				panelQuestionRainingNo.setBackgroundColor(Color.TRANSPARENT);

				// Update Image Opacity
				imageQuestionRainingYes.setAlpha(1.0f);
				imageQuestionRainingNo.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
			} else if (view.equals(panelQuestionRainingNo)){
		    	rainingValue = RAINING.NO;
		    	
		    	// Update Backgrounds
				panelQuestionRainingYes.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionRainingNo.setBackgroundDrawable(c);
				
				// Update Image Opacity
				imageQuestionRainingYes.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionRainingNo.setAlpha(1.0f);
			}
			
			// Update Status Buttons
			updateButtonsQuestionsUI();
		} catch (Exception e){
			// Show error. Don't get next state
			Toast.makeText(_this, getString(R.string.menu_activity_error_load_gen) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
		} finally{
			
		}			
	}
	/*
	 * Tap to Cloud Fraction Panel
	 */
	public void onCloudFractionClick(View view) throws Exception{
		try {
			// Create custom border
			CustomBorderDrawable c = new CustomBorderDrawable(new RectShape());
			c.setStrokeColour(Color.rgb(76, 164, 188));
			c.setFillColour(Color.WHITE);
			c.setStrokeWidth(6);
			
			// Update Layouts UI
			if (view.equals(panelQuestionCloudClear)){
		    	cloudFractionValue = CLOUD_FRACTION.CLEAR;
		    	
		    	// Update Backgrounds
				panelQuestionCloudClear.setBackgroundDrawable(c);
				panelQuestionCloudFew.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudScattered.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudBroken.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudOvercast.setBackgroundColor(Color.TRANSPARENT);
				
				// Update Image Opacity
				imageQuestionCloudClear.setAlpha(1.0f);
				imageQuestionCloudFew.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudScattered.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudBroken.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudOvercast.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);				
			} else if (view.equals(panelQuestionCloudFew)){
		    	cloudFractionValue = CLOUD_FRACTION.FEW;
		    	
		    	// Update Backgrounds
				panelQuestionCloudClear.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudFew.setBackgroundDrawable(c);
				panelQuestionCloudScattered.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudBroken.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudOvercast.setBackgroundColor(Color.TRANSPARENT);
				
				// Update Image Opacity
				imageQuestionCloudClear.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudFew.setAlpha(1.0f);
				imageQuestionCloudScattered.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudBroken.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudOvercast.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);				
			} else if (view.equals(panelQuestionCloudScattered)){
		    	cloudFractionValue = CLOUD_FRACTION.SCATERED;
		    	
		    	// Update Backgrounds
				panelQuestionCloudClear.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudFew.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudScattered.setBackgroundDrawable(c);
				panelQuestionCloudBroken.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudOvercast.setBackgroundColor(Color.TRANSPARENT);
				
				// Update Image Opacity
				imageQuestionCloudClear.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudFew.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudScattered.setAlpha(1.0f);
				imageQuestionCloudBroken.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudOvercast.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);				
			} else if (view.equals(panelQuestionCloudBroken)){
		    	cloudFractionValue = CLOUD_FRACTION.BROKEN;
		    	
		    	// Update Backgrounds
				panelQuestionCloudClear.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudFew.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudScattered.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudBroken.setBackgroundDrawable(c);
				panelQuestionCloudOvercast.setBackgroundColor(Color.TRANSPARENT);
				
				// Update Image Opacity
				imageQuestionCloudClear.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudFew.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudScattered.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudBroken.setAlpha(1.0f);
				imageQuestionCloudOvercast.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);				
			} else if (view.equals(panelQuestionCloudOvercast)){
		    	cloudFractionValue = CLOUD_FRACTION.OVERCAST;
		    	
		    	// Update Backgrounds
				panelQuestionCloudClear.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudFew.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudScattered.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudBroken.setBackgroundColor(Color.TRANSPARENT);
				panelQuestionCloudOvercast.setBackgroundDrawable(c);
				
				// Update Image Opacity
				imageQuestionCloudClear.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudFew.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudScattered.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudBroken.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
				imageQuestionCloudOvercast.setAlpha(1.0f);				
			}
			
			// Update Status Buttons
			updateButtonsQuestionsUI();

		} catch (Exception e){
			// Show error. Don't get next state
			Toast.makeText(_this, getString(R.string.menu_activity_error_load_gen) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
		} finally{
			
		}			
	}
	/*
	 * Touch of Square Selection
	 */
	
	private int _xDeltaSquareSelection;
	private int _yDeltaSquareSelection;
	
	View.OnTouchListener touchSquareSelection = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			final int X = (int) event.getRawX();
		    final int Y = (int) event.getRawY();
		    switch (event.getAction() & MotionEvent.ACTION_MASK) {
		        case MotionEvent.ACTION_DOWN:
		            FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) containerWorkPhotoSquare.getLayoutParams();
		            _xDeltaSquareSelection = X - lParams.leftMargin;
		            _yDeltaSquareSelection = Y - lParams.topMargin;
		            break;
		        case MotionEvent.ACTION_UP:
		            break;
		        case MotionEvent.ACTION_POINTER_DOWN:
		            break;
		        case MotionEvent.ACTION_POINTER_UP:
		            break;
		        case MotionEvent.ACTION_MOVE:
		        	FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) containerWorkPhotoSquare.getLayoutParams();
		            layoutParams.leftMargin = X - _xDeltaSquareSelection;
		            layoutParams.topMargin = Y - _yDeltaSquareSelection;
		            containerWorkPhotoSquare.setLayoutParams(layoutParams);
		            break;
		    }
		    return true;		}                         
	};
	/*
	 * Touch of FuScale
	 */
	View.OnTouchListener touchFuScale = new View.OnTouchListener() {                          
		public boolean onTouch(View view, MotionEvent event) {
			final int X = (int) event.getRawX();
			final int Y = (int) event.getRawY();
			boolean retorn = false;
			try{
				if (event.getAction() == MotionEvent.ACTION_DOWN){					// Get the data Tap
					TapCurrentX = (int) event.getRawX();
					TapCurrentY = (int) event.getRawY();										            
					IsTap = true;				// Initially we think we are doing a Tap
					retorn = true;				//We want to continue the event on View. True means that view has consumed the event.

					// We obtain data on movement
					xDelta = X - ((RelativeLayout.LayoutParams)panelFuScaleContainer.getLayoutParams()).leftMargin;
					yDelta = Y - ((RelativeLayout.LayoutParams)panelFuScaleContainer.getLayoutParams()).topMargin;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE){
					if (IsTap == false ){
						// We have detected that it was a Tap. Therefore it is a movement
						RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) panelFuScaleContainer.getLayoutParams();
						if (Y - yDelta > - 450){
							//--> Nomes moviment horitzontal 	layoutParams.setMargins(X - xDelta, Y - yDelta, 0, 0);
							layoutParams.setMargins(X - xDelta, layoutParams.topMargin, 0, 0);
							panelFuScaleContainer.setLayoutParams(layoutParams);
							marcoFuScaleSeekBar.setProgress(-(X - xDelta));
						}
						// Continue as if they had consumed event
						retorn = true;
					} else {
						// Although it may be a Tap. We look at the distance of movement
						float TmpTapCurrentX = (int) event.getRawX();
						float TmpTapCurrentY = (int) event.getRawY();

						//If you had done Tap in charge of project
						if ((TapCurrentX == 0.0f) && (TapCurrentY == 0.0f)){
							TapCurrentX = (int)TmpTapCurrentX;
							TapCurrentY = (int) TmpTapCurrentY;		
						}
						if ((Math.abs(TmpTapCurrentX - TapCurrentX) + Math.abs(TmpTapCurrentY - TapCurrentY)) > NUM_PIXELS_MOVE){
							//If you have moved over NUM_PIXELS_MOVE, decide that it is not a Tap
							IsTap = false;
							retorn = false;
						} else {
							//Possibly a Tap
							retorn = true;
						}	
					}					
				} else if (event.getAction() == MotionEvent.ACTION_UP){	
					if (IsTap){			
						//It definitely has been a Tap about view						
						FUBarContainerClick(view);	
					} 

					//Initialize Down the Tap
					TapCurrentX = TapCurrentY = 0.0f;					
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL){	
					//Initialize Down the Tap
					TapCurrentX = TapCurrentY = 0.0f;
				}	

			} catch (Exception e){
				Toast.makeText(FUScaleActivityExt.this, "Touch Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			} finally{

			}
			return retorn; 
		}
	};	
	/*
	 * Action click on a Bar of FU Scale or Container
	 */		
	private void FUBarContainerClick(View view) throws Exception{
		for (int i = 0; i < panelFuScaleContainer.getChildCount(); i++){
			View viewBar = panelFuScaleContainer.getChildAt(i);
			int idBarra = Integer.parseInt(viewBar.getTag().toString());
			if (viewBar == view){
				// Toggle of currentState
				if (estatsBarres[idBarra]){
					estatsBarres[idBarra] = false;
					setFUValue(Integer.MIN_VALUE);
					viewBar.setBackgroundColor(colorFonsBar);
				} else {
					estatsBarres[idBarra] = true;
					setFUValue((Integer.parseInt(viewBar.getTag().toString()) + 1));
					CustomBorderDrawable c = new CustomBorderDrawable(new RectShape());
					c.setFillColour(Color.RED);
					viewBar.setBackgroundDrawable(c);
				}
			} else {
				// Disable Bar
				estatsBarres[idBarra] = false;				
				viewBar.setBackgroundColor(colorFonsBar);				
			}
		}
	}
	/*
	 * Tap to FeedBack Panel. Hide It
	 */
	public void onClickPanelFeedBack(View view) throws Exception{
		// Back to Main Menu		
		exitActivity();
	}
	/*
	 * Tap to FeedBack Panel. Hide It
	 */
	public void onClickPanelFeedBackLink(View view) throws Exception{
		// Go to the citclops web		
		if (workingDialog == null) workingDialog = kTransparentWorkingDialog.show(FUScaleActivityExt.this, null, null);
		Intent intent = new Intent(_this, ViewCitclopsResultsActivity.class);
		startActivity(intent);
	}
	/*
	 * Sets de FuValue Selected
	 */
	private void setFUValue(Integer fuvalue){		
		FUValue = fuvalue;
		if ((FUValue == null) || (FUValue == Integer.MIN_VALUE)) {
			btSelectFUValueOK.setEnabled(false);
			metadata.setFUValue(0);
		} else {
			btSelectFUValueOK.setEnabled(true);
			metadata.setFUValue(fuvalue);						
		}
	}
	/*
	 * Back to Take Photo
	 */
	public void onClickQuestionsBack(View view){
		// Back to take Photo
		switchToStat(ACTIVITY_STATE.TAKE_PHOTO);
	}
	/*
	 * Go to FeedBack
	 */
	public void onClickAppSurveyBack(View view){
		// Back to Feedback
		switchToStat(ACTIVITY_STATE.FEEDBACK);
	}
	/*
	 * Send Button to finish data
	 */
	public void onClickQuestionsSend(View view) throws Exception{
		//Passing data to create the xml file	
		boolean bContinue = true;
		if (cloudFractionValue == CLOUD_FRACTION.CLEAR) 		metadata.setCloudFraction(FUScaleMetadata.CloudFractionValues.Clear);
		else if (cloudFractionValue == CLOUD_FRACTION.FEW) 		metadata.setCloudFraction(FUScaleMetadata.CloudFractionValues.Few);
		else if (cloudFractionValue == CLOUD_FRACTION.SCATERED) metadata.setCloudFraction(FUScaleMetadata.CloudFractionValues.Scattered);
		else if (cloudFractionValue == CLOUD_FRACTION.BROKEN) 	metadata.setCloudFraction(FUScaleMetadata.CloudFractionValues.Broken);
		else if (cloudFractionValue == CLOUD_FRACTION.OVERCAST) metadata.setCloudFraction(FUScaleMetadata.CloudFractionValues.Overcast);
		else bContinue = false;
					
		// Set Raining Value
		if (rainingValue == RAINING.YES) 						metadata.setRain(FUScaleMetadata.RainFractionValues.Yes);	
		else if (rainingValue == RAINING.NO) 					metadata.setRain(FUScaleMetadata.RainFractionValues.No);	
		else bContinue = false;
		
		// Set Bottom Value
		if (seeBottomValue == SEE_BOTTOM.YES) 					metadata.setBottom(FUScaleMetadata.BottomFractionValues.Yes);	
		else if (seeBottomValue == SEE_BOTTOM.NO) 				metadata.setBottom(FUScaleMetadata.BottomFractionValues.No);	
		else bContinue = false;
		
		// Set profile code
		metadata.setProfile(sProfile);
		
		// Set if user has SecchiDisk and estimated depth
		metadata.setSecchiDiskInfo(bUserHasSecchiDisk, fSecchiDiskDepth);
		
		// Set if user has a Plastic FuScale and if yes, the value
		metadata.setPlasticFuInfo(bUserHasPlasticFUScale, iPlasticFuValue);
		
		if (!bContinue){
			AlertDialog.Builder avisoSelectValue = new AlertDialog.Builder(FUScaleActivityExt.this);
			avisoSelectValue.setTitle(getString(R.string.main_panel_questions_no_value_title));
			avisoSelectValue.setMessage(getString(R.string.main_panel_questions_no_value_message)          );
			avisoSelectValue.setCancelable(false);
			avisoSelectValue.setNeutralButton(getString(R.string.main_panel_questions_plastic_fuscale_ask_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();																	
				}
			});
			avisoSelectValue.show();
		} else {			
			if (!chkPlaying.isChecked()){
				//Creating the metadata file into temp path
				metadata.Save(baseFullPathTemp, LastPictureNameMetadata, "jpg");
				
				// Create ZIP file and delete image and XML 
				String zipFile = baseFullPathTemp + "/" + LastPictureNameMetadata + ".zip";
				File f = new File(baseFullPathTemp);			
				if (f != null){
					File[] filesList = f.listFiles();
					if (filesList != null){
						if (filesList.length > 0){
							// Zip files in z single zip
							String files[] = new String[filesList.length];
							for (int i = 0; i < files.length; i++) files[i] = filesList[i].getAbsolutePath();
							ZipUtility.zip(files, zipFile);
							
							// Delete not ZIP files
							for (int i = 0; i < files.length; i++) filesList[i].delete();
						}
					}					
				}
				
				// Copy files to prepare To send folder
				FileUtils.copyDirectory(TemporalDirectory, TmpDirectory);			
				FileUtils.DeleteDirectory(TemporalDirectory);
			}
						
			// Go to FeedBack Panel
			switchToStat(ACTIVITY_STATE.APP_SURVEY_ASK);
		}
	}
	/*
	 * Send Survey Button
	 */
	public void onClickAppSurveySend(View view) {
		boolean bContinue = true;
		if ((!appsurveyEasytouseYes.isChecked()) && (!appsurveyEasytouseNo.isChecked())) bContinue = false; 
		if ((!appsurveyMatchBarsYes.isChecked()) && (!appsurveyMatchBarsNo.isChecked())) bContinue = false; 
		if ((!appsurveyLookWaterWater.isChecked()) && (!appsurveyLookWaterPhoto.isChecked())) bContinue = false; 
		if ((!appsurveyDesignYes.isChecked()) && (!appsurveyDesignNo.isChecked())) bContinue = false; 
		if ((!appsurveySecchiWith.isChecked()) && (!appsurveySecchiWithout.isChecked()) && (!appsurveySecchiDidnotuseit.isChecked())) bContinue = false; 
		
		if (!bContinue){
			AlertDialog.Builder avisoSelectValue = new AlertDialog.Builder(FUScaleActivityExt.this);
			avisoSelectValue.setTitle(getString(R.string.main_panel_appsurvey_no_value_title));
			avisoSelectValue.setMessage(getString(R.string.main_panel_appsurvey_no_value_message)          );
			avisoSelectValue.setCancelable(false);
			avisoSelectValue.setNeutralButton(getString(R.string.main_panel_questions_plastic_fuscale_ask_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();																	
				}
			});
			avisoSelectValue.show();
		} else {
			// Create File Name
			//String sFileName = "SURVEY_" + DispositiuFisic.getID(_this) + "." + System.currentTimeMillis() + ".txt";
			String sFileName = "SURVEY_" + LastPictureNameMetadata + ".txt";
			
			// Create File Content
			String sSurvey = "";
			if (appsurveyEasytouseYes.isChecked()) sSurvey += "Easy to Use: Yes\n";
			else if (appsurveyEasytouseNo.isChecked()) sSurvey += "Easy to Use: No\n";	
			if (appsurveyMatchBarsYes.isChecked()) sSurvey += "Match Bars: Yes\n";
			else if (appsurveyMatchBarsNo.isChecked()) sSurvey += "Match Bars: No\n";			
			if (appsurveyLookWaterWater.isChecked()) sSurvey += "Look water or Photo: Water\n";
			else if (appsurveyLookWaterPhoto.isChecked()) sSurvey += "Look water or Photo: Photo\n";
			if (appsurveyDesignYes.isChecked()) sSurvey += "Like Design: Yes\n";
			else if (appsurveyDesignNo.isChecked()) sSurvey += "Like Design: No\n";
			if (appsurveySecchiWith.isChecked()) sSurvey += "More easy WITH Secchi Disk\n";
			else if (appsurveySecchiWithout.isChecked()) sSurvey += "More easy WITHOUT Secchi Disk\n";
			else if (appsurveySecchiDidnotuseit.isChecked()) sSurvey += "Did not use Secchi Disk\n";
			
			sSurvey += "Suggestions: " + editSuggestion.getText().toString() +  "\n";
			
			// Create File
			try{
				// Create Directories
				TmpDirectory = new File(baseFullPath);
				TemporalDirectory = new File(baseFullPathTemp);		
				FileUtils.DeleteDirectory(TemporalDirectory);
				if (!TmpDirectory.exists()) TmpDirectory.mkdirs();
				if (!TemporalDirectory.exists()) TemporalDirectory.mkdirs();

				// Create File
		        File gpxfile = new File(TemporalDirectory, sFileName);
		        FileWriter writer = new FileWriter(gpxfile);
		        writer.append(sSurvey);
		        writer.flush();
		        writer.close();
							
				// Copy files to prepare To send folder
				FileUtils.copyDirectory(TemporalDirectory, TmpDirectory);			
				FileUtils.DeleteDirectory(TemporalDirectory);
			} catch (Exception e){
				// Error to generate Survey. Don't send 
				Toast.makeText(_this, getString(R.string.main_panel_appsurvey_error_generate) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
			} finally{
				
			}
			
			// Go to FeedBack State
			switchToStat(ACTIVITY_STATE.FEEDBACK);			
		}
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - */
	/* - - - -  S u n     P o s i t i o n  - - - - - */
	/* - - - - - - - - - - - - - - - - - - - - - - - */
	/*
	 * Calculate Sun Position by Time and GeoLocation 
	 */
	private double calculateSunPosition(double latitude, double longitude){
		// GregorianCalendar time = new GregorianCalendar(new SimpleTimeZone(-7 * 60 * 60 * 1000, "LST"));
		// time.set(2003, 9, 17, 12, 30, 30); // 17 October 2003, 12:30:30 LST-07:00
		double retorn = -999.0d;
		try{
			TimeZone.getDefault();
			GregorianCalendar time = new GregorianCalendar(TimeZone.getDefault());			
			time.set(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND)); // 17 October 2003, 12:30:30 LST-07:00
			AzimuthZenithAngle azmang = PSA.calculateSolarPosition(time, latitude, longitude);
			if (azmang != null) retorn = azmang.getAzimuth();
		} catch (Exception e){
			retorn = -999.0d;	
		} finally {			
			
		}
		return retorn;
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - */
	/* - - - -  F U    S c a l e     B a r   - - - - - - */
	/* - - - - - - - - - - - - - - - - - - - - - - - - - */
	/*
	 * Create the Bars of the Fu Scale
	 */
	private void createFuScaleBar(){
		// Width of container
		int sizeWidthBarsContainer = 0;
		
		// Create Fu Scale Container
		panelFuScaleContainer = new LinearLayout(this);
		panelFuScaleContainer.setOrientation(LinearLayout.HORIZONTAL);
		panelFuScaleContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));		
		panelFuScaleContainer.setOnTouchListener(touchFuScale);
		
		// Load Panel to Container
		marcoFuScaleContainer.removeAllViews();	
		marcoFuScaleContainer.addView(panelFuScaleContainer);
					
		// Get Display Metrics
		if (metrics == null) metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);		

		// Bar sizes 
		//int heightBarra = (int)(0.50d * (double)metrics.heightPixels );
		int heightContenidorN = (int)(50.0f * metrics.density);
		final int sizePadding = (int)(5.0f * metrics.density);
		final int heightInstructions = (int)((float)100 *  metrics.density);
		int heightBarra = (int)metrics.heightPixels - (int)getStatusBarHeight() - (int)getTitleBarHeight() - heightInstructions - heightContenidorN - (int)(60 * metrics.density);
		((RelativeLayout.LayoutParams)panelFuScaleContainer.getLayoutParams()).topMargin = heightInstructions + (int)(5 * metrics.density);
		
		// Adjust the width bar to the width of cropped photo
		int widthBarra = 0;
//		if (imgSelectValue != null) widthBarra = (imgSelectValue.getLayoutParams().width / 3) + (2 * sizePadding);
//		else widthBarra = (((Math.min(metrics.widthPixels, metrics.heightPixels) / 3) - sizePadding - sizePadding - leftMargin) / 3) ;
//		widthBarra = (Math.min(metrics.widthPixels, metrics.heightPixels)) / 3;
		widthBarra = metrics.widthPixels / 5;
		int leftMargin = widthBarra;
		
		//Load the parameters specified in configuration
		dataFuScaleBars.Load();
		
		// for each bar
		for (int i = 0; i < 21; i++){
			// Panel color
			LinearLayout pnlColor = new LinearLayout(this);
			pnlColor.setLayoutParams(new LinearLayout.LayoutParams(widthBarra, heightBarra));
			pnlColor.setBackgroundColor(Color.WHITE);
			pnlColor.setOrientation(LinearLayout.VERTICAL);
			
			RelativeLayout pnlColorTop = new RelativeLayout(this);
			pnlColorTop.setBackgroundColor(getFUColor(i));
			LinearLayout.LayoutParams lytParamsTop = new LinearLayout.LayoutParams(widthBarra, heightBarra);
			lytParamsTop.weight = 1;
			pnlColor.addView(pnlColorTop, lytParamsTop);
									
			RelativeLayout pnlColorMedium = new RelativeLayout(this);
			pnlColorMedium.setBackgroundColor(getFUColor_M(i));
			LinearLayout.LayoutParams lytParamsMedium = new LinearLayout.LayoutParams(widthBarra, heightBarra);
			lytParamsMedium.weight = 1;			
			pnlColor.addView(pnlColorMedium, lytParamsMedium);
			
			RelativeLayout pnlColorBottom = new RelativeLayout(this);
			pnlColorBottom.setBackgroundColor(getFUColor_B(i));
			LinearLayout.LayoutParams lytParamsBottom = new LinearLayout.LayoutParams(widthBarra, heightBarra);
			lytParamsBottom.weight = 1;			
			pnlColor.addView(pnlColorBottom, lytParamsBottom);
			
			// Panel container FU text value
			RelativeLayout pnlN = new RelativeLayout(this);
			pnlN.setLayoutParams(new RelativeLayout.LayoutParams(widthBarra, heightContenidorN));
			pnlN.setBackgroundColor(Color.DKGRAY);

			// TextView Fu Value
			TextView contNum = new TextView(this);
			contNum.setTextColor(Color.WHITE);
			contNum.setTextSize(metrics.scaledDensity * 14);
			contNum.setText(String.format("%02d", i + 1));
			contNum.setGravity(Gravity.CENTER);
			pnlN.addView(contNum, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			// General Container
			LinearLayout pnlC = new LinearLayout(this);
			pnlC.setOrientation(LinearLayout.VERTICAL);
			
			pnlC.setPadding(sizePadding, sizePadding, sizePadding, sizePadding);
			LinearLayout.LayoutParams lytMainParams = new LinearLayout.LayoutParams(widthBarra + (2 * sizePadding), heightBarra + sizePadding + heightContenidorN);
						
			if (i == 0) lytMainParams.leftMargin = 0;
			else lytMainParams.leftMargin = leftMargin;
			pnlC.setLayoutParams(lytMainParams);
			pnlC.addView(pnlColor);
			pnlC.addView(pnlN);
			pnlC.setTag(i);
			pnlC.setOnTouchListener(touchFuScale);
								
			//Introduced as the bars are visible or not.			
			if(dataFuScaleBars.getVisibleFuscale()[i] == true){
				panelFuScaleContainer.addView(pnlC);
				sizeWidthBarsContainer += leftMargin + widthBarra;
			}
		}

		// Add SeekBar
		RelativeLayout.LayoutParams marcoFuScaleSeekBarLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		marcoFuScaleSeekBarLayoutParams.leftMargin = (int)(15.0f * metrics.density);
		marcoFuScaleSeekBarLayoutParams.rightMargin = (int)(15.0f * metrics.density);
		marcoFuScaleSeekBarLayoutParams.topMargin = metrics.heightPixels - (int)getTitleBarHeight() - (int)(80 * metrics.density); 
		marcoFuScaleContainer.addView(marcoFuScaleSeekBar, marcoFuScaleSeekBarLayoutParams);
		marcoFuScaleSeekBar.setMax(sizeWidthBarsContainer);

		// Update the status bar
		estatsBarres = new boolean[21];
		inicialitzaColorsEstatsBarres();
	}
	/*
	 * Gets FU Color from bar number
	 */
	private int getFUColor(int numBarra){
		return Color.rgb(dataFuScaleBars.getFuscaleValoR()[numBarra], dataFuScaleBars.getFuscaleValoG()[numBarra], dataFuScaleBars.getFuscaleValoB()[numBarra]);
	}
	/*
	 * Gets FU Color from bar number
	 */
	private int getFUColor_M(int numBarra){
		return Color.rgb(dataFuScaleBars.getFuscaleValoR_M()[numBarra], dataFuScaleBars.getFuscaleValoG_M()[numBarra], dataFuScaleBars.getFuscaleValoB_M()[numBarra]);
	}
	/*
	 * Gets FU Color from bar number
	 */
	private int getFUColor_B(int numBarra){
		return Color.rgb(dataFuScaleBars.getFuscaleValoR_B()[numBarra], dataFuScaleBars.getFuscaleValoG_B()[numBarra], dataFuScaleBars.getFuscaleValoB_B()[numBarra]);
	}
	
	/*
	 * Initialize the state and color of the bars
	 */
	private void inicialitzaColorsEstatsBarres(){
		for (int i = 0; i < panelFuScaleContainer.getChildCount(); i++){
			LinearLayout lnrLayout = (LinearLayout)panelFuScaleContainer.getChildAt(i);
			lnrLayout.setBackgroundColor(colorFonsBar);
			estatsBarres[i] = false;
		}		
	}
	/* - - - - - - - - - - - - - - - - */
	/* - - - -  S u r v e y  - - - - - */
	/* - - - - - - - - - - - - - - - - */
	/*
	 * Clear App Survey Panel
	 */
	private void clearPanelAppSurvey(){
		appsurveyEasytouseRadiogroup.clearCheck();
		appsurveyMatchBarsRadiogroup.clearCheck();
		appsurveyLookWaterRadiogroup.clearCheck();
		appsurveyDesignRadiogroup.clearCheck();
		appsurveySecchiRadiogroup.clearCheck();
		editSuggestion.setText("");
	}
	/* - - - - - - - - - - - - - - - - - - - */
	/* - - - -  Q u e s t i o n s  - - - - - */
	/* - - - - - - - - - - - - - - - - - - - */
	/*
	 * Clear Questions Panel
	 */
	private void clearPanelQuestions(){
		// Cloud Fraction Value
    	cloudFractionValue = CLOUD_FRACTION.NOT_DEFINED;
    	rainingValue = RAINING.NOT_DEFINED;
    	seeBottomValue = SEE_BOTTOM.NOT_DEFINED;
    	    	
    	// Cloud Fraction UI
		panelQuestionCloudClear.setBackgroundColor(Color.TRANSPARENT);
		panelQuestionCloudFew.setBackgroundColor(Color.TRANSPARENT);
		panelQuestionCloudScattered.setBackgroundColor(Color.TRANSPARENT);
		panelQuestionCloudBroken.setBackgroundColor(Color.TRANSPARENT);
		panelQuestionCloudOvercast.setBackgroundColor(Color.TRANSPARENT);				
		imageQuestionCloudClear.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
		imageQuestionCloudFew.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
		imageQuestionCloudScattered.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
		imageQuestionCloudBroken.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
		imageQuestionCloudOvercast.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);				
		
		// Raining UI
		panelQuestionRainingYes.setBackgroundColor(Color.TRANSPARENT);
		panelQuestionRainingNo.setBackgroundColor(Color.TRANSPARENT);
		imageQuestionRainingYes.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
		imageQuestionRainingNo.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);

		// See Bottom UI
		panelQuestionBottomYes.setBackgroundColor(Color.TRANSPARENT);
		panelQuestionBottomNo.setBackgroundColor(Color.TRANSPARENT);
		imageQuestionBottomYes.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
		imageQuestionBottomNo.setAlpha(QUESTIONARY_IMAGE_OPACITY_NOT_ANSWERED);
		
		// Update Status Buttons
		updateButtonsQuestionsUI();
	}
	/*
	 * Update the UI status of question buttons  
	 */
	private void updateButtonsQuestionsUI(){
		if ((cloudFractionValue == CLOUD_FRACTION.NOT_DEFINED) || (rainingValue == RAINING.NOT_DEFINED) || (seeBottomValue == SEE_BOTTOM.NOT_DEFINED)){
			btSend.setEnabled(false);	
		} else btSend.setEnabled(true);
	}
	/* - - - - - - - - - - - - - - - - - - */
	/* - - - -  F e e d B a c k  - - - - - */
	/* - - - - - - - - - - - - - - - - - - */
	/*
	 * Sets the UI of the FeedBack depending on FU Value
	 */
	private void updateFeedBackUI(){
		// Show the FU Value in the text
		txtFeedback_text2.setText(String.format(getString(R.string.main_panel_feedback_text2), String.format("%2s", FUValue + "").replace(' ', '0')));

		String strFeedBackDescValue1 = getString(R.string.main_panel_feedback_desc_value1);
		if ((FUValue >= 0) && (FUValue <= 5)){
			Spannable WordtoSpan = Spannable.Factory.getInstance().newSpannable(strFeedBackDescValue1);
			WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), 0, strFeedBackDescValue1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			txtFeedBackDescValue1.setText(WordtoSpan);
		} else txtFeedBackDescValue1.setText(strFeedBackDescValue1);
		
		String strFeedBackDescValue2 = getString(R.string.main_panel_feedback_desc_value2);
		if ((FUValue >= 6) && (FUValue <= 9)){
			Spannable WordtoSpan = Spannable.Factory.getInstance().newSpannable(strFeedBackDescValue2);
			WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), 0, strFeedBackDescValue2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			txtFeedBackDescValue2.setText(WordtoSpan);
		} else txtFeedBackDescValue2.setText(strFeedBackDescValue2);
		
		String strFeedBackDescValue3 = getString(R.string.main_panel_feedback_desc_value3);
		if ((FUValue >= 10) && (FUValue <= 13)){
			Spannable WordtoSpan = Spannable.Factory.getInstance().newSpannable(strFeedBackDescValue3);
			WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), 0, strFeedBackDescValue3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			txtFeedBackDescValue3.setText(WordtoSpan);
		} else txtFeedBackDescValue3.setText(strFeedBackDescValue3);
		
		String strFeedBackDescValue4 = getString(R.string.main_panel_feedback_desc_value4);
		if ((FUValue >= 14) && (FUValue <= 17)){
			Spannable WordtoSpan = Spannable.Factory.getInstance().newSpannable(strFeedBackDescValue4);
			WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), 0, strFeedBackDescValue4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			txtFeedBackDescValue4.setText(WordtoSpan);
		} else txtFeedBackDescValue4.setText(strFeedBackDescValue4);
		
		String strFeedBackDescValue5 = getString(R.string.main_panel_feedback_desc_value5);
		if ((FUValue >= 18) && (FUValue <= 21)){
			Spannable WordtoSpan = Spannable.Factory.getInstance().newSpannable(strFeedBackDescValue5);
			WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), 0, strFeedBackDescValue5.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			txtFeedBackDescValue5.setText(WordtoSpan);
		} else txtFeedBackDescValue5.setText(strFeedBackDescValue5);
	}
	/* - - - - - - - - - - - - - - - - */
	/* - - - -  C a m e r a  - - - - - */
	/* - - - - - - - - - - - - - - - - */
	/*
	 * Camera Callback to control Camera photo  
	 */
	CameraCallback cameraCallback = new CameraCallback() {		
		@Override
		public void onShutter() {}		
		@Override
		public void onRawPictureTaken(byte[] data, Camera camera, boolean isDark) {}		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {}		
		@Override
		public void onJpegPictureTaken(byte[] data, Camera camera, boolean isDark) {			
			try{
				// Calculate file Name for the Instance		
				LastPictureNameMetadata = "C" + DispositiuFisic.getID(_this) + "." + System.currentTimeMillis() + "";

				// Recreate directories
				if (!TmpDirectory.exists()) TmpDirectory.mkdirs();
				if (!TemporalDirectory.exists()) TemporalDirectory.mkdirs();

				// Recycle Bitmaps of possible other photos			
				if (bmpCroppedPhoto != null) {
					bmpCroppedPhoto.recycle();
					bmpCroppedPhoto = null;
				}
				if (bmpPhotoTaken != null) {
					bmpPhotoTaken.recycle();
					bmpPhotoTaken = null;
				}
				
				// Update Screen Size
	    		getWindowManager().getDefaultDisplay().getMetrics(metrics);

				// Load image of picture taken, and set to Imageview 
				Bitmap bmpTemp =  BitmapFactory.decodeByteArray(data, 0, data.length);												
				int rotation = ((Activity)_this).getWindowManager().getDefaultDisplay().getRotation();
				if ((rotation == 0) || (rotation == 2)) {
                    // Notice that width and height are reversed
					//Bitmap bmpTemp2 = Bitmap.createScaledBitmap(bmpTemp, metrics.heightPixels, metrics.widthPixels, true);
                    Matrix mtx = new Matrix();
                	mtx.postRotate(90);
                	//bmpPhotoTaken = Bitmap.createBitmap(bmpTemp2, 0, 0, bmpTemp2.getWidth(), bmpTemp2.getHeight(), mtx, true);
                	bmpPhotoTaken = Bitmap.createBitmap(bmpTemp, 0, 0, bmpTemp.getWidth(), bmpTemp.getHeight(), mtx, false);
                                        
//                    // Recycle
//					if (bmpTemp != null){
//						bmpTemp.recycle();
//						bmpTemp = null;						
//					}
//					if (bmpTemp2 != null){
//						bmpTemp2.recycle();
//						bmpTemp2 = null;						
//					}
				} else if ((rotation == 1) || (rotation == 3)) {	
                    // Notice that width and height are not reversed
					//Bitmap bmpTemp2 = Bitmap.createScaledBitmap(bmpTemp, metrics.widthPixels, metrics.heightPixels, true);
					
                    // Setting post rotate
                    if (rotation == 1) {
                    	bmpPhotoTaken = Bitmap.createBitmap(bmpTemp);
                    } else {
                        Matrix mtx = new Matrix();
                    	mtx.postRotate(180);
                    	//bmpPhotoTaken = Bitmap.createBitmap(bmpTemp2, 0, 0, bmpTemp2.getWidth(), bmpTemp2.getHeight(), mtx, true);
                    	bmpPhotoTaken = Bitmap.createBitmap(bmpTemp, 0, 0, bmpTemp.getWidth(), bmpTemp.getHeight(), mtx, false);
                    }
                    
                    // Recycle
//					if (bmpTemp != null){
//						bmpTemp.recycle();
//						bmpTemp = null;						
//					}
//					if (bmpTemp2 != null){
//						bmpTemp2.recycle();
//						bmpTemp2 = null;						
//					}
                } 
								
				
				
				// change Activity Status
				switchToStat(ACTIVITY_STATE.SELECT_RECT);
								
			} catch (Exception e ){
				// Show error. Don't get next state
				Toast.makeText(_this, getString(R.string.main_panelsquare_load_photo_error) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
			} finally{
				
			}
		}
	};
	/*
	 * Init Camera Values and Parameters
	 */
	private void initCamera() throws Exception{
		// Get the possible values white balances
		if (supportedWhiteBalances == null){
			supportedWhiteBalances = cameraLib.getSupportedWhiteBalances();
			supportedStringWhiteBalance = new String[supportedWhiteBalances.size()];
			for (int i = 0;i < supportedStringWhiteBalance.length; i++){
				supportedStringWhiteBalance[i] = supportedWhiteBalances.get(i).toString();
			}
		}
		// Get the possible values focus Mode
		if (supportedFocusMode == null){
			supportedFocusMode = cameraLib.getSupportedFocusMode();
			supportedStringFocusMode = new String[supportedFocusMode.size()];	
			for (int i = 0;i < supportedStringFocusMode.length; i++){
				supportedStringFocusMode[i] = supportedFocusMode.get(i).toString();
			}
		}
		// Get the possible values Scene
		if (supportedScene == null){
			supportedScene = cameraLib.getSupportedScene();
			supportedStringScene = new String[supportedScene.size()];
			for (int i = 0;i < supportedStringScene.length; i++){
				supportedStringScene[i] = supportedScene.get(i).toString();
			}
		}
		
		// Update Screen Size
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		cameraLib.setPictureSizeFitScreen(metrics.widthPixels, metrics.heightPixels);
		
		// Camera assigned. Put the preview in the activity
		FrameLayout.LayoutParams camLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		previewContainer.removeView(cameraLib.getSurfacePreview());
		previewContainer.addView(cameraLib.getSurfacePreview(), 0, camLayoutParams);
	}
	/*
	 * Show Dialog Focus Mode Selection
	 */
	private void selectFocusMode() throws Exception{
		try{
			FOCUS_MODE currentFocusMode = cameraLib.getCurrentFocusMode();
			int idxSelectedFocusMode = -1;
			for(int i = 0; i < supportedFocusMode.size(); i++){
				if (supportedFocusMode.get(i) == currentFocusMode){
					idxSelectedFocusMode = i;
					break;
				}
			}

			// We create the selector 
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);			 
			builder.setTitle("Selector Focus Mode");
			builder.setSingleChoiceItems(
					supportedStringFocusMode, 
					idxSelectedFocusMode, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try{
								// Update the value 
								cameraLib.setFocusMode(supportedFocusMode.get(which));	

							} catch (Exception e){
								Toast.makeText(_this, e.getMessage(), Toast.LENGTH_LONG).show();
							} finally{
								// Close the dialog
								dialog.dismiss();
							}						
						}
					});

			// We show the dialog
			builder.show();
		} catch (Exception e){
			throw e;
		} finally{

		}						
	}
	/*
	 * Shows first dialog menu before Camera Menus
	 */
	private void selectFocusWhiteScene(){
		final CharSequence[] items = {getString(R.string.main_submenu_whitebalance), getString(R.string.main_submenu_focusmode), getString(R.string.main_submenu_scenemode)};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.main_submenu_title));
		builder.setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0:
					selectWhiteBalance();
					break;
				case 1:
					try {
						selectFocusMode();
					} catch (Exception e) {
						Toast.makeText(_this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
					}
					break;
				case 2:
					selectScene();
					break;
				}
				dialog.cancel();	
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	/*
	 * Show Dialog Scene Selection
	 */
	private void selectScene(){
		try {
			SCENE currentScene = cameraLib.getCurrentScene();
			int idxSelectedScene = -1;
			for (int i = 0; i < supportedScene.size(); i++){
				if(supportedScene.get(i) == currentScene){
					idxSelectedScene = i;
					break;
				}
			}

			final AlertDialog.Builder builder = new AlertDialog.Builder(this);			 
			builder.setTitle("Selector Scene");
			builder.setSingleChoiceItems(
					supportedStringScene, 
					idxSelectedScene, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try{
								// Update the value 
								cameraLib.setScene(supportedScene.get(which));	
							} catch (Exception e){
								Toast.makeText(_this, e.getMessage(), Toast.LENGTH_LONG).show();
							} finally{
								// Close the dialog
								dialog.dismiss();
							}						
						}
					});

			// We show the dialog
			builder.show();

		} catch (Exception e) {
			Toast.makeText(_this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	/*
	 * Show Dialog White Balance Selection
	 */
	private void selectWhiteBalance(){
		try {
			WHITE_BALANCE currentWhiteBalance = cameraLib.getCurrentWhiteBalance();
			int idxSelectedWhiteBalance = -1;
			for (int i = 0; i < supportedWhiteBalances.size(); i++){
				if(supportedWhiteBalances.get(i) == currentWhiteBalance){
					idxSelectedWhiteBalance = i;
					break;
				}
			}

			final AlertDialog.Builder builder = new AlertDialog.Builder(this);			 
			builder.setTitle("Selector White Balance");
			builder.setSingleChoiceItems(
					supportedStringWhiteBalance, 
					idxSelectedWhiteBalance, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try{
								// Update the value 
								cameraLib.setWhiteBalance(supportedWhiteBalances.get(which));
							} catch (Exception e){
								Toast.makeText(_this, e.getMessage(), Toast.LENGTH_LONG).show();
							} finally{
								// Close the dialog
								dialog.dismiss();
							}						
						}
					});

			// We show the dialog
			builder.show();
		} catch (Exception e) {
			Toast.makeText(_this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	/*
	 * Action OnClick Take Photo
	 */
	public void onClickTakePhoto(View view) throws Exception{
		try{
			boolean bIsPositioned = true;
//			boolean bIsPositioned = false;
//			if (fuScaleSensors.getNumberOfSensors() == 0) bIsPositioned = true;
//			else {
//				if 	((fuScaleSensors.getPitchDegrees() <= (BEST_PITCH_DEGREE + MARGIN_BEST_PITCH_DEGREE)) &&
//					(fuScaleSensors.getPitchDegrees() >= (BEST_PITCH_DEGREE - MARGIN_BEST_PITCH_DEGREE))){
//					bIsPositioned = true;
//				}
//			}

			//if(azimuthView.IsPositioned()){
			if (bIsPositioned){
				// Show Working Dialog
				if (workingDialog == null) workingDialog = kTransparentWorkingDialog.show(FUScaleActivityExt.this, null, null);

				// Take the Picture
				cameraLib.takePicture();					
			}			
		} catch (Exception e){
			Toast.makeText(FUScaleActivityExt.this, "Picture Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		} finally{
//			// Enable button
//			btObjetivo.setEnabled(false);
			
			// Dismiss Working Dialog
			if (workingDialog != null) {
				workingDialog.dismiss();
				workingDialog = null;
			}				
		}
	}
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
}
