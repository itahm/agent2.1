package com.itahm;

import java.io.IOException;

import com.itahm.json.JSONObject;

import com.itahm.snmp.TmpNode;
import com.itahm.table.Table;
import com.itahm.util.Util;

public class TestNode extends TmpNode {

	private final SNMPAgent agent;
	private final boolean onFailure;
	
	public TestNode(SNMPAgent agent, String ip, boolean onFailure) {
		super(agent, ip, Agent.MAX_TIMEOUT);
		
		this.agent = agent;
		
		this.onFailure = onFailure;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onSuccess(String profileName) {
		if (!this.agent.registerNode(this.ip, profileName)) {
			return;
		}			
		
		final Table deviceTable = Agent.getTable(Table.Name.DEVICE);
		final Table monitorTable = Agent.getTable(Table.Name.MONITOR);
	
		if (deviceTable.getJSONObject(super.ip) == null) {
			try {
				deviceTable.put(super.ip
					, new JSONObject()
						.put("ip", super.ip)
						.put("name", "")
						.put("type", "unknown")
						.put("label", "")
						.put("ifSpeed", new JSONObject()));
			} catch (IOException ioe) {
				Agent.syslog(Util.EToString(ioe));
			}
		}
		
		Agent.removeICMPNode(super.ip);
		
		monitorTable.getJSONObject().put(super.ip, new JSONObject()
			.put("protocol", "snmp")
			.put("ip", super.ip)
			.put("profile", profileName)
			.put("shutdown", false)
			.put("critical", false));
		
		try {
			monitorTable.save();
		} catch (IOException ioe) {
			Agent.syslog(Util.EToString(ioe));
		}
		
		Agent.log(ip, String.format("%s SNMP 등록 성공.", super.ip), Log.Type.SYSTEM, true, false);
	}

	@Override
	public void onFailure(int status) {
		if (!this.onFailure) {
			return;
		}
		
		Agent.log(ip, String.format("%s SNMP 등록 실패. status[%d]", super.ip, status), Log.Type.SHUTDOWN, false, false);
	}
}
