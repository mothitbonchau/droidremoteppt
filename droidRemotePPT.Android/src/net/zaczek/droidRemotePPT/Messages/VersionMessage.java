package net.zaczek.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.R.string;
import android.util.Log;

public class VersionMessage extends PPTMessage {

		private int version;

		public int getVersion() {
			return version;
		}
		@Override
		public byte getMessageId() {
			return PPTMessage.MESSAGE_VERSION;
		}

		@Override
		public void write(DataOutputStream sw) throws IOException {
			
		}
		@Override
		
		public void read(DataInputStream sr) throws IOException {
			isValid = false;
			version = sr.readInt();
			final int length = sr.readInt();

			if (version < 0 || version > 100000 || length < 0 || length > 100000) {
				Log.e("drPTT", "Invalid version received");
				return;
			} else {
				isValid = true;
			}
			
		}

}
