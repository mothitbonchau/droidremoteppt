using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ComponentModel;

namespace droidRemotePPT.Server
{
    public class WindowViewModel : INotifyPropertyChanged
    {
        private readonly BluetoothServer _server;

        public WindowViewModel(BluetoothServer server)
        {
            this._server = server;
            this._server.PropertyChanged += server_PropertyChanged;
        }

        void server_PropertyChanged(object sender, PropertyChangedEventArgs e)
        {
            OnPropertyChanged(e.PropertyName);
            OnPropertyChanged("StatusText");
        }

        public bool NotSupported
        {
            get
            {
                return _server.NotSupported;
            }
        }

        public bool Listening
        {
            get
            {
                return _server.Listening;
            }
        }

        public bool ClientConnected
        {
            get
            {
                return _server.ClientConnected;
            }
        }

        public string StatusText
        {
            get
            {
                if (NotSupported) return "Bluetooth not suppported";
                if (ClientConnected) return "Client connected";
                if (!ClientConnected) return "Client not connected";
                return "unknown status";
            }
        }

        #region INotifyPropertyChanged Members

        public event PropertyChangedEventHandler PropertyChanged;

        protected void OnPropertyChanged(string name)
        {
            PropertyChangedEventHandler temp = PropertyChanged;
            if (temp != null)
            {
                temp(this, new PropertyChangedEventArgs(name));
            }
        }

        #endregion
    }
}
