using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using InTheHand.Net.Bluetooth;
using System.Threading;
using InTheHand.Net.Sockets;
using System.ComponentModel;
using droidRemotePPT.Server.PPTMessages;

namespace droidRemotePPT.Server
{
    public class BluetoothServer : IDisposable, INotifyPropertyChanged
    {
        //unique service identifier
        public static Guid ServiceGUID = new Guid("ABF32797-4DAE-4890-A23D-33DC8E3E2111");

        private BluetoothRadio br;
        private BluetoothListener bl;
        private Thread thread;

        public delegate void MessageReceivedEventHandler(PPTMessage msg);
        public event MessageReceivedEventHandler MessageReceived;

        protected void OnMessageReceived(PPTMessage msg)
        {
            MessageReceivedEventHandler temp = MessageReceived;
            if (temp != null)
            {
                temp(msg);
            }
        }

        private bool _NotSupported = true;
        public bool NotSupported
        {
            get
            {
                return _NotSupported;
            }
            set
            {
                _NotSupported = value;
                OnPropertyChanged("NotSupported");
            }
        }

        private bool _Listening = false;
        public bool Listening
        {
            get
            {
                return _Listening;
            }
            set
            {
                _Listening = value;
                OnPropertyChanged("Listening");
            }
        }

        private bool _ClientConnected = false;
        public bool ClientConnected
        {
            get
            {
                return _ClientConnected;
            }
            set
            {
                _ClientConnected = value;
                OnPropertyChanged("ClientConnected");
            }
        }

        public BluetoothServer()
        {
            ClientConnected = false;
            Listening = false;

            br = BluetoothRadio.PrimaryRadio;
            if (br == null)
            {
                NotSupported = true;
            }
            else
            {
                NotSupported = false;
            }
        }

        public void StartBluetooth()
        {
            Listening = true;
            try
            {
                bl = new BluetoothListener(ServiceGUID);
                bl.Start();
            }
            catch
            {
                NotSupported = true;
                return;
            }

            thread = new Thread(new ThreadStart(ListenLoop));
            thread.Start();
        }

        public void StopBluetooth()
        {
            if (bl != null)
            {
                Listening = false;
                ClientConnected = false;
                bl.Stop();
                //thread.Abort();
            }
        }

        private BigEndianReader sr;
        private BigEndianWriter sw;

        private void ListenLoop()
        {
            while (Listening)
            {
                BluetoothClient bc;
                ClientConnected = false;
                try
                {
                    bc = bl.AcceptBluetoothClient();
                    var s = bc.GetStream();
                    sr = new BigEndianReader(new System.IO.BinaryReader(s));
                    sw = new BigEndianWriter(new System.IO.BinaryWriter(s));
                }
                catch (Exception ex)
                {
                    System.Diagnostics.Debug.WriteLine(ex.ToString());
                    break;
                }
                ClientConnected = true;

                //keep connection open
                while (Listening)
                {
                    byte msgID;
                    try
                    {
                        msgID = sr.ReadByte();
                    }
                    catch
                    {
                        //connection lost
                        break;
                    }


                    PPTMessage msg = PPTMessage.CreateMessage((PPTMessage.MessageKind)msgID);
                    msg.ReadMessage(sr);
                    OnMessageReceived(msg);
                }

                try
                {
                    bc.Close();
                }
                catch
                {
                    // Dont care
                }
            }

        }

        public void SendMessage(PPTMessage msg)
        {
            sw.Write((byte)msg.Kind);
            msg.WriteMessage(sw);
        }

        #region IDisposable Members

        public void Dispose()
        {

        }

        #endregion

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
