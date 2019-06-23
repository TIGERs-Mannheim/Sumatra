/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballpossession;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This calculator determines whether {@link EBallPossession#WE}, {@link EBallPossession#THEY},
 * {@link EBallPossession#BOTH} or {@link EBallPossession#NO_ONE} has the ball.
 * Based on CMDragons TDP 2016.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class BallPossessionCalc extends ACalculator
{
	private static final BotID NO_BOT = BotID.noBot();
	
	@Configurable(comment = "Distance below which t_near starts to tick. (Bot to ball, air gap) [mm]", defValue = "40")
	private static double distNear = 40;
	
	@Configurable(comment = "Distance above which t_far starts to tick. (Bot to ball, air gap) [mm]", defValue = "80")
	private static double distFar = 80;
	
	@Configurable(comment = "Time until a near ball is considered to belong to a robot. [s]", defValue = "0.1")
	private static double timeThresholdNear = 0.1;
	
	@Configurable(comment = "Time until a far ball is considered to be lost by a robot. [s]", defValue = "1.5")
	private static double timeThresholdFar = 1.5;
	
	@Configurable(comment = "Balls above max. height cannot change possession. [mm]", defValue = "120")
	private static double maxHeight = 120;
	
	private double timeWeNear = 0;
	private double timeTheyNear = 0;
	private double timeWeFar = 0;
	private double timeTheyFar = 0;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		BallPossession currentPossession = baseAiFrame.getPrevFrame().getTacticalField().getBallPossession();
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		IVector3 ballPos = wFrame.getBall().getPos3();
		
		// Which robots are near the ball? (Only closest of each team is considered)
		final BotDistance closestTiger = newTacticalField.getTigerClosestToBall();
		final BotDistance closestEnemy = newTacticalField.getEnemyClosestToBall();
		
		// At this point one could check if both BotDistances are equal to BotDistance.NULL_BOT_DISTANCE.
		// As NULL_BOT_DISTANCE.dist == Double.MAX_VALUE, the current decision is that no one has the ball!
		
		// update tNear and tFar for both teams
		double dt = (wFrame.getTimestamp() - baseAiFrame.getPrevFrame().getWorldFrame().getTimestamp()) * 1e-9;
		updateTimes(closestTiger.getDist(), closestEnemy.getDist(), dt, newTacticalField.getGameState(),
				newTacticalField.getOpponentPassReceiver(), currentPossession);
		
		// set ball possession based on near/far time thresholds
		if (ballPos.z() < maxHeight)
		{
			determinePossession(currentPossession, closestTiger, closestEnemy);
		}
		
		newTacticalField.setBallPossession(currentPossession);
		
		// drawing
		List<IDrawableShape> shapes = newTacticalField
				.getDrawableShapes()
				.get(EAiShapesLayer.AI_BALL_POSSESSION);
		
		if (!currentPossession.getTigersId().equals(NO_BOT))
		{
			ITrackedBot bot = baseAiFrame.getWorldFrame().getBot(currentPossession.getTigersId());
			if (bot != null)
			{
				shapes.add(new DrawableCircle(Circle.createCircle(bot.getPos(), Geometry.getBotRadius() + 20),
						Color.BLACK));
			}
		}
		
		if (!currentPossession.getOpponentsId().equals(NO_BOT))
		{
			ITrackedBot bot = baseAiFrame.getWorldFrame().getBot(currentPossession.getOpponentsId());
			if (bot != null)
			{
				shapes.add(new DrawableCircle(Circle.createCircle(bot.getPos(), Geometry.getBotRadius() + 20),
						Color.BLACK));
			}
		}
		
		Color hudColor = wFrame.getTeamColor().getColor();
		double posX = 318.0 + (hudColor.equals(Color.BLUE) ? 50 : 0);
		DrawableBorderText hud = new DrawableBorderText(Vector2.fromXY(posX, 35),
				currentPossession.getEBallPossession().toString(), hudColor);
		hud.setFontSize(10);
		shapes.add(hud);
	}
	
	
	private void determinePossession(final BallPossession currentPossession, final BotDistance closestTiger,
			final BotDistance closestEnemy)
	{
		if ((timeWeNear > timeThresholdNear) && (timeTheyNear < timeThresholdNear))
		{
			currentPossession.setEBallPossession(EBallPossession.WE);
			currentPossession.setTigersId(closestTiger.getBot().getBotId());
			currentPossession.setOpponentsId(NO_BOT);
		} else if ((timeWeNear < timeThresholdNear) && (timeTheyNear > timeThresholdNear))
		{
			currentPossession.setEBallPossession(EBallPossession.THEY);
			currentPossession.setTigersId(NO_BOT);
			currentPossession.setOpponentsId(closestEnemy.getBot().getBotId());
		} else if ((timeWeNear > timeThresholdNear) && (timeTheyNear > timeThresholdNear))
		{
			currentPossession.setEBallPossession(EBallPossession.BOTH);
			currentPossession.setTigersId(closestTiger.getBot().getBotId());
			currentPossession.setOpponentsId(closestEnemy.getBot().getBotId());
		} else if ((timeWeFar > timeThresholdFar) && (timeTheyFar > timeThresholdFar))
		{
			currentPossession.setEBallPossession(EBallPossession.NO_ONE);
			currentPossession.setOpponentsId(NO_BOT);
			currentPossession.setTigersId(NO_BOT);
		}
	}
	
	
	private void updateTimes(final double closeTiger, final double closeOpponent, final double dt,
			final GameState gameState, final Optional<ITrackedBot> passReceiver, final BallPossession lastPossession)
	{
		final double toleranceNear = Geometry.getBallRadius() + distNear
				+ Geometry.getBotRadius();
		final double toleranceFar = Geometry.getBallRadius() + distFar
				+ Geometry.getBotRadius();
		
		double bonusTigers = 0;
		if (gameState.isStandardSituationIncludingKickoffForUs())
		{
			bonusTigers = RuleConstraints.getStopRadius();
		}
		
		double bonusOpponent = 0;
		if (gameState.isStandardSituationIncludingKickoffForThem())
		{
			bonusOpponent = RuleConstraints.getStopRadius();
		} else if ((lastPossession.getEBallPossession() == EBallPossession.THEY) && passReceiver.isPresent())
		{
			bonusOpponent = getBall().getPos().distanceTo(passReceiver.get().getPos());
		}
		
		if (closeTiger < (toleranceNear + bonusTigers))
		{
			timeWeNear += dt;
		} else
		{
			timeWeNear = 0;
		}
		
		if (closeOpponent < (toleranceNear + bonusOpponent))
		{
			timeTheyNear += dt;
		} else
		{
			timeTheyNear = 0;
		}
		
		if (closeTiger > (toleranceFar + bonusTigers))
		{
			timeWeFar += dt;
		} else
		{
			timeWeFar = 0;
		}
		
		if (closeOpponent > (toleranceFar + bonusOpponent))
		{
			timeTheyFar += dt;
		} else
		{
			timeTheyFar = 0;
		}
	}
}
