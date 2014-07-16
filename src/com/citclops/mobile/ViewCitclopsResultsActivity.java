package com.citclops.mobile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.citclops.util.MessageDialog;
import com.citclops.widgets.kTransparentWorkingDialog;

public class ViewCitclopsResultsActivity extends Activity{

	private static boolean bFirstExecution = true;		// Booleana para saber si estamos en primera ejecución

	private kTransparentWorkingDialog workingDialog;	// Dialog treballant
    private Context _this;								// COntexte actual								
    private WebView webViewHelp;						// WebView on mostrarem l'ajuda
    private TextView txtHelpTancar;						// Texte de tancar
    private ImageButton HelpImgButtonBack;				// Botó de tornar de l'ajuda

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.citclops_results);
	
		// Obtenim la referencia actual
		_this = this;

		 // Inicializamos la variable de inicio de carga
        bFirstExecution = true;        
        
		// Mostrem el dialog de treballant
        if (workingDialog == null) workingDialog = kTransparentWorkingDialog.show(_this, null, null);
	}
    @Override
    protected void onResume() {
        super.onResume();
        
    	if (bFirstExecution){
    		// En primera ejecución 
    		bFirstExecution = false;
    		try{

        		// Obtenim la referencia a Widgets
        		webViewHelp = (WebView)findViewById(R.id.HelpWebViewHelp);
        		txtHelpTancar = (TextView)findViewById(R.id.HelpTxtHelpTancar);
        		HelpImgButtonBack = (ImageButton)findViewById(R.id.HelpImgButtonBack);
        		
        		// Configurem WebView
        		webViewHelp.getSettings().setJavaScriptEnabled(false);
        		webViewHelp.getSettings().setAllowFileAccess(false);        		
        		webViewHelp.loadUrl(getString(R.string.main_panel_feedback_url_results));
        		webViewHelp.setWebViewClient(new WebViewClient() {
        			   public void onPageFinished(WebView view, String url) {
        					// Amaguem el treballant
        					if (workingDialog != null) {
        						workingDialog.dismiss();
        						workingDialog = null;
        					}     
        			   }
        			});

        		// Fem referencia als llenguatges
        		txtHelpTancar.setText(getString(R.string.main_panel_feedback_url_results_back));
        		
        		// Configurem tornada
        		HelpImgButtonBack.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						finish();		
					}        			
        		});        		
            } catch (Exception e){
	            MessageDialog.ShowDialog(_this, getResources().getText(R.string.app_name).toString(), getString(R.string.main_panel_feedback_url_results_error_load) + ": " + e.getMessage(), getString(R.string.main_panel_feedback_ok), null, true);
    		} finally{
    			
    		}
    	}
    }
}
