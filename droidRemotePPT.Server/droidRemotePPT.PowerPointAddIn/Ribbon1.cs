using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Office.Tools.Ribbon;

namespace droidRemotePPT.PowerPointAddIn
{
    public partial class Ribbon1
    {
        private ThisAddIn addIn;

        public Ribbon1(ThisAddIn addIn)
            : this()
        {
            this.addIn = addIn;
        }

        private void Ribbon1_Load(object sender, RibbonUIEventArgs e)
        {

        }

        private void btnStatus_Click(object sender, RibbonControlEventArgs e)
        {
            // Do nothing
        }
    }
}
