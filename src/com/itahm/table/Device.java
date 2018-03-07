package com.itahm.table;

import java.io.File;
import java.io.IOException;

import com.itahm.json.JSONObject;
import com.itahm.Agent;

public class Device extends Table {
	
	public Device(File dataRoot) throws IOException {
		super(dataRoot, Name.DEVICE);
	}
	
	/**
	 * 추가인 경우 position 기본 정보를 생성해 주어야 하며,
	 * 삭제인 경우 monitor, critical 정보를 함께 삭제해 주고,
	 * position 정보는 클라이언트가 동기화 하므로 서버에서는 처리하지 않음.  
	 * @throws IOException 
	 */
	
	public JSONObject put(String ip, JSONObject device) throws IOException {
		if (device == null) {
			Agent.getTable(Name.MONITOR).put(ip, null);
			Agent.getTable(Name.CRITICAL).put(ip, null);
		}
		else {
			// 추가의 경우 client와 동기화 위해 생성해 줌
			Table posTable = Agent.getTable(Name.POSITION);
			
			if (posTable.getJSONObject(ip) == null) {
				posTable.put(ip, new JSONObject()
					.put("x", 0)
					.put("y", 0)
					.put("ifEntry", new JSONObject()));
			}
		}
		
		return super.put(ip, device);
	}
	
}
