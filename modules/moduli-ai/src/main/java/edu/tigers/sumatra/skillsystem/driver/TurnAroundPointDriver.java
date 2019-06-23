/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.01.2016
 * Author(s): kisle
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author MarkG <Mark.Geiger@dlr>
 */
public class TurnAroundPointDriver extends ABaseDriver
{
	private final double		circleRadius;
									
	private final boolean	rightTurn;
									
	// PID - Controller parameters
	private double				kI			= 0.0005;
	private double				kP			= 0.001;
	private double				kD			= 0.0;
	private double				iSum		= 0;
	private double				oiSum		= 0;
	// private double orient;
	
	private IVector2			target	= null;
												
												
	/**
	 * @param center
	 * @param radius
	 * @param turnRight 1 = turn right, 0 = turn left
	 */
	public TurnAroundPointDriver(final IVector2 center, final float radius, final boolean turnRight)
	{
		addSupportedCommand(EBotSkill.LOCAL_VELOCITY);
		rightTurn = turnRight;
		circleRadius = radius;
		target = center;
	}
	
	
	@Override
	public IVector3 getNextDestination(final ITrackedBot bot, final WorldFrame wFrame)
	{
		return null;
	}
	
	
	@Override
	public IVector3 getNextVelocity(final ITrackedBot bot, final WorldFrame wFrame)
	{
		List<IDrawableShape> shapes = new ArrayList<IDrawableShape>();
		IVector2 dest = null;
		// IVector2 targetToBall = getWorldFrame().getBall().getPos().subtractNew(target).normalizeNew();
		// double toBallDist = circleRadius - 100;
		
		IVector2 botToTarget = target.subtractNew(bot.getPos()).normalizeNew();
		IVector2 behindTarget = target.addNew(botToTarget.multiplyNew(20));
		
		// dest = getWorldFrame().getBall().getPos().addNew(targetToBall.multiplyNew(toBallDist));
		
		IVector2 normal = botToTarget.getNormalVector().normalizeNew();
		IVector2 behindWOffset1 = behindTarget.addNew(normal.multiplyNew(20));
		IVector2 behindWOffset2 = behindTarget.addNew(normal.multiplyNew(-20));
		
		if (!rightTurn)
		{
			dest = behindWOffset2;
		} else
		{
			dest = behindWOffset1;
		}
		
		Circle circle = new Circle(target, circleRadius);
		Line botDestLine = Line.newLine(bot.getPos(), dest);
		
		IVector2 startPoint = null;
		IVector2 endPoint = null;
		if (circle.isLineIntersectingShape(botDestLine))
		{
			List<IVector2> intersections = circle.lineIntersections(botDestLine);
			for (IVector2 key : intersections)
			{
				if ((startPoint == null) || (bot.getPos().subtractNew(startPoint).getLength2() > bot.getPos()
						.subtractNew(key).getLength2()))
				{
					startPoint = key;
				}
			}
			shapes.add(new DrawableCircle(new Circle(startPoint, 100), Color.RED));
		}
		
		if (circle.isLineIntersectingShape(Line.newLine(target, dest)))
		{
			List<IVector2> intersections = circle.lineIntersections(Line.newLine(target, dest));
			for (IVector2 key : intersections)
			{
				if ((endPoint == null) || (dest.subtractNew(endPoint).getLength2() > dest
						.subtractNew(key).getLength2()))
				{
					endPoint = key;
				}
			}
			shapes.add(new DrawableCircle(new Circle(endPoint, 100), Color.BLUE));
		}
		
		if (endPoint == null)
		{
			throw new IllegalArgumentException("nein nein nein!");
		}
		
		// System.out.println(AngleMath.RAD_TO_DEG * GeoMath.angleBetweenVectorAndVector(botToDest, destToBall));
		
		shapes.add(new DrawableLine(Line.newLine(bot.getPos(), dest)));
		shapes.add(new DrawableCircle(circle));
		
		IVector2 destPoint = startPoint;
		double distToDest = bot.getPos().subtractNew(destPoint).getLength2();
		
		IVector2 botToBall = target.subtractNew(bot.getPos());
		IVector2 normal1 = botToBall.getNormalVector().normalizeNew().multiplyNew(300);
		IVector2 normal2 = botToBall.getNormalVector().normalizeNew().multiplyNew(-300);
		IVector2 ballToEndPoint = endPoint.subtractNew(target);
		
		double angle1 = GeoMath.angleBetweenVectorAndVector(ballToEndPoint, normal1);
		
		if ((angle1 * AngleMath.RAD_TO_DEG) > 90)
		{
			shapes.add(new DrawableLine(new Line(startPoint, normal2), Color.gray));
			destPoint = bot.getPos().addNew(normal2);
		} else
		{
			shapes.add(new DrawableLine(new Line(startPoint, normal1), Color.gray));
			destPoint = bot.getPos().addNew(normal1);
		}
		distToDest = bot.getPos().subtractNew(endPoint).getLength2();
		
		
		if (bot.getPos().subtractNew(endPoint).getLength2() < 40)
		{
			destPoint = bot.getPos();
		}
		
		double distToBall = target.subtractNew(bot.getPos()).getLength2();
		double e = distToBall - circleRadius;
		
		double a = 0;
		iSum += e;
		a += (iSum * kI * 0.1) + (kP * e) + (kD * 0); // currently only PI - Controller
		
		float sgn = 1;
		if (wFrame.isInverted())
		{
			// sgn = -1;
		}
		IVector2 targetDir = bot.getPos().subtractNew(destPoint).normalizeNew().multiplyNew(sgn * calcSpeed(distToDest));
		targetDir = targetDir.turnToNew(targetDir.getAngle() - a);
		
		// orientation here.... this sucks.
		double oe = -AngleMath.getShortestRotation(target.subtractNew(bot.getPos()).getAngle(),
				bot.getAngle());
		oiSum += oe * 0.1;
		double b = Math.max(Math.min((7 * oe) + (0.1 * oiSum), 5), -5);
		
		setShapes(EShapesLayer.OFFENSIVE, shapes);
		
		return new Vector3(targetDir, b);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.CUSTOM;
	}
	
	
	private double calcSpeed(final double distance)
	{
		if (distance < 200)
		{
			return distance / 200.0;
		}
		return 1.0;
	}
	
	
	/**
	 * @return the target
	 */
	public IVector2 getTarget()
	{
		return target;
	}
	
	
	/**
	 * @param target the target to set
	 */
	public void setTarget(final IVector2 target)
	{
		this.target = target;
	}
}