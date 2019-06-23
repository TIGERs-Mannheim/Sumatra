/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.06.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderCommand.EBootCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderResponse;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;


/**
 * Bootloader logic.
 * 
 * @author AndreR
 */
public class Bootloader
{
	/** */
	public interface IBootloaderObserver
	{
		/**
		 * @param state
		 */
		void onStateChanged(EBootloaderState state);
		
		
		/**
		 * @param current
		 * @param total
		 */
		void onProgressUpdate(long current, long total);
	}
	
	/** */
	public enum EBootloaderState
	{
		/** */
		IDLE,
		/** */
		MODE_QUERY,
		/** */
		ERASING,
		/** */
		PROGRAMMING
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private RandomAccessFile						file							= null;
	private long										filesize						= 0;
	private Map<Integer, TimerTask>				activeChunks				= new HashMap<Integer, TimerTask>();
	private TigerBotV2								bot							= null;
	private int											nextOffset					= 0;
	private EBootloaderState						state							= EBootloaderState.IDLE;
	private final Logger								log							= Logger.getLogger(getClass());
	private CommandTimeout							lastCommandTimeout		= null;
	private int											target						= 0;
	
	private final List<IBootloaderObserver>	observers					= new ArrayList<IBootloaderObserver>();
	
	
	private static final int						CHUNK_TIMEOUT				= 500;
	private static final int						COMMAND_TIMEOUT			= 500;
	private static final int						ERASE_TIMEOUT				= 12000;
	private static final int						NUM_PACKETS_IN_FLIGHT	= 1;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param bot
	 */
	public Bootloader(final TigerBotV2 bot)
	{
		this.bot = bot;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final IBootloaderObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IBootloaderObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyStateChanged(final EBootloaderState state)
	{
		synchronized (observers)
		{
			for (IBootloaderObserver observer : observers)
			{
				observer.onStateChanged(state);
			}
		}
	}
	
	
	private void notifyProgressUpdate(final long current, final long total)
	{
		synchronized (observers)
		{
			for (IBootloaderObserver observer : observers)
			{
				observer.onProgressUpdate(current, total);
			}
		}
	}
	
	
	/**
	 * @param binFile
	 * @param target
	 * @return
	 */
	public boolean start(final String binFile, final int target)
	{
		this.target = target;
		
		try
		{
			file = new RandomAccessFile(binFile, "r");
			filesize = file.length();
		} catch (FileNotFoundException err)
		{
			return false;
		} catch (IOException err)
		{
			return false;
		}
		
		activeChunks.clear();
		nextOffset = 0;
		notifyProgressUpdate(nextOffset, filesize);
		
		log.debug("Starting wireless firmware update. Filesize: " + filesize + " Bytes");
		
		state = EBootloaderState.MODE_QUERY;
		notifyStateChanged(state);
		
		sendCommand(new TigerBootloaderCommand(EBootCommand.MODE_QUERY));
		
		return true;
	}
	
	
	/** */
	public void cancel()
	{
		for (TimerTask t : activeChunks.values())
		{
			t.cancel();
		}
		
		activeChunks.clear();
		
		if (lastCommandTimeout != null)
		{
			lastCommandTimeout.cancel();
		}
		
		try
		{
			if (file != null)
			{
				file.close();
			}
		} catch (IOException err)
		{
		}
	}
	
	
	private void sendCommand(final TigerBootloaderCommand cmd)
	{
		CommandTimeout timeout = new CommandTimeout(cmd);
		lastCommandTimeout = timeout;
		
		switch (cmd.getCommand())
		{
			case ERASE_MAIN:
			case ERASE_MEDIA:
			{
				GeneralPurposeTimer.getInstance().schedule(timeout, ERASE_TIMEOUT);
			}
				break;
			case ENTER:
			case EXIT:
			{
			}
				break;
			default:
			{
				GeneralPurposeTimer.getInstance().schedule(timeout, COMMAND_TIMEOUT);
			}
				break;
		}
		
		log.debug("Command outgoing: " + cmd.getType());
		
		bot.execute(cmd);
	}
	
	
	/**
	 * @param response
	 */
	public void response(final TigerBootloaderResponse response)
	{
		if (lastCommandTimeout != null)
		{
			lastCommandTimeout.cancel();
		}
		
		switch (state)
		{
			case MODE_QUERY:
			{
				switch (response.getResponse())
				{
					case MODE_NORMAL:
					{
						GeneralPurposeTimer.getInstance().schedule(new DelayToBoot(), 1000);
						
						sendCommand(new TigerBootloaderCommand(EBootCommand.ENTER));
					}
						break;
					case MODE_BOOTLOADER:
					{
						state = EBootloaderState.ERASING;
						
						sendCommand(new TigerBootloaderCommand(EBootCommand.getCommandConstant(EBootCommand.ERASE_MAIN
								.getId() + target)));
					}
						break;
					default:
					{
						log.warn("Invalid response in state MODE_QUERY: " + response.getType());
					}
				}
			}
				break;
			case ERASING:
			{
				switch (response.getResponse())
				{
					case ACK:
					{
						state = EBootloaderState.PROGRAMMING;
						
						for (int i = 0; i < NUM_PACKETS_IN_FLIGHT; i++)
						{
							sendChunk(nextOffset);
							nextOffset += TigerBootloaderData.BOOTLOADER_DATA_SIZE;
						}
						
						notifyProgressUpdate(nextOffset, filesize);
					}
						break;
					default:
					{
						log.warn("None-ACK response while in ERASING state");
					}
						break;
				}
			}
				break;
			case PROGRAMMING:
			{
				switch (response.getResponse())
				{
					case NACK:
					{
						TimerTask t = activeChunks.remove(response.getOffset());
						if (t != null)
						{
							t.cancel();
						}
						sendChunk(response.getOffset());
					}
						break;
					case ACK:
					{
						TimerTask t = activeChunks.remove(response.getOffset());
						if (t != null)
						{
							t.cancel();
						}
						
						if (nextOffset < filesize)
						{
							sendChunk(nextOffset);
							nextOffset += TigerBootloaderData.BOOTLOADER_DATA_SIZE;
							notifyProgressUpdate(nextOffset, filesize);
						} else if (activeChunks.isEmpty())
						{
							state = EBootloaderState.IDLE;
							
							sendCommand(new TigerBootloaderCommand(EBootCommand.EXIT));
							
							try
							{
								file.close();
							} catch (IOException err)
							{
							}
						}
					}
						break;
					default:
						break;
				}
			}
				break;
			default:
			{
				log.warn("Response " + response.getType() + " in IDLE mode");
			}
				break;
		}
		
		notifyStateChanged(state);
	}
	
	
	private void timeout(final int offset)
	{
		if (state != EBootloaderState.PROGRAMMING)
		{
			log.warn("Data chunk timeout while not in programming mode");
		}
		
		log.info("Data timeout @" + offset);
		
		activeChunks.remove(offset);
		
		sendChunk(offset);
	}
	
	
	private void timeout(final TigerBootloaderCommand cmd)
	{
		lastCommandTimeout = null;
		
		log.info("Command timeout " + cmd.getType());
		
		switch (state)
		{
			case MODE_QUERY:
			{
				sendCommand(new TigerBootloaderCommand(EBootCommand.MODE_QUERY));
			}
				break;
			case ERASING:
			{
				sendCommand(new TigerBootloaderCommand(EBootCommand.getCommandConstant(EBootCommand.ERASE_MAIN.getId()
						+ target)));
			}
				break;
			case PROGRAMMING:
			{
				log.warn("Command timeout in PROGRAMMING mode");
			}
			default:
				break;
		}
	}
	
	
	private void delayToBoot()
	{
		log.debug("Boot delay finished");
		
		sendCommand(new TigerBootloaderCommand(EBootCommand.MODE_QUERY));
	}
	
	
	private void sendChunk(final int offset)
	{
		byte[] data = new byte[TigerBootloaderData.BOOTLOADER_DATA_SIZE];
		
		try
		{
			file.seek(offset);
			file.readFully(data);
		} catch (EOFException err)
		{
			log.debug("EOF");
		} catch (IOException err)
		{
			log.error("File read error");
			state = EBootloaderState.IDLE;
			notifyStateChanged(state);
			return;
		}
		
		TigerBootloaderData cmd = new TigerBootloaderData(data, TigerBootloaderData.BOOTLOADER_DATA_SIZE, offset);
		
		DataTimeout t = new DataTimeout(offset);
		GeneralPurposeTimer.getInstance().schedule(t, CHUNK_TIMEOUT);
		
		activeChunks.put(offset, t);
		
		bot.execute(cmd);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- classes --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private class DataTimeout extends TimerTask
	{
		private final int	offset;
		
		
		/**
		 * @param o
		 */
		public DataTimeout(final int o)
		{
			offset = o;
		}
		
		
		@Override
		public void run()
		{
			timeout(offset);
		}
	}
	
	private class CommandTimeout extends TimerTask
	{
		private final TigerBootloaderCommand	cmd;
		
		
		/**
		 * @param cmd
		 */
		public CommandTimeout(final TigerBootloaderCommand cmd)
		{
			this.cmd = cmd;
		}
		
		
		@Override
		public void run()
		{
			timeout(cmd);
		}
	}
	
	private class DelayToBoot extends TimerTask
	{
		@Override
		public void run()
		{
			delayToBoot();
		}
	}
	
	
	/**
	 * @return the state
	 */
	public final EBootloaderState getState()
	{
		return state;
	}
	
	
	/**
	 * @param state the state to set
	 */
	public final void setState(final EBootloaderState state)
	{
		this.state = state;
		notifyStateChanged(state);
	}
}
