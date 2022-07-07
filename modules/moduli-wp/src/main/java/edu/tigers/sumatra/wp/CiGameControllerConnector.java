/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
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
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	private Socket socket;
	private Watchdog watchdog;
	private SslGcCi.CiInput lastInput;
	private CamGeometry lastGeometry;

	private final SSLVisionCamGeometryTranslator translator = new SSLVisionCamGeometryTranslator();
	private final TrackerPacketGenerator trackerPacketGenerator = new TrackerPacketGenerator("TIGERs");


	public synchronized void start() throws IOException
	{
		log.trace("Starting");
		watchdog = new Watchdog(5000, this.getClass().getSimpleName(), this::onTimeout);
		watchdog.start();
		connect();
		// Initialize connection by performing one roundtrip
		send(0);
		receiveRefereeMessages();
		log.trace("Started");
	}


	public synchronized void stop()
	{
		log.trace("Stopping");
		watchdog.stop();
		disconnect();
		log.trace("Stopped");
	}


	private void connect() throws IOException
	{
		log.trace("Connecting");

		for (int i = 0; i < 10; i++)
		{
			// Allow the GC to come up
			ThreadUtil.parkNanosSafe(TimeUnit.MILLISECONDS.toNanos(200));
			try
			{
				socket = new Socket(HOSTNAME, port);
				socket.setTcpNoDelay(true);
				log.debug("Connected");
				return;
			} catch (IOException e)
			{
				log.debug("Connection to SSL-Game-Controller failed", e);
			}
		}
		throw new IOException("Connection to SSL-Game-Controller failed repeatedly");
	}


	private void disconnect()
	{
		log.trace("Disconnect");
		if (socket != null)
		{
			try
			{
				socket.close();
				socket = null;
			} catch (IOException e)
			{
				log.warn("Closing socket failed", e);
			}
		}
		log.trace("Disconnected");
	}


	private void send(final long timestamp)
	{
		lastGeometry = Geometry.getLastCamGeometry();
		send(SslGcCi.CiInput.newBuilder()
				.setTimestamp(timestamp)
				.setGeometry(translator.toProtobuf(lastGeometry))
				.build());
	}


	private void send(final SimpleWorldFrame swf,
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
		send(builder.build());
	}


	private void send(final SslGcCi.CiInput input)
	{
		if (socket == null)
		{
			return;
		}
		try
		{
			lastInput = input;
			input.writeDelimitedTo(socket.getOutputStream());
			socket.getOutputStream().flush();
			watchdog.reset();
			watchdog.setActive(true);
		} catch (IOException e)
		{
			log.warn("Could not write to socket", e);
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
			do
			{
				SslGcCi.CiOutput output = SslGcCi.CiOutput.parseDelimitedFrom(socket.getInputStream());
				if (output == null)
				{
					throw new IOException(
							"Receiving Message failed: Socket was at EOF, most likely the connection was closed");
				}

				if (output.hasRefereeMsg())
				{
					messages.add(output.getRefereeMsg());
				}
			} while (socket.getInputStream().available() > 0);
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


	public synchronized List<SslGcRefereeMessage.Referee> process(final SimpleWorldFrame swf,
			List<SslGcApi.Input> inputs)
	{
		if (socket == null)
		{
			try
			{
				connect();
			} catch (IOException e)
			{
				log.warn("Failed to reconnect", e);
			}
		}
		if (socket == null)
		{
			return Collections.emptyList();
		}

		send(swf, inputs);
		return receiveRefereeMessages();
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
