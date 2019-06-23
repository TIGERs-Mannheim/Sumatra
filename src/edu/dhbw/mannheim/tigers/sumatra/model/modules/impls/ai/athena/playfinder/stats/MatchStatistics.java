/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2013
 * Author(s): jan
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.FetchType;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence.APersistence;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;


/**
 * Data holder for a series of match statistics consisting of {@link PlayStats}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MatchStatistics extends APersistence
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log			= Logger.getLogger(MatchStatistics.class.getName());
	private static final String	BASE_PATH	= "logs/matchstats/";
	
	private transient long			startTime	= 0;
	private StatisticsHolder		statsHolder;
	
	@Entity
	private static class StatisticsHolder
	{
		@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
		private List<PlayCombinations>	matchStatistic	= new LinkedList<PlayCombinations>();
		
		
		/**
		 * @return the matchStatistic
		 */
		public final List<PlayCombinations> getMatchStatistic()
		{
			return matchStatistic;
		}
		
		
		/**
		 * @param stats
		 */
		public void add(PlayCombinations stats)
		{
			matchStatistic.add(stats);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param name Stats will be saved and restored under this name
	 */
	public MatchStatistics(String name)
	{
		super(BASE_PATH, name);
		load();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Only for startTime capturing
	 * 
	 * @param plays of type List<APlay>
	 */
	public void onNewPlays(List<APlay> plays)
	{
		startTime = System.currentTimeMillis();
	}
	
	
	/**
	 * All plays will be added here
	 * 
	 * @param plays
	 */
	public void onFinishedPlays(List<APlay> plays)
	{
		final long endTime = System.currentTimeMillis();
		List<PlayStats> offStats = new ArrayList<PlayStats>(3);
		List<PlayStats> defStats = new ArrayList<PlayStats>(3);
		List<PlayStats> supStats = new ArrayList<PlayStats>(3);
		List<PlayStats> otherStats = new ArrayList<PlayStats>(3);
		
		for (APlay play : plays)
		{
			PlayStats stats = new PlayStats(play.getType(), play.getPlayState(), play.getNumAssignedRoles(),
					play.getSelectionReason(), startTime, endTime);
			switch (play.getType().getType())
			{
				case OFFENSIVE:
					offStats.add(stats);
					break;
				case DEFENSIVE:
					defStats.add(stats);
					break;
				case SUPPORT:
					supStats.add(stats);
					break;
				default:
					otherStats.add(stats);
			}
		}
		
		PlayCombinations playCombinations = new PlayCombinations(offStats, defStats, supStats, otherStats);
		statsHolder.add(playCombinations);
	}
	
	
	/**
	 * Load stats from database
	 */
	public final void load()
	{
		if (!new File(getDbFullPath()).exists())
		{
			statsHolder = new StatisticsHolder();
			return;
		}
		
		open();
		try
		{
			final EntityTransaction t = getEm().getTransaction();
			t.begin();
			final TypedQuery<StatisticsHolder> q = getEm().createQuery("SELECT stats FROM StatisticsHolder stats",
					StatisticsHolder.class);
			
			try
			{
				statsHolder = q.getSingleResult();
				if (statsHolder.matchStatistic == null)
				{
					throw new NoResultException();
				}
			} catch (final NoResultException e)
			{
				log.error("database does not contain statistics!");
				statsHolder = new StatisticsHolder();
			}
			t.commit();
		} catch (final PersistenceException e)
		{
			log.error("Could not load DB " + getDbFullPath() + ": " + e.getMessage());
		}
	}
	
	
	/**
	 * Save stats to database
	 */
	public void save()
	{
		if (statsHolder.matchStatistic.isEmpty())
		{
			return;
		}
		open();
		final EntityTransaction t = getEm().getTransaction();
		t.begin();
		
		getEm().persist(statsHolder);
		t.commit();
		log.info("Match statistics saved under " + getDbFullPath());
		close();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the matchStatistic
	 */
	public final List<PlayCombinations> getMatchStatistic()
	{
		return statsHolder.getMatchStatistic();
	}
}
