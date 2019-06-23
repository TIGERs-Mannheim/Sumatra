/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.05.2010
 * Author(s):
 * Maren Künemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.BallMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.IFilter;


/**
 * This class initiates the actual prediction and packs all results in a new {@link WorldFrame}
 */
public class WorldFramePacker
{
	private final PredictionContext	context;
	
	/** Used for first implementation with only one ball */
	private static final int BOT_ID = -1;
	
	private Vector2 lastBallPosInField = null;
	
	
	public WorldFramePacker(PredictionContext context)
	{
		this.context = context;
	}
	

	public WorldFrame pack(long currentFrameNumber, int camId)
	{
		double time = context.getLatestCaptureTimestamp();
		double delta = context.stepSize * context.stepCount;
		
		WorldFrame wFrame = buildWorldFrame(time + delta, currentFrameNumber, camId);

//		debugOutput(wFrame);
		// debugCSVoutput();
		// fileOutputWorldFrame(worldFrame);
		
		return wFrame;
	}
	

	private WorldFrame buildWorldFrame(double timestamp, long currentFrameNumber, int camId)
	{
		Map<Integer, TrackedTigerBot> tigers = new HashMap<Integer, TrackedTigerBot>();
		Map<Integer, TrackedBot> foes = new HashMap<Integer, TrackedBot>();
		TrackedBall trackedBall = null;
		
		IFilter ball = context.ball;
		if (ball != null)
		{
			BallMotionResult motion = (BallMotionResult) ball.getLookahead(context.stepCount);
			Vector2 position = new Vector2((float) motion.x, (float) motion.y);
			if (AIConfig.getGeometry().getField().isPointInShape(position))
			{
				trackedBall = TrackedBall.motionToTrackedBall(motion);
				lastBallPosInField = position;
			}
			else
			{
				if (lastBallPosInField != null)
				{
					trackedBall = new TrackedBall(BOT_ID, lastBallPosInField.x, 0.0f, 0.0f, lastBallPosInField.y, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false);
				}
				else
				{
					trackedBall = new TrackedBall(BOT_ID, AIConfig.INIT_VECTOR3.x, 0.0f, 0.0f, AIConfig.INIT_VECTOR3.y, 0.0f, 0.0f, AIConfig.INIT_VECTOR3.z, 0.0f, 0.0f, 0.0f, false);
				}
			}
		} else
		{
			trackedBall = new TrackedBall(BOT_ID, AIConfig.INIT_VECTOR3.x, 0.0f, 0.0f, AIConfig.INIT_VECTOR3.y, 0.0f, 0.0f, AIConfig.INIT_VECTOR3.z, 0.0f, 0.0f, 0.0f, false);
		}
		
		for (IFilter tiger : context.tigers.values())
		{
			tigers.put(	tiger.getId(), 
							TrackedTigerBot.motionToTrackedTigerBot(tiger.getId(),
									(RobotMotionResult_V2)tiger.getLookahead(context.stepCount),
									0,//height,
									1,//accuCharge,
									1,//kickerCharge,
									false));//defect);
		}
		
		for (IFilter food : context.food.values())
		{
			foes.put(	food.getId()+100,
							TrackedBot.motionToTrackedBot(food.getId()+100,
									(RobotMotionResult_V2)food.getLookahead(context.stepCount),
									0));//height) 
		}
//		debugOutput(new WorldFrame(foes, tigers, trackedBall, timestamp, currentFrameNumber, camId));
		return new WorldFrame(foes, tigers, trackedBall, timestamp, currentFrameNumber, camId);
	}

	public void fillMapsWithDummyObjects(Map<Integer, TrackedTigerBot> tigers, Map<Integer, TrackedBot> foes)
	{
		int nrOfTigers = tigers.size();
		while (nrOfTigers < context.minTigersInWorldFrame)
		{
//			TrackedTigerBot dummy = new TrackedTigerBot();
//			dummy.id		= 1000 + nrOfTigers;
//			dummy.pos.x = 0.0f;		//on the middelline
//			dummy.pos.y = 42000.0f;	//not on field
			TrackedTigerBot dummy = new TrackedTigerBot(1000+ nrOfTigers, new Vector2(0.0f, 42000.0f),new Vector2(0.0f, 0.0f),new Vector2(0.0f, 0.0f),0,0.0f,0.0f, 0.0f, 0.0f, 0.0f, 0.0f,true);
			tigers.put(dummy.id, dummy);
			nrOfTigers++;
			
		}
		
		int nrOfFood = foes.size();
		while (nrOfFood < context.minFoodInWorldFrame)
		{
//			TrackedBot dummy = new TrackedBot();
//			dummy.id		= 2000 + nrOfFood;
//			dummy.pos.x = 0.0f;		//on the middelline
//			dummy.pos.y = 42000.0f;	//not on field
			TrackedBot dummy = new TrackedBot(2000+ nrOfFood, new Vector2(0.0f, 42000.0f),new Vector2(0.0f, 0.0f),new Vector2(0.0f, 0.0f),0,0.0f,0.0f, 0.0f, 0.0f);
			foes.put(dummy.id, dummy);
			nrOfFood++;
		}
	}
	
