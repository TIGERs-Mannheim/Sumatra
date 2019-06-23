/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.BotCenterPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.BotConfigOverviewPanel.IBotConfigOverviewPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.TigerBotV2Summary;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.DummyBot;
import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * new 2015 bot center presenter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotCenterPresenter extends ASumatraViewPresenter implements IBotConfigOverviewPanelObserver
{
	private static final Logger					log							= Logger
																									.getLogger(
																											BotCenterPresenter.class
																													.getName());
	private final BotCenterPanel					botCenter					= new BotCenterPanel();
	private final BotManagerObserver				botManagerObserver		= new BotManagerObserver();
	private final List<BaseStationPresenter>	baseStationPresenters	= new ArrayList<>();
																							
	private final TigerBotPresenter				tigerBotPresenter;
															
	private ABotManager								botManager;
															
															
	/**
	 * 
	 */
	public BotCenterPresenter()
	{
		tigerBotPresenter = new TigerBotPresenter(botCenter.getBotOverviewPanel());
		GlobalShortcuts.register(EShortcut.CHARGE_ALL_BOTS, new Runnable()
		{
			@Override
			public void run()
			{
				if (botManager != null)
				{
					botManager.chargeAll();
				}
			}
		});
		
		GlobalShortcuts.register(EShortcut.DISCHARGE_ALL_BOTS, new Runnable()
		{
			@Override
			public void run()
			{
				if (botManager != null)
				{
					botManager.dischargeAll();
				}
			}
		});
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				try
				{
					botCenter.createPanel();
					botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					for (IBaseStation bs : botManager.getBaseStations())
					{
						BaseStationPresenter bsp = new BaseStationPresenter(bs, botCenter);
						bsp.onModuliStateChanged(state);
						baseStationPresenters.add(bsp);
						botCenter.addBaseStationTab(bsp.getBsPanel());
					}
					for (ABot bot : botManager.getAllBots().values())
					{
						botManagerObserver.onBotAdded(bot);
					}
					botManager.addObserver(botManagerObserver);
					
					GenericSkillSystem skillSystem = (GenericSkillSystem) SumatraModel.getInstance().getModule(
							ASkillSystem.MODULE_ID);
					tigerBotPresenter.setSkillsystem(skillSystem);
					tigerBotPresenter.setBotManager(botManager);
					botCenter.getOverviewPanel().setActive(true);
					botCenter.getBotOverviewPanel().addObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not find botmanager or skillsystem", err);
				}
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				botCenter.getBotOverviewPanel().removeObserver(this);
				botCenter.clearPanel();
				try
				{
					ABotManager bm = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					bm.removeObserver(botManagerObserver);
					for (BaseStationPresenter bsp : baseStationPresenters)
					{
						bsp.onModuliStateChanged(state);
					}
					baseStationPresenters.clear();
					for (ABot bot : bm.getAllBots().values())
					{
						botManagerObserver.onBotRemoved(bot);
					}
					botCenter.getOverviewPanel().setActive(false);
					botCenter.getBotOverviewPanel().getCmbBots().removeAllItems();
					botCenter.getBotOverviewPanel().getCmbBots().addItem(BotID.get());
					botCenter.getFeaturePanel().clearFeatureStates();
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not find botmanager", err);
				}
				break;
			default:
				break;
				
		}
	}
	
	
	@Override
	public Component getComponent()
	{
		return botCenter;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return botCenter;
	}
	
	
	private class BotManagerObserver implements IBotManagerObserver
	{
		
		@Override
		public void onBotAdded(final ABot bot)
		{
			JComboBox<BotID> cmbBots = botCenter.getBotOverviewPanel().getCmbBots();
			cmbBots.addItem(bot.getBotId());
			TigerBotV2Summary summ = new TigerBotV2Summary();
			summ.setId(bot.getBotId());
			summ.setBotName(bot.getName());
			summ.setNetworkState(ENetworkState.ONLINE);
			botCenter.getOverviewPanel().addBotPanel(bot.getBotId(), summ);
		}
		
		
		@Override
		public void onBotRemoved(final ABot bot)
		{
			JComboBox<BotID> cmbBots = botCenter.getBotOverviewPanel().getCmbBots();
			if (bot.getBotId().equals(cmbBots.getSelectedItem()))
			{
				cmbBots.setSelectedItem(BotID.get());
			}
			cmbBots.removeItem(bot.getBotId());
			botCenter.getOverviewPanel().removeBotPanel(bot.getBotId());
		}
	}
	
	
	@Override
	public void onBotIdSelected(final BotID botId)
	{
		ABot bot = botManager.getAllBots().get(botId);
		if (bot == null)
		{
			tigerBotPresenter.updateSelectedBotId(new DummyBot(botId));
		} else
		{
			tigerBotPresenter.updateSelectedBotId(bot);
		}
	}
	
}
