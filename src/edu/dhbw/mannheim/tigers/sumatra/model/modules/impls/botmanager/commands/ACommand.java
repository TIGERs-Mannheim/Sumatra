/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands;

public abstract class ACommand
{
	public abstract void setData(byte[] data);
	public abstract byte[] getData();
	public abstract int getCommand();
	public abstract int getDataLength();
	
	public byte[] getHeader()
	{
		byte header[] = new byte[CommandConstants.HEADER_SIZE];

		header[0] = (byte) (getCommand() & 0x000000FF);
		header[1] = (byte) ((getCommand() & 0x0000FF00) >> 8);
		header[2] = (byte) (getDataLength() & 0x000000FF);
		header[3] = (byte) ((getDataLength() & 0x0000FF00) >> 8);

		return header;
	}

	public int getSection()
	{
		return ((getCommand() & 0xFF00) >> 8);
	}

	public byte[] getTransferData()
	{
		byte[] header = getHeader();
		byte[] data = getData();
		
		byte[] packet = new byte[header.length + data.length];
		
		System.arraycopy(header, 0, packet, 0, header.length);
		System.arraycopy(data, 0, packet, header.length, data.length);

		return packet;
	}

	public static int byte2Int(byte b)
	{
		return (int) b;	// & 0xFFFFFFFF); <-- Has no effect
	}
	
	public static int byteArray2UByte(byte[] v, int offset)
	{
		int res = 0;
		res |= v[offset] & 0xFF;
		
		return res;
	}

	public static int byteArray2Int(byte[] v, int offset)
	{
		int res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;
		res |= (v[offset + 2] & 0xFF) << 16;
		res |= (v[offset + 3] & 0xFF) << 24;

		return res;
	}
	
	public static long byteArray2UInt(byte[] v, int offset)
	{
		long res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;
		res |= (v[offset + 2] & 0xFF) << 16;
		res |= ((long)(v[offset + 3] & 0xFF)) << 24;
		
		return res;
	}

	public static int byteArray2Short(byte[] v, int offset)
	{
		short res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;

		return res;
	}

	public static int byteArray2UShort(byte[] v, int offset)
	{
		int res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;

		return res;
	}

	public static float byteArray2Float(byte[] v, int offset)
	{
		int bits = 0;
		int i = 0;
		for (int shifter = 3; shifter >= 0; shifter--)
		{
			bits |= ((int) v[offset + i] & 0xFF) << (shifter * 8);
			i++;
		}

		return Float.intBitsToFloat(bits);
	}
	
	public static void byte2ByteArray(byte[] v, int offset, int value)
	{
		v[offset] = (byte) (value & 0xFF);
	}

	public static void short2ByteArray(byte[] v, int offset, int value)
	{
		v[offset + 0] = (byte) (value & 0xFF);
		v[offset + 1] = (byte) ((value & 0xFF00) >> 8);
	}

	public static void int2ByteArray(byte[] v, int offset, int value)
	{
		v[offset + 0] = (byte) (value & 0xFF);
		v[offset + 1] = (byte) ((value & 0xFF00) >> 8);
		v[offset + 2] = (byte) ((value & 0xFF0000) >> 16);
		v[offset + 3] = (byte) ((value & 0xFF000000) >> 24);
	}

	public static void float2ByteArray(byte[] v, int offset, float value)
	{
		int2ByteArray(v, offset, Float.floatToRawIntBits(value));
	}
}
