/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.ControllerParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParametersXYW;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorFusionParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.StateUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.Structure;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStationObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECtrlMoveType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats.WifiStats;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerBootloaderResponse;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.EControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetFilterParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetFilterParams.ParamType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetPIDParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetPIDParams.PIDParamType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerKickerStatusV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusExt;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.util.IWatchdogObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;


/**
 * This is the Tiger bot v2013.
 * 
 * @author AndreR
 */
public class TigerBotV2 extends ABot implements IBaseStationObserver, IWatchdogObserver
{
	// Logger
	private static final Logger				log							= Logger.getLogger(TigerBotV2.class.getName());
	
	private static final float					BAT_MIN						= 12;
	private static final float					BAT_MAX						= 16.8f;
	
	private IBaseStation							baseStation					= null;
	
	private final Set<ITigerBotV2Observer>	observers					= new HashSet<ITigerBotV2Observer>();
	
	private ENetworkState						netState						= ENetworkState.OFFLINE;
	private final Statistics					txStats						= new Statistics();
	private final Statistics					rxStats						= new Statistics();
	
	private Vector2								lastDirection				= new Vector2();
	private float									lastAngularVelocity		= 0.0f;
	private float									lastCompensatedVelocity	= 0.0f;
	
	private final Bootloader					bootloader;
	
	// Kicker
	private TigerKickerStatusV3				lastKickerStatus			= null;
	
	// Motor
	private TigerSystemSetLogs					setLogs						= new TigerSystemSetLogs();
	
	private WifiStats								lastWifiStats				= new WifiStats();
	
	private SensorFusionParameters			sensorFusionParams		= new SensorFusionParameters();
	private ControllerParameters				controllerParams			= new ControllerParameters();
	private EControllerType						controllerType				= EControllerType.NONE;
	private ECtrlMoveType						ctrlmoveType				= ECtrlMoveType.SPLINE;
	private Structure								structure					= new Structure();
	private TigerSystemStatusV2				systemStatus				= new TigerSystemStatusV2();
	private TigerSystemPowerLog				powerLog						= new TigerSystemPowerLog();
	
