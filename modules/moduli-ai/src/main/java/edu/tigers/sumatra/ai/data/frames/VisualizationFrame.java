/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data.frames;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.data.MatchStats;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAnalysedFrame;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 5)
public class VisualizationFrame
{
	@SuppressWarnings("unused") // required for compatibility with older versions
	private ETeamColor teamColor;
	private EAiTeam aiTeam;
	
	private final boolean inverted;
	
	private final ShapeMap shapes;
	
	private transient WorldFrameWrapper worldFrameWrapper = null;
	
	private final MatchStats matchStats;
	
	private final OffensiveStrategy offensiveStrategy;
	private final Map<BotID, OffensiveAction> offensiveActions = new HashMap<>();
	private final Map<BotID, BotAiInformation> aiInfos = new HashMap<>();
	private final Map<EPlay, RoleFinderInfo> roleFinderInfos = new EnumMap<>(EPlay.class);
	private final OffensiveAnalysedFrame offensiveStatisticsFrame;
	private final OffensiveStatisticsFrame offensiveStatisticsFrameRaw;
	
	
	@SuppressWarnings("unused")
	private VisualizationFrame()
	{
		aiTeam = null;
		inverted = false;
		matchStats = null;
		offensiveStrategy = null;
		shapes = new ShapeMap();
		offensiveStatisticsFrame = null;
		offensiveStatisticsFrameRaw = null;
	}
	
	
	/**
	 * @param aiFrame
	 */
	public VisualizationFrame(final AIInfoFrame aiFrame)
	{
		shapes = new ShapeMap(aiFrame.getTacticalField().getDrawableShapes());
		worldFrameWrapper = aiFrame.getWorldFrameWrapper();
		aiTeam = aiFrame.getAiTeam();
		inverted = aiFrame.getWorldFrame().isInverted();
		matchStats = aiFrame.getTacticalField().getMatchStatistics();
		offensiveStrategy = aiFrame.getTacticalField().getOffensiveStrategy();
		offensiveActions.putAll(aiFrame.getTacticalField().getOffensiveActions());
		aiInfos.putAll(aiFrame.getAresData().getBotAiInformation());
		roleFinderInfos.putAll(aiFrame.getTacticalField().getRoleFinderInfos());
		offensiveStatisticsFrame = aiFrame.getTacticalField().getAnalyzedOffensiveStatisticsFrame();
		offensiveStatisticsFrameRaw = aiFrame.getTacticalField().getOffensiveStatistics();
	}
	
	
	/**
	 * @param aiFrame
	 */
	public VisualizationFrame(final VisualizationFrame aiFrame)
	{
		shapes = new ShapeMap(aiFrame.getShapes());
		worldFrameWrapper = new WorldFrameWrapper(aiFrame.worldFrameWrapper);
		aiTeam = aiFrame.aiTeam;
		inverted = aiFrame.getWorldFrame().isInverted();
		matchStats = aiFrame.getMatchStats();
		offensiveStrategy = aiFrame.getOffensiveStrategy();
		offensiveActions.putAll(aiFrame.getOffensiveActions());
		aiInfos.putAll(aiFrame.getAiInfos());
		roleFinderInfos.putAll(aiFrame.getRoleFinderInfos());
		offensiveStatisticsFrame = aiFrame.getOffensiveStatisticsFrame();
		offensiveStatisticsFrameRaw = aiFrame.getOffensiveStatisticsFrameRaw();
	}
	
	
	/**
	 * @return the timestamp
	 */
	public final long getTimestamp()
	{
		return worldFrameWrapper.getSimpleWorldFrame().getTimestamp();
	}
	
	
	/**
	 * @return the shapes
	 */
	public final ShapeMap getShapes()
	{
		return shapes;
	}
	
	
	/**
	 * @return the refereeMsg
	 */
	public final RefereeMsg getLatestRefereeMsg()
	{
		return worldFrameWrapper.getRefereeMsg();
	}
	
	
	/**
	 * @return the worldFrame
	 */
	public final SimpleWorldFrame getSimpleWorldFrame()
	{
		return worldFrameWrapper.getSimpleWorldFrame();
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
		if (aiTeam == null)
		{
			// set aiTeam for old versions of this frame
			aiTeam = EAiTeam.primary(teamColor);
		}
		return aiTeam;
	}
	
	
	/**
	 * @return the inverted
	 */
	public final boolean isInverted()
	{
		return inverted;
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
	 * @return the worldFrame
	 */
	public final WorldFrame getWorldFrame()
	{
		return worldFrameWrapper.getWorldFrame(aiTeam);
	}
	
	
	/**
	 * @return the aiInfos
	 */
	public final Map<BotID, BotAiInformation> getAiInfos()
	{
		return aiInfos;
	}
	
	
	/**
	 * @return the roleFinderInfo
	 */
	public final Map<EPlay, RoleFinderInfo> getRoleFinderInfos()
	{
		return roleFinderInfos;
	}
	
	
	/**
	 * @return the worldFrameWrapper
	 */
	public final WorldFrameWrapper getWorldFrameWrapper()
	{
		return worldFrameWrapper;
	}
	
	
	/**
	 * @param worldFrameWrapper the worldFrameWrapper to set
	 */
	public final void setWorldFrameWrapper(final WorldFrameWrapper worldFrameWrapper)
	{
		this.worldFrameWrapper = worldFrameWrapper;
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
}
