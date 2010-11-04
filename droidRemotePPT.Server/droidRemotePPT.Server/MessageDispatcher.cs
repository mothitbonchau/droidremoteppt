using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using droidRemotePPT.Server.PPTMessages;
using System.Drawing;
using System.IO;

namespace droidRemotePPT.Server
{
    public class MessageDispatcher
    {
        public Size ScreenSize = new Size();

        public MessageDispatcher(BluetoothServer btServer, PPTController pptController)
        {
            this._btServer = btServer;
            this._pptController = pptController;

            _btServer.MessageReceived += new BluetoothServer.MessageReceivedEventHandler(server_MessageReceived);
        }

        private readonly BluetoothServer _btServer;
        private readonly PPTController _pptController;

        void server_MessageReceived(PPTMessage msg)
        {
            System.Windows.Threading.Dispatcher.CurrentDispatcher
                .Invoke(new Action(() => OnMessageReceived(msg)));
        }

        void OnMessageReceived(PPTMessage msg)
        {
            try
            {
                if (msg is NextMessage)
                {
                    _pptController.NextSlide();
                    SendSlideImage();
                }
                else if (msg is PrevMessage)
                {
                    _pptController.PrevSlide();
                    SendSlideImage();
                }
                else if (msg is StartMessage)
                {
                    _pptController.Start();
                    SendSlideImage();
                }
                else if (msg is ScreenSizeMessage)
                {
                    ScreenSizeMessage ssm = (ScreenSizeMessage)msg;
                    ScreenSize = new Size(Math.Max(ssm.Width, ssm.Height), Math.Min(ssm.Width, ssm.Height));
                }
                else if (msg is DrawMessage)
                {
                    DrawMessage dm = (DrawMessage)msg;
                    _pptController.Draw(dm.Points, ScreenSize);
                }
            }
            catch (IOException ex)
            {
                System.Diagnostics.Debug.WriteLine(ex.ToString());
            }
        }

        private void SendSlideImage()
        {
            if (!ScreenSize.IsEmpty)
            {
                var img = _pptController.GetSlideImage();
                if (img == null) return;

                var smallImg = Utils.Resize(img, ScreenSize.Width);
                var msg = new SlideChangedMessage(smallImg);
                _btServer.SendMessage(msg);
            }
        }        
    }
}
