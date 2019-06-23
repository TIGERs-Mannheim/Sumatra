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
import java.awt.EventQueue;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.EBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStationObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.Bootloader;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.DummyBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationEthStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationWifiStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bootloader.FirmwareUpdatePresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots.TigerBotV3Presenter;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationControlPanel.IBaseStationControlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.basestation.BaseStationV2Panel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.TigerBotV2Summary;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2.BotCenterPanelV2;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2.BotConfigOverviewPanel.IBotConfigOverviewPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * new 2015 bot center presenter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotCenterPresenterV2 implements ISumatraViewPresenter
{
	private static final Logger									log							= Logger
																													.getLogger(BotCenterPresenterV2.class
																															.getName());
	private final BotCenterPanelV2								botCenter					= new BotCenterPanelV2();
	private final BotManagerObserver								botManagerObserver		= new BotManagerObserver();
	private final Map<EBaseStation, BaseStationPresenter>	baseStationPresenters	= new EnumMap<>(EBaseStation.class);
	
	private final FirmwareUpdatePresenter						firmwareUpdatePresenter;
	private final TigerBotV3Presenter							tigerBotPresenter;
	
	private ABotManager												botManager;
	
	
	/**
	 * 
	 */
	public BotCenterPresenterV2()
	{
		botCenter.getBotOverviewPanel().addObserver(new BotConfigOverviewPanelObserver());
		firmwareUpdatePresenter = new FirmwareUpdatePresenter(botCenter.getFirmwareUpdatePanel());
		tigerBotPresenter = new TigerBotV3Presenter(botCenter.getBotOverviewPanel());
		
		Bootloader.addObserver(firmwareUpdatePresenter);
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				try
				{
					botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					for (Map.Entry<EBaseStation, BaseStation> entry : botManager.getBaseStations().entrySet())
					{
						baseStationPresenters.put(entry.getKey(), new BaseStationPresenter(entry.getKey()));
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
					botCenter.showPanel();
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not find botmanager or skillsystem", err);
				}
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				botCenter.hidePanel();
				try
				{
					ABotManager bm = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					bm.removeObserver(botManagerObserver);
					for (BaseStationPresenter bsp : baseStationPresenters.values())
					{
						bsp.delete();
					}
					baseStationPresenters.clear();
					for (ABot bot : bm.getAllBots().values())
					{
						botManagerObserver.onBotRemoved(bot);
					}
					botCenter.getOverviewPanel().setActive(false);
					botCenter.getBotOverviewPanel().getCmbBots().removeAllItems();
					botCenter.getBotOverviewPanel().getCmbBots().addItem(BotID.createBotId());
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
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
	private class BotManagerObserver implements IBotManagerObserver
	{
		
		@Override
		public void onBotAdded(final ABot bot)
		{
			EventQueue.invokeLater(() -> {
				JComboBox<BotID> cmbBots = botCenter.getBotOverviewPanel().getCmbBots();
				cmbBots.addItem(bot.getBotID());
				TigerBotV2Summary summ = new TigerBotV2Summary();
				summ.setId(bot.getBotID());
				summ.setBotName(bot.getName());
				summ.setNetworkState(ENetworkState.ONLINE);
				botCenter.getOverviewPanel().addBotPanel(bot.getBotID(), summ);
			});
		}
		
		
		@Override
		public void onBotRemoved(final ABot bot)
		{
			EventQueue.invokeLater(() -> {
				JComboBox<BotID> cmbBots = botCenter.getBotOverviewPanel().getCmbBots();
				if (cmbBots.getSelectedItem().equals(bot.getBotID()))
				{
					cmbBots.setSelectedItem(BotID.createBotId());
				}
				cmbBots.removeItem(bot.getBotID());
				botCenter.getOverviewPanel().removeBotPanel(bot.getBotID());
			});
		}
		
		
		@Override
		public void onBotIdChanged(final BotID oldId, final BotID newId)
		{
			// obsolete
		}
		
		
		@Override
		public void onBotConnectionChanged(final ABot bot)
		{
			// obsolete
		}
	}
	
	private class BotConfigOverviewPanelObserver implements IBotConfigOverviewPanelObserver
	{
		@Override
		public void onBotIdSelected(final BotID botId)
		{
			ABot bot = botManager.getAllBots().get(botId);
			if ((bot != null) && bot.getClass().equals(TigerBotV3.class))
			{
				TigerBotV3 botV3 = (TigerBotV3) bot;
				tigerBotPresenter.updateSelectedBotId(botV3);
			} else
			{
				tigerBotPresenter.updateSelectedBotId(new DummyBot());
			}
		}
	}
	
	
	private class BaseStationPresenter implements IBaseStationControlPanelObserver, IBaseStationObserver
	{
		private EBaseStation	ebaseStation;
		private Bootloader	bootloader;
		
		
		/**
		 * @param ebaseStation
		 */
		public BaseStationPresenter(final EBaseStation ebaseStation)
		{
			this.ebaseStation = ebaseStation;
			bootloader = new Bootloader(getBaseStation());
			
			final BaseStationV2Panel panel = botCenter.getBaseStationPanels().get(ebaseStation);
			EventQueue.invokeLater(() -> {
				panel.getControlPanel().setConnectionState(getBaseStation().getNetState());
			});
			
			getBaseStation().addObserver(this);
			panel.getControlPanel().addObserver(this);
		}
		
		
		public void delete()
		{
			final BaseStationV2Panel panel = botCenter.getBaseStationPanels().get(ebaseStation);
			
			bootloader.delete();
			getBaseStation().removeObserver(this);
			panel.getControlPanel().removeObserver(this);
			
			EventQueue.invokeLater(() -> {
				panel.getControlPanel().setConnectionState(ENetworkState.OFFLINE);
			});
		}
		
		
		private BaseStation getBaseStation()
		{
			return botManager.getBaseStations().get(ebaseStation);
		}
		
		
		private BaseStationV2Panel getPanel()
		{
			final BaseStationV2Panel panel = botCenter.getBaseStationPanels().get(ebaseStation);
			return panel;
		}
		
		
		@Override
		public void onConnectionChange(final boolean connect)
		{
			if (connect)
			{
				getBaseStation().setActive(true);
				getBaseStation().connect();
			} else
			{
				getBaseStation().setActive(false);
				getBaseStation().disconnect();
			}
		}
		
		
		@Override
		public void onIncommingBotCommand(final BotID id, final ACommand command)
		{
			switch (command.getType())
			{
				case CMD_SYSTEM_MATCH_FEEDBACK:
					final TigerSystemMatchFeedback feedback = (TigerSystemMatchFeedback) command;
					EventQueue.invokeLater(() -> {
						TigerBotV2Summary summ = (TigerBotV2Summary) botCenter.getOverviewPanel().getBotPanel(id);
						if (summ == null)
						{
							return;
						}
						summ.setBatteryLevel(feedback.getBatteryLevel());
						summ.setCap(feedback.getKickerLevel());
					});
					
					for (EFeature feature : EFeature.values())
					{
						boolean working = feedback.isFeatureWorking(feature);
						botCenter.getFeaturePanel().setFeatureState(id, feedback.getHardwareId(), feature, working);
					}
					botCenter.getFeaturePanel().setHWIdSet(id, feedback.getHardwareId() != 255);
					botCenter.getFeaturePanel().update();
					break;
				default:
					break;
			}
			
			ABot bot = tigerBotPresenter.getBot();
			if (bot == null)
			{
				return;
			}
			if (!bot.getBotID().equals(id))
			{
				return;
			}
			tigerBotPresenter.processCommand(command);
		}
		
		
		@Override
		public void onNewBaseStationWifiStats(final BaseStationWifiStats stats)
		{
			EventQueue.invokeLater(() -> {
				getPanel().getWifiStatsPanel().setStats(stats);
			});
		}
		
		
		@Override
		public void onNewBaseStationEthStats(final BaseStationEthStats stats)
		{
			EventQueue.invokeLater(() -> {
				getPanel().getEthStatsPanel().setStats(stats);
			});
		}
		
		
		@Override
		public void onNetworkStateChanged(final ENetworkState netState)
		{
			EventQueue.invokeLater(() -> {
				getPanel().getControlPanel().setConnectionState(netState);
			});
		}
		
		
		@Override
		public void onNewPingDelay(final float delay)
		{
			EventQueue.invokeLater(() -> {
				getPanel().getControlPanel().setPingDelay(delay);
			});
		}
		
		
		@Override
		public void onStartPing(final int numPings, final int payload)
		{
			getBaseStation().startPing(numPings, payload);
		}
		
		
		@Override
		public void onStopPing()
		{
			getBaseStation().stopPing();
		}
	}
}
