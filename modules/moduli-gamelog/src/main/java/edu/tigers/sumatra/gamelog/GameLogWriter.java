/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gamelog;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Logs arbitrary game log messages. Mostly a binary blob with timestamp and type info.
 */
@Log4j2
@RequiredArgsConstructor
public class GameLogWriter
{
	private static final String GAMELOG_PATH = "data/gamelog";
	private static final int VERSION = 1;
	private static final String FILE_TYPE = "SSL_LOG_FILE";

	private DataOutputStream outputStream;


	/**
	 * Open a gamelog for writing using current date/time as filename.
	 */
	public void open(String matchType, String stage, String teamYellow, String teamBlue)
	{
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		dt.setTimeZone(TimeZone.getDefault());
		String filename =
				dt.format(new Date()) + String.format("-%s-%s-%s-vs-%s", matchType, stage, teamYellow, teamBlue);
		open(filename);
	}


	/**
	 * Open a gamelog for writing.
	 * Suppress missing finally block and status code return ignorance
	 *
	 * @param filename
	 */
	@SuppressWarnings({ "squid:S2095", "squid:S899" })
	public void open(final String filename)
	{
		String fullName = GAMELOG_PATH + "/" + filename + ".log";

		try
		{
			File folder = new File(GAMELOG_PATH);
			// noinspection ResultOfMethodCallIgnored
			folder.mkdirs();

			// open file
			outputStream = new DataOutputStream(new FileOutputStream(fullName, false));

			writeHeader();
		} catch (IOException e)
		{
			log.error("Exception on opening gamelog file", e);
		}
	}


	/**
	 * Open a gamelog for writing.
	 * Suppress missing finally block and status code return ignorance
	 *
	 * @param fullName
	 */
	@SuppressWarnings({ "squid:S2095", "squid:S899" })
	public synchronized void openPath(final String fullName)
	{
		try
		{
			// open file
			outputStream = new DataOutputStream(new FileOutputStream(fullName, false));

			writeHeader();
		} catch (IOException e)
		{
			log.error("Exception on opening gamelog file", e);
		}
	}


	private void writeHeader() throws IOException
	{
		outputStream.writeBytes(FILE_TYPE);
		outputStream.writeInt(VERSION);
	}


	/**
	 * @return
	 */
	public boolean isOpen()
	{
		return outputStream != null;
	}


	/**
	 * Close gamelog.
	 */
	public synchronized void close()
	{
		if (outputStream != null)
		{
			try
			{
				outputStream.close();
			} catch (IOException e)
			{
				log.error("Exception on closing gamelog file", e);
			}
			outputStream = null;
		}
	}


	/**
	 * Write gamelog entry to file.
	 *
	 * @param msg
	 */
	public synchronized void write(final GameLogMessage msg)
	{
		if (outputStream == null)
		{
			return;
		}

		try
		{
			outputStream.writeLong(msg.getTimestampNs());
			outputStream.writeInt(msg.getType().getId());
			outputStream.writeInt(msg.getData().length);
			outputStream.write(msg.getData());
		} catch (IOException e)
		{
			log.error("Exception writing to gamelog", e);
		}
	}
}
