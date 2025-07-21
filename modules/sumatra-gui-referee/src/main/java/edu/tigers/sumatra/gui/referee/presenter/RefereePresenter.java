/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.referee.presenter;

import edu.tigers.sumatra.gui.referee.view.IRefBoxRemoteControlRequestObserver;
import edu.tigers.sumatra.gui.referee.view.RefereePanel;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.proto.SslGcApi;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.SwingUtilities;

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
	public void onModuliStarted()
	{
		ISumatraViewPresenter.super.onModuliStarted();
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
	public void onModuliStopped()
	{
		ISumatraViewPresenter.super.onModuliStopped();
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
		SwingUtilities.invokeLater(() -> {
			viewPanel.getShowRefereeMsgPanel().update(msg);
			viewPanel.getTeamsPanel().values().forEach(t -> t.update(msg));
		});
	}


	@Override
	public void onRefereeMsgSourceChanged(final ARefereeMessageSource src)
	{
		SwingUtilities.invokeLater(
				() -> viewPanel.setEnable(referee != null && src.getType() == ERefereeMessageSource.CI));
	}


	@Override
	public void sendGameControllerEvent(final SslGcApi.Input event)
	{
		referee.sendGameControllerEvent(event);
	}
}
