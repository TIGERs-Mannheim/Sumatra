/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.SpecialMoveCommand;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;

import java.awt.Color;
import java.text.DecimalFormat;


/**
 * @author MarkG
 */
public class AddSecondaryFeature extends AOffensiveStrategyFeature
{
	private SpecialMoveCommand prevCommand = null;
	
	
	/**
	 * Default
	 */
	public AddSecondaryFeature()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final TemporaryOffensiveInformation tempInfo, final OffensiveStrategy strategy)
	{
		processSpecialMove(newTacticalField, baseAiFrame, tempInfo, strategy);
	}
	
	
	private void processSpecialMove(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final TemporaryOffensiveInformation tempInfo, final OffensiveStrategy strategy)
	{
		AIInfoFrame prevFrame = baseAiFrame.getPrevFrame();
		
		if (newTacticalField.getGameState().isKickoffOrPrepareKickoffForUs())
		{
			return;
		}
		
		IPassTarget passTarget = prevFrame.getAICom().getPassTarget();
		if ((passTarget != null)
				&& passTarget.getBotId().isBot())
		{
			BotID passReceiver = passTarget.getBotId();
			SpecialMoveCommand command = new SpecialMoveCommand();
			
			command.getMovePosition().add(passTarget.getBotPos());
			command.getMoveTimes().add(0.0);
			command.setResponseStep(0);
			
			double shootTime = newTacticalField.getOffensiveTimeEstimations().get(tempInfo.getPrimaryBot().getBotId())
					.getBallContactTime();
			
			ITrackedBall ball = baseAiFrame.getWorldFrame().getBall();
			
			double dist = ball.getPos().distanceTo(passTarget.getKickerPos());
			
			double slackTime = (passTarget.getTimeReached() - baseAiFrame.getWorldFrame().getTimestamp()) * 1e-9;
			double kickSpeed = OffensiveMath.calcPassSpeedRedirect(slackTime, tempInfo.getPrimaryBot().getBotKickerPos(),
					passTarget.getKickerPos(), DefenseMath.getBisectionGoal(passTarget.getKickerPos()));
			
			double ballTravelTime = ball.getStraightConsultant().getTimeForKick(dist, kickSpeed);
			
			double time = shootTime + ballTravelTime;
			command.setTimeUntilPassArrives(time);
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			DrawableAnnotation dt = new DrawableAnnotation(
					prevFrame.getAICom().getPassTarget().getKickerPos().addNew(Vector2.fromXY(200, 0)),
					"Full time (t/b): " + df.format(time) + " -> (" + df.format(shootTime) + "|"
							+ df.format(ballTravelTime)
							+ ")" + " initVel: " + df.format(kickSpeed) + " dist: " + df.format(dist),
					Color.black);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_PASSING_DEBUG).add(dt);
			
			if (strategy.getCurrentOffensivePlayConfiguration().containsKey(passReceiver)
					&& (strategy.getCurrentOffensivePlayConfiguration()
							.get(passReceiver) == OffensiveStrategy.EOffensiveStrategy.SPECIAL_MOVE)
					&& (prevCommand != null))
			{
				command = prevCommand;
			}
			
			if (baseAiFrame.getWorldFrame().tigerBotsAvailable.containsKey(passReceiver) &&
					!newTacticalField.getCrucialDefender().contains(passReceiver))
			{
				strategy.getDesiredBots().add(passReceiver);
				strategy.setMaxNumberOfBots(strategy.getMaxNumberOfBots() + 1);
				strategy.getSpecialMoveCommands().add(command);
				strategy.getCurrentOffensivePlayConfiguration().put(passReceiver,
						OffensiveStrategy.EOffensiveStrategy.SPECIAL_MOVE);
				prevCommand = command;
				// else pass reciever is not a tigers bot but friendly. (or not availible)
			}
		}
	}
}
