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
            Logging.Root.InfoFormat("{0} message received", msg.GetType().Name);
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
            Logging.Root.Info("Starting BluetoothListener");
            Listening = true;
            try
            {
                bl = new BluetoothListener(ServiceGUID);
                bl.Start();
            }
            catch(Exception ex)
            {
                Logging.Root.Error("Error starting BluetoothListener", ex);
                NotSupported = true;
                return;
            }

            thread = new Thread(new ThreadStart(ListenLoop));
            thread.Start();
        }

        public void StopBluetooth()
        {
            Logging.Root.Info("Stopping BluetoothListener");
            try
            {
                if (bl != null)
                {
                    Listening = false;
                    ClientConnected = false;
                    bl.Stop();
                }
            }
            catch (Exception ex)
            {
                Logging.Root.Error("Error stopping BluetoothListener", ex);
            }
        }

        private BigEndianReader sr;
        private BigEndianWriter sw;

        private void ListenLoop()
        {
            while (Listening)
            {
                BluetoothClient bc = null;
                try
                {
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
                        Logging.Root.Debug("Error connecting client", ex);
                        break;
                    }
                    Logging.Root.Info("Client connected");
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
                            Logging.Root.Info("Connection lost");
                            break;
                        }

                        try
                        {
                            PPTMessage msg = PPTMessage.CreateMessage((PPTMessage.MessageKind)msgID);
                            msg.ReadMessage(sr);
                            OnMessageReceived(msg);
                        }
                        catch (Exception ex)
                        {
                            Logging.Root.Error("Error while processing messages", ex);
                            break;
                        }
                    }
                }
                finally
                {
                    try
                    {
                        if(bc != null) bc.Close();
                    }
                    catch
                    {
                        // Dont care
                    }
                }
            } // while (Listening)
        }

        public void SendMessage(PPTMessage msg)
        {
            if (msg == null) return;

            Logging.Root.InfoFormat("Sending {0} message", msg.GetType().Name);
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
