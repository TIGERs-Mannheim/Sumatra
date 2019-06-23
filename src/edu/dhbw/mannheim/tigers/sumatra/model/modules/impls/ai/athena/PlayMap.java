/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.02.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena;


import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.*;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.BALLCAPTURING_WITH_ONE_PASS_BLOCKER;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.BALLWINNING_WITH_ONE_BLOCKER;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.BALLWINNING_WITH_ONE_PASS_BLOCKER;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.FREEKICK_MARKER;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.FREEKICK_OFFENSE_PREPARE_WITH_THREE;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.GAME_OFFENSE_PREPARE_WITH_THREE;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.GAME_OFFENSE_PREPARE_WITH_TWO;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.INDIRECT_SHOT;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.MAN_TO_MAN_MARKER;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.PASS_FORWARD;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.PASS_TO_KEEPER;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.POSITION_IMPROVING_NO_BALL_WITH_ONE;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.POSITION_IMPROVING_NO_BALL_WITH_TWO;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay.DIRECT_SHOT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.PlayFactory;


/**
 * Static map, that contains the Plays which follow up to a given Play.
 * Also fakePlays and playTuples are stored here.
 * @author MalteM
 */
public class PlayMap
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger								log				= Logger.getLogger(getClass());
	private final PlayFactory						factory			= PlayFactory.getInstance();
	private static PlayMap							instance			= null;
	
	/**
	 * Contains all Play-Tuples that are defined as follow-tuple to
	 * a given EPlay.
	 */
	private final Map<EPlay, List<PlayTuple>>	followPlayMap	= new HashMap<EPlay, List<PlayTuple>>();
	
	/** A List of fake Play-instances. */
	private final Map<EPlay, APlay>				fakePlays		= new HashMap<EPlay, APlay>();
	
	private final List<PlayTuple>					tuples			= new ArrayList<PlayTuple>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public static synchronized PlayMap getInstance()
	{
		if (instance == null)
		{
			instance = new PlayMap();
		}
		return instance;
	}
	
	
	/**
	 * At the end of this constructor you can endorse your followPlays!
	 * 
	 */
	private PlayMap()
	{
		log.debug("PlayMap initializing...");
		
		// Initialize fake-plays for score-calculation
		for (EPlay type : EPlay.getGamePlays())
		{
			fakePlays.put(type, factory.createPlay(type, factory.createFakeFrame()));
		}
		for (EPlay type : EPlay.getStandardPlays())
		{
			fakePlays.put(type, factory.createPlay(type, factory.createFakeFrame()));
		}
		
		// Creation of Play-Tuples
		for (EPlay play1 : EPlay.getGamePlays())
		{
			tuples.add(new PlayTuple(play1));
			tuples.add(new PlayTuple(play1, EPlay.KEEPER_SOLO));
			tuples.add(new PlayTuple(play1, EPlay.KEEPER_PLUS_1_DEFENDER));
			tuples.add(new PlayTuple(play1, EPlay.KEEPER_PLUS_2_DEFENDER));
			for (EPlay play2 : EPlay.getGamePlays())
			{
				tuples.add(new PlayTuple(play1, play2, EPlay.KEEPER_SOLO));
				tuples.add(new PlayTuple(play1, play2, EPlay.KEEPER_PLUS_1_DEFENDER));
				tuples.add(new PlayTuple(play1, play2, EPlay.KEEPER_PLUS_2_DEFENDER));
			}
		}
		initTupleCheck();
		log.debug("Number Of Play Tuples = " + tuples.size());
		
		
		// The initialization of the play-map
		
		
		// relevant for Istanbul
		
		// attack
		put(BALL_GETTING, add(DIRECT_SHOT));
		
		// defense
		put(MAN_TO_MAN_MARKER, add(new PlayTuple(BALL_GETTING, SUPPORT_WITH_ONE_BLOCKER)));
		put(MAN_TO_MAN_MARKER, add(new PlayTuple(BALL_GETTING, SUPPORT_WITH_ONE_PASS_BLOCKER)));
		
		put(FREEKICK_MARKER, add(new PlayTuple(BALL_GETTING, SUPPORT_WITH_ONE_BLOCKER)));
		put(FREEKICK_MARKER, add(new PlayTuple(BALL_GETTING, SUPPORT_WITH_ONE_PASS_BLOCKER)));
		
		
		// NOT relevant for Istanbul
		
		// attack
		put(BALLCAPTURING_WITH_DOUBLING,
				add(GAME_OFFENSE_PREPARE_WITH_TWO, BALLWINNING_WITH_ONE_BLOCKER, BALLWINNING_WITH_ONE_PASS_BLOCKER));
		put(BALLCAPTURING_WITH_ONE_PASS_BLOCKER,
				add(GAME_OFFENSE_PREPARE_WITH_TWO, BALLWINNING_WITH_ONE_BLOCKER, BALLWINNING_WITH_ONE_PASS_BLOCKER));
		put(BALLWINNING_WITH_ONE_BLOCKER, add(GAME_OFFENSE_PREPARE_WITH_TWO, INDIRECT_SHOT, PASS_FORWARD));
		put(BALLWINNING_WITH_ONE_PASS_BLOCKER, add(GAME_OFFENSE_PREPARE_WITH_TWO));
		
		put(PASS_FORWARD, add(GAME_OFFENSE_PREPARE_WITH_TWO, INDIRECT_SHOT));
		put(PASS_TO_KEEPER, add(POSITION_IMPROVING_NO_BALL_WITH_ONE));
		
		put(GAME_OFFENSE_PREPARE_WITH_THREE, add(new PlayTuple(DIRECT_SHOT, POSITION_IMPROVING_NO_BALL_WITH_TWO)));
		put(GAME_OFFENSE_PREPARE_WITH_THREE, add(new PlayTuple(INDIRECT_SHOT, POSITION_IMPROVING_NO_BALL_WITH_ONE)));
		put(GAME_OFFENSE_PREPARE_WITH_THREE, add(new PlayTuple(PASS_FORWARD, POSITION_IMPROVING_NO_BALL_WITH_ONE)));
		put(GAME_OFFENSE_PREPARE_WITH_TWO, add(INDIRECT_SHOT, PASS_FORWARD));
		put(GAME_OFFENSE_PREPARE_WITH_TWO, add(new PlayTuple(DIRECT_SHOT, POSITION_IMPROVING_NO_BALL_WITH_ONE)));
		
		// support
		put(POSITION_IMPROVING_NO_BALL_WITH_ONE, add(SUPPORT_WITH_ONE_BLOCKER));
		put(POSITION_IMPROVING_NO_BALL_WITH_ONE, add(SUPPORT_WITH_ONE_PASS_BLOCKER));
		
		// defense
		put(MAN_TO_MAN_MARKER, add(GAME_OFFENSE_PREPARE_WITH_TWO));
		put(FREEKICK_MARKER, add(GAME_OFFENSE_PREPARE_WITH_TWO));
		
		// standard
		put(FREEKICK_MARKER, add(new PlayTuple(GAME_OFFENSE_PREPARE_WITH_TWO)));
		put(FREEKICK_OFFENSE_PREPARE_WITH_THREE, add(new PlayTuple(DIRECT_SHOT, POSITION_IMPROVING_NO_BALL_WITH_TWO)));
		put(FREEKICK_OFFENSE_PREPARE_WITH_THREE, add(new PlayTuple(INDIRECT_SHOT, POSITION_IMPROVING_NO_BALL_WITH_ONE)));
		put(FREEKICK_OFFENSE_PREPARE_WITH_THREE, add(new PlayTuple(PASS_FORWARD, POSITION_IMPROVING_NO_BALL_WITH_ONE)));
		
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Inititial check of the play tuples.
	 * Used for a pre-choice to downsize number of playtuples.
	 * @author MalteM
	 * 
	 */
	private void initTupleCheck()
	{
		final Iterator<PlayTuple> it = tuples.iterator();
		TUPLES: while (it.hasNext())
		{
			int keepers = 0;
			int shooters = 0;
			List<EPlay> plays = it.next().getPlays();
			for (EPlay play : plays)
			{
				APlay fake = getFakePlay(play);
				keepers = fake.hasKeeperRole() ? keepers + 1 : keepers;
				shooters = fake.isBallCarrying() ? shooters + 1 : shooters;
				// Are two equal plays in the tuple?
				if (plays.indexOf(play) != plays.lastIndexOf(play))
				{
					it.remove();
					continue TUPLES;
				}
			}
			if (keepers != 1)
			{
				it.remove();
				continue;
			}
			if (shooters > 1)
			{
				it.remove();
				continue;
			}
		}
		
		
	}
	
	
	/**
	 * @param type
	 * @return A list of {@link EPlay}s which are the follow-ups of the given play-type
	 */
	public List<PlayTuple> getFollowups(EPlay type)
	{
		return followPlayMap.get(type);
	}
	
	
	public List<APlay> getFakePlays()
	{
		return new ArrayList<APlay>(fakePlays.values());
	}
	
	
	public APlay getFakePlay(EPlay eplay)
	{
		return fakePlays.get(eplay);
	}
	
	
	private void put(EPlay lastPlay, List<PlayTuple> fTuples)
	{
		// Safety check: Same role-count as predecessor?
		final APlay lastFakePlay = getFakePlay(lastPlay);
		// Safety check: Has fake-play?
		if (lastFakePlay == null)
		{
			log.fatal(lastPlay + " has no fake play, not inserted into PlayMap!!!");
			return;
		}
		
		final Iterator<PlayTuple> it = fTuples.iterator();
		while (it.hasNext())
		{
			PlayTuple tuple = it.next();
			for (EPlay play : tuple.getPlays())
			{
				APlay fakeFPlay = getFakePlay(play);
				// Safety check: Has fake-play?
				if (fakeFPlay == null)
				{
					log.fatal(play + " has no fake play, not inserted into PlayMap!!!");
					return;
				}
			}
			
			if (getRoleCount(tuple) != lastFakePlay.getRoleCount())
			{
				log.fatal(lastPlay + "s follow-Tuple '" + tuple + "' has different role-count ("
						+ lastFakePlay.getRoleCount() + "|" + tuple.getRoleCount() + "), will be removed!!!");
				it.remove();
			}
		}
		
		// Actually put plays
		if (fTuples.size() > 0)
		{
			followPlayMap.put(lastPlay, fTuples);
		}
	}
	
	
	/**
	 * Just for easy usage in the constructor {@link #PlayMap()}
	 * 
	 * @param plays
	 * @return {@link Arrays#asList(plays)}
	 */
	private List<PlayTuple> add(EPlay... plays)
	{
		List<PlayTuple> tuples = new ArrayList<PlayTuple>();
		for (EPlay play : plays)
		{
			tuples.add(new PlayTuple(play));
		}
		return tuples;
	}
	
	
	private List<PlayTuple> add(PlayTuple... inTuples)
	{
		return Arrays.asList(inTuples);
	}
	
	
	public List<PlayTuple> getTuples()
	{
		return tuples;
	}
	
	
	private int getRoleCount(PlayTuple tuple)
	{
		int noOfBots = 0;
		for (EPlay play : tuple.getPlays())
		{
			noOfBots += fakePlays.get(play).getRoleCount();
		}
		return noOfBots;
	}
	
	
	/**
	 * Returns all fakePlays belonging to the given EPlays.
	 * 
	 * @param followups
	 * @return
	 */
	public List<APlay> getFakePlays(List<EPlay> followups)
	{
		List<APlay> fakePlays = new ArrayList<APlay>();
		for (EPlay play : followups)
		{
			fakePlays.add(getFakePlay(play));
		}
		return fakePlays;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
