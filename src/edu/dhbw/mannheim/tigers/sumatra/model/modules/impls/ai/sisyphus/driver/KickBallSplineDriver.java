/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableTrajectory;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickBallSplineDriver extends PositionDriver implements IKickPathDriver
{
	private final SplineTrajectoryGenerator	stg	= new SplineTrajectoryGenerator();
	private final DynamicPosition					receiver;
	
	
	/**
	 * @param receiver
	 */
	public KickBallSplineDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.KICK_BALL_SPLINE;
	}
	
	
	@Override
	public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		super.update(bot, wFrame);
		
		List<IDrawableShape> shapes = new ArrayList<>();
		
		IVector2 ballPos = wFrame.getBall().getPosByTime(0.3f);
		IVector2 shootDir = receiver.subtractNew(ballPos);
		IVector2 botVel = bot.getVel();
		IVector2 botPos = bot.getPos();
		
		IVector2 endVel = shootDir.scaleToNew(0.3f);
		
		List<IVector2> path = new ArrayList<>(3);
		path.add(botPos.multiplyNew(1e-3f));
		
		IVector2 ball2Bot = botPos.subtractNew(ballPos);
		float rotationShoot2Bot = AngleMath.difference(shootDir.getAngle(), ball2Bot.getAngle());
		if (Math.abs(rotationShoot2Bot) < AngleMath.PI_HALF)
		{
			IVector2 supportPoint = ballPos.addNew(shootDir
					.turnNew((AngleMath.PI - 0.7f) * -Math.signum(rotationShoot2Bot)).scaleTo(300));
			path.add(supportPoint.multiplyNew(1e-3f));
		}
		
		path.add(ballPos.multiplyNew(1e-3f));
		
		SplinePair3D spline3d = stg.create(path, botVel, endVel, 0, 0, 0, 0);
		shapes.add(new DrawableTrajectory(spline3d));
		
		IVector2 splineVel = spline3d.getVelocity(0.5f);
		IVector2 dest = bot.getPos().addNew(splineVel.multiplyNew(400));
		setDestination(dest);
		
		setOrientation(receiver.subtractNew(ballPos).getAngle());
		
		setShapes(shapes);
	}
	
	
	@Override
	public boolean isReceiving()
	{
		return false;
	}
	
	
	@Override
	public boolean armKicker()
	{
		return false;
	}
	
	
	@Override
	public void setPenAreaAllowed(final boolean allowed)
	{
	}
}
