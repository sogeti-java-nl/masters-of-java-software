package nl.moj.client.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ActionMessagesImpl implements Message.Actions {

	private List<ActionMessageImpl> actionMessages = new LinkedList<ActionMessageImpl>();
	private boolean multi = false;
	
	private static final int MSG_NEXT = 0;
	private static final int MSG_END = 1;
	
	
	public ActionMessagesImpl(String file, String contents, String action, int keyStrokes) {
		this(file, contents, action, -1, keyStrokes);
	}

	public ActionMessagesImpl(String file, String contents, String action, int idx, int keyStrokes) {
		ActionMessageImpl anActionMessage = new ActionMessageImpl(file, contents, action, idx, keyStrokes);
		actionMessages.add(anActionMessage);
	}

	public ActionMessagesImpl(int multitype, DataInput in) throws IOException {
		multi = true;
		int type=in.readInt();
		
		ActionMessageImpl anActionMessage = new ActionMessageImpl(type, in);
		actionMessages.add(anActionMessage);
		while(in.readInt() == MSG_NEXT) {
			type=in.readInt();
			ActionMessageImpl anNextActionMessage = new ActionMessageImpl(type, in);
			actionMessages.add(anNextActionMessage);
		};
	}

	public void write(DataOutput out) throws IOException {
		if(multi) {
			out.writeInt(Message.MSG_MULTI_ACTION);
		}
		int i = 1;
		for(ActionMessageImpl anActionMessage : actionMessages) {
			anActionMessage.write(out);
			
			if(i == actionMessages.size()) {
				out.writeInt(MSG_END);
			} else {
				out.writeInt(MSG_NEXT);
			}
			
			i++;
		}
	}
	
	public void addActionMessage(ActionMessageImpl anActionMessage) {
		actionMessages.add(anActionMessage);
		multi = true;
	}
	
	public List<ActionMessageImpl> getActionMessages() {
		return actionMessages;
	}

	@Override
	public int getId() {
		for(ActionMessageImpl anActionMessage : actionMessages) {
			return anActionMessage.getId();
		}
		return 0;
	}

	@Override
	public int getSourceId() {
		for(ActionMessageImpl anActionMessage : actionMessages) {
			return anActionMessage.getSourceId();
		}
		return 0;
	}

	@Override
	public int getType() {
		return Message.MSG_MULTI_ACTION;
	}
	
	@Override
	public int getType(int idx) {
		return actionMessages.get(idx).getType();
	}

	@Override
	public String getAction(int idx) {
		return actionMessages.get(idx).getAction();
	}

	@Override
	public String getFileName(int idx) {
		return actionMessages.get(idx).getFileName();
	}
	
	@Override
	public String[] getFileNames() {
		String[] result = new String[actionMessages.size()];
		for(int i = 0; i < actionMessages.size(); i++) {
			result[i] = actionMessages.get(i).getFileName(); 
		}
		return result;
	}

	@Override
	public String getContents(int idx) {
		return actionMessages.get(idx).getContents();
	}
	
	@Override
	public String[] getAllContents() {
		String[] result = new String[actionMessages.size()];
		for(int i = 0; i < actionMessages.size(); i++) {
			result[i] = actionMessages.get(i).getContents(); 
		}
		return result;
	}

	@Override
	public int getIndex(int idx) {
		return actionMessages.get(idx).getIndex();
	}

	@Override
	public int getKeyStrokes(int idx) {
		return actionMessages.get(idx).getKeyStrokes();
	}
	
	@Override
	public int getSize() {
		return actionMessages.size();
	}
}