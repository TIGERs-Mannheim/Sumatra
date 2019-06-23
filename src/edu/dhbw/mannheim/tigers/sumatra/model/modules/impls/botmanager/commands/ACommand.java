/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands;

/**
 *
 */
public abstract class ACommand
{
	private final ECommand	cmd;
	
	
	protected ACommand(ECommand cmd)
	{
		this.cmd = cmd;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ECommand getType()
	{
		return cmd;
	}
	
	
	/**
	 * 
	 * @param b
	 * @return
	 */
	public static int byte2Int(byte b)
	{
		// & 0xFFFFFFFF); <-- Has no effect
		return b;
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @return
	 */
	public static int byteArray2UByte(byte[] v, int offset)
	{
		int res = 0;
		res |= v[offset] & 0xFF;
		
		return res;
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @return
	 */
	public static int byteArray2Int(byte[] v, int offset)
	{
		int res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;
		res |= (v[offset + 2] & 0xFF) << 16;
		res |= (v[offset + 3] & 0xFF) << 24;
		
		return res;
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @return
	 */
	public static long byteArray2UInt(byte[] v, int offset)
	{
		long res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;
		res |= (v[offset + 2] & 0xFF) << 16;
		res |= ((long) (v[offset + 3] & 0xFF)) << 24;
		
		return res;
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @return
	 */
	public static int byteArray2Short(byte[] v, int offset)
	{
		short res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;
		
		return res;
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @return
	 */
	public static int byteArray2UShort(byte[] v, int offset)
	{
		int res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;
		
		return res;
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @return
	 */
	public static float byteArray2Float(byte[] v, int offset)
	{
		return Float.intBitsToFloat(byteArray2Int(v, offset));
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @return
	 */
	public static float byteArray2HalfFloat(byte[] v, int offset)
	{
		return halfFloatBitsToFloat(byteArray2Short(v, offset));
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @param value
	 */
	public static void byte2ByteArray(byte[] v, int offset, int value)
	{
		v[offset] = (byte) (value & 0xFF);
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @param value
	 */
	public static void short2ByteArray(byte[] v, int offset, int value)
	{
		v[offset + 0] = (byte) (value & 0xFF);
		v[offset + 1] = (byte) ((value & 0xFF00) >> 8);
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @param value
	 */
	public static void int2ByteArray(byte[] v, int offset, int value)
	{
		v[offset + 0] = (byte) (value & 0xFF);
		v[offset + 1] = (byte) ((value & 0xFF00) >> 8);
		v[offset + 2] = (byte) ((value & 0xFF0000) >> 16);
		v[offset + 3] = (byte) ((value & 0xFF000000) >> 24);
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @param value
	 */
	public static void float2ByteArray(byte[] v, int offset, float value)
	{
		int2ByteArray(v, offset, Float.floatToRawIntBits(value));
	}
	
	
	/**
	 * 
	 * @param v
	 * @param offset
	 * @param value
	 */
	public static void halfFloat2ByteArray(byte[] v, int offset, float value)
	{
		short2ByteArray(v, offset, floatToHalfFloatBits(value));
	}
	
	
	/**
	 * Converts two bytes representing a half float to a float.
	 * 
	 * Source from: http://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java
	 * 
	 * @param hbits 16 Bits of a half float
	 * @return 32Bit float
	 */
	public static float halfFloatBitsToFloat(int hbits)
	{
		int mant = hbits & 0x03ff; // 10 bits mantissa
		int exp = hbits & 0x7c00; // 5 bits exponent
		if (exp == 0x7c00)
		{
			exp = 0x3fc00; // -> NaN/Inf
		} else if (exp != 0) // normalized value
		{
			exp += 0x1c000; // exp - 15 + 127
			if ((mant == 0) && (exp > 0x1c400))
			{
				return Float.intBitsToFloat(((hbits & 0x8000) << 16) | (exp << 13) | 0x3ff);
			}
		} else if (mant != 0) // && exp==0 -> subnormal
		{
			exp = 0x1c400; // make it normal
			do
			{
				mant <<= 1; // mantissa * 2
				exp -= 0x400; // decrease exp by 1
			} while ((mant & 0x400) == 0); // while not normal
			mant &= 0x3ff; // discard subnormal bit
		} // else +/-0 -> +/-0
		return Float.intBitsToFloat( // combine all parts
				((hbits & 0x8000) << 16 // sign << ( 31 - 15 )
						)
						| ((exp | mant) << 13)); // value << ( 23 - 10 )
	}
	
	
	/**
	 * Converts a float to half float bits.
	 * 
	 * Higher bits are set to 0.
	 * 
	 * Source from: http://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java
	 * 
	 * @param fval Float to convert.
	 * @return 16Bits of a half float.
	 */
	public static int floatToHalfFloatBits(float fval)
	{
		int fbits = Float.floatToIntBits(fval);
		int sign = (fbits >>> 16) & 0x8000; // sign only
		int val = (fbits & 0x7fffffff) + 0x1000; // rounded value
		
		if (val >= 0x47800000) // might be or become NaN/Inf
		{ // avoid Inf due to rounding
			if ((fbits & 0x7fffffff) >= 0x47800000)
			{ // is or must become NaN/Inf
				if (val < 0x7f800000)
				{
					return sign | 0x7c00; // make it +/-Inf
				}
				return sign | 0x7c00 | // remains +/-Inf or NaN
						((fbits & 0x007fffff) >>> 13); // keep NaN (and Inf) bits
			}
			return sign | 0x7bff; // unrounded not quite Inf
		}
		if (val >= 0x38800000)
		{
			return sign | ((val - 0x38000000) >>> 13); // exp - 127 + 15
		}
		if (val < 0x33000000)
		{
			return sign; // becomes +/-0
		}
		val = (fbits & 0x7fffffff) >>> 23; // tmp exp for subnormal calc
		return sign | ((((fbits & 0x7fffff) | 0x800000) // add subnormal bit
				+ (0x800000 >>> (val - 102)) // round depending on cut off
				) >>> (126 - val)); // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
	}
}
