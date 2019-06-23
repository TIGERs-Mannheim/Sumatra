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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.IMulticastDelegate;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ITransceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.UnicastTransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerIrLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams.MotorMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillPositioningCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;
import edu.dhbw.mannheim.tigers.sumatra.util.IWatchdogObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.Watchdog;


/**
 * The old tiger
 *
 * @author AndreR
 */
public class TigerBot extends ABot implements ITransceiverUDPObserver, IWatchdogObserver, IWorldFrameConsumer
{
	// Logger
	private static final Logger				log							= Logger.getLogger(TigerBot.class.getName());
	
	private static final float					BAT_MIN						= 12;
	private static final float					BAT_MAX						= 16.8f;
	private static final int					TIMEOUT						= 2000;
	private final ITransceiverUDP				transceiver					= new UnicastTransceiverUDP(true);
	private final Watchdog						watchdog						= new Watchdog(TIMEOUT);
	
	private final List<ITigerBotObserver>	observers					= new ArrayList<ITigerBotObserver>();
	
	private IMulticastDelegate					mcastDelegate				= null;
	
	private ENetworkState						netState						= ENetworkState.OFFLINE;
	private boolean								active						= false;
	
	private Vector2								lastDirection				= new Vector2();
	private float									lastAngularVelocity		= 0.0f;
	private float									lastCompensatedVelocity	= 0.0f;
	
	// Identification
	private String									mac							= "02-00-00-00-00-00";
	private int										serverPort					= 0;
	private String									cpuId							= "000000000000000000000000";
	protected String								ip								= "";
	protected int									port							= 0;
	
	// Kicker
	private TigerKickerStatusV2				lastKickerStatus			= null;
	
	// Motor
	private TigerMotorSetParams				motorParams					= new TigerMotorSetParams();
	private TigerSystemSetLogs					setLogs						= new TigerSystemSetLogs();
	
	private boolean								useUpdateAll				= false;
	private boolean								oofCheck						= false;
	
	private Matrix									xEpsilon						= null;
	private Matrix									yEpsilon						= null;
	
