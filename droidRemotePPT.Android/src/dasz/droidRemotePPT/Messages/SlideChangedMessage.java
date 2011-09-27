package dasz.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

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
		bmp = BitmapFactory.decodeStream(new FlushedInputStream(sr));
	}

	// http://code.google.com/p/android/issues/detail?id=6066
	static class FlushedInputStream extends FilterInputStream {
	    public FlushedInputStream(InputStream inputStream) {
	        super(inputStream);
	    }

	    @Override
	    public long skip(long n) throws IOException {
	        long totalBytesSkipped = 0L;
	        while (totalBytesSkipped < n) {
	            long bytesSkipped = in.skip(n - totalBytesSkipped);
	            if (bytesSkipped == 0L) {
	                  int b = read();
	                  if (b < 0) {
	                      break;  // we reached EOF
	                  } else {
	                      bytesSkipped = 1; // we read one byte
	                  }
	           }
	            totalBytesSkipped += bytesSkipped;
	        }
	        return totalBytesSkipped;
	    }
	}
}
