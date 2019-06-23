/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.log.appender;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.log.GameLog.IGameLogObserver;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.autoreferee.engine.log.GameLogFormatter;
import edu.tigers.autoreferee.engine.log.GameTime;


/**
 * @author "Lukas Magel"
 */
public class GameLogFileAppender implements IGameLogObserver, Runnable
{
	private static final Logger log = Logger.getLogger(GameLogFileAppender.class);
	
	private final Path targetPath;
	private final DecimalFormat msFormat = new DecimalFormat("000");
	private final DecimalFormat sFormat = new DecimalFormat("00");
	private final DecimalFormat minFormat = new DecimalFormat("00");
	private Thread thread;
	private LinkedBlockingDeque<GameLogEntry> entryQueue;
	private BufferedWriter writer;
	
	
	/**
	 * @param outputPath
	 */
	public GameLogFileAppender(final Path outputPath)
	{
		targetPath = outputPath;
		entryQueue = new LinkedBlockingDeque<>();
		thread = new Thread(this, "GameLogFileAppender");
	}
	
	
	@Override
	public void onNewEntry(final int id, final GameLogEntry entry)
	{
		try
		{
			entryQueue.put(entry);
		} catch (InterruptedException e)
		{
			log.error("", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	/**
	 * @throws IOException
	 */
	public void start() throws IOException
	{
		Path folder = targetPath.getParent();
		if (folder != null && !folder.toFile().isDirectory())
		{
			File folderFile = folder.toFile();
			if (!folderFile.exists())
			{
				boolean created = folderFile.mkdirs();
				if (!created)
				{
					log.error("Could not create log file folder");
				}
			}
		}
		writer = Files.newBufferedWriter(targetPath);
		thread.start();
	}
	
	
	/**
	 * Tear down thread and writer
	 */
	public void stop()
	{
		try
		{
			thread.interrupt();
			thread.join();
		} catch (InterruptedException e)
		{
			log.error("Error while joining game log writer thread", e);
			Thread.currentThread().interrupt();
		}
		
		try
		{
			if (writer != null)
			{
				writer.close();
			}
		} catch (IOException e)
		{
			log.error("Unable to close game log output stream", e);
		}
	}
	
	
	@Override
	public void run()
	{
		try (BufferedWriter fileWriter = writer)
		{
			while (!Thread.interrupted())
			{
				GameLogEntry entry = entryQueue.take();
				String line = formatEntrySafe(entry);
				
				if (line != null)
				{
					fileWriter.write(line);
					fileWriter.newLine();
					fileWriter.flush();
				}
			}
		} catch (IOException e)
		{
			log.error("Unexpected I/O error while writing game log entry", e);
		} catch (InterruptedException e)
		{
			log.debug("GameLogWriteThread interrupted", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	private String formatEntrySafe(final GameLogEntry entry)
	{
		try
		{
			return formatEntry(entry);
		} catch (Exception e)
		{
			log.warn("Unexpected exception while formatting log entry", e);
			return null;
		}
	}
	
	
	private String formatEntry(final GameLogEntry entry)
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append(formatGameTime(entry.getGameTime()));
		builder.append(" | ");
		builder.append(formatInstant(entry.getInstant()));
		builder.append(" | ");
		
		switch (entry.getType())
		{
			case COMMAND:
				builder.append("Sending command: ");
				builder.append(GameLogFormatter.formatCommand(entry.getCommand()));
				break;
			case FOLLOW_UP:
				followUp(entry, builder);
				break;
			case GAME_EVENT:
				gameEvent(entry, builder);
				break;
			case GAME_STATE:
				builder.append("Game state changed to: ");
				builder.append(entry.getGamestate().toString());
				break;
			case REFEREE_MSG:
				builder.append("Received new Referee Msg: ");
				builder.append(GameLogFormatter.formatRefMsg(entry.getRefereeMsg()));
				break;
			default:
				builder.append("Unknown event type: ");
				builder.append(entry.getType());
				break;
		}
		
		return builder.toString();
	}
	
	
	private void gameEvent(final GameLogEntry entry, final StringBuilder builder)
	{
		IGameEvent event = entry.getGameEvent();
		builder.append("New event: ");
		builder.append(event.toString());
		FollowUpAction action = event.getFollowUpAction();
		if (action != null)
		{
			builder.append(" | Next action: ");
			builder.append(GameLogFormatter.formatFollowUp(action));
		}
	}
	
	
	private void followUp(final GameLogEntry entry, final StringBuilder builder)
	{
		FollowUpAction followUp = entry.getFollowUpAction();
		if (followUp != null)
		{
			builder.append("FollowUpAction set to: ");
			builder.append(GameLogFormatter.formatFollowUp(followUp));
		} else
		{
			builder.append("FollowUpAction reset");
		}
	}
	
	
	private String formatInstant(final Instant instant)
	{
		StringBuilder builder = new StringBuilder();
		LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		
		builder.append(minFormat.format(date.getHour()));
		builder.append(":");
		builder.append(minFormat.format(date.getMinute()));
		builder.append(":");
		builder.append(sFormat.format(date.getSecond()));
		builder.append(":");
		builder.append(msFormat.format((long) date.getNano() / 1_000_000));
		
		return builder.toString();
	}
	
	
	private String formatGameTime(final GameTime gameTime)
	{
		long micros = gameTime.getMicrosLeft();
		
		StringBuilder builder = new StringBuilder();
		builder.append(gameTime.getStage());
		builder.append(" ");
		
		if (micros < 0)
		{
			builder.append("-");
			micros = Math.abs(micros);
		}
		
		int minutes = (int) TimeUnit.MICROSECONDS.toMinutes(micros);
		int seconds = (int) TimeUnit.MICROSECONDS.toSeconds(micros) % 60;
		int ms = (int) TimeUnit.MICROSECONDS.toMillis(micros) % 1_000;
		
		
		builder.append(minFormat.format(minutes));
		builder.append(":");
		builder.append(sFormat.format(seconds));
		builder.append(":");
		builder.append(msFormat.format(ms));
		
		return builder.toString();
	}
	
}
