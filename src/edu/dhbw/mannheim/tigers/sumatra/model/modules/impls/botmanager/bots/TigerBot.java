/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.IMulticastDelegate;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ITransceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.UnicastTransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerIrLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams.MotorMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMovementLis3LogRaw;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;
import edu.dhbw.mannheim.tigers.sumatra.util.IWatchdogObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.Watchdog;


public class TigerBot extends ABot implements ITransceiverUDPObserver, IWatchdogObserver
{
	private static final int					TIMEOUT						= 2000;
	private ITransceiverUDP						transceiver					= new UnicastTransceiverUDP();
	private Watchdog								watchdog						= new Watchdog(TIMEOUT);
	
	private final List<ITigerBotObserver>	observers					= new ArrayList<ITigerBotObserver>();
	
	private final Logger							log							= Logger.getLogger(getClass());
	
	private IMulticastDelegate					mcastDelegate				= null;
	
	private ENetworkState						netState						= ENetworkState.OFFLINE;
	
	private Vector2								lastDirection				= new Vector2();
	private float									lastAngularVelocity		= 0.0f;
	private float									lastCompensatedVelocity	= 0.0f;
	
	// Identification
	private String									mac							= "02-00-00-00-00-00";
	private int										serverPort					= 0;
	private String									cpuId							= "000000000000000000000000";
	
	// Kicker
	private TigerKickerStatusV2				lastKickerStatus			= null;
	
	// Motor
	private TigerMotorSetParams				motorParams					= new TigerMotorSetParams();
	private TigerSystemSetLogs					setLogs						= new TigerSystemSetLogs();

	private boolean								useUpdateAll				= false;
	private boolean								oofCheck						= false;
	
	private Matrix									xEpsilon						= null;
	private Matrix									yEpsilon						= null;
	
	public TigerBot(SubnodeConfiguration botConfig) throws BotInitException
	{
		super(botConfig);
		
		try
		{
			this.mac = botConfig.getString("mac");
			this.serverPort = botConfig.getInt("serverport");
			this.cpuId = botConfig.getString("cpuid");
			this.useUpdateAll = botConfig.getBoolean("useUpdateAll", false);
			this.oofCheck = botConfig.getBoolean("oofcheck", false);
			
			motorParams.setMode(botConfig.getInt("motorMode", 2));
			
			setLogs.setMovement(botConfig.getBoolean("logs.movement", true));
			setLogs.setKicker(botConfig.getBoolean("logs.kicker", true));
			setLogs.setAccel(botConfig.getBoolean("logs.accel", false));
			setLogs.setIr(botConfig.getBoolean("logs.ir", false));
		} catch (NoSuchElementException nse)
		{
			throw new BotInitException(botConfig, nse);
		}
		
		List<?> motors = botConfig.configurationsAt("pid.motor");
		int i = 0;
		for (Iterator<?> it = motors.iterator(); it.hasNext();)
		{
			SubnodeConfiguration motor = (SubnodeConfiguration) it.next();
			
			motorParams.setPidParams(i, motor.getFloat("kp", 1.0f), motor.getFloat("ki", 0.5f),
					motor.getFloat("kd", 0.0f), motor.getInt("slewMax", 250));
			setLogs.setMotor(i, motor.getBoolean("log", false));
			
			i++;
		}
	}
	

	public TigerBot(int id)
	{
		super(EBotType.TIGER, id);
	}
	

