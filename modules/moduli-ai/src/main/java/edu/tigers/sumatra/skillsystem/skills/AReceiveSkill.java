/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 14, 2014
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.math.RedirectMath;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.test.PositionSkill;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * Receive a ball and stop it
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public abstract class AReceiveSkill extends PositionSkill
{
	private static final Logger	log								= Logger.getLogger(AReceiveSkill.class.getName());
	
	/**  */
	@Configurable(comment = "Time in s, where no more repositioning is allowed")
	public static double				minReceiverPositioningTime	= 0.1;
	
	private IVector2					desiredDestination			= null;
	
	
	/**
	 */
	public enum EReceiverState
	{
		/**  */
		PREPARE,
		/** */
		REDIRECT,
		/**  */
		RECEIVING,
		/**  */
		KEEP_BALL_STOPPED,
		/**  */
		KEEP_BALL_DRIBBLE
	}
	
	
	/**
	 * @param skillname ESkill name
	 */
	public AReceiveSkill(final ESkill skillname)
	{
		super(skillname);
		if (skillname == ESkill.REDIRECT)
		{
			// do nothing here... yet
		} else if (skillname == ESkill.RECEIVER)
		{
			// do nothing here... yet
		} else
		{
			log.error("Invalid inheritance of AReceiveSkill");
		}
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		
		List<IDrawableShape> shapes = new ArrayList<IDrawableShape>();
		
		if (desiredDestination == null)
		{
			desiredDestination = getPos();
			setDestination(desiredDestination);
		}
		
		IVector3 pose = RedirectMath.validateDest(getWorldFrame(), getTBot(), getPose());
		
		Line ballLine;
		TrackedBall ball = getWorldFrame().getBall();
		if (ball.getVel().getLength() > 0.1)
		{
			ballLine = new Line(ball.getPos(), ball.getVel());
		} else
		{
			ballLine = Line.newLine(getDesiredDestination(), ball.getPos());
		}
		IVector2 dest = GeoMath.leadPointOnLine(getDesiredDestination(), ballLine);
		dest = RedirectMath.validateDest(getWorldFrame(), getTBot(), new Vector3(dest, pose.z())).getXYVector();
		
		if (RedirectMath.isPositionReachable(getWorldFrame(), getTBot(), dest))
		{
			setDestination(dest);
		} else
		{
			DrawableCircle dc = new DrawableCircle(pose.getXYVector(), 100, Color.green);
			shapes.add(dc);
			setDestination(pose.getXYVector());
		}
		setOrientation(pose.z());
		DrawableCircle dc = new DrawableCircle(new Circle(desiredDestination, 120), new Color(0, 255, 255, 100));
		dc.setFill(true);
		shapes.add(dc);
		getPathDriver().setShapes(EShapesLayer.REDIRECT_SKILL, shapes);
	}
	
	
	protected abstract IVector3 getPose();
	
	
	/**
	 * @return the desiredDestination
	 */
	public IVector2 getDesiredDestination()
	{
		return desiredDestination;
	}
	
	
	/**
	 * @param desiredDestination the desiredDestination to set
	 */
	public void setDesiredDestination(final IVector2 desiredDestination)
	{
		this.desiredDestination = desiredDestination;
	}
	
	
	protected boolean isPointInPenaltyArea(final IVector2 point)
	{
		return Geometry.getPenaltyAreaTheir().isPointInShape(point, -Geometry.getBotRadius());
	}
	
	
	protected boolean isPointInField(final IVector2 point)
	{
		return Geometry.getField().isPointInShape(point, -Geometry.getBotRadius());
	}
}