/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.InterceptionSkill;

import java.awt.Color;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleInterceptionState extends AOffensiveRoleState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //

	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	private InterceptionSkill intercept = null;
	private AMoveToSkill move = null;
	
	
	/**
	 * @param role
	 */
	public OffensiveRoleInterceptionState(final OffensiveRole role)
	{
		super(role);
	}
	
	
	@Override
	public IVector2 getMoveDest()
	{
		if (getCurrentSkill().getType().toString().equals(ESkill.INTERCEPTION.toString()))
		{
			return intercept.getLastValidDestination();
		}
		return getCurrentSkill().getMoveCon().getDestination();
	}
	
	
	@Override
	public String getIdentifier()
	{
		return OffensiveStrategy.EOffensiveStrategy.INTERCEPT.name();
	}
	
	
	@Override
	public void doExitActions()
	{
		// nothing has to be done here
	}
	
	
	@Override
	public void doEntryActions()
	{
		intercept = new InterceptionSkill();
		move = AMoveToSkill.createMoveToSkill();
		setNewSkill(move);
	}
	
	
	@Override
	public void doUpdate()
	{
		IVector2 movePos = calcMovePosition();
		movePos = AiMath.adjustMovePositionWhenItsInvalid(getWFrame(), getBotID(), movePos);
		move.getMoveCon().updateDestination(movePos);
		if (movePos.distanceTo(getPos()) < 50 &&
				getCurrentSkill().getType() != ESkill.INTERCEPTION)
		{
			setNewSkill(intercept);
		}
		BotDistance nearestEnemyBot = getAiFrame().getTacticalField().getEnemyClosestToBall();
		if (nearestEnemyBot != null)
		{
			if (nearestEnemyBot.getBot() != null)
			{
				intercept.setNearestEnemyBotPos(nearestEnemyBot.getBot().getPos());
			} else
			{
				intercept.setNearestEnemyBotPos(null);
			}
		} else
		{
			intercept.setNearestEnemyBotPos(null);
		}
		if (OffensiveConstants.isIsSmartDistanceCalcAllowedForInterceptor())
		{
			double distance = calcDistancToEnemy();
			intercept.setDistanceToBall(distance);
		}
	}
	
	
	private IVector2 calcMovePosition()
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		IVector2 goal = Geometry.getGoalOur().getCenter();
		IVector2 dir = goal.subtractNew(ballPos).normalizeNew();
		return ballPos.addNew(dir.multiplyNew(Geometry.getBotToBallDistanceStop()
				+ (Geometry.getBotRadius() * 3)));
	}
	
	
	private double calcDistancToEnemy()
	{
		final double searchRadius = 1500;
		Triangle triangle = Triangle.fromCorners(getWFrame().getBall().getPos(), Geometry.getGoalOur().getLeftPost(),
				Geometry.getGoalOur().getRightPost());
		DrawableTriangle dt = new DrawableTriangle(triangle, new Color(125, 125, 125, 100));
		dt.setFill(true);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(dt);
		
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
