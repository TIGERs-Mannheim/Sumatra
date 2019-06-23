/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.ControllerParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParametersXYW;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorFusionParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.StateUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.Structure;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ITigerBotV2Observer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2.PingStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECtrlMoveType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.EControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerKickerStatusV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusExt;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.CSVExporter;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.CommandPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.FeaturePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.PowerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.SkillsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.SplinePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.ConsolePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.ConsolePanel.IConsolePanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.ControllerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.FusionCtrlPanel.IFusionCtrlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.MovePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.SelectControllerPanel.ISelectControllerPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.StructurePanel.IStructureObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.SystemStatusPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.SystemStatusPanel.ISystemStatusPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.TigerBotV2MainPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.TigerBotV2MainPanel.ITigerBotV2MainPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.TigerBotV2Summary;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.dribbler.DribblerConfigurationPanel.IDribblerConfigurationPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.dribbler.DribblerControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.kicker.KickerPanelV2;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.kicker.KickerPanelV2.IKickerPanelV2Observer;


/**
 * Presenter for a tiger bot V2.
 * 
 * @author AndreR
 */
public class TigerBotV2Presenter extends ABotPresenter implements ITigerBotV2MainPanelObserver,
		ILookAndFeelStateObserver, IWorldPredictorObserver, IFusionCtrlPanelObserver, IConsolePanelObserver,
		ISelectControllerPanelObserver, IDribblerConfigurationPanelObserver, ISystemStatusPanelObserver,
		IStructureObserver, IKickerPanelV2Observer, IInstanceableObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger		log							= Logger.getLogger(TigerBotV2Presenter.class.getName());
	
	private TigerBotV2					bot							= null;
	
	private AWorldPredictor				worldPredictor				= null;
	private TigerBotV2Summary			summary						= null;
	private KickerConfig					fastChgPanel				= null;
	private TigerBotV2MainPanel		mainPanel					= null;
	private SkillsPanel					skills						= null;
	private KickerPanelV2				kicker						= null;
	private PowerPanel					power							= null;
	private FeaturePanel					features						= null;
	private SystemStatusPanel			systemStatus				= null;
	private DribblerControlPanel		dribblerControl			= null;
	private ControllerPanel				controllerPanel			= null;
	private MovePanel						movePanel					= null;
	private ConsolePanel					consolePanel				= null;
	private SplinePanel					splinePanel					= null;
	private CommandPanel					commandPanel				= null;
	
	private CSVExporter					movementCSVExporter		= null;
	private long							movementExportStartTime	= 0;
	
	// Observer Handler
	private final TigerBotV2Observer	tigerBotObserver			= new TigerBotV2Observer();
	
	
	private long							startNewWP					= System.nanoTime();
	// in milliseconds
	private static final long			VISUALIZATION_FREQUENCY	= 200;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param bot
	 */
	public TigerBotV2Presenter(final ABot bot)
	{
		super(bot);
		try
		{
			worldPredictor = (AWorldPredictor) SumatraModel.getInstance().getModule("worldpredictor");
		} catch (final ModuleNotFoundException err)
		{
			log.error("Botmanager not found", err);
			
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
		movePanel = new MovePanel();
		features = new FeaturePanel();
		splinePanel = new SplinePanel();
		consolePanel = new ConsolePanel();
		dribblerControl = new DribblerControlPanel();
		commandPanel = new CommandPanel();
		
		this.bot = (TigerBotV2) bot;
		
		node = new BotCenterTreeNode(getPrintableName(), ETreeIconType.BOT, bot.getColor().getColor(), mainPanel, true);
		
		node.add(new BotCenterTreeNode("Power", ETreeIconType.LIGHTNING, power, true));
		node.add(new BotCenterTreeNode("Status", ETreeIconType.AP, systemStatus, true));
		node.add(new BotCenterTreeNode("Kicker", ETreeIconType.KICK, kicker, true));
		node.add(new BotCenterTreeNode("Controller", ETreeIconType.GRAPH, controllerPanel, true));
		node.add(new BotCenterTreeNode("Move", ETreeIconType.MOTOR, movePanel, true));
		node.add(new BotCenterTreeNode("Dribbler", ETreeIconType.MOTOR, dribblerControl, true));
		node.add(new BotCenterTreeNode("Skills", ETreeIconType.LAMP, skills, true));
		node.add(new BotCenterTreeNode("Features", ETreeIconType.GEAR, features, true));
		node.add(new BotCenterTreeNode("Spline", ETreeIconType.SPLINE, splinePanel, true));
		node.add(new BotCenterTreeNode("Console", ETreeIconType.CONSOLE, consolePanel, false));
		node.add(new BotCenterTreeNode("Command", ETreeIconType.CONSOLE, commandPanel, false));
		
		summary.setId(bot.getBotID());
		summary.setBotName(bot.getName());
		
		tigerBotObserver.onNameChanged(bot.getName());
		tigerBotObserver.onIdChanged(BotID.createBotId(), bot.getBotID());
		tigerBotObserver.onNetworkStateChanged(this.bot.getNetworkState());
		tigerBotObserver.onLogsChanged(this.bot.getLogs());
		tigerBotObserver.onSensorFusionParamsChanged(this.bot.getSensorFusionParams());
		tigerBotObserver.onControllerParamsChanged(this.bot.getControllerParams());
		tigerBotObserver.onControllerTypeChanged(this.bot.getControllerType());
		tigerBotObserver.onCtrlMoveTypeChanged(this.bot.getCtrlMoveType());
		tigerBotObserver.onBotFeaturesChanged(this.bot.getBotFeatures());
		tigerBotObserver.onStructureChanged(this.bot.getStructure());
		
		this.bot.addObserver(tigerBotObserver);
		mainPanel.addObserver(this);
		skills.addObserver(this);
		kicker.addObserver(this);
		worldPredictor.addObserver(this);
		controllerPanel.getFusionPanel().addObserver(this);
		controllerPanel.getSelectControllerPanel().addObserver(this);
		controllerPanel.getStructurePanel().addObserver(this);
		consolePanel.addObserver(this);
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		movePanel.getInputPanel().addObserver(this);
		movePanel.getEnhancedInputPanel().addObserver(this);
		features.addObserver(this);
		dribblerControl.getConfigPanel().addObserver(this);
		systemStatus.addObserver(this);
		commandPanel.addObserver(this);
		
		GeneralPurposeTimer.getInstance().scheduleAtFixedRate(new NetworkStatisticsUpdater(), 0, 1000);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private String getPrintableName()
	{
		return bot.getName() + " (" + bot.getBotID().getNumber() + ")";
	}
	
	
	@Override
	public void delete()
	{
		bot.removeObserver(tigerBotObserver);
		mainPanel.removeObserver(this);
		skills.removeObserver(this);
		kicker.removeObserver(this);
		worldPredictor.removeObserver(this);
		controllerPanel.getFusionPanel().removeObserver(this);
		controllerPanel.getSelectControllerPanel().removeObserver(this);
		consolePanel.removeObserver(this);
		LookAndFeelStateAdapter.getInstance().removeObserver(this);
		movePanel.getInputPanel().removeObserver(this);
		movePanel.getEnhancedInputPanel().removeObserver(this);
		dribblerControl.getConfigPanel().removeObserver(this);
		systemStatus.removeObserver(this);
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
				SwingUtilities.updateComponentTreeUI(movePanel);
			}
		});
	}
	
	
	@Override
	public void onSaveGeneral()
	{
		try
		{
			if ((mainPanel.getId() != bot.getBotID().getNumber())
					|| (mainPanel.getColor() != bot.getBotID().getTeamColor()))
			{
				getBotmanager().changeBotId(bot.getBotID(), BotID.createBotId(mainPanel.getId(), mainPanel.getColor()));
			}
			
			bot.setName(mainPanel.getBotName());
			bot.setBaseStationKey(mainPanel.getBaseStation());
			IBaseStation bs = getBotmanager().getBaseStations().get(mainPanel.getBaseStation());
			if (bs == null)
			{
				log.error("No baseStation with id=" + mainPanel.getBaseStation());
			} else
			{
				bot.setBaseStation(bs);
			}
			
			bot.stop();
			bot.start();
		}
		
		catch (final NumberFormatException e)
		{
			log.warn("Invalid value in a general configuration field");
		}
	}
	
	
	@Override
	public void onChangeId(final int id, final ETeamColor color)
	{
		TigerSystemConsoleCommand conCmd = new TigerSystemConsoleCommand();
		int cId = BotID.createBotId(id, color).getNumberWithColorOffsetBS();
		conCmd.setText("write botid " + cId);
		conCmd.setTarget(ConsoleCommandTarget.MAIN);
		bot.execute(conCmd);
	}
	
	
	@Override
	public void onStartPing(final int numPings, final int payloadSize)
	{
		bot.startPing(numPings, payloadSize);
	}
	
	
	@Override
	public void onStopPing()
	{
		bot.stopPing();
	}
	
	
	@Override
	public void onSaveLogs()
	{
		final boolean moveLog = mainPanel.getLogMovement();
		final boolean extMoveLog = mainPanel.getLogExtMovement();
		final boolean kickerLog = mainPanel.getLogKicker();
		final boolean powerLog = mainPanel.getLogPower();
		
		bot.setLogMovement(moveLog, extMoveLog);
		bot.setLogKicker(kickerLog);
		bot.setLogPower(powerLog);
	}
	
	
	@Override
	public void onKickerChargeAuto(final int max)
	{
		bot.setKickerMaxCap(max);
		bot.execute(new TigerKickerChargeAuto(max));
	}
	
	
	@Override
	public void onKickerFire(final float duration, final EKickerMode mode, final int device)
	{
		final TigerKickerKickV2 kick = new TigerKickerKickV2(device, mode, duration);
		bot.execute(kick);
	}
	
	
	@Override
	public void onNewWorldFrame(final SimpleWorldFrame wf)
	{
		final TrackedTigerBot tracked = wf.getBot(bot.getBotID());
		if (tracked == null)
		{
			return;
		}
		if ((System.nanoTime() - startNewWP) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
		{
			final Vector2f vel = new Vector2f(tracked.getVel().turnNew(-tracked.getAngle()));
			
			movePanel.getEnhancedInputPanel().setLatestWPData(new Vector2f(-vel.y(), vel.x()), tracked.getaVel());
			startNewWP = System.nanoTime();
		}
	}
	
	
	@Override
	public void onVisionSignalLost(final SimpleWorldFrame emptyWf)
	{
		movePanel.getEnhancedInputPanel().setLatestWPData(new Vector2f(0.0f, 0.0f), 0.0f);
	}
	
	
	@Override
	public void onNewCamDetectionFrame(final CamDetectionFrame frame)
	{
	}
	
	
	@Override
	public void onNewStateUncertainties(final StateUncertainties unc)
	{
		bot.setStateUncertainties(unc);
	}
	
	
	@Override
	public void onNewSensorUncertainties(final SensorUncertainties unc)
	{
		bot.setSensorUncertainties(unc);
	}
	
	
	@Override
	public void onNewControllerParams(final PIDParametersXYW pos, final PIDParametersXYW vel,
			final PIDParametersXYW spline, final PIDParameters motor)
	{
		bot.setPIDParamsPos(pos);
		bot.setPIDParamsVel(vel);
		bot.setPIDParamsSpline(spline);
		bot.setPIDParamsMotor(motor);
	}
	
	
	@Override
	public void onConsoleCommand(final String cmd, final ConsoleCommandTarget target)
	{
		TigerSystemConsoleCommand conCmd = new TigerSystemConsoleCommand();
		conCmd.setText(cmd);
		conCmd.setTarget(target);
		bot.execute(conCmd);
	}
	
	
	@Override
	public void onNewControllerSelected(final EControllerType type)
	{
		bot.setControllerType(type);
	}
	
	
	@Override
	public void onNewCtrlMoveSelected(final ECtrlMoveType type)
	{
		bot.setCtrlMoveType(type);
	}
	
	
	@Override
	public void onApplyControllerToAll(final EControllerType ctrlType, final ECtrlMoveType ctrlMoveType)
	{
		for (ABot b : getBotmanager().getAllBots().values())
		{
			if (b.getType() == EBotType.TIGER_V2)
			{
				TigerBotV2 botV2 = (TigerBotV2) b;
				botV2.setControllerType(ctrlType);
				botV2.setCtrlMoveType(ctrlMoveType);
				botV2.notifyControllerTypeChanged(ctrlType);
				botV2.notifyCtrlMoveTypeChanged(ctrlMoveType);
			}
		}
	}
	
	
	@Override
	public void onNewStructure(final Structure structure)
	{
		bot.setStructure(structure);
	}
	
	
	@Override
	public void onSetDribblerLog(final boolean logging)
	{
		bot.setDribblerLogging(logging);
	}
	
	
	@Override
	public void onSetDribblerPidParams(final float kp, final float ki, final float kd)
	{
		bot.setDribblerPid(new PIDParameters(kp, ki, kd));
	}
	
	
	@Override
	public void onSetDribblerRPM(final int rpm)
	{
		bot.execute(new TigerDribble(rpm));
		
	}
	
	
	@Override
	public void onCopyCtrlValuesToAll()
	{
		for (ABot abot : getBotmanager().getAllBots().values())
		{
			if (abot.getType() != EBotType.TIGER_V2)
			{
				continue;
			}
			
			TigerBotV2 v2 = (TigerBotV2) abot;
			
			v2.setControllerAndFusionParams(new ControllerParameters(bot.getControllerParams()),
					new SensorFusionParameters(bot.getSensorFusionParams()));
			v2.setStructure(new Structure(bot.getStructure()));
		}
		
		log.info("Copied controller values to all bots");
	}
	
	
	@Override
	public void onCaptureMovementData(final boolean capture)
	{
		if (capture)
		{
			if (movementCSVExporter == null)
			{
				newMovementExporter();
			}
		} else
		{
			if (movementCSVExporter != null)
			{
				movementCSVExporter.close();
				movementCSVExporter = null;
			}
		}
	}
	
	
	private void newMovementExporter()
	{
		if (movementCSVExporter != null)
		{
			movementCSVExporter.close();
		}
		String name = "movement_bot_" + getBot().getBotID().getNumber() + "_";
		movementCSVExporter = CSVExporter.createInstance(name, name, true);
		movementExportStartTime = System.nanoTime();
	}
	
	// -------------------------------------------------------------
	// Sub classes
	// -------------------------------------------------------------
	private class TigerBotV2Observer implements ITigerBotV2Observer
	{
		@Override
		public void onNameChanged(final String name)
		{
			summary.setBotName(name);
			mainPanel.setBotName(name);
			node.setTitle(getPrintableName());
		}
		
		
		@Override
		public void onIdChanged(final BotID oldId, final BotID newId)
		{
			summary.setId(newId);
			mainPanel.setId(newId);
			mainPanel.setColor(newId.getTeamColor());
			node.setTitle(getPrintableName());
			node.setColor(newId.getTeamColor().getColor());
		}
		
		
		@Override
		public void onNetworkStateChanged(final ENetworkState state)
		{
			getBotmanager().botConnectionChanged(bot);
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					summary.setNetworkState(state);
					mainPanel.setConnectionState(state);
					mainPanel.setBaseStation(bot.getBaseStationKey());
				}
			});
		}
		
		
		@Override
		public void onNewSystemStatusV2(final TigerSystemStatusV2 status)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					systemStatus.addSystemStatusV2(status);
					dribblerControl.getConfigPanel().setSpeedReached(status.isDribblerSpeedReached());
					summary.setCap(status.getKickerLevel());
					
					if (movementCSVExporter != null)
					{
						List<Float> vals = status.getAllValues();
						long timeDiff = System.nanoTime() - movementExportStartTime;
						timeDiff = TimeUnit.NANOSECONDS.toMillis(timeDiff);
						vals.add((float) timeDiff);
						movementCSVExporter.addValues(vals);
					}
				}
			});
		}
		
		
		@Override
		public void onNewSystemStatusExt(final TigerSystemStatusExt status)
		{
			onNewSystemStatusV2(status);
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					systemStatus.addSystemStatusExt(status);
				}
			});
		}
		
		
		@Override
		public void onNewKickerStatusV3(final TigerKickerStatusV3 status)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					kicker.getStatusPanel().setChg(status.getChargeCurrent());
					kicker.getStatusPanel().setCap(status.getCapLevel());
					kicker.getStatusPanel().setIrLevel(status.getIrLevel());
					
					kicker.getPlotPanel().addCapLevel(status.getCapLevel());
					kicker.getPlotPanel().addChargeCurrent(status.getChargeCurrent());
					
					fastChgPanel.setChargeLvL(status.getCapLevel());
				}
			});
		}
		
		
		@Override
		public void onNewSystemStatusMovement(final TigerSystemStatusMovement status)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					movePanel.getEnhancedInputPanel().setLatestVelocity(status.getVelocity());
					movePanel.getEnhancedInputPanel().setLatestAngularVelocity(status.getAngularVelocity());
				}
			});
		}
		
		
		@Override
		public void onNewSystemPowerLog(final TigerSystemPowerLog log)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					power.addPowerLog(log);
					summary.setBatteryLevel(log.getBatLevel());
				}
			});
		}
		
		
		@Override
		public void onLogsChanged(final TigerSystemSetLogs logs)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					mainPanel.setLogMovement(logs.getMovement());
					mainPanel.setLogExtMovement(logs.getExtMovement());
					mainPanel.setLogKicker(logs.getKicker());
					mainPanel.setLogPower(logs.getPower());
				}
			});
		}
		
		
		@Override
		public void onBotFeaturesChanged(final Map<EFeature, EFeatureState> newFeatures)
		{
			features.setFeatures(newFeatures);
		}
		
		
		@Override
		public void onNewPingStats(final PingStats stats)
		{
			mainPanel.setPingStats(stats);
		}
		
		
		@Override
		public void onNewMotorPidLog(final TigerMotorPidLog log)
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
		public void onSensorFusionParamsChanged(final SensorFusionParameters params)
		{
			controllerPanel.getFusionPanel().setSensorFusionParams(params);
		}
		
		
		@Override
		public void onControllerParamsChanged(final ControllerParameters params)
		{
			controllerPanel.getFusionPanel().setControllerParams(params);
			
			dribblerControl.getConfigPanel().setPidParams(params.getDribbler().getKp(), params.getDribbler().getKi(),
					params.getDribbler().getKd());
		}
		
		
		@Override
		public void onStructureChanged(final Structure structure)
		{
			controllerPanel.getStructurePanel().setStructure(structure);
		}
		
		
		@Override
		public void onSystemConsolePrint(final TigerSystemConsolePrint print)
		{
			consolePanel.addConsolePrint(print);
		}
		
		
		@Override
		public void onControllerTypeChanged(final EControllerType type)
		{
			controllerPanel.getSelectControllerPanel().setControllerType(type);
		}
		
		
		@Override
		public void onCtrlMoveTypeChanged(final ECtrlMoveType type)
		{
			controllerPanel.getSelectControllerPanel().setCtrlMoveType(type);
		}
		
		
		@Override
		public void onBlocked(final boolean blocked)
		{
		}
		
		
		@Override
		public void onNewSplineData(final SplinePair3D spline)
		{
			splinePanel.showSpline(spline);
			
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
	
	
	@Override
	public void onNewInstance(final Object object)
	{
		bot.execute((ACommand) object);
	}
	
}
