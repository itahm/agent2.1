package com.itahm.gcm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import com.itahm.json.JSONException;
import com.itahm.json.JSONObject;
import com.itahm.json.JSONTokener;

class Message implements DownStream.Request {

	private final static String INVALREG = "InvalidRegistration";
	private final static String UNAVAILABLE = "Unavailable";
	private final static String MISSINGREG = "MissingRegistration";
	private final static String MISMATCHSENDER= "MismatchSenderId";
	private final static String NOTREG = "NotRegistered";
	private final static String BIGMSG = "MessageTooBig"; // 4096bytes
	private final static String INVALKEY = "InvalidDataKey";
	private final static String INVALTTL = "InvalidTtl"; // < 2,419,200 (4week)
	private final static String INVALPACKAGE = "InvalidPackageName";
	private final static String MSGEXCEEDED = "DeviceMessageRateExceeded";
	
	private final DownStream downstream;
	private HttpURLConnection connection;
	private final JSONObject message;
	private final JSONObject data;
	
	public Message(DownStream downstream, String to, String title, String body, String host, boolean dryRun) {
		this(downstream, to, title, body, host);
		
		message.put("dry_run", dryRun);
	}
	
	public Message(DownStream downstream, String to, String title, String body, String host) {
		this.downstream = downstream;

		message = new JSONObject();
		data = new JSONObject();
		
		message.put("to", to);
		message.put("data", data);
		
		if (title.length() > 64) {
			title = title.substring(0, 63);
		}
		
		if (body.length() > 1024) {
			body = body.substring(0, 1023);
		}
		
		data.put("title", title);
		data.put("body", body);
		data.put("host", host);
		data.put("date", System.currentTimeMillis());
	}
	
	private boolean parseResponse() throws IOException {
		JSONObject response;
		
		try(InputStream is = this.connection.getInputStream()) {
			response = new JSONObject(new JSONTokener(is));
		}
		catch (IOException ioe) {
			this.connection.getErrorStream();
			
			throw ioe;
		}
		
		if (response.getInt("failure") > 0) {
			String error = response.getJSONArray("results").getJSONObject(0).getString("error");
			
			switch(error) {
			case INVALREG: // reg id의 포멧이 잘못되었음. 삭제할것
			case NOTREG: // unregister
				this.downstream.onUnRegister(this.message.getString("to"));
				break;
			case UNAVAILABLE:
				
				return false; // 구글문제.
			case MISSINGREG:
			case MSGEXCEEDED: // 스팸
			case MISMATCHSENDER:	
			case BIGMSG:
			case INVALKEY:
			case INVALTTL:
			case INVALPACKAGE:
				throw new IOException("GCM_ERROR: "+ error);
			}
		}
		else if (response.getInt("canonical_ids") > 0){
			this.downstream.onRefresh(this.message.getString("to"), response.getJSONArray("results").getJSONObject(0).getString("registration_id"));
		}
		
		return true;
	}
	
	private int getResponseStatus() throws IOException {
		int status = this.connection.getResponseCode();
		
		switch (status) {
		case 200:
			try {
				if (!parseResponse()) {
					status = 0;
				}
			} catch (JSONException jsone) {
				status = 0;
			}
			
			break;
		}
		
		return status;
	}
	
	@Override
	public void send() throws IOException {
		this.connection = this.downstream.getConnection();
	
		try (OutputStream os = this.connection.getOutputStream()) {
			os.write(this.message.toString().getBytes(StandardCharsets.UTF_8.name()));
		}
		
		int status = 0;
		
		try {
			status = getResponseStatus();
		}
		catch(JSONException jsone) {
			status = 0; // 구글이 잘못된 json 보냈음 발생하면 안됨.
		}
		
		this.connection.disconnect();
		
		this.downstream.onComplete(status);
	}
	
}
