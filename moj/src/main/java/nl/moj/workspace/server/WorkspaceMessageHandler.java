package nl.moj.workspace.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import nl.ctrlaltdev.net.server.Log;

import nl.moj.workspace.WorkspaceClientServer;
import nl.moj.workspace.io.Message;
import nl.moj.workspace.io.WorkspaceMessageFactory;

public class WorkspaceMessageHandler implements Runnable {

	private Socket socket;
	private Log log;
	private DataOutputStream out;
	private DataInputStream in;
	private boolean stop;
	private WorkspaceClientServer wcs;
	
	private WorkspaceMessageFactory factory=new WorkspaceMessageFactory();
	
	public WorkspaceMessageHandler(Socket s,Log l,WorkspaceClientServer wcs) {
		try {
			socket=s;
			log=l;
			s.setSoTimeout(1000);
			out=new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
			in=new DataInputStream(s.getInputStream());
			stop=false;
			this.wcs=wcs;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void run() {
		try {			
			do {
				//
				// Read the message from the client.
				//
				Message msg;
				try {
					msg=factory.createMessage(in);
				} catch (SocketTimeoutException ex) {
					msg=null;
				}
				//
				Message[] reply=null;
				//
				if (msg!=null) {
					switch (msg.getType()) {
						case Message.MSG_ASSIGNMENT : 
							reply=wcs.onAssignment((Message.Assignment)msg);
							break;
						case Message.MSG_PERFORM :
							reply=wcs.onPerform((Message.Perform)msg);
							break;
						case Message.MSG_REQ_CONTENTS :
							reply=wcs.onRequestContents((Message.ContentsRequest)msg);
							break;
						case Message.MSG_GOODBYE :
							reply=wcs.onGoodbye((Message.Goodbye)msg);
							stop=true;
							break;
					}
				} else {
					reply=wcs.onNoOp();
				}
				//
				// Send back replies
				//
				if (reply!=null) {
					for (int t=0;t<reply.length;t++) {
						reply[t].write(out);					
					}
					out.flush();
				}
				//
			} while (!stop);
			//
		} catch (Exception ex) {
			log.error("Error in WorkspaceMessageHandler : "+ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				log.info("Closing connection.");
				socket.close();
			} catch (IOException ex) {
				log.error("Error closing Socket : "+ex.getMessage());
			}
		}			
	}
	
}
