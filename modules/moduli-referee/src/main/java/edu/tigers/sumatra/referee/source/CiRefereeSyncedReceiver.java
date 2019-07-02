package edu.tigers.sumatra.referee.source;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.google.protobuf.CodedOutputStream;

import edu.tigers.sumatra.Referee;


/**
 * This referee source connections to a local game-controller directly with a fast TCP connection.
 * It sends the current time to the GC and receives the updated referee messages afterwards.
 */
public class CiRefereeSyncedReceiver extends ARefereeMessageSource
{
	private static final Logger log = Logger.getLogger(CiRefereeSyncedReceiver.class);
	
	private static final String HOSTNAME = "localhost";
	private int port = 10009;
	private Socket socket;
	
	
	public CiRefereeSyncedReceiver()
	{
		super(ERefereeMessageSource.CI);
	}
	
	
	@Override
	public void start()
	{
		super.start();
		
		connect();
	}
	
	
	@Override
	public void stop()
	{
		super.stop();
		
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
	}
	
	
	private void connect()
	{
		try
		{
			socket = new Socket(HOSTNAME, port);
			socket.setTcpNoDelay(true);
		} catch (IOException e)
		{
			log.warn("Connection to SSL-Game-Controller Failed", e);
		}
	}
	
	
	private void sendTime(long timestamp)
	{
		try
		{
			final CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(socket.getOutputStream());
			codedOutputStream.writeSInt64NoTag(timestamp);
			codedOutputStream.flush();
		} catch (IOException e)
		{
			log.warn("Could not write to socket", e);
		}
	}
	
	
	/**
	 * Receive a referee message from the controller
	 *
	 * @return read and parsed referee message
	 */
	private Referee.SSL_Referee receiveMessage()
	{
		try
		{
			Referee.SSL_Referee msg = Referee.SSL_Referee.parseDelimitedFrom(socket.getInputStream());
			if (msg == null)
			{
				log.warn("Receiving Message failed: Socket was at EOF, most likely the connection was closed");
			}
			return msg;
		} catch (IOException e)
		{
			log.warn("Receiving message from SSL-Game-Controller failed", e);
			return null;
		}
	}
	
	
	@Override
	public void setCurrentTime(long timestamp)
	{
		if (socket != null)
		{
			sendTime(timestamp);
			final Referee.SSL_Referee msg = receiveMessage();
			if (msg == null)
			{
				stop();
			} else
			{
				notifyNewRefereeMessage(msg);
			}
		}
	}
	
	
	public void setPort(final int port)
	{
		this.port = port;
	}
}
