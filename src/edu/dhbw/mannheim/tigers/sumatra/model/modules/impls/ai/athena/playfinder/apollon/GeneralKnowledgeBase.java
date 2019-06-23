/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 6, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


/**
 * General implementation of IKnowledgeBase.
 * 
 * @author dirk
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
@Entity
public class GeneralKnowledgeBase implements IKnowledgeBase
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(GeneralKnowledgeBase.class.getName());
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<KnowledgePlay>	knowledgePlays;
	
	@Id
	private String						name;
	
	@Version
	private long						version;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param name
	 */
	public GeneralKnowledgeBase(String name)
	{
		knowledgePlays = new LinkedList<KnowledgePlay>();
		this.name = name;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public List<IComparisonResult> findNextSuccessfulPlaysSorted(List<EPlay> plays, AKnowledgeField currentField,
			int numFieldsToCompare)
	{
		return doFindNextPlaysSorted(plays, currentField, numFieldsToCompare, true);
	}
	
	
	@Override
	public List<IComparisonResult> findNextFailedPlaysSorted(List<EPlay> plays, AKnowledgeField currentField,
			int numFieldsToCompare)
	{
		return doFindNextPlaysSorted(plays, currentField, numFieldsToCompare, false);
	}
	
	
	/**
	 * This methods will query the knowledgeBase
	 * 
	 * @param plays only consider these plays
	 * @param currentField KnowledgeField to compare with
	 * @param numFieldsToCompare how many fields should be compared
	 * @param successful find successful plays? else failed plays will be found
	 * @return a list of ComparisonResults, for each play one ComparisonResult
	 * @see IKnowledgeBase#findNextSuccessfulPlaysSorted(List, AKnowledgeField, int)
	 * @see IKnowledgeBase#findNextFailedPlaysSorted(List, AKnowledgeField, int)
	 */
	private List<IComparisonResult> doFindNextPlaysSorted(List<EPlay> plays, AKnowledgeField currentField,
			int numFieldsToCompare, boolean successful)
	{
		final List<IComparisonResult> comparisonResults = new LinkedList<IComparisonResult>();
		for (final KnowledgePlay knowledgePlay : knowledgePlays)
		{
			if (!plays.contains(knowledgePlay.getPlay().getePlay()))
			{
				// only consider plays given by plays-List
				continue;
			}
			IComparisonResult comparisonResult;
			if (successful)
			{
				comparisonResult = knowledgePlay.getSuccessFields().compareNext(currentField, numFieldsToCompare);
			} else
			{
				comparisonResult = knowledgePlay.getFailedFields().compareNext(currentField, numFieldsToCompare);
			}
			// a result of 0 means, there are no fields left
			if (comparisonResult.calcResult() > 0.0)
			{
				comparisonResult.setPlay(knowledgePlay.getPlay());
				comparisonResults.add(comparisonResult);
			}
		}
		Collections.sort(comparisonResults);
		return comparisonResults;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the knowledgePlays
	 */
	@Override
	public List<KnowledgePlay> getKnowledgePlays()
	{
		return knowledgePlays;
	}
	
	
	@Override
	public void addKnowledgePlay(KnowledgePlay knowledgePlay)
	{
		final int i = knowledgePlays.indexOf(knowledgePlay);
		if (i == -1)
		{
			knowledgePlays.add(knowledgePlay);
		} else
		{
			knowledgePlays.get(i).getSuccessFields().addAll(knowledgePlay.getSuccessFields());
			knowledgePlays.get(i).getFailedFields().addAll(knowledgePlay.getFailedFields());
		}
	}
	
	
	@Override
	public void resetCounters()
	{
		for (final KnowledgePlay knowledgePlay : knowledgePlays)
		{
			knowledgePlay.resetCounters();
		}
	}
	
	
	@Override
	public String getName()
	{
		return name;
	}
	
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	
	@Override
	public void setVersion(long ver)
	{
		version = ver;
	}
	
	
	@Override
	public long getVersion()
	{
		return version;
	}
	
	
	@Override
	public void initialize()
	{
		int count = 0;
		for (KnowledgePlay play : getKnowledgePlays())
		{
			for (AKnowledgeField akf : play.getFailedFields().getKnowledgeFields())
			{
				akf.initialize();
				count++;
			}
			for (AKnowledgeField akf : play.getSuccessFields().getKnowledgeFields())
			{
				akf.initialize();
				count++;
			}
		}
		log.debug("initialized general knowledgebase with " + count + " fields");
		log.info(getKbStatistics());
	}
	
	
	@Override
	public KbStatistics getKbStatistics()
	{
		return new KbStatistics(this);
	}
}
