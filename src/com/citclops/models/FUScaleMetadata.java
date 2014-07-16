package com.citclops.models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.citclops.util.DispositiuFisic;

import android.content.Context;
import android.hardware.Camera.Parameters;
import android.location.Location;
import android.os.Build;

public class FUScaleMetadata {

	private Context context;								// Context
	static private String _deviceName = "";					// Static field Device Name. Calculate only once
	
	private enum MeasurinAreaTypes {Point, Track, Area};	// Area Types
	public enum CloudFractionValues {No_Clouds, Some_Clouds, Fully_Clouded, Clear, Few, Scattered, Broken, Overcast};
	
	public enum RainFractionValues {Yes, No};
	public enum BottomFractionValues {Yes, No};
	
	private final String mDataset_name_KEY = "Ocean colour via citclops app";	// Name of dataset data
	private final String WGS84_2D_KEY = "4326:WGS84";		// Key for WGS84 (2D)
	private final String Parameters_Measured_KEY = "R410";	// Key for R410
	private final String Platform_type_KEY = "71:Human";	// Key for Platform Type
	private final String device_type_KEY = "XXX:???????";	// Key for Device Type			
	private final String Language_KEY = "en";				// Key for Language
	private final String Data_format_KEY_PNG = "PNG";		// Key for Data Format
	private final String Data_format_KEY_JPG = "JPG";		// Key for Data Format
	
	private String cameraParameters;						// Camera Parameters for abstract 
	
	private String mDataset_name;							// Short name of observation (e.g. ocean colour via citclops app)
	private String m_Observation_ID;						// Unique id of measurement/observation (primary key)
	private String Date_time;								// Date and time of measurement in ISO8601 notation: YYYY-MM-DDThh:mm:ss. Local time
	private MeasurinAreaTypes Measuring_area_type;			// Geographical type, possible values: Point/Track/Area	
	private Location Location_lat_long;						// Location
	private String Datum_coordinate_system; 				// Datum of the coordinate system. Default/Fixed = WGS84
	private String Parameters_measured;						// Via code used from vocabulary P02 - BODC Parameter Discovery Vocabulary (New codes might be needed.) (units will be part of the datafile!)
	private String Abstract;								// Short description of measurement. Default inserted by application. Specific details for measurement (other than strictly necessary for Data Quality check), such as aperture, exposure time etc.
	private String Platform_type;							// Type of platform. Platform type on which the sensor is mounted/carried
	private String device_type;								// Sensor/device/Instrument category	
	private String Station_name;							// Name of measurement Location
	private String device_name;								// Specific name / brand of the device/instrument or sensor/device id (free text)
	private String PI;										// For scientific data or for Citclops crowd the user name, mobile phone ID (privacy important!)
	private String Station_start_date_time;					// Derived from first measurement date/time in case multiple observations/images are taken. ISO8601 notation: YYYY-MM-DDThh:mm:ss (converted to UTC)
	private String Station_end_date_time;					// Derived from first measurement date/time in case multiple observations/images are taken. ISO8601 notation: YYYY-MM-DDThh:mm:ss (converted to UTC)
	private String Language;								// Language of metadata in file
	private String Profile;									// Citizen's Profile
	private String Data_format;								// SDN data transport format via code from L241.
	private int FUValue;									// Value of FU Scale
	private int PitchAngle;									// Angle de rotacio respecte el terra 
	private int AzimuthAngle;								// Angle azimuth respcte el sol 
	private CloudFractionValues Cloud_Fraction;				// no clouds, some clouds, fully_clouded
	private RainFractionValues Rain;						// It's raining
	private BottomFractionValues Bottom;					// Do you see the bottom?
	private boolean UserHasSecchiDisk = false;				// Gets if user has a Secchi Disk	
	private float SecchiDiskDepth = 0.0f;					// Estimated Depth of Secchi Disk
	private boolean UserHasPlasticFUScale = false;			// Gets if user has a Plastic FuScale
	private int PlasticFuValue = 0;							// Plastic Fu Value
		
