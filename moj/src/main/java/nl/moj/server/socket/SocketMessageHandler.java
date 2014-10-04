package nl.moj.server.socket;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import nl.ctrlaltdev.net.server.Log;
import nl.moj.client.io.Message;
import nl.moj.client.io.MessageFactory;
import nl.moj.model.Scheduler;
import nl.moj.server.ClientServerImpl;
import nl.moj.server.ClientServerInterface;
import nl.moj.server.ClientServerInterface.InvalidStateException;

public class SocketMessageHandler implements Runnable {

	private MessageFactory factory=new MessageFactory();
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	private Log log;
	private boolean stop;
	
	private ClientServerInterface csi;

	public SocketMessageHandler(Socket s,Log l,Scheduler scheduler) {
		try {
			socket=s;
			log=l;
			csi=new ClientServerImpl(scheduler);
			s.setSoTimeout(1000);
			out=new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
			in=new DataInputStream(s.getInputStream());
			stop=false;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}	
	
	public void run() {
		try {			
			do {
				csi.update();
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
				// Create replies.
				//
				Message[] reply=null;
				//
				if (msg!=null) {
					switch (msg.getType()) {
						case Message.MSG_HELLO : reply=csi.onHello((Message.Hello)msg);break;
						case Message.MSG_GOODBYE : reply=csi.onGoodBye((Message.GoodBye)msg);stop=true;break;
						case Message.MSG_ACTION : reply=csi.onAction((Message.Action)msg);break;
						case Message.MSG_MULTI_ACTION : reply=csi.onMultiAction((Message.Actions)msg);break;
					}
				} else {
					if (!csi.isInitial()) {
						reply=csi.onNoOp(null);
					} else {
						// Terminate if state is initial and there is no msg.
						stop=true;
					}
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
			} while (!stop);
		} catch (InvalidStateException ex) {
			log.error("StateException in MessageHandler : "+ex.getMessage());
			ex.printStackTrace();
		} catch (IOException ex) {
			log.error("Error in MessageHandler : "+ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				// Sign out  
				if (!csi.isInitial()) try {
					csi.onGoodBye(null);
				} catch (InvalidStateException ex) {
					// Ignore.
				}
				//
				log.info("Closing connection.");
				socket.close();
			} catch (IOException ex) {
				log.error("Error closing Socket : "+ex.getMessage());
			}
		}	
	}
	
}