	@Override
	public void execute(ACommand cmd)
	{
		switch (cmd.getCommand())
		{
			case CommandConstants.CMD_MOTOR_MOVE_V2:
			{
				TigerMotorMoveV2 move = (TigerMotorMoveV2) cmd;
				
				move.setUnusedComponents(lastDirection, lastAngularVelocity, lastCompensatedVelocity);
				
				lastDirection = move.getXY();
				lastAngularVelocity = move.getW();
				lastCompensatedVelocity = move.getV();
				
				if(xEpsilon != null && yEpsilon != null)
				{
					Matrix origCmd = new Matrix(3,1);
					origCmd.set(0, 0, move.getXY().x);
					origCmd.set(1, 0, move.getXY().y);
					origCmd.set(2, 0, move.getW());
					
					Matrix sendCmd = TigerBot.getMotorToCmdMatrix().minus(xEpsilon.times(move.getXY().x).plus(yEpsilon.times(move.getXY().y))).times(TigerBot.getCmdToMotorMatrix().times(origCmd));

					move.setX((float)sendCmd.get(0, 0));
					move.setY((float)sendCmd.get(1, 0));
					move.setW((float)sendCmd.get(2, 0));
				}
				
				if (useUpdateAll && mcastDelegate != null)
				{
					mcastDelegate.setGroupedMove(botId, move);
				} 
				else
				{
					transceiver.enqueueCommand(move);
				}
			}
				break;
			case CommandConstants.CMD_KICKER_KICKV2:
			{
				TigerKickerKickV2 kick = (TigerKickerKickV2) cmd;
				
				if (useUpdateAll && mcastDelegate != null)
				{
					mcastDelegate.setGroupedKick(botId, kick);
				} 
				else
				{
					transceiver.enqueueCommand(kick);
				}
			}
				break;
			case CommandConstants.CMD_MOTOR_DRIBBLE:
			{
				TigerDribble dribble = (TigerDribble) cmd;
				
				if (useUpdateAll && mcastDelegate != null)
				{
					mcastDelegate.setGroupedDribble(botId, dribble);
				} 
				else
				{
					transceiver.enqueueCommand(dribble);
				}
			}
				break;
			case CommandConstants.CMD_MOTOR_SET_PARAMS:
			{
				TigerMotorSetParams params = (TigerMotorSetParams) cmd;
				
				motorParams = params;
				
				transceiver.enqueueCommand(params);
				
				notifyMotorParamsChanged(params);
			}
				break;
			case CommandConstants.CMD_SYSTEM_SET_LOGS:
			{
				TigerSystemSetLogs logs = (TigerSystemSetLogs) cmd;
				
				setLogs = logs;
				
				transceiver.enqueueCommand(logs);
				
				notifyLogsChanged(logs);
			}
				break;
			default:
			{
				transceiver.enqueueCommand(cmd);
			}
				break;
		}
	}
	
	
	public void start()
	{
		if(!active)
		{
			return;
		}
		
		setUseUpdateAll(useUpdateAll);
		setActive(active);
		setOofCheck(oofCheck);
		
		if (netState == ENetworkState.OFFLINE)
		{
			changeNetworkState(ENetworkState.CONNECTING);
		}
	}

	public void stop()
	{
		changeNetworkState(ENetworkState.OFFLINE);
		
		if(mcastDelegate != null)
		{
			mcastDelegate.setMulticast(botId, false);
		}
	}
	

	@Override
	public void onIncommingCommand(ACommand cmd)
	{
		if (watchdog.isActive())
		{
			watchdog.reset();
		} else
		{
			changeNetworkState(ENetworkState.ONLINE);
		}
		
		switch (cmd.getCommand())
		{
			case CommandConstants.CMD_KICKER_STATUSV2:
			{
				TigerKickerStatusV2 stats = (TigerKickerStatusV2) cmd;
				
				lastKickerStatus = stats;
				
				notifyNewKickerStatusV2(stats);
			}
				break;
			case CommandConstants.CMD_MOTOR_PID_LOG:
			{
				TigerMotorPidLog log = (TigerMotorPidLog) cmd;
				
				notifyNewMotorPidLog(log);
			}
				break;
			case CommandConstants.CMD_SYSTEM_STATUS_MOVEMENT:
			{
				TigerSystemStatusMovement status = (TigerSystemStatusMovement) cmd;
				
				notifyNewSystemStatusMovement(status);
			}
				break;
			case CommandConstants.CMD_SYSTEM_POWER_LOG:
			{
				TigerSystemPowerLog log = (TigerSystemPowerLog) cmd;
				
				notifyNewSystemPowerLog(log);
			}
				break;
			case CommandConstants.CMD_MOVEMENT_LIS3_LOG_RAW:
			{
				TigerMovementLis3LogRaw log = (TigerMovementLis3LogRaw) cmd;
				
				notifyNewMovementLis3LogRaw(log);
			}
				break;
			case CommandConstants.CMD_SYSTEM_PONG:
			{
				TigerSystemPong pong = (TigerSystemPong) cmd;
				
				notifyNewSystemPong(pong);
			}
				break;
			case CommandConstants.CMD_KICKER_IR_LOG:
			{
				TigerKickerIrLog log = (TigerKickerIrLog) cmd;
				
				notifyNewKickerIrLog(log);
			}
				break;
		}
	}
	

