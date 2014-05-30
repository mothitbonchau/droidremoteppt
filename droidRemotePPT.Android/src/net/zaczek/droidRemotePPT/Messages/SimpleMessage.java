package net.zaczek.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SimpleMessage extends PPTMessage {

	public SimpleMessage(byte msgID) {
		this.msgID = msgID;
	}
	
	private byte msgID;
	
	@Override
	public byte getMessageId() {
		return msgID;
	}

	@Override
	public void write(DataOutputStream sw) throws IOException {
	}
 
	@Override
	public void read(DataInputStream sr) throws IOException {		
	}
}
