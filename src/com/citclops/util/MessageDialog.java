
package com.citclops.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

public class MessageDialog {
	public static String InputValue = "";
	public static int SelectedIndex = -1;
	public static boolean bMessagedActivated = false;
    /**
	* @ Mostramos Dialog de Si / No 
	*/	
	public static void ShowDialogYesNo(Context context, String Title, String Message, String MsgButtonYES, String MsgButtonNO, DialogInterface.OnClickListener listenerYES, DialogInterface.OnClickListener listenerNO, boolean bAlert){
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle(Title);
    	builder.setMessage(Message);
    	if (bAlert) builder.setIcon(android.R.drawable.ic_dialog_alert);
    	else builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setPositiveButton(MsgButtonYES, listenerYES);
    	builder.setNegativeButton(MsgButtonNO, listenerNO);
    	builder.setCancelable(false);
    	bMessagedActivated = true;
    	builder.show();
	}
    /**
	* @ Mostramos Dialog  
	*/	
	public static void ShowDialog(Context context, String Title, String Message, String MsgButton, DialogInterface.OnClickListener listener, boolean bAlert){
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle(Title);
    	builder.setMessage(Message);
    	if (bAlert) builder.setIcon(android.R.drawable.ic_dialog_alert);
    	else builder.setIcon(android.R.drawable.ic_dialog_info);
    	builder.setPositiveButton(MsgButton, listener);
    	builder.setCancelable(false);
    	bMessagedActivated = true;
    	builder.show();
	}
	public static void InputBoxDialog(Context context, String Title, String MsgButtonOK, String MsgButtonCANCEL, DialogInterface.OnClickListener listenerOK, DialogInterface.OnClickListener listenerCANCEL){
		final FrameLayout fl = new FrameLayout(context);
		final EditText input = new EditText(context);
		input.setGravity(Gravity.CENTER);
		input.setTextSize(42);
		input.addTextChangedListener(new TextWatcher() { 
            public void afterTextChanged(Editable s) {} 
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {} 
            public void onTextChanged(CharSequence s, int start, int before, int count) { 
            	InputValue = s.toString();
            } 
		}); 
		
		fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		input.setText("");
		InputValue = "";
    	bMessagedActivated = true;
		new AlertDialog.Builder(context)
			.setView(fl)
			.setTitle(Title)
			.setCancelable(false)
			.setPositiveButton(MsgButtonOK, listenerOK) 
			.setNegativeButton(MsgButtonCANCEL, listenerCANCEL).create().show();
	}
	public static void InputSelectorDialog(Context context, String Title, String MsgButtonOK, String MsgButtonCANCEL, DialogInterface.OnClickListener listenerOK, DialogInterface.OnClickListener listenerCANCEL, String values[]){
		// Creamos nuestra lista de dispositivos
		String array_spinner[] = new String[values.length + 1];
		array_spinner[0] = " ";
		for (int i = 0; i < values.length; i++){
			array_spinner[i + 1] = values[i];			
		}

		// Definimos layout
		final FrameLayout fl = new FrameLayout(context);
		final Spinner spinner = new Spinner(context);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, array_spinner);		
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter); 		
		fl.addView(spinner, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		
		// Creamos el dialog
		final AlertDialog dialog = new AlertDialog.Builder(context)
			.setView(fl)
			.setTitle(Title)
			.setCancelable(false)
			.setPositiveButton(MsgButtonOK, listenerOK) 
			.setNegativeButton(MsgButtonCANCEL, listenerCANCEL).create();
		
		// Inicializamos el spinner
		spinner.setSelection(0);
		InputValue = "";
		SelectedIndex = -1;
    	bMessagedActivated = true;

		// Definimos eventos del spinner
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() { 
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	try{
		    		InputValue = parentView.getItemAtPosition(position).toString();
		    		SelectedIndex = position - 1;
		    		if (SelectedIndex < 0) dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
		    		else dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true); 
		    	}catch (Exception e){
		    		InputValue = "";
		    		SelectedIndex = -1;
		    		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false); 
		    	} finally{
		    		
		    	}
		    } 
		    public void onNothingSelected(AdapterView<?> parentView) { 
            	InputValue = "";
            	SelectedIndex = -1;
            	dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false); 
		    } 		 
		}); 
		
		// Mostramos el Dialog
		dialog.show();
		
		// Desactivamos el botón
    	dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false); 
	}
}
