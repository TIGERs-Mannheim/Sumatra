/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;


/**
 * Enumeration that represents the different {@link APlay}.
 * <p>
 * <b>Important:</b> Don't forget to add your play in the {@link PlayFactory} if you wrote a new one!!!
 * </p>
 * 
 * @author Gero
 */
public enum EPlay
{
	// ##### OFFENSE
	
	// attack
	BALL_GETTING(Type.TEST),
	DIRECT_SHOT(Type.TEST),
	BALL_GETTING_AND_IMMEDIATE_SHOT,
	INDIRECT_SHOT(Type.TEST),
	GAME_OFFENSE_PREPARE_WITH_THREE(Type.TEST),
	GAME_OFFENSE_PREPARE_WITH_TWO(Type.TEST),
	PASS_FORWARD(Type.TEST),
	
	BALLCAPTURING_WITH_DOUBLING(Type.TEST),
	BALLCAPTURING_WITH_ONE_PASS_BLOCKER(Type.TEST),
	BALLWINNING_WITH_ONE_BLOCKER(Type.TEST),
	BALLWINNING_WITH_ONE_PASS_BLOCKER(Type.TEST),
	
	// support
	POSITION_IMPROVING_NO_BALL_WITH_ONE(Type.TEST),
	POSITION_IMPROVING_NO_BALL_WITH_TWO(Type.TEST),
	SUPPORT_WITH_ONE_BLOCKER(Type.TEST),
	SUPPORT_WITH_ONE_PASS_BLOCKER(Type.TEST),
	SUPPORT_WITH_ONE_MARKER,
	PULL_BACK(Type.TEST),
	
	// ##### DEFENSE
	KEEPER_SOLO,
	KEEPER_PLUS_1_DEFENDER,
	KEEPER_PLUS_2_DEFENDER,
	MAN_TO_MAN_MARKER(Type.TEST),
	
	// ##### STANDARD
	HALT(Type.STANDARD),
	INIT(Type.STANDARD),
	INIT4(Type.STANDARD),
	POSITIONING_ON_STOPPED_PLAY_WITH_THREE(Type.STANDARD),
	POSITIONING_ON_STOPPED_PLAY_WITH_TWO(Type.STANDARD),
	
	// free kick
	FREEKICK_WITH_TWO(Type.TEST),
	FREEKICK_MARKER(Type.STANDARD),
	FREEKICK_V2(Type.STANDARD),
	
	// kick-off
	POSITIONING_ON_KICK_OFF_THEM(Type.STANDARD),
	KICK_OF_US_SYMMETRY_POSITION(Type.STANDARD),
	STOP_MOVE(Type.STANDARD),
	STOP_MARKER(Type.STANDARD),
	
	// penalty kick
	PENALTY_US(Type.STANDARD),
	PENALTY_THEM(Type.STANDARD),
	

	// ##### TEST
	
	PASS_TO_KEEPER(Type.TEST),
	PASS_TWO_BOTS(Type.TEST),
	SHOOT_THREE_BOTS(Type.TEST),
	PASS_TRAINING(Type.TEST),
	

	ONE_BOT_TEST(Type.TEST),
	AROUND_THE_BALL(Type.TEST),
	PP_PLAY(Type.TEST),
	MAINTENANCE(Type.TEST),
	GUI_TEST_PLAY(Type.TEST),
	
	CHIP_FORWARD(Type.TEST),
	FREEKICK_OFFENSE_PREPARE_WITH_THREE(Type.TEST),
	INDIRECT_SHOTV2;
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * The type of a play may be either:
	 * <ul>
	 * <li> {@link Type#STANDARD},</li>
	 * <li> {@link Type#GAME} or</li>
	 * <li> {@link Type#TEST}.</li>
	 * </ul>
	 * 
	 * @author Gero
	 */
	private static enum Type
	{
		/** Each play that is meant to handle a "standard"-situation */
		STANDARD,
		/** Each play that is free to be chosen during a game */
		GAME,
		/** Plays that are meant for testing-purposes only */
		TEST;
	}
	
	private static List<EPlay>	gamePlays;
	private static List<EPlay>	standardPlays;
	
	/** @see Type */
	private final Type			type;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Calls {@link #EPlay(Type)} with {@link Type#GAME}
	 */
	private EPlay()
	{
		this(Type.GAME);
	}
	

	/**
	 * @param type
	 * @see EPlay
	 */
	private EPlay(Type type)
	{
		this.type = type;
		
		if (type == Type.GAME)
		{
			registerGamePlay(this);
		} else if (type == Type.STANDARD)
		{
			registerStandardPlay(this);
		}
	}
	

	private static void registerGamePlay(EPlay type)
	{
		if (gamePlays == null)
		{
			gamePlays = new LinkedList<EPlay>();
		}
		
		if (type.isGamePlay())
		{
			gamePlays.add(type);
		}
	}
	

	private static void registerStandardPlay(EPlay type)
	{
		if (standardPlays == null)
		{
			standardPlays = new LinkedList<EPlay>();
		}
		
		if (type.isStandardPlay())
		{
			standardPlays.add(type);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return The {@link Type}
	 */
	public Type getType()
	{
		return type;
	}
	

	/**
	 * @return Whether this is a play worth considering during a real match
	 */
	public boolean isGamePlay()
	{
		return this.type.equals(Type.GAME);
	}
	

	/**
	 * @return Whether this play is of type
	 */
	public boolean isStandardPlay()
	{
		return this.type.equals(Type.STANDARD);
	}
	

	/**
	 * @return An immutable list of {@link EPlay}s which are of type {@link Type#GAME}
	 */
	public static List<EPlay> getGamePlays()
	{
		return Collections.unmodifiableList(gamePlays);
	}
	

	/**
	 * @return An immutable list of {@link EPlay}s which are of type {@link Type#STANDARD}
	 */
	public static List<EPlay> getStandardPlays()
	{
		return Collections.unmodifiableList(standardPlays);
	}
	

	@Override
	public String toString()
	{
		switch (this)
		{
			case KEEPER_PLUS_2_DEFENDER:
			{
				return new String("K+2");
			}
			case KEEPER_PLUS_1_DEFENDER:
			{
				return new String("K+1");
			}
			case KEEPER_SOLO:
			{
				return new String("K+0");
			}
			default:
			{
				return this.name();
			}
		}
		
	}
}
