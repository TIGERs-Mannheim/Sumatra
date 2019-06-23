/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.10.2016
 * Author(s): Sebastian Stein <sebastian-stein@gmx.de>
 * *********************************************************
 */
package edu.tigers.sumatra.telegram;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;


/**
 * Provides asynchronous message handling
 * 
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class NotificationWorker extends Thread
{
	
	private static NotificationWorker instance = null;
	private static final Logger log = Logger
			.getLogger(NotificationWorker.class.getName());
	
	@Configurable(comment = "The server bridge hostname", defValue = "172.16.1.1")
	private static String serverHostname = "tigers-mannheim.de";
	
	@Configurable(comment = "The server bridge port", defValue = "4434")
	private static int serverPort = 4434;
	
	// This is only a default value, not the real password
	@SuppressWarnings("squid:S2068")
	@Configurable(comment = "The server bridge password", defValue = "SecretPw")
	private static String serverPassword = "SecretPw";
	
	private final List<String> messageQueue = new ArrayList<>();
	
	
	private boolean running = true;
	
	
	static
	{
		ConfigRegistration.registerClass("telegram", NotificationWorker.class);
	}
	
	
	private NotificationWorker()
	{
		
		super("NotificationWorker");
	}
	
	
	/**
	 * Destroys the currently loaded instance.
	 */
	public static synchronized void destroyInstance()
	{
		
		instance = null;
	}
	
	
	/**
	 * Returns the currently loaded instance
	 * 
	 * @return The current instance
	 */
	public static synchronized NotificationWorker getInstance()
	{
		
		if (instance == null)
		{
			instance = new NotificationWorker();
		}
		
		return instance;
	}
	
	
	/**
	 * Adds a message to the internal queue
	 * 
	 * @param message
	 */
	public synchronized void addMessage(final String message)
	{
		
		messageQueue.add(message);
	}
	
	
	private void processQueue()
	{
		if (!messageQueue.isEmpty())
		{
			List<String> queue;
			synchronized (messageQueue)
			{
				queue = new ArrayList<>(messageQueue);
			}
			
			for (String message : queue)
			{
				
				sendMessageToSubscribers(message);
				
				synchronized (messageQueue)
				{
					messageQueue.remove(message);
				}
			}
		}
	}
	
	
	@Override
	public void run()
	{
		super.run();
		
		while (running)
		{
			
			processQueue();
			
			try
			{
				Thread.sleep(500);
			} catch (InterruptedException e)
			{
				log.error("", e);
				Thread.currentThread().interrupt();
			}
		}
		
		// Reset running state to make thread runnable again
		running = true;
	}
	
	
	/**
	 * Schedules the worker thread to stop
	 */
	public void scheduleStop()
	{
		running = false;
	}
	
	
	private void sendMessageToSubscribers(final String message)
	{
		
		try (
				Socket socket = new Socket(serverHostname, serverPort);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true))
		{
			
			out.println(serverPassword);
			out.println(message);
			
		} catch (UnknownHostException e)
		{
			log.error("Unknown server bridge host (" + serverHostname + " : " + serverPort + ")", e);
		} catch (IOException e)
		{
			log.error("Not able to connect to server bridge", e);
		}
		
	}
	
}
