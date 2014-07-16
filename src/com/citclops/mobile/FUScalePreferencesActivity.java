package com.citclops.mobile;

import java.util.ArrayList;

import com.citclops.models.FUBarValues;
import com.citclops.util.DescriptionFUBarAdapter;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class FUScalePreferencesActivity extends Activity {

	// Control first run of Activity
	boolean firstExecution = true;											// Variable First Run

	FUBarValues datas;																// Datas object is created

	private Button btAccept;														// Accept Changes
	private Button btCancel;														// Cancel Changes
	private Button btResetAll;														// Reset All Changes
	private SeekBar seekBarRed;														// Seekbar RED
	private SeekBar seekBarGreen;													// Seekbar GREEN
	private SeekBar seekBarBlue;													// Seekbar BLUE
	private TextView txtRedValue;													// Value R
	private TextView txtGreenValue;													// Value G
	private TextView txtBlueValue;													// Value B
	private CheckBox cbVisible;														// Checkbox visibility bar	
	private Spinner spFuScale ;														// Spinner that FuScale selected
	private FrameLayout lytFUBar;													// Bar container panel													
	RelativeLayout pnlA;															// Layout forming seekbar
	RelativeLayout pnlB;															// Layout forming seekbar
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fuscale_pref);

		// Load de data of FU Scale Preferences Values
		datas = new FUBarValues(this);
		datas.Load();

		// Get Widgets References		
		btAccept = (Button) findViewById(R.idPrefs.btAccept);
		btCancel = (Button) findViewById(R.idPrefs.btCancel);
		btResetAll = (Button) findViewById(R.idPrefs.btResetAll);
		seekBarRed = (SeekBar) findViewById(R.idPrefs.seekBarRed);
		seekBarGreen = (SeekBar) findViewById(R.idPrefs.seekBarGreen);
		seekBarBlue = (SeekBar) findViewById(R.idPrefs.seekBarBlue);
		txtRedValue = (TextView) findViewById(R.idPrefs.txtRedValue);
		txtGreenValue = (TextView) findViewById(R.idPrefs.txtGreenValue);
		txtBlueValue = (TextView) findViewById(R.idPrefs.txtBlueValue);

		// Panels for the Bar
		pnlA = new RelativeLayout(this);
		pnlB = new RelativeLayout(this);
		
		/*********************************************/
		/********** Configure the seekbars  **********/
		/*********************************************/
		seekBarRed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				//Associate the color of the two layouts
				pnlA.setBackgroundColor(getFUColorFromSeekBars());
				pnlB.setBackgroundColor(getFUColorFromSeekBars());
				txtRedValue.setText(getString(R.string.preferences_redValue) + ": " + progress + "");
				
				//Introduced in position ARRAY indicating SPINNER
				datas.getFuscaleValoR()[spFuScale.getSelectedItemPosition()] = progress;
			}
		});
		seekBarGreen.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				//Associate the color of the two layouts
				pnlA.setBackgroundColor(getFUColorFromSeekBars());
				pnlB.setBackgroundColor(getFUColorFromSeekBars());
				txtGreenValue.setText(getString(R.string.preferences_greenValue) + ": " + progress + "");
				
				//Introduced in position ARRAY indicating SPINNER
				datas.getFuscaleValoG()[spFuScale.getSelectedItemPosition()] = progress;
			}
		});
		seekBarBlue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				//Associate the color of the two layouts
				pnlA.setBackgroundColor(getFUColorFromSeekBars());
				pnlB.setBackgroundColor(getFUColorFromSeekBars());
				txtBlueValue.setText(getString(R.string.preferences_blueValue) + ": " + progress + "");
				
				//Introduced in position ARRAY indicating SPINNER
				datas.getFuscaleValoB()[spFuScale.getSelectedItemPosition()] = progress;
			}
		});		
		/*********************************************/
		/********** Configure the CheckBox  **********/
		/*********************************************/		
		cbVisible = (CheckBox) findViewById(R.idPrefs.cbVisible);		
		cbVisible.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (cbVisible.isChecked() == true){
					datas.getVisibleFuscale()[spFuScale.getSelectedItemPosition()] = true;
				}else{
					datas.getVisibleFuscale()[spFuScale.getSelectedItemPosition()] = false;
				}
			}
		});		
		/*********************************************/
		/********** Configure the Spinner  ***********/
		/*********************************************/
		spFuScale = (Spinner) findViewById(R.idPrefs.spnFuBar);
		ArrayList<String> FUValues = new ArrayList<String>();
		for (int i = 0; i < 21; i++) FUValues.add(getString(R.string.preferences_FUBarName) + " " + (i + 1) );
		DescriptionFUBarAdapter adapterFU = new DescriptionFUBarAdapter(this, android.R.layout.simple_spinner_item, FUValues);
		adapterFU.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spFuScale.setAdapter(adapterFU);	
		spFuScale.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) {
				//Insert the individual value of each FuScale
				seekBarRed.setProgress(datas.getFuscaleValoR()[position]);
				seekBarGreen.setProgress(datas.getFuscaleValoG()[position]);
				seekBarBlue.setProgress(datas.getFuscaleValoB()[position]);
				cbVisible.setChecked(datas.getVisibleFuscale()[position]);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}
	/*****************************************/
	/********** O v e r r i d e s  ***********/
	/*****************************************/
	@Override
	protected void onResume() {
		super.onResume();
		
		if (firstExecution){
			firstExecution = false;

			// Create FU Scale
			crearFuScale();
		}
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);		
	}
	/***********************************************************/
	/********** B u t t o n s     L i s t e n e r s  ***********/
	/***********************************************************/
	/*
	 * Accept Changes
	 */
	public void onClickAccept(View view){
		btAccept.setEnabled(false);
		btCancel.setEnabled(false);
		btResetAll.setEnabled(false);  
		datas.Save();
		finish();
	}
	/*
	 * Dismiss Changes
	 */
	public void onClickCancel(View view){
		btAccept.setEnabled(false);
		btCancel.setEnabled(false);
		btResetAll.setEnabled(false);  
		finish();
	}
	/*
	 * Reset All Values
	 */
	public void onClickResetAll(View view){
		datas.Clear();
		seekBarRed.setProgress(datas.getFuscaleValoR()[spFuScale.getSelectedItemPosition()]);
		seekBarGreen.setProgress(datas.getFuscaleValoG()[spFuScale.getSelectedItemPosition()]);
		seekBarBlue.setProgress(datas.getFuscaleValoB()[spFuScale.getSelectedItemPosition()]);
		cbVisible.setChecked(datas.getVisibleFuscale()[spFuScale.getSelectedItemPosition()]);
	}
	/*******************************************************************/
	/********** C r e a t e     F u    S c a l e     B a r   ***********/
	/*******************************************************************/
	/*
	 * Create the FUScale
	 */
	private void crearFuScale(){
		// Creem el contenidor de les barres
		lytFUBar = (FrameLayout) findViewById(R.idPrefs.lytFUBar);

		// Containers		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int widthContenidor = (int)(60.0f * metrics.density);
		int heightContenidor = (int)(127.0f * metrics.density);
		int heightContenidorMax = (int)(300.0f * metrics.density);
		int heightContenidorN = (int)(49.0f * metrics.density);
		final int sizePadding = (int)(5.0f * metrics.density);
		int leftMargin = (int)(30.0f * metrics.density); 

		pnlA.setLayoutParams(new RelativeLayout.LayoutParams(widthContenidor,heightContenidor));
		pnlA.setBackgroundColor(getFUColorFromSeekBars());
		pnlB.setLayoutParams(new RelativeLayout.LayoutParams(widthContenidor,heightContenidor));
		pnlB.setBackgroundColor(getFUColorFromSeekBars());

		RelativeLayout pnlN = new RelativeLayout(this);
		pnlN.setLayoutParams(new RelativeLayout.LayoutParams(widthContenidor, heightContenidorN));
		pnlN.setBackgroundColor(Color.DKGRAY);

		RelativeLayout pnlNSup = new RelativeLayout(this);
		pnlNSup.setLayoutParams(new RelativeLayout.LayoutParams(widthContenidor, heightContenidorN));
		pnlNSup.setBackgroundColor(Color.DKGRAY);

		String num = String.valueOf("N");
		TextView contNum = new TextView(this);
		contNum.setTextColor(Color.WHITE);
		contNum.setText(num);
		contNum.setGravity(Gravity.CENTER);
		pnlN.addView(contNum, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		TextView contNum2 = new TextView(this);
		contNum2.setTextColor(Color.WHITE);
		contNum2.setText(num);
		contNum2.setGravity(Gravity.CENTER);   			
		pnlNSup.addView(contNum2, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		LinearLayout pnlC = new LinearLayout(this);
		pnlC.setOrientation(LinearLayout.VERTICAL);
		pnlC.setPadding(sizePadding, sizePadding, sizePadding, sizePadding);
		LinearLayout.LayoutParams lytMainParams = new LinearLayout.LayoutParams(widthContenidor,heightContenidorMax + heightContenidorN);
		lytMainParams.leftMargin = leftMargin;
		pnlC.setLayoutParams(lytMainParams);
		pnlC.addView(pnlNSup);
		pnlC.addView(pnlA);
		pnlC.addView(pnlB);
		pnlC.addView(pnlN);
		pnlC.setTag(1);

		lytFUBar.addView(pnlC);
	}
	/*
	 * Gets the color of the bar manually
	 */
	private int getFUColorFromSeekBars(){										
		int retorn;
		int valueR = seekBarRed.getProgress();
		int valueG = seekBarGreen.getProgress();
		int valueB = seekBarBlue.getProgress();
		retorn = Color.rgb(valueR, valueG, valueB);
		return retorn;	
	}	
}
