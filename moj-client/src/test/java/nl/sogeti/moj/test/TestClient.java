package nl.sogeti.moj.test;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import nl.ctrlaltdev.util.Encoder;
import nl.moj.client.io.ActionMessageImpl;
import nl.moj.client.io.HelloMessageImpl;
import nl.moj.client.io.Message;
import nl.moj.client.io.MessageFactory;
import nl.moj.util.InetAddressUtil;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class TestClient implements Runnable {
	/**
	 * Constructor: create a new TestClient.
	 * 
	 * @param server
	 * @param port
	 * @param caseFileName
	 * @param code
	 * @param username
	 * @param password
	 */
	public TestClient(String server, int port, String caseFileName, String code, String username, String password) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.password = password;
		this.caseFileName = caseFileName;
		this.code = code;
	}

	@Override
	public String toString() {
		return "TestClient [server=" + server + ", port=" + port + ", username=" + username + ", password=" + password + "]";
	}

	/** variables for testClient */
	private Logger logger = Logger.getLogger(TestClient.class);
	private String server;
	private int port;
	private String caseFileName;
	private String username;
	private String code;
	private String password;
	private Message msg;
	private boolean shouldRun;
	private Socket mySocket;
	private DataInput myDataInput;
	private DataOutput myDataOutput;
	private MessageFactory myFactory = new MessageFactory();

	/**
	 * dataInput object getter
	 * 
	 * @return dataInput Object
	 * @throws IOException
	 */
	protected synchronized DataInput getDataInput() throws IOException {
		if ((mySocket == null) || (!mySocket.isConnected())) {
			newConnection();
		}
		return myDataInput;
	}

	/**
	 * dataOutput object getter
	 * 
	 * @return dataOutput Object
	 * @throws IOException
	 */
	protected synchronized DataOutput getDataOutput() throws IOException {
		if ((mySocket == null) || (!mySocket.isConnected())) {
			newConnection();
		}
		return myDataOutput;
	}

	/**
	 * Makes a new connection to the server specified in the constructor vars
	 * 
	 * @throws IOException
	 */
	protected synchronized void newConnection() throws IOException {
		/** initialize socket and get stream Objects */
		mySocket = new Socket(InetAddressUtil.makeInetAddress(server), port);
		myDataInput = new DataInputStream(mySocket.getInputStream());
		myDataOutput = new DataOutputStream(mySocket.getOutputStream());
		/** send login message to server, generate hashed password */
		this.password = Encoder.hash(password);
		new HelloMessageImpl(username, password).write(myDataOutput);
	}

	/**
	 * this is the thread in which the client runs, it sends the file for
	 * compilation every timed period
	 */
	public void run() {

		/** log4j config */
		BasicConfigurator.configure();
		shouldRun = true;
		while (shouldRun) {
			DataInput in;
			try {
				in = getDataInput();
				Message msg = myFactory.createMessage(in);
				this.msg = msg;
				msg.getType();
				Thread.sleep((long) 10000);
				DataOutput out = getDataOutput();
				new ActionMessageImpl(this.caseFileName, this.code, "Test", 5).write(out);
				((DataOutputStream) out).flush();
			} catch (Exception e) {
				/** shouldnt be necessary */
				shouldRun = false;
				e.printStackTrace();
			}
		}
	}
}
