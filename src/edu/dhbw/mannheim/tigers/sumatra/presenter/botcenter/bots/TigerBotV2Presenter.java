/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.03.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.ControllerParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParametersXYW;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorFusionParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.StateUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ITigerBotV2Observer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.ControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerKickerStatusV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.EKickDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipAutoSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickAutoSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.RotateTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.StraightMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.TurnAroundBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.util.ThreadUtil;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.FeaturePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.IFeatureChangedObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.INetworkPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.ISkillsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.ITigerBotMainPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.PowerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.SkillsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.IMotorEnhancedInputPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorInputPanel.IMotorInputPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.ConsolePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.ConsolePanel.IConsolePanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.ControllerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.FusionCtrlPanel.IFusionCtrlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.SelectControllerPanel.ISelectControllerPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.SystemStatusPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.TigerBotV2MainPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.TigerBotV2Summary;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.TigerBotV2Summary.ITigerBotV2SummaryObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.dribbler.DribblerConfigurationPanel.IDribblerConfigurationPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.dribbler.DribblerControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.kicker.KickerPanelV2;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.kicker.KickerPanelV2.IKickerPanelV2Observer;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * Presenter for a tiger bot V2.
 * 
 * @author AndreR
 * 
 */
public class TigerBotV2Presenter extends ABotPresenter implements ITigerBotMainPanelObserver,
		ILookAndFeelStateObserver, ISkillsPanelObserver, IKickerPanelV2Observer, IMotorInputPanelObserver,
		IMotorEnhancedInputPanel, INetworkPanelObserver, IWorldPredictorObserver, IFusionCtrlPanelObserver,
		IFeatureChangedObserver, IConsolePanelObserver, ISelectControllerPanelObserver,
		IDribblerConfigurationPanelObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger					log							= Logger.getLogger(TigerBotV2Presenter.class
																									.getName());
	
	private TigerBotV2								bot							= null;
	
	private ABotManager								botmanager					= null;
	private AWorldPredictor							worldPredictor				= null;
	private ASkillSystem								skillsystem					= null;
	private TigerBotV2Summary						summary						= null;
	private KickerConfig								fastChgPanel				= null;
	private TigerBotV2MainPanel					mainPanel					= null;
	private SkillsPanel								skills						= null;
	private KickerPanelV2							kicker						= null;
	private PowerPanel								power							= null;
	private FeaturePanel								features						= null;
	private SystemStatusPanel						systemStatus				= null;
	private DribblerControlPanel					dribblerControl			= null;
	private PingThread								pingThread					= null;
	private ControllerPanel							controllerPanel			= null;
	private ConsolePanel								consolePanel				= null;
	
	// Observer Handler
	private final TigerBotV2Observer				tigerBotObserver			= new TigerBotV2Observer();
	private final TigerBotV2SummaryObserver	tigerBotSummaryObserver	= new TigerBotV2SummaryObserver();
	
	
	private long										startNewWP					= System.nanoTime();
	private long										startMotor					= System.nanoTime();
	// in milliseconds
	private static final long						VISUALIZATION_FREQUENCY	= 200;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param bot
	 */
	public TigerBotV2Presenter(ABot bot)
	{
		try
		{
			botmanager = (ABotManager) SumatraModel.getInstance().getModule("botmanager");
			worldPredictor = (AWorldPredictor) SumatraModel.getInstance().getModule("worldpredictor");
		} catch (final ModuleNotFoundException err)
		{
			log.error("Botmanager not found", err);
			
			return;
		}
		
		try
		{
			skillsystem = (ASkillSystem) SumatraModel.getInstance().getModule("skillsystem");
		} catch (final ModuleNotFoundException err)
		{
			log.error("Skillsystem not found", err);
			
			return;
		}
		
		summary = new TigerBotV2Summary();
		fastChgPanel = new KickerConfig(bot.getBotID());
		mainPanel = new TigerBotV2MainPanel();
		skills = new SkillsPanel();
		kicker = new KickerPanelV2();
		power = new PowerPanel();
		systemStatus = new SystemStatusPanel();
		controllerPanel = new ControllerPanel();
		features = new FeaturePanel();
		consolePanel = new ConsolePanel();
		dribblerControl = new DribblerControlPanel();
		
		this.bot = (TigerBotV2) bot;
		
		node = new BotCenterTreeNode(bot.getName(), ETreeIconType.BOT, mainPanel);
		
		node.add(new BotCenterTreeNode("Power", ETreeIconType.LIGHTNING, power));
		node.add(new BotCenterTreeNode("Status", ETreeIconType.AP, systemStatus));
		node.add(new BotCenterTreeNode("Kicker", ETreeIconType.KICK, kicker));
		node.add(new BotCenterTreeNode("Controller", ETreeIconType.GRAPH, controllerPanel));
		node.add(new BotCenterTreeNode("Dribbler", ETreeIconType.MOTOR, dribblerControl));
		node.add(new BotCenterTreeNode("Skills", ETreeIconType.LAMP, skills));
		node.add(new BotCenterTreeNode("Features", ETreeIconType.LIGHTNING, features));
		node.add(new BotCenterTreeNode("Console", ETreeIconType.CONSOLE, consolePanel));
		
		summary.setId(bot.getBotID());
		summary.setBotName(bot.getName());
		
		tigerBotObserver.onNameChanged(bot.getName());
		tigerBotObserver.onIdChanged(new BotID(), bot.getBotID());
		tigerBotObserver.onNetworkStateChanged(this.bot.getNetworkState());
		tigerBotObserver.onLogsChanged(this.bot.getLogs());
		tigerBotObserver.onSensorFusionParamsChanged(this.bot.getSensorFusionParams());
		tigerBotObserver.onControllerParamsChanged(this.bot.getControllerParams());
		tigerBotObserver.onControllerTypeChanged(this.bot.getControllerType());
		tigerBotObserver.onBotFeaturesChanged(this.bot.getBotFeatures());
		
		tigerBotSummaryObserver.onOOFCheckChange(this.bot.getOofCheck());
		
		this.bot.addObserver(tigerBotObserver);
		summary.addObserver(tigerBotSummaryObserver);
		mainPanel.addObserver(this);
		skills.addObserver(this);
		kicker.addObserver(this);
		worldPredictor.addObserver(this);
		controllerPanel.getFusionPanel().addObserver(this);
		controllerPanel.getSelectControllerPanel().addObserver(this);
		consolePanel.addObserver(this);
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		controllerPanel.getInputPanel().addObserver(this);
		controllerPanel.getEnhancedInputPanel().addObserver(this);
		features.addObserver(this);
		dribblerControl.getConfigPanel().addObserver(this);
		
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
		worldPredictor.removeObserver(this);
		controllerPanel.getFusionPanel().removeObserver(this);
		controllerPanel.getSelectControllerPanel().removeObserver(this);
		consolePanel.removeObserver(this);
		LookAndFeelStateAdapter.getInstance().removeObserver(this);
		controllerPanel.getInputPanel().removeObserver(this);
		controllerPanel.getEnhancedInputPanel().removeObserver(this);
		dribblerControl.getConfigPanel().removeObserver(this);
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
			@Override
			public void run()
			{
				SwingUtilities.updateComponentTreeUI(summary);
				SwingUtilities.updateComponentTreeUI(mainPanel);
				SwingUtilities.updateComponentTreeUI(skills);
				SwingUtilities.updateComponentTreeUI(kicker);
				SwingUtilities.updateComponentTreeUI(power);
				SwingUtilities.updateComponentTreeUI(dribblerControl);
				SwingUtilities.updateComponentTreeUI(controllerPanel);
			}
		});
	}
	
	
	@Override
	public void onConnectionChange()
	{
		connectionChange();
		botmanager.botConnectionChanged(bot);
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
			if (mainPanel.getId() != bot.getBotID().getNumber())
			{
				botmanager.changeBotId(bot.getBotID(), new BotID(mainPanel.getId()));
			}
			
			bot.setName(mainPanel.getBotName());
			
			bot.stop();
			bot.start();
		}
		
		catch (final NumberFormatException e)
		{
			log.warn("Invalid value in a general configuration field");
		}
	}
	
	
	@Override
	public void onMoveToXY(float x, float y)
	{
		final MovementCon moveCon = new MovementCon();
		moveCon.updateDestination(new Vector2(x, y));
		skillsystem.execute(bot.getBotID(), new MoveToSkill(moveCon));
	}
	
	
	@Override
	public void onRotateAndMoveToXY(float x, float y, float angle)
	{
		final MovementCon moveCon = new MovementCon();
		moveCon.updateDestination(new Vector2(x, y));
		skillsystem.execute(bot.getBotID(), new MoveToSkill(moveCon));
	}
	
	
	@Override
	public void onStraightMove(int distance, float angle)
	{
		skillsystem.execute(bot.getBotID(), new StraightMoveSkill(distance, angle));
	}
	
	
	@Override
	public void onRotate(float targetAngle)
	{
		skillsystem.execute(bot.getBotID(), new RotateTestSkill(targetAngle));
	}
	
	
	@Override
	public void onKick(float kicklength, EKickDevice device)
	{
		switch (device)
		{
			case CHIP:
				skillsystem.execute(bot.getBotID(), new ChipAutoSkill(kicklength, 0));
				break;
			case STRAIGHT:
				skillsystem.execute(bot.getBotID(), new KickAutoSkill(kicklength));
				break;
		}
	}
	
	
	@Override
	public void onLookAt(Vector2 lookAtTarget)
	{
		skillsystem.execute(bot.getBotID(), new TurnAroundBallSkill(lookAtTarget));
	}
	
	
	@Override
	public void onDribble(int rpm)
	{
		bot.execute(new TigerDribble(rpm));
	}
	
	
	@Override
	public void onSkill(AMoveSkill skill)
	{
		skillsystem.execute(bot.getBotID(), skill);
	}
	
	
	@Override
	public void onKickerFire(float duration, int mode, int device)
	{
		final TigerKickerKickV2 kick = new TigerKickerKickV2(device, mode, duration);
		
		bot.execute(kick);
	}
	
	
	@Override
	public void onSetSpeed(float x, float y, float w, float v)
	{
	}
	
	
	@Override
	public void onSetSpeed(float x, float y, float w)
	{
		bot.execute(new TigerMotorMoveV2(new Vector2(x, y), w));
	}
	
	
	@Override
	public void onNewVelocity(Vector2 xy)
	{
		bot.execute(new TigerMotorMoveV2(xy));
	}
	
	
	@Override
	public void onNewAngularVelocity(float w)
	{
		final TigerMotorMoveV2 move = new TigerMotorMoveV2(w);
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
		final boolean moveLog = mainPanel.getLogMovement();
		final boolean kickerLog = mainPanel.getLogKicker();
		
		bot.setLogMovement(moveLog);
		bot.setLogKicker(kickerLog);
	}
	
	
	@Override
	public void onNewWorldFrame(WorldFrame wf)
	{
		if ((System.nanoTime() - startNewWP) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
		{
			final TrackedTigerBot tracked = wf.tigerBotsVisible.getWithNull(bot.getBotID());
			if (tracked == null)
			{
				return;
			}
			
			final Vector2f vel = new Vector2f(tracked.getVel().turnNew(-tracked.getAngle()));
			
			controllerPanel.getEnhancedInputPanel().setLatestWPData(new Vector2f(-vel.y(), vel.x()), tracked.getaVel());
			startNewWP = System.nanoTime();
		}
	}
	
	
	@Override
	public void onVisionSignalLost(WorldFrame emptyWf)
	{
		controllerPanel.getEnhancedInputPanel().setLatestWPData(new Vector2f(0.0f, 0.0f), 0.0f);
	}
	
	
	@Override
	public void onNewStateUncertainties(StateUncertainties unc)
	{
		bot.setStateUncertainties(unc);
	}
	
	
	@Override
	public void onNewSensorUncertainties(SensorUncertainties unc)
	{
		bot.setSensorUncertainties(unc);
	}
	
	
	@Override
	public void onNewControllerParams(PIDParametersXYW pos, PIDParametersXYW vel, PIDParametersXYW acc)
	{
		bot.setPIDParamsPos(pos);
		bot.setPIDParamsVel(vel);
		bot.setPIDParamsAcc(acc);
	}
	
	
	@Override
	public void onFeatureChanged(EFeature feature, EFeatureState state)
	{
		bot.getBotFeatures().put(feature, state);
	}
	
	
	@Override
	public void onConsoleCommand(String cmd, ConsoleCommandTarget target)
	{
		TigerSystemConsoleCommand conCmd = new TigerSystemConsoleCommand();
		conCmd.setText(cmd);
		conCmd.setTarget(target);
		bot.execute(conCmd);
	}
	
	
	@Override
	public void onNewControllerSelected(ControllerType type)
	{
		bot.setControllerType(type);
	}
	
	
	@Override
	public void onSetDribblerLog(boolean logging)
	{
		bot.setDribblerLogging(logging);
	}
	
	
	@Override
	public void onSetDribblerPidParams(float kp, float ki, float kd)
	{
		bot.setDribblerPid(new PIDParameters(kp, ki, kd));
	}
	
	
	@Override
	public void onSetDribblerRPM(int rpm)
	{
		bot.execute(new TigerDribble(rpm));
		
	}
	
	
	@Override
	public void onUpdateFirmware(String filepath, boolean targetMain)
	{
		bot.getBootloader().start(filepath, targetMain);
	}
	
	
	@Override
	public void onCopyCtrlValuesToAll()
	{
		for (ABot abot : botmanager.getAllBots().values())
		{
			if (abot.getType() != EBotType.TIGER_V2)
			{
				continue;
			}
			
			TigerBotV2 v2 = (TigerBotV2) abot;
			
			v2.setControllerAndFusionParams(bot.getControllerParams(), bot.getSensorFusionParams());
		}
		
		log.info("Copied controller values to all bots");
	}
	
	// -------------------------------------------------------------
	// Sub classes
	// -------------------------------------------------------------
	private class TigerBotV2Observer implements ITigerBotV2Observer
	{
		@Override
		public void onNameChanged(String name)
		{
			summary.setBotName(name);
			mainPanel.setBotName(name);
		}
		
		
		@Override
		public void onIdChanged(BotID oldId, BotID newId)
		{
			summary.setId(newId);
			mainPanel.setId(newId);
		}
		
		
		@Override
		public void onOofCheckChanged(boolean enable)
		{
			summary.setOofCheck(enable);
		}
		
		
		@Override
		public void onNetworkStateChanged(ENetworkState state)
		{
			summary.setNetworkState(state);
			mainPanel.setConnectionState(state);
		}
		
		
		@Override
		public void onNewSystemStatusV2(TigerSystemStatusV2 status)
		{
			systemStatus.addSystemStatusV2(status);
			dribblerControl.getConfigPanel().setSpeedReached(status.isDribblerSpeedReached());
		}
		
		
		@Override
		public void onNewKickerStatusV3(TigerKickerStatusV3 status)
		{
			kicker.getStatusPanel().setChg(status.getChargeCurrent());
			kicker.getStatusPanel().setCap(status.getCapLevel());
			kicker.getStatusPanel().setIrLevel(status.getIrLevel());
			
			kicker.getPlotPanel().addCapLevel(status.getCapLevel());
			kicker.getPlotPanel().addChargeCurrent(status.getChargeCurrent());
			
			fastChgPanel.setChargeLvL(status.getCapLevel());
		}
		
		
		@Override
		public void onNewSystemStatusMovement(TigerSystemStatusMovement status)
		{
			if ((System.nanoTime() - startMotor) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
			{
				controllerPanel.getEnhancedInputPanel().setLatestVelocity(status.getVelocity());
				controllerPanel.getEnhancedInputPanel().setLatestAngularVelocity(status.getAngularVelocity());
				startMotor = System.nanoTime();
			}
		}
		
		
		@Override
		public void onNewSystemPowerLog(TigerSystemPowerLog log)
		{
			power.addPowerLog(log);
			summary.setBatteryLevel(log.getBatLevel());
		}
		
		
		@Override
		public void onLogsChanged(TigerSystemSetLogs logs)
		{
			mainPanel.setLogMovement(logs.getMovement());
			mainPanel.setLogKicker(logs.getKicker());
		}
		
		
		@Override
		public void onBotFeaturesChanged(final Map<EFeature, EFeatureState> newFeatures)
		{
			features.setFeatures(newFeatures);
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
			if (log.getId() != 4)
			{
				return;
			}
			
			dribblerControl.getPidPanel().setLog(log);
			
			dribblerControl.getConfigPanel().setLatest(log.getLatest());
			dribblerControl.getConfigPanel().setOverload(log.getOverload());
			dribblerControl.getConfigPanel().setECurrent(log.getECurrent());
		}
		
		
		@Override
		public void onSensorFusionParamsChanged(SensorFusionParameters params)
		{
			controllerPanel.getFusionPanel().setSensorFusionParams(params);
		}
		
		
		@Override
		public void onControllerParamsChanged(ControllerParameters params)
		{
			controllerPanel.getFusionPanel().setControllerParams(params);
			
			dribblerControl.getConfigPanel().setPidParams(params.getDribbler().getKp(), params.getDribbler().getKi(),
					params.getDribbler().getKd());
		}
		
		
		@Override
		public void onSystemConsolePrint(TigerSystemConsolePrint print)
		{
			consolePanel.addConsolePrint(print);
		}
		
		
		@Override
		public void onControllerTypeChanged(ControllerType type)
		{
			controllerPanel.getSelectControllerPanel().setControllerType(type);
		}
		
		
		@Override
		public void onBlocked(boolean blocked)
		{
		}
	}
	
	private class NetworkStatisticsUpdater extends TimerTask
	{
		private Statistics	lastTxStats	= new Statistics();
		private Statistics	lastRxStats	= new Statistics();
		
		
		@Override
		public void run()
		{
			final Statistics txStats = bot.getTxStats();
			mainPanel.setTxStat(txStats.substract(lastTxStats));
			lastTxStats = new Statistics(txStats);
			
			final Statistics rxStats = bot.getRxStats();
			mainPanel.setRxStat(rxStats.substract(lastRxStats));
			lastRxStats = new Statistics(rxStats);
		}
	}
	
	private class TigerBotV2SummaryObserver implements ITigerBotV2SummaryObserver
	{
		@Override
		public void onOOFCheckChange(boolean oofCheck)
		{
			bot.setOofCheck(oofCheck);
		}
		
		
		@Override
		public void onConnectionChange()
		{
			TigerBotV2Presenter.this.onConnectionChange();
		}
	}
	
	
	private class PingThread extends Thread
	{
		private long							delay			= 100000000;
		
		private int								id				= 0;
		
		private final Map<Integer, Long>	activePings	= new HashMap<Integer, Long>();
		
		
		/**
		 * @param numPings
		 */
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
				
				bot.execute(new TigerSystemPing(id));
				id++;
				
				ThreadUtil.parkNanosSafe(delay);
			}
			
			activePings.clear();
		}
		
		
		/**
		 * @param id
		 */
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
			
			// TODO AndreR: rework or delete?
			// final float delayPongArrive = (System.nanoTime() - startTime) / 1000000.0f;
			
			// network.setDelay(delayPongArrive);
		}
	}
	
}
