/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Handling of play types from EPlay
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public final class PlayType
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final PlayType			INSTANCE		= new PlayType();
	
	private Map<EPlayType, List<EPlay>>	playLists	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private PlayType()
	{
		initPlayLists();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the instance
	 */
	public static PlayType getInstance()
	{
		return INSTANCE;
	}
	
	
	/**
	 * Register a new play. This should only be called by EPlay
	 * 
	 * @param play
	 */
	protected static void registerPlay(EPlay play)
	{
		INSTANCE.playLists.get(play.getType()).add(play);
	}
	
	
	private void initPlayLists()
	{
		if (playLists == null)
		{
			playLists = new HashMap<EPlayType, List<EPlay>>();
			for (EPlayType t : EPlayType.values())
			{
				playLists.put(t, new LinkedList<EPlay>());
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type {@link EPlayType#OFFENSIVE},
	 *         {@link EPlayType#DEFENSIVE} and {@link EPlayType#SUPPORT}.
	 */
	public static List<EPlay> getGamePlays()
	{
		final List<EPlay> gamePlays = new LinkedList<EPlay>();
		gamePlays.addAll(getPlays(EPlayType.OFFENSIVE));
		gamePlays.addAll(getPlays(EPlayType.DEFENSIVE));
		gamePlays.addAll(getPlays(EPlayType.SUPPORT));
		return gamePlays;
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type {@link EPlayType#OFFENSIVE}
	 */
	public static List<EPlay> getOffensivePlays()
	{
		return getPlays(EPlayType.OFFENSIVE);
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type {@link EPlayType#DEFENSIVE}
	 */
	public static List<EPlay> getDefensivePlays()
	{
		return getPlays(EPlayType.DEFENSIVE);
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type {@link EPlayType#KEEPER}
	 */
	public static List<EPlay> getKeeperPlays()
	{
		return getPlays(EPlayType.KEEPER);
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type {@link EPlayType#SUPPORT}
	 */
	public static List<EPlay> getSupportPlays()
	{
		return getPlays(EPlayType.SUPPORT);
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type {@link EPlayType#STANDARD}
	 */
	public static List<EPlay> getStandardPlays()
	{
		return getPlays(EPlayType.STANDARD);
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type{@link EPlayType#TEST}
	 */
	public static List<EPlay> getTestPlays()
	{
		return (getPlays(EPlayType.TEST));
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type{@link EPlayType#TEST}
	 */
	public static List<EPlay> getCalibratePlays()
	{
		return (getPlays(EPlayType.CALIBRATE));
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type{@link EPlayType#DEPRECATED}
	 */
	public static List<EPlay> getDeprecatedPlays()
	{
		return (getPlays(EPlayType.DEPRECATED));
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type{@link EPlayType#DEFECT}
	 */
	public static List<EPlay> getDefectPlays()
	{
		return (getPlays(EPlayType.DEFECT));
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type{@link EPlayType#DEFECT}
	 */
	public static List<EPlay> getDisabledPlays()
	{
		return (getPlays(EPlayType.DISABLED));
	}
	
	
	/**
	 * @return An immutable list of {@link EPlay}s which are of type{@link EPlayType#CHALLENGE}
	 */
	public static List<EPlay> getChallengePlays()
	{
		return (getPlays(EPlayType.CHALLENGE));
	}
	
	
	/**
	 * @return A list of {@link EPlay}s which are mixed team plays
	 */
	public static List<EPlay> getMixedTeamPlays()
	{
		final List<EPlay> plays = new LinkedList<EPlay>();
		for (final EPlay play : EPlay.values())
		{
			if (play.isMixedTeamPlay())
			{
				plays.add(play);
			}
		}
		return plays;
	}
	
	
	/**
	 * All plays (excluding helper and deprecated)
	 * 
	 * @return An immutable list of {@link EPlay}s
	 */
	public static List<EPlay> getAllPlays()
	{
		final List<EPlay> allPlays = new LinkedList<EPlay>();
		for (final EPlayType t : EPlayType.values())
		{
			if ((t == EPlayType.HELPER) || (t == EPlayType.DEPRECATED))
			{
				continue;
			}
			allPlays.addAll(getPlays(t));
		}
		return allPlays;
	}
	
	
	private static List<EPlay> getPlays(EPlayType type)
	{
		return Collections.unmodifiableList(INSTANCE.playLists.get(type));
	}
}
