using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Drawing;
using System.Drawing.Imaging;

namespace droidRemotePPT.Server.PPTMessages
{
    public abstract class PPTMessage
    {
        public static int slide { get; protected set; }
        public static int Version { get; protected set; }
        public enum MessageKind
        {
            MESSAGE_NEXT = 1,
            MESSAGE_PREV = 2,
            MESSAGE_END = 3,
            MESSAGE_NOTES = 4,
            MESSAGE_CLEAR_DRAWING = 5,
            MESSAGE_SCREEN_SIZE = 6,
            MESSAGE_SLIDE_CHANGED = 7,
            MESSAGE_START = 8,
            MESSAGE_DRAW = 9,
            MESSAGE_VERSION = 10,
            MESSAGE_BLACK = 11,
	        MESSAGE_UNBLACK = 12,
            MESSAGE_FIRST_PAGE = 13,
            MESSAGE_SELECT_PAGE = 14,
            MESSAGE_MARK_PAGE =15,
            MESSAGE_LAST_PAGE = 16,
        
        }

        public static PPTMessage CreateMessage(MessageKind kind)
        {
            switch (kind)
            {
                case MessageKind.MESSAGE_NEXT:
                    return new NextMessage();
                case MessageKind.MESSAGE_PREV:
                    return new PrevMessage();
                case MessageKind.MESSAGE_END:
                    return new EndMessage();
//                case MessageKind.MESSAGE_NOTES:
//                    return new NotesMessage();
                case MessageKind.MESSAGE_CLEAR_DRAWING:
                    return new ClearDrawingMessage();
                case MessageKind.MESSAGE_SCREEN_SIZE:
                    return new ScreenSizeMessage();
                case MessageKind.MESSAGE_START:
                    return new StartMessage();
                case MessageKind.MESSAGE_DRAW:
                    return new DrawMessage();
                case MessageKind.MESSAGE_VERSION:
                    return new VersionMessage();
                case MessageKind.MESSAGE_BLACK:
                    return new BlackScreenMessage();
                case MessageKind.MESSAGE_UNBLACK:
                    return new UnBlackScreenMessage();
                case MessageKind.MESSAGE_FIRST_PAGE:
                    return new FirstPageMessage();
                case MessageKind.MESSAGE_LAST_PAGE:
                    return new LastPageMessage();
                case MessageKind.MESSAGE_SELECT_PAGE:
                    return new SelectPageMessage();
                case MessageKind.MESSAGE_MARK_PAGE:
                    return new MarkPageMessage();
                default:
                    return null;
            }
        }

        public abstract void ReadMessage(BigEndianReader sr);
        public abstract void WriteMessage(BigEndianWriter sw);

        public abstract MessageKind Kind { get; }
    }

    public abstract class SimpleMessage : PPTMessage
    {
        public override void ReadMessage(BigEndianReader sr)
        {
            // nothing to read
        }

        public override void WriteMessage(BigEndianWriter sw)
        {
            // nothing to write
        }
    }

    public class VersionMessage : PPTMessage
    {
        public override void ReadMessage(BigEndianReader sr)
        {
            
        }

        public override void WriteMessage(BigEndianWriter sw)
        {
            sw.Write(PPTController.Version);
        }

        public override PPTMessage.MessageKind Kind
        {
            get { return MessageKind.MESSAGE_VERSION; }
        }
    }

