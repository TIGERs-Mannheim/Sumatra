/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.04.2014
 * Author(s): Simon
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ITacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValueBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Calculates the best pass tartget
 * TODO L�nge des Passes bewerten
 * 
 * @author Simon
 */
public class PassCalc extends ACalculator implements IConfigObserver
{
	private static final Logger	log							= Logger.getLogger(PassCalc.class.getName());
	
	private int							DIRECT_SHOOT				= -1;
	private int							CHIP_KICK					= 1;
	
	@Configurable(comment = "Distance from Bot in which ignore Enemy Bots ")
	private static float				ignoreEnemyBotDistance	= 1000;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getWorldFrame().getTigerBotsAvailable().size() > 0)
		{
			newTacticalField.setBestPassTarget(getBestPassTarget2(baseAiFrame.getWorldFrame()));
		}
	}
	
	
	// Idee:
	// --------------------------------------------------------------------------
	// -- Anforderungen an PassCalc aus OffensiveSicht:
	
	// -- Die Berechnung sollte aus Sicht des Balles erfolgen wenn der Bot
	// -- ( die ID ) welche nach dem BestpassTarget Fragt der Offensive Bot ist
	// -- ( übergebene ID == Bot der von uns am nächsten am Ball ist ):
	// -- das ist wichtig, da die OffensiveRole die Entscheidung ob sie passt oder
	// -- schießt schon trifft, wenn sie noch relativ weit weg von Ball ist.
	
	// ansonsten
	// -- sollte die Berechnung von der KickerPos des bots ausgehen.
	// -- Das ist wichtig wenn nach der Ausrichtung für redirects gefragt wird,
	// -- z.B. wenn ein Bot der angespielt wird gleich wieder weiter passen will.W
	
	
	/**
	 * find best pass target/bot
	 * 
	 * @param bot
	 * @return
	 */
	@SuppressWarnings("unused")
	private ValueBot getBestPassTarget(final WorldFrame wFrame, final ITacticalField tfield, final TrackedTigerBot bot)
	{
		List<ValueBot> passReceivers = new ArrayList<ValueBot>();
		
		List<ValueBot> botReceiverLines = tfield.getShooterReceiverStraightLines().get(bot.getId());
		
		
		if (botReceiverLines == null)
		{
			ValueBot bestChipKickReceiver = new ValueBot(getBestChipKickBot(wFrame, tfield, bot, ignoreEnemyBotDistance),
					CHIP_KICK);
			return bestChipKickReceiver;
		}
		
		for (ValueBot receiver : botReceiverLines)
		{
			BotID receiverBot = receiver.getBotID();
			float passValue = receiver.getValue();
			if (receiverBot.equals(bot.getId()))
			{
				log.debug("receiverBot equals bot");
				continue;
			}
			passReceivers.add(new ValueBot(receiverBot, passValue));
		}
		
		for (ValueBot receiver : passReceivers)
		{
			
			ValuePoint bestDirectShot = tfield.getBestDirectShotTargetBots().get(receiver.getBotID());
			float receiverValue = receiver.getValue();
			float shootValue = bestDirectShot.getValue();
			receiver.setValue(receiverValue + shootValue);
		}
		
		Line ourPenaltyLine = AIConfig.getGeometry().getPenaltyAreaOur().getPenaltyAreaFrontLine();
		// Line ourgoalLine = AIConfig.getGeometry().getGoalLineOur();
		float penaltywidth = AIConfig.getGeometry().getPenaltyAreaOur().getRadiusOfPenaltyArea();
		List<ValueBot> removePassReceivers = new ArrayList<ValueBot>();
		
		
		for (ValueBot receiver : passReceivers)
		{
			TrackedTigerBot reciverBot = wFrame.getTigerBotsVisible().getWithNull(receiver.getBotID());
			if (reciverBot == null)
			{
				continue;
			}
			
			// direction in their goal
			if ((reciverBot.getPos().x() - bot.getPos().x()) > 0)
			{
				continue;
			}
			
			try
			{
				Line shooterReceiverLine = Line.newLine(bot.getPos(), reciverBot.getPos());
				try
				{
					Vector2 intersectionnPoint = GeoMath.intersectionPoint(ourPenaltyLine, shooterReceiverLine);
					
					if ((intersectionnPoint.y > (penaltywidth * -1f)) && (intersectionnPoint.y < (penaltywidth * 1f)))
					{
						removePassReceivers.add(receiver);
					}
					
				} catch (MathException err)
				{
					// log.trace("find no intersectionnPoint between ourPenaltyLine and shooterReceiverLine");
				}
			} catch (IllegalArgumentException err)
			{
				// ignore
				// log.warn("bot and receiver equal!", err);
			}
			
		}
		
		
		passReceivers.removeAll(removePassReceivers);
		
		Collections.sort(passReceivers, ValueBot.VALUELOWCOMPARATOR);
		
		if (passReceivers.size() == 0)
		{
			// log.debug(bot.getId().toString() + " : null bots ");l
			return new ValueBot(bot.getId(), DIRECT_SHOOT);
		}
		
		ValueBot bestpassreceiver = new ValueBot(passReceivers.get(0).getBotID(), DIRECT_SHOOT);
		
		// log.debug("POST: " + bot.getId().toString() + " : BEST PASS RECEIVER = " + bestpassreceiver);
		
		return bestpassreceiver;
	}
	
	
	private BotID getBestChipKickBot(final WorldFrame wFrame, final ITacticalField tfield, final TrackedTigerBot bot,
			final float ignoreEnemyBotDistance)
	{
		IVector2 botpos = bot.getPos();
		BotID botid = bot.getId();
		float raySize = AIConfig.getGeometry().getBallRadius() * 4;
		
		List<BotID> ignoredBots = new ArrayList<BotID>();
		for (Entry<BotID, TrackedTigerBot> foeBot : wFrame.foeBots)
		{
			if (GeoMath.distancePP(botpos, foeBot.getValue().getPos()) < ignoreEnemyBotDistance)
			{
				ignoredBots.add(foeBot.getKey());
			}
		}
		
		List<ValueBot> valuedBots = new ArrayList<ValueBot>();
		for (Entry<BotID, TrackedTigerBot> tigerBot : wFrame.tigerBotsVisible)
		{
			boolean foeBotFree = GeoMath.p2pVisibility(wFrame, botpos, tigerBot.getValue().getPos(), raySize, ignoredBots);
			
			if (foeBotFree)
			{
				ValuePoint valueTigerBot = tfield.getBestDirectShotTargetBots().get(tigerBot.getKey());
				if (valueTigerBot == null)
				{
					continue;
				}
				valuedBots.add(new ValueBot(botid, valueTigerBot.getValue()));
				
			}
		}
		
		Collections.sort(valuedBots, ValueBot.VALUELOWCOMPARATOR);
		
		if (valuedBots.size() == 0)
		{
			// log.debug(bot.getId().toString() + " : null bots ");
			return bot.getId();
		}
		
		return valuedBots.get(0).getBotID();
	}
	
	
	@Override
	public void onLoad(final HierarchicalConfiguration newConfig)
	{
		// TODO simon_000: Auto-generated method stub
		
	}
	
	
	@Override
	public void onReload(final HierarchicalConfiguration freshConfig)
	{
		// TODO simon_000: Auto-generated method stub
		
	}
	
	
	private ValueBot getBestPassTarget2(final WorldFrame wFrame)
	{
		IVector2 shootTarget = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		
		TrackedTigerBot shooterBot = AiMath.getNearestBot(wFrame.tigerBotsAvailable, wFrame.ball.getPos());
		IVector2 ballPos = wFrame.getBall().getPos();
		
		List<ValueBot> valuedBots = new ArrayList<ValueBot>();
		
		if (shooterBot != null)
		{
			for (BotID key : wFrame.getTigerBotsAvailable().keySet())
			{
				TrackedTigerBot potentialPassReciever = wFrame.getTigerBotsAvailable().get(key);
				if (shooterBot != null)
				{
					if ((key == shooterBot.getId())
							|| (GeoMath.distancePP(potentialPassReciever.getPos(), AIConfig.getGeometry().getGoalOur()
									.getGoalCenter()) < 1800))
					{
						continue;
					}
					if (GeoMath.distancePP(potentialPassReciever.getPos(), shooterBot.getPos()) < 2000)
					{
						continue;
					}
				}
				
				IVector2 dvShooterPassTarget = potentialPassReciever.getPos().subtractNew(ballPos);
				IVector2 dvPassReciverTarget = shootTarget.subtractNew(potentialPassReciever.getPos());
				
				float angle = Math.abs(AngleMath.rad2deg(GeoMath.angleBetweenVectorAndVector(dvShooterPassTarget,
						dvPassReciverTarget)));
				
				angle = angle - 30;
				float angleValue = 0;
				angleValue = angle;
				if (angleValue < 0)
				{
					angleValue = 0;
				}
				angleValue = (angleValue / 150) * 100;
				
				float dif = GeoMath.distancePP(potentialPassReciever.getPos(), shooterBot.getPos());
				
				
				float difScore = (float) (((float) -0.000001 * Math.pow((dif - 2900), 2)) + 100);
				if (difScore < 0)
				{
					difScore = 0;
				}
				
				float shootingValue = AiMath.getDirectShootScoreChance(wFrame, key, false) * 100;
				
				float botInPen = 100;
				if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(potentialPassReciever.getPos(), 400))
				{
					botInPen = 0;
				}
				
				float tmpBotScore = ((angleValue * 1) + (difScore * 1)
						+ (shootingValue * 2) + (botInPen * 2)) / 6;
				valuedBots.add(new ValueBot(key, tmpBotScore));
			}
		}
		Collections.sort(valuedBots, ValueBot.VALUEHIGHCOMPARATOR);
		
		List<BotID> ignoredBots = new ArrayList<BotID>();
		for (BotID key : wFrame.foeBots.keySet())
		{
			if (GeoMath.distancePP(ballPos, wFrame.foeBots.get(key).getPos()) < ignoreEnemyBotDistance)
			{
				ignoredBots.add(key);
			}
		}
		
		for (int i = 0; i < valuedBots.size(); i++)
		{
			if (GeoMath.p2pVisibility(wFrame, ballPos,
					wFrame.tigerBotsAvailable.get(valuedBots.get(i).getBotID())
							.getPos()))
			{
				// freeForDirectPass
				ValueBot passTarget = valuedBots.get(i);
				passTarget.setValue(DIRECT_SHOOT);
				return passTarget;
			} else if (GeoMath.p2pVisibility(wFrame, ballPos,
					wFrame.tigerBotsAvailable.get(valuedBots.get(i).getBotID())
							.getPos(), ignoredBots))
			{
				// freeForChipPass
				ValueBot passTarget = valuedBots.get(i);
				passTarget.setValue(CHIP_KICK);
				return passTarget;
			}
		}
		if (shooterBot == null)
		{
			return null;
		}
		return new ValueBot(shooterBot.getId());
	}
}
