/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense.states;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.DefenseConstants;
import edu.tigers.sumatra.ai.pandora.roles.defense.ADefenseRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;
import edu.tigers.sumatra.skillsystem.skills.util.InterceptorUtil;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>
 *         State used by Defender roles. Allows interception of given line defined by two dynamic positions.
 */
public class InterceptState implements IState
{
	private ADefenseRole parent;
	private AMoveToSkill skill;
	private DynamicPosition toIntercept;
	private DynamicPosition toProtect;
	private ILineSegment interceptLine;
	
	private double lookahead = 0.1;
	
	
	/**
	 * State used by defender Roles
	 * possible settings:
	 * 1. intercept line between ball and foeBot
	 * 2. intercept line between ball and goal center
	 * 3. intercept line between foeBot and goal center
	 * 4. intercept line between two foeBots
	 * 5. intercept line between two points
	 *
	 * @param parent role that executes this state
	 * @param toIntercept start point of interception line: ball or bot (or any dynamic position)
	 * @param toProtect end point of interception line: bot or goal (or any dynamic position)
	 */
	public InterceptState(ADefenseRole parent, DynamicPosition toIntercept, DynamicPosition toProtect)
	{
		this.parent = parent;
		Validate.notNull(toIntercept);
		Validate.notNull(toProtect);
		if (toIntercept.getTrackedId().equals(toProtect.getTrackedId())
				&& toIntercept.getXYVector().equals(toProtect.getXYVector()))
		{
			throw new IllegalArgumentException("toIntercept and toProtect should not be the same DynamicPosition");
		}
		this.toIntercept = toIntercept;
		this.toProtect = toProtect;
	}
	
	
	/**
	 * interceptLine needs to be set after creation!!!
	 * 
	 * @param parent role that executes this state
	 */
	public InterceptState(ADefenseRole parent)
	{
		IVector2 goalCenter = Geometry.getCenter();
		
		this.parent = parent;
		this.interceptLine = Lines.segmentFromPoints(goalCenter, DefenseMath.getBisectionGoal(goalCenter));
	}
	
	
	@Override
	public void doEntryActions()
	{
		skill = new MoveToTrajSkill();
		parent.setNewSkill(skill);
	}
	
	
	@Override
	public void doUpdate()
	{
		IVector2 threat;
		if (toProtect != null && toIntercept != null)
		{
			toIntercept.setLookahead(lookahead);
			toIntercept.update(parent.getWFrame());
			threat = toIntercept;
			toProtect.setLookahead(lookahead);
			toProtect.update(parent.getWFrame());
			if (toIntercept.getXYVector().equals(toProtect.getXYVector()))
			{
				toProtect.setPos(DefenseMath.getBisectionGoal(toIntercept).addNew(Vector2.fromX(1)));
			}
			
			interceptLine = getLineToIntercept();
		} else
		{
			threat = interceptLine.getStart();
		}
		IVector2 interceptionPoint = InterceptorUtil.fastestPointOnLine(interceptLine, parent.getBot(),
				skill.getMoveCon().getMoveConstraints()).getTarget();
		
		interceptionPoint = getClosestPosInAllowedArea(interceptionPoint, threat);
		interceptionPoint = parent.getValidPositionByIcing(interceptionPoint);
		skill.getMoveCon().updateDestination(interceptionPoint);
		drawShapes(interceptLine, interceptionPoint);
	}
	
	
	private ILineSegment getLineToIntercept()
	{
		IVector2 end = isBotOrBall(toProtect)
				? LineMath.stepAlongLine(toProtect, toIntercept, Geometry.getBotRadius() * 3) : toProtect;
		IVector2 start = isBotOrBall(toIntercept) ? LineMath.stepAlongLine(toIntercept, end,
				Geometry.getBotRadius() * 3) : toIntercept;
		return Lines.segmentFromPoints(start, end);
	}
	
	
	private boolean isBotOrBall(DynamicPosition pos)
	{
		return pos.getTrackedId().isBall() || pos.getTrackedId().isBot();
	}
	
	
	private IVector2 getClosestPosInAllowedArea(IVector2 pos, IVector2 toIntercept)
	{
		IVector2 newPos = Geometry.getPenaltyAreaTheir().withMargin(DefenseConstants.getMinGoOutDistance())
				.nearestPointOutside(pos, toIntercept);
		newPos = Geometry.getPenaltyAreaOur().withMargin(DefenseConstants.getMinGoOutDistance())
				.nearestPointOutside(newPos, toIntercept);
		return Geometry.getField().nearestPointInside(newPos, -2 * Geometry.getBotRadius());
	}
	
	
	private void drawShapes(ILineSegment interceptLine, IVector2 newPos)
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		shapes.add(new DrawableLine(interceptLine));
		shapes.add(new DrawableCircle(Circle.createCircle(newPos, Geometry.getBotRadius()), Color.GREEN));
		parent.getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.INTERCEPT_STATE).addAll(shapes);
	}
	
	
	// --------------------------------------------------------------------------
	// --- GETTER and SETTER-----------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected void setToIntercept(final DynamicPosition toIntercept)
	{
		Validate.notNull(toIntercept);
		this.toIntercept = toIntercept;
	}
	
	
	protected void setToProtect(final DynamicPosition toProtect)
	{
		Validate.notNull(toProtect);
		this.toProtect = toProtect;
	}
	
	
	protected double getLookahead()
	{
		return lookahead;
	}
	
	
	protected void setLookahead(final double lookahead)
	{
		this.lookahead = lookahead;
	}
	
	
	protected ILineSegment getInterceptionLine()
	{
		return interceptLine;
	}
	
	
	protected void setInterceptLine(final ILineSegment interceptLine)
	{
		this.interceptLine = interceptLine;
	}
	
}
