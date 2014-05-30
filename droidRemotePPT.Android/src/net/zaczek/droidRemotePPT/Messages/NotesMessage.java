package net.zaczek.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import android.util.Log;

public class NotesMessage extends PPTMessage {

	String notes;
	int totalSlides = 0;
	int currentSlide = 0;

	public String getNotes() {
		return notes;
	}

	public int getTotalSlides() {
		return totalSlides;
	}

	public int getCurrentSlide() {
		return currentSlide;
	}

	@Override
	public byte getMessageId() {
		return PPTMessage.MESSAGE_NOTES;
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
			notes = Arrays.toString(buffer);
		}
	}
}
