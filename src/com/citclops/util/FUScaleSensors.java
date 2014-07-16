package com.citclops.util;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class FUScaleSensors implements SensorEventListener{

	private Activity activity;									// Parent Activity
    SensorEventListener m_parent;   							// non-null if this class should call its parent after onSensorChanged(...) and onAccuracyChanged(...) notifications
    SensorManager mSensorManager;								// Sensor Manager
	int m_sun_azimuth_degrees;        							// angle of the device from sun
	int m_azimuth_degrees = Integer.MIN_VALUE;        			// angle of the device from sun (magnetic north if azimuth sun not defined)
	int rawSensorValue = 0;										// Value of the sensor
	int m_pitch_degrees;										// tilt angle of the device from the horizontal.  m_pitch_radians = 0 if the device if flat, m_pitch_radians = 90 means the device is upright.
	int numSensors = 0;											// Number of sensors registered

	/*
	 * Constructor
	 */
	public FUScaleSensors(Activity a, SensorEventListener parent){
		// Local Copy
		activity = a;
		m_parent = parent;
		
		// Create Sensors
		m_azimuth_degrees = Integer.MIN_VALUE;
		m_sun_azimuth_degrees = 0;
		rawSensorValue = 0;
		mSensorManager = (SensorManager)activity.getSystemService(android.content.Context.SENSOR_SERVICE);	
	}
	/*
	 * Register Sensor Options and listeners
	 */	
	public int Register(){
		numSensors = 0;
		m_azimuth_degrees = Integer.MIN_VALUE;
		m_sun_azimuth_degrees = 0;
		rawSensorValue = 0;
		if(mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME)) numSensors++; 
		return numSensors;	
	}
	/*
	 * Unregister Listeners
	 */
	public void UnRegister(){
		// Unregister Sensor Listeners
		mSensorManager.unregisterListener(this);
		m_azimuth_degrees = Integer.MIN_VALUE;
		m_sun_azimuth_degrees = 0;
		rawSensorValue = 0;
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (m_parent != null) m_parent.onAccuracyChanged(sensor, accuracy);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
				
		// get the angle around the z-axis rotated
		rawSensorValue = Math.round(event.values[0]); 
		while (rawSensorValue < 0) rawSensorValue += 360;
		m_azimuth_degrees = rawSensorValue - m_sun_azimuth_degrees;
		while (m_azimuth_degrees < 0) m_azimuth_degrees += 360;
		
		// get pitch value
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		if ((rotation == 0) || (rotation == 2)) m_pitch_degrees = Math.abs((int)event.values[1]); 
		else m_pitch_degrees = Math.abs((int)event.values[2]);
		
		// Update event
		if (m_parent != null) m_parent.onSensorChanged(event);
	}
	/*
	 * Set the Sun Azimuth degrees manully
	 */
	public void setSunAzimuthDegreesManual(int AzimuthDegrees) {
		this.m_sun_azimuth_degrees = AzimuthDegrees;
		while (this.m_sun_azimuth_degrees < 0) this.m_sun_azimuth_degrees += 360;
	}
	/*
	 * Set the Sun Azimuth degrees 
	 */
	public void setSunAzimuthDegrees() {
		this.m_sun_azimuth_degrees = rawSensorValue;
		while (this.m_sun_azimuth_degrees < 0) this.m_sun_azimuth_degrees += 360;
	}
	/*
	 * Get Angle of the device from sun (magnetic north if azimuth sun not defined) 
	 */
	public int getAzimuthDegrees() {
		return m_azimuth_degrees;
	}
	/*
	 * Get tilt angle of the device from the horizontal.  m_pitch_radians = 0 if the device if flat, m_pitch_radians = 90 means the device is upright. 
	 */
	public int getPitchDegrees() {
		return m_pitch_degrees;
	}
	/*
	 * Get tilt angle of the device from the horizontal.  m_pitch_radians = 0 if the device if flat, m_pitch_radians = 90 means the device is upright. 
	 */
	public int getNumberOfSensors() {
		return numSensors;
	}


}
