/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands;

import edu.tigers.sumatra.botmanager.serial.SerialByteConverter;
import edu.tigers.sumatra.botmanager.serial.SerialDescription;
import edu.tigers.sumatra.botmanager.serial.SerialException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Handles bot commands.
 */
public final class CommandFactory
{
	private static final Logger log = LogManager.getLogger(CommandFactory.class.getName());

	private static CommandFactory instance = null;

	private final Map<Integer, SerialDescription> commands = new HashMap<>();

	private static final int HEADER_LENGTH = 1;
	private static final int RELIABLE_HEADER_LENGTH = 3;

	/**
	 * Reliable commands use an extended header, signaled by 8th bit in cmd
	 */
	private static final int RELIABLE_CMD_MASK = 0x80;


	private CommandFactory()
	{
		for (ECommand cmd : ECommand.values())
		{
			getSerialDescription(cmd).ifPresent(desc -> commands.put(cmd.getId(), desc));
		}
	}


	/**
	 * Get singleton instance.
	 *
	 * @return
	 */
	public static synchronized CommandFactory getInstance()
	{
		if (instance == null)
		{
			instance = new CommandFactory();
		}
		return instance;
	}


	private Optional<SerialDescription> getSerialDescription(final ECommand ecmd)
	{
		SerialDescription desc;
		try
		{
			desc = new SerialDescription(ecmd.getInstanceableClass().getImpl());

			// do sanity checks for encode and decode - should not throw any exception
			desc.decode(desc.encode(desc.newInstance()));
		} catch (SerialException err)
		{
			log.error("Could not load command: {}", ecmd, err);
			return Optional.empty();
		}

		ACommand acmd;
		try
		{
			acmd = (ACommand) desc.newInstance();
		} catch (SerialException err)
		{
			log.error("Could not create instance of: {}", ecmd, err);
			return Optional.empty();
		} catch (ClassCastException err)
		{
			log.error("{} is not based on ACommand!", ecmd, err);
			return Optional.empty();
		}

		if (acmd.getType().getId() != ecmd.getId())
		{
			log.error("ECommand id mismatch in command: {}. The command does not use the correct enum.", ecmd);
			return Optional.empty();
		}

		if (commands.get(ecmd.getId()) != null)
		{
			log.error("{}'s command code is already defined by: {}", ecmd,
					commands.get(ecmd.getId()).getClass().getName());
			return Optional.empty();
		}
		return Optional.of(desc);
	}


	/**
	 * Parse byte data and create command.
	 *
	 * @param data
	 * @return
	 */
	public ACommand decode(final byte[] data)
	{
		int cmdId = SerialByteConverter.byteArray2UByte(data, 0);
		boolean reliable = false;
		int seq = 0;

		if ((cmdId & RELIABLE_CMD_MASK) == RELIABLE_CMD_MASK)
		{
			reliable = true;
			cmdId &= ~RELIABLE_CMD_MASK;
			seq = SerialByteConverter.byteArray2UShort(data, HEADER_LENGTH);
		}

		if (!commands.containsKey(cmdId))
		{
			log.debug("Unknown command: {}, length: {}", cmdId, data.length);
			return null;
		}

		byte[] cmdData;

		if (reliable)
		{
			cmdData = new byte[data.length - RELIABLE_HEADER_LENGTH];
			System.arraycopy(data, RELIABLE_HEADER_LENGTH, cmdData, 0, data.length - RELIABLE_HEADER_LENGTH);
		} else
		{
			cmdData = new byte[data.length - HEADER_LENGTH];
			System.arraycopy(data, HEADER_LENGTH, cmdData, 0, data.length - HEADER_LENGTH);
		}

		SerialDescription cmdDesc = commands.get(cmdId);

		ACommand acmd;

		try
		{
			acmd = (ACommand) cmdDesc.decode(cmdData);
			if (reliable)
			{
				acmd.setReliable(true);
				acmd.setSeq(seq);
			}
			int cmdDataLen = cmdDesc.getLength(acmd);
			if (cmdDataLen != cmdData.length)
			{
				String cmdStr = String.format("0x%x", cmdId);
				StringBuilder sb = new StringBuilder();
				for (byte b : cmdData)
				{
					sb.append(String.format("%02x ", b));
				}
				log.debug("Command {} did not parse all data ({}} used of {} available) Data: {}",
						cmdStr, cmdDataLen, cmdData.length, sb);
			}
		} catch (SerialException err)
		{
			log.info("Could not parse cmd: {}", cmdId, err);
			return null;
		}

		return acmd;
	}


	/**
	 * Encode command in byte stream.
	 *
	 * @param cmd
	 * @return
	 */
	public byte[] encode(final ACommand cmd)
	{
		int cmdId = cmd.getType().getId();

		if (!commands.containsKey(cmdId))
		{
			log.error("No description for command: {}", cmdId);
			return new byte[0];
		}

		SerialDescription cmdDesc = commands.get(cmdId);

		byte[] cmdData;
		try
		{
			cmdData = cmdDesc.encode(cmd);
		} catch (SerialException err)
		{
			log.error("Could not encode command: {}", cmdId, err);
			return new byte[0];
		}

		byte[] data;

		if (cmd.isReliable())
		{
			data = new byte[cmdData.length + RELIABLE_HEADER_LENGTH];

			cmdId |= RELIABLE_CMD_MASK;

			SerialByteConverter.byte2ByteArray(data, 0, cmdId);
			SerialByteConverter.short2ByteArray(data, HEADER_LENGTH, cmd.getSeq());

			System.arraycopy(cmdData, 0, data, RELIABLE_HEADER_LENGTH, cmdData.length);
		} else
		{
			data = new byte[cmdData.length + HEADER_LENGTH];

			SerialByteConverter.byte2ByteArray(data, 0, cmdId);

			System.arraycopy(cmdData, 0, data, HEADER_LENGTH, cmdData.length);
		}

		return data;
	}


	/**
	 * @param cmd
	 * @return
	 */
	public int getLength(final ACommand cmd)
	{
		int length;
		int cmdId = cmd.getType().getId();

		if (!commands.containsKey(cmdId))
		{
			log.error("No description for command: {}", cmdId);
			return 0;
		}

		SerialDescription cmdDesc = commands.get(cmdId);

		try
		{
			length = cmdDesc.getLength(cmd);
		} catch (SerialException err)
		{
			log.error("Could not get length of: {}", cmd.getType(), err);
			return 0;
		}

		if (cmd.isReliable())
		{
			length += RELIABLE_HEADER_LENGTH;
		} else
		{
			length += HEADER_LENGTH;
		}

		return length;
	}
}
