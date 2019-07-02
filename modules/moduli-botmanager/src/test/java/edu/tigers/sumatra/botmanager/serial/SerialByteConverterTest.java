package edu.tigers.sumatra.botmanager.serial;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class SerialByteConverterTest
{
	@Test
	public void testInt()
	{
		int input = 0xC0DEBA5E;
		byte[] data = new byte[4];
		
		SerialByteConverter.int2ByteArray(data, 0, input);
		
		int output = SerialByteConverter.byteArray2Int(data, 0);
		
		assertEquals(input, output);
	}
	
	
	@Test
	public void testUShort()
	{
		int input = 0xC0DE;
		byte[] data = new byte[2];
		
		SerialByteConverter.short2ByteArray(data, 0, input);
		
		int output = SerialByteConverter.byteArray2UShort(data, 0);
		
		assertEquals(input, output);
	}
	
	
	@Test
	public void testShort()
	{
		short input = (short) 0xC0DE;
		byte[] data = new byte[2];
		
		SerialByteConverter.short2ByteArray(data, 0, input);
		
		int output = SerialByteConverter.byteArray2Short(data, 0);
		
		assertEquals(input, output);
	}
	
	
	@Test
	public void testFloat()
	{
		float input = 0xC0DEBA5E;
		byte[] data = new byte[4];
		
		SerialByteConverter.float2ByteArray(data, 0, input);
		
		float output = SerialByteConverter.byteArray2Float(data, 0);
		
		assertEquals(input, output, Float.MIN_NORMAL);
	}
	
	
	@Test
	public void testHalfFloat()
	{
		// float input = 0xC0DEBA5E;
		float input = 1.0f;
		byte[] data = new byte[2];
		
		SerialByteConverter.halfFloat2ByteArray(data, 0, input);
		
		float output = SerialByteConverter.byteArray2HalfFloat(data, 0);
		
		assertEquals(input, output, 0.0002f);
	}
}