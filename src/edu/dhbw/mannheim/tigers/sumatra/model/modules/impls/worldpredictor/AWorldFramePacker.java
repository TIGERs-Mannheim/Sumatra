/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.01.2015
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * This is the base class for all WorldFramePacker. It contains some general code that is required by all
 * Packer
 * 
 * @author KaiE
 */
public abstract class AWorldFramePacker
{
	private static final Logger				log							= Logger.getLogger(AWorldFramePacker.class
																								.getName());
	
	private static final float					BOT_FRONT_OFFSET_HAS		= 5;
	private static final float					BALL_POSS_TOLERANCE_HAS	= 80;
	private static final float					BOT_FRONT_OFFSET_GET		= 5;
	private static final float					BALL_POSS_TOLERANCE_GET	= 30;
	
	private final Map<BotID, Boolean>		ballContactLastFrame		= new HashMap<BotID, Boolean>();
	
	/** Add the robot-id and the system.nanotime() to this map to determine if the bot is visible or not */
	protected final Map<BotID, Long>			botLastVisible				= new HashMap<BotID, Long>();
	protected final Map<BotID, IVector2>	botLastPos					= new HashMap<>();
	protected ABotManager						botManager;
	
	
	/**
	 * c-tor
	 */
	public AWorldFramePacker()
	{
		try
		{
			botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.fatal("Botmanager module not found!!");
		}
		
	}
	
	
	protected final boolean ballContact(final TrackedTigerBot trackedBot, final TrackedBall trackedBall)
	{
		BotID tigerID = trackedBot.getId();
		ABot bot = trackedBot.getBot();
		if ((bot != null) && (bot.getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING))
		{
			if (bot.getType() == EBotType.TIGER_V3)
			{
				TigerBotV3 botV3 = (TigerBotV3) bot;
				return botV3.getLatestFeedbackCmd().isBarrierInterrupted();
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
	
	
	protected final void setVisibility(final Map<BotID, TrackedTigerBot> bots)
	{
		for (TrackedTigerBot trackedTigerBot : bots.values())
		{
			Long lastVisible = botLastVisible.get(trackedTigerBot.getId());
			if ((lastVisible != null))
			{
				long diff = TimeUnit.NANOSECONDS.toMillis(SumatraClock.nanoTime() - lastVisible);
				if ((diff >= 2000))
				{
					// The position reported by the bot may not be correct anymore
					trackedTigerBot.setVisible(false);
				}
			} else
			{
				trackedTigerBot.setVisible(false);
			}
		}
	}
	
	
	protected final void setVisibilityBotmanager(final Map<BotID, TrackedTigerBot> bots)
	{
		IVector2 botPosOutside = new Vector2(-1500, (AIConfig.getGeometry().getFieldWidth() / 2) + 500);
		for (ABot bot : botManager.getAllBots().values())
		{
			if (!bots.containsKey(bot.getBotID()))
			{
				IVector2 pos;
				float orientation;
				if (bot.getType() == EBotType.TIGER_V3)
				{
					TigerBotV3 botV3 = (TigerBotV3) bot;
					pos = botV3.getLatestFeedbackCmd().getPosition().multiplyNew(1000);
					orientation = botV3.getLatestFeedbackCmd().getOrientation();
				} else
				{
					pos = botPosOutside;
					botPosOutside = new Vector2(250, 0).add(pos);
					orientation = 0;
				}
				
				TrackedTigerBot tBot = new TrackedTigerBot(bot.getBotID(), pos, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR,
						0, orientation, 0, 0, 1, bot, bot.getColor());
				tBot.setVisible(false);
				bots.put(bot.getBotID(), tBot);
			}
		}
	}
}
