/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gamelog;

import com.google.protobuf.AbstractMessage;
import edu.tigers.sumatra.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.Referee.SSL_Referee;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Reads a logfile containing SSL-Vision and referee commands.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class SSLGameLogReader
{
	private static final Logger			log						= Logger.getLogger(SSLGameLogReader.class.getName());
	
	private String								headerString			= "";
	private int									versionNumber			= 0;
	
	private List<SSLGameLogfileEntry>	packets					= new ArrayList<>();
	
	private ISSLGameLogfileObserver		loadCompleteObserver	= null;
	
	
	/**
	 * Load a logfile asynchronously.
	 *
	 * @param path
	 */
	public void loadFile(final String path)
	{
		new Thread(() -> loadFileBlocking(path), "Loader").start();
	}
	
	
	/**
	 * Load a logfile asynchronously and get notified when its done.
	 *
	 * @param path
	 * @param obs
	 */
	public void loadFile(final String path, final ISSLGameLogfileObserver obs)
	{
		loadCompleteObserver = obs;
		
		loadFile(path);
	}
	
	
	/**
	 * Load a logfile synchronously.
	 * 
	 * @param path
	 */
	public void loadFileBlocking(final String path)
	{
		try (FileInputStream fileInStream = new FileInputStream(path))
		{
			DataInputStream fileStream = new DataInputStream(fileInStream);
			
			byte[] header = new byte[12];
			fileStream.readFully(header);
			headerString = new String(header);
			
			versionNumber = fileStream.readInt();
			
			log.info("Logfile header: " + headerString + ", Version: " + versionNumber);
			
			while (fileStream.available() > 0)
			{
				long timestamp = fileStream.readLong();
				EMessageType msgType = EMessageType.getMessageTypeConstant(fileStream.readInt());
				int msgSize = fileStream.readInt();
				
				byte[] data = new byte[msgSize];
				fileStream.readFully(data);
				
				switch (msgType)
				{
					case SSL_REFBOX_2013:
						parseRefereeMsg(timestamp, data);
						break;
					case SSL_VISION_2014:
						parseWrapperPacket(timestamp, data);
						break;
					default:
						break;
				}
			}
			
			fileStream.close();
			notifyLoadComplete(true);
		} catch (EOFException e1)
		{
			notifyLoadComplete(true);
			log.info("Loading logfile complete", e1);
		} catch (IOException e1)
		{
			notifyLoadComplete(false);
			log.error("Loading logfile failed", e1);
		}
	}
	
	
	/**
	 * @return the packets
	 */
	public List<SSLGameLogfileEntry> getPackets()
	{
		return packets;
	}
	
	
	private void notifyLoadComplete(final boolean success)
	{
		if (loadCompleteObserver != null)
		{
			loadCompleteObserver.onLoadComplete(success);
		}
	}
	
	
	/**
	 * @param data
	 */
	private void parseWrapperPacket(final long timestamp, final byte[] data)
	{
		final SSL_WrapperPacket sslPacket;
		try
		{
			sslPacket = SSL_WrapperPacket.parseFrom(data);
			packets.add(new SSLGameLogfileEntry(timestamp, sslPacket));
		} catch (Exception err)
		{
			log.error("invalid ssl package", err);
		}
	}
	
	
	/**
	 * @param data
	 */
	private void parseRefereeMsg(final long timestamp, final byte[] data)
	{
		final SSL_Referee sslReferee;
		try
		{
			sslReferee = SSL_Referee.parseFrom(data);
			packets.add(new SSLGameLogfileEntry(timestamp, sslReferee));
		} catch (Exception err)
		{
			log.error("invalid ssl package", err);
		}
	}
	
	
	/**
	 * @return the headerString
	 */
	public String getHeaderString()
	{
		return headerString;
	}
	
	
	/**
	 * @return the versionNumber
	 */
	public int getVersionNumber()
	{
		return versionNumber;
	}
	
	/**
	 * Get notified when a file is loaded.
	 */
	@FunctionalInterface
	public interface ISSLGameLogfileObserver
	{
		/**
		 * @param success
		 */
		void onLoadComplete(boolean success);
	}
	
	
	/**
	 * Represents a single packet capture.
	 * Can either contain a SSL wrapper packet or a referee message.
	 */
	public class SSLGameLogfileEntry
	{
		private long							timestamp;
		private final SSL_WrapperPacket	visionPacket;
		private final SSL_Referee			refereePacket;
		
		
		/**
		 * Create from wrapper packet.
		 *
		 * @param timestamp
		 * @param vision
		 */
		public SSLGameLogfileEntry(final long timestamp, final SSL_WrapperPacket vision)
		{
			this.timestamp = timestamp;
			visionPacket = vision;
			refereePacket = null;
		}
		
		
		/**
		 * Create from referee packet.
		 *
		 * @param timestamp
		 * @param ref
		 */
		public SSLGameLogfileEntry(final long timestamp, final SSL_Referee ref)
		{
			this.timestamp = timestamp;
			refereePacket = ref;
			visionPacket = null;
		}
		
		
		/**
		 * @return the timestamp
		 */
		public long getTimestamp()
		{
			return timestamp;
		}
		
		
		/**
		 * @return the visionPacket
		 */
		public Optional<SSL_WrapperPacket> getVisionPacket()
		{
			return Optional.ofNullable(visionPacket);
		}
		
		
		/**
		 * @return the refereePacket
		 */
		public Optional<SSL_Referee> getRefereePacket()
		{
			return Optional.ofNullable(refereePacket);
		}
		
		
		/**
		 * @param adj
		 */
		public void adjustTimestamp(final long adj)
		{
			timestamp += adj;
		}
		
		
		/**
		 * Get protobuf message.
		 * 
		 * @return
		 */
		public AbstractMessage getProtobufMsg()
		{
			if (visionPacket != null)
			{
				return visionPacket;
			}
			
			if (refereePacket != null)
			{
				return refereePacket;
			}
			
			return null;
		}
	}
}
