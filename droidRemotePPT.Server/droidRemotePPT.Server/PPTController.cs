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
        public PPTController()
        {
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
    }
}
