/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 23, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Possible game states which depend on incoming referee messages and some other constraints
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EGameStateNeutral
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
	TIMEOUT_YELLOW,
	/**  */
	TIMEOUT_BLUE,
	
	/**  */
	PREPARE_KICKOFF_YELLOW,
	/**  */
	PREPARE_KICKOFF_BLUE,
	
	/**  */
	KICKOFF_YELLOW,
	/**  */
	KICKOFF_BLUE,
	
	/**  */
	PREPARE_PENALTY_YELLOW,
	/**  */
	PREPARE_PENALTY_BLUE,
	
	/**  */
	PENALTY_YELLOW,
	/**  */
	PENALTY_BLUE,
	
	// /** */
	// THROW_IN_YELLOW,
	// /** */
	// THROW_IN_BLUE,
	//
	// /** */
	// CORNER_KICK_YELLOW,
	// /** */
	// CORNER_KICK_BLUE,
	//
	// /** */
	// GOAL_KICK_YELLOW,
	// /** */
	// GOAL_KICK_BLUE,
	
	/**  */
	INDIRECT_KICK_YELLOW,
	/**  */
	INDIRECT_KICK_BLUE,
	
	/** A direct free kick within the field */
	DIRECT_KICK_YELLOW,
	/** A direct free kick within the field */
	DIRECT_KICK_BLUE,
	
	/**  */
	BALL_PLACEMENT_YELLOW,
	
	/**  */
	BALL_PLACEMENT_BLUE,
	
	/**  */
	BREAK,
	/**  */
	POST_GAME, ;
	
	/**
	 * @param color
	 * @return
	 */
	public static EGameStateNeutral getKickoff(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return PREPARE_KICKOFF_BLUE;
			case YELLOW:
				return PREPARE_KICKOFF_YELLOW;
			default:
				throw new IllegalArgumentException("Invalid color: " + color);
		}
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static EGameStateNeutral getPreparePenalty(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return PREPARE_PENALTY_BLUE;
			case YELLOW:
				return PREPARE_PENALTY_YELLOW;
			default:
				throw new IllegalArgumentException("Invalid color: " + color);
		}
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static EGameStateNeutral getPenalty(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return PENALTY_BLUE;
			case YELLOW:
				return PENALTY_YELLOW;
			default:
				throw new IllegalArgumentException("Invalid color: " + color);
		}
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static EGameStateNeutral getDirectKick(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return DIRECT_KICK_BLUE;
			case YELLOW:
				return DIRECT_KICK_YELLOW;
			default:
				throw new IllegalArgumentException("Invalid color: " + color);
		}
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static EGameStateNeutral getTimeout(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return TIMEOUT_BLUE;
			case YELLOW:
				return TIMEOUT_YELLOW;
			default:
				throw new IllegalArgumentException("Invalid color: " + color);
		}
	}
	
	
	/**
	 * Returns the Ball Placement state for the specified team color
	 * 
	 * @param color color of the team
	 * @throws IllegalArgumentException if the color is not BLUE or YELLOW
	 * @return the Ball Placement state for the specified team color
	 */
	public static EGameStateNeutral getBallPlacement(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return BALL_PLACEMENT_BLUE;
			case YELLOW:
				return BALL_PLACEMENT_YELLOW;
			default:
				throw new IllegalArgumentException("Invalid team color for ball placement: " + color);
		}
	}
	
	
	/**
	 * Returns true if the supplied {@code state} is either {@link #BALL_PLACEMENT_BLUE} or
	 * {@link #BALL_PLACEMENT_YELLOW}
	 * 
	 * @param state
	 * @return
	 */
	public static boolean isBallPlacement(final EGameStateNeutral state)
	{
		return (state == BALL_PLACEMENT_BLUE) || (state == BALL_PLACEMENT_YELLOW);
	}
	
	
	/**
	 * @See #isBallPlacement(EGameStateNeutral)
	 * @return
	 */
	public boolean isBallPlacement()
	{
		return isBallPlacement(this);
	}
	
	
	/**
	 * Returns true if the specified {@code state} is a {@link #DIRECT_KICK_BLUE} or {@link #DIRECT_KICK_YELLOW}
	 * 
	 * @param state
	 * @return
	 */
	public static boolean isDirectKick(final EGameStateNeutral state)
	{
		return (state == DIRECT_KICK_BLUE) || (state == DIRECT_KICK_YELLOW);
	}
	
	
	/**
	 * @see #isDirectKick(EGameStateNeutral)
	 * @return
	 */
	public boolean isDirectKick()
	{
		return isDirectKick(this);
	}
	
	
	/**
	 * Returns true if the specified {@code state} is an {@link #INDIRECT_KICK_BLUE} or {@link #INDIRECT_KICK_YELLOW}
	 * 
	 * @param state
	 * @return
	 */
	public static boolean isIndirectKick(final EGameStateNeutral state)
	{
		return (state == INDIRECT_KICK_BLUE) || (state == INDIRECT_KICK_YELLOW);
	}
	
	
	/**
	 * @see #isIndirectKick(EGameStateNeutral)
	 * @return
	 */
	public boolean isIndirectKick()
	{
		return isIndirectKick(this);
	}
	
	
	/**
	 * Returns true if the specified {@code state} is a {@link #KICKOFF_BLUE} or {@link #KICKOFF_YELLOW}
	 * 
	 * @param state
	 * @return
	 */
	public static boolean isKickOff(final EGameStateNeutral state)
	{
		return (state == KICKOFF_BLUE) || (state == KICKOFF_YELLOW);
	}
	
	
	/**
	 * @see #isKickOff(EGameStateNeutral)
	 * @return
	 */
	public boolean isKickOff()
	{
		return isKickOff(this);
	}
	
	
	/**
	 * Returns true if the specified {@code state} is either {@link #PENALTY_BLUE} or {@link #PENALTY_YELLOW}
	 * 
	 * @param state
	 * @return
	 */
	public static boolean isPenalty(final EGameStateNeutral state)
	{
		return (state == PENALTY_BLUE) || (state == PENALTY_YELLOW);
	}
	
	
	/**
	 * @see #isPenalty(EGameStateNeutral)
	 * @return
	 */
	public boolean isPenalty()
	{
		return isPenalty(this);
	}
	
	
	/**
	 * Returns true if the specified {@code state} is either {@link #PENALTY_BLUE} or {@link #PENALTY_YELLOW}
	 * 
	 * @param state
	 * @return
	 */
	public static boolean isTimeout(final EGameStateNeutral state)
	{
		return (state == TIMEOUT_BLUE) || (state == TIMEOUT_YELLOW);
	}
	
	
	/**
	 * @see #isPenalty(EGameStateNeutral)
	 * @return
	 */
	public boolean isTimeout()
	{
		return isTimeout(this);
	}
	
	
	/**
	 * Returns the color of the supplied state
	 * Example:
	 * KICKOFF_BLUE -> Blue
	 * INDIRECT_YELLOW -> Yellow
	 * HALTED -> Neutral
	 * 
	 * @param state
	 * @return blue, yellow or neutral
	 */
	public static ETeamColor getTeamColorOfState(final EGameStateNeutral state)
	{
		switch (state)
		{
			case BALL_PLACEMENT_BLUE:
			case DIRECT_KICK_BLUE:
			case INDIRECT_KICK_BLUE:
			case KICKOFF_BLUE:
			case PENALTY_BLUE:
			case PREPARE_KICKOFF_BLUE:
			case PREPARE_PENALTY_BLUE:
			case TIMEOUT_BLUE:
				return ETeamColor.BLUE;
			case BALL_PLACEMENT_YELLOW:
			case DIRECT_KICK_YELLOW:
			case INDIRECT_KICK_YELLOW:
			case KICKOFF_YELLOW:
			case PENALTY_YELLOW:
			case PREPARE_KICKOFF_YELLOW:
			case PREPARE_PENALTY_YELLOW:
			case TIMEOUT_YELLOW:
				return ETeamColor.YELLOW;
			case BREAK:
			case HALTED:
			case POST_GAME:
			case RUNNING:
			case STOPPED:
			case UNKNOWN:
				return ETeamColor.NEUTRAL;
			default:
				throw new IllegalArgumentException("Please add gamestate \"" + state + "\" to this switch case");
		}
	}
	
	
	/**
	 * See {@link #getTeamColorOfState(EGameStateNeutral)}
	 * 
	 * @return
	 */
	public ETeamColor getTeamColor()
	{
		return getTeamColorOfState(this);
	}
}
