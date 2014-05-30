package net.zaczek.droidRemotePPT.Messages;

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
	String notes = null;

	public Bitmap getBmp() {
		return bmp;
	}

	public int getTotalSlides() {
		return totalSlides;
	}

	public int getCurrentSlide() {
		return currentSlide;
	}
	
	public String getCurrentNotes(){
		return notes;
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
		final int lengthBmp = sr.readInt();
		final int lengthNotes = sr.readInt();

		if (currentSlide < 0 || currentSlide > 100000 || totalSlides < 0
				|| totalSlides > 100000 || lengthBmp < 0 || lengthBmp > 100000) {
			Log.e("drPTT", "Invalid slide changed message received");
			return;
		} else {
			isValid = true;
		}
//		if (lengthBmp > 0) {
//			byte[] buffer = new byte[lengthBmp];
//			int bytesRead = 0;
//			while(bytesRead < lengthBmp) {
//				bytesRead += sr.read(buffer, bytesRead, lengthBmp - bytesRead);
//			}
		if (length > 0) {
			byte[] buffer = new byte[length];
			int bytesRead = 0;
			while(bytesRead < length) {
				bytesRead += sr.read(buffer, bytesRead, length - bytesRead);
			}
		bmp = BitmapFactory.decodeByteArray(buffer, 0, lengthBmp);
		
        if(lengthNotes > 0)
        {
			byte[] smallerData = new byte[lengthNotes];
			System.arraycopy(buffer, lengthBmp, smallerData, 0, lengthNotes);
	        notes  = new String("Upper notes:\n\n\t");
	        notes += new String(smallerData);
        }
        else
        	notes = "No notes available for this slide.";
        
        
		System.out.println("notes="+notes);
		
		
		}
//		if (lengthNotes > 0) {
//			byte[] buffer2 = new byte[lengthNotes];
//			int bytesRead = 0;
//			while(bytesRead < lengthNotes) {
//				bytesRead += sr.read(buffer2, bytesRead + lengthBmp, length-bytesRead);
//			}
//			notes = buffer2.toString();
//			System.out.println("notes="+notes);
//		}
		System.out.println("currentSlide="+ currentSlide +" lengthBmp="+lengthBmp+" lengthNotes="+lengthNotes);
	}
}
		
//		if (length > 0) {
//			byte[] buffer = new byte[length];
//			int bytesRead = 0;
//			while(bytesRead < length) {
//				bytesRead += sr.read(buffer, bytesRead, length - bytesRead);
//			}
//			notes = new String(buffer);
//		}
