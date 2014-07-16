package com.citclops.widgets;

import java.io.File;
import java.util.Date;

import com.citclops.mobile.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

public class kImageButton extends ImageButton {
	Context context;									// Context
	Animation animMaginify;								// Referencia a la animación
	Animation animUnmaginify;							// Referencia a la animación
	boolean _bTristatePushed = false;					// Booleana para saber si el botón es triestado
	boolean _bTristateChanged = false;					// Booleana para saber si el botón es triestado
	boolean _bPulsado = false;							// Booleana para saber si estamos en estado pulsado
	private int _resourceChangeID = -1;					// Imagen resource para el cambio de estado
	private int _resourceID = -1;						// Imagen resource para el cambio de estado
	private Bitmap _bmpResource = null;					// Bitmap del Resource 
	private Bitmap _bmpResourceChanged = null;			// Bitmap del Resource Changed
	private long MILLISECONDS_ALLOW_CLICK = 200;		// Milisegundos a partir de los que permitiremos un nuevo click 
	private long millisecondsLastClick = 0;				// Almacenamos los milisegundos del last click
	private int widgetWidth = 0;						// Width del Widget
	private int widgetHeight = 0;						// Height del Widget

    public kImageButton(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        commonInitialisation();
        commonAttributes(attrs);
    }

