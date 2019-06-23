/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.summary;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.ESelectionReason;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.MatchStatistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.PlayCombinations;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.PlayStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlayState;


/**
 * the table model for the history statistics view
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class SummaryTableModel extends AbstractTableModel
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long							serialVersionUID		= -2861551927673164680L;
	
	private MatchStatistics								matchStats				= new MatchStatistics("fullStats");
	
	private Map<EPlay, PlayInformation>				orderdByPlays			= new HashMap<EPlay, PlayInformation>();
	
	private List<Entry<EPlay, PlayInformation>>	orderedByPlaysList	= new LinkedList<Entry<EPlay, PlayInformation>>();
	
	private long											startTime				= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public SummaryTableModel()
	{
		createOrderdByPlays();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public int getRowCount()
	{
		return orderedByPlaysList.size();
	}
	
	
	@Override
	public int getColumnCount()
	{
		return ESummaryTableColumns.getColumnLabels().size();
	}
	
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if (rowIndex >= orderedByPlaysList.size())
		{
			return "";
		}
		Entry<EPlay, PlayInformation> play = orderedByPlaysList.get(rowIndex);
		PlayInformation pi = play.getValue();
		switch (ESummaryTableColumns.getColumnById(columnIndex))
		{
			case TYPE:
				return pi.getPlayStats().get(0).getPlay().getType();
			case PLAY:
				return play.getKey();
			case AMOUNT:
				return new Integer(pi.size());
			case DURATION:
				return (long) pi.getAvg() + " ( " + (long) pi.getMin() + " - " + (long) pi.getMax() + " )";
			case BOTS:
				return String.format("%.2f", pi.getBotsAvg()) + " (" + (int) pi.getBotsMin() + " - "
						+ (int) pi.getBotsMax() + ")";
			case RANDOM:
				return pi.getReason().get(ESelectionReason.RANDOM);
			case SUCCESSFUL_FIRST_TRY:
				return pi.getReason().get(ESelectionReason.SUCCESSFUL_FIRST_TRY);
			case SUCCESSFUL_MULTIPLE_TRIES:
				return pi.getReason().get(ESelectionReason.SUCCESSFUL_MULTIPLE_TRIES);
			case SUCCESSFUL_EQUAL_MATCH:
				return pi.getReason().get(ESelectionReason.SUCCESSFUL_EQUAL_MATCH);
			case SUCCESSFUL_UNKNOWN:
				return pi.getReason().get(ESelectionReason.UNKNOWN);
			case REFEREE:
				return pi.getReason().get(ESelectionReason.REFEREE);
			case SUCCEEDED:
				return pi.getResult().get(EPlayState.SUCCEEDED);
			case FINISHED:
				return pi.getResult().get(EPlayState.FINISHED);
			case FAILED:
				return pi.getResult().get(EPlayState.FAILED);
				
		}
		throw new IllegalArgumentException("Wrong history column");
	}
	
	
	private void addToOrdered(PlayStats playStats)
	{
		if (playStats != null)
		{
			if (!orderdByPlays.containsKey(playStats.getPlay()))
			{
				orderdByPlays.put(playStats.getPlay(), new PlayInformation());
			}
			orderdByPlays.get(playStats.getPlay()).addPlayStats(playStats);
		}
	}
	
	
	@Override
	public String getColumnName(int column)
	{
		return ESummaryTableColumns.getColumnLabels().get(column);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the matchStats
	 */
	public MatchStatistics getMatchStats()
	{
		
		return matchStats;
	}
	
	
	/**
	 * @param matchStats the matchStats to set
	 */
	public void setMatchStats(MatchStatistics matchStats)
	{
		this.matchStats = matchStats;
		createOrderdByPlays();
	}
	
	
	private void createOrderdByPlays()
	{
		orderdByPlays = new HashMap<EPlay, PlayInformation>();
		for (PlayCombinations pc : matchStats.getMatchStatistic())
		{
			if (pc.getFirstPlayStats().getStartTime() > startTime)
			{
				addListToOrdered(pc.getOffensive());
				addListToOrdered(pc.getDeffensive());
				addListToOrdered(pc.getSupports());
				addListToOrdered(pc.getOthers());
			}
		}
		for (PlayInformation play : orderdByPlays.values())
		{
			play.calcStats();
		}
		orderedByPlaysList = new LinkedList<Entry<EPlay, PlayInformation>>(orderdByPlays.entrySet());
		fireTableDataChanged();
	}
	
	
	private void addListToOrdered(List<PlayStats> playStats)
	{
		for (PlayStats ps : playStats)
		{
			addToOrdered(ps);
		}
	}
	
	
	/**
	 * @return the startTime
	 */
	public long getStartTime()
	{
		return startTime;
	}
	
	
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
		createOrderdByPlays();
	}
	
}
