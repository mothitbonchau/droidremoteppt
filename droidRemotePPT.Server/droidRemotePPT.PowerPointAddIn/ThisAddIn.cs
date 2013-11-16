using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using PowerPoint = Microsoft.Office.Interop.PowerPoint;
using Office = Microsoft.Office.Core;
using droidRemotePPT.Server;
using Microsoft.Office.Core;

namespace droidRemotePPT.PowerPointAddIn
{
    public partial class ThisAddIn
    {
        private BluetoothServer server;
        private PPTController ppt;
        private MessageDispatcher msgDispatcher;

        #region Properties/Fields
        private static readonly object _lock = new object();

        Office.CommandBar toolBar;
        Office.CommandBarButton statusButton;

        Ribbon1 ribbon;
        IRibbonExtensibility ribbonMgr;
        #endregion

        #region ToolBar
        /// <summary>
        /// Finds a toolbar by name
        /// </summary>
        /// <param name="cmdBars">Outlooks CommandBar collection</param>
        /// <param name="name">Button Name</param>
        /// <returns>The CommandBar or null if not found</returns>
        private Office.CommandBar FindToolBar(Office.CommandBars cmdBars, string name)
        {
            foreach (var tb in cmdBars.OfType<Office.CommandBar>())
            {
                if (tb.Name == name)
                {
                    return tb;
                }
            }

            return null;
        }

        /// <summary>
        /// Finds a button by caption
        /// </summary>
        /// <param name="cmdBar">Outlooks CommandBar</param>
        /// <param name="name">Buttons caption</param>
        /// <returns>The Button or null if not found</returns>
        private Office.CommandBarButton FindButton(Office.CommandBar cmdBar, string name)
        {
            foreach (var bt in cmdBar.Controls.OfType<Office.CommandBarButton>())
            {
                if (bt.Caption == name)
                {
                    return bt;
                }
            }

            return null;
        }

        /// <summary>
        /// Create/Update the toolbar
        /// </summary>
        private void CreateToolbar()
        {
            Office.CommandBars cmdBars = this.Application.CommandBars;

            // find/create toolbar
            toolBar = FindToolBar(cmdBars, "Droid Remote PPT");
            if (toolBar == null)
            {
                toolBar = cmdBars.Add("Droid Remote PPT", Office.MsoBarPosition.msoBarTop, false, false);
            }

            // --- Static buttons ---            
            // Status button
            statusButton = (Office.CommandBarButton)toolBar.Controls.Add(1, missing, missing, missing, true);
            statusButton.Style = Office.MsoButtonStyle.msoButtonCaption;
            statusButton.Caption = "Idle";
            statusButton.Click += new Office._CommandBarButtonEvents_ClickEventHandler(statusButton_Click);
        }

        void statusButton_Click(Office.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            OnStatusButtonClick();
        }
        #endregion

        #region Ribbon
        protected override Microsoft.Office.Core.IRibbonExtensibility CreateRibbonExtensibilityObject()
        {
            ribbon = new Ribbon1(this);
            ribbon.btnStatus.Click += new Microsoft.Office.Tools.Ribbon.RibbonControlEventHandler(btnStatus_Click);
            var ribbons = new Microsoft.Office.Tools.Ribbon.IRibbonExtension[] { ribbon };
            ribbonMgr = Globals.Factory.GetRibbonFactory().CreateRibbonManager(ribbons);
            return ribbonMgr;
        }

        void btnStatus_Click(object sender, Microsoft.Office.Tools.Ribbon.RibbonControlEventArgs e)
        {
            OnStatusButtonClick();
        }
        #endregion

        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
            Logging.Root.Info("Starting AddIn");

            // Create Toolbar
            CreateToolbar();

            ppt = new PPTController(this.Application);
            server = new BluetoothServer();
            msgDispatcher = new MessageDispatcher(server, ppt);

            server.PropertyChanged += new System.ComponentModel.PropertyChangedEventHandler(server_PropertyChanged);

            server.StartBluetooth();
        }        

        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
            server.StopBluetooth();
        }

        void server_PropertyChanged(object sender, System.ComponentModel.PropertyChangedEventArgs e)
        {
            lock (_lock)
            {
                if (statusButton != null)
                {
                    if (server.NotSupported)
                    {
                        SetStatusButtonText("Not supported");
                    }
                    else
                    {
                        SetStatusButtonText(server.ClientConnected ? "Client connected" : "Client not connected");
                    }
                }
            }
        }

        private void OnStatusButtonClick()
        {
            var frm = new LoggingMessagesForm(Logging.GetMessages());
            frm.Show();
        }

        private void SetStatusButtonText(string text)
        {
            statusButton.Caption = text;
            if (ribbon != null && ribbon.btnStatus != null)
            {
                ribbon.btnStatus.Label = text;
            }
        }        

        #region VSTO generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InternalStartup()
        {
            this.Startup += new System.EventHandler(ThisAddIn_Startup);
            this.Shutdown += new System.EventHandler(ThisAddIn_Shutdown);
        }
        
        #endregion
    }
}
