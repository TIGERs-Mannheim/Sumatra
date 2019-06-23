/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 9, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.remote.impl;

import static edu.tigers.autoreferee.generic.CheckedRunnable.execAndCatchAll;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.remote.ICommandResult;
import edu.tigers.autoreferee.remote.IRefboxRemote;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlReply;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlReply.Outcome;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;


/**
 * @author "Lukas Magel"
 */
public class ThreadedTCPRefboxRemote implements IRefboxRemote, Runnable
{
	private static final Logger					log	= Logger.getLogger(ThreadedTCPRefboxRemote.class);
	
	private final String								hostname;
	private final int									port;
	
	private Thread										thread;
	private RefboxRemoteSocket						socket;
	
	private LinkedBlockingDeque<QueueEntry>	commandQueue;
	
	
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
		thread.start();
	}
	
	
	/**
	 * 
	 */
	@Override
	public synchronized void stop()
	{
		try
		{
			thread.interrupt();
			thread.join();
		} catch (InterruptedException e)
		{
			log.warn("Error while joining the sending thread", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	private synchronized void reconnect() throws IOException, InterruptedException
	{
		if (Thread.interrupted())
		{
			throw new InterruptedException();
		}
		socket.close();
		socket.connect(hostname, port);
	}
	
	
	@Override
	public ICommandResult sendCommand(final RefboxRemoteCommand command)
	{
		QueueEntry entry = new QueueEntry(command);
		try
		{
			commandQueue.put(entry);
		} catch (InterruptedException e)
		{
			log.error("", e);
			Thread.currentThread().interrupt();
		}
		return entry.getResult();
	}
	
	
	@Override
	public void run()
	{
		try
		{
			while (!Thread.interrupted())
			{
				try
				{
					readWriteLoop();
				} catch (IOException e)
				{
					/*
					 * Try to reconnect to the refbox.
					 * Fail with an error if the connection cannot be reestablished.
					 */
					try
					{
						log.debug("Reconnecting to refbox after IO error", e);
						reconnect();
					} catch (IOException ex)
					{
						log.error("Unable to reconnect to the refbox", ex);
						break;
					}
				}
			}
		} catch (InterruptedException e)
		{
			log.debug("Interrupted", e);
			Thread.currentThread().interrupt();
		}
		
		socket.close();
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
	private void readWriteLoop() throws InterruptedException, IOException
	{
		RemoteControlProtobufBuilder pbBuilder = new RemoteControlProtobufBuilder();
		
		while (true)
		{
			if (Thread.interrupted())
			{
				throw new InterruptedException();
			}
			
			QueueEntry entry = null;
			try
			{
				entry = commandQueue.take();
				SSL_RefereeRemoteControlRequest request = pbBuilder.buildRequest(entry.getCmd());
				SSL_RefereeRemoteControlReply reply = socket.sendRequest(request);
				
				if (reply.getOutcome() != Outcome.OK)
				{
					entry.getResult().setFailed();
				} else
				{
					entry.getResult().setSuccessful();
				}
			} catch (Exception e)
			{
				/*
				 * Put the entry back into the queue if an error occurs in case the connection can be reestablished.
				 */
				if (entry != null)
				{
					QueueEntry lambdaEntry = entry;
					execAndCatchAll(() -> commandQueue.putFirst(lambdaEntry));
				}
				throw e;
			}
		}
	}
	
	private static class QueueEntry
	{
		private final RefboxRemoteCommand cmd;
		private final CommandResultImpl	result;
		
		
		public QueueEntry(final RefboxRemoteCommand cmd)
		{
			this(cmd, new CommandResultImpl());
		}
		
		
		public QueueEntry(final RefboxRemoteCommand cmd, final CommandResultImpl result)
		{
			this.cmd = cmd;
			this.result = result;
		}
		
		
		/**
		 * @return the cmd
		 */
		public RefboxRemoteCommand getCmd()
		{
			return cmd;
		}
		
		
		/**
		 * @return the result
		 */
		public CommandResultImpl getResult()
		{
			return result;
		}
	}
}
