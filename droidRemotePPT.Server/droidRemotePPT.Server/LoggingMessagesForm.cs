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
    public partial class LoggingMessagesForm : Form
    {
        public LoggingMessagesForm(string messages)
        {
            InitializeComponent();

            this.txtMessages.Text = messages;
        }        
    }
}
