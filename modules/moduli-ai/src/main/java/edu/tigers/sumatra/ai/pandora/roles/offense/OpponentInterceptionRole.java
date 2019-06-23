/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.InterceptionSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;


public class OpponentInterceptionRole extends ARole
{
	@Configurable(defValue = "false")
	private static boolean smartDistanceCalcAllowedForInterceptor = false;
	
	
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
	
	private class PrepareState extends AState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(AMoveToSkill.createMoveToSkill());
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 movePos = calcMovePosition();
			movePos = AiMath.adjustMovePositionWhenItsInvalid(getWFrame(), getBotID(), movePos);
			getCurrentSkill().getMoveCon().updateDestination(movePos);
			
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
	
	private class InterceptState extends AState
	{
		private InterceptionSkill intercept = null;
		
		
		@Override
		public void doEntryActions()
		{
			intercept = new InterceptionSkill();
			setNewSkill(intercept);
		}
		
		
		@Override
		public void doUpdate()
		{
			BotDistance nearestEnemyBot = getAiFrame().getTacticalField().getEnemyClosestToBall();
			if (nearestEnemyBot.getBot() != null)
			{
				intercept.setNearestEnemyBotPos(nearestEnemyBot.getBot().getPos());
			} else
			{
				intercept.setNearestEnemyBotPos(Vector2.fromXY(0, 0));
			}
			
			if (smartDistanceCalcAllowedForInterceptor)
			{
				double distance = calcDistancToEnemy();
				intercept.setDistanceToBall(distance);
			}
			
			if (getPos().distanceTo(getBall().getPos()) > RuleConstraints.getStopRadius() + Geometry.getBotRadius() + 500)
			{
				triggerEvent(EEvent.FAR_FROM_MOVE_POS);
			}
		}
		
		
		private double calcDistancToEnemy()
		{
			final double searchRadius = 1500;
			Triangle triangle = Triangle.fromCorners(getWFrame().getBall().getPos(), Geometry.getGoalOur().getLeftPost(),
					Geometry.getGoalOur().getRightPost());
			DrawableTriangle dt = new DrawableTriangle(triangle, new Color(125, 125, 125, 100));
			dt.setFill(true);
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_OPPONENT_INTERCEPTION)
					.add(dt);
			
			for (int i = 1; i < getAiFrame().getTacticalField().getEnemiesToBallDist().size(); i++)
			{
				BotDistance bot = getAiFrame().getTacticalField().getEnemiesToBallDist().get(i);
				if (bot.getDist() < searchRadius
						&& dt.getTriangle().isPointInShape(bot.getBot().getPos(), Geometry.getBotRadius()))
				{
					return bot.getDist() + Geometry.getBotRadius() * 2 + 50;
				}
			}
			return InterceptionSkill.getStdDist();
		}
	}
}
