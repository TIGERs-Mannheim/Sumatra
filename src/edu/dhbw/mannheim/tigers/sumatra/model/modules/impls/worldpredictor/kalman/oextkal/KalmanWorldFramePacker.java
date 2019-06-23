/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.05.2010
 * Author(s):
 * Maren K�nemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.AWorldFramePacker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.BallMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.RobotMotionResult_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter.IFilter;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This class initiates the actual prediction and packs all results in a new {@link SimpleWorldFrame}
 */
public class KalmanWorldFramePacker extends AWorldFramePacker
{
	@SuppressWarnings("unused")
	private static final Logger				log						= Logger.getLogger(KalmanWorldFramePacker.class
																							.getName());
	
	private static final int					DEFAULT_BOT_HEIGHT	= 150;
	
	
	private final PredictionContext			context;
	private TrackedBall							lastBallPosInField	= new TrackedBall(AVector3.ZERO_VECTOR,
																							AVector3.ZERO_VECTOR,
																							AVector3.ZERO_VECTOR, 0, false);
	
	@Configurable(comment = "override WP data with status from bot? (pos,vel)")
	private static boolean						useBotStatus			= false;
	
	private List<MergedCamDetectionFrame>	camFrames				= new ArrayList<>();
	
	
	/**
	 * @param context
	 */
	public KalmanWorldFramePacker(final PredictionContext context)
	{
		this.context = context;
	}
	
	
	/**
	 * @param frame
	 * @return
	 */
	public SimpleWorldFrame pack(final MergedCamDetectionFrame frame)
	{
		final BotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
		TrackedBall trackedBall;
		
		final IFilter ball = context.getBall();
		final BallMotionResult motion = (BallMotionResult) ball.getLookahead(context.getStepCount());
		final IVector3 position = new Vector3((float) motion.x, (float) motion.y, 0);
		if (AIConfig.getGeometry().getFieldWReferee().isPointInShape(position.getXYVector()))
		{
			trackedBall = TrackedBall.motionToTrackedBall(motion);
			lastBallPosInField = trackedBall;
		} else
		{
			trackedBall = lastBallPosInField;
		}
		
		if (useBotStatus)
		{
			for (ABot bot : botManager.getAllBots().values())
			{
				final TrackedTigerBot trackedTigerBot;
				final BotID botID = BotID.createBotId(bot.getBotID().getNumber(), bot.getColor());
				if (bot.getType() == EBotType.TIGER_V3)
				{
					TigerBotV3 botV3 = (TigerBotV3) bot;
					if (!botV3.getLatestFeedbackCmd().isPositionValid() || !botV3.getLatestFeedbackCmd().isVelocityValid())
					{
						continue;
					}
					IVector2 pos = botV3.getLatestFeedbackCmd().getPosition().multiply(1000);
					float orient = botV3.getLatestFeedbackCmd().getOrientation();
					IVector2 vel = botV3.getLatestFeedbackCmd().getVelocity();
					float aVel = botV3.getLatestFeedbackCmd().getAngularVelocity();
					IVector2 acc = botV3.getLatestFeedbackCmd().getAcceleration();
					float aAcc = botV3.getLatestFeedbackCmd().getAngularAcceleration();
					
					trackedTigerBot = new TrackedTigerBot(botID, pos, vel, acc, DEFAULT_BOT_HEIGHT, orient,
							aVel,
							aAcc, 1, bot, bot.getColor());
				} else
				{
					continue;
				}
				bots.put(bot.getBotID(), trackedTigerBot);
			}
		}
		
		for (final IFilter filterBot : context.getYellowBots().values())
		{
			botLastVisible.put(BotID.createBotId(filterBot.getId(), ETeamColor.YELLOW), SumatraClock.nanoTime());
			processBot(filterBot, bots, ETeamColor.YELLOW, trackedBall);
		}
		for (final IFilter filterBot : context.getBlueBots().values())
		{
			botLastVisible.put(BotID.createBotId(filterBot.getId(), ETeamColor.BLUE), SumatraClock.nanoTime());
			processBot(filterBot, bots, ETeamColor.BLUE, trackedBall);
		}
		WorldFramePrediction wfp = new FieldPredictor(bots.values(), trackedBall).create();
		
		setVisibility(bots.getContentMap());
		
		setVisibilityBotmanager(bots.getContentMap());
		
		SimpleWorldFrame swf = new SimpleWorldFrame(bots, trackedBall, frame.getFrameNumber(), wfp);
		camFrames.add(frame);
		if (camFrames.size() > 10)
		{
			camFrames.remove(0);
		}
		swf.setCamFrames(new ArrayList<>(camFrames));
		return swf;
	}
	
	
	private void processBot(final IFilter filterBot, final IBotIDMap<TrackedTigerBot> bots, final ETeamColor color,
			final TrackedBall trackedBall)
	{
		final BotID botID = BotID.createBotId(filterBot.getId(), color);
		
		TrackedTigerBot trackedTigerBot;
		if (bots.containsKey(botID))
		{
			trackedTigerBot = bots.get(botID);
		} else
		{
			final ABot bot = botManager.getAllBots().get(botID);
			final RobotMotionResult_V2 motion = (RobotMotionResult_V2) filterBot.getLookahead(context.getStepCount());
			
			trackedTigerBot = TrackedTigerBot.motionToTrackedBot(botID, motion, DEFAULT_BOT_HEIGHT, bot, color);
			bots.put(botID, trackedTigerBot);
		}
		
		boolean ballContact = ballContact(trackedTigerBot, trackedBall);
		trackedTigerBot.setBallContact(ballContact);
	}
}
