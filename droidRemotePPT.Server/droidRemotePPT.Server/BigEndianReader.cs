using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace droidRemotePPT.Server
{
    /// <summary>
    /// Utilities for reading big-endian files
    /// </summary>
    public class BigEndianReader
    {
        public BigEndianReader(BinaryReader baseReader)
        {
            mBaseReader = baseReader;
        }

        private readonly BinaryReader mBaseReader;

        public short ReadInt16()
        {
            return BitConverter.ToInt16(ReadBigEndianBytes(2), 0);
        }

        public ushort ReadUInt16()
        {
            return BitConverter.ToUInt16(ReadBigEndianBytes(2), 0);
        }

        public uint ReadUInt32()
        {
            return BitConverter.ToUInt32(ReadBigEndianBytes(4), 0);
        }

        public int ReadInt32()
        {
            return BitConverter.ToInt32(ReadBigEndianBytes(4), 0);
        }

        public byte[] ReadBigEndianBytes(int count)
        {
            byte[] bytes = new byte[count];
            for (int i = count - 1; i >= 0; i--)
                bytes[i] = mBaseReader.ReadByte();

            return bytes;
        }

        public byte ReadByte()
        {
            return mBaseReader.ReadByte();
        }

        public byte[] ReadBytes(int count)
        {
            return mBaseReader.ReadBytes(count);
        }

        public void Close()
        {
            mBaseReader.Close();
        }

        public Stream BaseStream
        {
            get { return mBaseReader.BaseStream; }
        }

        
    }

    public class BigEndianWriter
    {
        public BigEndianWriter(BinaryWriter baseWriter)
        {
            mBaseWriter = baseWriter;
        }

        private readonly BinaryWriter mBaseWriter;

        public void Write(byte v)
        {
            mBaseWriter.Write(v);
        }

        public void Write(byte[] buffer)
        {
            mBaseWriter.Write(buffer);
        }

        public void Write(int v)
        {
            mBaseWriter.Write(ConvertBigEndianBytes(BitConverter.GetBytes(v)));
        }

        public byte[] ConvertBigEndianBytes(byte[] input)
        {
            byte[] bytes = new byte[input.Length];
            int j = 0;
            for (int i = input.Length - 1; i >= 0; i--, j++)
                bytes[i] = input[j];

            return bytes;
        }
    }
}

