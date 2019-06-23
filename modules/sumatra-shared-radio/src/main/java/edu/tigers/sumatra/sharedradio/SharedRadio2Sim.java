/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sharedradio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.RadioProtocolCommandOuterClass.RadioProtocolCommand;
import edu.tigers.sumatra.RadioProtocolWrapperOuterClass.RadioProtocolWrapper;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SharedRadio2Sim extends AModule
{
	/** */
	public static final String		MODULE_TYPE		= "SharedRadio2Sim";
	/** */
	public static final String		MODULE_ID		= "SharedRadio2Sim";
	
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(SharedRadio2Sim.class.getName());
	
	private DatagramSocket			socket;
	private Thread						receiverThread	= null;
	private ETeamColor				teamColor		= ETeamColor.YELLOW;
	
	private ABotManager				botMgr;
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public SharedRadio2Sim(final SubnodeConfiguration subnodeConfiguration)
	{
	}
	
	
	@Override
	public void deinitModule()
	{
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		try
		{
			socket = new DatagramSocket(10010);
		} catch (SocketException e)
		{
			log.error("", e);
		}
		
		try
		{
			botMgr = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
		} catch (ModuleNotFoundException e)
		{
			log.error("", e);
		}
		
		start();
	}
	
	
	@Override
	public void stopModule()
	{
		stop();
		socket.close();
	}
	
	
	/**
	 * 
	 */
	public void start()
	{
		if (receiverThread != null)
		{
			stop();
		}
		
		receiverThread = new Thread(new Receiver(), "ReceiverUDP");
		
		receiverThread.start();
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
		if (receiverThread == null)
		{
			return;
		}
		
		receiverThread.interrupt();
		try
		{
			receiverThread.join(100);
		} catch (final InterruptedException err)
		{
			Thread.currentThread().interrupt();
		}
		
		receiverThread = null;
	}
	
	
	protected class Receiver implements Runnable
	{
		@Override
		public void run()
		{
			if (socket == null)
			{
				log.error("Cannot start a receiver on a null socket");
				return;
			}
			
			Thread.currentThread().setName("SharedRadioReceiver");
			
			byte[] buf;
			
			try
			{
				buf = new byte[socket.getReceiveBufferSize()];
			} catch (final SocketException err)
			{
				log.error("Could not get receive buffer size", err);
				return;
			}
			
			log.debug("Receive buffer size set to: " + buf.length);
			
			while (!Thread.currentThread().isInterrupted())
			{
				final DatagramPacket packet = new DatagramPacket(buf, buf.length);
				
				try
				{
					socket.receive(packet);
					
					final ByteArrayInputStream packetIn = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					
					// Translate
					final RadioProtocolWrapper wrapper;
					try
					{
						wrapper = RadioProtocolWrapper.parseFrom(packetIn);
					} catch (Exception err)
					{
						log.error("invalid protobuf message", err);
						continue;
					}
					
					for (RadioProtocolCommand cmd : wrapper.getCommandList())
					{
						int id = cmd.getRobotId();
						IVector2 vel_xy = Vector2.fromXY(-cmd.getVelocityY(), cmd.getVelocityX());
						double vel_w = cmd.getVelocityR();
						
						for (ABot sBot : botMgr.getAllBots().values())
						{
							if ((sBot.getBotId().getNumber() == id) && (sBot.getBotId().getTeamColor() == teamColor))
							{
								MoveConstraints mc = new MoveConstraints(sBot.getBotParams().getMovementLimits());
								ABotSkill skill = new BotSkillLocalVelocity(vel_xy, vel_w, mc);
								sBot.getMatchCtrl().setSkill(skill);
								break;
							}
						}
					}
					
					
				} catch (final SocketException e)
				{
					log.info("UDP transceiver terminating", e);
					Thread.currentThread().interrupt();
				} catch (final IOException err)
				{
					log.error("Some IOException", err);
				} catch (final Exception err)
				{
					log.warn("Unexpected exception! See stacktrace", err);
				}
			}
		}
	}
	
}
