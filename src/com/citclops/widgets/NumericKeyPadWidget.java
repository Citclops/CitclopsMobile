package com.citclops.widgets;

import java.util.EventListener;

import com.citclops.mobile.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NumericKeyPadWidget extends FrameLayout{
	ImageButton bt0;
	ImageButton bt1;
	ImageButton bt2;
	ImageButton bt3;
	ImageButton bt4;
	ImageButton bt5;
	ImageButton bt6;
	ImageButton bt7;
	ImageButton bt8;
	ImageButton bt9;	
	ImageButton btPoint;
	ImageButton btDelete;
	ImageButton btEnter;
	TextView numberText;
	String _value = "";
    newValueListener mNewValueListener;
    boolean bOnlyInteger = false;
    final String decimalSeparator = ".";    				// parseFloat is not locale parse, so the separator is always dot
    boolean bInitValue = true;
    final Integer MAX_LENGTH = 4;							
    
	/**
	 * Constructor
	 */
    public NumericKeyPadWidget(Context context){
        super(context);
        
        // Init Widget
        initWidget(context);
    }
    /**
	* @ Constructor
	*/
	public NumericKeyPadWidget(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    
        // Init Widget
        initWidget(context);
	}    
	/*
	 * Sets if allow only integer values
	 */
	public void setOnlyInteger(boolean onlyInteger){
		bOnlyInteger = onlyInteger;		
		if (bOnlyInteger) btPoint.setEnabled(false);
		else btPoint.setEnabled(true);			
	}
    /*
     * Inits the widget
     */
    private void initWidget(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.numeric_layout, this, true);
        LinearLayout lytROOT = (LinearLayout)getChildAt(0);
        
        // Get Text References
        numberText = (TextView)lytROOT.getChildAt(0);
        
        // Get button references
        LinearLayout l1 = (LinearLayout)lytROOT.getChildAt(1);
        bt1 = (ImageButton)l1.getChildAt(0);
        bt2 = (ImageButton)l1.getChildAt(1);
        bt3 = (ImageButton)l1.getChildAt(2);
        LinearLayout l2 = (LinearLayout)lytROOT.getChildAt(2);
        bt4 = (ImageButton)l2.getChildAt(0);
        bt5 = (ImageButton)l2.getChildAt(1);
        bt6 = (ImageButton)l2.getChildAt(2);
        LinearLayout l3 = (LinearLayout)lytROOT.getChildAt(3);
        bt7 = (ImageButton)l3.getChildAt(0);
        bt8 = (ImageButton)l3.getChildAt(1);
        bt9 = (ImageButton)l3.getChildAt(2);
        LinearLayout l4 = (LinearLayout)lytROOT.getChildAt(4);
        btDelete = (ImageButton)l4.getChildAt(0);
        bt0 = (ImageButton)l4.getChildAt(1);
        btEnter = (ImageButton)l4.getChildAt(2);
        LinearLayout l5 = (LinearLayout)lytROOT.getChildAt(5);
        btPoint = (ImageButton)l5.getChildAt(1);
                
        // Assign Listeners
        bt0.setOnClickListener(listenerButton);
    	bt1.setOnClickListener(listenerButton);
    	bt2.setOnClickListener(listenerButton);
    	bt3.setOnClickListener(listenerButton);
    	bt4.setOnClickListener(listenerButton);
    	bt5.setOnClickListener(listenerButton);
    	bt6.setOnClickListener(listenerButton);
    	bt7.setOnClickListener(listenerButton);
    	bt8.setOnClickListener(listenerButton);
    	bt9.setOnClickListener(listenerButton);
    	btPoint.setOnClickListener(listenerButton);
    	btDelete.setOnClickListener(listenerButton);
    	btEnter.setOnClickListener(listenerButton);
    	
    	// Sets if allows float values
    	setOnlyInteger(false);
    	
    	// Inform that is Init Value
    	bInitValue = true;
    	
		// Update for new Value
		checkValue();			
    }
    /*
     * Init Value
     */
    public void InitValue(){
    	// Inform that is Init Value
		bInitValue = true;
		if (bOnlyInteger) _value = "0";
		else _value = "0.0";
    	
		// Update for new Value
		checkValue();			    	
    }
    OnClickListener listenerButton = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
			if (_value.length() <= MAX_LENGTH){
				if (bInitValue){
					_value = "";		
					bInitValue = false;
				}			
				if (arg0.equals(bt0)){
					_value += "0";								
				} else if (arg0.equals(bt1)){
					_value += "1";								
				} else if (arg0.equals(bt2)){
					_value += "2";								
				} else if (arg0.equals(bt3)){
					_value += "3";								
				} else if (arg0.equals(bt4)){
					_value += "4";								
				} else if (arg0.equals(bt5)){
					_value += "5";								
				} else if (arg0.equals(bt6)){
					_value += "6";								
				} else if (arg0.equals(bt7)){
					_value += "7";								
				} else if (arg0.equals(bt8)){
					_value += "8";								
				} else if (arg0.equals(bt9)){
					_value += "9";				
				} else if (arg0.equals(btPoint)){
					_value += decimalSeparator;
				} 
			}
			
			if (arg0.equals(btDelete)){
				bInitValue = true;
    			if (bOnlyInteger) _value = "0";
    			else _value = "0.0";
			} else if (arg0.equals(btEnter)){
				if (mNewValueListener != null) mNewValueListener.newValue(checkValue());
			}
			
			// Check for the new Value
			checkValue();			
		}    	
    };
    /*
     * Check the string of entered value
     */
    private float checkValue(){
    	float retorn = 0.0f;
    	try{
    		_value = _value.trim();
    		if ((_value.trim().length() <= 0) || (_value.equals("0")) || (_value.equals("0.0")))  {
    			if (bOnlyInteger) _value = "0";
    			else _value = "0.0";
    			retorn = 0.0f;
    		} else {
    			// Check for value
    			boolean bRetorn = false;
    			try{
    				// parseFloat is not locale parse, so the separator is always dot
    				String sNewValue = _value;
    				sNewValue = sNewValue.replace(',', '.');
    				if (sNewValue.endsWith(".")) sNewValue += "0";
    				retorn = Float.parseFloat(sNewValue);
    			} catch (Exception e2){
    				bRetorn = true;
    			} finally{ }
    			
    			// Bad format value
    			if (bRetorn){
    				if (_value.trim().length() > 1){
    					_value = _value.substring(0, _value.trim().length() - 2);
    					retorn = checkValue();
    				} else {
    	    			if (bOnlyInteger) _value = "0";
    	    			else _value = "0.0";
    	    			retorn = 0.0f;
    				}
    			}
    		}
    	} catch (Exception e){
			if (bOnlyInteger) _value = "0";
			else _value = "0.0";
			retorn = 0.0f;
    	} finally{
    		
    	}    	
    	
    	// Update UI
    	numberText.setText(_value);
    	
    	// Return final value    	
    	return retorn;
    }
    /****************************************/
    /***** I n t e r f a c e s **************/
    /****************************************/
	/*
	 * Listener to inform final value
	 */
	public interface newValueListener extends EventListener{
		public void newValue (float newValue);
	}
    /*
     * Sets Final Value Listener
     */
    public void setNewValueListener(newValueListener eventListener) {
    	mNewValueListener = eventListener;
    }
}
