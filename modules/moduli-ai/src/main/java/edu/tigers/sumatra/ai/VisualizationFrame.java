/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.athena.IPlayStrategy;
import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.situation.rating.OffensiveActionTreePath;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveAnalysedFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.statistics.stats.MatchStats;
import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.trees.EOffensiveSituation;
import edu.tigers.sumatra.trees.OffensiveActionTree;
import lombok.Value;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Data container that is send to the UI and that is stored in the Berkeley DB.
 */
@Persistent(version = 7)
@Value
public class VisualizationFrame
{
	long timestamp;

	EAiTeam aiTeam;

	MatchStats matchStats;
	OffensiveStrategy offensiveStrategy;
	Map<BotID, RatedOffensiveAction> offensiveActions = new HashMap<>();
	Map<BotID, BotAiInformation> aiInfos = new HashMap<>();
	OffensiveAnalysedFrame offensiveStatisticsFrame;
	OffensiveStatisticsFrame offensiveStatisticsFrameRaw;
	Map<EOffensiveSituation, OffensiveActionTree> actionTrees;
	OffensiveActionTreePath currentPath;
	transient IPlayStrategy playStrategy;
	Map<BotID, BallInterceptionInformation> ballInterceptionInformationMap;

	Map<BotID, ESupportBehavior> supportBehaviorAssignment;
	Map<BotID, EnumMap<ESupportBehavior, SupportBehaviorPosition>> supportBehaviorViabilities;
	Map<ESupportBehavior, Boolean> activeSupportBehaviors;


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
		playStrategy = null;
		ballInterceptionInformationMap = null;
		supportBehaviorAssignment = null;
		supportBehaviorViabilities = null;
		activeSupportBehaviors = null;
	}


	/**
	 * @param aiFrame
	 */
	public VisualizationFrame(final AIInfoFrame aiFrame)
	{
		timestamp = aiFrame.getSimpleWorldFrame().getTimestamp();
		aiTeam = aiFrame.getAiTeam();
		matchStats = aiFrame.getTacticalField().getMatchStats();
		offensiveStrategy = berkeleyFriendly(aiFrame.getTacticalField().getOffensiveStrategy());
		offensiveActions.putAll(aiFrame.getTacticalField().getOffensiveActions());
		aiInfos.putAll(aiFrame.getAresData().getBotAiInformation());
		offensiveStatisticsFrame = aiFrame.getTacticalField().getAnalyzedOffensiveStatisticsFrame();
		offensiveStatisticsFrameRaw = aiFrame.getTacticalField().getOffensiveStatistics();
		actionTrees = aiFrame.getTacticalField().getActionTrees().getActionTrees();
		currentPath = aiFrame.getTacticalField().getCurrentPath();
		playStrategy = aiFrame.getAthenaAiFrame().getPlayStrategy();
		ballInterceptionInformationMap = aiFrame.getTacticalField().getBallInterceptionInformationMap().entrySet()
				.stream().collect(Collectors.toMap(Map.Entry::getKey, e -> berkeleyFriendly(e.getValue())));
		supportBehaviorAssignment = new HashMap<>(aiFrame.getTacticalField().getSupportBehaviorAssignment());
		supportBehaviorViabilities = new HashMap<>(aiFrame.getTacticalField().getSupportViabilities());
		activeSupportBehaviors = aiFrame.getTacticalField().getActiveSupportBehaviors().isEmpty()
				? new HashMap<>()
				: new EnumMap<>(aiFrame.getTacticalField().getActiveSupportBehaviors());
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
		playStrategy = aiFrame.getPlayStrategy();
		ballInterceptionInformationMap = aiFrame.getBallInterceptionInformationMap();
		supportBehaviorAssignment = aiFrame.getSupportBehaviorAssignment();
		supportBehaviorViabilities = aiFrame.getSupportBehaviorViabilities();
		activeSupportBehaviors = aiFrame.activeSupportBehaviors;
	}


	/**
	 * @return the teamColor
	 */
	public final ETeamColor getTeamColor()
	{
		return getAiTeam().getTeamColor();
	}


	private OffensiveStrategy berkeleyFriendly(OffensiveStrategy offensiveStrategy)
	{
		return new OffensiveStrategy(offensiveStrategy.getAttackerBot().orElse(null),
				new HashMap<>(offensiveStrategy.getCurrentOffensivePlayConfiguration()));
	}


	private BallInterceptionInformation berkeleyFriendly(BallInterceptionInformation ballInterceptionInformation)
	{
		return ballInterceptionInformation.toBuilder()
				.interceptionCorridors(new ArrayList<>(ballInterceptionInformation.getInterceptionCorridors()))
				.zeroAxisChanges(new ArrayList<>(ballInterceptionInformation.getZeroAxisChanges()))
				.initialIterations(new ArrayList<>(ballInterceptionInformation.getInitialIterations()))
				.build();
	}
}
