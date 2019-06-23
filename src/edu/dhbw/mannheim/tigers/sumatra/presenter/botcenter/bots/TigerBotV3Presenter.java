/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 2, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParametersXYW;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.StateUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.Structure;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.PingThread;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.PingThread.IPingThreadObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.DummyBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.PingStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.EControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetFilterParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetFilterParams.ParamType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetPIDParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetPIDParams.PIDParamType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemQuery;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemQuery.EQueryType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerKickerConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemPerformance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.BotSkillWrapperSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.StraightMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.BotWatcher;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.ISkillsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.IMotorEnhancedInputPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorInputPanel.IMotorInputPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.ConsolePanel.IConsolePanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.FusionCtrlPanel.IFusionCtrlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.SelectControllerPanel.ISelectControllerPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.StructurePanel.IStructureObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.kicker.KickerPanelV2.IKickerPanelV2Observer;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv3.SystemMatchFeedbackPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv3.SystemMatchFeedbackPanel.ISystemMatchFeedbackPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2.BcBotPingPanel.IBcBotPingPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2.BotConfigOverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenterv2.KickerConfigPanel.IKickerConfigPanelObserver;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerBotV3Presenter
{
	private static final Logger				log						= Logger.getLogger(TigerBotV3Presenter.class.getName());
	private PingThread							pingThread				= null;
	private ABot									bot						= new DummyBot();
	private final BotConfigOverviewPanel	botConfigOverviewPanel;
	private GenericSkillSystem					skillsystem;
	private ABotManager							botManager;
	private ConfigPresenter						configPresenter;
	
	private ControllerData						knownControllerData	= new ControllerData();
	
	private static class ControllerData
	{
		PIDParametersXYW		pos;
		PIDParametersXYW		vel;
		PIDParametersXYW		spline;
		PIDParameters			motor;
		StateUncertainties	state;
		SensorUncertainties	sensor;
		EControllerType		ctrlType;
	}
	
	
	/**
	 * @param botConfigOverviewPanel
	 */
	public TigerBotV3Presenter(final BotConfigOverviewPanel botConfigOverviewPanel)
	{
		this.botConfigOverviewPanel = botConfigOverviewPanel;
		
		configPresenter = new ConfigPresenter(botConfigOverviewPanel.getConfigPanel(), bot);
		
		botConfigOverviewPanel.getBcBotPingPanel().addObserver(new BcBotPingPanelObserver());
		KickerPanelV2Observer kickerPanelObserver = new KickerPanelV2Observer();
		botConfigOverviewPanel.getBcBotKickerPanel().getKickerFirePanel().addObserver(kickerPanelObserver);
		botConfigOverviewPanel.getBcBotControllerCfgPanel().getSelectControllerPanel()
				.addObserver(new SelectControllerPanelObserver());
		botConfigOverviewPanel.getBcBotControllerCfgPanel().getFusionCtrlPanel()
				.addObserver(new FusionCtrlPanelObserver());
		botConfigOverviewPanel.getBcBotControllerCfgPanel().getStructurePanel().addObserver(new StructurePanelObserver());
		botConfigOverviewPanel.getMovePanel().getInputPanel().addObserver(new MotorInputPanelObserver());
		botConfigOverviewPanel.getMovePanel().getEnhancedInputPanel().addObserver(new MotorEnhancedInputPanelObserver());
		botConfigOverviewPanel.getSkillsPanel().addObserver(new SkillsPanelObserver());
		botConfigOverviewPanel.getConsolePanel().addObserver(new ConsolePanelObserver());
		botConfigOverviewPanel.getCommandPanel().addObserver(new NewCommandObserver());
		botConfigOverviewPanel.getBcBotKickerPanel().getKickerConfigPanel().addObserver(new KickerConfigPanelObserver());
		botConfigOverviewPanel.getSystemStatusPanel().addObserver(new SystemMatchFeedbackObserver());
		
		// schedule GUI updates every 1s
		Timer timer = new Timer(1000, new SwingUpdateTimer());
		timer.start();
	}
	
	
	/**
	 * @return the bot
	 */
	public ABot getBot()
	{
		return bot;
	}
	
	
	/**
	 * @param bot the selectedBotId to set
	 */
	public void updateSelectedBotId(final ABot bot)
	{
		this.bot = bot;
		configPresenter.setBot(bot);
	}
	
	
	/**
	 * @param skillsystem the skillsystem to set
	 */
	public void setSkillsystem(final GenericSkillSystem skillsystem)
	{
		this.skillsystem = skillsystem;
	}
	
	
	/**
	 * @param botManager the botManager to set
	 */
	public void setBotManager(final ABotManager botManager)
	{
		this.botManager = botManager;
		configPresenter.setBotManager(botManager);
	}
	
	
	/**
	 * @param cmd
	 */
	public void processCommand(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_SYSTEM_CONSOLE_PRINT:
				TigerSystemConsolePrint print = (TigerSystemConsolePrint) cmd;
				botConfigOverviewPanel.getConsolePanel().addConsolePrint(print);
				break;
			case CMD_SYSTEM_PONG:
			{
				final TigerSystemPong pong = (TigerSystemPong) cmd;
				
				if (pingThread != null)
				{
					pingThread.pongArrived(pong.getId());
					if (!pong.payloadValid())
					{
						log.warn("Invalid payload received: " + Arrays.toString(pong.getPayload()));
					}
				}
			}
				break;
			case CMD_SYSTEM_MATCH_FEEDBACK:
			{
				final TigerSystemMatchFeedback feedback = (TigerSystemMatchFeedback) cmd;
				SystemMatchFeedbackPanel statusPanel = botConfigOverviewPanel.getSystemStatusPanel();
				statusPanel.addTigerSystemMatchFeedback(feedback);
			}
				break;
			case CMD_SYSTEM_PERFORMANCE:
			{
				final TigerSystemPerformance perf = (TigerSystemPerformance) cmd;
				SystemMatchFeedbackPanel statusPanel = botConfigOverviewPanel.getSystemStatusPanel();
				statusPanel.addPerformance(perf);
			}
				break;
			case CMD_CTRL_SET_PID_PARAMS:
				final TigerCtrlSetPIDParams params = (TigerCtrlSetPIDParams) cmd;
				botConfigOverviewPanel.getBcBotControllerCfgPanel().getFusionCtrlPanel()
						.setParams(params.getParamType(), params.getParams());
				break;
			case CMD_CTRL_SET_CONTROLLER_TYPE:
				final TigerCtrlSetControllerType ctrlTypeCmd = (TigerCtrlSetControllerType) cmd;
				botConfigOverviewPanel.getBcBotControllerCfgPanel().getSelectControllerPanel()
						.setControllerType(ctrlTypeCmd.getControllerType());
				break;
			case CMD_CTRL_SET_FILTER_PARAMS:
				final TigerCtrlSetFilterParams filterParams = (TigerCtrlSetFilterParams) cmd;
				botConfigOverviewPanel.getBcBotControllerCfgPanel().getFusionCtrlPanel().setFilterParams(filterParams);
				break;
			case CMD_KICKER_CONFIG:
				final TigerKickerConfig cfg = (TigerKickerConfig) cmd;
				botConfigOverviewPanel.getBcBotKickerPanel().getKickerConfigPanel().setConfig(cfg);
				break;
			case CMD_CONFIG_FILE_STRUCTURE:
			case CMD_CONFIG_ITEM_DESC:
			case CMD_CONFIG_READ:
				configPresenter.onNewCommand(cmd);
				break;
			default:
				break;
		}
	}
	
	private class BcBotPingPanelObserver implements IBcBotPingPanelObserver
	{
		private ScheduledExecutorService	pingService	= null;
		
		
		@Override
		public void onStartPing(final int numPings, final int payloadSize)
		{
			onStopPing();
			
			pingThread = new PingThread(payloadSize, bot);
			pingThread.addObserver(new PingThreadObserver());
			pingService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Ping Executor"));
			pingService.scheduleAtFixedRate(pingThread, 0, 1000000000 / numPings, TimeUnit.NANOSECONDS);
		}
		
		
		@Override
		public void onStopPing()
		{
			if (pingService == null)
			{
				return;
			}
			
			if (pingThread != null)
			{
				pingThread.clearObservers();
			}
			
			pingService.shutdownNow();
			pingService = null;
			pingThread = null;
		}
	}
	
	private class PingThreadObserver implements IPingThreadObserver
	{
		@Override
		public void onNewPingStats(final PingStats stats)
		{
			botConfigOverviewPanel.getBcBotPingPanel().setPingStats(stats);
		}
	}
	
	private class FusionCtrlPanelObserver implements IFusionCtrlPanelObserver
	{
		@Override
		public void onNewStateUncertainties(final StateUncertainties unc)
		{
			bot.execute(new TigerCtrlSetFilterParams(ParamType.EX_POS, unc.getPos()));
			bot.execute(new TigerCtrlSetFilterParams(ParamType.EX_VEL, unc.getVel()));
			bot.execute(new TigerCtrlSetFilterParams(ParamType.EX_ACC, unc.getAcc()));
			knownControllerData.state = unc;
		}
		
		
		@Override
		public void onNewSensorUncertainties(final SensorUncertainties unc)
		{
			bot.execute(new TigerCtrlSetFilterParams(ParamType.EZ_VISION, unc.getVision()));
			bot.execute(new TigerCtrlSetFilterParams(ParamType.EZ_ENCODER, unc.getEncoder()));
			TigerCtrlSetFilterParams accGyro = new TigerCtrlSetFilterParams(ParamType.EZ_ACC_GYRO, new Vector3(
					unc.getAccelerometer().x(), unc.getAccelerometer().y(),
					unc.getGyroscope()));
			bot.execute(accGyro);
			bot.execute(new TigerCtrlSetFilterParams(ParamType.EZ_MOTOR, unc.getMotor()));
			knownControllerData.sensor = unc;
		}
		
		
		@Override
		public void onNewControllerParams(final PIDParametersXYW pos, final PIDParametersXYW vel,
				final PIDParametersXYW spline,
				final PIDParameters motor)
		{
			bot.execute(new TigerCtrlSetPIDParams(PIDParamType.POS_X, pos.getX()));
			bot.execute(new TigerCtrlSetPIDParams(PIDParamType.POS_Y, pos.getY()));
			bot.execute(new TigerCtrlSetPIDParams(PIDParamType.POS_W, pos.getW()));
			
			bot.execute(new TigerCtrlSetPIDParams(PIDParamType.VEL_X, vel.getX()));
			bot.execute(new TigerCtrlSetPIDParams(PIDParamType.VEL_Y, vel.getY()));
			bot.execute(new TigerCtrlSetPIDParams(PIDParamType.VEL_W, vel.getW()));
			
			bot.execute(new TigerCtrlSetPIDParams(PIDParamType.SPLINE_X, spline.getX()));
			bot.execute(new TigerCtrlSetPIDParams(PIDParamType.SPLINE_Y, spline.getY()));
			bot.execute(new TigerCtrlSetPIDParams(PIDParamType.SPLINE_W, spline.getW()));
			
			bot.execute(new TigerCtrlSetPIDParams(PIDParamType.MOTOR, motor));
			
			knownControllerData.pos = pos;
			knownControllerData.vel = vel;
			knownControllerData.spline = spline;
			knownControllerData.motor = motor;
		}
		
		
		@Override
		public void onQuery(final EQueryType queryType)
		{
			bot.execute(new TigerSystemQuery(queryType));
		}
	}
	
	private class ConsolePanelObserver implements IConsolePanelObserver
	{
		@Override
		public void onConsoleCommand(final String cmd, final ConsoleCommandTarget target)
		{
			TigerSystemConsoleCommand consoleCmd = new TigerSystemConsoleCommand();
			consoleCmd.setTarget(target);
			consoleCmd.setText(cmd);
			bot.execute(consoleCmd);
		}
		
		
		@Override
		public void onConsoleCommand2All(final String cmd, final ConsoleCommandTarget target)
		{
			TigerSystemConsoleCommand consoleCmd = new TigerSystemConsoleCommand();
			consoleCmd.setTarget(target);
			consoleCmd.setText(cmd);
			for (ABot bot : botManager.getAllBots().values())
			{
				bot.execute(consoleCmd);
			}
		}
	}
	
	private class SelectControllerPanelObserver implements ISelectControllerPanelObserver
	{
		@Override
		public void onNewControllerSelected(final EControllerType controllerType)
		{
			bot.execute(new TigerCtrlSetControllerType(controllerType));
			knownControllerData.ctrlType = controllerType;
		}
		
		
		@Override
		public void onApplyControllerToAll(final EControllerType ctrlType)
		{
			for (ABot bot : botManager.getAllBots().values())
			{
				bot.execute(new TigerCtrlSetControllerType(ctrlType));
			}
		}
		
		
		@Override
		public void onCopyCtrlValuesToAll()
		{
			for (ABot bot : botManager.getAllBots().values())
			{
				if (knownControllerData.ctrlType != null)
				{
					bot.execute(new TigerCtrlSetControllerType(knownControllerData.ctrlType));
				}
				
				if (knownControllerData.pos != null)
				{
					bot.execute(new TigerCtrlSetPIDParams(PIDParamType.POS_X, knownControllerData.pos.getX()));
					bot.execute(new TigerCtrlSetPIDParams(PIDParamType.POS_Y, knownControllerData.pos.getY()));
					bot.execute(new TigerCtrlSetPIDParams(PIDParamType.POS_W, knownControllerData.pos.getW()));
					
					bot.execute(new TigerCtrlSetPIDParams(PIDParamType.VEL_X, knownControllerData.vel.getX()));
					bot.execute(new TigerCtrlSetPIDParams(PIDParamType.VEL_Y, knownControllerData.vel.getY()));
					bot.execute(new TigerCtrlSetPIDParams(PIDParamType.VEL_W, knownControllerData.vel.getW()));
					
					bot.execute(new TigerCtrlSetPIDParams(PIDParamType.SPLINE_X, knownControllerData.spline.getX()));
					bot.execute(new TigerCtrlSetPIDParams(PIDParamType.SPLINE_Y, knownControllerData.spline.getY()));
					bot.execute(new TigerCtrlSetPIDParams(PIDParamType.SPLINE_W, knownControllerData.spline.getW()));
					
					bot.execute(new TigerCtrlSetPIDParams(PIDParamType.MOTOR, knownControllerData.motor));
				}
				
				if (knownControllerData.state != null)
				{
					bot.execute(new TigerCtrlSetFilterParams(ParamType.EX_POS, knownControllerData.state.getPos()));
					bot.execute(new TigerCtrlSetFilterParams(ParamType.EX_VEL, knownControllerData.state.getVel()));
					bot.execute(new TigerCtrlSetFilterParams(ParamType.EX_ACC, knownControllerData.state.getAcc()));
				}
				if (knownControllerData.sensor != null)
				{
					bot.execute(new TigerCtrlSetFilterParams(ParamType.EZ_VISION, knownControllerData.sensor.getVision()));
					bot.execute(new TigerCtrlSetFilterParams(ParamType.EZ_ENCODER, knownControllerData.sensor.getEncoder()));
					TigerCtrlSetFilterParams accGyro = new TigerCtrlSetFilterParams(ParamType.EZ_ACC_GYRO, new Vector3(
							knownControllerData.sensor.getAccelerometer().x(), knownControllerData.sensor.getAccelerometer()
									.y(),
							knownControllerData.sensor.getGyroscope()));
					bot.execute(accGyro);
					bot.execute(new TigerCtrlSetFilterParams(ParamType.EZ_MOTOR, knownControllerData.sensor.getMotor()));
				}
			}
		}
		
		
		@Override
		public void onQuery(final EQueryType queryType)
		{
			bot.execute(new TigerSystemQuery(queryType));
		}
	}
	
	private class StructurePanelObserver implements IStructureObserver
	{
		@Override
		public void onNewStructure(final Structure structure)
		{
			TigerSystemConsoleCommand cmd = new TigerSystemConsoleCommand();
			cmd.setTarget(ConsoleCommandTarget.MAIN);
			
			cmd.setText(String.format(Locale.ENGLISH, "structure %f %f %f %f %f", structure.getFrontAngle(),
					structure.getBackAngle(),
					structure.getBotRadius(), structure.getWheelRadius(), structure.getMass()));
			bot.execute(cmd);
		}
		
		
		@Override
		public void onQuery(final EQueryType queryType)
		{
			bot.execute(new TigerSystemQuery(queryType));
		}
	}
	
	private class KickerPanelV2Observer implements IKickerPanelV2Observer
	{
		@Override
		public void onKickerFire(final float duration, final EKickerMode mode, final int device)
		{
			final TigerKickerKickV2 kick = new TigerKickerKickV2(device, mode, duration);
			bot.execute(kick);
		}
		
		
		@Override
		public void onKickerChargeAuto(final int max)
		{
			bot.setKickerMaxCap(max);
			bot.execute(new TigerKickerChargeAuto(max));
		}
	}
	
	private class KickerConfigPanelObserver implements IKickerConfigPanelObserver
	{
		@Override
		public void onSave(final TigerKickerConfig cfg)
		{
			bot.execute(cfg);
		}
		
		
		@Override
		public void onQuery()
		{
			bot.execute(new TigerSystemQuery(EQueryType.KICKER_CONFIG));
		}
		
		
		@Override
		public void onApplyToAll(final TigerKickerConfig cfg)
		{
			for (ABot bot : botManager.getAllBots().values())
			{
				bot.execute(cfg);
			}
		}
	}
	
	/**
	 * This is called regularly and directly on the Swing event dispatch thread (no new thread created, no invokelater
	 * needed)
	 */
	private class SwingUpdateTimer implements ActionListener
	{
		private Statistics	lastTxStats	= new Statistics();
		private Statistics	lastRxStats	= new Statistics();
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (bot == null)
			{
				return;
			}
			final Statistics txStats = bot.getTxStats();
			botConfigOverviewPanel.getBcBotNetStatsPanel().setTxStat(txStats.substract(lastTxStats));
			lastTxStats = new Statistics(txStats);
			
			final Statistics rxStats = bot.getRxStats();
			botConfigOverviewPanel.getBcBotNetStatsPanel().setRxStat(rxStats.substract(lastRxStats));
			lastRxStats = new Statistics(rxStats);
		}
	}
	
	private class MotorInputPanelObserver implements IMotorInputPanelObserver
	{
		@Override
		public void onSetSpeed(final float x, final float y, final float w, final float v)
		{
			bot.execute(new TigerMotorMoveV2(new Vector2(x, y), w, v));
		}
		
		
		@Override
		public void onSetSpeed(final float x, final float y, final float w)
		{
			bot.execute(new TigerMotorMoveV2(new Vector2(x, y), w));
		}
	}
	
	private class MotorEnhancedInputPanelObserver implements IMotorEnhancedInputPanelObserver
	{
		private IVector2	xy	= new Vector2();
		private float		w	= 0;
		
		
		@Override
		public void onNewVelocity(final Vector2 xy)
		{
			this.xy = xy;
			bot.execute(new TigerMotorMoveV2(xy, w));
		}
		
		
		@Override
		public void onNewAngularVelocity(final float w)
		{
			this.w = w;
			bot.execute(new TigerMotorMoveV2(xy, w));
		}
	}
	
	private class SkillsPanelObserver implements ISkillsPanelObserver
	{
		
		@Override
		public void onMoveToXY(final float x, final float y)
		{
			IMoveToSkill skill = AMoveSkill.createMoveToSkill();
			skill.setDoComplete(true);
			skill.getMoveCon().updateDestination(new Vector2(x, y));
			skillsystem.execute(bot.getBotID(), skill);
		}
		
		
		@Override
		public void onRotateAndMoveToXY(final float x, final float y, final float angle)
		{
			IMoveToSkill skill = AMoveSkill.createMoveToSkill();
			skill.setDoComplete(true);
			skill.getMoveCon().updateDestination(new Vector2(x, y));
			skill.getMoveCon().updateTargetAngle(angle);
			skillsystem.execute(bot.getBotID(), skill);
		}
		
		
		@Override
		public void onStraightMove(final int distance, final float angle)
		{
			skillsystem.execute(bot.getBotID(), new StraightMoveSkill(distance, angle));
		}
		
		
		@Override
		public void onLookAt(final Vector2 lookAtTarget)
		{
			IMoveToSkill skill = AMoveSkill.createMoveToSkill();
			skill.setDoComplete(true);
			skill.getMoveCon().updateLookAtTarget(lookAtTarget);
			skillsystem.execute(bot.getBotID(), skill);
		}
		
		
		@Override
		public void onDribble(final int rpm)
		{
			bot.execute(new TigerDribble(rpm));
		}
		
		
		@Override
		public void onSkill(final ASkill skill)
		{
			skillsystem.execute(bot.getBotID(), skill);
		}
		
		
		@Override
		public void onBotSkill(final ABotSkill skill)
		{
			BotSkillWrapperSkill wrapperSkill = new BotSkillWrapperSkill(skill);
			skillsystem.execute(bot.getBotID(), wrapperSkill);
		}
	}
	
	private class NewCommandObserver implements IInstanceableObserver
	{
		@Override
		public void onNewInstance(final Object object)
		{
			bot.execute((ACommand) object);
		}
	}
	
	private class SystemMatchFeedbackObserver implements ISystemMatchFeedbackPanelObserver
	{
		BotWatcher	botWatcher	= null;
		
		
		@Override
		public void onCapture(final boolean enable)
		{
			if (enable)
			{
				if (botWatcher != null)
				{
					botWatcher.stop();
				}
				botWatcher = new BotWatcher((TigerBotV3) bot);
				botWatcher.start();
			} else
			{
				if (botWatcher != null)
				{
					botWatcher.stop();
					botWatcher = null;
				}
			}
			
		}
	}
}
