/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.ci;

import edu.tigers.autoreferee.proto.SslAutorefCi.AutoRefCiInput;
import edu.tigers.autoreferee.proto.SslAutorefCi.AutoRefCiOutput;
import edu.tigers.sumatra.cam.proto.SslVisionDetection;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.util.Safe;
import edu.tigers.sumatra.wp.proto.SslVisionWrapperTracked;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;


@Log4j2
@RequiredArgsConstructor
public class AutoRefereeCiServer
{
	private final Consumer<SslVisionDetection.SSL_DetectionFrame> detectionFrameConsumer;
	private final Consumer<SslVisionGeometry.SSL_GeometryData> geometryDataConsumer;
	private final Consumer<SslGcRefereeMessage.Referee> refereeConsumer;
	private final Consumer<SslVisionWrapperTracked.TrackerWrapperPacket> trackerWrapperPacketConsumer;

	@Setter
	private int port;
	private Thread thread;
	private boolean running;
	private ServerSocket serverSocket;
	private Socket currentSocket;


	public void start()
	{
		if (running)
		{
			throw new IllegalStateException("Server is already running");
		}
		running = true;
		try
		{
			serverSocket = new ServerSocket(port);
		} catch (IOException e)
		{
			log.error("Could not listen on port " + port, e);
			return;
		}
		thread = new Thread(() -> Safe.run(this::listen));
		thread.setName("AutoRef CI Server");
		thread.start();
	}


	public void stop()
	{
		if (!running)
		{
			throw new IllegalStateException("Server is already stopped");
		}
		running = false;
		try
		{
			serverSocket.close();
		} catch (IOException e)
		{
			log.warn("Failed to close server socket", e);
		}

		thread.interrupt();
		thread = null;
		serverSocket = null;
	}


	public void publish(SslVisionWrapperTracked.TrackerWrapperPacket trackerWrapperPacket)
	{
		Socket socket = currentSocket;
		if (socket == null)
		{
			return;
		}

		AutoRefCiOutput autoRefCiOutput = AutoRefCiOutput.newBuilder()
				.setTrackerWrapperPacket(trackerWrapperPacket)
				.build();
		try
		{
			autoRefCiOutput.writeDelimitedTo(socket.getOutputStream());
		} catch (IOException e)
		{
			log.warn("Failed to publish tracker wrapper packet", e);
		}
	}


	private void listen()
	{
		while (running)
		{
			try
			{
				currentSocket = accept();
				while (running)
				{
					if (!consume(currentSocket))
					{
						break;
					}
				}
			} catch (IOException e)
			{
				log.warn("Connection failed", e);
			}
			currentSocket = null;
		}
	}


	private boolean consume(Socket socket) throws IOException
	{
		if (socket == null)
		{
			return false;
		}
		AutoRefCiInput autoRefCiInput = AutoRefCiInput.parseDelimitedFrom(socket.getInputStream());
		if (autoRefCiInput == null)
		{
			return false;
		}
		if (autoRefCiInput.hasGeometry())
		{
			geometryDataConsumer.accept(autoRefCiInput.getGeometry());
		}
		autoRefCiInput.getDetectionList().forEach(detectionFrameConsumer);
		if (autoRefCiInput.hasRefereeMessage())
		{
			refereeConsumer.accept(autoRefCiInput.getRefereeMessage());
		}
		if (autoRefCiInput.hasTrackerWrapperPacket())
		{
			trackerWrapperPacketConsumer.accept(autoRefCiInput.getTrackerWrapperPacket());
		}
		return true;
	}


	private Socket accept() throws IOException
	{
		Socket socket = serverSocket.accept();
		socket.setTcpNoDelay(true);
		return socket;
	}
}
