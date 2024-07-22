/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.InterceptionSkill;
import edu.tigers.sumatra.statemachine.IEvent;

import java.awt.Color;


public class OpponentInterceptionRole extends ARole
{
	@Configurable(defValue = "true")
	private static boolean smartDistanceCalcAllowedForInterceptor = true;


	public OpponentInterceptionRole()
	{
		super(ERole.OPPONENT_INTERCEPTION);

		InterceptState interceptState = new InterceptState();
		PrepareState prepareState = new PrepareState();

		addTransition(EEvent.FAR_FROM_MOVE_POS, prepareState);
		addTransition(EEvent.NEAR_MOVE_POS, interceptState);

		setInitialState(prepareState);
	}


	private enum EEvent implements IEvent
	{
		NEAR_MOVE_POS,
		FAR_FROM_MOVE_POS
	}

	private class PrepareState extends MoveState
	{
		@Override
		protected void onUpdate()
		{
			IVector2 movePos = adjustMovePositionWhenItsInvalid(calcMovePosition());
			skill.updateDestination(movePos);

			if (movePos.distanceTo(getPos()) < 50)
			{
				triggerEvent(EEvent.NEAR_MOVE_POS);
			}
		}


		private IVector2 calcMovePosition()
		{
			IVector2 ballPos = getWFrame().getBall().getPos();
			IVector2 goal = Geometry.getGoalOur().getCenter();
			IVector2 dir = goal.subtractNew(ballPos).normalizeNew();
			return ballPos.addNew(dir.multiplyNew(RuleConstraints.getStopRadius()
					+ (Geometry.getBotRadius() * 3)));
		}
	}

	private class InterceptState extends RoleState<InterceptionSkill>
	{
		public InterceptState()
		{
			super(InterceptionSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			var nearestOpponentBot = getAiFrame().getTacticalField().getOpponentClosestToBall().getBotId();
			if (nearestOpponentBot.isBot())
			{
				IVector2 pos = getWFrame().getBot(nearestOpponentBot).getPos();
				if (getAiFrame().getGameState().isKickoffOrPrepareKickoff() &&
						pos.x() < Geometry.getBotRadius())
				{
					skill.setNearestOpponentBotPos(Vector2.fromXY(Math.max(-pos.x(), Geometry.getBotRadius()), pos.y()));
				} else
				{
					skill.setNearestOpponentBotPos(pos);
				}
			} else
			{
				IVector2 pos = LineMath.stepAlongLine(getWFrame().getBall().getPos(),
						Geometry.getGoalOur().getCenter(), -2 * Geometry.getBotRadius());
				skill.setNearestOpponentBotPos(pos);
			}

			if (smartDistanceCalcAllowedForInterceptor)
			{
				double distance = calcDistancToOpponent();
				skill.setDistanceToBall(distance);
			}

			if (getPos().distanceTo(getBall().getPos()) > RuleConstraints.getStopRadius() + Geometry.getBotRadius() + 500)
			{
				triggerEvent(EEvent.FAR_FROM_MOVE_POS);
			}
		}


		private double calcDistancToOpponent()
		{
			final double searchRadius = 1500;
			Triangle triangle = Triangle.fromCorners(getWFrame().getBall().getPos(), Geometry.getGoalOur().getLeftPost(),
					Geometry.getGoalOur().getRightPost());
			DrawableTriangle dt = new DrawableTriangle(triangle, new Color(125, 125, 125, 100));
			dt.setFill(true);
			getAiFrame().getShapeMap().get(EAiShapesLayer.OFFENSE_OPPONENT_INTERCEPTION)
					.add(dt);

			for (int i = 1; i < getAiFrame().getTacticalField().getOpponentsToBallDistances().size(); i++)
			{
				BotDistance bot = getAiFrame().getTacticalField().getOpponentsToBallDistances().get(i);
				if (bot.getDist() < searchRadius
						&& dt.getTriangle()
						.withMargin(Geometry.getBotRadius()).isPointInShape(getWFrame().getBot(bot.getBotId()).getPos()))
				{
					return bot.getDist() + Geometry.getBotRadius() * 2 + 50;
				}
			}
			return InterceptionSkill.getStdDist();
		}
	}
}
