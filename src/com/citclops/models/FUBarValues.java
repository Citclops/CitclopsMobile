package com.citclops.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class FUBarValues {
	
	private Context _this;															//Context
	
	private Integer[] _fuscaleValoR;												//Array color Red
	private Integer[] _fuscaleValoG;												//Array color Green
	private Integer[] _fuscaleValoB;												//Array color Blue
	
	private Integer[] _fuscaleValoR_M;												//Array color Red Medium
	private Integer[] _fuscaleValoG_M;												//Array color Green Medium 
	private Integer[] _fuscaleValoB_M;												//Array color Blue Medium

	private Integer[] _fuscaleValoR_B;												//Array color Red Bottom
	private Integer[] _fuscaleValoG_B;												//Array color Green Bottom 
	private Integer[] _fuscaleValoB_B;												//Array color Blue Bottom
	
	private Boolean[] _fuscaleVisibility;											//Array visibilite true or false

	//Parameters specified in the constructor
	public FUBarValues(Context thisParameter){
		_this = thisParameter;
		_fuscaleValoR = new Integer[21];
		_fuscaleValoG = new Integer[21];
		_fuscaleValoB = new Integer[21];
		_fuscaleVisibility = new Boolean[21];
		_fuscaleValoR_M = new Integer[21];
		_fuscaleValoG_M = new Integer[21];
		_fuscaleValoB_M = new Integer[21];
		_fuscaleValoR_B = new Integer[21];
		_fuscaleValoG_B = new Integer[21];
		_fuscaleValoB_B = new Integer[21];
	}
	
	//Defaults values bars
	public void Clear(){
	    				
		_fuscaleValoR[0] = 57; 		_fuscaleValoG[0] = 80; 		_fuscaleValoB[0] = 181;		
		_fuscaleValoR[1] = 51; 		_fuscaleValoG[1] = 105; 	_fuscaleValoB[1] = 180;
		_fuscaleValoR[2] = 65; 		_fuscaleValoG[2] = 126; 	_fuscaleValoB[2] = 171;
	    _fuscaleValoR[3] = 63; 		_fuscaleValoG[3] = 119; 	_fuscaleValoB[3] = 137;	    
	    _fuscaleValoR[4] = 58; 		_fuscaleValoG[4] = 113; 	_fuscaleValoB[4] = 116;
	    _fuscaleValoR[5] = 57; 		_fuscaleValoG[5] = 118; 	_fuscaleValoB[5] = 111;
	    _fuscaleValoR[6] = 61; 		_fuscaleValoG[6] = 121; 	_fuscaleValoB[6] = 104;
	    _fuscaleValoR[7] = 63; 		_fuscaleValoG[7] = 125; 	_fuscaleValoB[7] = 97;
	    _fuscaleValoR[8] = 66; 		_fuscaleValoG[8] = 131; 	_fuscaleValoB[8] = 87;
	    _fuscaleValoR[9] = 92; 		_fuscaleValoG[9] = 151; 	_fuscaleValoB[9] = 85;
	    _fuscaleValoR[10] = 122; 	_fuscaleValoG[10] = 186; 	_fuscaleValoB[10] = 86;
	    _fuscaleValoR[11] = 127; 	_fuscaleValoG[11] = 168; 	_fuscaleValoB[11] = 83;
	    _fuscaleValoR[12] = 138; 	_fuscaleValoG[12] = 165; 	_fuscaleValoB[12] = 89;
	    _fuscaleValoR[13] = 150; 	_fuscaleValoG[13] = 167; 	_fuscaleValoB[13] = 95;
	    _fuscaleValoR[14] = 160; 	_fuscaleValoG[14] = 164; 	_fuscaleValoB[14] = 97;
	    _fuscaleValoR[15] = 168; 	_fuscaleValoG[15] = 162; 	_fuscaleValoB[15] = 98;
	    _fuscaleValoR[16] = 165; 	_fuscaleValoG[16] = 148; 	_fuscaleValoB[16] = 93;
	    _fuscaleValoR[17] = 159; 	_fuscaleValoG[17] = 134; 	_fuscaleValoB[17] = 87;
	    _fuscaleValoR[18] = 168; 	_fuscaleValoG[18] = 130; 	_fuscaleValoB[18] = 89;
	    _fuscaleValoR[19] = 177; 	_fuscaleValoG[19] = 126; 	_fuscaleValoB[19] = 91;	    
	    _fuscaleValoR[20] = 165; 	_fuscaleValoG[20] = 111; 	_fuscaleValoB[20] = 82;	    		

		_fuscaleValoR_M[0] = 0; 	_fuscaleValoG_M[0] = 68; 	_fuscaleValoB_M[0] = 206;		
		_fuscaleValoR_M[1] = 0; 	_fuscaleValoG_M[1] = 100; 	_fuscaleValoB_M[1] = 204;
		_fuscaleValoR_M[2] = 0; 	_fuscaleValoG_M[2] = 132; 	_fuscaleValoB_M[2] = 207;
	    _fuscaleValoR_M[3] = 0; 	_fuscaleValoG_M[3] = 127; 	_fuscaleValoB_M[3] = 160;
	    _fuscaleValoR_M[4] = 0; 	_fuscaleValoG_M[4] = 124; 	_fuscaleValoB_M[4] = 130;	    
	    _fuscaleValoR_M[5] = 0; 	_fuscaleValoG_M[5] = 126; 	_fuscaleValoB_M[5] = 112;
	    _fuscaleValoR_M[6] = 0; 	_fuscaleValoG_M[6] = 128; 	_fuscaleValoB_M[6] = 98;
	    _fuscaleValoR_M[7] = 0; 	_fuscaleValoG_M[7] = 131; 	_fuscaleValoB_M[7] = 85;
	    _fuscaleValoR_M[8] = 19; 	_fuscaleValoG_M[8] = 114; 	_fuscaleValoB_M[8] = 60;
	    _fuscaleValoR_M[9] = 61; 	_fuscaleValoG_M[9] = 133; 	_fuscaleValoB_M[9] = 47;
	    _fuscaleValoR_M[10] = 92; 	_fuscaleValoG_M[10] = 161; 	_fuscaleValoB_M[10] = 33;
	    _fuscaleValoR_M[11] = 105; 	_fuscaleValoG_M[11] = 153; 	_fuscaleValoB_M[11] = 26;
	    _fuscaleValoR_M[12] = 117; 	_fuscaleValoG_M[12] = 151; 	_fuscaleValoB_M[12] = 27;
	    _fuscaleValoR_M[13] = 133; 	_fuscaleValoG_M[13] = 153; 	_fuscaleValoB_M[13] = 37;
	    _fuscaleValoR_M[14] = 150; 	_fuscaleValoG_M[14] = 155; 	_fuscaleValoB_M[14] = 43;	    
	    _fuscaleValoR_M[15] = 144; 	_fuscaleValoG_M[15] = 140; 	_fuscaleValoB_M[15] = 57;
	    _fuscaleValoR_M[16] = 141; 	_fuscaleValoG_M[16] = 123; 	_fuscaleValoB_M[16] = 54;
	    _fuscaleValoR_M[17] = 131; 	_fuscaleValoG_M[17] = 105; 	_fuscaleValoB_M[17] = 48;
	    _fuscaleValoR_M[18] = 127; 	_fuscaleValoG_M[18] = 90; 	_fuscaleValoB_M[18] = 46;
	    _fuscaleValoR_M[19] = 128; 	_fuscaleValoG_M[19] = 83; 	_fuscaleValoB_M[19] = 46;	    
	    _fuscaleValoR_M[20] = 122; 	_fuscaleValoG_M[20] = 72; 	_fuscaleValoB_M[20] = 44;	    		
	   	    
		_fuscaleValoR_B[0] = 0; 	_fuscaleValoG_B[0] = 48; 	_fuscaleValoB_B[0] = 151;		
		_fuscaleValoR_B[1] = 0; 	_fuscaleValoG_B[1] = 50; 	_fuscaleValoB_B[1] = 108;
		_fuscaleValoR_B[2] = 0; 	_fuscaleValoG_B[2] = 68; 	_fuscaleValoB_B[2] = 110;
	    _fuscaleValoR_B[3] = 0; 	_fuscaleValoG_B[3] = 65; 	_fuscaleValoB_B[3] = 83;
	    _fuscaleValoR_B[4] = 0; 	_fuscaleValoG_B[4] = 64; 	_fuscaleValoB_B[4] = 67;
	    _fuscaleValoR_B[5] = 0; 	_fuscaleValoG_B[5] = 64; 	_fuscaleValoB_B[5] = 57;
	    _fuscaleValoR_B[6] = 0; 	_fuscaleValoG_B[6] = 66; 	_fuscaleValoB_B[6] = 49;
	    _fuscaleValoR_B[7] = 0; 	_fuscaleValoG_B[7] = 67; 	_fuscaleValoB_B[7] = 42;
	    _fuscaleValoR_B[8] = 11; 	_fuscaleValoG_B[8] = 82; 	_fuscaleValoB_B[8] = 41;
	    _fuscaleValoR_B[9] = 35; 	_fuscaleValoG_B[9] = 82; 	_fuscaleValoB_B[9] = 26;	    
	    _fuscaleValoR_B[10] = 43; 	_fuscaleValoG_B[10] = 81; 	_fuscaleValoB_B[10] = 11;
	    _fuscaleValoR_B[11] = 59; 	_fuscaleValoG_B[11] = 88; 	_fuscaleValoB_B[11] = 11;
	    _fuscaleValoR_B[12] = 74; 	_fuscaleValoG_B[12] = 97; 	_fuscaleValoB_B[12] = 14;
	    _fuscaleValoR_B[13] = 89; 	_fuscaleValoG_B[13] = 104; 	_fuscaleValoB_B[13] = 22;
	    _fuscaleValoR_B[14] = 89; 	_fuscaleValoG_B[14] = 92; 	_fuscaleValoB_B[14] = 22;	    	    
	    _fuscaleValoR_B[15] = 88; 	_fuscaleValoG_B[15] = 85; 	_fuscaleValoB_B[15] = 32;
	    _fuscaleValoR_B[16] = 91; 	_fuscaleValoG_B[16] = 78; 	_fuscaleValoB_B[16] = 32;
	    _fuscaleValoR_B[17] = 95; 	_fuscaleValoG_B[17] = 75; 	_fuscaleValoB_B[17] = 33;
	    _fuscaleValoR_B[18] = 92; 	_fuscaleValoG_B[18] = 64; 	_fuscaleValoB_B[18] = 31;
	    _fuscaleValoR_B[19] = 88; 	_fuscaleValoG_B[19] = 56; 	_fuscaleValoB_B[19] = 29;	    
	    _fuscaleValoR_B[20] = 83; 	_fuscaleValoG_B[20] = 48; 	_fuscaleValoB_B[20] = 28;	    		
	    
		for (int i = 0; i < 21; i++){
			_fuscaleVisibility[i] = true;
		}
	}
	
	//We load values predetermined so as not.
	public void Load(){
		SharedPreferences prefs = _this.getSharedPreferences("DataRGB", Context.MODE_PRIVATE);
		
		if (!prefs.contains("SaveR0")){
			Clear();		
		} else {
			for(int i = 0; i < 21; i++){
				_fuscaleValoR[i] = prefs.getInt("SaveR" + i, 0);
				_fuscaleValoG[i] = prefs.getInt("SaveG" + i, 0);
				_fuscaleValoB[i] = prefs.getInt("SaveB" + i, 0);	
				_fuscaleVisibility[i] = prefs.getBoolean("SaveVisible" + i, true);
				_fuscaleValoR_M[i] = prefs.getInt("SaveR_M" + i, 0);
				_fuscaleValoG_M[i] = prefs.getInt("SaveG_M" + i, 0);
				_fuscaleValoB_M[i] = prefs.getInt("SaveB_M" + i, 0);	
				_fuscaleValoR_B[i] = prefs.getInt("SaveR_B" + i, 0);
				_fuscaleValoG_B[i] = prefs.getInt("SaveG_B" + i, 0);
				_fuscaleValoB_B[i] = prefs.getInt("SaveB_B" + i, 0);					
			}
		}
	}

	//We save values predetermined so as not.
	@SuppressLint("CommitPrefEdits")
	public void Save(){
		SharedPreferences prefs = _this.getSharedPreferences("DataRGB", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		for(int i = 0; i < 21; i++){
			editor.putInt("SaveR" + i, _fuscaleValoR[i]);
			editor.putInt("SaveG" + i, _fuscaleValoG[i]);
			editor.putInt("SaveB" + i, _fuscaleValoB[i]);
			editor.putBoolean("SaveVisible" + i, _fuscaleVisibility[i]);
			editor.putInt("SaveR_M" + i, _fuscaleValoR_M[i]);
			editor.putInt("SaveG_M" + i, _fuscaleValoG_M[i]);
			editor.putInt("SaveB_M" + i, _fuscaleValoB_M[i]);
			editor.putInt("SaveR_B" + i, _fuscaleValoR_B[i]);
			editor.putInt("SaveG_B" + i, _fuscaleValoG_B[i]);
			editor.putInt("SaveB_B" + i, _fuscaleValoB_B[i]);			
		}
		editor.commit();
	}

	//Methods of consultation
	public Integer []getFuscaleValoR(){
		return _fuscaleValoR;
	}
	public Integer []getFuscaleValoG(){
		return _fuscaleValoG;
	}
	public Integer []getFuscaleValoB(){
		return _fuscaleValoB;
	}	
	public Boolean []getVisibleFuscale(){
		return _fuscaleVisibility;
	}	
	public Integer []getFuscaleValoR_M(){
		return _fuscaleValoR_M;
	}
	public Integer []getFuscaleValoG_M(){
		return _fuscaleValoG_M;
	}
	public Integer []getFuscaleValoB_M(){
		return _fuscaleValoB_M;
	}	
	public Integer []getFuscaleValoR_B(){
		return _fuscaleValoR_B;
	}
	public Integer []getFuscaleValoG_B(){
		return _fuscaleValoG_B;
	}
	public Integer []getFuscaleValoB_B(){
		return _fuscaleValoB_B;
	}	
}
