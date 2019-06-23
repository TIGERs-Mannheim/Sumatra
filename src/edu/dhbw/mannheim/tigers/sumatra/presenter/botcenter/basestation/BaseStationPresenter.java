/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.06.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.basestation;

import java.awt.EventQueue;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStationObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationEthStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationWifiStats;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationControlPanel.IBaseStationControlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationPanel.IBaseStationPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationStatsPanel;


/**
 * Base station presenter.
 * 
 * @author AndreR
 */
public class BaseStationPresenter implements IBaseStationPanelObserver, IBaseStationObserver,
		ILookAndFeelStateObserver, IBaseStationControlPanelObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private BaseStationPanel		baseStationPanel	= null;
	private final BaseStation		baseStation;
	private BotCenterTreeNode		treeNode				= null;
	private BaseStationStatsPanel	statsPanel			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param station
	 */
	public BaseStationPresenter(final BaseStation station)
	{
		baseStationPanel = new BaseStationPanel();
		statsPanel = new BaseStationStatsPanel();
		
		baseStation = station;
		
		baseStationPanel.setNetCfg(baseStation.getHost(), baseStation.getDstPort(), baseStation.getLocalPort());
		baseStationPanel.getNetworkPanel().setConnectionState(baseStation.getNetState());
		
		baseStation.addObserver(this);
		baseStationPanel.addObserver(this);
		baseStationPanel.getNetworkPanel().addObserver(this);
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		
		treeNode = new BotCenterTreeNode("Base Station", ETreeIconType.AP, baseStationPanel, true);
		treeNode.add(new BotCenterTreeNode("Status", ETreeIconType.GRAPH, statsPanel, true));
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public void delete()
	{
		LookAndFeelStateAdapter.getInstance().removeObserver(this);
		baseStationPanel.getNetworkPanel().removeObserver(this);
		baseStationPanel.removeObserver(this);
		baseStation.removeObserver(this);
	}
	
	
	/**
	 * @return
	 */
	public JPanel getBaseStationPanel()
	{
		return baseStationPanel;
	}
	
	
	@Override
	public void onLookAndFeelChanged()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				SwingUtilities.updateComponentTreeUI(baseStationPanel);
			}
		});
	}
	
	
	@Override
	public void onNetCfgChanged(final String host, final int dstPort, final int localPort)
	{
		baseStation.setIpConfig(host, dstPort, localPort);
	}
	
	
	@Override
	public void onIncommingBotCommand(final BotID id, final ACommand command)
	{
	}
	
	
	@Override
	public void onIncommingBaseStationCommand(final ACommand command)
	{
	}
	
	
	@Override
	public void onNewBaseStationStats(final BaseStationStats stats)
	{
	}
	
	
	@Override
	public void onNewBaseStationWifiStats(final BaseStationWifiStats stats)
	{
	}
	
	
	@Override
	public void onNewBaseStationEthStats(final BaseStationEthStats stats)
	{
	}
	
	
	@Override
	public void onNetworkStateChanged(final ENetworkState netState)
	{
		EventQueue.invokeLater(() -> {
			baseStationPanel.getNetworkPanel().setConnectionState(netState);
		});
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
	public void onNewPingDelay(final float delay)
	{
		EventQueue.invokeLater(() -> baseStationPanel.setPingDelay(delay));
	}
	
	
	@Override
	public void onConnectionChange(final boolean connect)
	{
		if (connect)
		{
			baseStation.setActive(true);
			baseStation.connect();
		} else
		{
			baseStation.setActive(false);
			baseStation.disconnect();
		}
	}
	
	
	@Override
	public void onCfgChanged(final int ch, final int rate, final int bots, final int to)
	{
	}
	
	
	@Override
	public void onBotOffline(final BotID id)
	{
	}
	
	
	@Override
	public void onBotOnline(final BotID id)
	{
	}
	
	
	/**
	 * @return the treeNode
	 */
	public BotCenterTreeNode getTreeNode()
	{
		return treeNode;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
