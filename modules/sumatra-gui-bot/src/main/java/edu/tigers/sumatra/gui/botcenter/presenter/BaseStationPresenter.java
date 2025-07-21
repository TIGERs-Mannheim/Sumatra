/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.botcenter.presenter;

import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.communication.ENetworkState;
import edu.tigers.sumatra.gui.botcenter.view.basestation.BaseStationControlPanel.IBaseStationControlPanelObserver;
import edu.tigers.sumatra.gui.botcenter.view.basestation.BaseStationPanel;
import edu.tigers.sumatra.util.UiThrottler;
import edu.tigers.sumatra.views.ISumatraPresenter;
import lombok.Getter;

import javax.swing.SwingUtilities;


/**
 * The presenter for the base station
 */
public class BaseStationPresenter
		implements ISumatraPresenter, IBaseStationControlPanelObserver
{
	private final TigersBotManager botManager;
	private final TigersBaseStation baseStation;
	@Getter
	private final BaseStationPanel bsPanel;
	private final UiThrottler wifiStatsThrottler = new UiThrottler(1000);
	private final UiThrottler ethStatsThrottler = new UiThrottler(1000);


	public BaseStationPresenter(final TigersBotManager botManager)
	{
		this.botManager = botManager;
		this.baseStation = botManager.getBaseStation();
		bsPanel = new BaseStationPanel();
		wifiStatsThrottler.start();
		ethStatsThrottler.start();
	}


	@Override
	public void onConnectionChange(final boolean connect)
	{
		if (connect)
		{
			baseStation.connect();
		} else
		{
			baseStation.disconnect();
		}
	}


	private void onNewBaseStationWifiStats(final BaseStationWifiStats stats)
	{
		wifiStatsThrottler.execute(() -> SwingUtilities.invokeLater(() -> bsPanel.getWifiStatsPanel().setStats(stats)));
	}


	private void onNewBaseStationEthStats(final BaseStationEthStats stats)
	{
		ethStatsThrottler.execute(() -> SwingUtilities.invokeLater(() -> {
			bsPanel.getEthStatsPanel().setStats(stats);
			bsPanel.getNtpStatsPanel().setStats(stats);
		}));
	}


	private void onNetworkStateChanged(final ENetworkState oldState, final ENetworkState newState)
	{
		SwingUtilities.invokeLater(() -> bsPanel.getControlPanel().setConnectionState(newState));
	}


	private void onNewPingDelay(final double delay)
	{
		SwingUtilities.invokeLater(() -> bsPanel.getControlPanel().setPingDelay(delay));
	}


	@Override
	public void onStartPing(final int numPings, final int payload)
	{
		botManager.getLatencyTester().startPing(numPings, payload);
	}


	@Override
	public void onStopPing()
	{
		botManager.getLatencyTester().stopPing();
	}


	@Override
	public void onModuliStarted()
	{
		onNetworkStateChanged(ENetworkState.OFFLINE, baseStation.getNetworkState().get());
		botManager.getOnNewBaseStationWifiStats()
				.subscribe(getClass().getCanonicalName(), this::onNewBaseStationWifiStats);
		botManager.getOnNewBaseStationEthStats()
				.subscribe(getClass().getCanonicalName(), this::onNewBaseStationEthStats);
		baseStation.getNetworkState().subscribe(getClass().getCanonicalName(), this::onNetworkStateChanged);
		botManager.getLatencyTester().getOnNewPingDelay().subscribe(getClass().getCanonicalName(), this::onNewPingDelay);
		bsPanel.getControlPanel().addObserver(this);
	}


	@Override
	public void onModuliStopped()
	{
		botManager.getOnNewBaseStationWifiStats().unsubscribe(getClass().getCanonicalName());
		botManager.getOnNewBaseStationEthStats().unsubscribe(getClass().getCanonicalName());
		baseStation.getNetworkState().unsubscribe(getClass().getCanonicalName());
		botManager.getLatencyTester().getOnNewPingDelay().unsubscribe(getClass().getCanonicalName());
		bsPanel.getControlPanel().removeObserver(this);
		onNetworkStateChanged(baseStation.getNetworkState().get(), ENetworkState.OFFLINE);
	}
}