	public void debugOutput(WorldFrame frame)
	{
		System.out.println("WorldFrame " + frame.id);
		if (frame.ball != null)
		{
			System.out.println("Ball: " + frame.ball.pos.x + " " + frame.ball.pos.y);
		} else
		{
			System.out.println("Ball: None");
		}
		for (TrackedTigerBot bot : frame.tigerBots.values())
		{
			System.out.println("Tiger " + bot.id + " " + bot.pos.x + " " + bot.pos.y + " " + bot.angle);
		}
		
		for (TrackedBot bot : frame.foeBots.values())
		{
			System.out.println("Food " + bot.id + " " + bot.pos.x + " " + bot.pos.y + " " + bot.angle);
		}
	}
	

	// static FileAppender ballFile = new FileAppender("WorldFrame_ball");
	// static Map<Integer,FileAppender> tigerFiles = Collections.synchronizedMap(new HashMap<Integer, FileAppender>());
	// static Map<Integer,FileAppender> foodFiles = Collections.synchronizedMap(new HashMap<Integer, FileAppender>());
	//
	// public static synchronized void fileOutputWorldFrame(WorldFrame[] worldFrame)
	// {
	// if (worldFrame[0].ball != null)
	// {
	// for (WorldFrame frame: worldFrame)
	// {
	// ballFile.addTime(frame.time);
	// ballFile.addFloat(frame.ball.xPos);
	// ballFile.addFloat(frame.ball.yPos);
	// }
	// ballFile.done();
	// }
	//
	// for (TrackedBot bot: worldFrame[0].foeBots.values())
	// {
	// FileAppender a = foodFiles.get(bot.id);
	// if (a == null)
	// {
	// a = new FileAppender("WorldFrame_Food_"+bot.id);
	// foodFiles.put(bot.id, a);
	// }
	// for (WorldFrame frame: worldFrame)
	// {
	// a.addTime(frame.time);
	// a.addFloat(frame.foeBots.get(bot.id).xPos);
	// a.addFloat(frame.foeBots.get(bot.id).yPos);
	// a.addFloat(frame.foeBots.get(bot.id).angle);
	// }
	// a.done();
	// }
	//
	// for (TrackedTigerBot bot: worldFrame[0].tigerBots.values())
	// {
	// FileAppender a = tigerFiles.get(bot.id);
	// if (a == null)
	// {
	// a = new FileAppender("WorldFrame_Tiger_"+bot.id);
	// tigerFiles.put(bot.id, a);
	// }
	// for (int frame= 0; frame < worldFrame.length; frame++ )
	// {
	// a.addTime(worldFrame[frame].time);
	// a.addFloat(worldFrame[frame].tigerBots.get(bot.id).xPos);
	// a.addFloat(worldFrame[frame].tigerBots.get(bot.id).yPos);
	// a.addFloat(worldFrame[frame].tigerBots.get(bot.id).angle);
	// }
	// a.done();
	// }
	//
	// }
	
//	public void debugCSVoutput()
//	{
//		// Flags
//		// give the id of the object to produce output for, -1 means don't trac anything of this kind
//		// only one can be tracked, a ball, a tiger or a food
//		int BallId = 0;
//		int TigerId = -1;
//		int FoodId = -1;
//		
//		// output flags
//		boolean plotX = false;
//		boolean plotY = true;
//		boolean plotR = false;
//		
//		ObjectData obj = null;
//		obj = context.balls.get(BallId);
//		
//		if (obj == null)
//		{
//			obj = context.tigers.get(TigerId);
//		}
//		
//		if (obj == null)
//		{
//			obj = context.food.get(FoodId);
//		}
//		
//		if (obj != null)
//		{
//			StringBuilder s = new StringBuilder();
//			s.append("" + ((double) obj.positions.peekFirst().timestamp) / 1000000000l);
//			if (plotX)
//			{
//				s.append("\t" + obj.positions.peekFirst().pos.x);
//			}
//			if (plotY)
//			{
//				s.append("\t" + obj.positions.peekFirst().pos.y);
//			}
//			if (plotR && obj.positions.peekFirst() instanceof CamInfoBot)
//			{
//				s.append("\t" + ((CamInfoBot) obj.positions.peekFirst()).a);
//			}
//			for (WorldFrame frame : worldFrames)
//			{
//				s.append("\t" + ((double) frame.time) / 1000000000l);
//				ATrackedObject tracked = null;
//				tracked = (frame.ball.id == frame.ball.id) ? frame.ball : null;
//				if (tracked == null)
//				{
//					tracked = frame.foeBots.get(FoodId);
//				}
//				if (tracked == null)
//				{
//					tracked = frame.tigerBots.get(TigerId);
//				}
//				if (tracked == null)
//				{
//					return;
//				}
//				if (plotX)
//				{
//					s.append("\t" + tracked.pos.x);
//				}
//				if (plotY)
//				{
//					s.append("\t" + tracked.pos.y);
//				}
//				if (plotR && tracked instanceof TrackedBot)
//				{
//					s.append("\t" + ((TrackedBot) tracked).angle);
//				}
//				
//			}
//			System.out.println(s);
//		}
//	}
}
