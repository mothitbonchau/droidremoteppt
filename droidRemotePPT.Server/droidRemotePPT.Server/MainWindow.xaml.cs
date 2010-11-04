using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

using droidRemotePPT.Server.PPTMessages;

namespace droidRemotePPT.Server
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        private BluetoothServer server;
        private PPTController ppt;
        private MessageDispatcher msgDispatcher;
        private WindowViewModel viewMdl;

        public MainWindow()
        {
            InitializeComponent();

            ppt = new PPTController();
            server = new BluetoothServer();
            msgDispatcher = new MessageDispatcher(server, ppt);

            viewMdl = new WindowViewModel(server);
            DataContext = viewMdl;
            
            //PlayWithPPT();
        }

        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            server.StartBluetooth();

        }

        private void Window_Closed(object sender, EventArgs e)
        {
            server.StopBluetooth();
        }
    }
}
