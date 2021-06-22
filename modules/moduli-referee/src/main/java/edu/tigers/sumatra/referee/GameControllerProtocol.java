/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * Main Class to interact with the new (2019) SSL GameController Protocol
 * The communication is based on protobuf and features secure messages. To do so,
 * messages sent TO the server contain a secret token and a signature.
 * For more details about this protocol, visit the Repository of the official SSL-Game-Controller:
 * https://github.com/RoboCup-SSL/ssl-game-controller
 */
@Log4j2
public class GameControllerProtocol
{
	@Setter
	private String hostname;
	private Socket socket;
	private int port;

	private boolean connected = false;
	private List<IConnectedHandler> connectHandlerList = new ArrayList<>();


	/**
	 * Constructor
	 *
	 * @param hostname The hostname of the GameController
	 * @param port The Port of the GameController
	 */
	public GameControllerProtocol(String hostname, int port)
	{
		this.hostname = hostname;
		this.port = port;
	}


	private void notifyOnConnect()
	{
		for (IConnectedHandler handler : connectHandlerList)
		{
			handler.onConnect();
		}
	}


	/**
	 * Repeats connect until it succeeds
	 * Will call the onConnected callbacks and will close old sockets if it was already connected (aka reconnect)
	 */
	public void connectBlocking()
	{
		if (isConnected())
		{
			disconnect();
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}
			log.info("Reconnecting to " + hostname + ":" + port);
		} else
		{
			log.info("Connecting to " + hostname + ":" + port);
		}

		long start = System.nanoTime();
		while (true)
		{
			if (connect())
			{
				long time = System.nanoTime() - start;
				time /= 1_000_000; // Convert to ms
				log.info("successfully connect to " + hostname + ":" + port + " after " + time + "ms");
				return;
			}

			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e2)
			{
				Thread.currentThread().interrupt();
				// thread was probably interrupted to be shutdown, so exit endless loop here
				return;
			}
		}
	}


	private boolean connect()
	{
		try
		{
			socket = new Socket(hostname, port);
			socket.setTcpNoDelay(true);
			connected = true;
			notifyOnConnect();
			return true;
		} catch (IOException e)
		{
			log.warn("Connection to SSL-Game-Controller ({}:{}) failed", hostname, port, e);
		}
		return false;
	}


	/**
	 * Disconnect from the SSL-Game-Controller
	 *
	 * @return true on success else false
	 */
	public void disconnect()
	{
		try
		{
			if (connected)
			{
				socket.close();
			}
		} catch (IOException e)
		{
			log.warn("Closing Socket failed", e);
		}

		socket = null;
		connected = false;
	}


	/**
	 * Sends a Protobuf message to the controller
	 * Before you pass a message to this function make sure that:
	 * a) You set the token received with the last message
	 * b) You signed the message using the MessageSigner class
	 *
	 * @param msg The Protobuf message
	 * @return true on success else false
	 */
	public boolean sendMessage(Message msg)
	{
		try
		{
			msg.writeDelimitedTo(socket.getOutputStream());
			socket.getOutputStream().flush();
		} catch (IOException e)
		{
			log.warn("Sending message to ssl-game-controller failed", e);
			connectBlocking();
			return false;
		}
		return true;
	}


	/**
	 * Receive a message from the controller
	 * This generic method accepts the type of the protobuf message as template
	 * parameter.
	 *
	 * @param parser Protobuf-Parser for message type T
	 * @param <T> Type of Protobuf Message
	 * @return Parsed Message of type T or null if an error occurred
	 */
	public <T extends Message> T receiveMessage(Parser<T> parser)
	{
		try
		{
			T message = parser.parseDelimitedFrom(socket.getInputStream());
			if (message != null)
			{
				return message;
			}
			log.info("Connection to game controller closed");
		} catch (IOException e)
		{
			log.warn("Receiving message from SSL-Game-Controller failed", e);
		}
		connectBlocking();
		return null;
	}


	public boolean newMessageAvailable()
	{
		try
		{
			return socket.getInputStream().available() != 0;
		} catch (IOException e)
		{
			log.warn("Error checking for new Message", e);
			return false;
		}
	}


	public void addConnectedHandler(IConnectedHandler cb)
	{
		connectHandlerList.add(cb);
	}


	public boolean isConnected()
	{
		return connected;
	}

	@FunctionalInterface
	public interface IConnectedHandler
	{
		void onConnect();
	}
}
