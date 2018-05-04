package com.itahm.table;

import java.io.File;
import java.io.IOException;

import com.itahm.json.JSONObject;
import com.itahm.Agent;
import com.itahm.http.HTTPException;
import com.itahm.http.Response;

public class Device extends Table {
	
	private static final String PREFIX_DEVICE = "node.";
	private static final String PREFIX_GROUP = "group.";
	
	private long groupOrder = -1;
	private long deviceOrder = -1;
	
	public Device(File dataRoot) throws IOException {
		super(dataRoot, Name.DEVICE);
		
		String id;
		
		for (Object key : super.table.keySet()) {
			id = (String)key;
			
			if (id.indexOf(PREFIX_DEVICE) == 0) {
				try {
					deviceOrder = Math.max(deviceOrder, Long.valueOf(id.replace(PREFIX_DEVICE, "")));
				}
				catch(NumberFormatException nfe) {}
			}
			else if (id.indexOf(PREFIX_GROUP) == 0) {
				try {
					groupOrder = Math.max(groupOrder, Long.valueOf(id.replace(PREFIX_GROUP, "")));
				}
				catch(NumberFormatException nfe) {}
			} 
		}
	}
	
	/**
	 * 추가인 경우 position 기본 정보를 생성해 주어야 하며,
	 * 삭제인 경우 monitor, critical, position (링크 포함) 정보 삭제 
	 * @throws IOException 
	 */
	
	public JSONObject put(String id, JSONObject device) throws IOException, HTTPException {
		// 삭제시 모니터와 임계 정보 삭제
		if (device == null) {
			device = super.getJSONObject(id);
			
			// 동기화 문제로 없는 device라면
			if (device == null) {
				throw new HTTPException(Response.Status.CONFLICT.getCode());
			}
			
			// device, group 공통
			final Table posTable = Agent.getTable(Name.POSITION);
			final JSONObject posData = posTable.getJSONObject(),
				pos = posTable.getJSONObject(id);
			
			if (pos != null) {
				JSONObject peer;
				for (Object key : pos.getJSONObject("ifEntry").keySet()) {
					peer = posTable.getJSONObject((String)key);
					if (peer != null) {
						peer.getJSONObject("ifEntry").remove(id);
					}
				}
			}
			
			posTable.put(id, null);
			
			if (device.has("group") && device.getBoolean("group")) {
				JSONObject child;
				
				for (Object key : posData.keySet()) {
					child = posData.getJSONObject((String)key);
					
					if (child.has("parent") && id.equals(child.getString("parent"))) {
						child.remove("parent");
					}
				}
			}
			else {
				Agent.getTable(Name.MONITOR).put(id, null);
				Agent.getTable(Name.CRITICAL).put(id, null);
			}
		}
		else {
			// 추가
			if ("".equals(id)){
				if (device.has("group") && device.getBoolean("group")) {
					id = String.format("%s%d", PREFIX_GROUP, ++this.groupOrder);
				}
				else {
					id = String.format("%s%d", PREFIX_DEVICE, ++this.deviceOrder);
				}
			}
		}
		
		super.put(id, device);
		
		return new JSONObject().put(id, device);
	}
	
}