    public class EndMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }

    public class MarkPageMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }
    
    public class SelectPageMessage : SimpleMessage
    {
        //public static int page { get; private set; }

        public override void ReadMessage(BigEndianReader sr)
        {
            slide = sr.ReadInt32();
//            slide = 2;
        }

        public override void WriteMessage(BigEndianWriter sw)
        {
            throw new NotImplementedException();
        }

        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }

    public class NextMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }

    public class FirstPageMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }
    
    public class LastPageMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }

    public class PrevMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }

    public class BlackScreenMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }
    public class UnBlackScreenMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }

    public class ToggleToSlideMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }
    public class ClearDrawingMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }
    public class StartMessage : SimpleMessage
    {
        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }

    public class NotesMessage : PPTMessage
    {
        byte[] bytes = null;
        string notes;
        int currentSlide;
        int totalSlides;

        public NotesMessage(int currentSlide, int totalSlides, string notes)
        {
            this.currentSlide = currentSlide;
            this.totalSlides = totalSlides;
            this.notes = notes;

            if (!string.IsNullOrEmpty(notes))
            {
                System.Text.ASCIIEncoding enc = new System.Text.ASCIIEncoding();
                bytes = enc.GetBytes(notes);

                System.Diagnostics.Debug.WriteLine("StringSize = {0}", bytes.Length);
            }
            else
            {
                bytes = new byte[] { };
            }
        }
        public override void ReadMessage(BigEndianReader sr)
        {
            // nothing to read
        }

        public override void WriteMessage(BigEndianWriter sw)
        {
            sw.Write(currentSlide);
            sw.Write(totalSlides);
            sw.Write(bytes.Length);
            sw.Write(bytes);
        }

        public override PPTMessage.MessageKind Kind
        {
            get { return MessageKind.MESSAGE_NOTES; }
        }
    }

    public class SlideChangedMessage : PPTMessage
    {
        private static ImageCodecInfo GetCodecInfo(string mimeType)
        {
            foreach (ImageCodecInfo encoder in ImageCodecInfo.GetImageEncoders())
                if (encoder.MimeType == mimeType)
                    return encoder;
            throw new ArgumentOutOfRangeException(
                string.Format("'{0}' not supported", mimeType));
        }

        byte[] bytes = null;
        byte[] bytesNotes = null;
        byte[] combined = null;
        int currentSlide;
        int totalSlides;
        string notes;
        
        public SlideChangedMessage(int currentSlide, int totalSlides, Image img)
        {
            this.currentSlide = currentSlide;
            this.totalSlides = totalSlides;

            if (img != null)
            {
                using (var ms = new System.IO.MemoryStream())
                {
                    using (var bmp = new Bitmap(img))
                    {
                        bmp.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg);
                        bytes = ms.ToArray();
                        System.Diagnostics.Debug.WriteLine("ImageSize = {0}", bytes.Length);
                    }
                }
            }
            else
            {
                bytes = new byte[] { };
            }
        }

        public SlideChangedMessage(int currentSlide, int totalSlides, Image img, string notes)
        {
            this.currentSlide = currentSlide;
            this.totalSlides = totalSlides;
            this.notes = notes;

            if (img != null)
            {
                using (var ms = new System.IO.MemoryStream())
                {
                    using (var bmp = new Bitmap(img))
                    {
                        bmp.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg);
                        bytes = ms.ToArray();
                        System.Diagnostics.Debug.WriteLine("ImageSize = {0}", bytes.Length);
                    }
                }
            }
            else
            {
                bytes = new byte[] { };
            }
            
            if (!string.IsNullOrEmpty(notes))
            {
                System.Text.ASCIIEncoding enc = new System.Text.ASCIIEncoding();
                bytesNotes = enc.GetBytes(notes);

                System.Diagnostics.Debug.WriteLine("StringSize = {0}", bytesNotes.Length);
                
                
            }
            else
            {
                bytesNotes = new byte[] { };
            }

        }

        public override void ReadMessage(BigEndianReader sr)
        {
            // nothing to read
        }

        public override void WriteMessage(BigEndianWriter sw)
        {
            sw.Write(currentSlide);
            sw.Write(totalSlides);
            sw.Write(bytes.Length + bytesNotes.Length);
            sw.Write(bytes.Length);
            sw.Write(bytesNotes.Length);

            if (bytesNotes.Length > 0)
            {
                combined = new byte[bytes.Length + bytesNotes.Length];
                System.Buffer.BlockCopy(bytes, 0, combined, 0, bytes.Length);
                System.Buffer.BlockCopy(bytesNotes, 0, combined, bytes.Length, bytesNotes.Length);
                sw.Write(combined);
            }
            else
            {
                sw.Write(bytes);
            }
        }
/*      
        public override void WriteMessage(BigEndianWriter sw)
        {
            sw.Write(currentSlide);
            sw.Write(totalSlides);
            sw.Write(bytes.Length);
            sw.Write(bytes);
        }
*/
        public override PPTMessage.MessageKind Kind
        {
            get { return MessageKind.MESSAGE_SLIDE_CHANGED; }
        }
    }

    public class ScreenSizeMessage : PPTMessage
    {
        public int Width { get; private set; }
        public int Height { get; private set; }

        public override void ReadMessage(BigEndianReader sr)
        {
            Width = sr.ReadInt32();
            Height = sr.ReadInt32();
        }

        public override void WriteMessage(BigEndianWriter sw)
        {
            throw new NotImplementedException();
        }

        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }

    public class DrawMessage : PPTMessage
    {
        public int[] Points { get; private set; }

        public override void ReadMessage(BigEndianReader sr)
        {
            int length = sr.ReadInt32();
            Points = new int[length];
            for (int i = 0; i < length; i++)
            {
                Points[i] = sr.ReadInt32();
            }
        }

        public override void WriteMessage(BigEndianWriter sw)
        {
            throw new NotImplementedException();
        }

        public override PPTMessage.MessageKind Kind
        {
            get { throw new NotImplementedException(); }
        }
    }
}
