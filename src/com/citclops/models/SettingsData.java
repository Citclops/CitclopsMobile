package com.citclops.models;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsData {

	public enum PROFILES {STUDENT, RESEARCHER, POLICY, VOLUNTEER, OBSERVER, TOURIST};
	public static final String PREFERENCES_PROFILES = "PREFERENCES_PROFILES"; 	
	public static final String PROFILE_KEY = "PROFILE_KEY"; 	
	public static final String ASK_SECCHI_DISK_KEY = "ASK_SECCHI_DISK_KEY"; 	
	public static final String ASK_PLASTIC_SCALE_KEY = "ASK_PLASTIC_SCALE_KEY"; 	
	public static final String MAIL_ADDRESS_SURVEY_KEY = "MAIL_ADDRESS_SURVEY_KEY";
	public static final String DATA_UPLOAD_KEY = "DATA_UPLOAD_KEY";
	
	private static String EMAIL_SURVEY_DEFAULT = "seacolour@nioz.nl";
	
	Context _this;
	
	String [] profilesDescription;
	
	PROFILES selectedProfile;
	boolean bIsProfileDefined = false;
	boolean bAskSecchiDisk = true;
	boolean bAskPlasticScale = true;
	boolean bDataUpload = true;
	String mailAdressSurvey = EMAIL_SURVEY_DEFAULT;
	
	public SettingsData(Context context){
		_this = context;
		profilesDescription = new String[6];
		profilesDescription[0] = "Student";
		profilesDescription[1] = "Researcher";
		profilesDescription[2] = "Decision and Policy Maker";
		profilesDescription[3] = "Volunteer";
		profilesDescription[4] = "Observer";
		profilesDescription[5] = "Tourist/Vacationer";
		
		bIsProfileDefined = false;;
		bAskSecchiDisk = true;
		bAskPlasticScale = true;
		bDataUpload = true;
		mailAdressSurvey = EMAIL_SURVEY_DEFAULT;
	}
	
	public void LoadData(){
		SharedPreferences prefs = _this.getSharedPreferences(PREFERENCES_PROFILES, Context.MODE_PRIVATE);
		String profileValue = prefs.getString(PROFILE_KEY, "");
		if (profileValue.trim().length() <= 0) bIsProfileDefined = false;
		else {
			try{
				selectedProfile = PROFILES.valueOf(profileValue);
				bIsProfileDefined = true;
			} catch (Exception e){
				bIsProfileDefined = false;
			}
		}		
		bAskSecchiDisk = prefs.getBoolean(ASK_SECCHI_DISK_KEY, true);
		bAskPlasticScale = prefs.getBoolean(ASK_PLASTIC_SCALE_KEY, true);
		mailAdressSurvey = prefs.getString(MAIL_ADDRESS_SURVEY_KEY, EMAIL_SURVEY_DEFAULT);
		bDataUpload = prefs.getBoolean(DATA_UPLOAD_KEY, true);
	}
	/*
	 * Get profiles
	 */
	public String[] getDescriptionProfiles(){
		return profilesDescription;
	}
	/*
	 * Gets the Ask Secchi Disk Preference 
	 */
	public boolean getAskSecchiDiskPreference(){
		return bAskSecchiDisk;
	}
	/*
	 * Gets the Ask Plastic Scale Preference 
	 */
	public boolean getAskPlasticScalePreference(){
		return bAskPlasticScale;
	}
	/*
	 * Gets the Data Upload Value 
	 */
	public boolean getDataUploadPreference(){
		return bDataUpload;
	}
	/*
	 * Gets the Mail Survey Address 
	 */
	public String getMailAddressSurvey(){
		return mailAdressSurvey;
	}
	public void setCommPreferences(boolean bDataUpload){
		SharedPreferences prefs = _this.getSharedPreferences(PREFERENCES_PROFILES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(DATA_UPLOAD_KEY, bDataUpload);
		editor.commit();			
	}
	/*
	 * Sets the ask questions value
	 */
	public void setAskQuestionsPreferences(boolean askSecchiDisk, boolean askPlasticScale){
		SharedPreferences prefs = _this.getSharedPreferences(PREFERENCES_PROFILES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(ASK_SECCHI_DISK_KEY, askSecchiDisk);
		editor.putBoolean(ASK_PLASTIC_SCALE_KEY, askPlasticScale);
		editor.commit();			
	}
	/*
	 * Sets the user profile by index
	 */
	public void setProfileByIndex(int profileIndex){
		String PROFILE = "";
		if (profileIndex == 0) PROFILE =  PROFILES.STUDENT.toString();
		else if (profileIndex == 1) PROFILE =  PROFILES.RESEARCHER.toString();
		else if (profileIndex == 2) PROFILE =  PROFILES.POLICY.toString();
		else if (profileIndex == 3) PROFILE =  PROFILES.VOLUNTEER.toString();
		else if (profileIndex == 4) PROFILE =  PROFILES.OBSERVER.toString();
		else if (profileIndex == 5) PROFILE =  PROFILES.TOURIST.toString();

		if (PROFILE.length() >0 ){
			SharedPreferences prefs = _this.getSharedPreferences(PREFERENCES_PROFILES, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(PROFILE_KEY, PROFILE);
			editor.commit();			
		}
	}
	/*
	 * Gets if profile is set
	 */
	public boolean IsProfileInformed(){
		return bIsProfileDefined;
	}
	/*
	 * Gets the string of selected profile
	 */
	public String getSelectedProfileCode(){
		String retorn = "";
		int idxSelectedProfile = getSelectedProfile();
		switch (idxSelectedProfile){
		case 0:
			retorn = PROFILES.STUDENT.toString();
			break;
		case 1:
			retorn = PROFILES.RESEARCHER.toString();
			break;
		case 2:
			retorn = PROFILES.POLICY.toString();
			break;
		case 3:
			retorn = PROFILES.VOLUNTEER.toString();
			break;
		case 4:
			retorn = PROFILES.OBSERVER.toString();
			break;
		case 5:
			retorn = PROFILES.TOURIST.toString();
			break;			
		}
		return retorn;
	}
	/*
	 * Get Index of Selected Profile
	 */
	public int getSelectedProfile(){
		int retorn = -1;
		if (bIsProfileDefined){
			try{
				if (selectedProfile == PROFILES.STUDENT) retorn = 0;
				else if (selectedProfile == PROFILES.RESEARCHER) retorn = 1;
				else if (selectedProfile == PROFILES.POLICY) retorn = 2;
				else if (selectedProfile == PROFILES.VOLUNTEER) retorn = 3;
				else if (selectedProfile == PROFILES.OBSERVER) retorn = 4;
				else if (selectedProfile == PROFILES.TOURIST) retorn = 5;					
			} catch (Exception e){
				retorn = -1;
			}			
		}
		return retorn;
	}	
}
