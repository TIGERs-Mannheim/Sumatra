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
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionTree;
import edu.tigers.sumatra.ai.metis.offense.action.situation.EOffensiveSituation;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.offensive.view.OffensiveActionTreePanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * OffensiveActionTree Presenter
 *
 * @author Marius Messerschmidt <marius.messserschmidt@dlr.de>
 */
public class OffensiveActionTreePresenter extends ASumatraViewPresenter implements IVisualizationFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(OffensiveStrategyPresenter.class.getName());
	private final OffensiveActionTreePanel offensiveActionTreePanel;
	
	
	/**
	 * Default
	 */
	public OffensiveActionTreePresenter()
	{
		offensiveActionTreePanel = new OffensiveActionTreePanel();
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
                handleActive();
                break;
			case NOT_LOADED:
				break;
			case RESOLVED:
                handleResolved();
                break;
		}
	}

    private void handleActive() {
        try
        {
            Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.class);
            agent.addVisObserver(this);
        } catch (ModuleNotFoundException err)
        {
            log.error("Could not get agent module", err);
        }
    }

    private void handleResolved() {
        try
        {
            Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.class);
            agent.removeVisObserver(this);
        } catch (ModuleNotFoundException err)
        {
            log.error("Could not get agent module", err);
        }
    }


    @Override
	public Component getComponent()
	{
		return offensiveActionTreePanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return offensiveActionTreePanel;
	}
	
	
	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		Map<EOffensiveSituation, OffensiveActionTree> map = frame.getActionTrees();
		offensiveActionTreePanel.setActionTree(frame.getTeamColor(), map);
		offensiveActionTreePanel.setCurrentPath(frame.getTeamColor(), frame.getCurrentPath());
	}
}
