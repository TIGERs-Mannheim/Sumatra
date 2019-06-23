/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 28, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.OpenClHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.interfaces.IPointChecker;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.DestinationFreeCondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible.VisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.helper.ShooterMemory;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Calculation of redirect positions (originates from old RedirectRole)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectPosRefCalc extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private BaseAiFrame									aiFrame						= null;
	
	@Configurable(comment = "angle [rad] - Maximum angle allowed for redirect shot")
	private static float									maxAngleTol					= AngleMath.PI_HALF;
	private static final float							BEST_TARGET_EQUAL_TOL	= 30f;
	private static float									chipPassDistFactor		= 0.6f;
	
	private final BotIDMap<RedirectPointChecker>	pointCheckers				= new BotIDMap<RedirectPointChecker>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public RedirectPosRefCalc()
	{
		if (OpenClHandler.isOpenClSupported())
		{
			setActive(false);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		aiFrame = baseAiFrame;
		
		for (BotID botId : baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet())
		{
			RedirectPointChecker redirectPointChecker = pointCheckers.getWithNull(botId);
			if (redirectPointChecker == null)
			{
				redirectPointChecker = new RedirectPointChecker(botId);
				pointCheckers.put(botId, redirectPointChecker);
			}
			IVector2 bestDest = redirectPointChecker
					.getBestDestination(baseAiFrame.getWorldFrame().getBot(botId).getPos());
			newTacticalField.getSupportRedirectPositions().put(botId, bestDest);
		}
	}
	
	
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.setSupportRedirectPositions(baseAiFrame.getPrevFrame().getTacticalField()
				.getSupportRedirectPositions());
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class RedirectPointChecker implements IPointChecker
	{
		private final DestinationFreeCondition	destFreeCon;
		
		private final VisibleCon					targetVisibleCon			= new VisibleCon();
		private final VisibleCon					receiverVisibleCon		= new VisibleCon();
		
		private final BotID							botId;
		
		private final Vector2						shootTarget;
		
		private int										noDestinationFoundCtr	= 0;
		private ShooterMemory						mem							= new ShooterMemory();
		private boolean								passerUsesChipper			= false;
		
		
		/**
		 * @param botId
		 */
		public RedirectPointChecker(final BotID botId)
		{
			this.botId = botId;
			float freeTol = AIConfig.getGeometry().getBotRadius() * 4;
			destFreeCon = new DestinationFreeCondition(botId);
			destFreeCon.setDestFreeTolerance(freeTol);
			destFreeCon.setConsiderFoeBots(true);
			shootTarget = new Vector2(AIConfig.getGeometry().getGoalTheir()
					.getGoalCenter());
		}
		
		
		@Override
		public boolean checkPoint(final IVector2 point)
		{
			// only in their half of field and not too close to goal line
			// TODO verst�ndlicher schreiben
			if ((point.x() < -1000) || (point.x() > ((AIConfig.getGeometry().getFieldLength() / 2) - 200)))
			{
				return false;
			}
			
			if (!AIConfig.getGeometry().getField().isPointInShape(point))
			{
				return false;
			}
			
			if (AIConfig.getGeometry().getPenaltyAreaTheir().isPointInShape(point))
			{
				return false;
			}
			
			if (!checkAngle(point))
			{
				return false;
			}
			
			IVector2 kickerPos = AiMath.getBotKickerPos(point,
					getTargetAngle(aiFrame.getWorldFrame().ball.getPos(), point));
			receiverVisibleCon.updateStart(kickerPos);
			targetVisibleCon.updateStart(kickerPos);
			
			// not too close to redirector/ball
			if (GeoMath.distancePP(receiverVisibleCon.getStart(), receiverVisibleCon.getEnd()) < 500)
			{
				return false;
			}
			
			if (!targetVisibleCon.checkCondition(aiFrame.getWorldFrame(), botId).isOk())
			{
				return false;
			}
			
			if (!receiverVisibleCon.checkCondition(aiFrame.getWorldFrame(), botId).isOk())
			{
				return false;
			}
			
			destFreeCon.updateDestination(point);
			if (!destFreeCon.checkCondition(aiFrame.getWorldFrame(), botId).isOk())
			{
				return false;
			}
			
			return true;
		}
		
		
		private boolean checkAngle(final IVector2 initPos)
		{
			IVector2 senderPos = aiFrame.getWorldFrame().ball.getPos(); // BALL
			IVector2 kickerPos = AiMath.getBotKickerPos(initPos, getTargetAngle(senderPos, initPos));
			IVector2 shootDir = shootTarget.subtractNew(kickerPos);
			float shortestRotation = AngleMath.getShortestRotation(shootDir.getAngle(), senderPos.subtractNew(kickerPos)
					.getAngle());
			
			if (Math.abs(shortestRotation) > maxAngleTol)
			{
				return false;
			}
			return true;
		}
		
		
		private float getTargetAngle(final IVector2 senderPos, final IVector2 pos)
		{
			IVector2 targetAngleVec = GeoMath.calculateBisector(pos, senderPos, shootTarget);
			return targetAngleVec.getAngle();
		}
		
		
		private IVector2 getBotPos()
		{
			return aiFrame.getWorldFrame().getBot(botId).getPos();
		}
		
		
		private TrackedTigerBot getBot()
		{
			return aiFrame.getWorldFrame().getBot(botId);
		}
		
		
		private IVector2 getBestDestination(final IVector2 currentDestination)
		{
			mem.update(aiFrame.getWorldFrame(), getBotPos());
			IVector2 bestTarget = mem.getBestPoint();
			targetVisibleCon.updateEnd(shootTarget);
			
			// TODO Use Marks Calc
			if (passerUsesChipper)
			{
				float distance = GeoMath.distancePP(aiFrame.getWorldFrame().ball.getPos(), currentDestination);
				receiverVisibleCon.updateEnd(GeoMath.stepAlongLine(aiFrame.getWorldFrame().ball.getPos(),
						currentDestination, distance * chipPassDistFactor));
			} else
			{
				receiverVisibleCon.updateEnd(aiFrame.getWorldFrame().ball.getPos());
			}
			
			IVector2 bestPoint = currentDestination;
			if (!checkPoint(currentDestination))
			{
				// TODO configure visibleConRaySize
				float visibleConRaySizeBig = AIConfig.getGeometry().getBotRadius();
				float visibleConRaySizeBSmall = AIConfig.getGeometry().getBallRadius();
				
				IVector2 start = currentDestination
						.addNew(new Vector2(getBot().getAngle()).multiply(-1).scaleTo(visibleConRaySizeBig));
				
				// TODO configure rounds
				int rounds = 10;// (int) (AIConfig.getGeometry().getFieldLength() / AIConfig.getGeometry().getBotRadius());
				
				targetVisibleCon.setRaySize(visibleConRaySizeBig);
				receiverVisibleCon.setRaySize(visibleConRaySizeBig);
				
				bestPoint = AiMath.findBestPoint(currentDestination, start, this, rounds);
				if (bestPoint == null)
				{
					targetVisibleCon.setRaySize(visibleConRaySizeBSmall);
					receiverVisibleCon.setRaySize(visibleConRaySizeBSmall);
					
					bestPoint = AiMath.findBestPoint(currentDestination, start, this, rounds);
					if (bestPoint == null)
					{
						noDestinationFoundCtr++;
						if (noDestinationFoundCtr > 10)
						{
							// log.warn("There is no damn point on the field from where we can do an indirect shot?! Probably something is wrong...");
						}
						return currentDestination;
					}
				}
				
				noDestinationFoundCtr = 0;
				IVector2 kickerPos = AiMath.getBotKickerPos(bestPoint,
						getTargetAngle(receiverVisibleCon.getEnd(), bestPoint));
				targetVisibleCon.updateStart(kickerPos);
				receiverVisibleCon.updateStart(kickerPos);
			}
			
			if (!bestTarget.equals(shootTarget, BEST_TARGET_EQUAL_TOL))
			{
				// log.debug("Found better shoot target: " + bestTarget + " (before: " + shootTarget + ")");
				shootTarget.set(bestTarget);
			}
			
			return bestPoint;
		}
	}
}
