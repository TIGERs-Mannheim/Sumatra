/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.remote.impl;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.remote.IRefboxRemote;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlReply;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlReply.Outcome;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;


/**
 * @author "Lukas Magel"
 */
public class ThreadedTCPRefboxRemote implements IRefboxRemote, Runnable
{
	private static final Logger log = Logger.getLogger(ThreadedTCPRefboxRemote.class);
	
	private final String hostname;
	private final int port;
	
	private Thread thread;
	private RefboxRemoteSocket socket;
	private boolean running = false;
	private CountDownLatch terminationLatch = null;
	
	private LinkedBlockingDeque<QueueEntry> commandQueue;
	
	
	/**
	 * @param hostname the hostname
	 * @param port
	 */
	public ThreadedTCPRefboxRemote(final String hostname, final int port)
	{
		this.hostname = hostname;
		this.port = port;
		
		socket = new RefboxRemoteSocket();
		commandQueue = new LinkedBlockingDeque<>();
		thread = new Thread(this, "RefboxRemoteSenderThread");
	}
	
	
	/**
	 * Connect to the refbox via the specified hostname and port
	 * 
	 * @throws IOException
	 */
	public synchronized void start() throws IOException
	{
		try
		{
			socket.connect(hostname, port);
		} catch (IOException e)
		{
			throw new IOException("Unable to connect to the Refbox: " + e.getMessage(), e);
		}
		running = true;
		thread.start();
		log.info("Connected to refbox at " + hostname + ":" + port);
	}
	
	
	@Override
	public synchronized void stop()
	{
		terminationLatch = new CountDownLatch(1);
		running = false;
		thread.interrupt();
		try
		{
			Validate.isTrue(terminationLatch.await(1, TimeUnit.SECONDS));
		} catch (InterruptedException e)
		{
			log.warn("Interrupted while waiting for termination", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	private synchronized void reconnect() throws IOException
	{
		socket.close();
		socket.connect(hostname, port);
	}
	
	
	@Override
	public void sendCommand(final RefboxRemoteCommand command)
	{
		QueueEntry entry = new QueueEntry(command);
		commandQueue.add(entry);
	}
	
	
	@Override
	public void run()
	{
		while (running)
		{
			try
			{
				readWriteLoop();
			} catch (Exception e)
			{
				/*
				 * Try to reconnect to the refbox.
				 * Fail with an error if the connection cannot be reestablished.
				 */
				try
				{
					log.info("Reconnecting to refbox after error", e);
					reconnect();
				} catch (IOException ex)
				{
					log.error("Unable to reconnect to the refbox", ex);
					break;
				}
			}
		}
		
		socket.close();
		
		if (terminationLatch != null)
		{
			terminationLatch.countDown();
		}
	}
	
	
	/**
	 * This method runs in an infinite loop and sends commands to the refbox using the socket. The only way it ends is
	 * through an IOException or an external interrupt. Before a command is delivered to the refbox it is taken from the
	 * queue. In case an error occurs before the command has been successfully delivered to the refbox the command is
	 * readded to front of the queue to avoid lost commands.
	 * 
	 * @throws InterruptedException
	 * @throws InvalidProtocolBufferException
	 * @throws IOException
	 */
	private void readWriteLoop() throws IOException
	{
		RemoteControlProtobufBuilder pbBuilder = new RemoteControlProtobufBuilder();
		
		while (running)
		{
			QueueEntry entry = null;
			try
			{
				entry = commandQueue.take();
				SSL_RefereeRemoteControlRequest request = pbBuilder.buildRequest(entry.getCmd());
				SSL_RefereeRemoteControlReply reply = socket.sendRequest(request);
				
				if (reply.getOutcome() != Outcome.OK)
				{
					log.warn("Remote control rejected command " + entry.getCmd() + " with outcome " + reply.getOutcome());
				}
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			} catch (IOException e)
			{
				/*
				 * Put the entry back into the queue if an error occurs in case the connection can be reestablished.
				 */
				if (entry.retries < 5)
				{
					entry.retries++;
					QueueEntry lambdaEntry = entry;
					try
					{
						commandQueue.putFirst(lambdaEntry);
					} catch (InterruptedException e1)
					{
						Thread.currentThread().interrupt();
					}
				}
				throw e;
			}
		}
	}
	
	private static class QueueEntry
	{
		private final RefboxRemoteCommand cmd;
		private int retries = 0;
		
		
		public QueueEntry(final RefboxRemoteCommand cmd)
		{
			this.cmd = cmd;
		}
		
		
		/**
		 * @return the cmd
		 */
		public RefboxRemoteCommand getCmd()
		{
			return cmd;
		}
	}
}
