package nl.moj.client.io;

import java.io.DataOutput;
import java.io.IOException;

import nl.moj.client.anim.Anim;

/**
 * Message defines the communication packets between the server and the client.
 * The first message must always be a Hello message in order to authenticate.
 * The server will send UpdateClientStatistics periodically to update the clock
 * and the state of the client. There are 3 states : wait, programming and
 * finished. Wait indicates the waiting before the start of the game.
 * Programming is the actual game and finished is the state after the time has
 * expired or a solution was submitted. The client will send
 * UpdateServerStatistics periodically to update the # of keystrokes. The
 * AddAction and Editor messages will only be called as part of the
 * initialisation. The Action message will be sent in response to some user
 * action. The Console message will be sent in response to some action on the
 * server.
 * 
 * @author E.Hooijmeijer
 */
public interface Message {

	/** protocol version must be identical on both sides */
	public static int PROTOCOLVERSION = 2007111001;

	/** Logs on a team. Is the first message that must be sent. */
	public interface Hello extends Message {
		public String getTeamName();

		public String getPassword();

		public int getProtocolVersion();
	}

	/** opens a new editor with the specified content. */
	public interface Editor extends Message {
		public String getFileName();

		public String getContents();

		public boolean isJava();

		public boolean isReadOnly();

		public boolean isMonospaced();
	}

	/** adds a line to the specified console */
	public interface Console extends Message {
		public String getConsole();

		public String getContent();
	}

	/** Adds an action to the interface. */
	public interface AddAction extends Message {
		public String getAction();

		public boolean mustConfirm();

		public String getToolTip();
	}

	public interface NoOp extends Message {
		//
	}

	public interface GoodBye extends Message {
		//
	}

	public interface TestSet extends Message {
		public int getCount();

		public String getName(int nr);

		public String getDescription(int nr);
	}

	/** performs an action on the specified file */
	public interface Action extends Message {
		/** the name of the action */
		public String getAction();

		/** the file name(s) to apply it to */
		public String getFileName();

		/** the contents of the file(s) */
		public String getContents();

		/** the (optional) index of the operation, -1 for no index. */
		public int getIndex();

		public int getKeyStrokes();
	}
	
	/** performs an action on the specified file */
	public interface Actions extends Message {
		public int getType(int idx);
		/** the name of the action */
		public String getAction(int idx);

		/** the file name(s) to apply it to */
		public String getFileName(int idx);
		public String[] getFileNames();

		/** the contents of the file(s) */
		public String getContents(int idx);
		public String[] getAllContents();

		/** the (optional) index of the operation, -1 for no index. */
		public int getIndex(int idx);

		public int getKeyStrokes(int idx);
		
		public int getSize();
	}

	public interface UpdateClientStatistics extends Message {
		/** waiting to start */
		public static final int STATE_WAIT = 0;
		/** programming and testing */
		public static final int STATE_PROGRAMMING = 1;
		/** solution has been submitted */
		public static final int STATE_FINISHED = 2;

		public int getState();

		public int getMaxSeconds();

		public int getSecondsRemaining();

		public int getFinalScore();

		public int getTeamsOnline();

		public int getTeamCount();

		public int[] getTestResults();
	}

	public interface Animation extends Message {
		public int getTest();

		public Anim getAnimation();
	}

	public interface Assignment extends Message {
		public String getName();

		public String getAuthor();

		public byte[] getIcon();

		public byte[] getSponsorImage();
	}

	/** C->S Client logs on. */
	public static final int MSG_HELLO = 0;
	/** S->C indicates a message for the editor */
	public static final int MSG_EDITOR = 1;
	/** S->C indicates a message for the console */
	public static final int MSG_CONSOLE = 2;
	/** C->S indicates an action code message */
	public static final int MSG_ACTION = 3;
	/** Multi action message*/
	public static final int MSG_MULTI_ACTION = 14;
	/** S->C indicates an update of statistics */
	public static final int MSG_UPDATE_CLIENT = 4;
	/** S->C indicates action to be added */
	public static final int MSG_ADDACTION = 6;
	/** S->C sent if the provided username and password are incorrect. */
	public static final int MSG_UNKNOWNUSERPASSWORD = 7;
	/** S->C specifies available tests. */
	public static final int MSG_TESTSET = 8;
	/** S->C sent if the protocol version does not match */
	public static final int MSG_PROTOCOLVERSIONMISMATCH = 9;
	/** S->C sends over an animation */
	public static final int MSG_ANIMATION = 10;
	/** C->S no operation message (refresh stats) */
	public static final int MSG_NOOP = 11;
	/** C->S logoff message */
	public static final int MSG_GOODBYE = 12;
	/** S->C assignment info */
	public static final int MSG_ASSIGNMENT = 13;

	/** the ID of this message */
	public int getId();

	/** the ID of the message this message is a reply to or -1 */
	public int getSourceId();

	/** the type of this message */
	public int getType();

	/** writes the message to the dataoutput. */
	public void write(DataOutput out) throws IOException;

}