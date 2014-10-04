package nl.moj.server;

import nl.moj.client.io.Message;

/**
 * The client/server interface allows the client to be abstracted from the
 * actual communication technology (like sockets, webservice or webapp).
 * 
 * @author E.Hooijmeijer
 */

public interface ClientServerInterface {

	/** thrown if an onXXX method is called when its not allowed */
	public static class InvalidStateException extends Exception {
		/**
		 * <code>serialVersionUID</code> indicates/is used for.
		 */
		private static final long serialVersionUID = 4512883751515204779L;

		public InvalidStateException() {
			super();
		}

		public InvalidStateException(String msg) {
			super(msg);
		}
	}

	/** returns true if the client is not logged in and thus unknown. */
	public boolean isInitial();

	/**
	 * returns true if the client is logged in but the round has not yet
	 * started.
	 */
	public boolean isWaiting();

	/** returns true if the client is playing MoJ */
	public boolean isPlaying();

	/**
	 * returns true if the client has submitted its assignment and is waiting
	 * for the results
	 */
	public boolean isFinished();

	/** returns true if the results are known */
	public boolean isScoreAvailable();

	public void update();

	/**
	 * Logs on the client. Only allowed in Initial state.
	 * 
	 * @param msg
	 *            the hello message containing the username and password.
	 * @return reply messages containg new state information for the client.
	 * @throws InvalidStateException
	 *             if the c/s interface is not in Initial state.
	 */
	public Message[] onHello(Message.Hello msg) throws InvalidStateException;

	/**
	 * The client performed no operation but would like an update of the server
	 * state. Allowed in all states but Initial state.
	 * 
	 * @param msg
	 *            a NoOp message.
	 * @return reply messages.
	 * @throws InvalidStateException
	 *             if this method is called in the wrong state.
	 */
	public Message[] onNoOp(Message.NoOp msg) throws InvalidStateException;

	/**
	 * The client performed an action. Allowed only in Playing state.
	 * 
	 * @param msg
	 *            an Action message.
	 * @return reply messages.
	 * @throws InvalidStateException
	 *             if this method is called in the wrong state.
	 */
	public Message[] onAction(Message.Action msg) throws InvalidStateException;
	
	public Message[] onMultiAction(Message.Actions msg) throws InvalidStateException;

	/**
	 * The client logs off. Not allowed in the Initial state.
	 * 
	 * @param msg
	 * @return any remaining messages.
	 * @throws InvalidStateException
	 */
	public Message[] onGoodBye(Message.GoodBye msg) throws InvalidStateException;

}