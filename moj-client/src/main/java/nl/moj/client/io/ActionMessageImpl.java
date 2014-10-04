package nl.moj.client.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class ActionMessageImpl extends AbstractMessage implements Message.Action {

	private String myFile, myContents, myAction;
	private int myIndex, myKeyStrokes;

	public ActionMessageImpl(String file, String contents, String action, int keyStrokes) {
		this(file, contents, action, -1, keyStrokes);
	}

	public ActionMessageImpl(String file, String contents, String action, int idx, int keyStrokes) {
		super(Message.MSG_ACTION);
		myFile = file;
		myContents = contents;
		myAction = action;
		myIndex = idx;
		myKeyStrokes = keyStrokes;
	}

	public ActionMessageImpl(int type, DataInput in) throws IOException {
		super(type, in);
		if (type != Message.MSG_ACTION)
			throw new IOException("Incorrect Type");
		myFile = in.readUTF();
		myContents = in.readUTF();
		myAction = in.readUTF();
		myIndex = in.readInt();
		myKeyStrokes = in.readInt();
	}

	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeUTF(myFile);
		out.writeUTF(myContents);
		out.writeUTF(myAction);
		out.writeInt(myIndex);
		out.writeInt(myKeyStrokes);
	}

	public String getFileName() {
		return myFile;
	}

	public String getContents() {
		return myContents;
	}

	public String getAction() {
		return myAction;
	}

	public int getIndex() {
		return myIndex;
	}

	public int getKeyStrokes() {
		return myKeyStrokes;
	}

}