	private PingThread							pingThread					= null;
	private ScheduledExecutorService			pingService					= null;
	
	
	/**
	 * @param botConfig
	 * @throws BotInitException
	 */
	public TigerBotV2(final SubnodeConfiguration botConfig) throws BotInitException
	{
		super(botConfig);
		
		try
		{
			controllerType = EControllerType.getControllerTypeConstant(botConfig.getInt("controllerType", 0));
			ctrlmoveType = ECtrlMoveType.valueOf(botConfig.getString("ctrlmoveType", ECtrlMoveType.SPLINE.toString()));
			
			setLogs.setMovement(botConfig.getBoolean("logs.movement", true));
			setLogs.setExtMovement(botConfig.getBoolean("logs.extMovement", false));
			setLogs.setKicker(botConfig.getBoolean("logs.kicker", true));
			setLogs.setPower(botConfig.getBoolean("logs.power", true));
			setLogs.setAccel(botConfig.getBoolean("logs.accel", false));
			setLogs.setIr(botConfig.getBoolean("logs.ir", false));
			
			sensorFusionParams = new SensorFusionParameters(botConfig.configurationAt("sensorFusion"));
			controllerParams = new ControllerParameters(botConfig.configurationAt("controller"));
			structure = new Structure(botConfig.configurationAt("structure"));
		} catch (final NoSuchElementException nse)
		{
			throw new BotInitException(botConfig, nse);
		}
		
		bootloader = new Bootloader(this);
		
		if (getBaseStationKey() == -1)
		{
			setBaseStationKey(botId.getTeamColor() == ETeamColor.YELLOW ? 0 : 1);
		}
	}
	
	
	/**
	 * @param id
	 */
	public TigerBotV2(final BotID id)
	{
		super(EBotType.TIGER_V2, id, id.getTeamColor() == ETeamColor.YELLOW ? 0 : 1, -1);
		
		bootloader = new Bootloader(this);
	}
	
	
	@Override
	protected Map<EFeature, EFeatureState> getDefaultFeatureStates()
	{
		Map<EFeature, EFeatureState> result = EFeature.createFeatureList();
		result.put(EFeature.DRIBBLER, EFeatureState.WORKING);
		result.put(EFeature.CHIP_KICKER, EFeatureState.WORKING);
		result.put(EFeature.STRAIGHT_KICKER, EFeatureState.WORKING);
		result.put(EFeature.MOVE, EFeatureState.WORKING);
		result.put(EFeature.BARRIER, EFeatureState.WORKING);
		return result;
	}
	
	
	@Override
	public void execute(final ACommand cmd)
	{
		if (baseStation == null)
		{
			return;
		}
		
		if (netState != ENetworkState.ONLINE)
		{
			return;
		}
		
		switch (cmd.getType())
		{
			case CMD_MOTOR_MOVE_V2:
			{
				final TigerMotorMoveV2 move = (TigerMotorMoveV2) cmd;
				
				move.setUnusedComponents(lastDirection, lastAngularVelocity, lastCompensatedVelocity);
				
				lastDirection = move.getXY();
				lastAngularVelocity = move.getW();
				lastCompensatedVelocity = move.getV();
				
				baseStation.enqueueCommand(getBotID(), move);
			}
				break;
			case CMD_KICKER_KICKV2:
			{
				final TigerKickerKickV2 kick = (TigerKickerKickV2) cmd;
				
				baseStation.enqueueCommand(getBotID(), kick);
			}
				break;
			case CMD_MOTOR_DRIBBLE:
			{
				final TigerDribble dribble = (TigerDribble) cmd;
				
				baseStation.enqueueCommand(getBotID(), dribble);
			}
				break;
			case CMD_SYSTEM_SET_LOGS:
			{
				final TigerSystemSetLogs logs = (TigerSystemSetLogs) cmd;
				
				setLogs = logs;
				
				baseStation.enqueueCommand(getBotID(), logs);
				
				notifyLogsChanged(logs);
			}
				break;
			case CMD_CTRL_SET_FILTER_PARAMS:
			{
				final TigerCtrlSetFilterParams params = (TigerCtrlSetFilterParams) cmd;
				
				sensorFusionParams.updateWithCommand(params);
				
				baseStation.enqueueCommand(getBotID(), params);
				
				notifySensorFusionParamsChanged(sensorFusionParams);
			}
				break;
			case CMD_CTRL_SET_PID_PARAMS:
			{
				final TigerCtrlSetPIDParams params = (TigerCtrlSetPIDParams) cmd;
				
				controllerParams.updateWithCommand(params);
				
				baseStation.enqueueCommand(getBotID(), params);
				
				notifyControllerParamsChanged(controllerParams);
			}
				break;
			case CMD_CTRL_SET_CONTROLLER_TYPE:
			{
				final TigerCtrlSetControllerType type = (TigerCtrlSetControllerType) cmd;
				
				log.debug("Setting controller to: " + type.getControllerType().name());
				
				controllerType = type.getControllerType();
				
				baseStation.enqueueCommand(getBotID(), type);
				
				notifyControllerTypeChanged(controllerType);
			}
				break;
			case CMD_CTRL_SPLINE_1D:
				baseStation.enqueueCommand(getBotID(), cmd);
				break;
			case CMD_CTRL_SPLINE_2D:
				baseStation.enqueueCommand(getBotID(), cmd);
				break;
			default:
			{
				baseStation.enqueueCommand(getBotID(), cmd);
			}
				break;
		}
		
		txStats.packets++;
		txStats.payload += CommandFactory.getInstance().getLength(cmd, false);
	}
	
	
	@Override
	public void start()
	{
		// if (!active)
		// {
		// return;
		// }
		//
		// setActive(active);
		// setOofCheck(oofCheck);
		//
		// if (netState == ENetworkState.OFFLINE)
		// {
		// changeNetworkState(ENetworkState.CONNECTING);
		// }
	}
	
	
	@Override
	public void stop()
	{
		// changeNetworkState(ENetworkState.OFFLINE);
	}
	
	
	@Override
	public void onIncommingBotCommand(final BotID id, final ACommand cmd)
	{
		if (!id.equals(getBotID()))
		{
			return;
		}
		
		changeNetworkState(ENetworkState.ONLINE);
		
		switch (cmd.getType())
		{
			case CMD_SYSTEM_STATUS_V2:
			{
				final TigerSystemStatusV2 status = (TigerSystemStatusV2) cmd;
				
				notifyNewSystemStatusV2(status);
			}
				break;
			case CMD_SYSTEM_STATUS_EXT:
			{
				final TigerSystemStatusExt status = (TigerSystemStatusExt) cmd;
				
				notifyNewSystemStatusExt(status);
			}
				break;
			case CMD_KICKER_STATUSV3:
			{
				final TigerKickerStatusV3 stats = (TigerKickerStatusV3) cmd;
				
				lastKickerStatus = stats;
				
				notifyNewKickerStatusV3(stats);
			}
				break;
			case CMD_SYSTEM_CONSOLE_PRINT:
			{
				final TigerSystemConsolePrint print = (TigerSystemConsolePrint) cmd;
				
				notifySystemConsolePrint(print);
				log.info("Console(" + getBotID().getNumberWithColorOffset() + "): " + print.getText());
			}
				break;
			case CMD_MOTOR_PID_LOG:
			{
				final TigerMotorPidLog motorLog = (TigerMotorPidLog) cmd;
				
				notifyNewMotorPidLog(motorLog);
			}
				break;
			case CMD_SYSTEM_STATUS_MOVEMENT:
			{
				final TigerSystemStatusMovement status = (TigerSystemStatusMovement) cmd;
				
				notifyNewSystemStatusMovement(status);
			}
				break;
			case CMD_SYSTEM_POWER_LOG:
			{
				final TigerSystemPowerLog powerLog = (TigerSystemPowerLog) cmd;
				
				notifyNewSystemPowerLog(powerLog);
			}
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
			case CMD_MOVEMENT_LIS3_LOG:
			{
			}
				break;
			case CMD_BOOTLOADER_RESPONSE:
			{
				final TigerBootloaderResponse resp = (TigerBootloaderResponse) cmd;
				
				bootloader.response(resp);
			}
				break;
			default:
				break;
		}
		
		rxStats.packets++;
		rxStats.payload += CommandFactory.getInstance().getLength(cmd, false);
	}
	
	
	@Override
	public void onBotOffline(final BotID id)
	{
		if (botId.equals(id))
		{
			changeNetworkState(ENetworkState.OFFLINE);
		}
	}
	
	
	/**
	 * @param o
	 */
	public void addObserver(final ITigerBotV2Observer o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
		
		super.addObserverIfNotPresent(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final ITigerBotV2Observer o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
		
		super.removeObserver(o);
	}
	
	
	private void changeNetworkState(final ENetworkState newState)
	{
		if (netState == newState)
		{
			return;
		}
		
		if (baseStation == null)
		{
			log.error("changeNetworkState called without baseStation set!");
			return;
		}
		
		if (newState == ENetworkState.CONNECTING)
		{
			log.error("TigerBotV2 can no longer be in CONNECTING state.");
			return;
		}
		
		if ((netState == ENetworkState.OFFLINE) && (newState == ENetworkState.ONLINE))
		{
			GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.ALL), 4000);
			
			txStats.reset();
			rxStats.reset();
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Connected bot: " + name + " (" + botId + ")");
			
			return;
		}
		
