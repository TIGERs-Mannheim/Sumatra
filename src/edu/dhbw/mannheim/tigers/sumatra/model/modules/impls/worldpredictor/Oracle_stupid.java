/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s):
 * Maren Künemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObservable;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * This class is the core of Sumatras prediction-system. First it dispatches the incoming data, then it initiates their
 * processing. Furthermore, it handles the lifecycle of the whole module
 * 
 * @author Gero
 * 
 */
public class Oracle_stupid extends AWorldPredictor implements IWorldPredictorObservable
{

	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger									log						= Logger.getLogger(getClass());
	

	private final SumatraModel							model						= SumatraModel.getInstance();
	
	
	// CamDetectionFrame-Consumer
	private ACam											cam						= null;
	
	private CamBall ball = null;
	private ArrayList<CamRobot> friends  = new ArrayList<CamRobot>(5);
	private ArrayList<CamRobot> foes     = new ArrayList<CamRobot>(5);
	
	private TrackedBall wBall = null;
	private ArrayList<TrackedTigerBot> wFriends = new ArrayList<TrackedTigerBot>(5);
	private ArrayList<TrackedBot> wFoes= new ArrayList<TrackedBot>(5);
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	public Oracle_stupid(SubnodeConfiguration properties)
	{
		super();
	}
	

	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule()
	{		
		try
		{
			cam = (ACam) model.getModule(ACam.MODULE_ID);
			cam.setCamFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Unable to find module '" + ACam.MODULE_ID + "'!");
		}
		
		log.info("Initialized.");
		
	}
	

	@Override
	public void startModule()
	{
		
		if (consumer == null)
		{
			log.warn("No consumer setted!! WorldPredictor will go to bed.");
			return;
		}
		
		log.info("Started.");
	}
	

	@Override
	public void deinitModule()
	{
		ball = null;
		friends.clear();
		foes.clear();

		log.info("Deinitialized.");
	}
	

	@Override
	public void stopModule()
	{
		consumer = null;
		
		if (cam != null)
		{
			cam.setCamFrameConsumer(null);
			cam = null;
		}
		
		synchronized (observers)
		{
			observers.clear();
		}
		
		synchronized (functionalObservers)
		{
			functionalObservers.clear();
		}
		
		log.info("Stopped.");
	}
	
	private float noise(float max)
	{
		float r = (float) ((Math.random()-0.5)*2);
		return r*max;
	}
	
	private List<CamBall> noisyBalls(List<CamBall> b)
	{
		List<CamBall> l = new ArrayList<CamBall>();
		for (CamBall ball: b)
		{
			l.add(new CamBall(ball.confidence, ball.area, ball.pos.x + noise(WPConfig.NOISE_S), ball.pos.y + noise(WPConfig.NOISE_S), ball.pos.z, ball.pixelX, ball.pixelY));
		}
		return l;
	}
	
	private List<CamRobot> noisyBots(List<CamRobot> bots)
	{
		List<CamRobot> l = new ArrayList<CamRobot>();
		for (CamRobot bot: bots)
		{
			l.add(new CamRobot(bot.confidence, bot.robotID, bot.pos.x + noise(WPConfig.NOISE_S), bot.pos.y + noise(WPConfig.NOISE_S), bot.orientation+noise(WPConfig.NOISE_R), bot.pixelX, bot.pixelY, bot.height));
		}
		return l;		
	}
	
	private CamDetectionFrame addNoise(CamDetectionFrame f)
	{
		long tCapture = f.tCapture;
		long tSent = f.tSent;
		long tReceived = f.tReceived;
		int cameraId = f.cameraId;
		long frameNumber = f.frameNumber;
		double fps = f.fps;
		List<CamBall> balls = noisyBalls(f.balls);
		List<CamRobot> tigers = noisyBots(f.robotsTigers);
		List<CamRobot> enemies = noisyBots(f.robotsEnemies);
		CamDetectionFrame n = new CamDetectionFrame(tCapture, tSent, tReceived, cameraId, frameNumber, fps , balls , tigers , enemies);
		return n;
	}

	@Override
	public void onNewCamDetectionFrame(CamDetectionFrame camDetectionFrame)
	{
		if (WPConfig.ADD_NOISE)
		{
			camDetectionFrame = addNoise(camDetectionFrame);
		}
		
		WorldFrame f = getWorldFrame(camDetectionFrame);
		notifyFunctionalNewWorldFrame(f);
		notifyNewWorldFrame(f);
			
	}
	
