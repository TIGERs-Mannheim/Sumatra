/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2011
 * Author(s): Yakisoba
 * *********************************************************
 */
package edu.tigers.sumatra.cam;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.network.IReceiver;


/**
 * Receiver: If Vision Data is recorded this class playbacks the record.
 * 
 * @author MarenK
 */
public class FileReceiver implements IReceiver
{
	// Logger
	private static final Logger		log	= Logger.getLogger(FileReceiver.class.getName());
	
	private final FileInputStream		fis;
	private final ObjectInputStream	in;
	private SSLVisionData				object;
	
	
	/**
	 * @param filename
	 * @throws IOException
	 */
	public FileReceiver(final String filename) throws IOException
	{
		fis = new FileInputStream(filename);
		in = new ObjectInputStream(fis);
		final byte[] data = new byte[0];
		object = new SSLVisionData(0, data);
	}
	
	
	@Override
	public void cleanup() throws IOException
	{
		object = null;
		fis.close();
		in.close();
	}
	
	
	@Override
	public DatagramPacket receive(final DatagramPacket arg0) throws IOException
	{
		final long oldTimestamp = object.getTimestamp();
		
		try
		{
			object = (SSLVisionData) in.readObject();
		} catch (final ClassNotFoundException err)
		{
			log.fatal("ClassNotFoundException", err);
		}
		arg0.setData(object.getData());
		
		ThreadUtil.parkNanosSafe(object.getTimestamp() - oldTimestamp);
		return arg0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public boolean isReady()
	{
		return true;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
