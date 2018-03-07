package com.itahm.table;

import java.io.File;
import java.io.IOException;

import com.itahm.json.JSONObject;
import com.itahm.Agent;

public class Monitor extends Table {
	
	public Monitor(File dataRoot) throws IOException {
		super(dataRoot, Name.MONITOR);
	}
	
	private void remove(JSONObject monitor, String ip) throws IOException {
		if ("snmp".equals(monitor.getString("protocol"))) {
			if (Agent.removeSNMPNode(ip)) {
				Agent.getTable(Name.CRITICAL).put(ip, null);
			}
		}
		else if ("icmp".equals(monitor.getString("protocol"))) {
			Agent.removeICMPNode(ip);
		}
		
		super.put(ip, null);
	}
	
	public JSONObject put(String ip, JSONObject monitor) throws IOException {
		if (super.table.has(ip)) {
			remove(super.table.getJSONObject(ip), ip);
		}
		
		if (monitor != null) {
			super.put(ip, null);
			
			if ("snmp".equals(monitor.getString("protocol"))) {
				Agent.testSNMPNode(ip, true);
			}
			else if ("icmp".equals(monitor.getString("protocol"))) {
				Agent.testICMPNode(ip);
			}
		}
		
		return super.table;
	}
}
