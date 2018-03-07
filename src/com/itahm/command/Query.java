package com.itahm.command;

import java.io.IOException;

import com.itahm.json.JSONException;
import com.itahm.json.JSONObject;
import com.itahm.Agent;
import com.itahm.SNMPNode;
import com.itahm.http.Request;
import com.itahm.http.Response;

public class Query implements Command {
	
	@Override
	public Response execute(Request request, JSONObject data) throws IOException {
		
		try {
			SNMPNode node = Agent.getNode(data.getString("ip"));
			
			if (node == null) {
				return Response.getInstance(Response.Status.BADREQUEST,
					new JSONObject().put("error", "node not found").toString());
			}
			
			JSONObject json = node.getData(data.getString("database")
				, String.valueOf(data.getInt("index"))
				, data.getLong("start")
				, data.getLong("end")
				, data.has("summary")? data.getBoolean("summary"): false);
			
			if (json == null) {
				return Response.getInstance(Response.Status.BADREQUEST,
					new JSONObject().put("error", "data not found").toString());
			}
			
			// json은 새로 만들어진 것이기에 toString시 동기화 불필요
			return Response.getInstance(Response.Status.OK, json.toString());
		}
		catch(NullPointerException npe) {
			return Response.getInstance(Response.Status.UNAVAILABLE);
		}
		catch (JSONException jsone) {
			return Response.getInstance(Response.Status.BADREQUEST,
				new JSONObject().put("error", "invalid json request").toString());
		}
		
	}

}
