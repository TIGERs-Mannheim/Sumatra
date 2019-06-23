/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.01.2011
 * Author(s): Dion
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;

import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.BeginFirstHalf;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.BeginHalfTime;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.BeginOvertimeHalf1;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.BeginOvertimeHalf2;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.BeginPenaltyShootout;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.BeginSecondHalf;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.Cancel;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.DecreaseGoalScoreEnemies;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.DecreaseGoalScoreTigers;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.DirectFreeKickEnemies;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.DirectFreeKickTigers;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.GoalScoredEnemies;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.GoalScoredTigers;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.Halt;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.IndirectFreeKickEnemies;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.IndirectFreeKickTigers;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.KickOffEnemies;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.KickOffTigers;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.PenaltyEnemies;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.PenaltyTigers;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.Ready;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.RedCardEnemies;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.RedCardTigers;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.Start;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.Stop;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.TimeUpdate;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.TimeoutEnd;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.TimeoutEnemies;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.TimeoutTigers;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.YellowCardEnemies;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand.YellowCardTigers;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand;
import edu.dhbw.mannheim.tigers.sumatra.util.collection.BijectiveHashMap;
/**
 * 
 * A Map to convert ERefereeCommand to byte or Character
 * and the other way around.
 * <br>
 * <b>getCommand:</b> returns the ERefereeCommand from the given byte-code<br>
 * <b>getCharacter:</b> returns the Character of a Command from the Command<br>
 * <b>getByte:</b> returns the byte-code from a ERefereeCommand<br>
 * 
 * @author Dion
 * 
 */
public class RefereeMap
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final static BijectiveHashMap<Character, ERefereeCommand>	map;
	
	/** List where all commands are stored that are not color sensitive. */
	public static final ArrayList<ERefereeCommand> nonColorSensitiveCmds;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	static
	{
		map = new BijectiveHashMap<Character, ERefereeCommand>();
		map.put('H', Halt);
		map.put('S', Stop);
		map.put(' ', Ready);
		map.put('s', Start);
		map.put('1', BeginFirstHalf);
		map.put('h', BeginHalfTime);
		map.put('2', BeginSecondHalf);
		map.put('o', BeginOvertimeHalf1);
		map.put('O', BeginOvertimeHalf2);
		map.put('a', BeginPenaltyShootout);
		map.put('k', KickOffTigers);
		map.put('K', KickOffEnemies);
		map.put('p', PenaltyTigers);
		map.put('P', PenaltyEnemies);
		map.put('f', DirectFreeKickTigers);
		map.put('F', DirectFreeKickEnemies);
		map.put('i', IndirectFreeKickTigers);
		map.put('I', IndirectFreeKickEnemies);
		map.put('t', TimeoutTigers);
		map.put('T', TimeoutEnemies);
		map.put('z', TimeoutEnd);
		map.put('g', GoalScoredTigers);
		map.put('G', GoalScoredEnemies);
		map.put('d', DecreaseGoalScoreTigers);
		map.put('D', DecreaseGoalScoreEnemies);
		map.put('y', YellowCardTigers);
		map.put('Y', YellowCardEnemies);
		map.put('r', RedCardTigers);
		map.put('R', RedCardEnemies);
		map.put('c', Cancel);
		map.put('#', TimeUpdate);
		
		nonColorSensitiveCmds = new ArrayList<ERefereeCommand>();
		nonColorSensitiveCmds.add(Halt);
		nonColorSensitiveCmds.add(Stop);
		nonColorSensitiveCmds.add(Ready);
		nonColorSensitiveCmds.add(Start);
		nonColorSensitiveCmds.add(BeginFirstHalf);
		nonColorSensitiveCmds.add(BeginHalfTime);
		nonColorSensitiveCmds.add(BeginSecondHalf);
		nonColorSensitiveCmds.add(BeginOvertimeHalf1);
		nonColorSensitiveCmds.add(BeginOvertimeHalf2);
		nonColorSensitiveCmds.add(BeginPenaltyShootout);
		nonColorSensitiveCmds.add(Cancel);
		nonColorSensitiveCmds.add(TimeUpdate);

	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Returns a ERefereeCommand to the given, related byte.
	 * 
	 * @param code
	 * @return ERefereeCommand
	 */
	public static ERefereeCommand getCommand(byte code)
	{
		ERefereeCommand com = map.getValue( (char)code );
		if(com == null)
		{
			return ERefereeCommand.UnknownCommand;
		}
		else
		{
			return com;
		}	
	}
	

	/**
	 * Returns a Character to the given, related ERefereeCommand.
	 * 
	 * @param command
	 * @return Character
	 */
	public static Character getCharacter(ERefereeCommand command)
	{
		Character c = map.getKey(command);
		if(c == null)
		{
			return 0;
		}
		else
		{
			return c;
		}
	}
	

	/**
	 * Returns a byte to the given, related ERefereeCommand.
	 * 
	 * @param command
	 * @return byte
	 */
	public static byte getByte(ERefereeCommand command)
	{
		Character b = map.getKey(command);
		if(b == null)
		{
			return 0;
		}
		else
		{
			return (byte)(char) b;
		}
	}
}
