/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.DefenseConstants;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.states.InterceptState;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;
import edu.tigers.sumatra.skillsystem.skills.util.SkillUtil;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;


/**
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public class ManToManMarkerRole extends ADefenseRole
{
	@Configurable(comment = "mark foe to protect this target", defValue = "GOAL")
	private static EProtectionTarget protectionTarget = EProtectionTarget.GOAL;
	
	@Configurable(comment = "minimal Distance to marked foe", defValue = "100.0")
	private static double minDistanceToFoe = 100.0;
	
	@Configurable(comment = "maximal Distance to marked foe", defValue = "2000.0")
	private static double maxDistanceToFoe = 2000;
	
	@Configurable(comment = "time to look ahead in foe trajectory", defValue = "0.25")
	private static double lookahead = 0.25;
	
	@Configurable(comment = "Use FastMove when foe is moving fast", defValue = "false")
	private static boolean useFastMove = false;
	
	private IDefenseThreat threat;
	
	
	/**
	 * @param foe
	 */
	public ManToManMarkerRole(final IDefenseThreat foe)
	{
		super(ERole.MAN_TO_MAN_MARKER);
		threat = foe;
		
		addTransition(EManMarkerEvent.REACHED_INTERSECTION, new ManMarkerState());
		addTransition(EManMarkerEvent.FOE_MOVING_FAST, new ManMarkerInterceptState(this));
		addTransition(EManMarkerEvent.BALL_NEAR_FOE, new HinderFoeState());
		setInitialState(new ManMarkerInterceptState(this));
	}
	
	
	@Override
	public ILineSegment getProtectionLine(final ILineSegment threatLine)
	{
		if (protectionTarget == EProtectionTarget.GOAL)
		{
			return DefenseMath.getThreatDefendingLineForManToManMarker(threatLine, minDistanceToFoe);
		}
		
		return DefenseMath.getThreatDefendingLineToBall(threatLine.getStart(),
				getBall().getTrajectory().getPosByTime(0.1),
				minDistanceToFoe, DefenseConstants.getMinGoOutDistance(), DefenseConstants.getMaxGoOutDistance());
	}
	
	
	private void drawManMarkerShapes(final IVector2 newPosition, final IVector2 toProtect)
	{
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.MAN_MARKER)
				.add(new DrawableCircle(Circle.createCircle(newPosition, Geometry.getBotRadius()), Color.GREEN));
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.MAN_MARKER)
				.add(new DrawableLine(Line.fromPoints(threat.getPos(), toProtect)));
		if (threat.isBot())
		{
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.MAN_MARKER).add(new DrawableLine(
					Line.fromPoints(threat.getPos(),
							protectionTarget == EProtectionTarget.GOAL ? Geometry.getGoalOur().getCenter()
									: getBall().getPos()),
					Color.red));
		}
	}
	
	
	private enum EManMarkerEvent implements IEvent
	{
		REACHED_INTERSECTION,
		FOE_MOVING_FAST,
		BALL_NEAR_FOE
	}
	
	
	private enum EProtectionTarget
	{
		/** */
		GOAL,
		/** */
		BALL
	}
	
	
	private class ManMarkerInterceptState extends InterceptState
	{
		ARole parent;
		
		
		public ManMarkerInterceptState(final ADefenseRole parent)
		{
			super(parent);
			this.parent = parent;
		}
		
		
		@Override
		public void doUpdate()
		{
			parent.getCurrentSkill().getMoveCon()
					.setTheirBotsObstacle(getPos().distanceTo(threat.getPos()) > minDistanceToFoe);
			if (getBall().getPos().distanceTo(threat.getPos()) < minDistanceToFoe)
			{
				triggerEvent(EManMarkerEvent.BALL_NEAR_FOE);
				return;
			}
			ILineSegment protectionLine = getProtectionLine(threat.getThreatLine());
			setInterceptLine(protectionLine);
			super.doUpdate();
			if (protectionLine.distanceTo(getPos()) < Geometry.getBotRadius())
			{
				triggerEvent(EManMarkerEvent.REACHED_INTERSECTION);
			}
		}
	}
	
	
	private class ManMarkerState implements IState
	{
		private AMoveToSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new MoveToTrajSkill();
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.getMoveCon().setTheirBotsObstacle(getPos().distanceTo(threat.getPos()) > minDistanceToFoe);
			if (getBall().getPos().distanceTo(threat.getPos()) < minDistanceToFoe)
			{
				triggerEvent(EManMarkerEvent.BALL_NEAR_FOE);
				return;
			}
			IVector2 toProtect;
			if (protectionTarget == EProtectionTarget.GOAL)
			{
				toProtect = threat.getThreatLine().getEnd();
			} else
			{
				toProtect = getBall().getTrajectory().getPosByTime(0.1);
			}
			
			IVector2 desiredPos = LineMath.stepAlongLine(threat.getPos(), toProtect, minDistanceToFoe);
			ILineSegment protectionLine = getProtectionLine(threat.getThreatLine());
			IVector2 newPosition = protectionLine.closestPointOnLine(desiredPos);
			skill.getMoveCon().updateDestination(newPosition);
			skill.getMoveCon().updateTargetAngle(
					Vector2.fromPoints(Geometry.getGoalOur().getCenter(), newPosition).getAngle());
			drawManMarkerShapes(newPosition, toProtect);
			if (threat.isBot() && (threat.getVel().getLength2() > 0.3))
			{
				triggerEvent(EManMarkerEvent.FOE_MOVING_FAST);
			}
		}
	}
	
	private class HinderFoeState implements IState
	{
		
		private AMoveToSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new MoveToTrajSkill();
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setTheirBotsObstacle(false);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 ballPos = getBall().getTrajectory().getPosByTime(0.1);
			IVector2 toProtect = protectionTarget == EProtectionTarget.GOAL ? threat.getThreatLine().getEnd()
					: ballPos;
			IVector2 desiredPos = (protectionTarget == EProtectionTarget.GOAL) && (ballPos.x() > threat.getPos().x())
					? LineMath.stepAlongLine(threat.getPos(), toProtect, 3 * Geometry.getBotRadius())
					: ballPos;
			
			desiredPos = Geometry.getField().nearestPointInside(desiredPos, -Geometry.getBotRadius() * 2);
			desiredPos = SkillUtil.movePosOutOfPenAreaWrtBall(desiredPos, getBall(),
					Geometry.getPenaltyAreaOur().withMargin(DefenseConstants.getMinGoOutDistance()));
			
			skill.getMoveCon().updateTargetAngle(Vector2.fromPoints(getPos(), ballPos).getAngle());
			skill.getMoveCon().updateDestination(desiredPos);
			drawManMarkerShapes(desiredPos, toProtect);
			
			if (getBall().getPos().distanceTo(threat.getPos()) > (minDistanceToFoe + Geometry.getBotRadius()))
			{
				triggerEvent(EManMarkerEvent.FOE_MOVING_FAST);
			}
		}
	}
}
