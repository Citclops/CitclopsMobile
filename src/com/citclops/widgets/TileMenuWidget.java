package com.citclops.widgets;

import java.io.File;
import java.util.Calendar;
import java.util.EventListener;
import java.util.Random;

import com.citclops.mobile.R;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class TileMenuWidget  extends FrameLayout{

	Context context;										// Context
	View view;												// Referencia a la view misma
	private ScaleAnimation animHorizontal;					// Animación Horizontal
	private ScaleAnimation animVertical;					// Animación Vertical
	private int ANIMATION_MILLISECONDS = 150;				// Milisegundos de cada parte de la animación
	private int _intervalMilliseconds = 5000;				// Intervalo entre rotación y rotación 
	private boolean enabled = false;						// Para saber si tenemso activado las rotaciones 
	private String _txtReverse = "";						// Texto a mostrar en el reverso
	private boolean _bIsReversed = false;					// booleana para saber si estamos en reversed
	private TextView txtReversed = null;					// TextView para mostrar el Reversed
	private ImageView imgvw = null;							// ImageView con imagen de fondo 
	private int _reversedBackgroundColor;					// Color de fondo en modo reversed
	private int _imageWidth = 0;							// Width de la imagen de fondo
	private int _imageHeight = 0;							// Height de la imagen de fondo
	private Animation animMaginify;							// Referencia a la animación
	private Animation animUnmaginify;						// Referencia a la animación
	private boolean bIsPushed = false;						// Booleana para saber si tenemos el botón pulsado 
	private OnClickInterface mListener;						// Listener para activar le evento de click
	private Bitmap bmpFondo = null;							// Bitmap de Fondo 
	private Random rnd = null;								// Cálculo nuevo intervalo

	public TileMenuWidget(Context context) {
		super(context);
		this.view = this;
		this.context = context;
		inicializar();
	}

	public TileMenuWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.view = this;
		this.context = context;
		inicializar();
		
	    // Obtenemos las propiedades del layout XML
	    TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TileMenuWidget);
	    setIntervalMilliseconds(a.getInt(R.styleable.TileMenuWidget_IntervalMilliseconds, _intervalMilliseconds));
	    
	    // Texto Reversed
	    String txt = a.getString(R.styleable.TileMenuWidget_ReverseText);
	    if (txt != null) setReverseText(txt);

	    // Reversed Background Color
	    txt = a.getString(R.styleable.TileMenuWidget_ReversedBackgroundColor);
	    if (txt != null) {
	    	try{setReversedBackgroundColor(Color.parseColor(txt));
	    	} catch(Exception e){ 
	    		setReversedBackgroundColor(Color.TRANSPARENT);
	    	}
	    } else {
	    	setReversedBackgroundColor(Color.TRANSPARENT);
	    }

	    // Reversed Foreground Color
	    txt = a.getString(R.styleable.TileMenuWidget_ReversedForegroundColor);
	    if (txt != null) {
	    	try{setReversedForegroundColor(Color.parseColor(txt));
	    	} catch(Exception e){ 
	    		setReversedForegroundColor(Color.WHITE);
	    	}
	    } else {
	    	setReversedForegroundColor(Color.WHITE);
	    }

	    // Tamaño del Texto
	    setReversedTextSize(a.getFloat(R.styleable.TileMenuWidget_ReversedTextSize, 18.0f));

	    // URL de la imagen de fondo
	    txt = a.getString(R.styleable.TileMenuWidget_URLBackGroundImage);
	    if (txt != null) setBackgroundImageFromPath(txt);
	    
	    if (a!= null) a.recycle();
	}
	/**
	* @ Inicializa las propiedades del control
	*/
	private void inicializar() {
		// Instanciamos para crear nuevos valores aleatorios
    	rnd = new Random(Calendar.getInstance().getTimeInMillis());

	    // Creamos las animaciones
		animHorizontal = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animHorizontal.setDuration(ANIMATION_MILLISECONDS);
		animHorizontal.setInterpolator(new AccelerateInterpolator());
        final ScaleAnimation animX2 = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animX2.setDuration(ANIMATION_MILLISECONDS);
        animX2.setInterpolator(new AccelerateInterpolator());
        animHorizontal.setAnimationListener(new AnimationListener(){
			public void onAnimationEnd(Animation animation) {
				updateReversedUI();
				view.startAnimation(animX2);				
			}
			public void onAnimationRepeat(Animation animation) { }
			public void onAnimationStart(Animation animation) {}        	
        });
		animVertical = new ScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animVertical.setDuration(ANIMATION_MILLISECONDS);
		animVertical.setInterpolator(new AccelerateInterpolator());
        final ScaleAnimation animY2 = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animY2.setDuration(ANIMATION_MILLISECONDS);
        animY2.setInterpolator(new AccelerateInterpolator());
        animVertical.setAnimationListener(new AnimationListener(){
			public void onAnimationEnd(Animation animation) {
				updateReversedUI();
				view.startAnimation(animY2);				
			}
			public void onAnimationRepeat(Animation animation) { }
			public void onAnimationStart(Animation animation) {}        	
        });
        
        // Creamos el ImageView
        imgvw = new ImageView(context);
        FrameLayout.LayoutParams lytImgvwParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		imgvw.setScaleType(ScaleType.MATRIX); 
		Matrix matrix = new Matrix(); 
		imgvw.setImageMatrix(matrix); 
        ((ViewGroup)view).addView(imgvw, lytImgvwParams);
        
        // Creamos el TextView del Texto Reversed
        txtReversed = new TextView(context);
        FrameLayout.LayoutParams lytChildParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        txtReversed.setGravity(Gravity.CENTER);        
        ((ViewGroup)view).addView(txtReversed, lytChildParams);    
        
        // Inicialmente el color de fondo en modoo reversed es transparente
        setReversedBackgroundColor(Color.TRANSPARENT);
        setReversedForegroundColor(Color.WHITE);
        setReversedTextSize(18);
        
    	// Cargamos las animaciones
		animMaginify = AnimationUtils.loadAnimation( context, R.anim.magnify);
		animUnmaginify = AnimationUtils.loadAnimation( context, R.anim.unmagnify);

		// Asignamos el evento de magnify
    	setOnTouchListener(new View.OnTouchListener() {			
			public boolean onTouch(View v, MotionEvent event) {
				 switch (event.getAction()) {
		             case MotionEvent.ACTION_DOWN: 
		            	 bIsPushed = true;
		            	 startAnimation(animMaginify);
		            	 animMaginify.setFillAfter(true);		            	 
		                 break;		             	             
		             case MotionEvent.ACTION_UP:
		            	 bIsPushed = false;
		            	 startAnimation(animUnmaginify);
		            	 animUnmaginify.setFillAfter(false);	
		            	 if (mListener != null) mListener.OnClick(view);
		                 break;		            	 
		             case MotionEvent.ACTION_OUTSIDE:
		            	 bIsPushed = false;
		            	 startAnimation(animUnmaginify);
		            	 animUnmaginify.setFillAfter(false);		            	 
		                 break;
		             case MotionEvent.ACTION_MOVE:
		            	 bIsPushed = false;
		            	 startAnimation(animUnmaginify);
		            	 animUnmaginify.setFillAfter(false);		            	 
		                 break;
				 }
		         return true;
			}
        });
	}
	/**
	 * Libera las imágenes y los recursos del widget
	 */
    public void recycle(){
    	if (bmpFondo != null) bmpFondo.recycle(); 
    }
	/*******************************/
	/***** I n t e r f a c e  ******/
	/*******************************/		
	/**
	 * @ Interface para gestionar el click del widget
	*/	
	public interface OnClickInterface extends EventListener {
	    public void OnClick(View tile);
	}
	/**
	 * @ Assigna el Listener per controlar el rogres
	*/	
	public void setOnClick(OnClickInterface eventListener) {
		mListener = eventListener;
	}
	/******************************/
	/***** O v e r r i d e s  *****/
	/******************************/	
	/**
	* @ Actualizamos las medidas 
	*/
	@Override 	
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);

		//Actualizamos las medidas
	    updateReversedUI();	     
	}	
	/****************************************************/
	/***** G e t t e r s     y     S e t t e r s    *****/
	/****************************************************/	
	/**
	 * Asigna la imagen del widget desde path
	 */
    public void setBackgroundImageFromPath(String path){
    	File f = null;
    	try{
    		f = new File(path);
    		if (f.exists()){
    			bmpFondo = BitmapFactory.decodeFile(path);
    			setBackGroundImage(bmpFondo);
    		}    		
    	} catch (Exception e){
    		// No cargamos la imagen
    	} finally{
    		
    	}
    }
	/**
	* @ Establece el Texto del Reverso 
	*/
	public void setReverseText(String ReverseText){
		_txtReverse = ReverseText;
		if (_txtReverse.trim().length() <= 0){
			// Posicionamos el Tile en posición natural			
			_bIsReversed = false;
			updateReversedUI();
		}
	}
	/**
	* @ El Intervalo en Milisegundos entre rotaciones
	*/
	public void setIntervalMilliseconds(int IntervalMilliseconds){
		_intervalMilliseconds = IntervalMilliseconds;
	}
	/**
	* @ Color de Fondo del Tile en modo reverso
	*/
	public void setReversedBackgroundColor(int ReversedBackgroundColor){
		_reversedBackgroundColor = ReversedBackgroundColor;
	}
	/**
	* @ Color de la letra del Tile en modo reverso
	*/
	public void setReversedForegroundColor(int ReversedForegroundColor){
		txtReversed.setTextColor(ReversedForegroundColor);
	}
	/**
	* @ Tamaño de la letra en modo reversed
	*/
	public void setReversedTextSize(float TextSize){
		txtReversed.setTextSize(TextSize);		
	}
	/**
	* @ Estabelce la iamgen de fondo del control
	*/
	public void setBackGroundImage(Drawable drawable){
		// Obtenemos las dimensiones de la imagen
		if (drawable == null){
			_imageWidth = 0;
			_imageHeight = 0;						
		} else {
			if (drawable instanceof BitmapDrawable){
				Bitmap p = ((BitmapDrawable)drawable).getBitmap();
				_imageWidth = p.getWidth();
				_imageHeight = p.getHeight();
			} else {
				_imageWidth = 0;
				_imageHeight = 0;			
			}
		}
		
		// Establecemos el Drawable
		imgvw.setImageDrawable(drawable);
	}
	/**
	* @ Estabelce la iamgen de fondo del control
	*/
	public void setBackGroundImage(Bitmap bitmap){
		// Obtenemos las dimensiones de la imagen
		if (bitmap == null){
			_imageWidth = 0;
			_imageHeight = 0;						
		} else {
			_imageWidth = bitmap.getWidth();
			_imageHeight = bitmap.getHeight();
		}

		// Establecemos el Bitmap de Fondo
		imgvw.setImageBitmap(bitmap);
	}
	/*********************************************************/
	/******************  R o t a c i o n e s  ****************/
	/*********************************************************/	
	public void start() { 
        if (!enabled) {
            enabled = true; 
            handler.postDelayed(doRotation, _intervalMilliseconds + (rnd.nextInt(_intervalMilliseconds / 1000) * 1000));                 	
        } 
    } 
	public void stop() { 
        if (enabled){ 
	        enabled = false; 
	        handler.removeCallbacks(doRotation); 
	        handler.removeCallbacks(RotateHorizontal);
	        handler.removeCallbacks(RotateVertical);
        }
    }
	private Runnable RotateHorizontal = new Runnable() {  
        public void run() {
        	_bIsReversed = !_bIsReversed;
        	view.startAnimation(animHorizontal);
        } 
	};  
	private Runnable RotateVertical = new Runnable() {  
        public void run() {
        	_bIsReversed = !_bIsReversed;
            view.startAnimation(animVertical);
        } 
	};  
	public Handler handler = new Handler(); 
	private Runnable doRotation = new Runnable() { 
        public void run() {
            // Rotación vertical
        	if (view.isEnabled()){
	        	if (!bIsPushed) {
	        		if (_txtReverse.trim().length() > 0)handler.post(RotateVertical);
	        	}
        	}
  
        	// Caluclamos el nuevo intervalo
            int newIntervalMilliseconds = _intervalMilliseconds + (rnd.nextInt(_intervalMilliseconds / 1000) * 1000);
            handler.postDelayed(doRotation, newIntervalMilliseconds); 
        } 
    };  
	/**
	* @ Actualiza el control en estado reversed
	*/
    private void updateReversedUI(){
    	
		float fScale = 1.0f;
		// Calculamos la escala a aplicar
		if ((_imageWidth > 0) && (_imageHeight > 0)){
			if ((view.getWidth() > 0) && (view.getHeight() > 0)){
				fScale = Math.min((float)view.getWidth() / (float)_imageWidth, (float)view.getHeight() / (float)_imageHeight);
			}
		}
    	
		// Calculamos márgenes
		int _margenWidth = (view.getWidth() - (int)((float)_imageWidth * fScale)) / 2;
		int _margenHeight = (view.getHeight() - (int)((float)_imageHeight * fScale)) / 2;
		
    	if (_bIsReversed){
    		// Estamos en modo reversed
    		txtReversed.setText(_txtReverse.trim());
    		txtReversed.setBackgroundColor(_reversedBackgroundColor);    		
    		    		
    		// Flipeamos la imagen
    		Matrix matrix = imgvw.getImageMatrix();
    		matrix.setTranslate(_margenWidth + (_imageWidth * fScale), _margenHeight);
    		matrix.preScale(-fScale, fScale);

    		// Refrescamos
    		imgvw.invalidate();
    	} else {
    		// Estamos en modo normal
    		txtReversed.setText("");
    		txtReversed.setBackgroundColor(Color.TRANSPARENT);
    		
    		// Flipeamos la imagen
    		Matrix matrix = imgvw.getImageMatrix();
    		matrix.setTranslate(_margenWidth, _margenHeight);
    		matrix.preScale(fScale, fScale);
    		
    		// Refrescamos
    		imgvw.invalidate();
    	}
    }    
}
