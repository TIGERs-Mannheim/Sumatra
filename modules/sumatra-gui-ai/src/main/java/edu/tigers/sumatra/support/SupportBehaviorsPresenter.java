/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.support;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.match.SupportPlay;
import edu.tigers.sumatra.ai.pandora.roles.support.ESupportBehavior;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.support.view.SupportBehaviorsPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * SupportBehaviors Presenter
 *
 * @author Marius Messerschmidt <marius.messserschmidt@dlr.de>
 */
public class SupportBehaviorsPresenter extends ASumatraViewPresenter implements IVisualizationFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SupportBehaviorsPresenter.class.getName());
	private final SupportBehaviorsPanel supportBehaviorsPanel;
	
	
	/**
	 * Default
	 */
	public SupportBehaviorsPresenter()
	{
		supportBehaviorsPanel = new SupportBehaviorsPanel();
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
	
	
	private void handleActive()
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
	
	
	private void handleResolved()
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
	
	
	@Override
	public Component getComponent()
	{
		return supportBehaviorsPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return supportBehaviorsPanel;
	}
	
	
	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		ETeamColor color = frame.getTeamColor();

		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			List<ESupportBehavior> inactiveBehaviors = new ArrayList<>();
			if (play.getType() == EPlay.SUPPORT)
			{
				if (!play.getRoles().isEmpty())
				{
					inactiveBehaviors = ((SupportRole) play.getRoles().iterator()
							.next()).getInactiveBehaviors();
				}
				
				
				supportBehaviorsPanel.setViabilityMap(color,
						((SupportPlay) play).getViabilityMap(),
						inactiveBehaviors);
			}
		}
	}
	
}
