package com.citclops.util;

import android.content.Context;

public class DispositiuFisic {
	static private String _instance = "";
	/**
    * @Retorna el valor del dispositiu físic
    */
	static public String getID(Context c) {
		if(_instance.length() <= 0) _instance = android.provider.Settings.Secure.getString(c.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID).toUpperCase().trim();
		return _instance;
	}
}
