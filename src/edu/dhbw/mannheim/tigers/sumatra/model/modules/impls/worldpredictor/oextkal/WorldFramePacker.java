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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.BallMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.IFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This class initiates the actual prediction and packs all results in a new {@link WorldFrame}
 */
public class WorldFramePacker
{
	// Logger
	private static final Logger			log							= Logger.getLogger(WorldFramePacker.class.getName());
	
	
	private static final int				DEFAULT_HEIGHT				= 0;
	private static final float				BOT_FRONT_OFFSET_HAS		= 5;
	private static final float				BALL_POSS_TOLERANCE_HAS	= 80;
	private static final float				BOT_FRONT_OFFSET_GET		= 5;
	private static final float				BALL_POSS_TOLERANCE_GET	= 30;
	
	
	private final PredictionContext		context;
	private TrackedBall						lastBallPosInField		= null;
	
	private final Map<BotID, Boolean>	ballContactLastFrame;
	private final Map<BotID, Long>		botLastVisible				= new HashMap<BotID, Long>();
	
	@Configurable(comment = "override WP data with status from bot? (pos,vel)")
	private static boolean					useBotStatus				= false;
	
	private ABotManager						botManager;
	
	
	/**
	 * @param context
	 */
	public WorldFramePacker(final PredictionContext context)
	{
		this.context = context;
		ballContactLastFrame = new HashMap<BotID, Boolean>();
		try
		{
			botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.fatal("Botmanager module not found!!");
		}
	}
	
	
	/**
	 * @param currentFrameNumber
	 * @param camId
	 * @return
	 */
	public SimpleWorldFrame pack(final long currentFrameNumber, final int camId)
	{
		final double time = context.getLatestCaptureTimestamp();
		final double delta = context.stepSize * context.stepCount;
		
		SimpleWorldFrame wFrame = buildWorldFrame(time + delta, currentFrameNumber, camId);
		return wFrame;
	}
	
	
	private SimpleWorldFrame buildWorldFrame(final double timestamp, final long currentFrameNumber, final int camId)
	{
		final BotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
		TrackedBall trackedBall;
		
		final IFilter ball = context.ball;
		if (ball != null)
		{
			final BallMotionResult motion = (BallMotionResult) ball.getLookahead(context.stepCount);
			final IVector3 position = new Vector3((float) motion.x, (float) motion.y, 0);
			if (AIConfig.getGeometry().getFieldWReferee().isPointInShape(position.getXYVector()))
			{
				trackedBall = TrackedBall.motionToTrackedBall(motion);
				lastBallPosInField = trackedBall;
			} else
			{
				if (lastBallPosInField != null)
				{
					trackedBall = lastBallPosInField;
				} else
				{
					trackedBall = new TrackedBall(GeoMath.INIT_VECTOR3, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0, false);
				}
			}
		} else
		{
			trackedBall = new TrackedBall(GeoMath.INIT_VECTOR3, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0, false);
		}
		
		if (useBotStatus)
		{
			for (ABot bot : botManager.getAllBots().values())
			{
				if (bot.getType() != EBotType.TIGER_V2)
				{
					continue;
				}
				TigerBotV2 botV2 = (TigerBotV2) bot;
				
				if (!botV2.getLogMovement())
				{
					continue;
				}
				
				TigerSystemStatusV2 statusV2 = botV2.getSystemStatusV2();
				// TODO WP: this is a hack, the WP still spends time predicting motions for tiger bots which is not used :/
				if (!statusV2.isPositionUpdated() || !(statusV2.isVelocityUpdated()) || !(statusV2.isAccelerationUpdated()))
				{
					continue;
				}
				
				if (bot.getNetworkState() != ENetworkState.ONLINE)
				{
					continue;
				}
				
				IVector2 pos = statusV2.getPosition().multiply(1000.0f);
				float orient = statusV2.getOrientation();
				
				IVector2 vel = statusV2.getVelocity();
				float aVel = statusV2.getAngularVelocity();
				
				IVector2 acc = statusV2.getAcceleration();
				float aAcc = statusV2.getAngularAcceleration();
				
				
				final BotID botID = BotID.createBotId(bot.getBotID().getNumber(), bot.getColor());
				TrackedTigerBot trackedTigerBot = new TrackedTigerBot(botID, pos, vel, acc, DEFAULT_HEIGHT, orient, aVel,
						aAcc, 1, bot, bot.getColor());
				
				Long lastVisible = botLastVisible.get(botID);
				if ((lastVisible != null) && (TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - lastVisible) >= 5))
				{
					// The position reported by the bot may not be correct anymore
					trackedTigerBot.setVisible(false);
				}
				
				bots.put(bot.getBotID(), trackedTigerBot);
			}
		}
		
