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

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp.ITransceiverTCPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp.ITransceiverTCP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp.TransceiverTCP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.tcp.TransceiverTCP.EConnectionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTCalibrate;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTPIDHistory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTSetPID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTSetSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTStatus;
import edu.dhbw.mannheim.tigers.sumatra.util.IWatchdogObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.Watchdog;

public class CtBot extends ABot implements ITransceiverTCPObserver, IWatchdogObserver
{
	private final Logger				log						= Logger.getLogger(getClass());
	
	private float						kp[]						= new float[2];
	private float						ki[]						= new float[2];
	private float						kd[]						= new float[2];
	private int							delay;
	
	private float						min[]						= new float[2];
	private float						max[]						= new float[2];
	
	private static final int		TIMEOUT					= 2000;
	private ITransceiverTCP				transceiver			= new TransceiverTCP();
	private Watchdog					watchdog					= new Watchdog(TIMEOUT);
	
	private List<ICtBotObserver>	observers				= new ArrayList<ICtBotObserver>();
	
	private Vector2					lastVector				= new Vector2();
	private float						lastAngularVelocity	= 0.0f;
	
	
	public CtBot(SubnodeConfiguration botConfig)
	{
		super(botConfig);
		
		this.kp[0] = botConfig.getFloat("pid.left.kp");
		this.kp[1] = botConfig.getFloat("pid.right.kp");
		this.ki[0] = botConfig.getFloat("pid.left.ki");
		this.ki[1] = botConfig.getFloat("pid.right.ki");
		this.kd[0] = botConfig.getFloat("pid.left.kd");
		this.kd[1] = botConfig.getFloat("pid.right.kd");
		this.delay = botConfig.getInt("pid.delay");
		
		min[0] = -1;
		min[1] = -1;
		max[0] = 1;
		max[1] = 1;
	}
	
	public CtBot(int id)
	{
		super(EBotType.CT, id);
		
		kp[0] = 0.3f;
		ki[0] = 0.05f;
		kd[0] = 0.01f;
		kp[1] = 0.3f;
		ki[1] = 0.05f;
		kd[1] = 0.01f;
		delay = 100;
		
		min[0] = -1;
		min[1] = -1;
		max[0] = 1;
		max[1] = 1;
	
	}
	
	public void start()
	{
		transceiver.addObserver(this);

		transceiver.connect(getIp(), getPort());
		
		log.debug("Bot started: " + getName() + " (" + getBotId() + ")");
	}
	
	public void stop()
	{
		transceiver.removeObserver(this);

		transceiver.disconnect();
	}
	

	@Override
	public void execute(ACommand cmd)
	{
		switch (cmd.getCommand())
		{
			case CommandConstants.CMD_CT_SET_SPEED:
			{
				CTSetSpeed move = (CTSetSpeed) cmd;
				
				move.setUnusedComponents(lastVector, lastAngularVelocity);
				
				lastVector = move.getDir();
				lastAngularVelocity = move.getAngularVelocity();
				
				transceiver.enqueueCommand(move);
			}
			break;
			case CommandConstants.CMD_CT_CALIBRATE:
			{
				transceiver.enqueueCommand(cmd);
			}
			break;
			case CommandConstants.CMD_CT_SET_PID:
			{
				CTSetPID pid = (CTSetPID)cmd;
				
				kp[0] = pid.getKp()[0];
				kp[1] = pid.getKp()[1];
				ki[0] = pid.getKi()[0];
				ki[1] = pid.getKi()[1];
				kd[0] = pid.getKd()[0];
				kd[1] = pid.getKd()[1];
				delay = pid.getDelay();
				
				notifyPidChanged();
				
				transceiver.enqueueCommand(cmd);
			}
			break;
			default:
			{
				log.warn("CtBot cannot handle command: " + cmd.getCommand());
			}
			break;
		}
	}
	
	@Override
	public void onIncommingCommand(ACommand cmd)
	{
		switch (cmd.getCommand())
		{
			case CommandConstants.CMD_CT_STATUS:
			{
				watchdog.reset();
				
				CTStatus cts = (CTStatus) cmd;
				
				min = cts.getMin();
				max = cts.getMax();
				
				synchronized(observers)
				{
					for (ICtBotObserver o : observers)
					{
						o.onNewStatus(cts);
					}
				}
			}
			break;
			
			case CommandConstants.CMD_CT_PID_HISTORY:
			{
				CTPIDHistory history = (CTPIDHistory) cmd;
				history.postProcess(getMaxSpeed(0));
				
				synchronized(observers)
				{
					for (ICtBotObserver o : observers)
					{
						o.onNewPIDHistory(history);
					}
				}
			}
			break;
			default:
			{
			}
			break;
		}
	}
	
