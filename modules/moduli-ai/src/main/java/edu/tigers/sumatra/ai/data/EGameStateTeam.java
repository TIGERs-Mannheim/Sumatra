/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 23, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Possible game states which depend on incoming referee messages and some other constraints
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EGameStateTeam
{
	/**  */
	UNKNOWN,
	
	/**  */
	HALTED,
	/**  */
	STOPPED,
	/**  */
	RUNNING,
	
	/**  */
	TIMEOUT_WE,
	/**  */
	TIMEOUT_THEY,
	
	/**  */
	PREPARE_KICKOFF_WE,
	/**  */
	PREPARE_KICKOFF_THEY,
	
	/**  */
	PREPARE_PENALTY_WE,
	/**  */
	PREPARE_PENALTY_THEY,
	
	/**  */
	THROW_IN_WE,
	/**  */
	THROW_IN_THEY,
	
	/**  */
	CORNER_KICK_WE,
	/**  */
	CORNER_KICK_THEY,
	
	/**  */
	GOAL_KICK_WE,
	/**  */
	GOAL_KICK_THEY,
	
	/** A direct free kick within the field */
	DIRECT_KICK_WE,
	/** A direct free kick within the field */
	DIRECT_KICK_THEY,
	
	/**  */
	BALL_PLACEMENT_WE,
	
	/**  */
	BALL_PLACEMENT_THEY,
	
	/**  */
	BREAK,
	/**  */
	POST_GAME,;
	
	
	/**
	 * Get the required ball position for this game state
	 * 
	 * @param wf
	 * @param tacticalField
	 * @return
	 */
	public IVector2 getRequiredBallPos(final WorldFrame wf, final ITacticalField tacticalField)
	{
		IVector2 marker = null;
		double fLength = Geometry.getFieldLength();
		double fWidth = Geometry.getFieldWidth();
		int ballSide = wf.getBall().getPos().y() > 0 ? 1 : -1;
		switch (this)
		{
			case CORNER_KICK_THEY:
				marker = new Vector2(-((fLength / 2.0) - 100), ballSide * ((fWidth / 2.0) - 100));
				break;
			case CORNER_KICK_WE:
				marker = new Vector2((fLength / 2.0) - 100, ballSide * ((fWidth / 2.0) - 100));
				break;
			case GOAL_KICK_THEY:
				marker = new Vector2(((fLength / 2.0) - 500), ballSide * ((fWidth / 2.0) - 100));
				break;
			case GOAL_KICK_WE:
				marker = new Vector2(-((fLength / 2.0) - 500), ballSide * ((fWidth / 2.0) - 100));
				break;
			case DIRECT_KICK_THEY:
			case DIRECT_KICK_WE:
			case STOPPED:
				marker = wf.getBall().getPos();
				break;
			case THROW_IN_THEY:
			case THROW_IN_WE:
				if (tacticalField.getBallLeftFieldPos() != null)
				{
					marker = tacticalField.getBallLeftFieldPos().addNew(new Vector2(0, -ballSide * 100));
				}
				break;
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
				marker = Geometry.getCenter();
				break;
			default:
				break;
		}
		return marker;
	}
	
	
	/**
	 * @param gsn
	 * @param ourTeam
	 * @return
	 */
	public static EGameStateTeam fromNeutral(final EGameStateNeutral gsn, final ETeamColor ourTeam)
	{
		switch (ourTeam)
		{
			case BLUE:
				switch (gsn)
				{
					case BALL_PLACEMENT_BLUE:
						return EGameStateTeam.BALL_PLACEMENT_WE;
					case BALL_PLACEMENT_YELLOW:
						return EGameStateTeam.BALL_PLACEMENT_THEY;
					case BREAK:
						return EGameStateTeam.BREAK;
					case DIRECT_KICK_BLUE:
						return EGameStateTeam.DIRECT_KICK_WE;
					case DIRECT_KICK_YELLOW:
						return EGameStateTeam.DIRECT_KICK_THEY;
					case HALTED:
						return EGameStateTeam.HALTED;
					case INDIRECT_KICK_BLUE:
						return EGameStateTeam.THROW_IN_WE;
					case INDIRECT_KICK_YELLOW:
						return EGameStateTeam.THROW_IN_THEY;
					case POST_GAME:
						return EGameStateTeam.POST_GAME;
					case PREPARE_KICKOFF_BLUE:
					case KICKOFF_BLUE:
						return EGameStateTeam.PREPARE_KICKOFF_WE;
					case PREPARE_KICKOFF_YELLOW:
					case KICKOFF_YELLOW:
						return EGameStateTeam.PREPARE_KICKOFF_THEY;
					case PREPARE_PENALTY_BLUE:
					case PENALTY_BLUE:
						return EGameStateTeam.PREPARE_PENALTY_WE;
					case PREPARE_PENALTY_YELLOW:
					case PENALTY_YELLOW:
						return EGameStateTeam.PREPARE_PENALTY_THEY;
					case RUNNING:
						return EGameStateTeam.RUNNING;
					case STOPPED:
						return EGameStateTeam.STOPPED;
					case TIMEOUT_BLUE:
						return EGameStateTeam.TIMEOUT_WE;
					case TIMEOUT_YELLOW:
						return EGameStateTeam.TIMEOUT_THEY;
					case UNKNOWN:
						return EGameStateTeam.UNKNOWN;
				}
				throw new IllegalStateException();
			case YELLOW:
				switch (gsn)
				{
					case BALL_PLACEMENT_BLUE:
						return EGameStateTeam.BALL_PLACEMENT_THEY;
					case BALL_PLACEMENT_YELLOW:
						return EGameStateTeam.BALL_PLACEMENT_WE;
					case BREAK:
						return EGameStateTeam.BREAK;
					case DIRECT_KICK_BLUE:
						return EGameStateTeam.DIRECT_KICK_THEY;
					case DIRECT_KICK_YELLOW:
						return EGameStateTeam.DIRECT_KICK_WE;
					case HALTED:
						return EGameStateTeam.HALTED;
					case INDIRECT_KICK_BLUE:
						return EGameStateTeam.THROW_IN_THEY;
					case INDIRECT_KICK_YELLOW:
						return EGameStateTeam.THROW_IN_WE;
					case POST_GAME:
						return EGameStateTeam.POST_GAME;
					case PREPARE_KICKOFF_BLUE:
					case KICKOFF_BLUE:
						return EGameStateTeam.PREPARE_KICKOFF_THEY;
					case PREPARE_KICKOFF_YELLOW:
					case KICKOFF_YELLOW:
						return EGameStateTeam.PREPARE_KICKOFF_WE;
					case PREPARE_PENALTY_BLUE:
					case PENALTY_BLUE:
						return EGameStateTeam.PREPARE_PENALTY_THEY;
					case PREPARE_PENALTY_YELLOW:
					case PENALTY_YELLOW:
						return EGameStateTeam.PREPARE_PENALTY_WE;
					case RUNNING:
						return EGameStateTeam.RUNNING;
					case STOPPED:
						return EGameStateTeam.STOPPED;
					case TIMEOUT_BLUE:
						return EGameStateTeam.TIMEOUT_THEY;
					case TIMEOUT_YELLOW:
						return EGameStateTeam.TIMEOUT_WE;
					case UNKNOWN:
						return EGameStateTeam.UNKNOWN;
				}
				throw new IllegalStateException();
			case NEUTRAL:
			case UNINITIALIZED:
			default:
				throw new IllegalArgumentException();
		}
	}
}
