/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


public class KeepDistToBallRole extends ARole
{
	private static final Logger log = Logger.getLogger(KeepDistToBallRole.class.getName());
	
	
	public KeepDistToBallRole()
	{
		super(ERole.KEEP_DIST_TO_BALL);
		
		setInitialState(new DefaultState());
	}
	
	private class DefaultState extends AState
	{
		@Override
		public void doEntryActions()
		{
			AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateLookAtTarget(new DynamicPosition(getWFrame().getBall()));
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 moveTarget = calcStopMoveTarget(getWFrame(), getBotID());
			moveTarget = AiMath.adjustMovePositionWhenItsInvalid(getWFrame(), getBotID(), moveTarget);
			getCurrentSkill().getMoveCon().updateDestination(moveTarget);
		}
		
		
		private IVector2 calcStopMoveTarget(final WorldFrame wFrame, final BotID key)
		{
			IVector2 ballPos = wFrame.getBall().getPos();
			IVector2 moveTarget;
			double distanceToBall = RuleConstraints.getStopRadius() + Geometry.getBotRadius() + 50;
			
			int safteyCounter = 0;
			do
			{
				ICircle positionCircle = Circle.createCircle(ballPos, distanceToBall);
				moveTarget = ballPos.subtractNew(Vector2.fromXY(RuleConstraints.getStopRadius(), 0));
				moveTarget = positionCircle.nearestPointOutside(moveTarget);
				
				if (safteyCounter > 8)
				{
					log.warn("Offensive Stop State could not find a valid Position.");
					break;
				}
				
				// check circle around ball
				for (int i = 0; i < 18; i++)
				{
					moveTarget = CircleMath.stepAlongCircle(moveTarget, ballPos, AngleMath.deg2rad(20));
					if (isMoveTargetValid(moveTarget, wFrame, key))
					{
						break;
					}
				}
				
				// bigger radius for next circle check
				distanceToBall += Geometry.getBotRadius() * 1.2;
				safteyCounter++;
			} while (!isMoveTargetValid(moveTarget, wFrame, key));
			return moveTarget;
		}
		
		
		private boolean isMoveTargetValid(final IVector2 moveTarget, final WorldFrame wFrame, final BotID key)
		{
			double marginPenalty = Geometry.getBotRadius() + Geometry.getPenaltyAreaMargin();
			if (!Geometry.getField().isPointInShape(moveTarget))
			{
				return false;
			}
			if (Geometry.getPenaltyAreaOur().isPointInShape(moveTarget, marginPenalty))
			{
				return false;
			}
			if (Geometry.getPenaltyAreaTheir().isPointInShape(moveTarget, marginPenalty))
			{
				return false;
			}
			
			BotIDMap<ITrackedBot> botMap = new BotIDMap<>(wFrame.getBots());
			botMap.remove(key);
			
			for (BotID id : botMap.keySet())
			{
				if (VectorMath.distancePP(moveTarget, botMap.getWithNull(id).getPos()) < (Geometry.getBotRadius() * 3))
				{
					return false;
				}
			}
			return true;
		}
	}
}
