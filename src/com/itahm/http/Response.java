package com.itahm.http;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Response {

	public final static String CRLF = "\r\n";
	public final static String FIELD = "%s: %s"+ CRLF;
	
	private final Map<String, String> header = new HashMap<String, String>();
	private String startLine;
	private byte [] body;
	
	public enum Status {
		OK, BADREQUEST, UNAUTHORIZED, NOTFOUND, NOTALLOWED, VERSIONNOTSUP, CONFLICT, UNAVAILABLE, SERVERERROR
	};
	
	private Response(Status status, byte [] body) {
		int code = 200;
		String reason = "OK";
		
		switch (status) {
		case BADREQUEST:
			code = 400;
			reason = "Bad request";
			
			break;
		case NOTFOUND:
			code = 404;
			reason = "Not found";
			
			break;
		case NOTALLOWED:
			code = 405;
			reason = "Method Not Allowed";
			
			setResponseHeader("Allow", "GET");
			
			break;
		case UNAUTHORIZED:
			code = 401;
			reason = "Unauthorized";
			
			break;
		case SERVERERROR:
			code = 500;
			reason = "Internal Server Error";
			
			break;
		case VERSIONNOTSUP:
			code = 505;
			reason = "HTTP Version Not Supported";
			
			break;
		case CONFLICT:
			code = 409;
			reason = "Conflict";
			
			break;
		case UNAVAILABLE:
			code = 503;
			reason = "Service Unavailable";
			
		case OK:
		}
		
		this.startLine = String.format("HTTP/1.1 %d %s" +CRLF, code, reason);
		
		this.body = body;
	}
	
	/**
	 * 
	 * @param request
	 * @param status
	 * @param body
	 * @return
	 * 
	 */
	public static Response getInstance(Status status, String body) {
		try {
			return new Response(status, body.getBytes(StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
	 * 
	 * @param status
	 * @return
	 */
	public static Response getInstance(Status status) {
		return new Response(status, new byte[0]);
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static Response getInstance(File url) throws IOException {
		Path path = url.toPath();
		
		return new Response(Status.OK, Files.readAllBytes(path))
			.setResponseHeader("Content-type", Files.probeContentType(path));
	}
	
	public Response setResponseHeader(String name, String value) {
		this.header.put(name, value);
		
		return this;
	}
	
	public ByteBuffer build() throws IOException {
		if (this.startLine == null || this.body == null) {
			throw new IOException("malformed http request!");
		}
		
		StringBuilder sb = new StringBuilder();
		Iterator<String> iterator;		
		String key;
		byte [] header;
		byte [] message;
		
		sb.append(this.startLine);
		sb.append(String.format(FIELD, "Content-Length", String.valueOf(this.body.length)));
		
		iterator = this.header.keySet().iterator();
		while(iterator.hasNext()) {
			key = iterator.next();
			
			sb.append(String.format(FIELD, key, this.header.get(key)));
		}
		
		sb.append(CRLF);
		
		header = sb.toString().getBytes(StandardCharsets.US_ASCII.name());
		
		message = new byte [header.length + this.body.length];
		
		System.arraycopy(header, 0, message, 0, header.length);
		System.arraycopy(this.body, 0, message, header.length, this.body.length);
		
		return ByteBuffer.wrap(message);
	}
	
}
