package net.zaczek.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class PPTMessage {
	public static final int MESSAGE_NEXT = 1;
	public static final int MESSAGE_PREV = 2;
	public static final int MESSAGE_END = 3;
	public static final int MESSAGE_NOTES = 4;
	public static final int MESSAGE_CLEAR_DRAWING = 5;
	public static final int MESSAGE_SCREEN_SIZE = 6;
	public static final int MESSAGE_SLIDE_CHANGED = 7;
	public static final int MESSAGE_START = 8;
	public static final int MESSAGE_DRAW = 9;
	public static final int MESSAGE_VERSION = 10;
	public static final int MESSAGE_BLACK = 11;
	public static final int MESSAGE_UNBLACK = 12;
	public static final int MESSAGE_FIRST_PAGE = 13;
	public static final int MESSAGE_SELECT_PAGE = 14;
	public static final int MESSAGE_MARK_PAGE = 15;
	public static final int MESSAGE_LAST_PAGE = 16;
	public static final int FIRST_INVALID_MESSAGE_NUMBER = 17;
	
//	public static int slide; 
//	
//	public void getSlide(int slide)
//	{
//		PPTMessage.slide = slide;	
//	}

	public abstract byte getMessageId();

	public abstract void read(DataInputStream sr) throws IOException;
	public abstract void write(DataOutputStream sw) throws IOException;
	
	protected boolean isValid = true;

	public boolean isValid() {
		return isValid;
	}
}