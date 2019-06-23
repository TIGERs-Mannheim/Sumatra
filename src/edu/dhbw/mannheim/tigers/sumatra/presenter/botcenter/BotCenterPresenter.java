/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.GenericManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.EBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeManual;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Device;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.basestation.BaseStationPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots.ABotPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots.BotPresenterFactory;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.BotCenterPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.IBotTreeObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.NetworkSummaryPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.NetworkSummaryPanel.INetworkSummaryPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.NewBotPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.OverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.FastKickerConfigOverview;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.FastKickerConfigOverview.IFastKickerConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * Presenter for the bot center.
 * 
 * @author AndreR
 */
public class BotCenterPresenter implements ISumatraViewPresenter, IBotTreeObserver,
		IBotCenterPresenter, ILookAndFeelStateObserver, IBotManagerObserver, INetworkSummaryPanelObserver,
		IFastKickerConfigObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log						= Logger.getLogger(BotCenterPresenter.class.getName());
	
	
	private BotCenterPanel					panel						= null;
	// Nodes
	private BotCenterTreeNode				rootNode					= null;
	private BotCenterTreeNode				mcastStatsNode			= null;
	private BotCenterTreeNode				fastChgNode				= null;
	private List<BotCenterTreeNode>		baseStationNode		= new ArrayList<BotCenterTreeNode>();
	
	/** Guarded by <code>this</code> */
	private ABotManager						botManager				= null;
	private final List<ABotPresenter>	botPresenters			= new ArrayList<ABotPresenter>();
	private OverviewPanel					overviewPanel			= null;
	private DefaultTreeModel				model						= null;
	private NetworkSummaryPanel			netSummaryPanel		= null;
	private TimerTask							netStatsUpdater		= null;
	private FastKickerConfigOverview		fastChgPanel			= null;
	private List<BaseStationPresenter>	baseStationPresenter	= new ArrayList<BaseStationPresenter>();
	
	private boolean							networkStatsActive	= false;
	
	private ModulesState						currentState			= ModulesState.NOT_LOADED;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public BotCenterPresenter()
	{
		overviewPanel = new OverviewPanel();
		
		final JScrollPane pane = new JScrollPane(overviewPanel);
		pane.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		rootNode = new BotCenterTreeNode("Overview", ETreeIconType.ROOT, pane, true);
		
		panel = new BotCenterPanel(rootNode);
		
		panel.getTreePanel().addObserver(this);
		
		model = (DefaultTreeModel) panel.getTreePanel().getTreeModel();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	private void addBotPresenter(final ABot bot)
	{
		final ABotPresenter presenter = BotPresenterFactory.createBotPresenter(bot);
		
		rootNode.add(presenter.getTreeNode());
		overviewPanel.addBotPanel(bot.getBotID(), presenter.getSummaryPanel());
		fastChgPanel.addBotPanel(bot.getBotID(), presenter.getFastChgPanel());
		
		botPresenters.add(presenter);
	}
	
	
	@Override
	public void reloadNode(final DefaultMutableTreeNode node)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				((DefaultTreeModel) panel.getTreePanel().getTreeModel()).nodeChanged(node);
			}
		});
	}
	
	
	@Override
	public void onItemSelected(final BotCenterTreeNode node)
	{
		if ((node == null))
		{
			return;
		}
		if ((node.getIconType() == ETreeIconType.AP) && (node.getLevel() == 1))
		{
			networkStatsActive = true;
		} else
		{
			networkStatsActive = false;
		}
		if ((node.getIconType() == ETreeIconType.AP) && (node.getLevel() == 2))
		{
			for (ABotPresenter presenter : botPresenters)
			{
				String title = ((BotCenterTreeNode) node.getParent()).getTitle();
				if (title.equals(presenter.getBot().getName()))
				{
					presenter.setStatsActive(true);
				} else
				{
					presenter.setStatsActive(false);
				}
			}
		} else
		{
			for (ABotPresenter presenter : botPresenters)
			{
				presenter.setStatsActive(false);
			}
		}
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState newState)
	{
		if ((newState != ModulesState.NOT_LOADED))
		{
			// init
			try
			{
				botManager = (ABotManager) SumatraModel.getInstance().getModule("botmanager");
			} catch (final ModuleNotFoundException err)
			{
				log.error("Botmanager not found");
				return;
			}
			
			if (!(botManager instanceof GenericManager))
			{
				return;
			}
		}
		
		switch (newState)
		{
			case ACTIVE:
				for (Map.Entry<EBaseStation, BaseStation> entry : botManager.getBaseStations().entrySet())
				{
					int key = entry.getKey() == EBaseStation.PRIMARY ? 0 : 1;
					BaseStation baseStation = entry.getValue();
					BaseStationPresenter presenter = new BaseStationPresenter(baseStation);
					baseStationPresenter.add(presenter);
					presenter.getTreeNode().setTitle("Base Station (" + key + ")");
					baseStationNode.add(presenter.getTreeNode());
					rootNode.add(presenter.getTreeNode());
				}
				
				netSummaryPanel = new NetworkSummaryPanel();
				fastChgPanel = new FastKickerConfigOverview();
				
				mcastStatsNode = new BotCenterTreeNode("Multicast", ETreeIconType.AP, netSummaryPanel, true);
				rootNode.add(mcastStatsNode);
				
				fastChgNode = new BotCenterTreeNode("Fast Charge", ETreeIconType.KICK, fastChgPanel, true);
				rootNode.add(fastChgNode);
				
				model.reload(rootNode);
				
				LookAndFeelStateAdapter.getInstance().addObserver(this);
				fastChgPanel.addObserver(this);
				
				// Get initial state
				for (final ABot bot : botManager.getAllBots().values())
				{
					onBotAdded(bot);
				}
				
				botManager.addObserver(this);
				netSummaryPanel.addObserver(this);
				
				netSummaryPanel.setEnableMulticast(botManager.getUseMulticast());
				netSummaryPanel.setSleepTime(botManager.getUpdateAllSleepTime());
				
				overviewPanel.setActive(true);
				
				netStatsUpdater = new NetworkStatisticsUpdater();
				GeneralPurposeTimer.getInstance().scheduleAtFixedRate(netStatsUpdater, 0, 1000);
				break;
			case RESOLVED:
				if (currentState != ModulesState.ACTIVE)
				{
					break;
				}
				LookAndFeelStateAdapter.getInstance().removeObserver(this);
				
				botManager.removeObserver(this);
				
				for (BaseStationPresenter presenter : baseStationPresenter)
				{
					presenter.delete();
				}
				baseStationPresenter.clear();
				
				netStatsUpdater.cancel();
				netStatsUpdater = null;
				
				netSummaryPanel.removeObserver(this);
				netSummaryPanel = null;
				
				model.removeNodeFromParent(mcastStatsNode);
				mcastStatsNode = null;
				model.removeNodeFromParent(fastChgNode);
				fastChgNode = null;
				fastChgPanel = null;
				for (BotCenterTreeNode node : baseStationNode)
				{
					model.removeNodeFromParent(node);
				}
				baseStationNode.clear();
				
				overviewPanel.setActive(false);
				break;
			case NOT_LOADED:
				break;
			default:
				break;
		
		}
		currentState = newState;
	}
	
	
	@Override
	public void onNodeRightClicked(final BotCenterTreeNode node)
	{
		if (botManager == null)
		{
			return;
		}
		
		if ((node != null) && (node.getIconType() == ETreeIconType.BOT))
		{
			panel.getTreePanel().showAddRemoveContextMenu();
		} else
		{
			panel.getTreePanel().showAddContextMenu();
		}
	}
	
	
	@Override
	public void onLookAndFeelChanged()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// SwingUtilities.updateComponentTreeUI(panel);
				// SwingUtilities.updateComponentTreeUI(overviewPanel);
				// SwingUtilities.updateComponentTreeUI(netSummaryPanel);
				// SwingUtilities.updateComponentTreeUI(fastChgPanel);
			}
		});
	}
	
	
	@Override
	public void onAddBot()
	{
		if (botManager == null)
		{
			return;
		}
		
		
		final NewBotPanel newBot = new NewBotPanel(EBotType.getBotTypeMap());
		
		newBot.setVisible(true);
		
		if (newBot.isDataValid())
		{
			botManager.addBot(newBot.getEBotType(), newBot.getId(), newBot.getName());
		}
	}
	
	
	@Override
	public void onRemoveBot(final BotCenterTreeNode node)
	{
		if (botManager == null)
		{
			return;
		}
		
		for (final ABotPresenter presenter : botPresenters)
		{
			if (presenter.getTreeNode().equals(node))
			{
				botManager.removeBot(presenter.getBot().getBotID());
				
				break;
			}
		}
	}
	
	
	@Override
	public void onBotAdded(final ABot bot)
	{
		if (botManager == null)
		{
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				addBotPresenter(bot);
				
				model.reload(rootNode);
			}
		});
	}
	
	
	@Override
	public void onBotRemoved(final ABot bot)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (final ABotPresenter presenter : botPresenters)
				{
					synchronized (model)
					{
						if (presenter.getBot().getBotID() == bot.getBotID())
						{
							overviewPanel.removeBotPanel(bot.getBotID());
							if (fastChgPanel != null)
							{
								fastChgPanel.removeBotPanel(presenter.getFastChgPanel());
							}
							
							presenter.delete();
							
							model.removeNodeFromParent(presenter.getTreeNode());
							
							botPresenters.remove(presenter);
							
							panel.onItemSelected(rootNode);
							
							break;
						}
					}
					
					model.reload(rootNode);
				}
			}
		});
	}
	
	
	@Override
	public void onBotIdChanged(final BotID oldId, final BotID newId)
	{
	}
	
	
	@Override
	public void onEnableMulticastChanged(final boolean multicast)
	{
		botManager.setUseMulticast(multicast);
	}
	
	
	@Override
	public void onSleepTimeChanged(final long time)
	{
		botManager.setUpdateAllSleepTime(time);
	}
	
	
	@Override
	public void onSetAutoChg(final BotID botId)
	{
		// auto charge is always enabled, nothing todo
	}
	
	
	@Override
	public void onSetChgAll()
	{
		synchronized (BotCenterPresenter.this)
		{
			botManager.chargeAll();
		}
	}
	
	
	@Override
	public void onDischargeAll()
	{
		for (final ABot bot : botManager.getAllBots().values())
		{
			if ((bot.getType() == EBotType.TIGER) || (bot.getType() == EBotType.GRSIM))
			{
				bot.execute(new TigerKickerChargeManual(6, 78, 5));
				bot.execute(new TigerKickerKickV2(Device.STRAIGHT, EKickerMode.FORCE, 10000));
			}
		}
	}
	
	
	private class NetworkStatisticsUpdater extends TimerTask
	{
		private Statistics	lastTxStats	= new Statistics();
		private Statistics	lastRxStats	= new Statistics();
		
		
		@Override
		public void run()
		{
			if (!networkStatsActive)
			{
				return;
			}
			ABotManager localBotManager = null;
			synchronized (BotCenterPresenter.this)
			{
				localBotManager = botManager;
			}
			if (localBotManager == null)
			{
				return;
			}
			
			final Collection<ABot> bots = localBotManager.getAllBots().values();
			
			Statistics txStats = new Statistics();
			Statistics rxStats = new Statistics();
			
			for (final ABot bot : bots)
			{
				if ((bot.getType() == EBotType.TIGER))
				{
					final TigerBot tiger = (TigerBot) bot;
					txStats = txStats.add(tiger.getTransceiver().getTransmitterStats());
					rxStats = rxStats.add(tiger.getTransceiver().getReceiverStats());
				}
			}
			
			final ITransceiverUDP mcastTransceiver = localBotManager.getMulticastTransceiver(0);
			
			if (mcastTransceiver != null)
			{
				txStats = txStats.add(mcastTransceiver.getTransmitterStats());
				rxStats = rxStats.add(mcastTransceiver.getReceiverStats());
			}
			
			netSummaryPanel.setTxStat(txStats.substract(lastTxStats));
			netSummaryPanel.setRxStat(rxStats.substract(lastRxStats));
			
			lastTxStats = new Statistics(txStats);
			lastRxStats = new Statistics(rxStats);
		}
	}
	
	
	@Override
	public void onBotConnectionChanged(final ABot bot)
	{
	}
	
	
	@Override
	public Component getComponent()
	{
		return panel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return panel;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
}
