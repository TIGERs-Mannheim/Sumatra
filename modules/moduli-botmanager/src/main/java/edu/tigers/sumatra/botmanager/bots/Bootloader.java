/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.basestation.IBaseStationObserver;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerBootloaderData;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerBootloaderCrc;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerBootloaderRequestCrc;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerBootloaderRequestData;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerBootloaderRequestSize;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerBootloaderSize;
import edu.tigers.sumatra.ids.BotID;


/**
 * Bootloader logic.
 * 
 * @author AndreR
 */
public class Bootloader implements IBaseStationObserver
{
	private static final Logger log = Logger.getLogger(Bootloader.class);
	
	/**
	 * Booloader process observer.
	 */
	@FunctionalInterface
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
	
	
	private static String programFolder;
	private final IBaseStation baseStation;
	private final List<IBootloaderObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/**
	 * @param bs
	 */
	public Bootloader(final IBaseStation bs)
	{
		baseStation = bs;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Set the folder containing main.bin, media.bin, ...
	 *
	 * @param folder
	 */
	public static void setProgramFolder(final String folder)
	{
		programFolder = folder;
	}
	
	
	@Override
	public void onIncomingBotCommand(final BotID id, final ACommand command)
	{
		switch (command.getType())
		{
			case CMD_BOOTLOADER_REQUEST_SIZE:
				handleRequestSize(id, command);
				break;
			case CMD_BOOTLOADER_REQUEST_CRC:
				handleRequestCrc(id, command);
				break;
			case CMD_BOOTLOADER_REQUEST_DATA:
				handleRequestData(id, command);
				break;
			default:
				break;
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IBootloaderObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IBootloaderObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyBootloaderProgress(final BotID botId, final EProcessorID procId, final long bytesRead,
			final long totalSize)
	{
		for (IBootloaderObserver observer : observers)
		{
			observer.onBootloaderProgress(botId, procId, bytesRead, totalSize);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	private RandomAccessFile openFile(final int procId) throws IOException
	{
		Path path = Paths.get(programFolder, EProcessorID.getProcessorIDConstant(procId).getFilename());
		return new RandomAccessFile(path.toFile(), "r");
	}
	
	private void handleRequestData(final BotID id, final ACommand command)
	{
		long fileLength = 0;
		TigerBootloaderRequestData reqData = (TigerBootloaderRequestData) command;
		TigerBootloaderData ansData = new TigerBootloaderData();

		byte[] b = new byte[(int) reqData.getSize()];
		Arrays.fill(b, (byte) 0xFF);

		try
		{
			RandomAccessFile file = openFile(reqData.getProcId());
			file.seek(reqData.getOffset());
			fileLength = file.length();
			file.readFully(b);
			file.close();
		} catch (EOFException err)
		{
			// expected error
			log.debug("End of file", err);
		} catch (IOException err)
		{
			log.error("Could not read proc file", err);
			return;
		}

		ansData.setProcId(reqData.getProcId());
		ansData.setOffset(reqData.getOffset());
		ansData.setPayload(b);

		baseStation.enqueueCommand(id, ansData);

		notifyBootloaderProgress(id, EProcessorID.getProcessorIDConstant(reqData.getProcId()), reqData.getOffset()
				+ reqData.getSize(), fileLength);
	}
	
	private void handleRequestCrc(final BotID id, final ACommand command)
	{
		TigerBootloaderRequestCrc crcReq = (TigerBootloaderRequestCrc) command;
		TigerBootloaderCrc crcAns = new TigerBootloaderCrc();

		int readSize = (int) (crcReq.getEndAddr() - crcReq.getStartAddr());

		byte[] b = new byte[readSize];
		Arrays.fill(b, (byte) 0xFF);

		try
		{
			RandomAccessFile file = openFile(crcReq.getProcId());
			file.seek(crcReq.getStartAddr());
			file.readFully(b);
			file.close();
		} catch (EOFException err)
		{
			// expected error
			log.debug("End of file", err);
		} catch (IOException err)
		{
			log.error("Could not open/read proc file", err);
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
	
	private void handleRequestSize(final BotID id, final ACommand command)
	{
		TigerBootloaderRequestSize reqSize = (TigerBootloaderRequestSize) command;

		TigerBootloaderSize size = new TigerBootloaderSize();
		size.setProcId(reqSize.getProcId());

		try
		{
			RandomAccessFile file = openFile(reqSize.getProcId());
			size.setSize(file.length());
			file.close();
		} catch (IOException err)
		{
			size.setInvalidSize();
			log.error("Could not open/read proc file", err);
		}

		baseStation.enqueueCommand(id, size);

		notifyBootloaderProgress(id, EProcessorID.getProcessorIDConstant(reqSize.getProcId()), 0, size.getSize());
	}
	
	
	/**
	 * Bot processor ID.
	 */
	public enum EProcessorID
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
		PROC_ID_MAIN2016(5, "main2016.bin"),
		/** */
		PROC_ID_UNKNOWN(6, "unknown.bin");

		private String name;
		private int id;


		EProcessorID(final int id, final String filename)
		{
			name = filename;
			this.id = id;
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
	}
}
