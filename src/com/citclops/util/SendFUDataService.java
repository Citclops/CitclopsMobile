package com.citclops.util;

import java.io.File;
import java.io.FileFilter;

import com.citclops.models.SettingsData;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

public class SendFUDataService extends Service implements Runnable {
	
	@Override	
	public IBinder onBind(Intent intent) {
	 return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Thread thread = new Thread(this);
		thread.start();
		return START_STICKY;
	}
	/**
	 * Async Method which is executed each xxx millisconds
	 */
	public void run() {
	    try {
	    	// Check for the files in the folder
			String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DataConstants.baseFolder;
			String  baseFullPath = basePath + "/" + DataConstants.FUScale_FolderPhotoName + "/";
			String  baseFullPathSending = basePath + "/" + DataConstants.FUScale_FolderPhotoNameSending + "/";
			String  baseFullPathSent = basePath + "/" + DataConstants.FUScale_FolderPhotoNameSent + "/";
						
			// Create the file to get files	    	
			File f = new File(baseFullPath);
			if (f.exists()){
				// Create Sent if not existes
				File tmpSent = new File(baseFullPathSent);
				File tmpSending = new File(baseFullPathSending);
				if (!tmpSent.exists()) tmpSent.mkdirs();
				if (!tmpSending.exists()) tmpSending.mkdirs();

				// Update for more files
				FileFilter onlyFileFilter = new FileFilter() {						
					@Override
					public boolean accept(File pathname) {
						return pathname.isFile();
					}
				};

				// Get User to Send Mail
				SettingsData settingsData = new SettingsData(this);
				settingsData.LoadData();
				String userMail = settingsData.getMailAddressSurvey();
				boolean bSendInformation = settingsData.getDataUploadPreference();
				
				// Get files
				File[] files = f.listFiles(onlyFileFilter);
				boolean bStopService = false;
				while ((bSendInformation) && ((files != null) && (files.length > 0) && (!bStopService))){
					for (int i = 0; i < files.length; i++){
						File fWork = files[i];
						File fWorkSending = null;
						File fWorkSent = null;
						if (fWork.exists()){
							if (fWork.isFile()){
								try{
									// Move files to sending folder
									fWorkSending = new File (baseFullPathSending + "/" + fWork.getName());
									if (fWorkSending.exists()) fWorkSending.delete();
									fWork.renameTo(fWorkSending);
									
									if (fWork.getName().endsWith("txt")){
										// Is a survey. Send by email
										// The two files exists... Send it								
										Mail mail = new Mail( DataConstants.SMTP_USER, DataConstants.SMTP_PASSWORD);
										mail.set_host(DataConstants.SMTP_SERVER);
										mail.set_port("25");
										mail.set_sport("25");
										String[] toArr = {userMail};
										//String[] toArr = {"rfarres@kinetical.com"};
										mail.set_to(toArr);
										mail.set_from(DataConstants.SMTP_USER);								
										mail.set_subject("Survey " + " " + fWork.getName());
										mail.set_body("Survey " + " " + fWork.getName());								
										try {
											mail.addAttachment(fWorkSending.getAbsolutePath());
											if(mail.send()) {
												// File Sent. Move to sent folder
												fWorkSent = new File (baseFullPathSent + "/" + fWork.getName());
												if (fWorkSent.exists()) fWorkSent.delete();									
												fWorkSending.renameTo(fWorkSent);									
												bStopService = false;
											}else{
												// Error sending information. Return files to initial folder
												fWorkSending.renameTo(fWork);
												bStopService = true;
											}
										} catch (Exception e) {
											// Error sending information. Return files to initial folder
											fWorkSending.renameTo(fWork);
											bStopService = true;
										}
									} else {
										// Is a measurement. Send to WS Send file
										if (!SendCitclopsDataToWS.Send(fWorkSending.getAbsolutePath())){
											// Error sending information. Return files to initial folder
											fWorkSending.renameTo(fWork);
											bStopService = true;
										} else {
											// File Sent. Move to sent folder
											fWorkSent = new File (baseFullPathSent + "/" + fWork.getName());
											if (fWorkSent.exists()) fWorkSent.delete();									
											fWorkSending.renameTo(fWorkSent);									
											bStopService = false;
										}
									}
								} catch (Exception e){
									// Error sending information. Return files to initial folder
									fWorkSending.renameTo(fWork);
									bStopService = true;
								} finally{
									
								}
							}
						}
					}					
					files = f.listFiles(onlyFileFilter);
				}
			}				
			// Stop Service
	    	SendFUDataServiceManager.StopSend();
		} catch (Exception e) {
			// Impossible to send data. Will send next time
		} finally{
			
		}
	}
}
