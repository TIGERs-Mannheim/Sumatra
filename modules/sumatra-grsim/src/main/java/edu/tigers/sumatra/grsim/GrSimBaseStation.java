/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, Tigers DHBW Mannheim
 * Project: TIGERS - GrSimAdapter
 * Date: 17.07.2012
 * Author(s): Peter Birkenkampf, TilmanS
 * *********************************************************
 */
package edu.tigers.sumatra.grsim;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.google.protobuf.UninitializedMessageException;

import edu.dhbw.mannheim.tigers.sumatra.model.data.GrSimCommands;
import edu.dhbw.mannheim.tigers.sumatra.model.data.GrSimPacket;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.basestation.ABaseStation;
import edu.tigers.sumatra.botmanager.bots.communication.ENetworkState;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.MatchCommand;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Creates the Protobuf commands that are to be sent to grSim
 * 
 * @author Peter Birkenkampf, TilmanS
 */
public class GrSimBaseStation extends ABaseStation implements IWorldFrameObserver
{
	
	
	private static final Logger			log					= Logger.getLogger(GrSimBaseStation.class.getName());
	private DatagramSocket					ds;
	private final Object						sync					= new Object();
	
	@Configurable(comment = "IP of computer running grSim", defValue = "127.0.0.1")
	private static String					ip;
	@Configurable(comment = "Port where grSim waits for bot commands", defValue = "20011")
	private static int						portYellow;
	@Configurable(comment = "Port where grSim sends status for bots", defValue = "30011")
	private static int						statusPortYellow;
	@Configurable(comment = "Port where grSim waits for bot commands", defValue = "20011")
	private static int						portBlue;
	@Configurable(comment = "Port where grSim sends status for bots", defValue = "30012")
	private static int						statusPortBlue;
	@Configurable(comment = "Use WP as feedback for control")
	private static boolean					useWpFeedback		= true;
	
