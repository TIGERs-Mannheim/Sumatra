/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.SkillUtil;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ProtectBallSkill extends AMoveSkill
{
	@Configurable(comment = "Distance to keep to the ball during protection")
	private static double dist2Ball = 10;
	
	private DynamicPosition protectionTarget;
	
	
	/**
	 * @param protectionTarget
	 */
	public ProtectBallSkill(DynamicPosition protectionTarget)
	{
		super(ESkill.PROTECT_BALL);
		this.protectionTarget = protectionTarget;
		IState protectBallState = new ProtectBallState();
		setInitialState(protectBallState);
	}
	
	
	public void setProtectionTarget(final DynamicPosition protectionTarget)
	{
		this.protectionTarget = protectionTarget;
	}
	
	
	private class ProtectBallState extends MoveToState
	{
		protected ProtectBallState()
		{
			super(ProtectBallSkill.this);
		}
		
		
		@Override
		public void doUpdate()
		{
			// copy for thread-safety
			DynamicPosition curProtectTarget = protectionTarget;
			curProtectTarget.update(getWorldFrame());
			IVector2 dest = AroundBallCalc
					.aroundBall()
					.withBallPos(getBall().getPos())
					.withTBot(getTBot())
					.withDestination(getDestination(0, curProtectTarget))
					.withMaxMargin(50)
					.withMinMargin(dist2Ball)
					.build()
					.getAroundBallDest();
			
			double targetOrientation = getTargetOrientation(dest);
			dest = SkillUtil.movePosInsideFieldWrtBall(dest, getBallPos());
			dest = SkillUtil.movePosOutOfPenAreaWrtBall(dest, getBall(),
					Geometry.getPenaltyAreaOur().withMargin(Geometry.getPenaltyAreaMargin()),
					Geometry.getPenaltyAreaTheir().withMargin(getTBot().getCenter2DribblerDist()));
			
			getMoveCon().updateDestination(dest);
			getMoveCon().updateTargetAngle(targetOrientation);
			
			getMoveCon().setBallObstacle(false);
			
			super.doUpdate();
		}
		
		
		private IVector2 getDestination(double margin, DynamicPosition curProtectTarget)
		{
			return LineMath.stepAlongLine(getBallPos(), curProtectTarget, getDistance(margin));
		}
		
		
		private double getDistance(double margin)
		{
			return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + dist2Ball + margin;
		}
		
		
		private IVector2 getBallPos()
		{
			return getBall().getPos();
		}
		
		
		private double getTargetOrientation(IVector2 dest)
		{
			return getBallPos().subtractNew(dest).getAngle(0);
		}
	}
}
