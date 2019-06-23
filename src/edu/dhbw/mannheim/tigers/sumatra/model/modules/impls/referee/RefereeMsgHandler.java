/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.01.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;


/**
 * This class provides methods to:<br>
 * - {@link #build} data packets out of {@link RefereeMsg}<br>
 * - {@link #translate} given data packets and return a new {@link RefereeMsg}<br>
 * Data packets are treated according to 
 * <a href = http://small-size.informatik.uni-bremen.de/referee:protocol>that</a> protocol.
 * 
 * 
 * @author Malte, Dion, Gero
 * 
 */
public class RefereeMsgHandler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected static final Logger	LOG	= Logger.getLogger("RefereeMsgHandler");
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public static byte[] build(RefereeMsg msg)
	{
		byte idBytes = sIntTouByte(msg.id);
		byte goalEnemyBytes = sIntTouByte(msg.goalsEnemies);
		byte goalTigerBytes = sIntTouByte(msg.goalsTigers);
		
		byte[] packet = { RefereeMap.getByte(msg.cmd), idBytes, goalEnemyBytes, goalTigerBytes, 0,
				(byte) msg.timeRemaining };
		return packet;
	}
	

	private static byte sIntTouByte(int sInt)
	{
		// Okay, seems as if the cast just cuts, without any extra-handling for the sign-bit, perfect! =)
		// int offset = sInt & 0x80; // Detect whether the critical (byte-sign) bit is set
		byte uByte = (byte) sInt; // Cut
		// uByte &= 0x7F; // Empty critical bit
		// uByte |= offset; // Fill it with the correct information
		return uByte;
	}
	
	public static RefereeMsg translate(byte[] packet, boolean weAreYellow)
	{
		// --- putting the remaining time and the command counter ---
		short timeRemaining = (short) ((packet[4] << 8) + (packet[5] & 0xff));
		byte idBytes = packet[1];
		

		// --- putting the goals (yellow/blue) ---
		byte tigerGoalBytes;
		byte enemyGoalBytes;
		
		if (weAreYellow)
		{
			tigerGoalBytes = packet[3];
			enemyGoalBytes = packet[2];
		} else
		{
			tigerGoalBytes = packet[2];
			enemyGoalBytes = packet[3];
		}
		
		// --- convert byte (unsigned) to int (Does NOT respect byte-order!)
		int goalsTigers = uByteTosInt(tigerGoalBytes);
		int goalsEnemies = uByteTosInt(enemyGoalBytes);
		int id = uByteTosInt(idBytes);
		

		// --- handles all non-color-sensitive commands ---
		ERefereeCommand cmd = RefereeMap.getCommand(packet[0]);
		
		if (RefereeMap.nonColorSensitiveCmds.contains(cmd)) // Message is a non-color-sensitive one, so we can return it here!
		{
			return new RefereeMsg(id, cmd, goalsTigers, goalsEnemies, timeRemaining);
		}
		
		// --- switches the upper/lower-case letters (ascii) if we are not yellow ---
		// Conversion of (xxxblue, xxxyellow) to (xxxtigers, xxxenemies)
		if (!weAreYellow)
		{
			if (packet[0] < 96)
			{
				packet[0] += 32;
			} else
			{
				packet[0] -= 32;
			}
		}
		
		// --- handles all colour-sensitive commands, as if we were yellow ---
		cmd = RefereeMap.getCommand(packet[0]);		

		// --- no run should ever reach this far ---
		return new RefereeMsg(id, cmd, goalsTigers, goalsEnemies, timeRemaining);
	}
	
	
	private static int uByteTosInt(byte uByte) {
		int offset = uByte & 0x80; // 0 or 128
		uByte &= 0x7f; // Remove first bit, which is our sign (Java) or extra-unsigned-bit (C)
		return uByte + offset;
	}

	

}