	@Override
	public void onOutgoingCommand(ACommand cmd)
	{
	}
	

	public void setMulticastDelegate(IMulticastDelegate delegate)
	{
		mcastDelegate = delegate;
	}
	

	public void addObserver(ITigerBotObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
		
		super.addObserverIfNotPresent(o);
		
		transceiver.addObserver(o);
	}
	

	public void removeObserver(ITigerBotObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
		
		super.removeObserver(o);
		
		transceiver.removeObserver(o);
	}
	
	private void changeNetworkState(ENetworkState newState)
	{
		if (netState == newState)
		{
			return;
		}
		
		if (netState == ENetworkState.OFFLINE && newState == ENetworkState.CONNECTING)
		{
			// start transceiver
			transceiver.addObserver(this);
			transceiver.setLocalPort(getServerPort());
			transceiver.open(getIp(), getPort());
			
			if(mcastDelegate != null)
			{
				mcastDelegate.setIdentity(this);
			}
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.debug("Bot connecting: " + getName() + " (" + getBotId() + ")");
			
			return;
		}
		
		if (netState == ENetworkState.CONNECTING && newState == ENetworkState.OFFLINE)
		{
			// stop transceiver
			transceiver.removeObserver(this);
			if(mcastDelegate != null)
			{
				mcastDelegate.removeIdentity(this);
			}
			transceiver.close();
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Disconnected bot: " + name + " (" + botId + ")");
			
			return;
		}
		
		if (netState == ENetworkState.CONNECTING && newState == ENetworkState.ONLINE)
		{
			// set saved parameters
			for (int i = 0; i < 5; i++)
			{
				try
				{
					Thread.sleep(200);
				} catch (InterruptedException err)
				{
				}
				
				execute(motorParams);
				execute(setLogs);
			}
			
			// start watchdog
			watchdog.start(this);
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Connected bot: " + name + " (" + botId + ")");
			
			return;
		}
		
		if (netState == ENetworkState.ONLINE && newState == ENetworkState.CONNECTING)
		{
			// stop watchdog
			watchdog.stop();
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.debug("Bot timed out: " + getName() + " (" + getBotId() + ")");
			
			return;
		}
		
		if (netState == ENetworkState.ONLINE && newState == ENetworkState.OFFLINE)
		{
			// stop watchdog
			watchdog.stop();
			
			// terminate transceiver
			transceiver.removeObserver(this);
			int saveId = botId;
			botId = 255; // bot will deinitialize network interface
			if(mcastDelegate != null)
			{
				mcastDelegate.setIdentity(this);
			}
			botId = saveId;
			transceiver.close();
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Disconnected bot: " + name + " (" + botId + ")");
			
			return;
		}
		
		log.error("Invalid state transition from " + netState + " to " + newState);
	}
	

	public TigerKickerStatusV2 getLastKickerStatus()
	{
		return lastKickerStatus;
	}
	
	public void setCorrectionMatrices(Matrix X, Matrix Y)
	{
		xEpsilon = X;
		yEpsilon = Y;
	}
	

	public void setPid(int motorId, float kp, float ki, float kd, int slew)
	{
		motorParams.setPidParams(motorId, kp, ki, kd, slew);
		
		execute(motorParams);
	}
	

