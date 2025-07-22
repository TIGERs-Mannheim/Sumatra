package edu.tigers.sumatra.botmanager.serial;

public final class SerialByteConverter
{

	private SerialByteConverter()
	{
	}


	/**
	 * @param v
	 * @param offset
	 * @return
	 */
	public static int byteArray2UByte(final byte[] v, final int offset)
	{
		int res = 0;
		res |= v[offset] & 0xFF;

		return res;
	}


	/**
	 * @param v
	 * @param offset
	 * @return
	 */
	public static int byteArray2Int(final byte[] v, final int offset)
	{
		int res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;
		res |= (v[offset + 2] & 0xFF) << 16;
		res |= (v[offset + 3] & 0xFF) << 24;

		return res;
	}


	/**
	 * @param v
	 * @param offset
	 * @return
	 */
	public static long byteArray2UInt(final byte[] v, final int offset)
	{
		long res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;
		res |= (v[offset + 2] & 0xFF) << 16;
		res |= ((long) (v[offset + 3] & 0xFF)) << 24;

		return res;
	}


	/**
	 * @param v
	 * @param offset
	 * @return
	 */
	public static int byteArray2Short(final byte[] v, final int offset)
	{
		short res = 0;
		res |= (short) (v[offset] & 0xFF);
		res |= (short) ((v[offset + 1] & 0xFF) << 8);

		return res;
	}


	/**
	 * @param v
	 * @param offset
	 * @return
	 */
	public static int byteArray2UShort(final byte[] v, final int offset)
	{
		int res = 0;
		res |= v[offset] & 0xFF;
		res |= (v[offset + 1] & 0xFF) << 8;

		return res;
	}


	/**
	 * @param v
	 * @param offset
	 * @return
	 */
	public static float byteArray2Float(final byte[] v, final int offset)
	{
		return Float.intBitsToFloat(byteArray2Int(v, offset));
	}


	/**
	 * @param v
	 * @param offset
	 * @return
	 */
	public static float byteArray2HalfFloat(final byte[] v, final int offset)
	{
		return halfFloatBitsToFloat(byteArray2Short(v, offset));
	}


	/**
	 * @param v
	 * @param offset
	 * @param value
	 */
	public static void byte2ByteArray(final byte[] v, final int offset, final int value)
	{
		v[offset] = (byte) (value & 0xFF);
	}


	/**
	 * @param v
	 * @param offset
	 * @param value
	 */
	public static void short2ByteArray(final byte[] v, final int offset, final int value)
	{
		v[offset] = (byte) (value & 0xFF);
		v[offset + 1] = (byte) ((value & 0xFF00) >> 8);
	}


	/**
	 * @param v
	 * @param offset
	 * @param value
	 */
	public static void int2ByteArray(final byte[] v, final int offset, final int value)
	{
		v[offset] = (byte) (value & 0xFF);
		v[offset + 1] = (byte) ((value & 0xFF00) >> 8);
		v[offset + 2] = (byte) ((value & 0xFF0000) >> 16);
		v[offset + 3] = (byte) ((value & 0xFF000000) >> 24);
	}


	/**
	 * @param v
	 * @param offset
	 * @param value
	 */
	public static void float2ByteArray(final byte[] v, final int offset, final double value)
	{
		int2ByteArray(v, offset, Float.floatToRawIntBits((float) value));
	}


	/**
	 * @param v
	 * @param offset
	 * @param value
	 */
	public static void halfFloat2ByteArray(final byte[] v, final int offset, final double value)
	{
		short2ByteArray(v, offset, floatToHalfFloatBits((float) value));
	}


	/**
	 * Converts two bytes representing a half float to a float.
	 * Source from: <a href="http://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java">...</a>
	 *
	 * @param hbits 16 Bits of a half float
	 * @return 32Bit float
	 */
	private static float halfFloatBitsToFloat(final int hbits)
	{
		int mant = hbits & 0x03ff; // 10.0 bits mantissa
		int exp = hbits & 0x7c00; // 5.0 bits exponent
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
	 * Higher bits are set to 0.
	 * Source from: <a href="http://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java">...</a>
	 *
	 * @param fval Float to convert.
	 * @return 16Bits of a half float.
	 */
	private static int floatToHalfFloatBits(final float fval)
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


	/**
	 * Pack a value at an arbitrary bit position of an array.
	 *
	 * @param dst        Destination data array.
	 * @param bitsOffset Where to put the value.
	 * @param bitsWidth  How many bits to put from value.
	 * @param value      The value to pack.
	 * @note This method does not do any bounds checking!
	 */
	public static void packBits(final byte[] dst, final int bitsOffset, final int bitsWidth, final int value)
	{
		int srcBitIndex = 0;

		for (int dstBitIndex = bitsOffset; dstBitIndex < (bitsOffset + bitsWidth); dstBitIndex++)
		{
			int dstByte = dstBitIndex / 8;
			int dstBit = dstBitIndex % 8;

			dst[dstByte] &= (byte) ~(1 << dstBit);
			dst[dstByte] |= (byte) (((value >> srcBitIndex) & 0x01) << dstBit);

			srcBitIndex++;
		}
	}


	/**
	 * Unpack a value from an arbitrary bit position from an array.
	 *
	 * @param src Source data array.
	 * @param bitsOffset Where to get the value.
	 * @param bitsWidth How many bits to get.
	 * @return The unpacked value.
	 */
	public static int unpackBits(final byte[] src, int bitsOffset, int bitsWidth)
	{
		int result = 0;
		int dstBitIndex = 0;

		for (int srcBitIndex = bitsOffset; srcBitIndex < (bitsOffset + bitsWidth); srcBitIndex++)
		{
			int srcByte = srcBitIndex / 8;
			int srcBit = srcBitIndex % 8;

			result |= ((src[srcByte] >> srcBit) & 0x01) << dstBitIndex;
			dstBitIndex++;
		}

		return result;
	}
}
