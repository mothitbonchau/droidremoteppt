using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using PPT = Microsoft.Office.Interop.PowerPoint;
using Microsoft.Office.Core;
using System.Drawing;

namespace droidRemotePPT.Server
{
    public abstract class PPTConnection
    {
        private PPT.Application app;

        

        public PPTConnection()
        {
            app = new PPT.Application();
            app.Visible = MsoTriState.msoCTrue;
        }

        public PPTConnection(PPT.Application app)
        {
            this.app = app;
        }

        public PPT.Application App
        {
            get
            {
                return app;
            }
        }

        public PPT.Presentation Presentation
        {
            get
            {
                if (App.Presentations.Count == 0) return null;
                return App.ActivePresentation;
            }
        }
    }

    public class PPTController : PPTConnection
    {
        public static int MarkedSlide;
//        public static int NewFunc;
    
        public PPTController()
        {

            MarkedSlide = 1;
            black = false;
        }

        public string SendVersion()
        {
            if (!IsActive)
            {
//                return Convert.ToInt32(Presentation.Application.Version);
                return (Presentation.Application.Version);
            }
            return null;

        }

        public PPTController(PPT.Application application)
            : base(application)
        {
        }

        public bool IsActive
        {
            get
            {
                if (Presentation == null) return false;
                // Try to touch SlideShowWindow
                try
                {
                    var tmp = Presentation.SlideShowWindow.Active;
                    return tmp != MsoTriState.msoFalse;
                }
                catch
                {
                    return false;
                }
            }
        }


        public void MarkSlide()
        {
            if (!IsActive) return;
           
            MarkedSlide = CurrentSlide;

        }
        
        public void BlackScreen()
        {
            if (!IsActive) return;
                        System.Windows.Forms.SendKeys.SendWait("{b}");

            //            Start();

            //            Presentation.SlideShowWindow.View.EraseDrawing();
            //MarkedSlide = Presentation.SlideShowWindow.View.CurrentShowPosition;
            MarkedSlide = CurrentSlide;
/*
            Presentation.SlideShowWindow.View.Last();
            NextSlide();

            SetCurrentSlide(MarkedSlide);
*/
            black = true;

        }

        public void UnBlackScreen()
        {
            if (!IsActive) return;
            Presentation.SlideShowWindow.View.GotoSlide(MarkedSlide);

            black = false;
        }

        public void SelectSlide(int slide)
        {
            if (!IsActive) return;

//           slide = MarkedSlide;
           MarkedSlide = CurrentSlide;
            
            Presentation.SlideShowWindow.View.GotoSlide(slide);
            
        }

        public void EndPresentation()
        {
            if (!IsActive) return;
            Presentation.SlideShowWindow.View.Exit();
        }

        public void FirstPage()
        {
            if (!IsActive) return;
            Presentation.SlideShowWindow.View.First();
        }

        public void LastPage()
        {
            if (!IsActive) return;
            Presentation.SlideShowWindow.View.Last();
        }

        public void NextSlide()
        {
            if (!IsActive) return;
            Presentation.SlideShowWindow.View.Next();
        }

        public void PrevSlide()
        {
            if (!IsActive) return;
           Presentation.SlideShowWindow.View.Previous();
        }

        public void Start()
        {
            if (Presentation == null) return;
            Presentation.SlideShowSettings.Run();
        }

        public string GetSlideNotes()
        {
            if (!IsActive) return "";
            string text = "";
 
            int i = 0;
            for (i = 2; i <= 3; i++)
            {
                if (CurrentSlide <= TotalSlides && Presentation.Slides[CurrentSlide].NotesPage.Shapes[i].HasTextFrame == MsoTriState.msoTrue)
                    if (text != "")
                    {
                        text += "\n\nLower notes:\n\n\t";
                        text += Presentation.Slides[CurrentSlide].NotesPage.Shapes[i].TextFrame.TextRange.Text.ToString().Trim();
                    }
                    else
                        text = Presentation.Slides[CurrentSlide].NotesPage.Shapes[i].TextFrame.TextRange.Text.ToString().Trim();
            }
            return text;
        }

        public Image GetSlideImage()
        {
            if (!IsActive) return null;
            var w = Presentation.SlideShowWindow;
            return Utils.CaptureWindow(new IntPtr(w.HWND));
        }

        public void Draw(int[] points, Size ScreenSize)
        {
            if (!IsActive) return;
            var v = Presentation.SlideShowWindow.View;

            // Fixed size??
            SizeF s = new SizeF(800, 600);

            for (int i = 2; i < points.Length; i += 2)
            {
                var startX = (float)points[i - 2] / (float)ScreenSize.Width * s.Width;
                var startY = (float)points[i - 1] / (float)ScreenSize.Height * s.Height;
                var endX = (float)points[i - 0] / (float)ScreenSize.Width * s.Width;
                var endY = (float)points[i + 1] / (float)ScreenSize.Height * s.Height;

                v.DrawLine(startX, startY, endX, endY);
            }
        }


        void SetCurrentSlide(int var)
        {
            CurrentSlide = (var);
        }

        private int _CurrentSlide;

        bool black;

        public static int Version;

        public int CurrentSlide
        {
            
            get
            {
                if (!IsActive) return 0;
//                if (black)
                {
                    return Presentation.SlideShowWindow.View.CurrentShowPosition;
                }
//                else
                {
//                    return _CurrentSlide;
                }
            }
            set
            {
                _CurrentSlide = value;
            }
        }

        public int TotalSlides
        {
            get
            {
                if (!IsActive) return 0;
                return Presentation.Slides.Count;
            }
        }
    }
}
