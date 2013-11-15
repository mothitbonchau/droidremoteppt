package dasz.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class SlideChangedMessage extends PPTMessage {

	Bitmap bmp;
	int totalSlides = 0;
	int currentSlide = 0;

	public Bitmap getBmp() {
		return bmp;
	}

	public int getTotalSlides() {
		return totalSlides;
	}

	public int getCurrentSlide() {
		return currentSlide;
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
		isValid = false;
		currentSlide = sr.readInt();
		totalSlides = sr.readInt();
		final int length = sr.readInt();

		if (currentSlide < 0 || currentSlide > 100000 || totalSlides < 0
				|| totalSlides > 100000 || length < 0 || length > 100000) {
			Log.e("drPTT", "Invalid slide changed message received");
			return;
		} else {
			isValid = true;
		}
		if (length > 0) {
			byte[] buffer = new byte[length];
			int bytesRead = 0;
			while(bytesRead < length) {
				bytesRead += sr.read(buffer, bytesRead, length - bytesRead);
			}
			bmp = BitmapFactory.decodeByteArray(buffer, 0, length);
		}
	}
}