	@Override
	public void onOutgoingCommand(ACommand cmd)
	{
	}
	

	public void addObserver(ICtBotObserver o)
	{
		synchronized(observers)
		{
			observers.add(o);
		}

//		super.addObserver(o);
		super.addObserverIfNotPresent(o);
		
		transceiver.addObserver(o);
	}
	

	public void removeObserver(ICtBotObserver o)
	{
		synchronized(observers)
		{
			observers.remove(o);
		}
		
		super.removeObserver(o);
		
		transceiver.removeObserver(o);
	}
	
	public float[] getKp()
	{
		return kp;
	}
	
	public float[] getKi()
	{
		return ki;
	}
	
	public float[] getKd()
	{
		return kd;
	}
	
	public int getDelay()
	{
		return delay;
	}
	
	public void setPid(float kp[], float ki[], float kd[], int delay)
	{
		this.kp = kp;
		this.ki = ki;
		this.kd = kd;
		this.delay = delay;

		CTSetPID pid = new CTSetPID();
		pid.setKp(kp[0], kp[1]);
		pid.setKi(ki[0], ki[1]);
		pid.setKd(kd[0], kd[1]);
		pid.setDelay(delay);
		
		transceiver.enqueueCommand(pid);
	}
	
	public void calibrate(int time)
	{
		CTCalibrate cal = new CTCalibrate(time);
		
		transceiver.enqueueCommand(cal);
	}
	
	public void setIp(String ip)
	{
		super.setIp(ip);
		
		transceiver.setDestination(ip, port);
	}
	
	public void setPort(int port)
	{
		super.setPort(port);
		
		transceiver.setDestination(ip, port);
	}
		
	@Override
	public float getMaxSpeed(float angle)
	{
		if (angle > Math.PI * 0.5 && angle < Math.PI * 1.5)
		{
			return Math.min(min[0], min[1]);
		} else
		{
			return Math.min(max[0], max[1]);
		}
	}

	protected void notifyPidChanged()
	{
		synchronized(observers)
		{
			for(ICtBotObserver o : observers)
			{
				o.onPidChanged(kp, ki, kd, delay);
			}
		}
	}

	@Override
	public float getMaxAngularVelocity()
	{
		return Math.min(Math.min(min[0], min[1]), Math.min(max[0], max[1])) / 0.075f;
	}

	@Override
	public void onConnectionChanged(EConnectionState state)
	{
		if(state == EConnectionState.DISCONNECTED)
		{
			watchdog.stop();
			log.info("Disconnected bot: " + name + " (" + botId + ")");
			
			notifyNetworkStateChanged(ENetworkState.OFFLINE);
		}
		
		if(state == EConnectionState.CONNECTED)
		{
			CTSetPID pid = new CTSetPID();
			pid.setKp(kp[0], kp[1]);
			pid.setKi(ki[0], ki[1]);
			pid.setKd(kd[0], kd[1]);
			pid.setDelay(delay);
			
			transceiver.enqueueCommand(pid);
			
			watchdog.start(this);
			
			log.info("Connected bot: " + name + " (" + botId + ")");
			
			notifyNetworkStateChanged(ENetworkState.ONLINE);
		}
		
		if(state == EConnectionState.CONNECTING)
		{
			log.info("Connecting bot: " + name + " (" + botId + ") ...");
			
			notifyNetworkStateChanged(ENetworkState.OFFLINE);
		}
	}

	@Override
	public void onWatchdogTimeout()
	{
		transceiver.reconnect();
		
		log.warn("Bot " + getName() + " (" + getBotId() + ") timed out");
	}

	@Override
	public ITransceiverTCP getTransceiver()
	{
		return transceiver;
	}
	
	@Override
	protected void notifyNetworkStateChanged(ENetworkState state)
	{
		synchronized(observers)
		{
			for (ICtBotObserver observer : observers)
			{
				observer.onNetworkStateChanged(state);
			}
		}
	}
	
	public HierarchicalConfiguration getConfiguration()
	{
		HierarchicalConfiguration config = super.getConfiguration();
		
		config.addProperty("bot.pid.left.kp", kp[0]);
		config.addProperty("bot.pid.left.ki", ki[0]);
		config.addProperty("bot.pid.left.kd", kd[0]);
		config.addProperty("bot.pid.right.kp", kp[1]);
		config.addProperty("bot.pid.right.ki", ki[1]);
		config.addProperty("bot.pid.right.kd", kd[1]);
		config.addProperty("bot.pid.delay", delay);
		
		return config;
	}
}