	private SimpleWorldFrame				latestWorldFrame	= SimpleWorldFrame.createEmptyWorldFrame(0, 0);
	private ExtendedCamDetectionFrame	camFrame				= null;
	private ExtendedCamDetectionFrame	lastCamFrame		= null;
	private double								dt						= 0.001;
	private final Set<BotID>				knownBots			= new HashSet<>();
	
	
	static
	{
		ConfigRegistration.registerClass("botmgr", GrSimBaseStation.class);
	}
	
	
	/**
	 */
	public GrSimBaseStation()
	{
		super(EBotType.GRSIM);
	}
	
	
	/**
	 * FIXME this does not work yet
	 * 
	 * @return
	 */
	public GrSimStatus receive()
	{
		byte[] buffer2 = new byte[1];
		try
		{
			DatagramSocket ds = new DatagramSocket();
			// DatagramPacket dp = new DatagramPacket(buffer2, buffer2.length, InetAddress.getByName(ip),
			// statusPort);
			// ds.receive(dp);
			ds.close();
			return new GrSimStatus(buffer2);
		} catch (IOException e)
		{
			log.error("Could not send package to grSim", e);
		}
		return null;
	}
	
	
	@Override
	public void enqueueCommand(final BotID id, final ACommand cmd)
	{
		log.warn("Could not execute command for bot " + id + ": " + cmd.getType());
	}
	
	
	/**
	 * Construct grSim command and send it out
	 * 
	 * @param bot
	 */
	public void sendMatchCommand(final GrSimBot bot)
	{
		if (ds == null)
		{
			return;
		}
		
		MatchCommand matchCmd = bot.getMatchCtrl();
		float kickSpeed = (float) (matchCmd.getKickSpeed());
		GrSimCommands.grSim_Robot_Command.Builder robotCmd = GrSimCommands.grSim_Robot_Command.newBuilder();
		robotCmd.setId(bot.getBotId().getNumber());
		robotCmd.setKickmode(convertKickMode(bot, matchCmd.getMode()));
		robotCmd.setDisarmKicker(matchCmd.getMode() == EKickerMode.DISARM);
		robotCmd.setKickspeedx(kickSpeed);
		
		switch (matchCmd.getDevice())
		{
			case STRAIGHT:
				robotCmd.setKickspeedz(0);
				break;
			case CHIP:
				robotCmd.setKickspeedz((float) matchCmd.getKickSpeed());
				break;
		}
		
		robotCmd.setSpinner(matchCmd.getDribbleSpeed() > 0);
		robotCmd.setSpinnerspeed((float) matchCmd.getDribbleSpeed());
		
		switch (matchCmd.getSkill().getType())
		{
			case LOCAL_VELOCITY:
			case GLOBAL_POSITION:
			{
				IVector3 localVel = new Vector3();
				if (useWpFeedback)
				{
					ITrackedBot tBot;
					synchronized (sync)
					{
						tBot = latestWorldFrame.getBot(bot.getBotId());
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
				} else
				{
					List<CamRobot> bots;
					List<CamRobot> lastBots;
					synchronized (sync)
					{
						if (camFrame == null)
						{
							return;
						}
						if (lastCamFrame == null)
						{
							lastCamFrame = camFrame;
							return;
						}
						ExtendedCamDetectionFrame preFrame = lastCamFrame;
						lastCamFrame = camFrame;
						dt = (camFrame.gettCapture() - preFrame.gettCapture()) / 1e9;
						if ((dt > 0.5) || (dt < 0.0001))
						{
							return;
						}
						
						bots = camFrame.getRobotsYellow();
						lastBots = preFrame.getRobotsYellow();
						if (bot.getBotId().getTeamColor() == ETeamColor.BLUE)
						{
							bots = camFrame.getRobotsBlue();
							lastBots = preFrame.getRobotsBlue();
						}
					}
					for (CamRobot cBot : bots)
					{
						if (cBot.getRobotID() == bot.getBotId().getNumber())
						{
							IVector3 pos = new Vector3(cBot.getPos(), cBot.getOrientation());
							for (CamRobot lastBot : lastBots)
							{
								if (lastBot.getRobotID() == bot.getBotId().getNumber())
								{
									IVector3 lastPos = new Vector3(lastBot.getPos(), lastBot.getOrientation());
									IVector2 velxy = pos.getXYVector().subtractNew(lastPos.getXYVector());
									double velw = AngleMath.difference(pos.z(), lastPos.z());
									IVector3 vel = new Vector3(velxy, velw).multiplyNew(1e-3 / dt);
									localVel = bot.executeBotSkill(matchCmd.getSkill(), pos, vel, dt);
									break;
								}
							}
							break;
						}
					}
				}
				robotCmd.setWheelsspeed(false);
				robotCmd.setVelnormal((float) -localVel.x());
				robotCmd.setVeltangent((float) localVel.y());
				robotCmd.setVelangular((float) localVel.z());
				
				break;
			}
			case WHEEL_VELOCITY:
			{
				BotSkillWheelVelocity skill = (BotSkillWheelVelocity) matchCmd.getSkill();
				robotCmd.setWheelsspeed(true);
				robotCmd.setWheel1((float) skill.getWheelVelocity(1));
				robotCmd.setWheel2((float) skill.getWheelVelocity(2));
				robotCmd.setWheel3((float) skill.getWheelVelocity(3));
				robotCmd.setWheel4((float) skill.getWheelVelocity(0));
				robotCmd.setVelnormal(0);
				robotCmd.setVeltangent(0);
				robotCmd.setVelangular(0);
				break;
			}
			case MOTORS_OFF:
			default:
			{
				robotCmd.setWheelsspeed(false);
				robotCmd.setVelnormal(0);
				robotCmd.setVeltangent(0);
				robotCmd.setVelangular(0);
				break;
			}
		}
		
		GrSimCommands.grSim_Commands grSimCmd;
		try
		{
			grSimCmd = GrSimCommands.grSim_Commands.newBuilder()
					.setTimestamp(System.currentTimeMillis())
					.setIsteamyellow(bot.getBotId().getTeamColor() == ETeamColor.YELLOW)
					.addRobotCommands(robotCmd)
					.build();
		} catch (UninitializedMessageException err)
		{
			log.error("Invalid proto message", err);
			return;
		}
		
		
		GrSimPacket.grSim_Packet packet = GrSimPacket.grSim_Packet.newBuilder().setCommands(grSimCmd).build();
		byte[] buffer = packet.toByteArray();
		try
		{
			int port = bot.getBotId().getTeamColor() == ETeamColor.YELLOW ? portYellow : portBlue;
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port);
			synchronized (sync)
			{
				ds.send(dp);
			}
		} catch (IOException e)
		{
			log.error("Could not send package to grSim", e);
		}
	}
	
	
	/**
	 * @param mode
	 * @return
	 */
	private int convertKickMode(final GrSimBot bot, final EKickerMode mode)
	{
		switch (mode)
		{
			case ARM_AIM:
				if (bot.isTargetAngleReached())
				{
					return 1;
				}
				return 0;
			case ARM:
			case DISARM:
				return 1;
			case DRIBBLER:
				log.warn("Dribble-arm not implemented atm");
				break;
			case FORCE:
				return 0;
			case NONE:
				break;
			default:
				break;
		}
		return 0;
	}
	
	
	@Override
	public void onConnect()
	{
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFramePrioConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find WP module", err);
		}
		
		try
		{
			ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			cam.setBallReplacer(new GrSimBallReplacer(ip, portYellow));
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find cam module", err);
		}
		
		try
		{
			ds = new DatagramSocket(null);
		} catch (SocketException err)
		{
			log.error("Could not open datagram socket for grSimBot", err);
		}
	}
	
	
	@Override
	public void onDisconnect()
	{
		ds.close();
		ds = null;
		
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeWorldFramePrioConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find WP module", err);
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
				notifyBotOnline(new GrSimBot(botId, this));
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
		return "GRSIM BS";
	}
	
	
	@Override
	public synchronized void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
		camFrame = frame;
	}
}
