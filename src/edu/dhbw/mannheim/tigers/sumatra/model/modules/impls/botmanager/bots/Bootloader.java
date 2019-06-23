/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.06.2013
 * Author(s): AndreR
 * 
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderCommand.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderResponse;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderResponse.EResponse;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;


/**
 * Bootloader logic.
 * 
 * @author AndreR
 * 
 */
public class Bootloader
{
	/** */
	public interface IBootloaderObserver
	{
		/**
		 * 
		 * @param state
		 */
		void onStateChanged(EBootloaderState state);
		
		
		/**
		 * 
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
	private RandomAccessFile			file						= null;
	private long							filesize					= 0;
	private Map<Integer, TimerTask>	activeChunks			= new HashMap<Integer, TimerTask>();
	private TigerBotV2					bot						= null;
	private int								nextOffset				= 0;
	private EBootloaderState			state						= EBootloaderState.IDLE;
	private final Logger					log						= Logger.getLogger(getClass());
	private CommandTimeout				lastCommandTimeout	= null;
	private boolean						targetMain				= true;
	
	private final List<IBootloaderObserver>	observers				= new ArrayList<IBootloaderObserver>();
	
	
	private final static int			CHUNK_TIMEOUT			= 500;
	private final static int			COMMAND_TIMEOUT		= 500;
	private final static int			ERASE_TIMEOUT			= 12000;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param bot
	 */
	public Bootloader(TigerBotV2 bot)
	{
		this.bot = bot;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param observer
	 */
	public void addObserver(IBootloaderObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * 
	 * @param observer
	 */
	public void removeObserver(IBootloaderObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyStateChanged(EBootloaderState state)
	{
		synchronized (observers)
		{
			for (IBootloaderObserver observer : observers)
			{
				observer.onStateChanged(state);
			}
		}
	}
	
	
	private void notifyProgressUpdate(long current, long total)
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
	 * 
	 * @param binFile
	 * @param targetMain
	 * @return
	 */
	public boolean start(String binFile, boolean targetMain)
	{
		this.targetMain = targetMain;
		
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
		
		sendCommand(new TigerBootloaderCommand(ECommand.MODE_QUERY));
		
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
	
	
	private void sendCommand(TigerBootloaderCommand cmd)
	{
		CommandTimeout timeout = new CommandTimeout(cmd);
		lastCommandTimeout = timeout;
		
		switch (cmd.getType())
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
	 * 
	 * @param response
	 */
	public void response(TigerBootloaderResponse response)
	{
		if (lastCommandTimeout != null)
		{
			lastCommandTimeout.cancel();
		}
		
		switch (state)
		{
			case MODE_QUERY:
			{
				switch (response.getType())
				{
					case MODE_NORMAL:
					{
						GeneralPurposeTimer.getInstance().schedule(new DelayToBoot(), 1000);
						
						sendCommand(new TigerBootloaderCommand(ECommand.ENTER));
					}
						break;
					case MODE_BOOTLOADER:
					{
						state = EBootloaderState.ERASING;
						
						if (targetMain)
						{
							sendCommand(new TigerBootloaderCommand(ECommand.ERASE_MAIN));
						} else
						{
							sendCommand(new TigerBootloaderCommand(ECommand.ERASE_MEDIA));
						}
					}
						break;
					default:
					{
						log.warn("Invalid response in state MODE_QUERY: " + response.getType());
					}
				}
				if (response.getType() == EResponse.MODE_NORMAL)
				{
				}
				
			}
				break;
			case ERASING:
			{
				switch (response.getType())
				{
					case ACK:
					{
						state = EBootloaderState.PROGRAMMING;
						
						sendChunk(nextOffset);
						nextOffset += TigerBootloaderData.BOOTLOADER_DATA_SIZE;
						sendChunk(nextOffset);
						nextOffset += TigerBootloaderData.BOOTLOADER_DATA_SIZE;
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
				switch (response.getType())
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
							
							sendCommand(new TigerBootloaderCommand(ECommand.EXIT));
							
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
	
	
	private void timeout(int offset)
	{
		if (state != EBootloaderState.PROGRAMMING)
		{
			log.warn("Data chunk timeout while not in programming mode");
		}
		
		log.info("Data timeout @" + offset);
		
		activeChunks.remove(offset);
		
		sendChunk(offset);
	}
	
	
	private void timeout(TigerBootloaderCommand cmd)
	{
		lastCommandTimeout = null;
		
		log.info("Command timeout " + cmd.getType());
		
		switch (state)
		{
			case MODE_QUERY:
			{
				sendCommand(new TigerBootloaderCommand(ECommand.MODE_QUERY));
			}
				break;
			case ERASING:
			{
				if (targetMain)
				{
					sendCommand(new TigerBootloaderCommand(ECommand.ERASE_MAIN));
				} else
				{
					sendCommand(new TigerBootloaderCommand(ECommand.ERASE_MEDIA));
				}
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
		
		sendCommand(new TigerBootloaderCommand(ECommand.MODE_QUERY));
	}
	
	
	private void sendChunk(int offset)
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
		
		
		public DataTimeout(int o)
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
		
		
		public CommandTimeout(TigerBootloaderCommand cmd)
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
}
