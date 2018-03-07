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
		Table deviceTable = Agent.getTable(Table.Name.DEVICE);
		Table monitorTable = Agent.getTable(Table.Name.MONITOR);
	
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
		
		try {
			this.agent.addNode(this.ip, profileName);
			
			Agent.log(ip, String.format("%s SNMP 등록 성공.", super.ip), "", true, false);
		} catch (IOException ioe) {
			Agent.syslog(Util.EToString(ioe));
			
			Agent.log(ip, "시스템에 심각한 오류가 있습니다.", "information", false, false);
		}
	}

	@Override
	public void onFailure(int status) {
		if (!this.onFailure) {
			return;
		}
		
		Agent.log(ip, String.format("%s SNMP 등록 실패. status[%d]", super.ip, status), "shutdown", false, false);
	}
}
