package nl.moj.client.io;

import java.io.DataInput;
import java.io.IOException;

/**
 *
 */

public class ProtocolVersionMismatchMessageImpl extends AbstractMessage implements Message {

	public ProtocolVersionMismatchMessageImpl() {
		super(Message.MSG_PROTOCOLVERSIONMISMATCH);
	}

	public ProtocolVersionMismatchMessageImpl(int type,DataInput in) throws IOException {		
		super(type,in);
	}

}