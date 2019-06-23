/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.DrawableEllipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.EApexType;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.Ellipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine.ETextLocation;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * Test the DEBUG shapes
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class DebugShapesTestPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int		ROT_STEP_DIV		= 32;
	private static final float		ROT_STEP				= (float) Math.PI / ROT_STEP_DIV;
	private static final int		ROT_SPEED			= 1;
	private static final int		POS_EDGE_X			= 2000;
	private static final int		POS_EDGE_Y			= 1500;
	
	private final MoveRole			role;
	private final List<IVector2>	path;
	private int							currentPathPoint	= -1;
	private IVector2					curDestination		= Vector2.ZERO_VECTOR;
	
	private float						curRotation			= 0;
	private int							speedCounter		= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public DebugShapesTestPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		setTimeout(Long.MAX_VALUE);
		
		role = new MoveRole(EMoveBehavior.LOOK_AT_BALL);
		addDefensiveRole(role, aiFrame.worldFrame.ball.getPos());
		
		path = new ArrayList<IVector2>();
		path.add(new Vector2(0, 0));
		path.add(new Vector2(-POS_EDGE_X, POS_EDGE_Y));
		path.add(new Vector2(-POS_EDGE_X, -POS_EDGE_Y));
		path.add(new Vector2(POS_EDGE_X, -POS_EDGE_Y));
		path.add(new Vector2(POS_EDGE_X, POS_EDGE_Y));
		changeDestination();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		final List<IDrawableShape> shapes = new LinkedList<IDrawableShape>();
		
		// lines
		DrawableLine l1 = new DrawableLine(new Line(Vector2.ZERO_VECTOR, role.getPos()), Color.red);
		l1.setText("Bot");
		l1.setTextLocation(ETextLocation.CENTER);
		shapes.add(l1);
		IDrawableShape l2 = new DrawableLine(new Line(role.getPos(), AIConfig.getGeometry().getGoalTheir()
				.getGoalCenter().subtractNew(role.getPos())), Color.green);
		shapes.add(l2);
		
		// circles
		final float rad = AIConfig.getGeometry().getBotRadius() * 2;
		IDrawableShape c1 = new DrawableCircle(new Circle(role.getPos(), rad), Color.cyan);
		shapes.add(c1);
		IDrawableShape c2 = new DrawableCircle(new Circle(Vector2.ZERO_VECTOR, rad / 2), Color.magenta);
		shapes.add(c2);
		
		// ellipses around bot
		IDrawableShape e1 = new DrawableEllipse(new Ellipse(role.getPos(), 200, 130), Color.black);
		shapes.add(e1);
		IDrawableShape e2 = new DrawableEllipse(new Ellipse(role.getPos(), 130, 200), Color.darkGray);
		shapes.add(e2);
		
		// rotating ellipse
		IVector2 center = new Vector2(1000, -1000);
		float ra = 500;
		float rb = 150;
		float outerOffset = 100;
		IDrawableShape e4 = new DrawableEllipse(new Ellipse(center, ra, rb, curRotation), Color.darkGray);
		shapes.add(e4);
		DrawableEllipse ec1 = new DrawableEllipse(new Ellipse(center, ra, rb, curRotation), Color.orange);
		ec1.setCurve(ec1.getApex(EApexType.MAIN_NEG), ec1.getCircumference() / 4);
		shapes.add(ec1);
		DrawableEllipse ec2 = new DrawableEllipse(new Ellipse(center, ra, rb, curRotation), Color.red);
		ec2.setCurve(ec2.getApex(EApexType.SEC_POS), ec2.getCircumference() / 4);
		shapes.add(ec2);
		DrawableEllipse ec3 = new DrawableEllipse(new Ellipse(center, ra, rb, curRotation), Color.green);
		ec3.setCurve(ec3.getApex(EApexType.MAIN_POS), -ec3.getCircumference() / 4);
		shapes.add(ec3);
		DrawableEllipse ec4 = new DrawableEllipse(new Ellipse(center, ra + outerOffset, rb + outerOffset, curRotation),
				Color.magenta);
		ec4.setCurve(ec4.getApex(EApexType.CENTER_NORTH), ec4.getCircumference() / 2);
		shapes.add(ec4);
		DrawableEllipse ec5 = new DrawableEllipse(new Ellipse(center, ra + outerOffset, rb + outerOffset, curRotation),
				Color.blue);
		ec5.setCurve(ec5.getApex(EApexType.CENTER_SOUTH), ec5.getCircumference() / 2);
		shapes.add(ec5);
		
		for (float i = -(float) Math.PI / 2; i <= ((float) Math.PI / 2); i = i + ((float) Math.PI / 8))
		{
			IDrawableShape e = new DrawableEllipse(new Ellipse(role.getPos(), 250, 500, i), Color.red);
			shapes.add(e);
		}
		
		for (IDrawableShape shape : shapes)
		{
			frame.addDebugShape(shape);
		}
		
		role.updateDestination(curDestination);
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		EConditionState state = role.checkMovementCondition(currentFrame.worldFrame);
		if (state == EConditionState.FULFILLED)
		{
			changeDestination();
		} else if (state == EConditionState.BLOCKED)
		{
			curDestination = GeoMath.stepAlongLine(curDestination, Vector2.ZERO_VECTOR, -50);
		}
		
		if (speedCounter > ROT_SPEED)
		{
			curRotation += ROT_STEP;
			if (curRotation > Math.PI)
			{
				curRotation = -(float) Math.PI;
			}
			speedCounter = 0;
		}
		speedCounter++;
	}
	
	
	private void changeDestination()
	{
		currentPathPoint++;
		currentPathPoint %= path.size();
		curDestination = path.get(currentPathPoint);
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