		if ((netState == ENetworkState.ONLINE) && (newState == ENetworkState.OFFLINE))
		{
			// TODO: what about bootloader? Need a cancel button.
			// bootloader.cancel();
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Disconnected bot: " + name + " (" + botId + ")");
			
			return;
		}
		
		log.error("Invalid state transition from " + netState + " to " + newState);
	}
	
	
	/**
	 * @return
	 */
	public TigerKickerStatusV3 getLastKickerStatus()
	{
		return lastKickerStatus;
	}
	
	
	/**
	 * @return
	 */
	public Statistics getRxStats()
	{
		return new Statistics(rxStats);
	}
	
	
	/**
	 * @return
	 */
	public Statistics getTxStats()
	{
		return new Statistics(txStats);
	}
	
	
	/**
	 * @param params
	 */
	public void setDribblerPid(final PIDParameters params)
	{
		TigerCtrlSetPIDParams cmd = new TigerCtrlSetPIDParams(PIDParamType.DRIBBLER, params);
		execute(cmd);
	}
	
	
	/**
	 * @param type
	 */
	public void setControllerType(final EControllerType type)
	{
		if (type == controllerType)
		{
			return;
		}
		
		controllerType = type;
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.ALL), 10);
	}
	
	
	/**
	 * @param type
	 */
	public void setCtrlMoveType(final ECtrlMoveType type)
	{
		ctrlmoveType = type;
	}
	
	
	/**
	 * @param enable
	 */
	public void setDribblerLogging(final boolean enable)
	{
		setLogs.setMotor(4, enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * @return
	 */
	public boolean getDribblerLogging()
	{
		return setLogs.getMotor(4);
	}
	
	
	/**
	 * @return
	 */
	public EControllerType getControllerType()
	{
		return controllerType;
	}
	
	
	/**
	 * @return
	 */
	public ECtrlMoveType getCtrlMoveType()
	{
		return ctrlmoveType;
	}
	
	
	/**
	 * @param params
	 */
	public void setPIDParamsPos(final PIDParametersXYW params)
	{
		controllerParams.setPos(params);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.PID_POS), 10);
		
		notifyControllerParamsChanged(controllerParams);
	}
	
	
	/**
	 * @param params
	 */
	public void setPIDParamsVel(final PIDParametersXYW params)
	{
		controllerParams.setVel(params);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.PID_VEL), 10);
		
		notifyControllerParamsChanged(controllerParams);
	}
	
	
	/**
	 * @param params
	 */
	public void setPIDParamsSpline(final PIDParametersXYW params)
	{
		controllerParams.setSpline(params);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.PID_SPLINE), 10);
		
		notifyControllerParamsChanged(controllerParams);
	}
	
	
	/**
	 * @param params
	 */
	public void setPIDParamsMotor(final PIDParameters params)
	{
		controllerParams.setMotor(params);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.PID_MOTOR), 10);
		
		notifyControllerParamsChanged(controllerParams);
	}
	
	
	/**
	 * @param ctrl
	 * @param sensor
	 */
	public void setControllerAndFusionParams(final ControllerParameters ctrl, final SensorFusionParameters sensor)
	{
		controllerParams = ctrl;
		sensorFusionParams = sensor;
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.ALL), 10);
		
		notifySensorFusionParamsChanged(sensor);
		notifyControllerParamsChanged(ctrl);
	}
	
	
	/**
	 * @param structure
	 */
	public void setStructure(final Structure structure)
	{
		this.structure = structure;
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.STRUCTURE), 10);
		
		notifyStructureChanged(structure);
	}
	
	
	/**
	 * @param unc
	 */
	public void setStateUncertainties(final StateUncertainties unc)
	{
		sensorFusionParams.setEx(unc);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.EX), 10);
	}
	
	
	/**
	 * @param unc
	 */
	public void setSensorUncertainties(final SensorUncertainties unc)
	{
		sensorFusionParams.setEz(unc);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.EZ), 10);
	}
	
	
	/**
	 * @param enable
	 * @param extended
	 */
	public void setLogMovement(final boolean enable, final boolean extended)
	{
		setLogs.setMovement(enable);
		setLogs.setExtMovement(extended);
		
		execute(setLogs);
	}
	
	
	/**
	 * @return
	 */
	public boolean getLogMovement()
	{
		return setLogs.getMovement();
	}
	
	
	/**
	 * @return
	 */
	public boolean getLogExtMovement()
	{
		return setLogs.getExtMovement();
	}
	
	
	/**
	 * @param enable
	 */
	public void setLogKicker(final boolean enable)
	{
		setLogs.setKicker(enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * @return
	 */
	public boolean getLogKicker()
	{
		return setLogs.getKicker();
	}
	
	
	/**
	 * @param enable
	 */
	public void setLogPower(final boolean enable)
	{
		setLogs.setPower(enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * @return
	 */
	public boolean getLogPower()
	{
		return setLogs.getPower();
	}
	
	
	/**
	 * @param enable
	 */
	public void setLogAccel(final boolean enable)
	{
		setLogs.setAccel(enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * @return
	 */
	public boolean getLogAccel()
	{
		return setLogs.getAccel();
	}
	
	
	/**
	 * @param enable
	 */
	public void setLogIr(final boolean enable)
	{
		setLogs.setIr(enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * @return
	 */
	public boolean getLogIr()
	{
		return setLogs.getIr();
	}
	
	
	/**
	 * @return
	 */
	public TigerSystemSetLogs getLogs()
	{
		return setLogs;
	}
	
	
	@Override
	public float getBatteryLevel()
	{
		return powerLog.getBatLevel();
	}
	
	
	/**
	 * @return
	 */
	public TigerSystemPowerLog getPowerLog()
	{
		return powerLog;
	}
	
	
	/**
	 * Start sending pings to bot.
	 * 
	 * @param numPings
	 * @param payloadSize
	 */
	public void startPing(final int numPings, final int payloadSize)
	{
		stopPing();
		
		pingThread = new PingThread(payloadSize);
		pingService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Ping Executor"));
		pingService.scheduleAtFixedRate(pingThread, 0, 1000000000 / numPings, TimeUnit.NANOSECONDS);
	}
	
	
	/**
	 * Stop sending pings.
	 */
	public void stopPing()
	{
		if (pingService == null)
		{
			return;
		}
		
		pingService.shutdownNow();
		pingService = null;
		pingThread = null;
	}
	
	
	private void notifyNewKickerStatusV3(final TigerKickerStatusV3 status)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onNewKickerStatusV3(status);
			}
		}
	}
	
	
	private void notifyNewMotorPidLog(final TigerMotorPidLog log)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onNewMotorPidLog(log);
			}
		}
	}
	
	
	private void notifyNewSystemStatusMovement(final TigerSystemStatusMovement status)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onNewSystemStatusMovement(status);
			}
		}
	}
	
	
	private void notifyNewSystemPowerLog(final TigerSystemPowerLog log)
	{
		powerLog = log;
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onNewSystemPowerLog(log);
			}
		}
	}
	
	
	private void notifyLogsChanged(final TigerSystemSetLogs logs)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onLogsChanged(logs);
			}
		}
	}
	
	
	private void notifyNewSystemStatusV2(final TigerSystemStatusV2 status)
	{
		systemStatus = status;
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onNewSystemStatusV2(status);
			}
		}
	}
	
	
	private void notifyNewSystemStatusExt(final TigerSystemStatusExt status)
	{
		systemStatus = status;
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onNewSystemStatusExt(status);
			}
		}
	}
	
	
	private void notifySensorFusionParamsChanged(final SensorFusionParameters params)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onSensorFusionParamsChanged(params);
			}
		}
	}
	
	
	private void notifyControllerParamsChanged(final ControllerParameters params)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onControllerParamsChanged(params);
			}
		}
	}
	
	
	private void notifyStructureChanged(final Structure structure)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onStructureChanged(structure);
			}
		}
	}
	
	
	private void notifySystemConsolePrint(final TigerSystemConsolePrint print)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onSystemConsolePrint(print);
			}
		}
	}
	
	
	/**
	 * @param type
	 */
	public void notifyControllerTypeChanged(final EControllerType type)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onControllerTypeChanged(type);
			}
		}
	}
	
	
	/**
	 * @param type
	 */
	public void notifyCtrlMoveTypeChanged(final ECtrlMoveType type)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onCtrlMoveTypeChanged(type);
			}
		}
	}
	
	
	private void notifyNewPingStats(final PingStats stats)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onNewPingStats(stats);
			}
		}
	}
	
	
	@Override
	public void onWatchdogTimeout()
	{
		if (netState == ENetworkState.ONLINE)
		{
			changeNetworkState(ENetworkState.OFFLINE);
		}
	}
	
	
	/**
	 * @return
	 */
	@Override
	public ENetworkState getNetworkState()
	{
		return netState;
	}
	
	
	@Override
	public float getKickerLevel()
	{
		return systemStatus.getKickerLevel();
	}
	
	
	@Override
	public void onIncommingBaseStationCommand(final ACommand command)
	{
		if (command.getType() == ECommand.CMD_BASE_STATS)
		{
			BaseStationStats stats = (BaseStationStats) command;
			
			WifiStats wifiStats = stats.getWifiStats(botId);
			if (wifiStats == null)
			{
				lastWifiStats = new WifiStats();
			} else
			{
				lastWifiStats = wifiStats;
			}
		}
	}
	
	
	/**
	 * @return
	 */
	public boolean isBarrierInterrupted()
	{
		return systemStatus.isBarrierInterrupted();
	}
	
	
	/**
	 * @return
	 */
	public TigerSystemStatusV2 getSystemStatusV2()
	{
		return systemStatus;
	}
	
	
	/**
	 * @return
	 */
	public SensorFusionParameters getSensorFusionParams()
	{
		return sensorFusionParams;
	}
	
	
	/**
	 * @return
	 */
	public ControllerParameters getControllerParams()
	{
		return controllerParams;
	}
	
	
	/**
	 * @return
	 */
	public Structure getStructure()
	{
		return structure;
	}
	
	
	@Override
	public HierarchicalConfiguration getConfiguration()
	{
		final CombinedConfiguration config = new CombinedConfiguration();
		config.addConfiguration(super.getConfiguration());
		
		config.addProperty("bot.controllerType", controllerType.getId());
		config.addProperty("bot.ctrlmoveType", ctrlmoveType);
		
		config.addProperty("bot.logs.movement", setLogs.getMovement());
		config.addProperty("bot.logs.extMovement", setLogs.getExtMovement());
		config.addProperty("bot.logs.kicker", setLogs.getKicker());
		config.addProperty("bot.logs.power", setLogs.getPower());
		config.addProperty("bot.logs.accel", setLogs.getAccel());
		config.addProperty("bot.logs.ir", setLogs.getIr());
		
		config.addConfiguration(sensorFusionParams.getConfiguration(), "sensorFusion", "bot.sensorFusion");
		config.addConfiguration(controllerParams.getConfiguration(), "controller", "bot.controller");
		config.addConfiguration(structure.getConfiguration(), "structure", "bot.structure");
		
		return config;
	}
	
	
	/**
	 * @return the baseStation
	 */
	public IBaseStation getBaseStation()
	{
		return baseStation;
	}
	
	
	/**
	 * @param baseStation the baseStation to set
	 */
	public void setBaseStation(final IBaseStation baseStation)
	{
		this.baseStation = baseStation;
		
		if ((baseStation == null) && (this.baseStation != null))
		{
			this.baseStation.removeObserver(this);
		}
		
		if (baseStation != null)
		{
			baseStation.addObserver(this);
		}
	}
	
	
	/**
	 * @return
	 */
	public Bootloader getBootloader()
	{
		return bootloader;
	}
	
	
	/**
	 * @return the lastWifiStats
	 */
	public WifiStats getLastWifiStats()
	{
		return lastWifiStats;
	}
	
	private static enum EParametersToSet
	{
		ALL,
		PID_POS,
		PID_VEL,
		PID_SPLINE,
		PID_MOTOR,
		EX,
		EZ,
		DRIBBLER,
		STRUCTURE
	}
	
	private class ParameterSetter extends TimerTask
	{
		private EParametersToSet	toSet	= EParametersToSet.ALL;
		
		
		/**
		 * @param set
		 */
		public ParameterSetter(final EParametersToSet set)
		{
			toSet = set;
		}
		
		
		@Override
		public void run()
		{
			log.debug("Parameter Setter on Bot " + botId + " executing: " + toSet.name());
			
			if (toSet == EParametersToSet.ALL)
			{
				execute(setLogs);
				sleep(10);
			}
			
			if (toSet == EParametersToSet.ALL)
			{
				execute(new TigerCtrlSetControllerType(controllerType));
				sleep(10);
			}
			
			if ((toSet == EParametersToSet.ALL) || (toSet == EParametersToSet.PID_POS))
			{
				execute(new TigerCtrlSetPIDParams(PIDParamType.POS_X, controllerParams.getPos().getX()));
				sleep(10);
				execute(new TigerCtrlSetPIDParams(PIDParamType.POS_Y, controllerParams.getPos().getY()));
				execute(new TigerCtrlSetPIDParams(PIDParamType.POS_W, controllerParams.getPos().getW()));
			}
			
			if ((toSet == EParametersToSet.ALL) || (toSet == EParametersToSet.PID_VEL))
			{
				execute(new TigerCtrlSetPIDParams(PIDParamType.VEL_X, controllerParams.getVel().getX()));
				sleep(10);
				execute(new TigerCtrlSetPIDParams(PIDParamType.VEL_Y, controllerParams.getVel().getY()));
				execute(new TigerCtrlSetPIDParams(PIDParamType.VEL_W, controllerParams.getVel().getW()));
			}
			
			if ((toSet == EParametersToSet.ALL) || (toSet == EParametersToSet.PID_SPLINE))
			{
				execute(new TigerCtrlSetPIDParams(PIDParamType.SPLINE_X, controllerParams.getSpline().getX()));
				sleep(10);
				execute(new TigerCtrlSetPIDParams(PIDParamType.SPLINE_Y, controllerParams.getSpline().getY()));
				execute(new TigerCtrlSetPIDParams(PIDParamType.SPLINE_W, controllerParams.getSpline().getW()));
			}
			
			if ((toSet == EParametersToSet.ALL) || (toSet == EParametersToSet.PID_MOTOR))
			{
				execute(new TigerCtrlSetPIDParams(PIDParamType.MOTOR, controllerParams.getMotor()));
				sleep(10);
			}
			
			if ((toSet == EParametersToSet.ALL) || (toSet == EParametersToSet.EX))
			{
				execute(new TigerCtrlSetFilterParams(ParamType.EX_POS, sensorFusionParams.getEx().getPos()));
				sleep(10);
				execute(new TigerCtrlSetFilterParams(ParamType.EX_VEL, sensorFusionParams.getEx().getVel()));
				execute(new TigerCtrlSetFilterParams(ParamType.EX_ACC, sensorFusionParams.getEx().getAcc()));
			}
			
			if ((toSet == EParametersToSet.ALL) || (toSet == EParametersToSet.EZ))
			{
				execute(new TigerCtrlSetFilterParams(ParamType.EZ_VISION, sensorFusionParams.getEz().getVision()));
				sleep(10);
				execute(new TigerCtrlSetFilterParams(ParamType.EZ_ENCODER, sensorFusionParams.getEz().getEncoder()));
				TigerCtrlSetFilterParams accGyro = new TigerCtrlSetFilterParams(ParamType.EZ_ACC_GYRO, new Vector3(
						sensorFusionParams.getEz().getAccelerometer().x(), sensorFusionParams.getEz().getAccelerometer().y(),
						sensorFusionParams.getEz().getGyroscope()));
				execute(accGyro);
				execute(new TigerCtrlSetFilterParams(ParamType.EZ_MOTOR, sensorFusionParams.getEz().getMotor()));
			}
			
			if ((toSet == EParametersToSet.ALL) || (toSet == EParametersToSet.DRIBBLER))
			{
				execute(new TigerCtrlSetPIDParams(PIDParamType.DRIBBLER, controllerParams.getDribbler()));
				sleep(10);
			}
			
			if ((toSet == EParametersToSet.ALL) || (toSet == EParametersToSet.STRUCTURE))
			{
				TigerSystemConsoleCommand cmd = new TigerSystemConsoleCommand();
				cmd.setTarget(ConsoleCommandTarget.MAIN);
				
				cmd.setText(String.format(Locale.ENGLISH, "structure %f %f %f %f %f", structure.getFrontAngle(),
						structure.getBackAngle(),
						structure.getBotRadius(), structure.getWheelRadius(), structure.getMass()));
				execute(cmd);
			}
		}
		
		
		private void sleep(final int ms)
		{
			try
			{
				Thread.sleep(ms);
			} catch (final InterruptedException err)
			{
			}
		}
	}
	
	
	@Override
	public void onNetworkStateChanged(final ENetworkState netState)
	{
	}
	
	
	@Override
	public void onNewBaseStationStats(final BaseStationStats stats)
	{
	}
	
	
	@Override
	public void onNewPingDelay(final float delay)
	{
	}
	
	
	/** Ping statistics */
	public static class PingStats
	{
		/** */
		public PingStats()
		{
			avgDelay = 0;
			minDelay = Float.MAX_VALUE;
			maxDelay = 0;
			lostPings = 0;
		}
		
		/** Average Delay */
		public float	avgDelay;
		/** Minimum Delay */
		public float	minDelay;
		/** Maximum Delay */
		public float	maxDelay;
		/** Lost pings per second */
		public int		lostPings;
	}
	
	private static class PingDatum
	{
		public PingDatum(final int id)
		{
			this.id = id;
			startTime = System.nanoTime();
			endTime = 0;
			delay = 0;
			lost = false;
		}
		
		public long		startTime;
		public long		endTime;
		public float	delay;
		public int		id;
		public boolean	lost;
	}
	
	private class PingThread extends Thread
	{
		private static final long		UPDATE_RATE		= 100000000;
		
		private int							id					= 0;
		
		private long						lastStatTime	= 0;
		private int							payloadSize		= 0;
		
		private final List<PingDatum>	pings				= new LinkedList<PingDatum>();
		private final List<PingDatum>	completed		= new LinkedList<PingDatum>();
		
		
		public PingThread(final int payloadSize)
		{
			this.payloadSize = payloadSize;
		}
		
		
		@Override
		public void run()
		{
			synchronized (pings)
			{
				pings.add(new PingDatum(id));
			}
			
			execute(new TigerSystemPing(id, payloadSize));
			id++;
			
			synchronized (pings)
			{
				while (!pings.isEmpty())
				{
					PingDatum dat = pings.get(0);
					
					if ((System.nanoTime() - dat.startTime) < 1000000000)
					{
						break;
					}
					
					dat.endTime = System.nanoTime();
					dat.delay = (dat.endTime - dat.startTime) / 1e9f;
					dat.lost = true;
					
					pings.remove(0);
					
					completed.add(dat);
				}
			}
			
			processCompleted();
		}
		
		
		/**
		 * @param id
		 */
		public void pongArrived(final int id)
		{
			PingDatum dat = null;
			
			synchronized (pings)
			{
				while (!pings.isEmpty())
				{
					dat = pings.remove(0);
					
					dat.endTime = System.nanoTime();
					dat.delay = (dat.endTime - dat.startTime) / 1e9f;
					
					completed.add(dat);
					
					if (dat.id == id)
					{
						break;
					}
					
					dat.lost = true;
				}
			}
			
			processCompleted();
		}
		
		
		private synchronized void processCompleted()
		{
			PingDatum dat;
			
			while (!completed.isEmpty())
			{
				dat = completed.get(0);
				
				if ((System.nanoTime() - dat.endTime) > 1e9)
				{
					completed.remove(0);
				} else
				{
					break;
				}
			}
			
			if ((System.nanoTime() - lastStatTime) > UPDATE_RATE)
			{
				lastStatTime = System.nanoTime();
				
				PingStats stats = new PingStats();
				
				for (PingDatum d : completed)
				{
					if (d.delay < stats.minDelay)
					{
						stats.minDelay = d.delay;
					}
					
					if (d.delay > stats.maxDelay)
					{
						stats.maxDelay = d.delay;
					}
					
					stats.avgDelay += d.delay;
					
					if (d.lost)
					{
						stats.lostPings++;
					}
				}
				
				stats.avgDelay /= completed.size();
				
				if (completed.isEmpty())
				{
					stats.minDelay = 0;
					stats.avgDelay = 0;
					stats.maxDelay = 0;
					stats.lostPings = 0;
				}
				
				notifyNewPingStats(stats);
			}
		}
	}
	
	
	@Override
	public void newSpline(final SplinePair3D spline)
	{
		notifyNewSplineData(spline);
	}
	
	
	@Override
	public float getBatteryLevelMax()
	{
		return BAT_MAX;
	}
	
	
	@Override
	public float getBatteryLevelMin()
	{
		return BAT_MIN;
	}
	
	
	@Override
	public float getKickerLevelMax()
	{
		return 180f;
	}
}
