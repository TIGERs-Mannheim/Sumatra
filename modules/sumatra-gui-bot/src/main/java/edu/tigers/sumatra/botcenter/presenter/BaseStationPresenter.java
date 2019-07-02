/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botcenter.presenter;

import java.awt.EventQueue;

import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.botcenter.view.basestation.BaseStationControlPanel.IBaseStationControlPanelObserver;
import edu.tigers.sumatra.botcenter.view.basestation.BaseStationPanel;
import edu.tigers.sumatra.botmanager.basestation.ITigersBaseStationObserver;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationEthStats;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStats;
import edu.tigers.sumatra.botmanager.communication.ENetworkState;
import edu.tigers.sumatra.util.UiThrottler;


/**
 * The presenter for the base station
 */
public class BaseStationPresenter implements IBaseStationControlPanelObserver, ITigersBaseStationObserver,
		IModuliStateObserver
{
	private final TigersBaseStation baseStation;
	private final FirmwareUpdatePresenter firmwareUpdatePresenter;
	private final BaseStationPanel bsPanel;
	private final UiThrottler wifiStatsThrottler = new UiThrottler(1000);
	private final UiThrottler ethStatsThrottler = new UiThrottler(1000);


	public BaseStationPresenter(final TigersBaseStation baseStation)
	{
		this.baseStation = baseStation;
		bsPanel = new BaseStationPanel();
		firmwareUpdatePresenter = new FirmwareUpdatePresenter(bsPanel.getFirmwareUpdatePanel(), baseStation);
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


	@Override
	public void onNewBaseStationWifiStats(final BaseStationWifiStats stats)
	{
		wifiStatsThrottler.execute(() -> bsPanel.getWifiStatsPanel().setStats(stats));
	}


	@Override
	public void onNewBaseStationEthStats(final BaseStationEthStats stats)
	{
		ethStatsThrottler.execute(() -> {
			bsPanel.getEthStatsPanel().setStats(stats);
			bsPanel.getNtpStatsPanel().setStats(stats);
		});
	}


	@Override
	public void onNetworkStateChanged(final ENetworkState netState)
	{
		EventQueue.invokeLater(() -> bsPanel.getControlPanel().setConnectionState(netState));
	}


	@Override
	public void onNewPingDelay(final double delay)
	{
		EventQueue.invokeLater(() -> bsPanel.getControlPanel().setPingDelay(delay));
	}


	@Override
	public void onStartPing(final int numPings, final int payload)
	{
		baseStation.startPing(numPings, payload);
	}


	@Override
	public void onStopPing()
	{
		baseStation.stopPing();
	}


	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				EventQueue.invokeLater(() -> bsPanel.getControlPanel().setConnectionState(baseStation.getNetState()));
				baseStation.addTigersBsObserver(this);
				bsPanel.getControlPanel().addObserver(this);
				break;
			case RESOLVED:
				baseStation.removeTigersBsObserver(this);
				bsPanel.getControlPanel().removeObserver(this);
				EventQueue.invokeLater(() -> bsPanel.getControlPanel().setConnectionState(ENetworkState.OFFLINE));
				break;
			case NOT_LOADED:
			default:
				break;
		}
		firmwareUpdatePresenter.onModuliStateChanged(state);
	}


	/**
	 * @return the bsPanel
	 */
	public final BaseStationPanel getBsPanel()
	{
		return bsPanel;
	}
}
