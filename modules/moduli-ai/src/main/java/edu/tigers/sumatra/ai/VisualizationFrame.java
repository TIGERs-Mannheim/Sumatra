/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai;

import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionTree;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionTreePath;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.situation.EOffensiveSituation;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveAnalysedFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.statistics.MatchStats;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 5)
public class VisualizationFrame
{
	private final long timestamp;
	
	private EAiTeam aiTeam;
	
	private final MatchStats matchStats;
	private final OffensiveStrategy offensiveStrategy;
	private final Map<BotID, OffensiveAction> offensiveActions = new HashMap<>();
	private final Map<BotID, BotAiInformation> aiInfos = new HashMap<>();
	private final OffensiveAnalysedFrame offensiveStatisticsFrame;
	private final OffensiveStatisticsFrame offensiveStatisticsFrameRaw;
	private final Map<EOffensiveSituation, OffensiveActionTree> actionTrees;
	private final OffensiveActionTreePath currentPath;
	
	
	@SuppressWarnings("unused")
	private VisualizationFrame()
	{
		timestamp = 0;
		aiTeam = null;
		matchStats = null;
		offensiveStrategy = null;
		offensiveStatisticsFrame = null;
		offensiveStatisticsFrameRaw = null;
		actionTrees = null;
		currentPath = null;
	}
	
	
	/**
	 * @param aiFrame
	 */
	public VisualizationFrame(final AIInfoFrame aiFrame)
	{
		timestamp = aiFrame.getSimpleWorldFrame().getTimestamp();
		aiTeam = aiFrame.getAiTeam();
		matchStats = aiFrame.getTacticalField().getMatchStatistics();
		offensiveStrategy = aiFrame.getTacticalField().getOffensiveStrategy();
		offensiveActions.putAll(aiFrame.getTacticalField().getOffensiveActions());
		aiInfos.putAll(aiFrame.getAresData().getBotAiInformation());
		offensiveStatisticsFrame = aiFrame.getTacticalField().getAnalyzedOffensiveStatisticsFrame();
		offensiveStatisticsFrameRaw = aiFrame.getTacticalField().getOffensiveStatistics();
		actionTrees = aiFrame.getTacticalField().getActionTrees();
		currentPath = aiFrame.getTacticalField().getCurrentPath();
	}
	
	
	/**
	 * @param aiFrame
	 */
	public VisualizationFrame(final VisualizationFrame aiFrame)
	{
		timestamp = aiFrame.getTimestamp();
		aiTeam = aiFrame.aiTeam;
		matchStats = aiFrame.getMatchStats();
		offensiveStrategy = aiFrame.getOffensiveStrategy();
		offensiveActions.putAll(aiFrame.getOffensiveActions());
		aiInfos.putAll(aiFrame.getAiInfos());
		offensiveStatisticsFrame = aiFrame.getOffensiveStatisticsFrame();
		offensiveStatisticsFrameRaw = aiFrame.getOffensiveStatisticsFrameRaw();
		actionTrees = aiFrame.getActionTrees();
		currentPath = aiFrame.getCurrentPath();
	}
	
	
	/**
	 * @return the timestamp
	 */
	public final long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the teamColor
	 */
	public final ETeamColor getTeamColor()
	{
		return getAiTeam().getTeamColor();
	}
	
	
	/**
	 * @return
	 */
	public EAiTeam getAiTeam()
	{
		return aiTeam;
	}
	
	
	/**
	 * @return
	 */
	public final MatchStats getMatchStats()
	{
		return matchStats;
	}
	
	
	/**
	 * @return the offensiveStrategy
	 */
	public final OffensiveStrategy getOffensiveStrategy()
	{
		return offensiveStrategy;
	}
	
	
	/**
	 * @return the offensiveActions
	 */
	public final Map<BotID, OffensiveAction> getOffensiveActions()
	{
		return offensiveActions;
	}
	
	
	/**
	 * @return the aiInfos
	 */
	public final Map<BotID, BotAiInformation> getAiInfos()
	{
		return aiInfos;
	}
	
	
	/**
	 * @return
	 */
	public OffensiveAnalysedFrame getOffensiveStatisticsFrame()
	{
		return offensiveStatisticsFrame;
	}
	
	
	/**
	 * @return
	 */
	public OffensiveStatisticsFrame getOffensiveStatisticsFrameRaw()
	{
		return offensiveStatisticsFrameRaw;
	}
	
	
	public Map<EOffensiveSituation, OffensiveActionTree> getActionTrees()
	{
		return actionTrees;
	}
	
	
	public OffensiveActionTreePath getCurrentPath()
	{
		return currentPath;
	}
}
