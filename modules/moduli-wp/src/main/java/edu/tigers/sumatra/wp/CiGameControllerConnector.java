/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.cam.SSLVisionCamGeometryTranslator;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.referee.proto.SslGcApi;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.thread.Watchdog;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.proto.SslGcCi;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64OutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * This connector connects to the CI interface of a local game-controller directly with a fast TCP connection.
 * It sends the current time and the tracker packets to the GC and receives the updated referee messages afterwards.
 */
@RequiredArgsConstructor
@Log4j2
public class CiGameControllerConnector
{
	private static final String HOSTNAME = "localhost";
	private final int port;
	private final Watchdog watchdog = new Watchdog(5000, this.getClass().getSimpleName(), this::onTimeout);
	private final Object lockSendRecv = new Object();
	private final Object lockSocket = new Object();
	private CountDownLatch latchInit = new CountDownLatch(1);
	private Socket socket;
	private SslGcCi.CiInput lastInput;
	private CamGeometry lastGeometry;
	private boolean connectionFailed = false;

	private final SSLVisionCamGeometryTranslator translator = new SSLVisionCamGeometryTranslator();
	private final TrackerPacketGenerator trackerPacketGenerator = new TrackerPacketGenerator("TIGERs");


	public void start()
	{
		log.trace("Starting");
		watchdog.start();
		initializeConnection();
		latchInit.countDown();
		log.trace("Started");
	}


	public void stop()
	{
		log.trace("Stopping");
		latchInit = new CountDownLatch(1);
		watchdog.stop();
		disconnect();
		log.trace("Stopped");
	}


	private void initializeConnection()
	{
		// Connect to the SSL-Game-Controller, wait until the connection is established
		connectBlocking();
		synchronized (lockSendRecv)
		{
			// Perform a first round for initialization
			if (send(0))
			{
				receiveRefereeMessages();
			}
		}
	}


	private void connectBlocking()
	{
		for (int i = 0; i < 50; i++)
		{
			if (watchdog.isStopped())
			{
				log.trace("Watchdog stopped, aborting connection");
				return;
			}
			// Allow the GC to come up
			ThreadUtil.parkNanosSafe(TimeUnit.MILLISECONDS.toNanos(200));
			try
			{
				connect();
				log.debug("Connected to SSL-Game-Controller");
				return;
			} catch (ConnectException e)
			{
				log.debug("Still connecting: {}", e.getMessage());
			} catch (IOException e)
			{
				log.warn("Connection to SSL-Game-Controller failed", e);
			}
		}
		log.error("Connection to SSL-Game-Controller failed repeatedly");
	}


	private void connect() throws IOException
	{
		log.trace("Connecting");
		synchronized (lockSocket)
		{
			try
			{
				socket = new Socket(HOSTNAME, port);
				socket.setTcpNoDelay(true);
				connectionFailed = false;
			} catch (IOException e)
			{
				connectionFailed = true;
				throw e;
			}
		}
		log.trace("Connected");
	}


	private void disconnect()
	{
		log.trace("Disconnect");
		synchronized (lockSocket)
		{
			if (socket != null)
			{
				log.trace("Closing socket");
				try
				{
					socket.close();
					socket = null;
				} catch (IOException e)
				{
					log.warn("Closing socket failed", e);
				}
			}
		}
		log.trace("Disconnected");
	}


	private boolean send(final long timestamp)
	{
		lastGeometry = Geometry.getLastCamGeometry();
		return send(SslGcCi.CiInput.newBuilder()
				.setTimestamp(timestamp)
				.setGeometry(translator.toProtobuf(lastGeometry))
				.build());
	}


	private boolean send(final SimpleWorldFrame swf,
			final List<SslGcApi.Input> inputs)
	{
		SslGcCi.CiInput.Builder builder = SslGcCi.CiInput.newBuilder()
				.setTimestamp(swf.getTimestamp())
				.setTrackerPacket(trackerPacketGenerator.generate(swf))
				.addAllApiInputs(inputs);
		if (!Geometry.getLastCamGeometry().equals(lastGeometry))
		{
			lastGeometry = Geometry.getLastCamGeometry();
			builder.setGeometry(translator.toProtobuf(lastGeometry));
		}
		return send(builder.build());
	}


	private boolean send(final SslGcCi.CiInput input)
	{
		synchronized (lockSocket)
		{
			if (socket == null)
			{
				return false;
			}
			try
			{
				lastInput = input;
				input.writeDelimitedTo(socket.getOutputStream());
				socket.getOutputStream().flush();
				watchdog.reset();
				watchdog.setActive(true);
				return true;
			} catch (SocketException e)
			{
				log.debug("Can not write to socket: {}", e.getMessage());
			} catch (IOException e)
			{
				log.warn("Could not write to socket", e);
			}
			return false;
		}
	}


	/**
	 * Receive a referee message from the controller
	 */
	private List<SslGcRefereeMessage.Referee> receiveRefereeMessages()
	{
		List<SslGcRefereeMessage.Referee> messages = new ArrayList<>();
		try
		{
			synchronized (lockSocket)
			{
				do
				{
					SslGcCi.CiOutput output = SslGcCi.CiOutput.parseDelimitedFrom(socket.getInputStream());
					if (output == null)
					{
						log.debug("Socket was at EOF, most likely the connection was closed");
					} else if (output.hasRefereeMsg())
					{
						messages.add(output.getRefereeMsg());
					}
				} while (socket.getInputStream().available() > 0);
			}
		} catch (SocketException e)
		{
			log.debug("Can not read from socket: {}", e.getMessage());
		} catch (IOException e)
		{
			log.warn("Receiving CI message from SSL-Game-Controller failed", e);
			disconnect();
		} finally
		{
			watchdog.setActive(false);
		}
		return messages;
	}


	public List<SslGcRefereeMessage.Referee> process(final SimpleWorldFrame swf, List<SslGcApi.Input> inputs)
	{
		try
		{
			boolean initialized = latchInit.await(10, TimeUnit.SECONDS);
			if (!initialized)
			{
				log.error("Timeout while waiting for connection to be initialized");
				return Collections.emptyList();
			}
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			log.warn("Interrupted while waiting for connection to be initialized", e);
			return Collections.emptyList();
		}

		synchronized (lockSocket)
		{
			if (socket == null)
			{
				boolean wasConnectionFailed = connectionFailed;
				try
				{
					connect();
				} catch (IOException e)
				{
					if (!wasConnectionFailed)
					{
						log.warn("Connection to SSL-Game-Controller failed", e);
					}
				}
			}
		}
		synchronized (lockSendRecv)
		{
			if (send(swf, inputs))
			{
				return receiveRefereeMessages();
			}
		}
		return Collections.emptyList();
	}


	private void onTimeout()
	{
		log.error("Sumatra to GC communication got stuck! Last input: {}", lastInput);
		if (lastInput != null)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Base64OutputStream stream = new Base64OutputStream(baos);
			try
			{
				lastInput.writeDelimitedTo(stream);
				String base64EncodedInput = baos.toString();
				log.error("Base64 encoded last input: {}", base64EncodedInput);
			} catch (IOException e)
			{
				log.error("Could not convert last input to base64", e);
			}
		}
		disconnect();
	}
}