	private void updateBall(CamBall ball)
	{
		if (this.ball == null)
		{
			this.ball = ball;
			TrackedBall tb = new TrackedBall(0, ball.pos.x, 0, 0, ball.pos.y, 0, 0, 0, 0, 0, 1, true);
			wBall = tb;
		}
		else
		{
			TrackedBall tb = new TrackedBall(0, ball.pos.x, 0, 0, ball.pos.y, 0, 0, 0, 0, 0, 1, true);
			wBall = tb;
		}
	}
	
	private void updateFriend(CamRobot friend)
	{
		CamRobot oldRobot = null;
		TrackedTigerBot bot = null;
		int listId = 0;
		for (int i = 0; i < friends.size(); i++)
		{
			if (friends.get(i).robotID == friend.robotID)
			{
				oldRobot = friends.get(i);
				listId = i;
			}
		}
		
		if (oldRobot == null)
		{
			friends.add(friend);
			bot = new TrackedTigerBot(friend.robotID, friend.pos,
					new Vector2(0,0),// vel
					new Vector2(0,0),
					20, 
					friend.orientation,
					0, 0, 1, 0, 0, false);	
		}
		else
		{
			friends.set(listId, friend);
			bot = new TrackedTigerBot(friend.robotID, friend.pos,
					new Vector2(0,0),// vel
					new Vector2(0,0),
					20, 
					friend.orientation,
					0, 0, 1, 0, 0, false);			
		}
		
		if (bot != null)
		{
			for (int i = 0; i < wFriends.size(); i++)
			{
				if (bot.id == wFriends.get(i).id)
				{
					wFriends.set(i, bot);
					bot = null;
				}
			}
			if (bot != null)
			{
				wFriends.add(bot);
			}
		}
		
	}
	
	private void updateFoe(CamRobot foe)
	{
		CamRobot oldRobot = null;
		TrackedBot bot = null;
		int listId = 0;
		for (int i = 0; i < foes.size(); i++)
		{
			if (foes.get(i).robotID == foe.robotID)
			{
				oldRobot = foes.get(i);
				listId = i;
			}
		}
		
		if (oldRobot == null)
		{
			foes.add(foe);
			bot = new TrackedBot(foe.robotID, foe.pos,
					new Vector2(0,0),// vel
					new Vector2(0,0),
					20, 
					foe.orientation,
					0, 0, 1);	
		}
		else
		{
			foes.set(listId, foe);
			bot = new TrackedBot(foe.robotID, foe.pos,
					new Vector2(0,0),// vel
					new Vector2(0,0),
					20, 
					foe.orientation,
					0, 0, 1);			
		}
		
		if (bot != null)
		{
			for (int i = 0; i < wFoes.size(); i++)
			{
				if (bot.id == wFoes.get(i).id)
				{
					wFoes.set(i, bot);
					bot = null;
				}
			}
			if (bot != null)
			{
				wFoes.add(bot);
			}
		}
		
	}
	

	private WorldFrame getWorldFrame(CamDetectionFrame frame)
	{
		if (frame.balls.size() > 0)
			updateBall(frame.balls.get(0));
		for (int i = 0; i < frame.robotsTigers.size(); i++)
		{
			updateFriend(frame.robotsTigers.get(i));
		}
		for (int i = 0; i < frame.robotsEnemies.size(); i++)
		{
			updateFoe(frame.robotsEnemies.get(i));
		}
		
		HashMap<Integer, TrackedBot> foeBots = new  HashMap<Integer, TrackedBot>(5);
		HashMap<Integer, TrackedTigerBot> tigerBots = new  HashMap<Integer, TrackedTigerBot>(5);
		
		for (int i = 0; i < wFoes.size();i++)
			foeBots.put(wFoes.get(i).id, wFoes.get(i));
		for (int i = 0; i < wFriends.size();i++)
			tigerBots.put(wFriends.get(i).id, wFriends.get(i));
		
		WorldFrame wf = new WorldFrame(foeBots, tigerBots, wBall, frame.tCapture+150*10e6, frame.frameNumber, frame.cameraId);
		
		
		return wf;
	}


	@Override
	public void onBotAdded(ABot bot)
	{
	}

	@Override
	public void onBotRemoved(ABot bot)
	{
	}

	@Override
	public void onBotIdChanged(int oldId, int newId)
	{
	}
	
	
	@Override
	public void notifyFunctionalNewWorldFrame(WorldFrame wFrame)
	{
		synchronized (functionalObservers)
		{
			for (IWorldPredictorObserver observer : functionalObservers)
			{
				observer.onNewWorldFrame(new WorldFrame(wFrame));
			}
		}
	}
	

	@Override
	public void notifyNewWorldFrame(WorldFrame wFrame)
	{
		synchronized (observers)
		{
			for (IWorldPredictorObserver observer : observers)
			{
				observer.onNewWorldFrame(new WorldFrame(wFrame));
			}
		}
	}
}
