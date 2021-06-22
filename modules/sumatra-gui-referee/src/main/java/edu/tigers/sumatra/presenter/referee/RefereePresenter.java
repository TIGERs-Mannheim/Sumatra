/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.referee;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.proto.SslGcApi;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.view.referee.IRefBoxRemoteControlRequestObserver;
import edu.tigers.sumatra.view.referee.RefereePanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Component;


/**
 * This is the presenter for the referee in sumatra.
 *
 * @author MalteM
 */
public class RefereePresenter extends ASumatraViewPresenter
		implements IRefereeObserver, IRefBoxRemoteControlRequestObserver
{
	private static final Logger log = LogManager.getLogger(RefereePresenter.class.getName());

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
	public void onNewRefereeMsg(final Referee msg)
	{
		refereePanel.getShowRefereeMsgPanel().update(msg);
		refereePanel.getTeamsPanel().values().forEach(t -> t.update(msg));
	}


	@Override
	public void onRefereeMsgSourceChanged(final ARefereeMessageSource src)
	{
		refereePanel.setEnable(referee != null && src.getType() == ERefereeMessageSource.CI);
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
	public void sendGameControllerEvent(final SslGcApi.Input event)
	{
		referee.sendGameControllerEvent(event);
	}
}
