/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.exporter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.MulticastUDPTransmitter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class WorldFrameSender extends AModule implements IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger
			.getLogger(WorldFrameSender.class.getName());
	/** */
	public static final String MODULE_TYPE = "WorldFrameSender";
	/** */
	public static final String MODULE_ID = "wfexporter";
	
	private static final int LOCAL_PORT = 42000;
	private static final int PORT = 42001;
	private static final String ADDRESS = "224.5.23.3";
	private static final boolean ADD_NOISE = false;
	
	private static final double MIN_DT = 0.005;
	private final double delay;
	private MulticastUDPTransmitter transmitter;
	private long tLast = 0;
	private Random rnd = new Random(0);
	
	private List<SumatraWfExport.WorldFrame.Builder> buffer = new LinkedList<>();
	
	static
	{
		ConfigRegistration.registerClass("export", WorldFrameSender.class);
	}
	
	
	/**
	 * @param config
	 */
	public WorldFrameSender(final SubnodeConfiguration config)
	{
		delay = config.getDouble("delay", 0);
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		transmitter = new MulticastUDPTransmitter(LOCAL_PORT, ADDRESS, PORT);
	}
	
	
	@Override
	public void deinitModule()
	{
		// empty
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find WP module", e);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find WP module", e);
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		long tNow = wFrameWrapper.getSimpleWorldFrame().getTimestamp();
		double dt = (tNow - tLast) / 1e9;
		if (dt < MIN_DT)
		{
			return;
		}
		tLast = tNow;
		
		SimpleWorldFrame swf = wFrameWrapper.getSimpleWorldFrame();
		SumatraWfExport.WorldFrame.Builder wfBuilder = SumatraWfExport.WorldFrame.newBuilder();
		wfBuilder.setFrameId(swf.getId()).setTimestamp(swf.getTimestamp());
		
		SumatraWfExport.Ball.Builder ball = SumatraWfExport.Ball.newBuilder();
		ball.setPos(convertVector(swf.getBall().getPos3().multiplyNew(1e-3)))
				.setVel(convertVector(swf.getBall().getVel3()));
		wfBuilder.setBall(ball);
		
		double std = 0.1;
		double aStd = 0.3;
		for (ITrackedBot tBot : swf.getBots().values())
		{
			SumatraWfExport.Bot.Builder bot = SumatraWfExport.Bot.newBuilder();
			bot.setId(tBot.getBotId().getNumber())
					.setTeamColor(tBot.getBotId().getTeamColor() == ETeamColor.BLUE
							? SumatraWfExport.TeamColor.BLUE : SumatraWfExport.TeamColor.YELLOW);
			bot.setPos(convertVector(tBot.getPos().multiplyNew(1e-3), tBot.getOrientation()));
			IVector2 vel = tBot.getVel();
			double aVel = tBot.getAngularVel();
			if (ADD_NOISE)
			{
				vel = vel.addNew(Vector2.fromXY(rnd.nextGaussian() * std, rnd.nextGaussian() * std));
				aVel += (rnd.nextGaussian() * aStd);
			}
			
			bot.setVel(convertVector(vel, aVel));
			wfBuilder.addBots(bot);
		}
		
		buffer.add(wfBuilder);
		
		List<SumatraWfExport.WorldFrame.Builder> toBeRem = new ArrayList<>();
		for (SumatraWfExport.WorldFrame.Builder wf : buffer)
		{
			if ((swf.getTimestamp() - (delay * 1E9) - wf.getTimestamp()) < 0)
			{
				break;
			}
			wf.setTimestamp(swf.getTimestamp());
			toBeRem.add(wf);
			transmitter.send(wf.build().toByteArray());
		}
		buffer.removeAll(toBeRem);
	}
	
	
	private SumatraWfExport.Vector3 convertVector(final IVector3 in)
	{
		SumatraWfExport.Vector3.Builder out = SumatraWfExport.Vector3.newBuilder();
		return out.setX(in.x()).setY(in.y()).setZ(in.z()).build();
	}
	
	
	private SumatraWfExport.Vector3 convertVector(final IVector2 in, final double z)
	{
		SumatraWfExport.Vector3.Builder out = SumatraWfExport.Vector3.newBuilder();
		return out.setX(in.x()).setY(in.y()).setZ(z).build();
	}
}
