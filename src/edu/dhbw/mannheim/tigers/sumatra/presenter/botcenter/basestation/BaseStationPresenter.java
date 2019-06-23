/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.basestation;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStationObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationNetworkPanel.IBaseStationNetworkPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationPanel.IBaseStationPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationStatsPanel;


/**
 * Base station presenter.
 * 
 * @author AndreR
 * 
 */
public class BaseStationPresenter implements IBaseStationPanelObserver, IBaseStationObserver,
		ILookAndFeelStateObserver, IBaseStationNetworkPanel
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
	public BaseStationPresenter(BaseStation station)
	{
		baseStationPanel = new BaseStationPanel();
		statsPanel = new BaseStationStatsPanel();
		
		baseStation = station;
		
		baseStationPanel.setNetCfg(baseStation.getHost(), baseStation.getDstPort(), baseStation.getLocalPort());
		baseStationPanel.setConfig(baseStation.getChannel(), baseStation.isPositionInverted(),
				baseStation.getVisionRate(), baseStation.getMaxBots(), baseStation.getTimeout());
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
	 * 
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
	public void onNetCfgChanged(String host, int dstPort, int localPort)
	{
		baseStation.setIpConfig(host, dstPort, localPort);
	}
	
	
	@Override
	public void onIncommingBotCommand(BotID id, ACommand command)
	{
	}
	
	
	@Override
	public void onIncommingBaseStationCommand(ACommand command)
	{
	}
	
	
	@Override
	public void onNewBaseStationStats(BaseStationStats stats)
	{
		statsPanel.setStats(stats);
		baseStationPanel.getNetworkPanel().setStats(stats.getEthStats());
	}
	
	
	@Override
	public void onNetworkStateChanged(ENetworkState netState)
	{
		baseStationPanel.getNetworkPanel().setConnectionState(netState);
	}
	
	
	@Override
	public void onStartPing(int numPings, int payload)
	{
		baseStation.startPing(numPings, payload);
	}
	
	
	@Override
	public void onStopPing()
	{
		baseStation.stopPing();
	}
	
	
	@Override
	public void onNewPingDelay(float delay)
	{
		baseStationPanel.setPingDelay(delay);
	}
	
	
	@Override
	public void onConnectionChange(boolean connect)
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
	public void onCfgChanged(int ch, boolean invertPos, int rate, int bots, int to)
	{
		baseStation.setConfig(ch, invertPos, bots, rate, to);
	}
	
	
	@Override
	public void onBotOffline(BotID id)
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
