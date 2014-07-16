package com.citclops.util;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Window;
import android.view.WindowManager;

public class BrightnessUtility {

	private Window window = null;
	private Context context = null;
	
	int currentMode = -1;						// To store the current brightness mode
	float previousScreenBrightness = 0;			// To store the current brightness value
	
	/*
	 * Constructor
	 */
	public BrightnessUtility(Window wdw, Context ctx){
		// Local Copy
		window = wdw;	
		context = ctx;
	}
	/*
	 * Set the Activity to Maximum Brightness
	 */
	public void MaxBrightness() throws SettingNotFoundException{
		currentMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		WindowManager.LayoutParams lp = window.getAttributes();
	    previousScreenBrightness = lp.screenBrightness;
	    lp.screenBrightness = 1.0f; 
	    window.setAttributes(lp);
	}
	/*
	 * Get and store the current values of Brightness
	 */
	public void restoreBrightness () throws SettingNotFoundException{
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, currentMode);        
		WindowManager.LayoutParams lp = window.getAttributes();
	    previousScreenBrightness = lp.screenBrightness;
	    float brightness = previousScreenBrightness;
	    lp.screenBrightness = brightness; 
	    window.setAttributes(lp);
	}
}
