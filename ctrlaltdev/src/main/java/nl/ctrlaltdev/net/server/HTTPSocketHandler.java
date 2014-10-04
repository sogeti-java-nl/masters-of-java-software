package nl.ctrlaltdev.net.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of the HTTP protocol. Use as baseclass for all HTTP
 * communications. Override the handle request method.
 * 
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1
 */

public class HTTPSocketHandler implements Runnable {

	/** Wrapper for a HTTP request, containing the headers and content */
	public static class HTTPRequest {
		private String myRequest, myContent;
		private Map<String, String> myHeader;

		public HTTPRequest(String req, Map<String, String> header,
				String content) {
			myRequest = req;
			myHeader = header;
			myContent = content;
		}

		public String getRequest() {
			return myRequest;
		}

		public String getContent() {
			return myContent;
		}

		public String getHeaderValue(String name) {
			return myHeader.get(name.toLowerCase());
		}

		public String[] getHeaderNames() {
			return myHeader.keySet().toArray(new String[myHeader.size()]);
		}
	}

	/**
	 * Wrapper for a HTTP response, allowing header and content creation. Any
	 * modifications to the header should be done prior to getting the Writer.
	 */
	public static class HTTPResponse {
		private BufferedWriter myOutput;
		private int myContentLength;
		private int myReplyCode;
		private String myContentType;
		private boolean headerSent;

		public HTTPResponse(OutputStream out) {
			myOutput = new BufferedWriter(new OutputStreamWriter(out));
			myReplyCode = 200;
			myContentLength = -1;
			myContentType = "text/html";
		}

		public void setReplyCode(int code) {
			myReplyCode = code;
		}

		public void setContentType(String contentType) {
			myContentType = contentType;
		}

		public void setContentLength(int len) {
			myContentLength = len;
		}

		public BufferedWriter getWriter() throws IOException {
			if (!headerSent) {
				if (myReplyCode == 200)
					myOutput.write("HTTP/1.0 200 OK");
				else
					myOutput.write("HTTP/1.0 " + myReplyCode + " ERR");
				myOutput.newLine();
				myOutput.write("Content-type: " + myContentType);
				myOutput.newLine();
				if (myContentLength > 0)
					myOutput.write("Content-length: " + myContentLength);
				myOutput.newLine();
				myOutput.newLine();
				myOutput.flush();
				headerSent = true;
			}
			return myOutput;
		}
	}

	private Socket mySocket;
	protected Log myLog;
	private String myRequest;
	private Map<String, String> myHeaderData;
	private StringBuffer myContent;

	public HTTPSocketHandler(Socket s, Log l) {
		mySocket = s;
		myLog = l;
		myHeaderData = new HashMap<String, String>();
		myContent = new StringBuffer();
	}

	public void run() {
		try {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						mySocket.getInputStream()));
				String s = null;
				//
				// Read Header
				//
				int cnt = 0;
				do {
					s = in.readLine();
					if (s != null) {
						if (cnt == 0)
							myRequest = s;
						else if (s.indexOf(':') >= 0) {
							myHeaderData.put(s.substring(0, s.indexOf(':'))
									.toLowerCase(),
									s.substring(s.indexOf(':') + 2));
						}
						cnt++;
					}
				} while ((s != null) && (!s.equals("")));
				//
				// Read Content, if any.
				//
				String l = getHeaderField("Content-length");
				if (l != null) {
					int size = Integer.parseInt(l);
					int read = 0;
					char[] buffer = new char[256];
					while (read < size) {
						int r = in.read(buffer);
						if (r == -1)
							throw new IOException("End of Stream ??");
						myContent.append(buffer, 0, r);
						read = read + r;
					}
					s = null;
				}
				//
				//
				//
				HTTPRequest req = new HTTPRequest(myRequest, myHeaderData,
						myContent.toString());
				HTTPResponse res = new HTTPResponse(mySocket.getOutputStream());
				//
				try {
					handleRequest(req, res);
				} catch (Exception e) {
					myLog.error(e.getMessage());
					res.setReplyCode(500);
					res.getWriter().write(e.toString());
					res.getWriter().newLine();
				}
				//
				res.getWriter().flush();
				//
			} catch (IOException e) {
				myLog.error(e.getMessage());
			} finally {
				//
				mySocket.close();
				//
			}
			//
		} catch (IOException e) {
			myLog.error(e.getMessage());
		}
	}

	public String getHeaderField(String name) {
		return myHeaderData.get(name.toLowerCase());
	}

	/**
	 * handleRequest contains the actual implementation of the request. This one
	 * prints HelloWorld. Override and do something useful with it.
	 */
	protected void handleRequest(HTTPRequest req, HTTPResponse res)
			throws IOException {
		//
		// Do Nothing Implementation.
		//
		res.getWriter().write("Hallo Wereld");
		res.getWriter().newLine();
	}

}
