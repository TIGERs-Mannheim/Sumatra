/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeManual;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Device;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Mode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkillV2.AccelerationFacade;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkillV2.PIDFacade;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots.ABotPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots.BotPresenterFactory;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.BotCenterPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.IBotTreeObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.MoveSkillPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.MoveSkillPanel.IMoveSkillPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.NetworkSummaryPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.NetworkSummaryPanel.INetworkSummaryPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.NewBotPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.OverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.FastKickerConfigOverview;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.FastKickerConfigOverview.IFastKickerConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * Presenter for the bot center.
 * 
 * @author AndreR
 * 
 */
public class BotCenterPresenter implements ISumatraView, IBotTreeObserver, IModuliStateObserver, IBotCenterPresenter,
		ILookAndFeelStateObserver, IBotManagerObserver, INetworkSummaryPanelObserver, IFastKickerConfigObserver,
		IMoveSkillPanelObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private BotCenterPanel				panel					= null;
	private BotCenterTreeNode			rootNode				= null;
	private Logger							log					= Logger.getLogger(getClass());
	/** Guarded by <code>this</code> */
	private ABotManager					botManager			= null;
	private List<ABotPresenter>		botPresenters		= new ArrayList<ABotPresenter>();
	private OverviewPanel				overviewPanel		= null;
	private DefaultTreeModel			model					= null;
	private JMenu							botConfigMenu		= null;
	private NetworkSummaryPanel		netSummaryPanel	= null;
	private MoveSkillPanel				moveSkillPanel		= null;
	private TimerTask						netStatsUpdater	= null;
	private FastKickerConfigOverview	fastChgPanel		= null;
	
	private List<JMenuItem>				configs				= new ArrayList<JMenuItem>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public BotCenterPresenter()
	{
		botConfigMenu = new JMenu("Botconfiguration");
		JMenuItem saveConfig = new JMenuItem("save");
		JMenuItem deleteConfig = new JMenuItem("remove");
		
		saveConfig.addActionListener(new SaveConfig());
		deleteConfig.addActionListener(new DeleteConfig());
		
		botConfigMenu.add(saveConfig);
		botConfigMenu.add(deleteConfig);
		botConfigMenu.addSeparator();
		
		overviewPanel = new OverviewPanel();
		
		JScrollPane pane = new JScrollPane(overviewPanel);
		pane.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		rootNode = new BotCenterTreeNode("Overview", ETreeIconType.ROOT, pane);
		
		netSummaryPanel = new NetworkSummaryPanel();
		moveSkillPanel = new MoveSkillPanel();
		fastChgPanel = new FastKickerConfigOverview();
		
		BotCenterTreeNode mcastStatsNode = new BotCenterTreeNode("Network", ETreeIconType.AP, netSummaryPanel);
		rootNode.add(mcastStatsNode);
		
		BotCenterTreeNode accSkillPanelNode = new BotCenterTreeNode("Move Skill", ETreeIconType.LAMP, moveSkillPanel);
		rootNode.add(accSkillPanelNode);
		
		BotCenterTreeNode fastChgNode = new BotCenterTreeNode("Fast Charge", ETreeIconType.KICK, fastChgPanel);
		rootNode.add(fastChgNode);
		

		panel = new BotCenterPanel(rootNode);
		
		panel.getTreePanel().addObserver(this);
		
		model = (DefaultTreeModel) panel.getTreePanel().getTreeModel();
		
		ModuliStateAdapter.getInstance().addObserver(this);
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		moveSkillPanel.addObserver(this);
		fastChgPanel.addObserver(this);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	private void addBotPresenter(ABot bot)
	{
		ABotPresenter presenter = BotPresenterFactory.createBotPresenter(bot);
		
		presenter.setBotCenterPresenter(this);
		
		rootNode.add(presenter.getTreeNode());
		overviewPanel.addBotPanel(presenter.getSummaryPanel());
		fastChgPanel.addBotPanel(bot.getBotId(), presenter.getFastChgPanel());
		
		botPresenters.add(presenter);
	}
	

	private void updateConfigMenu()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				for (JMenuItem item : configs)
				{
					botConfigMenu.remove(item);
				}
				
				configs.clear();
				
				synchronized (BotCenterPresenter.this)
				{
					if (botManager != null)
					{
						List<String> files = botManager.getAvailableConfigs();
						
						ButtonGroup group = new ButtonGroup();
						for (String name : files)
						{
							JMenuItem item = new JRadioButtonMenuItem(name);
							group.add(item);
							
							if (ABotManager.getSelectedPersistentConfig() != null
									&& name.equals(ABotManager.getSelectedPersistentConfig()))
							{
								item.setSelected(true);
							}
							
							item.addActionListener(new LoadConfig(name));
							
							configs.add(item);
							botConfigMenu.add(item);
						}
					}
				}
			}
		});
	}
	

	@Override
	public void reloadNode(final DefaultMutableTreeNode node)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				((DefaultTreeModel) panel.getTreePanel().getTreeModel()).nodeChanged(node);
			}
		});
	}
	

	@Override
	public int getID()
	{
		return 2;
	}
	

	@Override
	public String getTitle()
	{
		return "Bot Center";
	}
	

	@Override
	public Component getViewComponent()
	{
		return panel;
	}
	

	@Override
	public List<JMenu> getCustomMenus()
	{
		List<JMenu> menus = new ArrayList<JMenu>();
		
		menus.add(botConfigMenu);
		
		return menus;
	}
	

	@Override
	public void onShown()
	{
	}
	

	@Override
	public void onHidden()
	{
	}
	

	@Override
	public void onFocused()
	{
	}
	

	@Override
	public void onFocusLost()
	{
	}
	

	@Override
	public void onItemSelected(BotCenterTreeNode data)
	{
	}
	

	@Override
	public synchronized void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case RESOLVED:
			{
				try
				{
					botManager = (ABotManager) SumatraModel.getInstance().getModule("botmanager");
					log.debug("Moduli resolved");
					
					botManager.removeObserver(this);
					netSummaryPanel.removeObserver(this);
					botManager.addObserver(this);
					netSummaryPanel.addObserver(this);
					
					netSummaryPanel.setEnableMulticast(botManager.getUseMulticast());
					netSummaryPanel.setSleepTime(botManager.getUpdateAllSleepTime());
					
					updateConfigMenu();
				} catch (ModuleNotFoundException err)
				{
					log.fatal("Botmanager not found");
				}
				
			}
				break;
			case ACTIVE:
			{
				overviewPanel.setActive(true);
				fastChgPanel.setActive(true);
				
				GeneralPurposeTimer.getInstance().scheduleAtFixedRate(netStatsUpdater = new NetworkStatisticsUpdater(), 0,
						1000);
				
				moveSkillPanel.getAccPanel().setAccelerationFacade(AIConfig.getSkills().getAcceleration());
				moveSkillPanel.getPidPanel().setPIDFacade(AIConfig.getSkills().getPids());
			}
				break;
			default:
			{
				if (netStatsUpdater != null)
				{
					netStatsUpdater.cancel();
				}
				netStatsUpdater = null;
				
				if (botManager != null)
				{
					botManager.removeObserver(this);
					netSummaryPanel.removeObserver(this);
				}
				botManager = null;
				
				overviewPanel.setActive(false);
				fastChgPanel.setActive(false);
			}
				break;
		}
	}
	

	@Override
	public synchronized void onNodeRightClicked(BotCenterTreeNode node)
	{
		if (botManager == null)
		{
			return;
		}
		
		if (node != null && node.getIconType() == ETreeIconType.BOT)
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
			public void run()
			{
				SwingUtilities.updateComponentTreeUI(panel);
				SwingUtilities.updateComponentTreeUI(overviewPanel);
				SwingUtilities.updateComponentTreeUI(netSummaryPanel);
				SwingUtilities.updateComponentTreeUI(fastChgPanel);
				SwingUtilities.updateComponentTreeUI(moveSkillPanel);
			}
		});
	}
	

	@Override
	public synchronized void onAddBot()
	{
		if (botManager == null)
		{
			return;
		}
		

		NewBotPanel newBot = new NewBotPanel(botManager.getBotTypeMap());
		
		newBot.setVisible(true);
		
		if (newBot.isDataValid())
		{
			botManager.addBot(newBot.getType(), newBot.getId(), newBot.getName());
		}
	}
	

	@Override
	public synchronized void onRemoveBot(BotCenterTreeNode node)
	{
		if (botManager == null)
		{
			return;
		}
		
		for (ABotPresenter presenter : botPresenters)
		{
			if (presenter.getTreeNode().equals(node))
			{
				botManager.removeBot(presenter.getBot().getBotId());
				
				break;
			}
		}
	}
	

	@Override
	public synchronized void onBotAdded(final ABot bot)
	{
		if (botManager == null)
		{
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable()
		{
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
			public void run()
			{
				for (ABotPresenter presenter : botPresenters)
				{
					if (presenter.getBot().getBotId() == bot.getBotId())
					{
						overviewPanel.removeBotPanel(presenter.getSummaryPanel());
						fastChgPanel.removeBotPanel(presenter.getFastChgPanel());
						
						presenter.delete();
						
						model.removeNodeFromParent(presenter.getTreeNode());
						
						botPresenters.remove(presenter);
						
						panel.onItemSelected(rootNode);
						
						break;
					}
				}
				
				model.reload(rootNode);
			}
		});
	}
	

	@Override
	public void onBotIdChanged(int oldId, int newId)
	{
	}
	

	@Override
	public void onEnableMulticastChanged(boolean multicast)
	{
		botManager.setUseMulticast(multicast);
	}
	

	@Override
	public void onSleepTimeChanged(long time)
	{
		botManager.setUpdateAllSleepTime(time);
	}
	

	@Override
	public void onNewAccelerationFacade(AccelerationFacade accFacade)
	{
		if (AIConfig.isLoaded())
		{
			AIConfig.getSkills().setAcceleration(accFacade);
		}
	}
	

	@Override
	public void onNewPIDFacade(PIDFacade pidFacade)
	{
		if (AIConfig.isLoaded())
		{
			AIConfig.getSkills().setPids(pidFacade);
		}
	}
	

	@Override
	public void onSetAutoChg(int botId)
	{
		for (ABot bot : botManager.getAllBots().values())
		{
			if (bot.getType() == EBotType.TIGER)
			{
				if (bot.getBotId() == botId)
				{
					bot.execute(new TigerKickerChargeAuto());
				}
			}
		}
	}
	

	@Override
	public void onSetChgAll(int chg)
	{
		for (ABot bot : botManager.getAllBots().values())
		{
			if (bot.getType() == EBotType.TIGER)
			{
				bot.execute(new TigerKickerChargeAuto(chg));
			}
		}
	}
	

	@Override
	public void onDischargeAll()
	{
		for (ABot bot : botManager.getAllBots().values())
		{
			if (bot.getType() == EBotType.TIGER)
			{
				bot.execute(new TigerKickerChargeManual(6, 78, 5));
				bot.execute(new TigerKickerKickV2(Device.STRAIGHT, Mode.FORCE, 25000));
			}
		}
	}
	
	protected class SaveConfig implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String filename = JOptionPane.showInputDialog(null, "Please specify the name of the config file:",
					"Save Bot Configuration", JOptionPane.QUESTION_MESSAGE);
			
			botManager.saveConfig(filename);
			
			updateConfigMenu();
		}
	}
	
	protected class LoadConfig implements ActionListener
	{
		private String	filename;
		
		
		public LoadConfig(String filename)
		{
			this.filename = filename;
		}
		

		@Override
		public void actionPerformed(ActionEvent e)
		{
			botManager.loadConfig(filename);
			
			updateConfigMenu();
		}
	}
	
	protected class DeleteConfig implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			botManager.deleteConfig(ABotManager.getSelectedPersistentConfig());
			
			updateConfigMenu();
		}
	}
	
	private class NetworkStatisticsUpdater extends TimerTask
	{
		private Statistics	lastTxStats	= new Statistics();
		private Statistics	lastRxStats	= new Statistics();
		
		
		@Override
		public void run()
		{
			ABotManager botManager = null;
			synchronized (BotCenterPresenter.this)
			{
				botManager = BotCenterPresenter.this.botManager;
			}
			if (botManager == null)
			{
				return;
			}
			
			Collection<ABot> bots = botManager.getAllBots().values();
			
			Statistics txStats = new Statistics();
			Statistics rxStats = new Statistics();
			
			for (ABot bot : bots)
			{
				if (bot.getType() == EBotType.TIGER)
				{
					TigerBot tiger = (TigerBot) bot;
					txStats = txStats.add(tiger.getTransceiver().getTransmitterStats());
					rxStats = rxStats.add(tiger.getTransceiver().getReceiverStats());
				}
			}
			
			ITransceiverUDP mcastTransceiver = botManager.getMulticastTransceiver();
			
			if (mcastTransceiver != null)
			{
				txStats = txStats.add(mcastTransceiver.getTransmitterStats());
				rxStats = rxStats.add(mcastTransceiver.getReceiverStats());
			}
			
			netSummaryPanel.setTxStat(txStats.substract(lastTxStats));
			netSummaryPanel.setRxStat(rxStats.substract(lastRxStats));
			
			lastTxStats = txStats.clone();
			lastRxStats = rxStats.clone();
		}
	}
	

}
