package net.zaczek.droidRemotePPT.Messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SelectSlideMessage extends PPTMessage {

		private int slide;

		public SelectSlideMessage(int slide) {
			this.slide = slide;
		}
	
		@Override
		public byte getMessageId() {
			return PPTMessage.MESSAGE_SELECT_PAGE;
		}

		@Override
		public void write(DataOutputStream sw) throws IOException {
			sw.writeInt(slide);
		}
		@Override
		public void read(DataInputStream sr) throws IOException {
			
		}

}