    public kImageButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        commonInitialisation();
        commonAttributes(attrs);
    }
    
    public kImageButton(final Context context) {
        super(context);
        this.context = context;
        commonInitialisation();
    }
    /************************************/
    /******* A t r i b u t o s   ********/
    /************************************/    
    private void commonAttributes(AttributeSet attrs){
	    // Obtenemos las propiedades del layout XML
	    TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.KImageButton);	     
	    _resourceID = a.getResourceId(R.styleable.KImageButton_srcBase, -1);
	    _resourceChangeID = a.getResourceId(R.styleable.KImageButton_srcChanged, -1);
	    
	    // En estado inicial, aplicamos la imagen
	    if (_resourceID != -1) this.setImageResource(_resourceID);

	    // Cargamos la imagen de fondo desde ls URL
	    String txt = a.getString(R.styleable.KImageButton_pathImgBase);
	    if (txt != null) setImageFromPath(txt);

	    // Cargamos la imagen cambiada de fondo desde ls URL
	    txt = a.getString(R.styleable.KImageButton_pathImgChanged);
	    if (txt != null) setImageChangedFromPath(txt);

		_bTristatePushed = false;
		_bTristateChanged = false;
	    boolean b = a.getBoolean(R.styleable.KImageButton_tristateChanged, false);
	    if (b) setTristateChanged(true);
	    b = a.getBoolean(R.styleable.KImageButton_tristatePushed, false);
	    if (b) setTristatePushed(true);	  

	    // Recycle
	    a.recycle();
    }
    /************************************/
    /********** M é t o d o s    ********/
    /************************************/
	/**
	 * Libera las imágenes y los recursos del widget
	 */
    public void recycle(){
    	if (_bmpResource != null) _bmpResource.recycle();
    	if (_bmpResourceChanged != null) _bmpResourceChanged.recycle();    	
    }
	/**
	 * Asigna la imagen del widget desde path
	 */
    public void setImageFromPath(String path){
    	File f = null;
    	try{
    		f = new File(path);
    		if (f.exists()){
    			_bmpResource = BitmapFactory.decodeFile(path);
    			this.setImageBitmap(_bmpResource);
    		}    		
    	} catch (Exception e){
    		// No cargamos la imagen
    	} finally{
    		
    	}
    }
	/**
	 * Asigna la imagen cambiada
	 */
    public void setImageChangedFromPath(String path){
    	File f = null;
    	try{
    		f = new File(path);
    		if (f.exists()){
    			_bmpResourceChanged = BitmapFactory.decodeFile(path);
    		}    		
    	} catch (Exception e){
    		// No cargamos la imagen
    	} finally{
    		
    	}    	
    }
	/**
	 * Obtiene si el botón está pulsado
	 */
    public boolean getPushed(){
    	return _bPulsado; 
    }
	/**
	 * Asigna el botón como no pulsado
	 */
    public void setNotPushed(){
    	// Inicializamos
    	_bPulsado = false; 
   	 	startAnimation(animUnmaginify);
     	if (_bTristatePushed){
     		animUnmaginify.setFillAfter(true);
     	 } else if (_bTristateChanged) {
        	 // Actualizamos a la imagen inicial
           	 if (_bmpResource != null) this.setImageBitmap(_bmpResource);
           	 else if (_resourceID != -1) this.setImageResource(_resourceID);
     	 }
    }
	/**
	 * Asigna el botón como pulsado
	 */
    public void setPushed(){
   	 	_bPulsado = true;
      	 startAnimation(animMaginify);
      	 if (_bTristatePushed){
      		 animMaginify.setFillAfter(true);
      	 } else if (_bTristateChanged) {
           	 if (_bmpResourceChanged!= null) this.setImageBitmap(_bmpResourceChanged);
           	 else if (_resourceChangeID != -1) this.setImageResource(_resourceChangeID);
      	 }
    }    
	/**
	 * Obtiene si el botón es triestado en modo pushed
	 */
    public boolean getTristatePushed(){
    	return _bTristatePushed; 
    }
	/**
	 * Establece el botón triestado pushed
	 */
    public void setTristatePushed(boolean isTristate){
    	_bTristatePushed = isTristate; 
    	_bTristateChanged = false;
    }
	/**
	 * Obtiene si el botón es triestado
	 */
    public boolean getTristateChanged(){
    	return _bTristateChanged; 
    }
	/**
	 * Establece el botón triestado
	 */
    public void setTristateChanged(boolean isTristate){
    	_bTristateChanged = isTristate; 
    	_bTristatePushed = false;
    }    
	/**
	 * Establece el recurso de la imagen changed
	 */
    public void setImageChange(int ResourceChangeID){
    	_resourceChangeID = ResourceChangeID;
    }
    /*************************************/
    /******* O v e r r i d e s    ********/
    /*************************************/    
	/**
	 * Aplicamos alpha con el botón desactivado
	 */
    @Override
    public void setEnabled(boolean isEnabled){
    	super.setEnabled(isEnabled);
    	super.setClickable(isEnabled);
    	if (!isEnabled) super.setAlpha(100);
    	else super.setAlpha(255);
    }
    
    @Override 
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	widgetWidth = w;
    	widgetHeight = h;
    } 
    /***************************************************/
    /********** I n i c i l i z a c i o n e s   ********/
    /***************************************************/
	/**
	 * Inicializaciones
	 */
    protected final void commonInitialisation() {
    	// Color de fondo transparente
    	this.setBackgroundColor(Color.TRANSPARENT);
    	
    	this.setScaleType(ScaleType.FIT_CENTER);
    	
    	// Cargamos la animación
		animMaginify = AnimationUtils.loadAnimation( context, R.anim.magnify);
		animUnmaginify = AnimationUtils.loadAnimation( context, R.anim.unmagnify);
    	    	
		// Asignamos el evento de magnify
    	setOnTouchListener(new View.OnTouchListener() {			
			public boolean onTouch(View v, MotionEvent event) {				
				 switch (event.getAction()) {
	             case MotionEvent.ACTION_UP: {
	     				// Obtenemos si permitimos el click. 
	            	 	boolean retorn = false;
	     			    long millisecondsClick = new Date().getTime();			    
	     			    if ((millisecondsClick - millisecondsLastClick) < MILLISECONDS_ALLOW_CLICK) retorn = true;
	     			    millisecondsLastClick = millisecondsClick;
	     			    if (retorn) return true;
	     			    else {
	     			    	if ((event.getX() < 0) || (event.getY() < 0) || (event.getX() > widgetWidth) || (event.getY() > widgetHeight)){
	     			    		// UP fuera del control. No se produce el click
	     		            	 if (_bTristatePushed) {
			   	            		 // Botón triestado Pushed (Dejamos pulsado el botón)
			   	            		 if (_bPulsado){
			   	            			 // Botón pulsado. Pasamos a estado inicial
			   	            			 _bPulsado = false;
			   			            	 startAnimation(animUnmaginify);
			   			            	 animUnmaginify.setFillAfter(true);
			   	            		 } else {
			   	            			 // Botón NO pulsado. Pasamos a estado pulsado
			   	            			 _bPulsado = true;
			   			            	 startAnimation(animMaginify);
			   			            	 animMaginify.setFillAfter(true);	            			 
			   	            		 }
			   	            	 } else {
			   	            		 // Botón triestado Changed (Cambio de imagen)
			   	            		 if (_bPulsado){
			   	            			 // Botón pulsado. Pasamos a estado inicial
			   	            			 _bPulsado = false;
			   			            	 startAnimation(animUnmaginify);
			   			            	 if (_bmpResource != null) ((ImageButton)v).setImageBitmap(_bmpResource);
			   			            	 else if (_resourceID != -1) ((ImageButton)v).setImageResource(_resourceID);
			   	            		 } else {
			   	            			 // Botón NO pulsado. Pasamos a estado pulsado
			   	            			 _bPulsado = true;
			   			            	 startAnimation(animMaginify);
			   			            	 if (_bmpResourceChanged!= null) ((ImageButton)v).setImageBitmap(_bmpResourceChanged);
			   			            	 else if (_resourceChangeID != -1) ((ImageButton)v).setImageResource(_resourceChangeID);
			   	            		 }
			   	            	 }
	     			    	}	     			    	
	     			    }
	            	 break;
	             }
	             case MotionEvent.ACTION_DOWN: {
	            	 if ((!_bTristatePushed) && (!_bTristateChanged)){
	     				// Obtenemos si permitimos el click. 
	     			    long millisecondsClick = new Date().getTime();			    
	     			    if ((millisecondsClick - millisecondsLastClick) < MILLISECONDS_ALLOW_CLICK) return true;
		     			    
	            		 // No es triestado. Sólo hacemos animación pulsado
	            		 _bPulsado = false;
		            	 startAnimation(animMaginify);
		            	 animMaginify.setFillAfter(false);		            	 		            	 
	            	 } else if (_bTristatePushed) {
	            		 // Botón triestado Pushed (Dejamos pulsado el botón)
	            		 if (_bPulsado){
	            			 // Botón pulsado. Pasamos a estado inicial
	            			 _bPulsado = false;
			            	 startAnimation(animUnmaginify);
			            	 animUnmaginify.setFillAfter(true);
	            		 } else {
	            			 // Botón NO pulsado. Pasamos a estado pulsado
	            			 _bPulsado = true;
			            	 startAnimation(animMaginify);
			            	 animMaginify.setFillAfter(true);	            			 
	            		 }
	            	 } else {
	            		 // Botón triestado Changed (Cambio de imagen)
	            		 if (_bPulsado){
	            			 // Botón pulsado. Pasamos a estado inicial
	            			 _bPulsado = false;
			            	 startAnimation(animUnmaginify);
			            	 if (_bmpResource != null) ((ImageButton)v).setImageBitmap(_bmpResource);
			            	 else if (_resourceID != -1) ((ImageButton)v).setImageResource(_resourceID);
	            		 } else {
	            			 // Botón NO pulsado. Pasamos a estado pulsado
	            			 _bPulsado = true;
			            	 startAnimation(animMaginify);
			            	 if (_bmpResourceChanged!= null) ((ImageButton)v).setImageBitmap(_bmpResourceChanged);
			            	 else if (_resourceChangeID != -1) ((ImageButton)v).setImageResource(_resourceChangeID);
	            		 }
	            	 }
	                 break;
	             	}	             
				 }
		         return false;
			}
        });
    }
}
