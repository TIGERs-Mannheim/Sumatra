/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 18, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.Triangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.ECommandType;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickBallV2Driver extends ABaseDriver implements IKickPathDriver
{
	private final DynamicPosition	receiver;
	
	@Configurable
	private static float				movingSpeed		= 1.0f;
	
	@Configurable
	private static float				triangleAngle	= 0.5f;
	
	
	/**
	 * @param receiver
	 */
	public KickBallV2Driver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
		addSupportedCommand(ECommandType.VEL);
	}
	
	
	@Override
	public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 ballAdapted = GeoMath.stepAlongLine(ballPos, receiver, bot.getBot().getCenter2DribblerDist());
		IVector2 velDir = ballAdapted.subtractNew(bot.getPos());
		float minMovingSpeed = movingSpeed;
		if (bot.getVel().getLength2() > 0.2)
		{
			float angleDiff = AngleMath.getShortestRotation(bot.getVel().getAngle(), velDir.getAngle());
			if (Math.abs(angleDiff) > AngleMath.PI_HALF)
			{
				angleDiff = Math.signum(angleDiff) * (AngleMath.PI - Math.abs(angleDiff));
			}
			velDir = velDir.turnNew(angleDiff);
			minMovingSpeed = minMovingSpeed * (1 - (Math.abs(angleDiff) / AngleMath.PI_HALF));
		}
		
		float targetAngle = receiver.subtractNew(ballPos).getAngle();
		IVector3 linVel = getVelocityPolicy().getControl(new Vector3(bot.getPos(), targetAngle), bot);
		float speed = minMovingSpeed;
		if ((wFrame.getBall().getVel().getLength2() > 0.1f)
				&& (Math.abs(AngleMath.getShortestRotation(wFrame.getBall().getVel().getAngle(), targetAngle)) < AngleMath.PI_HALF))
		{
			speed += wFrame.getBall().getVel().getLength2();
		}
		IVector3 vel = new Vector3(velDir.scaleToNew(speed), linVel.z());
		
		IVector2 dir = receiver.subtractNew(ballPos);
		Triangle triangleOuter = new Triangle(
				ballPos.addNew(dir.scaleToNew(50)),
				ballPos.addNew(dir.scaleToNew(-50000).turnNew(triangleAngle)),
				ballPos.addNew(dir.scaleToNew(-50000).turnNew(-triangleAngle)));
		
		if (!triangleOuter.isPointInShape(bot.getPos()))
		{
			setDone(true);
		}
		
		List<IDrawableShape> shapes = new ArrayList<>();
		shapes.add(new DrawableLine(new Line(bot.getPos(), vel.getXYVector().multiplyNew(1000)), Color.orange));
		setShapes(shapes);
		
		return vel;
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.KICK_BALL_V2;
	}
	
	
	@Override
	public boolean isReceiving()
	{
		return false;
	}
	
	
	@Override
	public void setPenAreaAllowed(final boolean allowed)
	{
	}
	
	
	@Override
	public boolean armKicker()
	{
		return true;
	}
}
