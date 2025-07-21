/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamelog;

import com.google.common.primitives.Bytes;
import edu.tigers.sumatra.gamelog.filters.MessageFilter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;


/**
 * Reads file in SSL game log format.
 */
@Log4j2
public class GameLogReader
{
	@Getter
	private int versionNumber;

	@Getter
	private List<GameLogMessage> messages = new ArrayList<>();

	private GameLogReaderObserver loadCompleteObserver = null;

	/**
	 * Messages are only loaded if all filters return true for them.
	 */
	private List<MessageFilter> filters = new ArrayList<>();


	/**
	 * Load a logfile asynchronously.
	 *
	 * @param path
	 */
	private void loadFile(final String path)
	{
		new Thread(() -> loadFileBlocking(path), "Loader").start();
	}


	/**
	 * Load a logfile asynchronously and get notified when it is done.
	 *
	 * @param path
	 * @param obs
	 */
	public void loadFile(final String path, final GameLogReaderObserver obs)
	{
		loadCompleteObserver = obs;

		loadFile(path);
	}


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

			if (!parseHeader(fileStream))
				throw new IOException("Unhandled log file type.");

			parseMessages(fileStream);

			fileStream.close();
			notifyLoadComplete(true);
			log.info("Loading logfile {} complete", path);
		} catch (EOFException e1)
		{
			notifyLoadComplete(true);
			log.info("Loading logfile {} complete", path, e1);
		} catch (IOException e1)
		{
			notifyLoadComplete(false);
			log.error("Loading logfile {} failed", path, e1);
		}
	}


	public void addFilter(final MessageFilter filter)
	{
		filters.add(filter);
	}


	private boolean parseHeader(DataInputStream fileStream) throws IOException
	{
		byte[] nextBytes = new byte[4];
		fileStream.readFully(nextBytes);
		String startHeader = new String(nextBytes).toUpperCase();

		// Due to a race condition, that should be fixed by now, the header might be incomplete.
		// The following code can be simplified later, when old gamelogs are not needed anymore.
		if (!"SSL_".equals(startHeader))
		{
			log.warn("Unknown header: {}", startHeader);
			return false;
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

		String headerString =
				startHeader + new String(Bytes.toArray(middleHeaderBuilder)).toUpperCase() + "_" + endHeader;

		versionNumber = fileStream.readInt();
		log.info("Logfile header: {}, Version: {}", headerString, versionNumber);

		return true;
	}


	private void parseMessages(DataInputStream fileStream) throws IOException
	{
		while (fileStream.available() > 0)
		{
			long timestamp = fileStream.readLong();
			EMessageType msgType = EMessageType.getMessageTypeConstant(fileStream.readInt());
			int msgSize = fileStream.readInt();

			boolean isFiltered = filters.stream().anyMatch(f -> !f.filter(timestamp, msgType));
			if (isFiltered)
			{
				fileStream.skipBytes(msgSize);
			} else
			{
				byte[] data = new byte[msgSize];
				fileStream.readFully(data);

				GameLogMessage message = new GameLogMessage(timestamp, msgType, data);
				messages.add(message);
			}
		}
	}


	@FunctionalInterface
	public interface GameLogReaderObserver
	{
		/**
		 * @param success
		 */
		void onLoadComplete(boolean success);
	}


	private void notifyLoadComplete(final boolean success)
	{
		if (loadCompleteObserver != null)
		{
			loadCompleteObserver.onLoadComplete(success);
		}
	}
}
