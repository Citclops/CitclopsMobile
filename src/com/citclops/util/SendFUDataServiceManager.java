package com.citclops.util;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class SendFUDataServiceManager {
	private static PendingIntent pendingIntent;				// Intent Service Alarm Checker
	private static long secondsInterval = 60000;			// Millisconds between checks
	private static Context _ctx = null;						// Local Context
	
    /**
     * Start Sending data
     */
    public static void StartSend(Context ctx, Activity act) {
    	if (pendingIntent == null){
    		try{
    			// Local Copy
    			_ctx = ctx;
    			
		    	// creem el Intent sempre que no estigui ja creat 
		    	Intent intent = new Intent(ctx, SendFUDataService.class);
		    	pendingIntent = PendingIntent.getService(ctx, 0, intent, 0);
		
		    	// Creem el Alarm Manager
		    	AlarmManager alarmManager = (AlarmManager)act.getSystemService(android.content.Context.ALARM_SERVICE);
		
		    	// Calendari per els intervals
		    	Calendar calendar = Calendar.getInstance();
		    	calendar.setTimeInMillis(System.currentTimeMillis());
		    	calendar.add(Calendar.SECOND, 10);
		    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), secondsInterval, pendingIntent);		    	
		    	
    		} catch (Exception e){
    			// Impossible to start sending information.Try iteration
    			pendingIntent = null;
    		}
    	}
    }
    /*
     * Stop Sending Data
     */
    public static void StopSend(){
    	if (pendingIntent != null){
    		try{
    			AlarmManager alarmManager = (AlarmManager)_ctx.getSystemService(android.content.Context.ALARM_SERVICE);
    			alarmManager.cancel(pendingIntent);
    	    	pendingIntent = null;
    		} catch (Exception e){
    			// Impossible stop sending information. Try next iteration    			
    		}    		    		
    	}    	
    }
}
