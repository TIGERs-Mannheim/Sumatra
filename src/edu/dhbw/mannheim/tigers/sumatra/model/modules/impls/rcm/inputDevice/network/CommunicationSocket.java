/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.05.2012
 * Author(s): Manuel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;


/**
 * 
 * @author Manuel
 * 
 */
public class CommunicationSocket
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(CommunicationSocket.class.getName());
	
	private final Socket				socket;
	private ObjectInputStream		inputStream;
	private ObjectOutputStream		outputStream;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param socket
	 */
	public CommunicationSocket(Socket socket)
	{
		this.socket = socket;
		try
		{
			inputStream = new ObjectInputStream(this.socket.getInputStream());
			outputStream = new ObjectOutputStream(this.socket.getOutputStream());
		} catch (final IOException e)
		{
			log.error("Communication Error: " + e.getMessage());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public void closeSocketConnection()
	{
		try
		{
			inputStream.close();
			outputStream.close();
			socket.close();
		} catch (final IOException e)
		{
			log.error("Close Communication Error: " + e.getMessage());
		} catch (final NullPointerException e)
		{
			log.error("Close Communication Error: " + e.getMessage());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public Socket getSocket()
	{
		return socket;
	}
	
	
	/**
	 * @return
	 */
	public ObjectInputStream getInputStream()
	{
		return inputStream;
	}
	
	
	/**
	 * @return
	 */
	public ObjectOutputStream getOutputStream()
	{
		return outputStream;
	}
}
