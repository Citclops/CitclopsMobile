package com.citclops.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

@SuppressWarnings("deprecation")
public class SendCitclopsDataToWS {
	public static boolean Send(String filePath) throws Exception{
//		// The files exists... Send it
//		boolean retorn = false;
//		Mail mail = new Mail( DataConstants.SMTP_USER, DataConstants.SMTP_PASSWORD);
//		mail.set_host(DataConstants.SMTP_SERVER);
//		mail.set_port("25");
//		mail.set_sport("25");
//		//String[] toArr = {DataConstants.COLOR_USER1};
//		String[] toArr = {"rfarres@kinetical.com"};
//		mail.set_to(toArr);
//		mail.set_from(DataConstants.SMTP_USER);								
//		mail.set_subject(filePath);
//		mail.set_body(filePath);								
//		mail.addAttachment(filePath);
//		retorn = mail.send();
//		return retorn;
		

		
		// Send file to web service
		boolean retorn = true;
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(DataConstants.URL_WEB_SERVICE);

		MultipartEntity entity = new MultipartEntity();
		File file = new File(filePath);
		entity.addPart("file", new FileBody(file));
		post.setEntity(entity);
		HttpResponse response = null;
		try {
			response = client.execute(post);
			HttpEntity respEntity = response.getEntity();
			InputStream inputStream = respEntity.getContent();	
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));			
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			retorn = false;
			throw e;
		}
		return retorn;
	}
}
