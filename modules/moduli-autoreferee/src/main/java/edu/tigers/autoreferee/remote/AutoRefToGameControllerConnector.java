/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.remote;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import com.google.protobuf.ByteString;

import edu.tigers.sumatra.SslGameControllerAutoRef;
import edu.tigers.sumatra.SslGameControllerCommon;
import edu.tigers.sumatra.referee.GameControllerProtocol;
import edu.tigers.sumatra.referee.MessageSigner;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.thread.NamedThreadFactory;


/**
 * Connector to game controller
 */
public class AutoRefToGameControllerConnector implements Runnable
{
	private static final Logger log = Logger.getLogger(AutoRefToGameControllerConnector.class);
	private static final String AUTO_REF_ID = "TIGERs AutoRef";
	
	private GameControllerProtocol protocol;
	private ExecutorService executorService;
	
	private LinkedBlockingDeque<QueueEntry> commandQueue;
	
	private List<IGameEventResponseObserver> responseObserverList = new ArrayList<>();
	
	private String nextToken;
	private MessageSigner signer;
	
	
	public AutoRefToGameControllerConnector(final String hostname, final int port)
	{
		protocol = new GameControllerProtocol(hostname, port);
		protocol.addConnectedHandler(this::register);
		
		commandQueue = new LinkedBlockingDeque<>();
		try
		{
			signer = new MessageSigner(
					IOUtils.resourceToString("/edu/tigers/autoreferee/remote/TIGERs-Mannheim-autoRef.key.pem.pkcs8",
							Charset.forName("UTF-8")),
					IOUtils.resourceToString("/edu/tigers/autoreferee/remote/TIGERs-Mannheim-autoRef.pub.pem",
							Charset.forName("UTF-8")));
		} catch (IOException e)
		{
			log.error("Could not read certificates from classpath", e);
			signer = new MessageSigner();
		}
	}
	
	
	private void register()
	{
		log.debug("Starting registering");
		SslGameControllerAutoRef.ControllerToAutoRef reply;
		reply = protocol.receiveMessage(SslGameControllerAutoRef.ControllerToAutoRef.parser());
		if (reply == null || !reply.hasControllerReply())
		{
			log.error("Receiving initial Message failed");
			return;
		}
		
		nextToken = reply.getControllerReply().getNextToken();
		
		SslGameControllerAutoRef.AutoRefRegistration.Builder registration = SslGameControllerAutoRef.AutoRefRegistration
				.newBuilder()
				.setIdentifier(AUTO_REF_ID);
		registration.getSignatureBuilder().setToken(nextToken).setPkcs1V15(ByteString.EMPTY);
		byte[] signature = signer.sign(registration.build().toByteArray());
		registration.getSignatureBuilder().setPkcs1V15(ByteString.copyFrom(signature));
		
		protocol.sendMessage(registration.build());
		
		reply = protocol.receiveMessage(SslGameControllerAutoRef.ControllerToAutoRef.parser());
		if (reply == null)
		{
			log.error("Receiving AutoRefRegistration reply failed");
		} else if (reply.getControllerReply().getStatusCode() != SslGameControllerCommon.ControllerReply.StatusCode.OK)
		{
			log.error("Server did not allow registration: " + reply.getControllerReply().getStatusCode() + " - "
					+ reply.getControllerReply().getReason());
		} else
		{
			log.info("Successfully registered AutoRef");
			nextToken = reply.getControllerReply().getNextToken();
		}
	}
	
	
	/**
	 * Connect to the refbox via the specified hostname and port
	 * 
	 * @throws IOException
	 */
	public void start()
	{
		log.debug("Starting connector");
		executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("AutoRefToGameControllerConnector"));
		executorService.execute(this);
	}
	
	
	public void stop()
	{
		log.debug("Stopping connector");
		executorService.shutdownNow();
		try
		{
			Validate.isTrue(executorService.awaitTermination(2, TimeUnit.SECONDS));
		} catch (InterruptedException e)
		{
			log.warn("Interrupted while waiting for termination", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	public void sendEvent(final IGameEvent event)
	{
		QueueEntry entry = new QueueEntry(event);
		commandQueue.add(entry);
	}
	
	
	@Override
	public void run()
	{
		log.debug("Started connector");
		protocol.connectBlocking();
		log.debug("Connected");
		while (!executorService.isShutdown())
		{
			try
			{
				readWriteLoop();
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			} catch (Exception e)
			{
				log.error("Uncaught exception in autoRef -> game-controller connector", e);
			}
		}
		protocol.disconnect();
		log.debug("Stopped connector");
	}
	
	
	private void readWriteLoop() throws InterruptedException
	{
		QueueEntry entry = commandQueue.take();
		SslGameControllerAutoRef.AutoRefToController.Builder req = SslGameControllerAutoRef.AutoRefToController
				.newBuilder();
		req.setGameEvent(entry.getEvent().toProtobuf());
		
		if (nextToken != null)
		{
			req.getSignatureBuilder().setToken(nextToken).setPkcs1V15(ByteString.EMPTY);
			byte[] signature = signer.sign(req.build().toByteArray());
			req.getSignatureBuilder().setPkcs1V15(ByteString.copyFrom(signature));
		}
		
		if (!protocol.sendMessage(req.build()))
		{
			
			log.info(String.format("Put game event '%s' back into queue after lost connection", entry.getEvent()));
			commandQueue.addFirst(entry);
			return;
		}
		SslGameControllerAutoRef.ControllerToAutoRef reply = protocol
				.receiveMessage(SslGameControllerAutoRef.ControllerToAutoRef.parser());
		if (reply == null || !reply.hasControllerReply())
		{
			log.error("Receiving GameController Reply failed");
		} else if (reply.getControllerReply()
				.getStatusCode() != SslGameControllerCommon.ControllerReply.StatusCode.OK)
		{
			log.warn(
					"Remote control rejected command " + entry.getEvent() + " with outcome "
							+ reply.getControllerReply().getStatusCode());
		}
		
		if (reply != null)
		{
			responseObserverList.forEach(a -> a.notify(new GameEventResponse(reply.getControllerReply())));
			nextToken = reply.getControllerReply().getNextToken();
		}
	}
	
	
	public void addGameEventResponseObserver(IGameEventResponseObserver observer)
	{
		this.responseObserverList.add(observer);
	}
	
	private static class QueueEntry
	{
		private final IGameEvent event;
		
		
		public QueueEntry(final IGameEvent event)
		{
			this.event = event;
		}
		
		
		/**
		 * @return the cmd
		 */
		public IGameEvent getEvent()
		{
			return event;
		}
	}
	
	@FunctionalInterface
	public interface IGameEventResponseObserver
	{
		void notify(GameEventResponse response);
	}
}
