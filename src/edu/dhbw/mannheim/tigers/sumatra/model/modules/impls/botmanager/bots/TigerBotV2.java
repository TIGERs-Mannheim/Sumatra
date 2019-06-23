/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.ControllerParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.PIDParametersXYW;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorFusionParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.SensorUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.botmanager.StateUncertainties;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStationObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation.BaseStationStats;
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.ControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetFilterParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetFilterParams.ParamType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetPIDParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetPIDParams.PIDParamType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerKickerStatusV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.util.IWatchdogObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.Watchdog;


/**
 * This is the Tiger bot v2013.
 * 
 * @author AndreR
 */
public class TigerBotV2 extends ABot implements IBaseStationObserver, IWatchdogObserver
{
	// Logger
	private static final Logger					log							= Logger.getLogger(TigerBotV2.class.getName());
	
	private static final int						SPLINE_SENDING_DELAY		= 200;
	private static final int						TIMEOUT						= 2000;
	private final Watchdog							watchdog						= new Watchdog(TIMEOUT);
	
	private IBaseStation								baseStation					= null;
	
	private final List<ITigerBotV2Observer>	observers					= new ArrayList<ITigerBotV2Observer>();
	
	private ENetworkState							netState						= ENetworkState.OFFLINE;
	private final Statistics						txStats						= new Statistics();
	private final Statistics						rxStats						= new Statistics();
	
	private Vector2									lastDirection				= new Vector2();
	private float										lastAngularVelocity		= 0.0f;
	private float										lastCompensatedVelocity	= 0.0f;
	
	private final Bootloader						bootloader;
	private Connector									connectTimer				= null;
	
	// Kicker
	private TigerKickerStatusV3					lastKickerStatus			= null;
	
	// Motor
	private TigerSystemSetLogs						setLogs						= new TigerSystemSetLogs();
	
	private SensorFusionParameters				sensorFusionParams		= new SensorFusionParameters();
	private ControllerParameters					controllerParams			= new ControllerParameters();
	private ControllerType							controllerType				= ControllerType.NONE;
	
	private boolean									oofCheck						= false;
	
	private TigerSystemStatusV2					systemStatus				= new TigerSystemStatusV2();
	private TigerSystemPowerLog					powerLog						= new TigerSystemPowerLog();
	
	private long										timeLastSpline1Cmd		= 0;
	private long										timeLastSpline2Cmd		= 0;
	
