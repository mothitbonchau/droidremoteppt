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
        public Size ScreenSize = new Size( );
        // More makes no sense, BT will take too long
        public static readonly Size MaxScreenSize = new Size(320, 200); 

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
                if (msg is MarkPageMessage)
                {
                    _pptController.MarkSlide();
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);
                }

                else if (msg is ClearDrawingMessage)
                {
                    _pptController.NextSlide();
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);

                }
                else if (msg is EndMessage)
                {
                    _pptController.EndPresentation();
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);
                }

                else  if (msg is LastPageMessage)
                {
                    _pptController.LastPage();
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);
                }

                else if (msg is FirstPageMessage)
                {
                    _pptController.FirstPage();
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);

                }

                else if (msg is BlackScreenMessage)
                {
                    _pptController.BlackScreen();
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);

                }

                else if (msg is UnBlackScreenMessage)
                {
                    _pptController.UnBlackScreen();
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);

                }
                else if (msg is NextMessage)
                {
                    _pptController.NextSlide();
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);
                }
                else if (msg is PrevMessage)
                {
                    _pptController.PrevSlide();
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);
                }
                else if (msg is StartMessage)
                {
                    _pptController.Start();
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);
                }

                else if (msg is SelectPageMessage)
                {
                    int slide = PPTMessage.slide;
                    _pptController.SelectSlide(slide);
                    SendSlideImage(_pptController.CurrentSlide, _pptController.TotalSlides);
                }

                else if (msg is ScreenSizeMessage)
                {
                    ScreenSizeMessage ssm = (ScreenSizeMessage)msg;
                    ScreenSize = new Size(Math.Max(ssm.Width, ssm.Height), Math.Min(ssm.Width, ssm.Height));
                    // Limit to MaxScreenSize
                    if (ScreenSize.Width > MaxScreenSize.Width)
                    {
                        ScreenSize = MaxScreenSize;
                    }
                }
                else if (msg is VersionMessage)
                {

                    _pptController.SendVersion();
                    //int ver = _pptController.SendVersion();
                    //   _btServer.SendMessage(PPTController.Version);
                }else if (msg is NotesMessage)
                {
                    SendSlideNotes(_pptController.CurrentSlide, _pptController.TotalSlides);
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

        private void SendSlideNotes(int currentSlide, int totalSlides)
        {
            string notes = _pptController.GetSlideNotes();
            if (string.IsNullOrEmpty(notes)) return;
            
            var msg = new NotesMessage(currentSlide, totalSlides, notes);
            _btServer.SendMessage(msg);
        }

        private void SendSlideImage(int currentSlide, int totalSlides)
        {
            if (!ScreenSize.IsEmpty)
            {
                var img = _pptController.GetSlideImage();
                if (img == null) return;

                var notes = _pptController.GetSlideNotes();

                var smallImg = Utils.Resize(img, ScreenSize.Width);
                var msg = new SlideChangedMessage(currentSlide, totalSlides, smallImg, notes);//, _pptController.GetSlideNotes());
                _btServer.SendMessage(msg);
            }
        }        
    }
}
