/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveAnalysedBotFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveAnalysedFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveBotFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.offensive.view.OffensiveStatisticsPanel;
import edu.tigers.sumatra.offensive.view.TeamOffensiveStatisticsPanel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Map;


/**
 * OffensiveStrategy Presenter
 */
@Log4j2
public class OffensiveStatisticsPresenter implements ISumatraViewPresenter, IVisualizationFrameObserver
{
	@Getter
	private final OffensiveStatisticsPanel viewPanel = new OffensiveStatisticsPanel();


	@Override
	public void onStartModuli()
	{
		ISumatraViewPresenter.super.onStartModuli();
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(agent -> agent.addVisObserver(this));
	}


	@Override
	public void onStopModuli()
	{
		ISumatraViewPresenter.super.onStopModuli();
		SumatraModel.getInstance().getModuleOpt(AAgent.class).ifPresent(agent -> agent.removeVisObserver(this));
	}


	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		OffensiveStatisticsFrame rawFrame = frame.getOffensiveStatisticsFrameRaw();
		OffensiveAnalysedFrame oFrame = frame.getOffensiveStatisticsFrame();
		if (oFrame != null)
		{
			TeamOffensiveStatisticsPanel strategyPanel;
			if (frame.getTeamColor() == ETeamColor.BLUE)
			{
				strategyPanel = viewPanel.getBluePanel();
			} else
			{
				strategyPanel = viewPanel.getYellowPanel();
			}
			if (strategyPanel.isCurrentFrameOnly())
			{
				handleCurrentFrameOnly(rawFrame, strategyPanel);
			} else
			{
				handleFrame(oFrame, strategyPanel);
			}
		}
	}


	private void handleFrame(final OffensiveAnalysedFrame oFrame, final TeamOffensiveStatisticsPanel strategyPanel)
	{
		strategyPanel.setMaxMinDesiredAVG(oFrame.getAvgDesiredRoles());
		for (Map.Entry<BotID, OffensiveAnalysedBotFrame> bFrame : oFrame.getBotFrames().entrySet())
		{
			strategyPanel.setBotFrame(bFrame.getKey(), bFrame.getValue());
			if (oFrame.getPrimaryPercantages().containsKey(bFrame.getKey()))
			{
				double val = oFrame.getPrimaryPercantages().get(bFrame.getKey());
				strategyPanel.setPrimaryPercentage(bFrame.getKey(), val);
			}
		}
	}


	private void handleCurrentFrameOnly(
			OffensiveStatisticsFrame rawFrame,
			TeamOffensiveStatisticsPanel strategyPanel
	)
	{
		if (rawFrame == null)
		{
			return;
		}
		strategyPanel.setMaxMinDesired(rawFrame.getDesiredNumBots());
		for (Map.Entry<BotID, OffensiveBotFrame> e : rawFrame.getBotFrames().entrySet())
		{
			strategyPanel.setBotSFrame(e.getKey(), e.getValue());
			BotID primary = rawFrame.getPrimaryOffensiveBot();
			if (e.getKey().equals(primary))
			{
				strategyPanel.setPrimaryPercentage(e.getKey(), 100);
			} else
			{
				strategyPanel.setPrimaryPercentage(e.getKey(), 0);
			}
		}
	}
}
