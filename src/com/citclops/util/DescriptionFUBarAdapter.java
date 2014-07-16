package com.citclops.util;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DescriptionFUBarAdapter extends ArrayAdapter<String>{ 
	private Typeface tipusLletra = null;
	private int colorTexte = -99;
	private float sizeTexte = -99;
	private int backgroundColor = -99;
	ArrayList<String> values; 

    public DescriptionFUBarAdapter(Context context, int textViewResourceId, ArrayList<String> values) { 
        super(context, textViewResourceId, values); 
        this.values = values; 
    } 
    public int getCount(){ 
        return values.size(); 
     }   
     public String getItem(int position){    	 
        return values.get(position); 
     }   
     public long getItemId(int position){ 
        return position; 
     } 
     public int getPositionByValor(String Valor){
    	 int retorn = -1;
    	 for (int i = 0; i < values.size(); i++){
    		 if (values.get(i).trim().equalsIgnoreCase(Valor.trim())){
    			 retorn = i;
    			 break;
    		 }
    	 }
    	 return retorn;
     }
     public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
         View v =super.getDropDownView(position, convertView, parent);
			if(sizeTexte!=-99) ((TextView) v).setTextSize(sizeTexte);
			if (tipusLletra!=null)((TextView) v).setTypeface(tipusLletra);
 			if(colorTexte!=-99) ((TextView) v).setTextColor(colorTexte);	
 			if(backgroundColor!=-99) v.setBackgroundColor(backgroundColor);

        return v;
     }
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = super.getView(position, convertView, parent);
 		if (view instanceof TextView) {
 			if(sizeTexte!=-99) ((TextView) view).setTextSize(sizeTexte);
 			if (tipusLletra!=null)((TextView) view).setTypeface(tipusLletra);
 			((TextView) view).setBackgroundColor(Color.TRANSPARENT);
 			if(colorTexte!=-99) ((TextView) view).setTextColor(colorTexte);	
 		}
 		return view;
 	}	
 	public void setTextColor(int colorLletra) {
 		colorTexte = colorLletra;
 	}	
 	/**
 	 * @param tipusLletra the tipusLletra to set
 	 */
 	public void setTipusLletra(Typeface tipusLletra) {
 		this.tipusLletra = tipusLletra;
 	}
 	/**
 	 * @return the tipusLletra
 	 */
 	public Typeface getTipusLletra() {
 		return tipusLletra;
 	}
 	public void setTextSize(float tamanyLletra) {
 		sizeTexte = tamanyLletra;
 	}	
 	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
}