	private TimerTask									spline1DTimer				= null;
	private TimerTask									spline2DTimer				= null;
	
	
	/**
	 * 
	 * @param botConfig
	 * @throws BotInitException
	 */
	public TigerBotV2(SubnodeConfiguration botConfig) throws BotInitException
	{
		super(botConfig);
		
		try
		{
			oofCheck = botConfig.getBoolean("oofcheck", false);
			
			controllerType = ControllerType.getControllerTypeConstant(botConfig.getInt("controllerType", 0));
			
			setLogs.setMovement(botConfig.getBoolean("logs.movement", true));
			setLogs.setKicker(botConfig.getBoolean("logs.kicker", true));
			setLogs.setAccel(botConfig.getBoolean("logs.accel", false));
			setLogs.setIr(botConfig.getBoolean("logs.ir", false));
			
			sensorFusionParams = new SensorFusionParameters(botConfig.configurationAt("sensorFusion"));
			controllerParams = new ControllerParameters(botConfig.configurationAt("controller"));
		} catch (final NoSuchElementException nse)
		{
			throw new BotInitException(botConfig, nse);
		}
		
		bootloader = new Bootloader(this);
	}
	
	
	/**
	 * 
	 * @param id
	 */
	public TigerBotV2(BotID id)
	{
		super(EBotType.TIGER_V2, id);
		
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
	public void execute(ACommand cmd)
	{
		if (baseStation == null)
		{
			return;
		}
		
		if (netState != ENetworkState.ONLINE)
		{
			return;
		}
		
		switch (cmd.getCommand())
		{
			case CommandConstants.CMD_MOTOR_MOVE_V2:
			{
				final TigerMotorMoveV2 move = (TigerMotorMoveV2) cmd;
				
				move.setUnusedComponents(lastDirection, lastAngularVelocity, lastCompensatedVelocity);
				
				lastDirection = move.getXY();
				lastAngularVelocity = move.getW();
				lastCompensatedVelocity = move.getV();
				
				baseStation.enqueueCommand(getBotID(), move);
			}
				break;
			case CommandConstants.CMD_KICKER_KICKV2:
			{
				final TigerKickerKickV2 kick = (TigerKickerKickV2) cmd;
				
				baseStation.enqueueCommand(getBotID(), kick);
			}
				break;
			case CommandConstants.CMD_MOTOR_DRIBBLE:
			{
				final TigerDribble dribble = (TigerDribble) cmd;
				
				baseStation.enqueueCommand(getBotID(), dribble);
			}
				break;
			case CommandConstants.CMD_SYSTEM_SET_LOGS:
			{
				final TigerSystemSetLogs logs = (TigerSystemSetLogs) cmd;
				
				setLogs = logs;
				
				baseStation.enqueueCommand(getBotID(), logs);
				
				notifyLogsChanged(logs);
			}
				break;
			case CommandConstants.CMD_CTRL_SET_FILTER_PARAMS:
			{
				final TigerCtrlSetFilterParams params = (TigerCtrlSetFilterParams) cmd;
				
				sensorFusionParams.updateWithCommand(params);
				
				baseStation.enqueueCommand(getBotID(), params);
				
				notifySensorFusionParamsChanged(sensorFusionParams);
			}
				break;
			case CommandConstants.CMD_CTRL_SET_PID_PARAMS:
			{
				final TigerCtrlSetPIDParams params = (TigerCtrlSetPIDParams) cmd;
				
				controllerParams.updateWithCommand(params);
				
				baseStation.enqueueCommand(getBotID(), params);
				
				notifyControllerParamsChanged(controllerParams);
			}
				break;
			case CommandConstants.CMD_CTRL_SET_CONTROLLER_TYPE:
			{
				final TigerCtrlSetControllerType type = (TigerCtrlSetControllerType) cmd;
				
				log.debug("Setting controller to: " + type.getType().name());
				
				controllerType = type.getType();
				
				baseStation.enqueueCommand(getBotID(), type);
				
				notifyControllerTypeChanged(controllerType);
			}
				break;
			case CommandConstants.CMD_CTRL_SPLINE_1D:
				// log.debug("Incoming spline");
				long diff = System.nanoTime() - timeLastSpline1Cmd;
				if (spline1DTimer != null)
				{
					spline1DTimer.cancel();
				}
				if ((diff) < TimeUnit.MILLISECONDS.toNanos(SPLINE_SENDING_DELAY))
				{
					
					spline1DTimer = new SplineSenderTask(cmd);
					GeneralPurposeTimer.getInstance().schedule(spline1DTimer,
							SPLINE_SENDING_DELAY - TimeUnit.NANOSECONDS.toMillis(diff));
					return;
				}
				timeLastSpline1Cmd = System.nanoTime();
				baseStation.enqueueCommand(getBotID(), cmd);
				// log.debug("send normal");
				break;
			case CommandConstants.CMD_CTRL_SPLINE_2D:
				long diff2 = System.nanoTime() - timeLastSpline2Cmd;
				if (spline2DTimer != null)
				{
					spline2DTimer.cancel();
				}
				if ((diff2) < TimeUnit.MILLISECONDS.toNanos(SPLINE_SENDING_DELAY))
				{
					
					spline2DTimer = new SplineSenderTask(cmd);
					GeneralPurposeTimer.getInstance().schedule(spline2DTimer,
							SPLINE_SENDING_DELAY - TimeUnit.NANOSECONDS.toMillis(diff2));
					return;
				}
				timeLastSpline2Cmd = System.nanoTime();
				baseStation.enqueueCommand(getBotID(), cmd);
				break;
			default:
			{
				baseStation.enqueueCommand(getBotID(), cmd);
			}
				break;
		}
		
		txStats.packets++;
		txStats.payload += cmd.getDataLength() + CommandConstants.HEADER_SIZE;
	}
	
	
	@Override
	public void start()
	{
		if (!active)
		{
			return;
		}
		
		setActive(active);
		setOofCheck(oofCheck);
		
		if (netState == ENetworkState.OFFLINE)
		{
			changeNetworkState(ENetworkState.CONNECTING);
		}
	}
	
	
	@Override
	public void stop()
	{
		changeNetworkState(ENetworkState.OFFLINE);
	}
	
	
	@Override
	public void onIncommingBotCommand(BotID id, ACommand cmd)
	{
		if (!id.equals(getBotID()))
		{
			return;
		}
		
		if (watchdog.isActive())
		{
			watchdog.reset();
		} else
		{
			changeNetworkState(ENetworkState.ONLINE);
		}
		
		switch (cmd.getCommand())
		{
			case CommandConstants.CMD_SYSTEM_STATUS_V2:
			{
				final TigerSystemStatusV2 status = (TigerSystemStatusV2) cmd;
				
				notifyNewSystemStatusV2(status);
			}
				break;
			case CommandConstants.CMD_KICKER_STATUSV3:
			{
				final TigerKickerStatusV3 stats = (TigerKickerStatusV3) cmd;
				
				lastKickerStatus = stats;
				
				notifyNewKickerStatusV3(stats);
			}
				break;
			case CommandConstants.CMD_SYSTEM_CONSOLE_PRINT:
			{
				final TigerSystemConsolePrint print = (TigerSystemConsolePrint) cmd;
				
				notifySystemConsolePrint(print);
			}
				break;
			case CommandConstants.CMD_MOTOR_PID_LOG:
			{
				final TigerMotorPidLog motorLog = (TigerMotorPidLog) cmd;
				
				notifyNewMotorPidLog(motorLog);
			}
				break;
			case CommandConstants.CMD_SYSTEM_STATUS_MOVEMENT:
			{
				final TigerSystemStatusMovement status = (TigerSystemStatusMovement) cmd;
				
				notifyNewSystemStatusMovement(status);
			}
				break;
			case CommandConstants.CMD_SYSTEM_POWER_LOG:
			{
				final TigerSystemPowerLog powerLog = (TigerSystemPowerLog) cmd;
				
				notifyNewSystemPowerLog(powerLog);
			}
				break;
			case CommandConstants.CMD_SYSTEM_PONG:
			{
				final TigerSystemPong pong = (TigerSystemPong) cmd;
				
				notifyNewSystemPong(pong);
			}
				break;
			case CommandConstants.CMD_MOVEMENT_LIS3_LOG:
			{
			}
				break;
			case CommandConstants.CMD_BOOTLOADER_RESPONSE:
			{
				final TigerBootloaderResponse resp = (TigerBootloaderResponse) cmd;
				
				bootloader.response(resp);
			}
				break;
		}
		
		rxStats.packets++;
		rxStats.payload += cmd.getDataLength() + CommandConstants.HEADER_SIZE;
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void addObserver(ITigerBotV2Observer o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
		
		super.addObserverIfNotPresent(o);
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void removeObserver(ITigerBotV2Observer o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
		
		super.removeObserver(o);
	}
	
	
	private void changeNetworkState(ENetworkState newState)
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
		
		if ((netState == ENetworkState.OFFLINE) && (newState == ENetworkState.CONNECTING))
		{
			// start transceiver
			baseStation.addObserver(this);
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			connectTimer = new Connector();
			GeneralPurposeTimer.getInstance().schedule(connectTimer, 0, 1000);
			
			log.debug("Bot connecting: " + getName() + " (" + getBotID() + ")");
			
			return;
		}
		
		if ((netState == ENetworkState.CONNECTING) && (newState == ENetworkState.OFFLINE))
		{
			bootloader.cancel();
			if (connectTimer != null)
			{
				connectTimer.cancel();
			}
			
			// stop transceiver
			baseStation.removeObserver(this);
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Disconnected bot: " + name + " (" + botId + ")");
			
			return;
		}
		
		if ((netState == ENetworkState.CONNECTING) && (newState == ENetworkState.ONLINE))
		{
			if (connectTimer != null)
			{
				connectTimer.cancel();
			}
			
			GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.ALL), 100);
			
			txStats.reset();
			rxStats.reset();
			
			// start watchdog
			watchdog.start(this);
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Connected bot: " + name + " (" + botId + ")");
			
			return;
		}
		
		if ((netState == ENetworkState.ONLINE) && (newState == ENetworkState.CONNECTING))
		{
			// stop watchdog
			watchdog.stop();
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			connectTimer = new Connector();
			GeneralPurposeTimer.getInstance().schedule(connectTimer, 0, 1000);
			
			log.debug("Bot timed out: " + getName() + " (" + getBotID() + ")");
			
			return;
		}
		
		if ((netState == ENetworkState.ONLINE) && (newState == ENetworkState.OFFLINE))
		{
			bootloader.cancel();
			
			// stop watchdog
			watchdog.stop();
			
			// terminate transceiver
			baseStation.removeObserver(this);
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Disconnected bot: " + name + " (" + botId + ")");
			
			return;
		}
		
		log.error("Invalid state transition from " + netState + " to " + newState);
	}
	
	
	/**
	 * 
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
	 * 
	 * @param params
	 */
	public void setDribblerPid(PIDParameters params)
	{
		TigerCtrlSetPIDParams cmd = new TigerCtrlSetPIDParams(PIDParamType.DRIBBLER, params);
		execute(cmd);
	}
	
	
	/**
	 * 
	 * @param type
	 */
	public void setControllerType(ControllerType type)
	{
		if (type == controllerType)
		{
			return;
		}
		
		controllerType = type;
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.ALL), 100);
	}
	
	
	/**
	 * 
	 * @param enable
	 */
	public void setDribblerLogging(boolean enable)
	{
		setLogs.setMotor(4, enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean getDribblerLogging()
	{
		return setLogs.getMotor(4);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ControllerType getControllerType()
	{
		return controllerType;
	}
	
	
	/**
	 * 
	 * @param params
	 */
	public void setPIDParamsPos(PIDParametersXYW params)
	{
		controllerParams.setPos(params);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.PID_POS), 10);
		
		notifyControllerParamsChanged(controllerParams);
	}
	
	
	/**
	 * 
	 * @param params
	 */
	public void setPIDParamsVel(PIDParametersXYW params)
	{
		controllerParams.setVel(params);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.PID_VEL), 10);
		
		notifyControllerParamsChanged(controllerParams);
	}
	
	
	/**
	 * 
	 * @param params
	 */
	public void setPIDParamsAcc(PIDParametersXYW params)
	{
		controllerParams.setAcc(params);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.PID_ACC), 10);
		
		notifyControllerParamsChanged(controllerParams);
	}
	
	
	/**
	 * 
	 * @param ctrl
	 * @param sensor
	 */
	public void setControllerAndFusionParams(ControllerParameters ctrl, SensorFusionParameters sensor)
	{
		controllerParams = ctrl;
		sensorFusionParams = sensor;
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.ALL), 10);
		
		notifySensorFusionParamsChanged(sensor);
		notifyControllerParamsChanged(ctrl);
	}
	
	
	/**
	 * 
	 * @param unc
	 */
	public void setStateUncertainties(StateUncertainties unc)
	{
		sensorFusionParams.setEx(unc);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.EX), 10);
	}
	
	
	/**
	 * 
	 * @param unc
	 */
	public void setSensorUncertainties(SensorUncertainties unc)
	{
		sensorFusionParams.setEz(unc);
		
		GeneralPurposeTimer.getInstance().schedule(new ParameterSetter(EParametersToSet.EZ), 10);
	}
	
	
	/**
	 * 
	 * @param enable
	 */
	public void setLogMovement(boolean enable)
	{
		setLogs.setMovement(enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean getLogMovement()
	{
		return setLogs.getMovement();
	}
	
	
	/**
	 * 
	 * @param enable
	 */
	public void setLogKicker(boolean enable)
	{
		setLogs.setKicker(enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean getLogKicker()
	{
		return setLogs.getKicker();
	}
	
	
	/**
	 * 
	 * @param enable
	 */
	public void setLogAccel(boolean enable)
	{
		setLogs.setAccel(enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean getLogAccel()
	{
		return setLogs.getAccel();
	}
	
	
	/**
	 * 
	 * @param enable
	 */
	public void setLogIr(boolean enable)
	{
		setLogs.setIr(enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean getLogIr()
	{
		return setLogs.getIr();
	}
	
	
	/**
	 * 
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
	
	
	private void notifyNewKickerStatusV3(TigerKickerStatusV3 status)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onNewKickerStatusV3(status);
			}
		}
	}
	
	
	private void notifyNewMotorPidLog(TigerMotorPidLog log)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onNewMotorPidLog(log);
			}
		}
	}
	
	
	private void notifyNewSystemStatusMovement(TigerSystemStatusMovement status)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onNewSystemStatusMovement(status);
			}
		}
	}
	
	
	private void notifyNewSystemPowerLog(TigerSystemPowerLog log)
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
	
	
	private void notifyNewSystemPong(TigerSystemPong pong)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onNewSystemPong(pong);
			}
		}
	}
	
	
	private void notifyLogsChanged(TigerSystemSetLogs logs)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onLogsChanged(logs);
			}
		}
	}
	
	
	private void notifyNewSystemStatusV2(TigerSystemStatusV2 status)
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
	
	
	private void notifySensorFusionParamsChanged(SensorFusionParameters params)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onSensorFusionParamsChanged(params);
			}
		}
	}
	
	
	private void notifyControllerParamsChanged(ControllerParameters params)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onControllerParamsChanged(params);
			}
		}
	}
	
	
	private void notifySystemConsolePrint(TigerSystemConsolePrint print)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onSystemConsolePrint(print);
			}
		}
	}
	
	
	private void notifyControllerTypeChanged(ControllerType type)
	{
		synchronized (observers)
		{
			for (ITigerBotV2Observer observer : observers)
			{
				observer.onControllerTypeChanged(type);
			}
		}
	}
	
	
	@Override
	public void onWatchdogTimeout()
	{
		if (netState == ENetworkState.ONLINE)
		{
			changeNetworkState(ENetworkState.CONNECTING);
		}
	}
	
	
	/**
	 * 
	 * @param enable
	 */
	public void setOofCheck(boolean enable)
	{
		oofCheck = enable;
		
		notifyOofCheckChanged(enable);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean getOofCheck()
	{
		return oofCheck;
	}
	
	
	/**
	 * 
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
	public void onIncommingBaseStationCommand(ACommand command)
	{
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
	
	
	@Override
	public HierarchicalConfiguration getConfiguration()
	{
		final CombinedConfiguration config = new CombinedConfiguration();
		config.addConfiguration(super.getConfiguration());
		
		config.addProperty("bot.oofcheck", oofCheck);
		config.addProperty("bot.controllerType", controllerType.getId());
		
		config.addProperty("bot.logs.movement", setLogs.getMovement());
		config.addProperty("bot.logs.kicker", setLogs.getKicker());
		config.addProperty("bot.logs.accel", setLogs.getAccel());
		config.addProperty("bot.logs.ir", setLogs.getIr());
		
		config.addConfiguration(sensorFusionParams.getConfiguration(), "sensorFusion", "bot.sensorFusion");
		config.addConfiguration(controllerParams.getConfiguration(), "controller", "bot.controller");
		
		return config;
	}
	
	
	private void notifyOofCheckChanged(boolean enable)
	{
		synchronized (observers)
		{
			for (final ITigerBotV2Observer observer : observers)
			{
				observer.onOofCheckChanged(enable);
			}
		}
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
	public void setBaseStation(IBaseStation baseStation)
	{
		this.baseStation = baseStation;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Bootloader getBootloader()
	{
		return bootloader;
	}
	
	private class Connector extends TimerTask
	{
		@Override
		public void run()
		{
			TigerSystemPing pingCmd = new TigerSystemPing();
			baseStation.enqueueCommand(getBotID(), pingCmd);
		}
	}
	
	private static enum EParametersToSet
	{
		ALL,
		PID_POS,
		PID_VEL,
		PID_ACC,
		EX,
		EZ,
		DRIBBLER
	}
	
	private class ParameterSetter extends TimerTask
	{
		private EParametersToSet	toSet	= EParametersToSet.ALL;
		
		
		public ParameterSetter(EParametersToSet set)
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
			
			if ((toSet == EParametersToSet.ALL) || (toSet == EParametersToSet.PID_ACC))
			{
				execute(new TigerCtrlSetPIDParams(PIDParamType.ACC_X, controllerParams.getAcc().getX()));
				sleep(10);
				execute(new TigerCtrlSetPIDParams(PIDParamType.ACC_Y, controllerParams.getAcc().getY()));
				execute(new TigerCtrlSetPIDParams(PIDParamType.ACC_W, controllerParams.getAcc().getW()));
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
			TigerSystemConsoleCommand cmd = new TigerSystemConsoleCommand();
			cmd.setTarget(ConsoleCommandTarget.MAIN);
			cmd.setText("structure 30 45 0.072 0.02 4");
			execute(cmd);
		}
		
		
		private void sleep(int ms)
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
	public void onNetworkStateChanged(ENetworkState netState)
	{
	}
	
	
	@Override
	public void onNewBaseStationStats(BaseStationStats stats)
	{
	}
	
	
	@Override
	public void onNewPingDelay(float delay)
	{
	}
	
	private class SplineSenderTask extends TimerTask
	{
		
		private ACommand	cmd;
		
		
		public SplineSenderTask(ACommand cmd)
		{
			this.cmd = cmd;
		}
		
		
		@Override
		public void run()
		{
			timeLastSpline1Cmd = System.nanoTime();
			if (baseStation != null)
			{
				baseStation.enqueueCommand(getBotID(), cmd);
			}
		}
		
	}
}