	/*
	* Constructor
	*/
	public FUScaleMetadata(Context c){
		// Local Copy
		context = c;
		
		//Initialize Data
		Init();		
	}
	/*
	 * Initilize
	 */	
	private void Init(){
		String dateNow = dateNowToISO8601();
		mDataset_name = mDataset_name_KEY;
		m_Observation_ID = DispositiuFisic.getID(context) + java.lang.System.currentTimeMillis();
		Date_time = dateNow;
		Measuring_area_type = MeasurinAreaTypes.Point;
		Location_lat_long = new Location("FuScale");
		Datum_coordinate_system = WGS84_2D_KEY;
		Parameters_measured = Parameters_Measured_KEY;
		cameraParameters = "";
		Abstract = "";
		Platform_type = Platform_type_KEY;
		device_type = device_type_KEY;
		device_name = getDeviceName();
		Station_name = DispositiuFisic.getID(context);
		PI = DispositiuFisic.getID(context);
		Station_start_date_time = dateNow;
		Station_end_date_time = dateNow;
		Language = Language_KEY;
		Profile = "";
		Data_format = Data_format_KEY_PNG;
		PitchAngle = 0; 
		AzimuthAngle = 0; 
		Cloud_Fraction = CloudFractionValues.No_Clouds;
		Rain = RainFractionValues.No;
		Bottom = BottomFractionValues.No;
		UserHasSecchiDisk = false;	
		SecchiDiskDepth = 0.0f;
		UserHasPlasticFUScale = false;			// Gets if user has a Plastic FuScale
		PlasticFuValue = 0;							// Plastic Fu Value
	}	
	/*
	 * Format Date to YYYY-MM-DDThh:mm:ss
	 */
	private String dateNowToISO8601(){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.ITALIAN);
		return df.format(new Date());
	}
	/*
	 * Get Unique Device Name and Descriptio
	 */
	private String getDeviceName() {
		if(_deviceName.length() <= 0) {
			_deviceName = (Build.MANUFACTURER.trim() + " " + Build.MODEL).trim();
		}
		return _deviceName;
	}
	/*
	 * Gets XML of Data
	 */
	private String getXmlData(String fileName, String format){
		
		if (format.equalsIgnoreCase("JPG")) Data_format = Data_format_KEY_JPG;
		else if (format.equalsIgnoreCase("PNG")) Data_format = Data_format_KEY_PNG;
		
		String retorn = "";
		retorn += "<" + fileName + ">";
		retorn += "<Dataset_name>" + 			mDataset_name 				+ "</Dataset_name>";
		retorn += "<Observation_ID>" + 			m_Observation_ID 			+ "</Observation_ID>";
		retorn += "<Date_time>" + 				Date_time 					+ "</Date_time>";
		retorn += "<Measuring_area_type>" + 	Measuring_area_type 		+ "</Measuring_area_type>";		
		if ((Location_lat_long.getLatitude() < 180) && (Location_lat_long.getLongitude() < 180) &&		
			(Location_lat_long.getLatitude() > -180) && (Location_lat_long.getLongitude() > -180) ){		
			retorn += "<Location_lat_long>" + 		Double.valueOf(Location_lat_long.getLatitude()).toString() + "," + Double.valueOf(Location_lat_long.getLongitude()).toString() 	+ "</Location_lat_long>";
		} 		
		retorn += "<Datum_coordinate_system>" + Datum_coordinate_system 	+ "</Datum_coordinate_system>";
		retorn += "<Parameters_measured>" + 	Parameters_measured 		+ "</Parameters_measured>";	
		Abstract = "<![CDATA[";
		if ((Location_lat_long.getLatitude() < 180) && (Location_lat_long.getLongitude() < 180) &&		
			(Location_lat_long.getLatitude() > -180) && (Location_lat_long.getLongitude() > -180) ){		
			if (Location_lat_long.hasAccuracy()){
				Abstract += "<LocationAccuracy>\n";
				Abstract += Float.valueOf(Location_lat_long.getAccuracy()).toString();			
				Abstract += "</LocationAccuracy>\n";						
			}
		}
		Abstract += cameraParameters;
		Abstract += "]]>";		
		retorn += "<Abstract>" + 				Abstract 					+ "</Abstract>";
		retorn += "<Platform_type>" + 			Platform_type 				+ "</Platform_type>";
		retorn += "<device_type>" + 			device_type 				+ "</device_type>";
		retorn += "<device_name>" + 			device_name 				+ "</device_name>";
		retorn += "<Station_name>" + 			Station_name 				+ "</Station_name>";
		retorn += "<PI>" + 						PI 							+ "</PI>";
		retorn += "<Station_start_date_time>" + Station_start_date_time 	+ "</Station_start_date_time>";
		retorn += "<Station_end_date_time>" + 	Station_end_date_time 		+ "</Station_end_date_time>";
		retorn += "<Language>" + 				Language 					+ "</Language>";
		retorn += "<Profile>" + 				Profile 					+ "</Profile>";
		retorn += "<Data_format>" + 			Data_format 				+ "</Data_format>";
		retorn += "<Data_file>" + 				fileName + "." + format 	+ "</Data_file>";
		retorn += "<FU_Value>" + 				FUValue 					+ "</FU_Value>";
		retorn += "<viewing_angle>" + 			PitchAngle  				+ "</viewing_angle>";
		retorn += "<azimuth_angle>" + 			AzimuthAngle 				+ "</azimuth_angle>";
		retorn += "<Cloud_Fraction>" + 			Cloud_Fraction 				+ "</Cloud_Fraction>";
		retorn += "<Rain>" + 					Rain 						+ "</Rain>";
		retorn += "<Bottom>" + 					Bottom 						+ "</Bottom>";		
		retorn += "<UserHasSecchiDisk>" + 		UserHasSecchiDisk 			+ "</UserHasSecchiDisk>";
		if (UserHasSecchiDisk){
			retorn += "<SecchiDiskDepth>" + 	SecchiDiskDepth 			+ "</SecchiDiskDepth>";
		}
		retorn += "<UserHasPlasticFUScale>" + 	UserHasPlasticFUScale 		+ "</UserHasPlasticFUScale>";
		if (UserHasPlasticFUScale){
			retorn += "<PlasticFuValue>" + 		PlasticFuValue 				+ "</PlasticFuValue>";
		}
		
		retorn += "</" + fileName + ">";
		return retorn;		
	}
	/**
	 * *******************************************************************************/
	/*
	 * Sets the final file of metadata
	 */
	public void Save(String baseFullPath, String pictureName, String pictureFormat) throws Exception{
        File  fileParameters = null;            
        FileOutputStream fout = null;
        OutputStreamWriter ows = null;
		try{
            fileParameters = new File(baseFullPath, pictureName + ".xml");            
            fout = new FileOutputStream(fileParameters);
        	ows = new  OutputStreamWriter(fout);
            ows.write(getXmlData(pictureName, pictureFormat));
            ows.flush();
		} catch (Exception e){
			throw e;		
		} finally{			
        	if (ows != null) ows.close();
        	if (fout != null) fout.close();
		}
	}
	/*
	 * Sets Latitude Value
	 */
	public void setLatitude(double latitude){
		Location_lat_long.setLatitude(latitude);		
	}
	/*
	 * Sets Longitude Value
	 */
	public void setLongitude(double longitude){
		Location_lat_long.setLongitude(longitude);
	}
	/*
	 * Sets Accuracy of Location
	 */
	public void setAccuracy(float accuracy){
		Location_lat_long.setAccuracy(accuracy);		
	}
	/*
	 * Sets the FU Value
	 */
	public void setFUValue(int FUScaleValue){
		FUValue = FUScaleValue;
	}
	/*
	 * Sets Camera Parameters 
	 */
	public void setCameraParameters(Parameters cameraParameters){
		this.cameraParameters += "<CameraParameters>\n";
		this.cameraParameters += "<Antibanding>" + cameraParameters.getAntibanding() + "</Antibanding>\n";
		this.cameraParameters += "<AutoExposureLock>" + cameraParameters.getAutoExposureLock() + "</AutoExposureLock>\n";
		this.cameraParameters += "<AutoWhiteBalanceLock>" + cameraParameters.getAutoWhiteBalanceLock() + "</AutoWhiteBalanceLock>\n";
		this.cameraParameters += "<ColorEffect>" + cameraParameters.getColorEffect() + "</ColorEffect>\n";
		this.cameraParameters += "<ExposureCompensation>" + cameraParameters.getExposureCompensation() + "</ExposureCompensation>\n";
		this.cameraParameters += "<FlashMode>" + cameraParameters.getFlashMode() + "</FlashMode>\n";
		this.cameraParameters += "<FocalLength>" + cameraParameters.getFocalLength() + "</FocalLength>\n";
		this.cameraParameters += "<FocusMode>" + cameraParameters.getFocusMode() + "</FocusMode>\n";
		this.cameraParameters += "<HorizontalViewAngle>" + cameraParameters.getHorizontalViewAngle() + "</HorizontalViewAngle>\n";
		this.cameraParameters += "<JpegQuality>" + cameraParameters.getJpegQuality() + "</JpegQuality>\n";
		this.cameraParameters += "<PictureFormat>" + cameraParameters.getPictureFormat() + "</PictureFormat>\n";
		this.cameraParameters += "<PictureSize>" + cameraParameters.getPictureSize().width + "x" + cameraParameters.getPictureSize().height + "</PictureSize>\n";
		this.cameraParameters += "<Scene>" + cameraParameters.getSceneMode() + "</Scene>\n";
		this.cameraParameters += "<VerticalViewAngle>" + cameraParameters.getVerticalViewAngle() + "</VerticalViewAngle>\n";
		this.cameraParameters += "<WhiteBalance>" + cameraParameters.getWhiteBalance() + "</WhiteBalance>\n";
		this.cameraParameters += "<Zoom>" + cameraParameters.getZoom() + "</Zoom>\n";
		this.cameraParameters += "</CameraParameters>\n";			
	}
	/*
	 * Set Pitch Angle 
	 */
	public void setPitch(int pitch){
		PitchAngle = pitch; 
	}
	/*
	 * Sets Azimuth from sun
	 */
	public void setAzimuth(int azimuth){
		AzimuthAngle = azimuth;
	}
	/*
	 * Sets the Cloud Fraction
	 */
	public void setCloudFraction(CloudFractionValues value){
		Cloud_Fraction = value;
	}
	/*
	 * Sets the Rain value
	 */
	public void setRain(RainFractionValues rainValue){
		Rain = rainValue;
	}
	/*
	 * Sets the Bottom value
	 */
	public void setBottom(BottomFractionValues bottomValue){
		Bottom = bottomValue;
	}
	public void setProfile(String sProfile){
		Profile = sProfile;
	}	
	/*
	 * Sets if User Has Secchi Disk, and if yes, the estimated depth
	 */
	public void setSecchiDiskInfo (boolean hasSecchiDisk, float secchiDiskDepth){
		UserHasSecchiDisk = hasSecchiDisk;	
		SecchiDiskDepth = secchiDiskDepth;
	}
	/*
	 * Sets if User Has Plastic FuScale, and if yes, the FuValue
	 */
	public void setPlasticFuInfo (boolean hasPlasticFUScale, int plasticFuValue){
		UserHasPlasticFUScale = hasPlasticFUScale;	
		PlasticFuValue = plasticFuValue;
	}	
}