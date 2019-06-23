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
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStationObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerBootloaderCrc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerBootloaderRequestCrc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerBootloaderRequestData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerBootloaderRequestSize;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerBootloaderSize;


/**
 * Bootloader logic.
 * 
 * @author AndreR
 */
public class Bootloader implements IBaseStationObserver
{
	/** */
	public interface IBootloaderObserver
	{
		/**
		 * @param botId
		 * @param procId
		 * @param bytesRead
		 * @param totalSize
		 */
		void onBootloaderProgress(BotID botId, EProcessorID procId, long bytesRead, long totalSize);
	}
	
	/** */
	public static enum EProcessorID
	{
		/** */
		PROC_ID_MAIN(0, "main.bin"),
		/** */
		PROC_ID_MEDIA(1, "media.bin"),
		/** */
		PROC_ID_LEFT(2, "left.bin"),
		/** */
		PROC_ID_RIGHT(3, "right.bin"),
		/** */
		PROC_ID_KD(4, "kd.bin"),
		/** */
		PROC_ID_UNKNOWN(5, "unknown.bin");
		
		private EProcessorID(final int id, final String filename)
		{
			name = filename;
			this.id = id;
		}
		
		private String	name;
		private int		id;
		
		
		/**
		 * @return the name
		 */
		public String getFilename()
		{
			return name;
		}
		
		
		/**
		 * @return the id
		 */
		public int getId()
		{
			return id;
		}
		
		
		/**
		 * Convert procID to enum.
		 * 
		 * @param id
		 * @return
		 */
		public static EProcessorID getProcessorIDConstant(final int id)
		{
			for (EProcessorID t : values())
			{
				if (t.getId() == id)
				{
					return t;
				}
			}
			
			return PROC_ID_UNKNOWN;
		}
	}
	
	private final IBaseStation								baseStation;
	private static final List<IBootloaderObserver>	observers	= new ArrayList<IBootloaderObserver>();
	private static String									programFolder;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param bs
	 */
	public Bootloader(final IBaseStation bs)
	{
		baseStation = bs;
		
		bs.addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public void delete()
	{
		baseStation.removeObserver(this);
	}
	
	
	/**
	 * @param observer
	 */
	public static void addObserver(final IBootloaderObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public static void removeObserver(final IBootloaderObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyBootloaderProgress(final BotID botId, final EProcessorID procId, final long bytesRead,
			final long totalSize)
	{
		for (IBootloaderObserver observer : observers)
		{
			observer.onBootloaderProgress(botId, procId, bytesRead, totalSize);
		}
	}
	
	
	/**
	 * Set the folder containing main.bin, media.bin, ...
	 * 
	 * @param folder
	 */
	public static void setProgramFolder(final String folder)
	{
		programFolder = folder;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	private RandomAccessFile openFile(final int procId) throws FileNotFoundException, IOException
	{
		String filename = programFolder + "\\"
				+ EProcessorID.getProcessorIDConstant(procId).getFilename();
		return new RandomAccessFile(filename, "r");
	}
	
	
	@Override
	public void onIncommingBotCommand(final BotID id, final ACommand command)
	{
		switch (command.getType())
		{
			case CMD_BOOTLOADER_REQUEST_SIZE:
			{
				TigerBootloaderRequestSize reqSize = (TigerBootloaderRequestSize) command;
				
				TigerBootloaderSize size = new TigerBootloaderSize();
				size.setProcId(reqSize.getProcId());
				
				try
				{
					RandomAccessFile file = openFile(reqSize.getProcId());
					size.setSize(file.length());
					file.close();
				} catch (FileNotFoundException err)
				{
					size.setInvalidSize();
				} catch (IOException err)
				{
					size.setInvalidSize();
				}
				
				baseStation.enqueueCommand(id, size);
				
				notifyBootloaderProgress(id, EProcessorID.getProcessorIDConstant(reqSize.getProcId()), 0, size.getSize());
			}
				break;
			case CMD_BOOTLOADER_REQUEST_CRC:
			{
				TigerBootloaderRequestCrc crcReq = (TigerBootloaderRequestCrc) command;
				TigerBootloaderCrc crcAns = new TigerBootloaderCrc();
				
				int readSize = (int) (crcReq.getEndAddr() - crcReq.getStartAddr());
				
				byte b[] = new byte[readSize];
				Arrays.fill(b, (byte) 0xFF);
				
				try
				{
					RandomAccessFile file = openFile(crcReq.getProcId());
					file.seek(crcReq.getStartAddr());
					file.readFully(b);
					file.close();
				} catch (FileNotFoundException err)
				{
					return;
				} catch (EOFException err)
				{
				} catch (IOException err)
				{
					return;
				}
				
				CRC32 crc = new CRC32();
				crc.reset();
				crc.update(b);
				crcAns.setCrc(crc.getValue());
				crcAns.setProcId(crcReq.getProcId());
				crcAns.setStartAddr(crcReq.getStartAddr());
				crcAns.setEndAddr(crcReq.getEndAddr());
				
				baseStation.enqueueCommand(id, crcAns);
			}
				break;
			case CMD_BOOTLOADER_REQUEST_DATA:
			{
				long fileLength = 0;
				TigerBootloaderRequestData reqData = (TigerBootloaderRequestData) command;
				TigerBootloaderData ansData = new TigerBootloaderData();
				
				byte b[] = new byte[(int) reqData.getSize()];
				Arrays.fill(b, (byte) 0xFF);
				
				try
				{
					RandomAccessFile file = openFile(reqData.getProcId());
					file.seek(reqData.getOffset());
					fileLength = file.length();
					file.readFully(b);
					file.close();
				} catch (FileNotFoundException err)
				{
					return;
				} catch (EOFException err)
				{
				} catch (IOException err)
				{
					return;
				}
				
				ansData.setProcId(reqData.getProcId());
				ansData.setOffset(reqData.getOffset());
				ansData.setPayload(b);
				
				baseStation.enqueueCommand(id, ansData);
				
				notifyBootloaderProgress(id, EProcessorID.getProcessorIDConstant(reqData.getProcId()), reqData.getOffset()
						+ reqData.getSize(), fileLength);
			}
				break;
			default:
				break;
		}
	}
}
