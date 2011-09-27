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
        public enum MessageKind
        {
            MESSAGE_NEXT = 1,
            MESSAGE_PREV = 2,
            MESSAGE_END = 3,
            MESSAGE_TOGGLE_BLACK_SCREEN = 4,
            MESSAGE_CLEAR_DRAWING = 5,
            MESSAGE_SCREEN_SIZE = 6,
            MESSAGE_SLIDE_CHANGED = 7,
            MESSAGE_START = 8,
            MESSAGE_DRAW = 9,
        }

        public static PPTMessage CreateMessage(MessageKind kind)
        {
            switch (kind)
            {
                case MessageKind.MESSAGE_NEXT:
                    return new NextMessage();
                case MessageKind.MESSAGE_PREV:
                    return new PrevMessage();
                case MessageKind.MESSAGE_TOGGLE_BLACK_SCREEN:
                    return new ToggleBlackScreenMessage();
                case MessageKind.MESSAGE_CLEAR_DRAWING:
                    return new ClearDrawingMessage();
                case MessageKind.MESSAGE_SCREEN_SIZE:
                    return new ScreenSizeMessage();
                case MessageKind.MESSAGE_START:
                    return new StartMessage();
                case MessageKind.MESSAGE_DRAW:
                    return new DrawMessage();
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
    public class NextMessage : SimpleMessage
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
    public class ToggleBlackScreenMessage : SimpleMessage
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
        public SlideChangedMessage(Image img)
        {
            using (var ms = new System.IO.MemoryStream())
            {
                //var enc = GetCodecInfo("image/jpeg");
                //var eps = new EncoderParameters(1);
                //eps.Param[0] = new EncoderParameter(System.Drawing.Imaging.Encoder.Quality, 8L);

                //img.Save(ms, enc, eps);
                img.Save(ms, System.Drawing.Imaging.ImageFormat.Png);
                bytes = ms.ToArray();
                System.Diagnostics.Debug.WriteLine("ImageSize = {0}", bytes.Length);
            }
        }

        public override void ReadMessage(BigEndianReader sr)
        {
            // nothing to read
        }

        public override void WriteMessage(BigEndianWriter sw)
        {
            sw.Write(bytes.Length);
            sw.Write(bytes);
        }

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