		for (final IFilter filterBot : context.yellowBots.values())
		{
			botLastVisible.put(BotID.createBotId(filterBot.getId(), ETeamColor.YELLOW), System.nanoTime());
			processBot(filterBot, bots, ETeamColor.YELLOW, trackedBall);
		}
		for (final IFilter filterBot : context.blueBots.values())
		{
			botLastVisible.put(BotID.createBotId(filterBot.getId(), ETeamColor.BLUE), System.nanoTime());
			processBot(filterBot, bots, ETeamColor.BLUE, trackedBall);
		}
		WorldFramePrediction wfp = new FieldPredictor(bots.values(), trackedBall).create();
		
		return new SimpleWorldFrame(bots, trackedBall, timestamp, currentFrameNumber, camId, wfp);
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
			final RobotMotionResult_V2 motion = (RobotMotionResult_V2) filterBot.getLookahead(context.stepCount);
			
			trackedTigerBot = TrackedTigerBot.motionToTrackedBot(botID, motion, DEFAULT_HEIGHT, bot, color);
			bots.put(botID, trackedTigerBot);
		}
		
		boolean ballContact = ballContact(trackedTigerBot, trackedBall);
		trackedTigerBot.setBallContact(ballContact);
	}
	
	
	private boolean ballContact(final TrackedTigerBot trackedBot, final TrackedBall trackedBall)
	{
		BotID tigerID = trackedBot.getId();
		ABot bot = trackedBot.getBot();
		if ((bot != null) && (bot.getType() == EBotType.TIGER_V2)
				&& (bot.getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING))
		{
			TigerBotV2 botV2 = (TigerBotV2) bot;
			if (botV2.getLogMovement())
			{
				return ((TigerBotV2) bot).isBarrierInterrupted();
			}
		}
		float botFrontOffset = 0;
		float ballPossTolerance = 0;
		if ((ballContactLastFrame.get(tigerID) != null) && ballContactLastFrame.get(tigerID))
		{
			botFrontOffset = BOT_FRONT_OFFSET_HAS;
			ballPossTolerance = BALL_POSS_TOLERANCE_HAS;
		} else
		{
			botFrontOffset = BOT_FRONT_OFFSET_GET;
			ballPossTolerance = BALL_POSS_TOLERANCE_GET;
		}
		
		final IVector2 lookDir = new Vector2(trackedBot.getAngle());
		final IVector2 botPos = new Vector2f(
				(float) (trackedBot.getPos().x() / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT), (float) (trackedBot
						.getPos().y() / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT));
		final IVector2 optimalBallPossPos = botPos.addNew(lookDir.normalizeNew().scaleTo(
				AIConfig.getGeometry().getBotRadius() - botFrontOffset));
		Circle circle = new Circle(optimalBallPossPos, ballPossTolerance);
		
		boolean ballContact = circle.isPointInShape(trackedBall.getPos());
		ballContactLastFrame.put(tigerID, ballContact);
		return ballContact;
	}
	
	
	/**
	 * @param frame
	 */
	public void debugOutput(final WorldFrame frame)
	{
		if (frame.ball != null)
		{
			log.debug("Ball: " + frame.ball.getPos().x() + " " + frame.ball.getPos().y());
		} else
		{
			log.debug("Ball: None");
		}
		for (final TrackedTigerBot bot : frame.tigerBotsVisible.values())
		{
			log.debug("Tiger " + bot.getId().getNumber() + " " + bot.getPos().x() + " " + bot.getPos().y() + " "
					+ bot.getAngle());
		}
		
		for (final TrackedBot bot : frame.foeBots.values())
		{
			log.debug("Food " + bot.getId().getNumber() + " " + bot.getPos().x() + " " + bot.getPos().y() + " "
					+ bot.getAngle());
		}
	}
}
