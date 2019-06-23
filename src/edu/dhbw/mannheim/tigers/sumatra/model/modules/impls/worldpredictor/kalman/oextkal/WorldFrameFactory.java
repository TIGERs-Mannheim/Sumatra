/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.11.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal;

import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.DummyBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.GrSimBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.SumatraBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;


/**
 * Factory which creates a list of {@link WorldFrame}s with random positioned bots.
 * 
 * @author Oliver Steinbrecher
 */
public class WorldFrameFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** random without seed for reproducibility */
	private static final Random	RND	= new Random();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public WorldFrameFactory()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param frameNumber
	 * @return
	 */
	public WorldFrame createWorldFrame(final long frameNumber)
	{
		return new WorldFrame(createSimpleWorldFrame(frameNumber), ETeamColor.YELLOW, false);
	}
	
	
	/**
	 * Creates a new WorldFrame with random positioned bots.
	 * 
	 * @param frameNumber the id of the {@link WorldFrame}.
	 * @return
	 */
	public SimpleWorldFrame createSimpleWorldFrame(final long frameNumber)
	{
		final IBotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
		
		for (int i = 0; i < 10; i++)
		{
			BotID idF = BotID.createBotId(i, ETeamColor.BLUE);
			bots.put(idF, createBot(idF, ETeamColor.BLUE));
			
			BotID idT = BotID.createBotId(i, ETeamColor.YELLOW);
			bots.put(idT, createBot(idT, ETeamColor.YELLOW));
		}
		
		final TrackedBall ball = new TrackedBall(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0,
				true);
		
		WorldFramePrediction wfp = new FieldPredictor(bots.values(), ball).create();
		SimpleWorldFrame swf = new SimpleWorldFrame(bots, ball, frameNumber, wfp);
		swf.setCamFps(210);
		swf.setWfFps(60);
		return swf;
	}
	
	
	/**
	 * Creates a new WorldFrame without bots
	 * 
	 * @param frameNumber the id of the {@link WorldFrame}.
	 * @return
	 */
	public static SimpleWorldFrame createEmptyWorldFrame(final long frameNumber)
	{
		return SimpleWorldFrame.createEmptyWorldFrame(frameNumber);
	}
	
	
	/**
	 * Create bot with random positions
	 * 
	 * @param id
	 * @param color
	 * @return bot
	 */
	public TrackedTigerBot createBot(final BotID id, final ETeamColor color)
	{
		float x = (RND.nextFloat() * AIConfig.getGeometry().getFieldLength())
				- (AIConfig.getGeometry().getFieldLength() / 2);
		float y = (RND.nextFloat() * AIConfig.getGeometry().getFieldWidth())
				- (AIConfig.getGeometry().getFieldWidth() / 2);
		final IVector2 pos = new Vector2(x, y);
		
		ABot bot;
		switch (id.getNumber())
		{
			case 6:
				bot = new SumatraBot(id);
				bot.setControlledBy("someone");
				break;
			case 7:
				bot = new GrSimBot(id);
				bot.setControlledBy("someone");
				break;
			case 8:
				bot = new TigerBot(id);
				bot.setControlledBy("someone");
				break;
			case 9:
				bot = new TigerBotV3(id, null);
				bot.setControlledBy("someone");
				break;
			default:
				DummyBot dbot = new DummyBot();
				dbot.setAvail2Ai(true);
				bot = dbot;
				break;
		}
		return new TrackedTigerBot(id, pos, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0, 0, 0, bot,
				color);
	}
}
