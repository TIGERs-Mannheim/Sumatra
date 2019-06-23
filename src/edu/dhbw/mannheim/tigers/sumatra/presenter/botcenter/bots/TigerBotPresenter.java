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

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ITigerBotObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeManual;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerIrLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetManual;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams.MotorMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetPidSp;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMovementLis3LogRaw;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemBigPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickBallV1;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickBallV1.EKickDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickBallV1.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveFixedGlobalOrientation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveFixedTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.TigerStraightMove;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots.tiger.TrainingPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.util.ThreadUtil;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.AccelerationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.INetworkPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.IRPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.ISkillsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.ITigerBotMainPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.NetworkPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.PowerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.SkillsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.TigerBotMainPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.TigerBotSummary;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.TigerBotSummary.ITigerBotSummaryObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerPanel.IKickerPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.IMotorConfigurationPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.IMotorEnhancedInputPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.IMotorMainPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorMainPanel;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * Presenter for a tiger bot.
 * 
 * @author AndreR
 * 
 */
public class TigerBotPresenter extends ABotPresenter implements ITigerBotMainPanelObserver, ILookAndFeelStateObserver,
		ISkillsPanelObserver, IKickerPanelObserver, IMotorMainPanelObserver, IMotorEnhancedInputPanel,
		INetworkPanelObserver, IWorldPredictorObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger							log							= Logger.getLogger(getClass());
	
	private TigerBot								bot							= null;
	
	private ABotManager							botmanager					= null;
	private AWorldPredictor						worldPredictor				= null;
	private ASkillSystem							skillsystem					= null;
	private TigerBotSummary						summary						= null;
	private KickerConfig							fastChgPanel				= null;
	private TigerBotMainPanel					mainPanel					= null;
	private SkillsPanel							skills						= null;
	private KickerPanel							kicker						= null;
	private NetworkPanel							network						= null;
	private MotorMainPanel						motorMain					= null;
	private PowerPanel							power							= null;
	private AccelerationPanel					acceleration				= null;
	private IRPanel								ir								= null;
	private TrainingPresenter					trainingPresenter			= null;
	private MotorControlPanel					motorControls[]			= new MotorControlPanel[5];
	private MotorConfigPresenter				motorPresenters[]			= new MotorConfigPresenter[5];
	private PingThread							pingThread					= null;
	
	// Observer Handler
	private final TigerBotObserver			tigerBotObserver			= new TigerBotObserver();
	private final TigerBotSummaryObserver	tigerBotSummaryObserver	= new TigerBotSummaryObserver();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerBotPresenter(ABot bot)
	{
		try
		{
			botmanager = (ABotManager) SumatraModel.getInstance().getModule("botmanager");
			worldPredictor = (AWorldPredictor) SumatraModel.getInstance().getModule("worldpredictor");
		} catch (ModuleNotFoundException err)
		{
			log.error("Botmanager not found", err);
			
			return;
		}
		
		try
		{
			skillsystem = (ASkillSystem) SumatraModel.getInstance().getModule("skillsystem");
		} catch (ModuleNotFoundException err)
		{
			log.error("Skillsystem not found", err);
			
			return;
		}
		
		summary = new TigerBotSummary();
		fastChgPanel = new KickerConfig(bot.getBotId());
		mainPanel = new TigerBotMainPanel();
		skills = new SkillsPanel();
		kicker = new KickerPanel();
		network = new NetworkPanel();
		motorMain = new MotorMainPanel();
		power = new PowerPanel();
		acceleration = new AccelerationPanel();
		ir = new IRPanel();
		
		for (int i = 0; i < 5; i++)
		{
			motorControls[i] = new MotorControlPanel();
			motorPresenters[i] = new MotorConfigPresenter(i);
		}
		
		this.bot = (TigerBot) bot;
		
		trainingPresenter = new TrainingPresenter(this.bot);
		
		node = new BotCenterTreeNode(bot.getName(), ETreeIconType.BOT, mainPanel);
		
		node.add(new BotCenterTreeNode("Power", ETreeIconType.LIGHTNING, power));
		node.add(new BotCenterTreeNode("Network", ETreeIconType.AP, network));
		BotCenterTreeNode motorsNode = new BotCenterTreeNode("Motors", ETreeIconType.MOTOR, motorMain);
		motorsNode.add(new BotCenterTreeNode("Front Right", ETreeIconType.GRAPH, motorControls[0]));
		motorsNode.add(new BotCenterTreeNode("Front Left", ETreeIconType.GRAPH, motorControls[1]));
		motorsNode.add(new BotCenterTreeNode("Rear Left ", ETreeIconType.GRAPH, motorControls[2]));
		motorsNode.add(new BotCenterTreeNode("Rear Right", ETreeIconType.GRAPH, motorControls[3]));
		motorsNode.add(new BotCenterTreeNode("Dribbler", ETreeIconType.GRAPH, motorControls[4]));
		node.add(motorsNode);
		node.add(new BotCenterTreeNode("Kicker", ETreeIconType.KICK, kicker));
		node.add(new BotCenterTreeNode("Skills", ETreeIconType.LAMP, skills));
		node.add(new BotCenterTreeNode("Acceleration", ETreeIconType.GRAPH, acceleration));
		node.add(new BotCenterTreeNode("IR", ETreeIconType.GRAPH, ir));
		node.add(trainingPresenter.getNode());
		
		summary.setId(bot.getBotId());
		summary.setBotName(bot.getName());
		
		tigerBotObserver.onNameChanged(bot.getName());
		tigerBotObserver.onIdChanged(0, bot.getBotId());
		tigerBotObserver.onIpChanged(bot.getIp());
		tigerBotObserver.onPortChanged(bot.getPort());
		tigerBotObserver.onNetworkStateChanged(this.bot.getNetworkState());
		tigerBotObserver.onServerPortChanged(this.bot.getServerPort());
		tigerBotObserver.onMacChanged(this.bot.getMac());
		tigerBotObserver.onCpuIdChanged(this.bot.getCpuId());
		tigerBotObserver.onUseUpdateAllChanged(this.bot.getUseUpdateAll());
		tigerBotObserver.onLogsChanged(this.bot.getLogs());
		tigerBotObserver.onMotorParamsChanged(this.bot.getMotorParams());
		
		tigerBotSummaryObserver.onConnectionTypeChange(this.bot.getUseUpdateAll());
		tigerBotSummaryObserver.onOOFCheckChange(this.bot.getOofCheck());
		
		this.bot.addObserver(tigerBotObserver);
		this.summary.addObserver(tigerBotSummaryObserver);
		this.mainPanel.addObserver(this);
		this.skills.addObserver(this);
		this.kicker.addObserver(this);
		this.network.addObserver(this);
		this.worldPredictor.addObserver(this);
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		motorMain.addObserver(this);
		motorMain.getEnhancedInputPanel().addObserver(this);
		for (int i = 0; i < 5; i++)
		{
			motorControls[i].getConfigPanel().addObserver(motorPresenters[i]);
		}
		
		GeneralPurposeTimer.getInstance().scheduleAtFixedRate(new NetworkStatisticsUpdater(), 0, 1000);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void delete()
	{
		bot.removeObserver(tigerBotObserver);
		summary.removeObserver(tigerBotSummaryObserver);
		mainPanel.removeObserver(this);
		skills.removeObserver(this);
		kicker.removeObserver(this);
		network.removeObserver(this);
		worldPredictor.removeObserver(this);
		LookAndFeelStateAdapter.getInstance().removeObserver(this);
		motorMain.removeObserver(this);
		motorMain.getEnhancedInputPanel().removeObserver(this);
		
		trainingPresenter.delete();
		
		for (int i = 0; i < 5; i++)
		{
			motorControls[i].getConfigPanel().removeObserver(motorPresenters[i]);
		}
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
	public JPanel getFastChgPanel()
	{
		return fastChgPanel;
	}
	

	@Override
	public void onLookAndFeelChanged()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				SwingUtilities.updateComponentTreeUI(summary);
				SwingUtilities.updateComponentTreeUI(mainPanel);
				SwingUtilities.updateComponentTreeUI(skills);
				SwingUtilities.updateComponentTreeUI(kicker);
				SwingUtilities.updateComponentTreeUI(network);
				SwingUtilities.updateComponentTreeUI(power);
				SwingUtilities.updateComponentTreeUI(motorMain);
			}
		});
	}
	

	@Override
	public void onConnectionChange()
	{
		connectionChange();
	}
	

	private void connectionChange()
	{
		switch (bot.getNetworkState())
		{
			case OFFLINE:
				bot.setActive(true);
				bot.start();
				break;
			case CONNECTING:
				bot.stop();
				bot.setActive(false);
				break;
			case ONLINE:
				bot.stop();
				bot.setActive(false);
				break;
		}
	}
	

	@Override
	public void onSaveGeneral()
	{
		try
		{
			if (mainPanel.getId() != bot.getBotId())
			{
				botmanager.changeBotId(bot.getBotId(), mainPanel.getId());
			}
			
			bot.setName(mainPanel.getBotName());
			bot.setIp(mainPanel.getIp());
			bot.setPort(mainPanel.getPort());
			bot.setCpuId(mainPanel.getCpuId());
			bot.setMac(mainPanel.getMac());
			bot.setServerPort(mainPanel.getServerPort());
			bot.setUseUpdateAll(mainPanel.getUseUpdateAll());
			
			bot.stop();
			bot.start();
		}

		catch (NumberFormatException e)
		{
			log.warn("Invalid value in a general configuration field");
		}
	}
	

	@Override
	public void onMoveToXY(float x, float y)
	{
		skillsystem.execute(bot.getBotId(), new MoveV2(new Vector2(x, y), 0));
		// skillsystem.execute(bot.getBotId(), new MoveFixedCurrentOrientation(new Vector2(x, y)));
	}
	

	@Override
	public void onRotateAndMoveToXY(float x, float y, float angle)
	{
		skillsystem.execute(bot.getBotId(), new MoveFixedGlobalOrientation(new Vector2(x, y), angle));
	}
	

	@Override
	public void onStraightMove(int time)
	{
		skillsystem.execute(bot.getBotId(), new TigerStraightMove(time));
	}
	

	@Override
	public void onRotate(float targetAngle)
	{
		skillsystem.execute(bot.getBotId(), new MoveFixedGlobalOrientation(null, targetAngle));
	}
	

	@Override
	public void onKick(float kicklength, EKickMode mode, EKickDevice device)
	{
		if (kicklength > 0)
			skillsystem.execute(bot.getBotId(), new KickBallV1(kicklength, mode, device));
		else
			skillsystem.execute(bot.getBotId(), new KickBallV1(mode, device));
	}
	

	@Override
	public void onLookAt(Vector2 lookAtTarget)
	{
		skillsystem.execute(bot.getBotId(), new MoveFixedTarget(null, lookAtTarget));
	}
	

	@Override
	public void onDribble(int rpm)
	{
		// skillsystem.execute(bot.getBotId(), new DribbleBall(rpm));
		bot.execute(new TigerDribble(rpm));
	}
	

	@Override
	public void onKickerFire(int level, float duration, int mode, int device)
	{
		TigerKickerKickV2 kick = new TigerKickerKickV2(device, mode, duration, level);
		
		bot.execute(kick);
	}
	

	@Override
	public void onKickerChargeManual(int duration, int on, int off)
	{
		if (on < 0 || off < 0 || on > 60000 || off > 60000 || duration < 0 || duration > 60000)
		{
			return;
		}
		
		bot.execute(new TigerKickerChargeManual(on, off, duration));
	}
	

	@Override
	public void onSetAutomaticSpeed(float x, float y, float w, float v)
	{
		bot.execute(new TigerMotorMoveV2(new Vector2(x, y), w, v));
	}
	

	@Override
	public void onNewVelocity(Vector2 xy)
	{
		bot.execute(new TigerMotorMoveV2(xy));
	}
	

	@Override
	public void onNewAngularVelocity(float w)
	{
		TigerMotorMoveV2 move = new TigerMotorMoveV2(w);
		move.setV(0);
		bot.execute(move);
	}
	

	@Override
	public void onKickerChargeAuto(int max)
	{
		bot.execute(new TigerKickerChargeAuto(max));
	}
	

	@Override
	public void onStartPing(int numPings)
	{
		onStopPing();
		
		pingThread = new PingThread(numPings);
		pingThread.start();
	}
	

	@Override
	public void onStopPing()
	{
		if (pingThread == null)
		{
			return;
		}
		
		pingThread.interrupt();
		pingThread = null;
	}
	

	@Override
	public void onSaveLogs()
	{
		boolean move = mainPanel.getLogMovement();
		boolean kicker = mainPanel.getLogKicker();
		boolean ir = mainPanel.getLogIr();
		boolean accel = mainPanel.getLogAccel();
		
		bot.setLogMovement(move);
		bot.setLogKicker(kicker);
		bot.setLogIr(ir);
		bot.setLogAccel(accel);
	}
	

	@Override
	public void onNewWorldFrame(WorldFrame wf)
	{
		TrackedTigerBot tracked = wf.tigerBots.get(bot.getBotId());
		if (tracked == null)
			return;
		
		Vector2f vel = new Vector2f(tracked.vel.turnNew(-tracked.angle));
		
		motorMain.getEnhancedInputPanel().setLatestWPData(new Vector2f(-vel.y, vel.x), tracked.aVel);
	}
	

	@Override
	public void onSetMotorMode(MotorMode mode)
	{
		bot.setMode(mode);
	}
	
	// -------------------------------------------------------------
	// Sub classes
	// -------------------------------------------------------------
	private class TigerBotObserver implements ITigerBotObserver
	{
		@Override
		public void onNameChanged(String name)
		{
			summary.setBotName(name);
			mainPanel.setBotName(name);
		}
		

		@Override
		public void onIdChanged(int oldId, int newId)
		{
			summary.setId(newId);
			mainPanel.setId(newId);
		}
		

		@Override
		public void onIpChanged(String ip)
		{
			mainPanel.setIp(ip);
		}
		

		@Override
		public void onPortChanged(int port)
		{
			mainPanel.setPort(port);
		}
		

		@Override
		public void onUseUpdateAllChanged(boolean useUpdateAll)
		{
			mainPanel.setUseUpdateAll(useUpdateAll);
			summary.setMulticast(useUpdateAll);
		}
		

		@Override
		public void onOofCheckChanged(boolean enable)
		{
			summary.setOofCheck(enable);
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
		public void onNetworkStateChanged(ENetworkState state)
		{
			summary.setNetworkState(state);
			mainPanel.setConnectionState(state);
		}
		

		@Override
		public void onNewKickerStatusV2(TigerKickerStatusV2 status)
		{
			kicker.getStatusPanel().setChg(status.getChargeCurrent());
			kicker.getStatusPanel().setCap(status.getCapLevel());
			kicker.getStatusPanel().setTDiode(status.getTDiode());
			kicker.getStatusPanel().setTIGBT(status.getTIGBT());
			
			kicker.getPlotPanel().addCapLevel(status.getCapLevel());
			kicker.getPlotPanel().addChargeCurrent(status.getChargeCurrent());
			
			fastChgPanel.setChargeLvL(status.getCapLevel());
		}
		

		@Override
		public void onNewSystemStatusMovement(TigerSystemStatusMovement status)
		{
			motorMain.getEnhancedInputPanel().setLatestVelocity(status.getVelocity());
			motorMain.getEnhancedInputPanel().setLatestAngularVelocity(status.getAngularVelocity());
		}
		

		@Override
		public void onNewSystemPowerLog(TigerSystemPowerLog log)
		{
			power.addPowerLog(log);
			summary.setBatteryLevel(log.getBatLevel());
		}
		

		@Override
		public void onNewKickerIrLog(TigerKickerIrLog log)
		{
			ir.addIrLog(log);
		}
		

		@Override
		public void onNewMovementLis3LogRaw(TigerMovementLis3LogRaw log)
		{
			acceleration.addMovementLog(log);
		}
		

		@Override
		public void onServerPortChanged(int port)
		{
			mainPanel.setServerPort(port);
		}
		

		@Override
		public void onCpuIdChanged(String cpuId)
		{
			mainPanel.setCpuId(cpuId);
		}
		

		@Override
		public void onMacChanged(String mac)
		{
			mainPanel.setMac(mac);
		}
		

		@Override
		public void onLogsChanged(TigerSystemSetLogs logs)
		{
			mainPanel.setLogMovement(logs.getMovement());
			mainPanel.setLogKicker(logs.getKicker());
			mainPanel.setLogIr(logs.getIr());
			mainPanel.setLogAccel(logs.getAccel());
		}
		

		@Override
		public void onMotorParamsChanged(TigerMotorSetParams params)
		{
			motorMain.setMode(params.getMode());
			
			for (int i = 0; i < 5; i++)
			{
				motorControls[i].getConfigPanel().setLogging(bot.getPidLogging(i));
				motorControls[i].getConfigPanel().setPidParams(bot.getKp(i), bot.getKi(i), bot.getKd(i), bot.getSlewMax(i));
			}
		}
		

		@Override
		public void onNewSystemPong(TigerSystemPong pong)
		{
			if (pingThread == null)
			{
				return;
			}
			
			pingThread.pongArrived(pong.getId());
		}
		

		@Override
		public void onNewMotorPidLog(TigerMotorPidLog log)
		{
			motorControls[log.getId()].getPidPanel().setLog(log);
			
			motorControls[log.getId()].getConfigPanel().setLatest(log.getLatest());
			motorControls[log.getId()].getConfigPanel().setOverload(log.getOverload());
			motorControls[log.getId()].getConfigPanel().setECurrent(log.getECurrent());
		}
	}
	
	private class TigerBotSummaryObserver implements ITigerBotSummaryObserver
	{
		@Override
		public void onConnectionTypeChange(boolean multicast)
		{
			bot.setUseUpdateAll(multicast);
		}
		

		@Override
		public void onOOFCheckChange(boolean oofCheck)
		{
			bot.setOofCheck(oofCheck);
		}
		

		@Override
		public void onConnectionChange()
		{
			connectionChange();
		}
	}
	
	private class NetworkStatisticsUpdater extends TimerTask
	{
		private Statistics	lastTxStats	= new Statistics();
		private Statistics	lastRxStats	= new Statistics();
		
		
		@Override
		public void run()
		{
			Statistics stat = bot.getTransceiver().getTransmitterStats();
			network.setTxStat(stat.substract(lastTxStats));
			lastTxStats = stat.clone();
			
			stat = bot.getTransceiver().getReceiverStats();
			network.setRxStat(stat.substract(lastRxStats));
			lastRxStats = stat.clone();
			
			network.setTxAllStat(lastTxStats);
			network.setRxAllStat(lastRxStats);
		}
	}
	
	private class MotorConfigPresenter implements IMotorConfigurationPanelObserver
	{
		private int	id;
		
		
		public MotorConfigPresenter(int motorId)
		{
			id = motorId;
		}
		

		@Override
		public void onSetLog(boolean logging)
		{
			bot.setPidLogging(id, logging);
		}
		

		@Override
		public void onSetPidParams(float kp, float ki, float kd, int slew)
		{
			bot.setPid(id, kp, ki, kd, slew);
		}
		

		@Override
		public void onSetManual(int power)
		{
			bot.execute(new TigerMotorSetManual(id, power));
		}
		

		@Override
		public void onSetPidSetpoint(int setpoint)
		{
			bot.execute(new TigerMotorSetPidSp(id, setpoint));
		}
	}
	
	private class PingThread extends Thread
	{
		private long					delay			= 100000000;
		
		private int						id				= 0;
		
		private Map<Integer, Long>	activePings	= new HashMap<Integer, Long>();
		
		
		public PingThread(int numPings)
		{
			delay = 1000000000 / numPings;
		}
		

		@Override
		public void run()
		{
			while (!Thread.currentThread().isInterrupted())
			{
				synchronized (activePings)
				{
					activePings.put(id, System.nanoTime());
				}
				
				bot.execute(new TigerSystemBigPing(id));
				id++;
				
				ThreadUtil.parkNanosSafe(delay);
			}
			
			activePings.clear();
		}
		

		public void pongArrived(int id)
		{
			Long startTime = null;
			
			synchronized (activePings)
			{
				startTime = activePings.remove(id);
			}
			
			if (startTime == null)
			{
				return;
			}
			
			float delay = (System.nanoTime() - startTime) / 1000000.0f;
			
			network.setDelay(delay);
		}
	}
}
