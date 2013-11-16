package net.zaczek.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DrawMessage extends PPTMessage {

	private float[] points;
	public DrawMessage(float[] points) {
		this.points = points;
	}
	
	@Override
	public byte getMessageId() {
		return PPTMessage.MESSAGE_DRAW;
	}

	@Override
	public void read(DataInputStream sr) throws IOException {
	}

	@Override
	public void write(DataOutputStream sw) throws IOException {
		sw.writeInt(points.length);
		for(int i=0;i<points.length;i++) {
			sw.writeInt((int)points[i]);
		}
	}
}
