package dasz.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class PPTMessage {
	public static final int MESSAGE_NEXT = 1;
	public static final int MESSAGE_PREV = 2;
	public static final int MESSAGE_END = 3;
	public static final int MESSAGE_TOGGLE_BLACK_SCREEN = 4;
	public static final int MESSAGE_CLEAR_DRAWING = 5;
	public static final int MESSAGE_SCREEN_SIZE = 6;
	public static final int MESSAGE_SLIDE_CHANGED = 7;
	public static final int MESSAGE_START = 8;
	public static final int MESSAGE_DRAW = 9;

	public abstract byte getMessageId();

	public abstract void read(DataInputStream sr) throws IOException;
	public abstract void write(DataOutputStream sw) throws IOException;
}