	public float getKp(int motorId)
	{
		return motorParams.getKp(motorId);
	}
	

	public float getKi(int motorId)
	{
		return motorParams.getKi(motorId);
	}
	

	public float getKd(int motorId)
	{
		return motorParams.getKd(motorId);
	}
	

	public int getSlewMax(int motorId)
	{
		return motorParams.getSlewMax(motorId);
	}
	

	public TigerMotorSetParams getMotorParams()
	{
		return motorParams;
	}

	public void setPidLogging(int motorId, boolean enable)
	{
		setLogs.setMotor(motorId, enable);
		
		execute(setLogs);
	}
	

	public boolean getPidLogging(int motorId)
	{
		return setLogs.getMotor(motorId);
	}
	

	public void setMode(MotorMode mode)
	{
		motorParams.setMode(mode);
		
		execute(motorParams);
	}
	

	public MotorMode getMode()
	{
		return motorParams.getMode();
	}
	

	public void setLogMovement(boolean enable)
	{
		setLogs.setMovement(enable);
		
		execute(setLogs);
	}
	

	public boolean getLogMovement()
	{
		return setLogs.getMovement();
	}
	

	public void setLogKicker(boolean enable)
	{
		setLogs.setKicker(enable);
		
		execute(setLogs);
	}
	

	public boolean getLogKicker()
	{
		return setLogs.getKicker();
	}
	

	public void setLogAccel(boolean enable)
	{
		setLogs.setAccel(enable);
		
		execute(setLogs);
	}
	

	public boolean getLogAccel()
	{
		return setLogs.getAccel();
	}
	

	public void setLogIr(boolean enable)
	{
		setLogs.setIr(enable);
		
		execute(setLogs);
	}
	

	public boolean getLogIr()
	{
		return setLogs.getIr();
	}
	

	public TigerSystemSetLogs getLogs()
	{
		return setLogs;
	}
	

