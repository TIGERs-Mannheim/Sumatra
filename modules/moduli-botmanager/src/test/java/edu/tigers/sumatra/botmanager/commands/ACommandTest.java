/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.03.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * Tests some byte array to type conversions.
 * 
 * @author AndreR
 * 
 */
public class ACommandTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	@Test
	public void testInt()
	{
		int input = 0xC0DEBA5E;
		byte[] data = new byte[4];
		
		ACommand.int2ByteArray(data, 0, input);
		
		int output = ACommand.byteArray2Int(data, 0);
		
		assertEquals(input, output);
	}
	
	
	/** */
	@Test
	public void testUShort()
	{
		int input = 0xC0DE;
		byte[] data = new byte[2];
		
		ACommand.short2ByteArray(data, 0, input);
		
		int output = ACommand.byteArray2UShort(data, 0);
		
		assertEquals(input, output);
	}
	
	
	/** */
	@Test
	public void testShort()
	{
		short input = (short) 0xC0DE;
		byte[] data = new byte[2];
		
		ACommand.short2ByteArray(data, 0, input);
		
		int output = ACommand.byteArray2Short(data, 0);
		
		assertEquals(input, output);
	}
	
	
	/** */
	@Test
	public void testFloat()
	{
		float input = 0xC0DEBA5E;
		byte[] data = new byte[4];
		
		ACommand.float2ByteArray(data, 0, input);
		
		float output = ACommand.byteArray2Float(data, 0);
		
		assertEquals(input, output, Float.MIN_NORMAL);
	}
	
	
	/** */
	@Test
	public void testHalfFloat()
	{
		// float input = 0xC0DEBA5E;
		float input = 1.0f;
		byte[] data = new byte[2];
		
		ACommand.halfFloat2ByteArray(data, 0, input);
		
		float output = ACommand.byteArray2HalfFloat(data, 0);
		
		assertEquals(input, output, 0.0002f);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
