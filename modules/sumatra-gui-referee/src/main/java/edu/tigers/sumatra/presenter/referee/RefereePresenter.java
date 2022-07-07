/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.referee;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.proto.SslGcApi;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.view.referee.IRefBoxRemoteControlRequestObserver;
import edu.tigers.sumatra.view.referee.RefereePanel;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;


/**
 * This is the presenter for the referee in sumatra.
 */
@Log4j2
public class RefereePresenter
		implements ISumatraViewPresenter, IRefereeObserver, IRefBoxRemoteControlRequestObserver
{
	@Getter
	private final RefereePanel viewPanel = new RefereePanel();
	private AReferee referee;


	@Override
	public void onStartModuli()
	{
		ISumatraViewPresenter.super.onStartModuli();
		viewPanel.getCommonCommandsPanel().addObserver(this);
		viewPanel.getChangeStatePanel().addObserver(this);
		viewPanel.getTeamsPanel().values().forEach(p -> p.addObserver(this));
		SumatraModel.getInstance().getModuleOpt(AReferee.class).ifPresent(ref -> {
			referee = ref;
			referee.addObserver(this);
			onRefereeMsgSourceChanged(referee.getActiveSource());
		});
	}


	@Override
	public void onStopModuli()
	{
		ISumatraViewPresenter.super.onStopModuli();
		if (referee != null)
		{
			referee.removeObserver(this);
			referee = null;
		}

		viewPanel.getCommonCommandsPanel().removeObserver(this);
		viewPanel.getChangeStatePanel().removeObserver(this);
		viewPanel.getTeamsPanel().values().forEach(p -> p.removeObserver(this));
	}


	@Override
	public void onNewRefereeMsg(final Referee msg)
	{
		viewPanel.getShowRefereeMsgPanel().update(msg);
		viewPanel.getTeamsPanel().values().forEach(t -> t.update(msg));
	}


	@Override
	public void onRefereeMsgSourceChanged(final ARefereeMessageSource src)
	{
		viewPanel.setEnable(referee != null && src.getType() == ERefereeMessageSource.CI);
	}


	@Override
	public void sendGameControllerEvent(final SslGcApi.Input event)
	{
		referee.sendGameControllerEvent(event);
	}
}
