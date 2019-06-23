/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.04.2014
 * Author(s): jaue_ma
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


/**
 * TODO jaue_ma, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author jaue_ma
 */
public class RoleFinder
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	enum Flags
	{
		ENFORCE_KEEPER,
		NO_OFFENSE_BOT,
		PREFER_DEFENSE,
		NO_DEFENSE_BOTS
	}
	
	private Map<Flags, Boolean>	staticFlags;
	private Map<Flags, Boolean>	dynamicFlags;
	private EGameState				currentState;
	private int							countBotsAvailable;
	
	
	/**
	 * 
	 */
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	public RoleFinder()
	{
		
		staticFlags = new HashMap<Flags, Boolean>();
		dynamicFlags = new HashMap<Flags, Boolean>();
		
		unsetFlags(false, Flags.values());
		unsetFlags(true, Flags.values());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Computes the Roles in Plays for the current game situation.
	 * 
	 * @param plays
	 * @param frame
	 */
	public void computeRoles(final List<APlay> plays, final AthenaAiFrame frame)
	{
		
		// if GameState changed, update Flags to the default for the new state.
		EGameState newState = frame.getTacticalField().getGameState();
		if (newState != currentState)
		{
			updateFlags(newState);
			currentState = newState;
		}
		
		countBotsAvailable = frame.getWorldFrame().getTigerBotsAvailable().size();
		
		if (plays.size() == 1) // only one play --> no normal game situation
		{
			APlay play = plays.get(0);
			if ((play.getType() != EPlay.OFFENSIVE) && (play.getType() != EPlay.DEFENSIVE)
					&& (play.getType() != EPlay.KEEPER))
			{
				adjustNumRoles(plays.get(0), countBotsAvailable);
			}
			return;
		}
		
		if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(frame.getWorldFrame().getBall().getPos()))
		{
			setFlags(false, Flags.NO_OFFENSE_BOT);
		} else
		{
			unsetFlags(false, Flags.NO_OFFENSE_BOT);
		}
		
		if (frame.getWorldFrame().getBall().getPos().x() > 0)
		{
			unsetFlags(true, Flags.ENFORCE_KEEPER);
		}
		
		int keeperRoleCount = getDesiredRoleCount(EPlay.KEEPER);
		adjustNumRoles(getPlay(plays, EPlay.KEEPER), keeperRoleCount);
		
		int offenseRoleCount = getDesiredRoleCount(EPlay.OFFENSIVE);
		adjustNumRoles(getPlay(plays, EPlay.OFFENSIVE), offenseRoleCount);
		
		int defenseRoleCount = getDesiredRoleCount(EPlay.DEFENSIVE);
		adjustNumRoles(getPlay(plays, EPlay.DEFENSIVE), defenseRoleCount);
		
		int supportRoleCount = 0;
		if (currentState != EGameState.PREPARE_PENALTY_WE)
		{
			supportRoleCount = countBotsAvailable - offenseRoleCount - defenseRoleCount - keeperRoleCount;
		}
		adjustNumRoles(getPlay(plays, EPlay.SUPPORT), supportRoleCount);
		
	}
	
	
	private void updateFlags(final EGameState state)
	{
		unsetFlags(true, Flags.values());
		
		
		switch (state)
		{
			case CORNER_KICK_THEY:
			case GOAL_KICK_THEY:
				setFlags(true, Flags.NO_OFFENSE_BOT, Flags.ENFORCE_KEEPER);
				break;
			case PREPARE_PENALTY_THEY:
				setFlags(true, Flags.NO_OFFENSE_BOT, Flags.NO_DEFENSE_BOTS, Flags.ENFORCE_KEEPER);
				break;
			case PREPARE_PENALTY_WE:
				unsetFlags(true, Flags.NO_OFFENSE_BOT);
				break;
			case PREPARE_KICKOFF_THEY:
			case DIRECT_KICK_THEY:
			case THROW_IN_THEY:
				setFlags(true, Flags.ENFORCE_KEEPER);
			case STOPPED:
				setFlags(true, Flags.NO_OFFENSE_BOT);
				break;
			case PREPARE_KICKOFF_WE:
			case CORNER_KICK_WE:
			case DIRECT_KICK_WE:
			case GOAL_KICK_WE:
			case HALTED:
			case RUNNING:
			case THROW_IN_WE:
			case TIMEOUT_THEY:
			case TIMEOUT_WE:
			case UNKNOWN:
			default:
				break;
		
		}
	}
	
	
	/**
	 * TODO jaue_ma, add comment!
	 * 
	 * @param isStatic
	 * @param flagsToSet
	 */
	public void setFlags(final boolean isStatic, final Flags... flagsToSet)
	{
		for (Flags flag : flagsToSet)
		{
			if (isStatic)
			{
				staticFlags.put(flag, true);
			} else
			{
				dynamicFlags.put(flag, true);
			}
		}
		
	}
	
	
	/**
	 * @param f
	 * @return
	 */
	public boolean isFlagSet(final Flags f)
	{
		return (staticFlags.get(f) || dynamicFlags.get(f));
	}
	
	
	/**
	 * TODO jaue_ma, add comment!
	 * 
	 * @param isStatic
	 * @param flagsToUnSet
	 */
	public void unsetFlags(final boolean isStatic, final Flags... flagsToUnSet)
	{
		for (Flags flag : flagsToUnSet)
		{
			if (isStatic)
			{
				staticFlags.put(flag, false);
			} else
			{
				dynamicFlags.put(flag, false);
			}
		}
	}
	
	
	// Do NOT call for Support role;
	private int getDesiredRoleCount(final EPlay playType)
	{
		
		int count = 0;
		switch (playType)
		{
		
			case OFFENSIVE:
				if (((countBotsAvailable > 4) || !isFlagSet(Flags.PREFER_DEFENSE))
						&& !isFlagSet(Flags.NO_OFFENSE_BOT))
				{
					count = 1;
				} else
				{
					count = 0;
				}
				break;
			
			case DEFENSIVE:
				switch (countBotsAvailable)
				{
					case 0:
					case 1:
					case 2:
						count = 0;
						break;
					case 3:
						count = 1;
						break;
					case 4:
						if (isFlagSet(Flags.PREFER_DEFENSE))
						{
							count = 2;
						} else
						{
							count = 1;
						}
						break;
					default:
						if (isFlagSet(Flags.PREFER_DEFENSE))
						{
							count = 3;
						} else
						{
							count = 2;
						}
						break;
				}
				if (isFlagSet(Flags.NO_DEFENSE_BOTS))
				{
					count = 0;
				}
				break;
			case KEEPER:
				if ((countBotsAvailable <= 1) && !isFlagSet(Flags.ENFORCE_KEEPER) && !isFlagSet(Flags.PREFER_DEFENSE)
						&& !isFlagSet(Flags.NO_OFFENSE_BOT))
				{
					count = 0;
				} else
				{
					count = 1;
				}
				break;
			case MAINTENANCE:
			case CHEERING:
				count = countBotsAvailable;
				break;
			default:
				break;
		}
		return count;
	}
	
	
	private void adjustNumRoles(final APlay play, final int num)
	{
		if (play == null)
		{
			return;
		}
		if (play.getType().equals(EPlay.GUI_TEST))
		{
			return;
		}
		
		if (play.getRoles().size() > num)
		{
			play.removeRoles(play.getRoles().size() - num);
		} else if (play.getRoles().size() < num)
		{
			play.addRoles(num - play.getRoles().size());
		}
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private APlay getPlay(final List<APlay> plays, final EPlay playType)
	{
		
		for (APlay p : plays)
		{
			if (p.getType() == playType)
			{
				return p;
			}
		}
		
		return null;
	}
}