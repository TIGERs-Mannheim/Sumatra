/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2011
 * Author(s): Yakisoba
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

import edu.dhbw.mannheim.tigers.sim.util.network.IReceiver;
import edu.dhbw.mannheim.tigers.sumatra.util.ThreadUtil;


/**
 * Receiver: If Vision Data is recorded this class playbacks the record.
 * @author MarenK
 * 
 */
public class FileReceiver implements IReceiver
{
	
	private FileInputStream		fis;
	private ObjectInputStream	in;
	private SSLVisionData		object;
	
	
	public FileReceiver(String filename) throws IOException
	{
		fis = new FileInputStream(filename);
		in = new ObjectInputStream(fis);
		object = new SSLVisionData(0, null);
	}
	

	@Override
	public void cleanup() throws IOException
	{
		// TODO Auto-generated method stub
		object = null;
		fis.close();
		in.close();
	}
	

	@Override
	public InetAddress getLocalAddress()
	{
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public int getLocalPort()
	{
		// TODO Auto-generated method stub
		return 0;
	}
	

	@Override
	public boolean isReady()
	{
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public DatagramPacket receive(DatagramPacket arg0) throws IOException
	{
		long oldTimestamp = object.getTimestamp();
		
		try
		{
			object = (SSLVisionData) in.readObject();
		} catch (ClassNotFoundException err)
		{
			// TODO Auto-generated catch block
			err.printStackTrace();
		}
		arg0.setData(object.getData());
		
		ThreadUtil.parkNanosSafe(object.getTimestamp() - oldTimestamp);
		return arg0;
	}
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	

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
