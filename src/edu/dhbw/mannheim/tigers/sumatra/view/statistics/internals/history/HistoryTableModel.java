/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.history;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.MatchStatistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.PlayCombinations;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.PlayStats;


/**
 * the table model for the history statistics view
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class HistoryTableModel extends AbstractTableModel
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long			serialVersionUID	= -2861551927673164680L;
	
	private List<PlayCombinations>	matchStatsList		= new LinkedList<PlayCombinations>();
	
	private MatchStatistics				matchStats			= new MatchStatistics("fullStats");
	
	private long							startTime			= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public HistoryTableModel()
	{
		matchStatsList.addAll(matchStats.getMatchStatistic());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public int getRowCount()
	{
		return matchStats.getMatchStatistic().size();
	}
	
	
	@Override
	public int getColumnCount()
	{
		return EHistoryTableColumns.getColumnLabels().size();
	}
	
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		PlayCombinations plays = matchStatsList.get(rowIndex);
		PlayStats anyPlay = plays.getFirstPlayStats();
		SimpleDateFormat shortTimeFormat = new SimpleDateFormat("hh:mm:ss.SSS");
		
		try
		{
			if (anyPlay.getStartTime() > startTime)
			{
				switch (EHistoryTableColumns.getColumnById(columnIndex))
				{
					case START:
						return shortTimeFormat.format(new Date(anyPlay.getStartTime()));
					case END:
						return shortTimeFormat.format(new Date(anyPlay.getEndTime()));
					case DURATION:
						return anyPlay.getEndTime() - anyPlay.getStartTime();
					case OFFENSIVE:
						return createPlayList(plays.getOffensive());
					case DEFFENSIVE:
						return createPlayList(plays.getDeffensive());
					case SUPPORT:
						return createPlayList(plays.getSupports());
					case OTHERS:
						return createPlayList(plays.getOthers());
					case REASON:
						return anyPlay.getSelectionReason();
					case RESULT:
						return anyPlay.getResult();
					default:
						throw new IllegalArgumentException("This column does not exist in the history table.");
				}
			}
		} catch (NullPointerException err)
		{
			return "null";
		}
		return null;
	}
	
	
	private String createPlayList(List<PlayStats> playStats)
	{
		StringBuilder supports = new StringBuilder();
		for (PlayStats supp : playStats)
		{
			supports.append(supp.getPlay());
			supports.append(", ");
		}
		return supports.toString();
	}
	
	
	@Override
	public String getColumnName(int column)
	{
		return EHistoryTableColumns.getColumnLabels().get(column);
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
		matchStatsList = new LinkedList<PlayCombinations>(matchStats.getMatchStatistic());
		fireTableDataChanged();
	}
	
	
	/**
	 * @return the matchStatsList
	 */
	public List<PlayCombinations> getMatchStatsList()
	{
		return matchStatsList;
	}
	
	
	/**
	 * @param matchStatsList the matchStatsList to set
	 */
	public void setMatchStatsList(List<PlayCombinations> matchStatsList)
	{
		this.matchStatsList = new LinkedList<PlayCombinations>(matchStatsList);
		fireTableDataChanged();
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
		fireTableDataChanged();
	}
	
	
	/**
	 * @return lowest start time of all contained plays
	 */
	public long getFirstTime()
	{
		return getMatchStatsList().get(0).getFirstPlayStats().getStartTime();
	}
	
	
	/**
	 * @return highest start time of all contained plays
	 */
	public long getLastTime()
	{
		return getMatchStatsList().get(getMatchStatsList().size() - 1).getFirstPlayStats().getStartTime();
	}
}
