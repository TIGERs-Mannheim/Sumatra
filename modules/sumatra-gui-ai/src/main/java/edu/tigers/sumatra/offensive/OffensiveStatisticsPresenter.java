/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive;

import java.awt.Component;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
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
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * OffensiveStrategy Presenter
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveStatisticsPresenter extends ASumatraViewPresenter implements IVisualizationFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(OffensiveStatisticsPresenter.class.getName());
	private final OffensiveStatisticsPanel offensiveStrategyPanel;
	
	
	/**
	 * present offensive frames
	 */
	public OffensiveStatisticsPresenter()
	{
		offensiveStrategyPanel = new OffensiveStatisticsPanel();
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				start();
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				stop();
				break;
		}
	}
	
	
	private void stop()
	{
		try
		{
			Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.class);
			agent.removeVisObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get agent module", err);
		}
	}
	
	
	private void start()
	{
		try
		{
			Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.class);
			agent.addVisObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get agent module", err);
		}
	}
	
	
	@Override
	public Component getComponent()
	{
		return offensiveStrategyPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return offensiveStrategyPanel;
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
				strategyPanel = offensiveStrategyPanel.getBlueStrategyPanel();
			} else
			{
				strategyPanel = offensiveStrategyPanel.getYellowStrategyPanel();
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
	
	
	private void handleCurrentFrameOnly(final OffensiveStatisticsFrame rawFrame,
			final TeamOffensiveStatisticsPanel strategyPanel)
	{
		if (rawFrame != null)
		{
			strategyPanel.setMaxMinDesired(
					rawFrame.getDesiredNumBots());
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
}