	private TigerSystemPowerLog				powerLog						= new TigerSystemPowerLog();
	private TigerKickerStatusV2				kickerStatus				= new TigerKickerStatusV2();
	
	
	private SimpleWorldFrame					latestWorldFrame			= null;
	
	
	/**
	 * @param botConfig
	 * @throws BotInitException
	 */
	public TigerBot(final SubnodeConfiguration botConfig) throws BotInitException
	{
		super(botConfig);
		
		try
		{
			mac = botConfig.getString("mac");
			serverPort = botConfig.getInt("serverport");
			cpuId = botConfig.getString("cpuid");
			useUpdateAll = botConfig.getBoolean("useUpdateAll", false);
			oofCheck = botConfig.getBoolean("oofcheck", false);
			active = botConfig.getBoolean("active", true);
			ip = botConfig.getString("ip");
			port = botConfig.getInt("port");
			
			motorParams.setMode(botConfig.getInt("motorMode", 2));
			
			setLogs.setMovement(botConfig.getBoolean("logs.movement", true));
			setLogs.setKicker(botConfig.getBoolean("logs.kicker", true));
			setLogs.setPower(botConfig.getBoolean("logs.power", true));
			setLogs.setAccel(botConfig.getBoolean("logs.accel", false));
			setLogs.setIr(botConfig.getBoolean("logs.ir", false));
		} catch (final NoSuchElementException nse)
		{
			throw new BotInitException(botConfig, nse);
		}
		
		final List<?> motors = botConfig.configurationsAt("pid.motor");
		int i = 0;
		for (final Object name2 : motors)
		{
			final SubnodeConfiguration motor = (SubnodeConfiguration) name2;
			
			motorParams.setPidParams(i, motor.getFloat("kp", 1.0f), motor.getFloat("ki", 0.5f),
					motor.getFloat("kd", 0.0f), motor.getInt("slewMax", 250));
			setLogs.setMotor(i, motor.getBoolean("log", false));
			
			i++;
		}
		
		if (getMcastDelegateKey() == -1)
		{
			setMcastDelegateKey(botId.getTeamColor() == ETeamColor.YELLOW ? 0 : 1);
		}
	}
	
	
	@Override
	protected Map<EFeature, EFeatureState> getDefaultFeatureStates()
	{
		Map<EFeature, EFeatureState> result = EFeature.createFeatureList();
		result.put(EFeature.DRIBBLER, EFeatureState.WORKING);
		result.put(EFeature.CHIP_KICKER, EFeatureState.KAPUT);
		result.put(EFeature.STRAIGHT_KICKER, EFeatureState.WORKING);
		result.put(EFeature.MOVE, EFeatureState.WORKING);
		result.put(EFeature.BARRIER, EFeatureState.LIMITED);
		return result;
	}
	
	
	/**
	 * @param id
	 */
	public TigerBot(final BotID id)
	{
		super(EBotType.TIGER, id, -1, id.getTeamColor() == ETeamColor.YELLOW ? 0 : 1);
	}
	
	
	@Override
	public void execute(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_MOTOR_MOVE_V2:
			{
				final TigerMotorMoveV2 move = (TigerMotorMoveV2) cmd;
				
				move.setUnusedComponents(lastDirection, lastAngularVelocity, lastCompensatedVelocity);
				
				lastDirection = move.getXY();
				lastAngularVelocity = move.getW();
				lastCompensatedVelocity = move.getV();
				
				if ((xEpsilon != null) && (yEpsilon != null))
				{
					final Matrix origCmd = new Matrix(3, 1);
					origCmd.set(0, 0, move.getXY().x);
					origCmd.set(1, 0, move.getXY().y);
					origCmd.set(2, 0, move.getW());
					
					final Matrix sendCmd = TigerBot.getMotorToCmdMatrix()
							.minus(xEpsilon.times(move.getXY().x).plus(yEpsilon.times(move.getXY().y)))
							.times(TigerBot.getCmdToMotorMatrix().times(origCmd));
					
					move.setX((float) sendCmd.get(0, 0));
					move.setY((float) sendCmd.get(1, 0));
					move.setW((float) sendCmd.get(2, 0));
				}
				
				if (useUpdateAll && (mcastDelegate != null))
				{
					mcastDelegate.setGroupedMove(botId, move);
				} else
				{
					transceiver.enqueueCommand(move);
				}
			}
				break;
			case CMD_SKILL_POSITIONING:
				final TigerMotorMoveV2 move = new TigerMotorMoveV2();
				TigerSkillPositioningCommand posCmd = (TigerSkillPositioningCommand) cmd;
				IVector2 dest = posCmd.getDestination();
				float orient = posCmd.getOrientation();
				
				if (latestWorldFrame != null)
				{
					TrackedTigerBot bot = latestWorldFrame.getBot(getBotID());
					if (bot == null)
					{
						log.warn("No bot with id " + getBotID());
						return;
					}
					IVector2 error = dest.subtractNew(bot.getPos()).multiply(0.003f);
					IVector2 localVel = AiMath.convertGlobalBotVector2Local(error, bot.getAngle());
					move.setX(localVel.x());
					move.setY(localVel.y());
					
					float errorW = orient - bot.getAngle();
					move.setW(AngleMath.normalizeAngle(errorW) * 2);
				} else
				{
					move.setX(0);
					move.setY(0);
					move.setW(0);
				}
				
				if (useUpdateAll && (mcastDelegate != null))
				{
					mcastDelegate.setGroupedMove(botId, move);
				} else
				{
					transceiver.enqueueCommand(move);
				}
				break;
			case CMD_KICKER_KICKV2:
			{
				final TigerKickerKickV2 kick = (TigerKickerKickV2) cmd;
				
				if (useUpdateAll && (mcastDelegate != null))
				{
					mcastDelegate.setGroupedKick(botId, kick);
				} else
				{
					transceiver.enqueueCommand(kick);
				}
			}
				break;
			case CMD_MOTOR_DRIBBLE:
			{
				final TigerDribble dribble = (TigerDribble) cmd;
				
				if (useUpdateAll && (mcastDelegate != null))
				{
					mcastDelegate.setGroupedDribble(botId, dribble);
				} else
				{
					transceiver.enqueueCommand(dribble);
				}
			}
				break;
			case CMD_MOTOR_SET_PARAMS:
			{
				final TigerMotorSetParams params = (TigerMotorSetParams) cmd;
				
				motorParams = params;
				
				transceiver.enqueueCommand(params);
				
				notifyMotorParamsChanged(params);
			}
				break;
			case CMD_SYSTEM_SET_LOGS:
			{
				final TigerSystemSetLogs logs = (TigerSystemSetLogs) cmd;
				
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
		notifyOutGoingCommand(cmd);
	}
	
	
	@Override
	public void start()
	{
		if (!active)
		{
			return;
		}
		
		setUseUpdateAll(useUpdateAll);
		setOofCheck(oofCheck);
		
		if (netState == ENetworkState.OFFLINE)
		{
			changeNetworkState(ENetworkState.CONNECTING);
		}
		
		AWorldPredictor wp;
		try
		{
			wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find WP module", err);
		}
	}
	
	
	@Override
	public void stop()
	{
		changeNetworkState(ENetworkState.OFFLINE);
		
		if (mcastDelegate != null)
		{
			mcastDelegate.setMulticast(botId, false);
		}
	}
	
	
	@Override
	public void onIncommingCommand(final ACommand cmd)
	{
		if (watchdog.isActive())
		{
			watchdog.reset();
		} else
		{
			changeNetworkState(ENetworkState.ONLINE);
		}
		
		switch (cmd.getType())
		{
			case CMD_KICKER_STATUSV2:
			{
				final TigerKickerStatusV2 stats = (TigerKickerStatusV2) cmd;
				
				lastKickerStatus = stats;
				
				notifyNewKickerStatusV2(stats);
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
				
				notifyNewSystemPong(pong);
			}
				break;
			case CMD_KICKER_IR_LOG:
			{
				final TigerKickerIrLog kickerLog = (TigerKickerIrLog) cmd;
				
				notifyNewKickerIrLog(kickerLog);
			}
				break;
			default:
				break;
		}
	}
	
	
	@Override
	public void onOutgoingCommand(final ACommand cmd)
	{
	}
	
	
	/**
	 * @param delegate
	 */
	public void setMulticastDelegate(final IMulticastDelegate delegate)
	{
		mcastDelegate = delegate;
	}
	
	
	/**
	 * @param o
	 */
	public void addObserver(final ITigerBotObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
		
		super.addObserverIfNotPresent(o);
		
		transceiver.addObserver(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final ITigerBotObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
		
		super.removeObserver(o);
		
		transceiver.removeObserver(o);
	}
	
	
	private void changeNetworkState(final ENetworkState newState)
	{
		if (netState == newState)
		{
			return;
		}
		
		if ((netState == ENetworkState.OFFLINE) && (newState == ENetworkState.CONNECTING))
		{
			// start transceiver
			transceiver.addObserver(this);
			transceiver.setLocalPort(getServerPort());
			transceiver.open(getIp(), getPort());
			
			if (mcastDelegate != null)
			{
				mcastDelegate.setIdentity(this);
			}
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			TigerSystemPing pingCmd = new TigerSystemPing();
			
			transceiver.enqueueCommand(pingCmd);
			
			log.debug("Bot connecting: " + getName() + " (" + getBotID() + ")");
			
			return;
		}
		
		if ((netState == ENetworkState.CONNECTING) && (newState == ENetworkState.OFFLINE))
		{
			// stop transceiver
			transceiver.removeObserver(this);
			if (mcastDelegate != null)
			{
				mcastDelegate.removeIdentity(this);
			}
			transceiver.close();
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.info("Disconnected bot: " + name + " (" + botId + ")");
			
			return;
		}
		
		if ((netState == ENetworkState.CONNECTING) && (newState == ENetworkState.ONLINE))
		{
			// set saved parameters
			for (int i = 0; i < 5; i++)
			{
				try
				{
					Thread.sleep(200);
				} catch (final InterruptedException err)
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
		
		if ((netState == ENetworkState.ONLINE) && (newState == ENetworkState.CONNECTING))
		{
			// stop watchdog
			watchdog.stop();
			
			netState = newState;
			notifyNetworkStateChanged(netState);
			
			log.debug("Bot timed out: " + getName() + " (" + getBotID() + ")");
			
			return;
		}
		
		if ((netState == ENetworkState.ONLINE) && (newState == ENetworkState.OFFLINE))
		{
			// stop watchdog
			watchdog.stop();
			
			// terminate transceiver
			transceiver.removeObserver(this);
			final BotID saveId = botId;
			
			// bot will deinitialize network interface --> botId must be 255
			botId = BotID.createBotId();
			if (mcastDelegate != null)
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
	
	
	@Override
	public void newSpline(final SplinePair3D spline)
	{
		notifyNewSplineData(spline);
	}
	
	
	/**
	 * @return
	 */
	public TigerKickerStatusV2 getLastKickerStatus()
	{
		return lastKickerStatus;
	}
	
	
	/**
	 * @param x
	 * @param y
	 */
	public void setCorrectionMatrices(final Matrix x, final Matrix y)
	{
		xEpsilon = x;
		yEpsilon = y;
	}
	
	
	/**
	 * @param motorId
	 * @param kp
	 * @param ki
	 * @param kd
	 * @param slew
	 */
	public void setPid(final int motorId, final float kp, final float ki, final float kd, final int slew)
	{
		motorParams.setPidParams(motorId, kp, ki, kd, slew);
		
		execute(motorParams);
	}
	
	
	/**
	 * @param motorId
	 * @return
	 */
	public float getKp(final int motorId)
	{
		return motorParams.getKp(motorId);
	}
	
	
	/**
	 * @param motorId
	 * @return
	 */
	public float getKi(final int motorId)
	{
		return motorParams.getKi(motorId);
	}
	
	
	/**
	 * @param motorId
	 * @return
	 */
	public float getKd(final int motorId)
	{
		return motorParams.getKd(motorId);
	}
	
	
	/**
	 * @param motorId
	 * @return
	 */
	public int getSlewMax(final int motorId)
	{
		return motorParams.getSlewMax(motorId);
	}
	
	
	/**
	 * @return
	 */
	public TigerMotorSetParams getMotorParams()
	{
		return motorParams;
	}
	
	
	/**
	 * @param motorId
	 * @param enable
	 */
	public void setPidLogging(final int motorId, final boolean enable)
	{
		setLogs.setMotor(motorId, enable);
		
		execute(setLogs);
	}
	
	
	/**
	 * @param motorId
	 * @return
	 */
	public boolean getPidLogging(final int motorId)
	{
		return setLogs.getMotor(motorId);
	}
	
	
	/**
	 * @param mode
	 */
	public void setMode(final MotorMode mode)
	{
		motorParams.setMode(mode);
		
		execute(motorParams);
	}
	
	
	/**
	 * @return
	 */
	public MotorMode getMode()
	{
		return motorParams.getMode();
	}
	
	
	/**
	 * @param enable
	 */
	public void setLogMovement(final boolean enable)
	{
		setLogs.setMovement(enable);
		
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
	
	
	private void notifyNewKickerStatusV2(final TigerKickerStatusV2 status)
	{
		kickerStatus = status;
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onNewKickerStatusV2(status);
			}
		}
	}
	
	
	private void notifyNewMotorPidLog(final TigerMotorPidLog log)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onNewMotorPidLog(log);
			}
		}
	}
	
	
	private void notifyNewSystemStatusMovement(final TigerSystemStatusMovement status)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
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
			for (final ITigerBotObserver observer : observers)
			{
				observer.onNewSystemPowerLog(log);
			}
		}
	}
	
	
	private void notifyNewSystemPong(final TigerSystemPong pong)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onNewSystemPong(pong);
			}
		}
	}
	
	
	private void notifyLogsChanged(final TigerSystemSetLogs logs)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onLogsChanged(logs);
			}
		}
	}
	
	
	private void notifyMotorParamsChanged(final TigerMotorSetParams params)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onMotorParamsChanged(params);
			}
		}
	}
	
	
	private void notifyNewKickerIrLog(final TigerKickerIrLog log)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
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
	
	
	/**
	 * @return
	 */
	public ITransceiver getTransceiver()
	{
		return transceiver;
	}
	
	
	/**
	 * @return
	 */
	public String getMac()
	{
		return mac;
	}
	
	
	/**
	 * @param mac
	 */
	public void setMac(final String mac)
	{
		this.mac = mac;
		
		notifyMacChanged(mac);
	}
	
	
	/**
	 * @return
	 */
	public int getServerPort()
	{
		return serverPort;
	}
	
	
	/**
	 * @param serverPort
	 */
	public void setServerPort(final int serverPort)
	{
		this.serverPort = serverPort;
		
		notifyServerPortChanged(serverPort);
	}
	
	
	/**
	 * @return
	 */
	public String getCpuId()
	{
		return cpuId;
	}
	
	
	/**
	 * @param cpuId
	 */
	public void setCpuId(final String cpuId)
	{
		this.cpuId = cpuId;
		
		notifyCpuIdChanged(cpuId);
	}
	
	
	/**
	 * @param enable
	 */
	public void setUseUpdateAll(final boolean enable)
	{
		if (mcastDelegate == null)
		{
			return;
		}
		
		if (mcastDelegate.setMulticast(botId, enable))
		{
			useUpdateAll = enable;
		} else
		{
			useUpdateAll = false;
		}
		
		notifyUseUpdateAllChanged(useUpdateAll);
	}
	
	
	/**
	 * @return
	 */
	public boolean getUseUpdateAll()
	{
		return useUpdateAll;
	}
	
	
	/**
	 * @param enable
	 */
	public void setOofCheck(final boolean enable)
	{
		oofCheck = enable;
		
		notifyOofCheckChanged(enable);
	}
	
	
	/**
	 * @return
	 */
	public boolean getOofCheck()
	{
		return oofCheck;
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
	public float getBatteryLevel()
	{
		return powerLog.getBatLevel();
	}
	
	
	@Override
	public float getKickerLevel()
	{
		return kickerStatus.getCapLevel();
	}
	
	
	@Override
	public HierarchicalConfiguration getConfiguration()
	{
		final HierarchicalConfiguration config = super.getConfiguration();
		
		config.addProperty("bot.ip", ip);
		config.addProperty("bot.port", port);
		config.addProperty("bot.mac", mac);
		config.addProperty("bot.serverport", serverPort);
		config.addProperty("bot.cpuid", cpuId);
		config.addProperty("bot.useUpdateAll", useUpdateAll);
		config.addProperty("bot.oofcheck", oofCheck);
		config.addProperty("bot.active", active);
		config.addProperty("bot.motorMode", motorParams.getMode().ordinal());
		
		config.addProperty("bot.logs.movement", setLogs.getMovement());
		config.addProperty("bot.logs.kicker", setLogs.getKicker());
		config.addProperty("bot.logs.power", setLogs.getPower());
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
	
	
	/**
	 * @return
	 */
	public String getIp()
	{
		return ip;
	}
	
	
	/**
	 * @param ip
	 */
	public void setIP(final String ip)
	{
		this.ip = ip;
		
		notifyIpChanged();
	}
	
	
	/**
	 * @return
	 */
	public int getPort()
	{
		return port;
	}
	
	
	/**
	 * @param port
	 */
	public void setPort(final int port)
	{
		this.port = port;
		
		notifyPortChanged();
	}
	
	
	protected void notifyIpChanged()
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver o : observers)
			{
				o.onIpChanged(ip);
			}
		}
	}
	
	
	protected void notifyPortChanged()
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver o : observers)
			{
				o.onPortChanged(port);
			}
		}
	}
	
	
	private void notifyUseUpdateAllChanged(final boolean useUpdateAll)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onUseUpdateAllChanged(useUpdateAll);
			}
		}
	}
	
	
	private void notifyMacChanged(final String mac)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onMacChanged(mac);
			}
		}
	}
	
	
	private void notifyServerPortChanged(final int port)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onServerPortChanged(port);
			}
		}
	}
	
	
	private void notifyCpuIdChanged(final String cpuId)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onCpuIdChanged(cpuId);
			}
		}
	}
	
	
	private void notifyOofCheckChanged(final boolean enable)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onOofCheckChanged(enable);
			}
		}
	}
	
	
	private void notifyOutGoingCommand(final ACommand cmd)
	{
		synchronized (observers)
		{
			for (final ITigerBotObserver observer : observers)
			{
				observer.onOutgoingCommand(cmd);
			}
		}
	}
	
	
	/**
	 * This is psi
	 *
	 * @return
	 */
	public static final Matrix getCmdToMotorMatrix()
	{
		final double alpha = (28 * Math.PI) / 180;
		final double beta = (45 * Math.PI) / 180;
		final double theta1 = alpha;
		final double theta2 = Math.PI - alpha;
		final double theta3 = Math.PI + beta;
		final double theta4 = (2 * Math.PI) - beta;
		
		final Matrix psi = new Matrix(4, 3);
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
	
	
	@Override
	public void onNewSimpleWorldFrame(final SimpleWorldFrame worldFrame)
	{
		latestWorldFrame = worldFrame;
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrame wFrame)
	{
	}
	
	
	@Override
	public void onStop()
	{
	}
	
	
	@Override
	public void onVisionSignalLost(final SimpleWorldFrame emptyWf)
	{
		latestWorldFrame = null;
	}
	
	
	/**
	 * This is psi_inv
	 *
	 * @return
	 */
	public static final Matrix getMotorToCmdMatrix()
	{
		return getCmdToMotorMatrix().inverse();
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
	
	
	/**
	 * Shall this bot connect on start?
	 *
	 * @param active
	 */
	public void setActive(final boolean active)
	{
		this.active = active;
	}
	
	
	/**
	 * @return
	 */
	public boolean isActive()
	{
		return active;
	}
	
	
	@Override
	public float getKickerLevelMax()
	{
		return 350f;
	}
}
