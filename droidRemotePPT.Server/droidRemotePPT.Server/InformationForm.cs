using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace droidRemotePPT.Server
{
    public partial class InformationForm : Form
    {
        private string _messages;
        private string _version;

        public InformationForm(string messages)
        {
            InitializeComponent();

            _version = typeof(InformationForm).Assembly.GetName().Version.ToString();
            _messages = messages ?? string.Empty;

            this.txtMessages.Text = _messages;
            this.lbVersion.Text = string.Format("Verison: {0}", _version);

            lnkProject.Links.Add(new LinkLabel.Link() { LinkData = "https://code.google.com/p/droidremoteppt/", Enabled = true, Start = 0, Length = lnkProject.Text.Length });
            lnkSupport.Links.Add(new LinkLabel.Link() { LinkData = "https://groups.google.com/forum/#!forum/droidremoteppt-support", Enabled = true, Start = 0, Length = lnkSupport.Text.Length });
        }

        private void btnClose_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void linkLabel_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            System.Diagnostics.Process.Start(e.Link.LinkData as string);
        }
    }
}
