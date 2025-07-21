package edu.tigers.sumatra.botmanager.serial;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class SerialByteConverterTest
{
	@Test
	void testInt()
	{
		int input = 0xC0DEBA5E;
		byte[] data = new byte[4];
		
		SerialByteConverter.int2ByteArray(data, 0, input);
		
		int output = SerialByteConverter.byteArray2Int(data, 0);
		
		assertEquals(input, output);
	}
	
	
	@Test
	void testUShort()
	{
		int input = 0xC0DE;
		byte[] data = new byte[2];
		
		SerialByteConverter.short2ByteArray(data, 0, input);
		
		int output = SerialByteConverter.byteArray2UShort(data, 0);
		
		assertEquals(input, output);
	}
	
	
	@Test
	void testShort()
	{
		short input = (short) 0xC0DE;
		byte[] data = new byte[2];
		
		SerialByteConverter.short2ByteArray(data, 0, input);
		
		int output = SerialByteConverter.byteArray2Short(data, 0);
		
		assertEquals(input, output);
	}
	
	
	@Test
	void testFloat()
	{
		float input = 0xC0DEBA5E;
		byte[] data = new byte[4];
		
		SerialByteConverter.float2ByteArray(data, 0, input);
		
		float output = SerialByteConverter.byteArray2Float(data, 0);
		
		assertEquals(input, output, Float.MIN_NORMAL);
	}
	
	
	@Test
	void testHalfFloat()
	{
		// float input = 0xC0DEBA5E;
		float input = 1.0f;
		byte[] data = new byte[2];
		
		SerialByteConverter.halfFloat2ByteArray(data, 0, input);
		
		float output = SerialByteConverter.byteArray2HalfFloat(data, 0);
		
		assertEquals(input, output, 0.0002f);
	}
}