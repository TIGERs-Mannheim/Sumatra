/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gamelog;

import com.google.common.primitives.Bytes;
import com.google.protobuf.AbstractMessage;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.gamelog.proto.LogLabelerData;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;


/**
 * Reads a logfile containing SSL-Vision and referee commands.
 *
 * @author AndreR <andre@ryll.cc>
 */
public class SSLGameLogReader
{
	private static final Logger log = LogManager.getLogger(SSLGameLogReader.class.getName());

	private String headerString = "";
	private int versionNumber = 0;

	private List<SSLGameLogfileEntry> packets = new ArrayList<>();

	private ISSLGameLogfileObserver loadCompleteObserver = null;

	private enum LogFileType
	{
		LOG_FILE("SSL_LOG_FILE"),
		LABELER_FILE("SSL_LABELER_DATA"),
		UNKNOWN("");

		private String header;


		LogFileType(String header)
		{
			this.header = header;
		}
	}


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
			DataInputStream fileStream;
			if (path.endsWith(".gz"))
			{
				final GZIPInputStream gzipInputStream = new GZIPInputStream(fileInStream);
				fileStream = new DataInputStream(gzipInputStream);
			} else
			{
				fileStream = new DataInputStream(fileInStream);
			}


			LogFileType logFileTypeFromHeader = getLogFileTypeFromHeader(fileStream);
			switch (logFileTypeFromHeader)
			{
				case LOG_FILE:
					readLogFile(fileStream);
					break;
				case LABELER_FILE:
					readLogLabelerData(fileStream, path);
					break;
				default:
				case UNKNOWN:
					throw new IOException("Unhandled log file type: " + logFileTypeFromHeader);
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


	private LogFileType getLogFileTypeFromHeader(DataInputStream fileStream) throws IOException
	{
		byte[] nextBytes = new byte[4];
		fileStream.readFully(nextBytes);
		String startHeader = new String(nextBytes).toUpperCase();

		if (!"SSL_".equals(startHeader))
		{
			log.warn("Unknown header: {}", startHeader);
			return LogFileType.UNKNOWN;
		}

		List<Byte> middleHeaderBuilder = new ArrayList<>();
		byte nextByte = fileStream.readByte();
		while ((char) nextByte != '_')
		{
			middleHeaderBuilder.add(nextByte);
			nextByte = fileStream.readByte();
		}

		nextBytes = new byte[4];
		fileStream.readFully(nextBytes);
		String endHeader = new String(nextBytes).toUpperCase();

		headerString = startHeader + new String(Bytes.toArray(middleHeaderBuilder)).toUpperCase() + "_" + endHeader;

		Optional<LogFileType> optionalLogFileType = Arrays.stream(LogFileType.values())
				.filter(logFileType -> logFileType.header.equals(headerString))
				.findFirst();


		versionNumber = fileStream.readInt();
		log.info("Logfile header: {}, Version: {}", headerString, versionNumber);

		return optionalLogFileType.orElse(LogFileType.UNKNOWN);
	}


	private void readLogFile(DataInputStream fileStream) throws IOException
	{
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
	}


	private void readLogLabelerData(DataInputStream fileStream, String path) throws IOException
	{
		int sizeMetadata;
		File file = new File(path);
		try (RandomAccessFile randomAccessLogFile = new RandomAccessFile(file, "r"))
		{
			randomAccessLogFile.seek(file.length() - 4);
			sizeMetadata = randomAccessLogFile.readInt();
		} catch (IOException ex)
		{
			throw new IOException("Log Labeler Data: Error on reading length of metadata block", ex);
		}

		long frameBlockSizeLeft = file.length() - sizeMetadata - 24; // 20 Bytes Header + 4 Bytes metadata size

		int frameId = 0;
		while (frameBlockSizeLeft > 0)
		{
			int msgSize = fileStream.readInt();
			frameBlockSizeLeft -= 4 + msgSize;

			byte[] data = new byte[msgSize];
			fileStream.readFully(data);

			LogLabelerData.LabelerFrameGroup frameGroup = LogLabelerData.LabelerFrameGroup.parseFrom(data);

			for (LogLabelerData.LabelerFrame labelerFrame : frameGroup.getFramesList())
			{
				long timestamp = labelerFrame.getTimestamp();

				switch (labelerFrame.getFrameCase())
				{
					case REFEREE_FRAME:
						Referee sslRefereePacket = labelerFrame.getRefereeFrame();
						final SSLGameLogfileEntry e = new SSLGameLogfileEntry(timestamp, sslRefereePacket);
						e.setFrameId(frameId);
						packets.add(e);
						break;
					case VISION_FRAME:
						SSL_WrapperPacket sslVisionPacket = labelerFrame.getVisionFrame();
						final SSLGameLogfileEntry e1 = new SSLGameLogfileEntry(timestamp, sslVisionPacket);
						e1.setFrameId(frameId);
						packets.add(e1);
						break;
					default:
						break;
				}
			}
			frameId++;
		}

		byte[] data = new byte[sizeMetadata];
		fileStream.readFully(data);
		final LogLabelerData.LabelerMetadata labelerMetadata = LogLabelerData.LabelerMetadata.parseFrom(data);
		log.info("Metadata: " + labelerMetadata);
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
		final Referee sslReferee;
		try
		{
			sslReferee = Referee.parseFrom(data);
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
		private long timestamp;
		private long frameId;
		private final SSL_WrapperPacket visionPacket;
		private final Referee refereePacket;


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
		public SSLGameLogfileEntry(final long timestamp, final Referee ref)
		{
			this.timestamp = timestamp;
			refereePacket = ref;
			visionPacket = null;
		}


		public long getFrameId()
		{
			return frameId;
		}


		public void setFrameId(final long frameId)
		{
			this.frameId = frameId;
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
		public Optional<Referee> getRefereePacket()
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

			return refereePacket;
		}
	}
}
