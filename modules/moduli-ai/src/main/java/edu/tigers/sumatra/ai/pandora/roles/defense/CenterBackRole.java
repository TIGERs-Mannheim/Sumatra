/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.data.IDefenseThreat;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.defense.states.InterceptState;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;


/**
 * Protects the goal from a given threat by driving
 * on the line between goal and threat covering the whole goal.
 *
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class CenterBackRole extends ADefenseRole
{
	
	@Configurable(comment = "The space between the bots (actual distance = configured distance + bot diameter)", defValue = "10")
	private static double distanceBetweenBots = 10;
	@Configurable(comment = "Check if CenterBack should stay closer to goal when more than one bot defends same threat", defValue = "true")
	private static boolean goBackWhenInGroup = true;

	private CoverMode coverMode;
	private IDefenseThreat threat;
	private Set<BotID> companions = new HashSet<>();
	private boolean defendCloserToGoal = false;

	
	/**
	 * Creates a new CenterBackRole to protect the goal from the given threat
	 *
	 * @param threat The threat
	 * @param coverMode The coverMode
	 */
	public CenterBackRole(final IDefenseThreat threat, final CoverMode coverMode)
	{
		
		super(ERole.CENTER_BACK);
		this.threat = threat;
		this.coverMode = coverMode;
		
		addTransition(EEvent.FAR_AWAY, new CenterInterceptState(this));
		addTransition(EEvent.REACHED_INTERCEPT_POINT, new CenterBackState());
		setInitialState(new CenterBackState());
	}
	
	
	public void setThreat(final IDefenseThreat threat)
	{
		this.threat = threat;
	}
	

	public void setCoverMode(final CoverMode coverMode)
	{
		this.coverMode = coverMode;
	}
	

	public void setCompanions(final Set<BotID> companions)
	{
		this.companions = companions;
	}
	

	public void setDefendCloserToGoal(final boolean defendCloserToGoal)
	{
		this.defendCloserToGoal = defendCloserToGoal;
	}


	private double getDistanceToProtectionLine(double distance)
	{
		switch (coverMode)
		{
			case LEFT:
				return distance;
			case RIGHT:
				return -distance;
			case CENTER_LEFT:
				return distance / 2;
			case CENTER_RIGHT:
				return -(distance / 2);
			case CENTER:
				return 0.0;
			default:
				throw new IllegalStateException("Unknown CoverMode!");
		}
	}
	
	
	@Override
	public ILineSegment getProtectionLine(final ILineSegment threatLine)
	{
		return DefenseMath.getThreatDefendingLineForCenterBack(threatLine);
	}
	
	
	/**
	 * Specifies the bots position
	 */
	public enum CoverMode
	{
		/** On the line between threat and goal **/
		CENTER,
		/** Left from CENTER */
		LEFT,
		/** Right from CENTER */
		RIGHT,
		/** Left position if two bots are assigned to center */
		CENTER_LEFT,
		/** Right position if two bots are assigned to center */
		CENTER_RIGHT
	}
	
	
	/**
	 * CenterBack events
	 */
	public enum EEvent implements IEvent
	{
		/** */
		FAR_AWAY,
		/** */
		REACHED_INTERCEPT_POINT
	}
	
	private class CenterBackState implements IState
	{
		
		private AMoveToSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			setNewSkill(skill);
		}
		
		
		@SuppressWarnings("squid:MethodCyclomaticComplexity")
		@Override
		public void doUpdate()
		{
			skill.getMoveCon().setIgnoredBots(companions);
			skill.getMoveCon().setBallObstacle(isBehindBall());
			armDefenders(skill);
			
			
			Goal goal = Geometry.getGoalOur();
			
			double distance = (Geometry.getBotRadius() * 2) + distanceBetweenBots;
			double width = Geometry.getBotRadius();
			if (defendCloserToGoal && goBackWhenInGroup)
			{
				width = (companions.size() == 2 ? distance / 2 : distance) + width;
			}
			IVector2 dest = DefenseMath.calculateLineDefPoint(threat.getPos(), goal.getLeftPost(), goal.getRightPost(),
					width);
			ILineSegment protectionLine = getProtectionLine(threat.getThreatLine());
			dest = protectionLine.closestPointOnLine(dest);
			
			DrawableLine dProtectionLine = new DrawableLine(protectionLine, Color.GRAY);
			dProtectionLine.setStrokeWidth(20);
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.CENTER_BACK).add(dProtectionLine);
			
			ILine goalLine = Line.fromPoints(threat.getPos(), goal.getCenter());
			ILine orthogonalLine = goalLine.getOrthogonalLine();
			
			ILine positioningLine = Line.fromDirection(dest, orthogonalLine.directionVector());

			distance = getDistanceToProtectionLine(distance);
			
			IVector2 finalDest = LineMath.stepAlongLine(dest, positioningLine.getEnd(), distance);
			finalDest = getValidPositionByIcing(finalDest);
			skill.getMoveCon().updateDestination(finalDest);
			
			ILine lineToGoal = Line.fromPoints(Geometry.getGoalOur().getCenter(), getPos());
			
			double targetAngle = lineToGoal.getAngle().orElse(0.0);
			skill.getMoveCon().updateTargetAngle(targetAngle);
			
			skill.getMoveCon().setBotsObstacle(getPos().distanceTo(finalDest) > 200);
			drawCenterBackShapes();
			
			if (protectionLine.distanceTo(getPos()) > ((2 * Geometry.getBotRadius()) + Math.abs(distance)))
			{
				triggerEvent(EEvent.FAR_AWAY);
			}
		}
		
		
		private void drawCenterBackShapes()
		{
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.CENTER_BACK)
					.add(new DrawableLine(Line.fromPoints(threat.getPos(), Geometry.getGoalOur().getLeftPost())));
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.CENTER_BACK)
					.add(new DrawableLine(Line.fromPoints(threat.getPos(), Geometry.getGoalOur().getRightPost())));
		}
		
		
		private boolean isBehindBall()
		{
			IVector2 ball2Goal = Geometry.getGoalOur().getCenter().subtractNew(getBall().getPos());
			IVector2 ball2Bot = getPos().subtractNew(getBall().getPos());
			double angle = ball2Goal.angleToAbs(ball2Bot).orElse(0.0);
			return angle > AngleMath.PI_HALF;
		}
	}
	
	private class CenterInterceptState extends InterceptState
	{
		
		/**
		 * intercept line between two points
		 *
		 * @param parent role that executes this state
		 */
		public CenterInterceptState(final ADefenseRole parent)
		{
			super(parent);
		}
		
		
		@Override
		public void doUpdate()
		{
			ILineSegment protectionLine = getProtectionLine(threat.getThreatLine());
			
			IVector2 orthoDir = Line.fromPoints(threat.getPos(), Geometry.getGoalOur().getCenter()).directionVector()
					.getNormalVector();
			double distance = getDistanceToProtectionLine((Geometry.getBotRadius() * 2) + distanceBetweenBots);
			
			IVector2 start = protectionLine.getStart().addNew(orthoDir.scaleToNew(distance));
			IVector2 end = protectionLine.getEnd().addNew(orthoDir.scaleToNew(distance));
			protectionLine = Lines.segmentFromPoints(start, end);
			
			setInterceptLine(protectionLine);
			super.doUpdate();
			if (protectionLine.distanceTo(getPos()) < Geometry.getBotRadius())
			{
				triggerEvent(EEvent.REACHED_INTERCEPT_POINT);
			}
		}
	}
	
}
