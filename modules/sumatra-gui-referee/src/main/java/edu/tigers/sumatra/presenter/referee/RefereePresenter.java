/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.referee;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.control.Event;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.view.referee.IRefBoxRemoteControlRequestObserver;
import edu.tigers.sumatra.view.referee.RefereePanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * This is the presenter for the referee in sumatra.
 * 
 * @author MalteM
 */
public class RefereePresenter extends ASumatraViewPresenter
		implements IRefereeObserver, IRefBoxRemoteControlRequestObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(RefereePresenter.class.getName());
	
	private final RefereePanel refereePanel = new RefereePanel();
	private AReferee referee;
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		if (state == ModulesState.ACTIVE)
		{
			try
			{
				referee = SumatraModel.getInstance().getModule(AReferee.class);
				referee.addObserver(this);
			} catch (final ModuleNotFoundException err)
			{
				log.error("referee Module not found", err);
			}
			
			refereePanel.getCommonCommandsPanel().addObserver(this);
			refereePanel.getChangeStatePanel().addObserver(this);
			refereePanel.getTeamsPanel().values().forEach(p -> p.addObserver(this));
			onRefereeMsgSourceChanged(referee.getActiveSource());
		} else if (state == ModulesState.RESOLVED)
		{
			if (referee != null)
			{
				referee.removeObserver(this);
				referee = null;
			}
			
			refereePanel.getCommonCommandsPanel().removeObserver(this);
			refereePanel.getChangeStatePanel().removeObserver(this);
			refereePanel.getTeamsPanel().values().forEach(p -> p.removeObserver(this));
		}
	}
	
	
	@Override
	public void onNewRefereeMsg(final SSL_Referee msg)
	{
		refereePanel.getShowRefereeMsgPanel().update(msg);
		refereePanel.getTeamsPanel().values().forEach(t -> t.update(msg));
	}
	
	
	@Override
	public void onRefereeMsgSourceChanged(final ARefereeMessageSource src)
	{
		refereePanel.setEnable(referee != null && referee.isControllable());
	}
	
	
	@Override
	public Component getComponent()
	{
		return refereePanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return refereePanel;
	}
	
	
	@Override
	public void sendGameControllerEvent(final Event event)
	{
		referee.sendGameControllerEvent(event);
	}
}
