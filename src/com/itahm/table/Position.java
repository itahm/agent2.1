package com.itahm.table;

import java.io.File;
import java.io.IOException;

import com.itahm.json.JSONObject;

public class Position extends Table {
	
	public Position(File dataRoot) throws IOException {
		super(dataRoot, Name.POSITION);
	}
	/*
	@Override
	public JSONObject put(String key, JSONObject value) throws IOException {
		if (value != null) {
			if (!value.has("x")) {
				value.put("x", 0);
			}
			
			if (!value.has("y")) {
				value.put("y", 0);
			}
			
			if (!value.has("ifEntry")) {
				value.put("ifEntry", new JSONObject());
			}
		}
		
		return super.put(key, value);
	}
	*/
	@Override
	public JSONObject save(JSONObject data) throws IOException {
		for (Object key : data.keySet()) {
			String ip = (String)key;
			
			super.table.put(ip, data.getJSONObject(ip));
		}
		
		return super.save();
	}

}
