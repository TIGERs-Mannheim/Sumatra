/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.redirector;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveRedirectorMath;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.NoObjectWithThisIDException;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.util.Optional;


/**
 * @author MarkG
 */
public class RedirectorDetectionCalc extends ACalculator
{
	private static final Logger log = Logger.getLogger(RedirectorDetectionCalc.class.getName());
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		RedirectorDetectionInformation information = new RedirectorDetectionInformation();
		newTacticalField.setRedirectorDetectionInformation(information);
		
		if (getWFrame().getBall().getVel().getLength() < 1.0)
		{
			// in case of slow balls this calculations are useless
			return;
		}
		
		IHalfLine ballTravelLine = calcBallTravelLine();
		
		detectEnemyPassReceiver(information, ballTravelLine);
		detectFriendlyPassReceiver(information, ballTravelLine);
		drawReceiverShapes(newTacticalField, information);
		
		characterizeReceiverSituation(newTacticalField, information, ballTravelLine);
		
	}
	
	
	private void characterizeReceiverSituation(final TacticalField newTacticalField,
			final RedirectorDetectionInformation information, final IHalfLine ballTravelLine)
	{
		if (!information.isEnemyReceiving() || !information.isFriendlyBotReceiving())
		{
			return;
		}
		
		// both teams want to receive the ball, now characterize more deeply
		determineRecommendedAction(newTacticalField, information, ballTravelLine);
		
		calcCertaintyAndKeepOldActionOnDemand(newTacticalField, information);
		
		drawInfoShapes(newTacticalField, information);
	}
	
	
	private void determineRecommendedAction(final TacticalField newTacticalField,
			final RedirectorDetectionInformation information, final IHalfLine ballTravelLine)
	{
		double distFromBallToEnemyReceivePos = getBall().getPos().distanceTo(information.getEnemyReceiverPos());
		double distFromBallToFriendlyReceivePos = getBall().getPos().distanceTo(information.getFriendlyReceiverPos());
		
		double timeToImpactEnemy = getBall().getTrajectory().getTimeByDist(distFromBallToEnemyReceivePos);
		double timeToImpactFriendly = getBall().getTrajectory().getTimeByDist(distFromBallToFriendlyReceivePos);
		information.setTimeToImpactToEnemy(timeToImpactEnemy);
		information.setTimeToImpactToFriendlyBot(timeToImpactFriendly);
		
		if (distFromBallToEnemyReceivePos - Geometry.getBotRadius() < distFromBallToFriendlyReceivePos)
		{
			information.setEnemyReceivingBeforeMe(true);
			
			if (information.isFriendlyStillApproaching() && information.getFriendlyReceiver().isPresent())
			{
				IVector2 catchPos = information.getEnemyReceiverPos()
						.addNew(ballTravelLine.directionVector().scaleToNew(-Geometry.getBotRadius() * 2.5));
				double minTrajTime = TrajectoryGenerator
						.generatePositionTrajectory(getWFrame().getBot(information.getFriendlyReceiver().get()), catchPos)
						.getTotalTime();
				
				if (minTrajTime + 0.2 < information.getTimeToImpactToEnemy())
				{
					information.setRecommendedAction(ERecommendedReceiverAction.CATCH_BEFORE_ENEMY);
				} else
				{
					information.setRecommendedAction(ERecommendedReceiverAction.DOUBLE_ATTACKER);
				}
				
				DrawableAnnotation da = new DrawableAnnotation(
						getWFrame().getBot(information.getFriendlyReceiver().get()).getBotKickerPos(),
						"TimeToOvertake: " + minTrajTime)
								.withOffset(Vector2.fromXY(-200, 0));
				newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(da);
			} else
			{
				// disrupt
				information.setRecommendedAction(ERecommendedReceiverAction.DISRUPT_ENEMY);
			}
		}
	}
	
	
	private void drawInfoShapes(final TacticalField newTacticalField, final RedirectorDetectionInformation information)
	{
		DrawableAnnotation df = new DrawableAnnotation(
				information.getEnemyReceiverPos().addNew(information.getFriendlyReceiverPos()).multiplyNew(0.5),
				"TimeToImpactFriendly: " + information.getTimeToImpactToFriendlyBot() + "\n" +
						"TimeToImpactEnemy: " + information.getTimeToImpactToEnemy() + "\n" +
						"RecAction: " + information.getRecommendedAction() + "\n" +
						"certainty: " + information.getCertainty())
								.withOffset(Vector2.fromXY(-200, 0));
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(df);
	}
	
	
	private void calcCertaintyAndKeepOldActionOnDemand(final TacticalField newTacticalField,
			final RedirectorDetectionInformation information)
	{
		ERecommendedReceiverAction oldRecAction = getAiFrame().getPrevFrame().getTacticalField()
				.getRedirectorDetectionInformation().getRecommendedAction();
		double oldCertainty = getAiFrame().getPrevFrame().getTacticalField().getRedirectorDetectionInformation()
				.getCertainty();
		double certainty = oldCertainty;
		switch (information.getRecommendedAction())
		{
			case NONE:
				certainty -= 0.05;
				break;
			case DISRUPT_ENEMY:
			case CATCH_BEFORE_ENEMY:
			case DOUBLE_ATTACKER:
				if (oldRecAction == information.getRecommendedAction())
				{
					certainty = certainty * 1.1 + 0.1;
				} else
				{
					certainty *= 0.9;
				}
				break;
		}
		if (certainty > 0.5)
		{
			information.setRecommendedAction(oldRecAction);
		}
		
		certainty = SumatraMath.cap(certainty, 0, 1);
		information.setCertainty(certainty);
		
		DrawableCircle dc = new DrawableCircle(Circle.createCircle(
				information.getEnemyReceiverPos().addNew(information.getFriendlyReceiverPos()).multiplyNew(0.5),
				2 + 500 * certainty));
		dc.setColor(new Color((int) (255 * certainty), 57, 0, (int) (100 * certainty)));
		dc.setFill(true);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(dc);
	}
	
	
	private void drawReceiverShapes(final TacticalField newTacticalField,
			final RedirectorDetectionInformation information)
	{
		if (information.isEnemyReceiving())
		{
			// draw receiving shapes
			DrawableCircle dc = new DrawableCircle(Circle.createCircle(information.getEnemyReceiverPos(), 150),
					new Color(0, 113, 214, 152));
			dc.setFill(true);
			DrawableAnnotation da = new DrawableAnnotation(information.getEnemyReceiverPos(), "Enemy is receiving");
			
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(dc);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(da);
		}
		
		if (information.isFriendlyBotReceiving())
		{
			// draw receiving shapes
			DrawableCircle dc = new DrawableCircle(Circle.createCircle(information.getFriendlyReceiverPos(), 150),
					new Color(0, 113, 214, 152));
			dc.setFill(true);
			DrawableAnnotation da = new DrawableAnnotation(information.getFriendlyReceiverPos(), "Friendly is receiving");
			
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(dc);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.AI_REDIRECTOR_DETECTION).add(da);
		}
	}
	
	
	private IHalfLine calcBallTravelLine()
	{
		IVector2 ballStart = getBall().getPos();
		return Lines.halfLineFromDirection(ballStart,
				getBall().getTrajectory().getTravelLine().directionVector());
	}
	
	
	private void detectFriendlyPassReceiver(final RedirectorDetectionInformation information,
			final IHalfLine ballTravelLine)
	{
		// detect friendly pass receiver
		Optional<BotID> attacker = getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy().getAttackerBot();
		Optional<AttackerRole> attackerRole = getAiFrame().getPrevFrame().getPlayStrategy()
				.getActiveRoles(ERole.ATTACKER)
				.stream()
				.map(r -> (AttackerRole) r).findAny();
		
		if (attackerRole.isPresent() && attacker.isPresent() && attacker.get().equals(attackerRole.get().getBotID()))
		{
			// friendly pass receiver has been found
			attackerRole.get().getBot().getRobotInfo().getTrajectory().ifPresent(
					e -> {
						information.setFriendlyReceiver(attacker.get());
						information.setFriendlyReceiverPos(e.getFinalDestination().getXYVector());
						information.setFriendlyBotReceiving(true);
						if (ballTravelLine
								.closestPointOnLine(attackerRole.get().getBot().getBotKickerPos())
								.distanceTo(attackerRole.get().getBot().getBotKickerPos()) > Geometry.getBotRadius()
										* 4)
						{
							information.setFriendlyStillApproaching(true);
						}
					});
		}
	}
	
	
	private void detectEnemyPassReceiver(final RedirectorDetectionInformation information,
			final IHalfLine ballTravelLine)
	{
		BotID enemyReceiver = OffensiveRedirectorMath.getBestRedirector(getWFrame(), getWFrame().foeBots);
		if (enemyReceiver.isBot() && getWFrame().getFoeBots().containsKey(enemyReceiver))
		{
			information.setEnemyReceiver(enemyReceiver);
			try
			{
				ITrackedBot enemyBot = getWFrame().getFoeBots().get(enemyReceiver);
				IVector2 enemyPos = enemyBot.getBotKickerPos();
				IVector2 enemyReceivePos = ballTravelLine
						.closestPointOnLine(enemyPos);
				
				IVector2 velOffset = enemyReceivePos.addNew(enemyBot.getVel().multiplyNew(1000.0));
				IVector2 helper = ballTravelLine
						.closestPointOnLine(velOffset);
				double relativeBotSpeed = helper.distanceTo(velOffset) / 1000.0;
				
				if (enemyPos.distanceTo(enemyReceivePos) < Geometry.getBotRadius() * 3.0
						&& relativeBotSpeed < 0.3)
				{
					information.setEnemyReceiverPos(enemyReceivePos);
					information.setEnemyReceiving(true);
				}
			} catch (NoObjectWithThisIDException e)
			{
				log.warn("could not find enemy receiver bot", e);
			}
		}
	}
}