	private void notifyNewKickerStatusV2(TigerKickerStatusV2 status)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onNewKickerStatusV2(status);
			}
		}
	}
	

	private void notifyNewMotorPidLog(TigerMotorPidLog log)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onNewMotorPidLog(log);
			}
		}
	}
	

	private void notifyNewSystemStatusMovement(TigerSystemStatusMovement status)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onNewSystemStatusMovement(status);
			}
		}
	}
	

	private void notifyNewSystemPowerLog(TigerSystemPowerLog log)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onNewSystemPowerLog(log);
			}
		}
	}
	

	private void notifyNewMovementLis3LogRaw(TigerMovementLis3LogRaw log)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onNewMovementLis3LogRaw(log);
			}
		}
	}
	

	private void notifyNewSystemPong(TigerSystemPong pong)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onNewSystemPong(pong);
			}
		}
	}
	

	private void notifyLogsChanged(TigerSystemSetLogs logs)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onLogsChanged(logs);
			}
		}
	}
	

	private void notifyMotorParamsChanged(TigerMotorSetParams params)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onMotorParamsChanged(params);
			}
		}
	}
	

	private void notifyNewKickerIrLog(TigerKickerIrLog log)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onNewKickerIrLog(log);
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
	

	@Override
	public ITransceiver getTransceiver()
	{
		return transceiver;
	}
	

	public String getMac()
	{
		return mac;
	}
	

	public void setMac(String mac)
	{
		this.mac = mac;
		
		notifyMacChanged(mac);
	}
	

	public int getServerPort()
	{
		return serverPort;
	}
	

	public void setServerPort(int serverPort)
	{
		this.serverPort = serverPort;
		
		notifyServerPortChanged(serverPort);
	}
	

	public String getCpuId()
	{
		return cpuId;
	}
	

	public void setCpuId(String cpuId)
	{
		this.cpuId = cpuId;
		
		notifyCpuIdChanged(cpuId);
	}
	

	public void setUseUpdateAll(boolean enable)
	{
		if(mcastDelegate == null)
		{
			return;
		}
		
		if(mcastDelegate.setMulticast(botId, enable))
		{
			useUpdateAll = enable;
		}
		else
		{
			useUpdateAll = false;
		}
		
		notifyUseUpdateAllChanged(useUpdateAll);
	}
	

	public boolean getUseUpdateAll()
	{
		return useUpdateAll;
	}
	
	public void setOofCheck(boolean enable)
	{
		oofCheck = enable;
		
		notifyOofCheckChanged(enable);
	}
	
	public boolean getOofCheck()
	{
		return oofCheck;
	}
	

	public ENetworkState getNetworkState()
	{
		return netState;
	}
	

	public float getMaxSpeed(float angle)
	{
		// TODO: calculate
		return 0;
	}
	

	public float getMaxAngularVelocity()
	{
		// TODO: calculate
		return 0;
	}
	

	public HierarchicalConfiguration getConfiguration()
	{
		HierarchicalConfiguration config = super.getConfiguration();
		
		config.addProperty("bot.mac", mac);
		config.addProperty("bot.serverport", serverPort);
		config.addProperty("bot.cpuid", cpuId);
		config.addProperty("bot.useUpdateAll", useUpdateAll);
		config.addProperty("bot.oofcheck", oofCheck);
		config.addProperty("bot.motorMode", motorParams.getMode().ordinal());
		
		config.addProperty("bot.logs.movement", setLogs.getMovement());
		config.addProperty("bot.logs.kicker", setLogs.getKicker());
		config.addProperty("bot.logs.accel", setLogs.getAccel());
		config.addProperty("bot.logs.ir", setLogs.getIr());
		
		for (int i = 0; i < 5; i++)
		{
			config.addProperty("bot.pid.motor(-1).kp", motorParams.getKp(i));
			config.addProperty("bot.pid.motor.ki", motorParams.getKi(i));
			config.addProperty("bot.pid.motor.kd", motorParams.getKd(i));
			config.addProperty("bot.pid.motor.slewMax", motorParams.getSlewMax(i));
			config.addProperty("bot.pid.motor.log", setLogs.getMotor(i));
		}
		
		return config;
	}
	
	private void notifyUseUpdateAllChanged(boolean useUpdateAll)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onUseUpdateAllChanged(useUpdateAll);
			}
		}
	}
	

	private void notifyMacChanged(String mac)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onMacChanged(mac);
			}
		}
	}
	

	private void notifyServerPortChanged(int port)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onServerPortChanged(port);
			}
		}
	}
	

	private void notifyCpuIdChanged(String cpuId)
	{
		synchronized (observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onCpuIdChanged(cpuId);
			}
		}
	}
	
	private void notifyOofCheckChanged(boolean enable)
	{
		synchronized(observers)
		{
			for (ITigerBotObserver observer : observers)
			{
				observer.onOofCheckChanged(enable);
			}
		}
	}
	
	public static final Matrix getCmdToMotorMatrix()	// This is psi
	{
		double alpha = 28*Math.PI/180;
		double beta = 45*Math.PI/180;
		double theta1 = alpha;
		double theta2 = Math.PI-alpha;
		double theta3 = Math.PI+beta;
		double theta4 = 2*Math.PI-beta;
		
		Matrix psi = new Matrix(4, 3);
		psi.set(0, 0, -Math.sin(theta1));
		psi.set(0, 1, Math.cos(theta1));
		psi.set(0, 2, 1);
		psi.set(1, 0, -Math.sin(theta2));
		psi.set(1, 1, Math.cos(theta2));
		psi.set(1, 2, 1);
		psi.set(2, 0, -Math.sin(theta3));
		psi.set(2, 1, Math.cos(theta3));
		psi.set(2, 2, 1);
		psi.set(3, 0, -Math.sin(theta4));
		psi.set(3, 1, Math.cos(theta4));
		psi.set(3, 2, 1);

		return psi;
	}
	
	public static final Matrix getMotorToCmdMatrix()	// This is psi_inv
	{
		return getCmdToMotorMatrix().inverse();
	}
}
