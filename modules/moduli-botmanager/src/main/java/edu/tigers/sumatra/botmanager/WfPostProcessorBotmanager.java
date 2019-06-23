/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 6, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.bot.DummyBot;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.IWfPostProcessor;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.data.MotionContext.BotInfo;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class WfPostProcessorBotmanager implements IWfPostProcessor
{
	@SuppressWarnings("unused")
	private static final Logger				log							= Logger.getLogger(WfPostProcessorBotmanager.class
			.getName());
	
	private static final double				BOT_FRONT_OFFSET_HAS		= 5;
	private static final double				BALL_POSS_TOLERANCE_HAS	= 80;
	private static final double				BOT_FRONT_OFFSET_GET		= 5;
	private static final double				BALL_POSS_TOLERANCE_GET	= 30;
	
	private final Map<BotID, Boolean>		ballContactLastFrame		= new HashMap<BotID, Boolean>();
	
	/** Add the robot-id and the system.nanotime() to this map to determine if the bot is visible or not */
	protected final Map<BotID, Long>			botLastVisible				= new HashMap<BotID, Long>();
	protected final Map<BotID, IVector2>	botLastPos					= new HashMap<>();
	protected ABotManager						botManager;
	
	private boolean								overrideState				= false;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public WfPostProcessorBotmanager()
	{
		try
		{
			botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.fatal("Botmanager module not found!!");
		}
	}
	
	
	@Override
	public SimpleWorldFrame process(final SimpleWorldFrame swf)
	{
		IBotIDMap<ITrackedBot> bots = new BotIDMap<>(swf.getBots().size());
		TrackedBall ball = swf.getBall();
		
		for (ITrackedBot origbot : swf.getBots().values())
		{
			TrackedBot bot = new TrackedBot(origbot);
			bot.setBallContact(ballContact(bot, ball));
			ABot abot = botManager.getBotTable().get(bot.getBotId());
			if (abot != null)
			{
				bot.setBot(abot);
				if (overrideState)
				{
					abot.getSensoryPos().ifPresent((p) -> {
						bot.setPos(p.getXYVector());
					});
					abot.getSensoryVel().ifPresent((v) -> {
						bot.setVel(v.getXYVector());
					});
				}
				abot.getSensoryPos().ifPresent((p) -> {
					bot.setAngle(p.z());
				});
				abot.getSensoryVel().ifPresent((v) -> {
					bot.setaVel(v.z());
				});
			} else
			{
				bot.setBot(new DummyBot(bot.getBotId()));
			}
			bots.put(bot.getBotId(), bot);
		}
		
		addBotsFromBotmanager(swf.getTimestamp(), bots);
		
		SimpleWorldFrame nswf = new SimpleWorldFrame(bots, ball, swf.getId(), swf.getTimestamp());
		nswf.setCamFrame(swf.getCamFrame());
		return nswf;
	}
	
	
	protected final boolean ballContact(final ITrackedBot trackedBot, final TrackedBall trackedBall)
	{
		BotID tigerID = trackedBot.getBotId();
		ABot bot = botManager.getBotTable().get(trackedBot.getBotId());
		if ((bot != null)
				&& (bot.getType() != EBotType.SUMATRA)
				&& (bot.getType() != EBotType.GRSIM)
				&& (bot.getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING))
		{
			return bot.isBarrierInterrupted();
		}
		double botFrontOffset = 0;
		double ballPossTolerance = 0;
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
				trackedBot.getPos().x(), trackedBot
						.getPos().y());
		final IVector2 optimalBallPossPos = botPos.addNew(lookDir.normalizeNew().scaleTo(
				Geometry.getBotRadius() - botFrontOffset));
		Circle circle = new Circle(optimalBallPossPos, ballPossTolerance);
		
		boolean ballContact = circle.isPointInShape(trackedBall.getPos());
		ballContactLastFrame.put(tigerID, ballContact);
		return ballContact;
	}
	
	
	private final void addBotsFromBotmanager(final long timestamp, final IBotIDMap<ITrackedBot> bots)
	{
		for (ABot bot : botManager.getAllBots().values())
		{
			if (!bots.containsKey(bot.getBotId()))
			{
				Optional<IVector3> optPos = bot.getSensoryPos();
				Optional<IVector3> optVel = bot.getSensoryVel();
				if (optPos.isPresent() && optVel.isPresent())
				{
					TrackedBot tBot = new TrackedBot(timestamp, bot.getBotId());
					tBot.setPos(optPos.get().getXYVector());
					tBot.setVel(optVel.get().getXYVector());
					tBot.setAngle(optPos.get().z());
					tBot.setaVel(optVel.get().z());
					tBot.setVisible(false);
					if (Geometry.getFieldWBorders().isPointInShape(tBot.getPos()))
					{
						tBot.setBot(bot);
					}
					bots.put(bot.getBotId(), tBot);
				}
			}
		}
	}
	
	
	@Override
	public void processMotionContext(final MotionContext context, final long timestamp)
	{
		for (BotInfo botInfo : context.getBots().values())
		{
			ABot bot = botManager.getBotTable().get(botInfo.getBotId());
			if (bot != null)
			{
				botInfo.setCenter2DribblerDist(bot.getCenter2DribblerDist());
				botInfo.setKickSpeed(bot.getMatchCtrl().getKickSpeed());
				botInfo.setChip(bot.getMatchCtrl().getDevice() == EKickerDevice.CHIP);
				botInfo.setDribbleRpm(bot.getMatchCtrl().getDribbleSpeed());
				botInfo.setBallContact(bot.isBarrierInterrupted());
				if (bot.getSensoryVel().isPresent())
				{
					botInfo.setVel(bot.getSensoryVel().get());
				}
			}
		}
	}
}
