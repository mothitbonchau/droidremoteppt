package dasz.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class SlideChangedMessage extends PPTMessage {

	Bitmap bmp;
	
	public Bitmap getBmp() {
		return bmp;
	}

	@Override
	public byte getMessageId() {
		return PPTMessage.MESSAGE_SLIDE_CHANGED;
	}

	@Override
	public void write(DataOutputStream sw) throws IOException {
	}

	@Override
	public void read(DataInputStream sr) throws IOException {
		@SuppressWarnings("unused")
		int length = sr.readInt();
		//byte[] buffer = new byte[length];
		//sr.read(buffer);
		//bmp = BitmapFactory.decodeByteArray(buffer, 0, length);
		bmp = BitmapFactory.decodeStream(sr);
	}

}
