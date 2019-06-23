package edu.tigers.sumatra.sharedradio;
/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 22, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.RadioProtocolCommandOuterClass.RadioProtocolCommand;
import edu.tigers.sumatra.RadioProtocolWrapperOuterClass.RadioProtocolWrapper;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.basestation.ABaseStation;
import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.MatchCommand;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SharedRadioBaseStation extends ABaseStation implements IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(SharedRadioBaseStation.class.getName());
	
	
	@Configurable(comment = "IP", defValue = "127.0.0.1")
	private static String			ip;
	@Configurable(comment = "Port for yellow team", defValue = "10010")
	private static int				portYellow;
	@Configurable(comment = "Port for blue team", defValue = "10011")
	private static int				portBlue;
	
	
	private final Object				sync					= new Object();
	private DatagramSocket			ds;
	private SimpleWorldFrame		latestWorldFrame	= SimpleWorldFrame.createEmptyWorldFrame(0, 0);
	private double						dt						= 0.001;
	private final Set<BotID>		knownBots			= new HashSet<>();
	
	
	static
	{
		ConfigRegistration.registerClass("botmgr", SharedRadioBaseStation.class);
	}
	
	
	/**
	 * 
	 */
	public SharedRadioBaseStation()
	{
		super(EBotType.SHARED_RADIO);
	}
	
	
	@Override
	public void enqueueCommand(final BotID id, final ACommand cmd)
	{
		log.warn("Could not execute command for bot " + id + ": " + cmd.getType());
	}
	
	
	/**
	 * @param bot
	 */
	public void sendMatchCommand(final SharedRadioBot bot)
	{
		MatchCommand matchCmd = bot.getMatchCtrl();
		
		RadioProtocolCommand.Builder cmdBuilder = RadioProtocolCommand.newBuilder();
		cmdBuilder.setRobotId(bot.getBotId().getNumber());
		float spin = Math.min(1, (float) matchCmd.getDribbleSpeed() / 10000f);
		if (spin != 0)
		{
			cmdBuilder.setDribblerSpin(-spin);
		}
		
		switch (matchCmd.getMode())
		{
			case ARM_AIM:
				if (!bot.isTargetAngleReached())
				{
					break;
				}
			case ARM:
			case FORCE:
			case DRIBBLER:
				switch (matchCmd.getDevice())
				{
					case CHIP:
						cmdBuilder.setChipKick((float) matchCmd.getKickSpeed());
						break;
					case STRAIGHT:
						cmdBuilder.setFlatKick((float) matchCmd.getKickSpeed());
						break;
				}
				break;
			case DISARM:
			case NONE:
			default:
				break;
		}
		
		switch (matchCmd.getSkill().getType())
		{
			case LOCAL_VELOCITY:
			case GLOBAL_POSITION:
			{
				IVector3 localVel = new Vector3();
				
				ITrackedBot tBot;
				double dt;
				synchronized (sync)
				{
					tBot = latestWorldFrame.getBot(bot.getBotId());
					dt = this.dt;
				}
				if (tBot != null)
				{
					IVector3 pos = new Vector3(tBot.getPos(), tBot.getAngle());
					IVector3 vel = new Vector3(tBot.getVel(), tBot.getaVel());
					localVel = bot.executeBotSkill(matchCmd.getSkill(), pos, vel, dt);
				} else
				{
					localVel = new Vector3();
				}
				
				cmdBuilder.setVelocityX((float) localVel.y());
				cmdBuilder.setVelocityY((float) -localVel.x());
				cmdBuilder.setVelocityR((float) localVel.z());
				
				break;
			}
			case WHEEL_VELOCITY:
			case MOTORS_OFF:
			default:
			{
				cmdBuilder.setVelocityX(0);
				cmdBuilder.setVelocityY(0);
				cmdBuilder.setVelocityR(0);
				break;
			}
		}
		
		
		RadioProtocolWrapper wrapper = RadioProtocolWrapper.newBuilder().addCommand(cmdBuilder).build();
		byte[] buffer = wrapper.toByteArray();
		try
		{
			int port = bot.getBotId().getTeamColor() == ETeamColor.YELLOW ? portYellow : portBlue;
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port);
			
			synchronized (sync)
			{
				if (ds != null)
				{
					ds.send(dp);
				}
			}
		} catch (IOException e)
		{
			log.error("Could not send package to grSim", e);
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		synchronized (sync)
		{
			dt = (wFrameWrapper.getSimpleWorldFrame().getTimestamp() - latestWorldFrame.getTimestamp()) / 1e9;
			latestWorldFrame = wFrameWrapper.getSimpleWorldFrame();
			
			Set<BotID> curBots = new HashSet<>();
			for (ITrackedBot tBot : latestWorldFrame.getBots().values())
			{
				if (tBot.isVisible())
				{
					curBots.add(tBot.getBotId());
				}
			}
			
			Set<BotID> newBots = new HashSet<>(curBots);
			newBots.removeAll(knownBots);
			Set<BotID> remBots = new HashSet<>(knownBots);
			remBots.removeAll(curBots);
			
			for (BotID botId : newBots)
			{
				notifyBotOnline(new SharedRadioBot(botId, this));
				knownBots.add(botId);
			}
			
			for (BotID botId : remBots)
			{
				notifyBotOffline(botId);
				knownBots.remove(botId);
			}
		}
	}
	
	
	@Override
	public ENetworkState getNetState()
	{
		return ENetworkState.ONLINE;
	}
	
	
	@Override
	public String getName()
	{
		return "Shared Radio BS";
	}
	
	
	@Override
	protected void onConnect()
	{
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFramePrioConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find WP module", err);
		}
		
		synchronized (sync)
		{
			try
			{
				ds = new DatagramSocket(null);
			} catch (SocketException err)
			{
				log.error("Could not open datagram socket for grSimBot", err);
			}
		}
	}
	
	
	@Override
	protected void onDisconnect()
	{
		synchronized (sync)
		{
			ds.close();
			ds = null;
		}
		
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeWorldFramePrioConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find WP module", err);
		}
	}
	
}
