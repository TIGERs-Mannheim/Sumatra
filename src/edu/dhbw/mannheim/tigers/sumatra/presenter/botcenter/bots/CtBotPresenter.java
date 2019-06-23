/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.CtBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ICtBotObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp.ITransceiverTCP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp.TransceiverTCP.EConnectionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTPIDHistory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTStatus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.ct.CtBotPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.ct.CtBotSummary;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.ct.CtMotorPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.ct.ICtBotPanelObserver;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * Presenter for a CT bot.
 * 
 * @author AndreR
 * 
 */
public class CtBotPresenter extends ABotPresenter implements ICtBotPanelObserver, ICtBotObserver,
		ILookAndFeelStateObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger	log			= Logger.getLogger(getClass());
	private CtBot			bot			= null;
	private CtBotSummary	summary		= null;
	private CtBotPanel	botPanel		= null;
	private ABotManager	botmanager	= null;
	private CtMotorPanel	telemetry	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public CtBotPresenter(ABot bot)
	{
		try
		{
			botmanager = (ABotManager) SumatraModel.getInstance().getModule("botmanager");
		} catch (ModuleNotFoundException err)
		{
			log.error("Botmanager not found", err);
			
			return;
		}
		
		botPanel = new CtBotPanel();
		telemetry = new CtMotorPanel();
		
		node = new BotCenterTreeNode(bot.getName(), ETreeIconType.BOT, botPanel);
		node.add(new BotCenterTreeNode("Telemetry", ETreeIconType.GRAPH, telemetry));
		
		this.bot = (CtBot) bot;
		
		summary = new CtBotSummary();
		
		summary.getBaseSummary().setId(bot.getBotId());
		summary.getBaseSummary().setBotName(bot.getName());
		summary.getBaseSummary().setIP(bot.getIp());
		summary.getBaseSummary().setPort(bot.getPort());
		
		onConnectionChanged(((ITransceiverTCP) bot.getTransceiver()).getConnectionState());
		
		onNameChanged(bot.getName());
		onIdChanged(0, bot.getBotId());
		onIpChanged(bot.getIp());
		onPortChanged(bot.getPort());
		onPidChanged(this.bot.getKp(), this.bot.getKi(), this.bot.getKd(), this.bot.getDelay());
		
		botPanel.addObserver(this);
		this.bot.addObserver(this);
		
		LookAndFeelStateAdapter.getInstance().addObserver(this);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void delete()
	{
		botPanel.removeObserver(this);
		bot.removeObserver(this);
		
		LookAndFeelStateAdapter.getInstance().removeObserver(this);
	}
	

	@Override
	public ABot getBot()
	{
		return bot;
	}
	

	@Override
	public JPanel getSummaryPanel()
	{
		return summary;
	}
	

	@Override
	public void onSaveGeneral()
	{
		try
		{
			if (botPanel.getId() != bot.getBotId())
			{
				botmanager.changeBotId(bot.getBotId(), botPanel.getId());
			}
			
			bot.setName(botPanel.getName());
			bot.setIp(botPanel.getIp());
			bot.setPort(botPanel.getPort());
		} catch (NumberFormatException e)
		{
			log.warn("Invalid value in a general configuration field");
		}
	}
	

	@Override
	public void onSavePid()
	{
		try
		{
			bot.setPid(botPanel.getKp(), botPanel.getKi(), botPanel.getKd(), botPanel.getDelay());
		} catch (NumberFormatException e)
		{
			log.warn("Invalid value in a PID field");
		}
	}
	

	@Override
	public void onConnectionChange()
	{
		switch (bot.getTransceiver().getConnectionState())
		{
			case DISCONNECTED:
				bot.start();
				break;
			case CONNECTED:
				bot.stop();
				break;
			case CONNECTING:
				bot.stop();
				break;
		}
	}
	

	@Override
	public void onCalibrate()
	{
		try
		{
			bot.calibrate(botPanel.getCalibrationTime());
		} catch (NumberFormatException e)
		{
			log.warn("Invalid value for calibration time");
		}
	}
	

	@Override
	public void onNewStatus(CTStatus status)
	{
	}
	

	@Override
	public void onNewPIDHistory(CTPIDHistory history)
	{
		telemetry.setData(history);
	}
	

	@Override
	public void onIncommingCommand(ACommand cmd)
	{
	}
	

	@Override
	public void onOutgoingCommand(ACommand cmd)
	{
	}
	

	@Override
	public void onNameChanged(String name)
	{
		botPanel.setName(name);
		summary.getBaseSummary().setName(name);
		
		if (botCenterPresenter != null)
		{
			node.setTitle(name);
			botCenterPresenter.reloadNode(node);
		}
	}
	

	@Override
	public void onIdChanged(int oldId, int newId)
	{
		botPanel.setId(newId);
		summary.getBaseSummary().setId(newId);
	}
	

	@Override
	public void onIpChanged(String ip)
	{
		botPanel.setIp(bot.getIp());
		summary.getBaseSummary().setIP(ip);
	}
	

	@Override
	public void onPortChanged(int port)
	{
		botPanel.setPort(bot.getPort());
		summary.getBaseSummary().setPort(port);
	}
	

	@Override
	public void onPidChanged(float[] kp, float[] ki, float[] kd, int delay)
	{
		botPanel.setKp(kp);
		botPanel.setKi(ki);
		botPanel.setKd(kd);
		botPanel.setDelay(delay);
	}
	

	@Override
	public void onConnectionChanged(EConnectionState state)
	{
		botPanel.setConnectionState(state);
	}
	

	@Override
	public void onLookAndFeelChanged()
	{
		SwingUtilities.updateComponentTreeUI(summary);
		SwingUtilities.updateComponentTreeUI(botPanel);
		SwingUtilities.updateComponentTreeUI(telemetry);
	}
	

	@Override
	public void onNetworkStateChanged(ENetworkState state)
	{
		summary.getBaseSummary().setNetworkState(state);
	}
	

	@Override
	public JPanel getFastChgPanel()
	{
		return new JPanel();
	}
}
