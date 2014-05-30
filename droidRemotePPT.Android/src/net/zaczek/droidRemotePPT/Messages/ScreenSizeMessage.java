package net.zaczek.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ScreenSizeMessage extends PPTMessage {

	private int width;
	private int height;
	public ScreenSizeMessage(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public byte getMessageId() {
		return PPTMessage.MESSAGE_SCREEN_SIZE;
	}

	@Override
	public void write(DataOutputStream sw) throws IOException {
		sw.writeInt(width);
		sw.writeInt(height);		
	}
	
	@Override
	public void read(DataInputStream sr) throws IOException {
	}

}
