/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.states;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.PassReceiverCalc;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.triangle.Triangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/** This state fakes a receiver */
public class FakePassReceiverState extends ASupporterState
{
	
	private AMoveToSkill moveToSkill;
	
	@Configurable(defValue = "10")
	private static double distanceToPassLine = 10;
	
	static
	{
		ConfigRegistration.registerClass("roles", GlobalPositionRunnerState.class);
	}
	
	
	/**
	 * @param role the parent of the states
	 */
	public FakePassReceiverState(final SupportRole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		moveToSkill = new MoveToTrajSkill();
		setNewSkill(moveToSkill);
	}
	
	
	@Override
	public void doUpdate()
	{
		ITrackedBall ball = parent.getWFrame().getBall();
		IVector2 destination = calcNormalFakeDestination(getPos(), distanceToPassLine);
		destination = checkNearReceiverAndCorrect(destination);
		
		if (ball.getVel().getLength2() > 0.001)
		{
			
			moveToSkill.getMoveCon().updateDestination(destination);
			moveToSkill.getMoveCon().updateLookAtTarget(Geometry.getGoalTheir().getCenter());
		} else
		{
			triggerEvent(SupportRole.EEvent.MOVE);
		}
	}
	
	
	private IVector2 calcNormalFakeDestination(IVector2 origin, double distanceToPassLine)
	{
		ITrackedBall ball = parent.getWFrame().getBall();
		DefenseMath.ReceiveData receiveData = new DefenseMath.ReceiveData(parent.getBot(),
				ball.getTrajectory().getPlanarCurve(), ball.getPos());
		if (receiveData.getDistToBallCurve() > PassReceiverCalc.getValidReceiveDistance()
				|| ball.getVel().getLength() < 0.01)
		{
			return getPos();
		}
		ILine passLine = Lines.lineFromDirection(ball.getPos(), ball.getVel());
		IVector2 leadPoint = passLine.closestPointOnLine(origin);
		double time = ball.getTrajectory().getTimeByPos(leadPoint);
		if (Double.isInfinite(time))
		{
			time = parent.getBot().getVel().getLength2();
		}
		
		IVector2 destination = passLine.closestPointOnLine(
				origin.addNew(parent.getBot().getVel().scaleToNew(time)));
		IVector2 orthogonal = origin.subtractNew(leadPoint);
		if (orthogonal.isZeroVector())
		{
			orthogonal = passLine.getOrthogonalLine().directionVector();
		}
		destination = destination
				.addNew(orthogonal
						.scaleToNew(Geometry.getBotRadius() + Geometry.getBallRadius() * 2 + distanceToPassLine));
		if (Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius()).isPointInShape(destination))
		{
			destination = Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius())
					.nearestPointOutside(destination);
		}
		if (Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius()).isPointInShape(destination))
		{
			destination = Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius())
					.nearestPointOutside(destination);
		}
		return destination;
	}
	
	
	private IVector2 checkNearReceiverAndCorrect(IVector2 destination)
	{
		IVector2 safeDestination = destination;
		if (getAiFrame().getTacticalField().getOffensiveStrategy().getDesiredBots().iterator().hasNext())
		{
			BotID offensiveBot = getAiFrame().getTacticalField().getOffensiveStrategy().getDesiredBots().iterator().next();
			ITrackedBot primaryOffensive = getWFrame().getBot(offensiveBot);
			ICircle safetyCircle = Circle.createCircle(primaryOffensive.getPos(), 10 * Geometry.getBotRadius());
			if (safetyCircle.isPointInShape(destination))
			{
				safeDestination = safetyCircle.nearestPointOutside(destination);
			}
			Triangle shootTriangle = Triangle.fromCorners(Geometry.getGoalTheir().getLeftPost(),
					Geometry.getGoalTheir().getRightPost(), primaryOffensive.getBotKickerPos());
			if (shootTriangle.isPointInShape(destination, Geometry.getBotRadius() * 4))
			{
				safeDestination = calcNormalFakeDestination(safeDestination, Geometry.getGoalTheir().getWidth());
			}
		}
		return safeDestination;
	}
}
