/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.timer.presenter;

import edu.tigers.sumatra.gui.timer.view.TimerChartPanel;
import edu.tigers.sumatra.gui.timer.view.TimerPanel;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.timer.ITimerObserver;
import edu.tigers.sumatra.timer.SumatraTimer;
import edu.tigers.sumatra.timer.TimerInfo;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;


/**
 * Presenter for the Timer-GUI
 */
@Log4j2
public class TimerPresenter implements ISumatraViewPresenter, ITimerObserver
{
	private static final int TIMER_UPDATE_RATE = 100;

	@Getter
	private final TimerPanel viewPanel = new TimerPanel();
	private final TimerChartPanel chartPanel = viewPanel.getChartPanel();

	private SumatraTimer timer;
	private Timer updateTimer;


	@Override
	public void onModuliStarted()
	{
		ISumatraViewPresenter.super.onModuliStarted();

		final Optional<SumatraTimer> timerOpt = SumatraModel.getInstance().getModuleOpt(SumatraTimer.class);
		if (timerOpt.isPresent())
		{
			timer = timerOpt.get();
			timer.addObserver(this);

			updateTimer = new Timer(TIMER_UPDATE_RATE, new Runner());
			updateTimer.start();
		}
	}


	@Override
	public void onModuliStopped()
	{
		ISumatraViewPresenter.super.onModuliStopped();

		if (updateTimer != null)
		{
			updateTimer.stop();
		}
		if (timer != null)
		{
			timer.removeObserver(this);
		}

		SwingUtilities.invokeLater(chartPanel::clearChart);
	}


	@Override
	public void onShown()
	{
		SwingUtilities.invokeLater(() -> chartPanel.setVisible(true));
	}


	@Override
	public void onNewTimerInfo(final TimerInfo info)
	{
		SwingUtilities.invokeLater(() -> chartPanel.onNewTimerInfo(info));
	}


	private class Runner implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (timer == null)
			{
				return;
			}
			try
			{
				TimerInfo info = timer.getTimerInfo();
				SwingUtilities.invokeLater(() -> chartPanel.onNewTimerInfo(info));
			} catch (Exception err)
			{
				log.error("Error in TimerRunner", err);
			}
		}
	}
}
