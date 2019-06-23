/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.02.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.WorldFrameFactory;


/**
 * Simple factory class which helps especially
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena} for in-game play creation and decision
 * 
 * @author Gero
 */
public final class PlayFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log			= Logger.getLogger(PlayFactory.class.getName());
	
	private static PlayFactory		instance		= null;
	
	private final List<APlay>		dummyPlays	= new ArrayList<APlay>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public static synchronized PlayFactory getInstance()
	{
		if (instance == null)
		{
			instance = new PlayFactory();
		}
		return instance;
	}
	
	
	private PlayFactory()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Create a play with minimal number of roles
	 * 
	 * (@see {@link #createPlay(EPlay, AIInfoFrame, int)})
	 * @param ePlay
	 * @param curFrame
	 * @return
	 */
	public APlay createPlay(EPlay ePlay, AIInfoFrame curFrame)
	{
		return createPlay(ePlay, curFrame, ePlay.getMinRoles());
	}
	
	
	/**
	 * Factory method for {@link APlay}s
	 * <p>
	 * <b>Important:</b> Don't forget to add your play here if you wrote one!!!
	 * </p>
	 * 
	 * @param ePlay The {@link EPlay} associated with the {@link APlay} to return
	 * @param curFrame The {@link AIInfoFrame} the created play should be initialized with
	 * @param numAssignedRoles
	 * @return The generated {@link APlay}
	 * @throws IllegalArgumentException If play or wf == null or the given {@link EPlay} is not associated with any
	 *            {@link APlay}
	 */
	public APlay createPlay(EPlay ePlay, AIInfoFrame curFrame, int numAssignedRoles)
	{
		int numRolesToAssign = numAssignedRoles;
		if ((ePlay == null) || (curFrame == null))
		{
			throw new IllegalArgumentException("ePlay or AIFrame == null, unable to generate APlay!");
		}
		APlay aPlay = null;
		
		if (numAssignedRoles == EPlay.MAX_BOTS)
		{
			numRolesToAssign = ePlay.getMaxRoles();
			if (numRolesToAssign == EPlay.MAX_BOTS)
			{
				numRolesToAssign = curFrame.worldFrame.tigerBotsAvailable.size();
			}
		}
		
		try
		{
			final Constructor<?> con = ePlay.getConstructor();
			aPlay = (APlay) con.newInstance(curFrame, numRolesToAssign);
			aPlay.setType(ePlay);
		} catch (final SecurityException err)
		{
			log.fatal("Play type could not be handled by play factory! Play = " + ePlay, err);
		} catch (final InstantiationException err)
		{
			log.fatal("Play type could not be handled by play factory! Play = " + ePlay, err);
		} catch (final IllegalAccessException err)
		{
			log.fatal("Play type could not be handled by play factory! Play = " + ePlay, err);
		} catch (final IllegalArgumentException err)
		{
			log.fatal("Play type could not be handled by play factory! Play = " + ePlay, err);
		} catch (final InvocationTargetException err)
		{
			log.fatal("Play type could not be handled by play factory! Play = " + ePlay, err);
		}
		
		return aPlay;
	}
	
	
	/**
	 * Calls {@link #doSelfCheck(List)} with {@link PlayType#getGamePlays()}
	 * @return
	 */
	public List<EPlay> selfCheckPlays()
	{
		return doSelfCheck(PlayType.getAllPlays());
	}
	
	
	/**
	 * Simply tries to create an instance of {@link APlay} for every given {@link EPlay} using
	 * {@link #createPlay(EPlay, AIInfoFrame)}.
	 * 
	 * @param plays
	 * @return A list of {@link EPlay} which failed
	 */
	private List<EPlay> doSelfCheck(List<EPlay> plays)
	{
		final WorldFrameFactory wfFactory = new WorldFrameFactory();
		final List<EPlay> failedPlays = new ArrayList<EPlay>();
		for (final EPlay testPlay : plays)
		{
			try
			{
				int numAssignedRoles = testPlay.getMinRoles();
				if (numAssignedRoles == 0)
				{
					// for testing, this number should not really matter
					numAssignedRoles = 6;
				}
				final APlay play = createPlay(testPlay, wfFactory.createFakeAIInfoFrame(), numAssignedRoles);
				if (play != null)
				{
					dummyPlays.add(play);
					play.changeToCanceled();
				}
			} catch (final IllegalArgumentException err)
			{
				failedPlays.add(testPlay);
				log.error("createPlay failed with: " + err.getMessage());
			}
		}
		
		return failedPlays;
	}
	
	
	/**
	 * Will give you an APlay for the according ePlay
	 * @param ePlay
	 * 
	 * @return the dummyPlays Carfull! will return null, if play not found!
	 */
	public APlay getDummyPlay(EPlay ePlay)
	{
		for (final APlay dPlay : dummyPlays)
		{
			if (dPlay.getType() == ePlay)
			{
				return dPlay;
			}
		}
		return null;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
