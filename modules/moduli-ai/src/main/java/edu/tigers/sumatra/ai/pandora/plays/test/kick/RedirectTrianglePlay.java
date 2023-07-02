/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.test.kick;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Setter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


/**
 * Redirect in a triangle with a specified distance and angle.
 * This repeatably tests redirects with a fixed angle.
 */
public class RedirectTrianglePlay extends ARedirectPlay
{
	@Setter
	private IVector2 redirectPos;
	@Setter
	private IVector2 dir;
	@Setter
	private double distance;
	@Setter
	private double angleDegMin;
	@Setter
	private double angleDegMax;
	@Setter
	private double angleDegChangeSpeed;
	@Setter
	private double maxReceiveBallSpeedMin;
	@Setter
	private double maxReceiveBallSpeedMax;
	@Setter
	private double maxReceiveBallSpeedChangeSpeed;
	@Setter
	private double maxRedirectBallSpeedMin;
	@Setter
	private double maxRedirectBallSpeedMax;
	@Setter
	private double maxRedirectBallSpeedChangeSpeed;
	@Setter
	private boolean goalKick;

	private int currentReceiverTargetSign = 1;
	private long tLast;
	private long timePassed;
	private double angleCurrent;


	public RedirectTrianglePlay()
	{
		super(EPlay.REDIRECT_TRIANGLE);
	}


	@Override
	protected void updateDuringExecution()
	{
		if (tLast != 0 && getBall().getVel().getLength2() > 0.1)
		{
			timePassed += (getWorldFrame().getTimestamp() - tLast);
		}
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();

		var shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.TEST_KICK);
		shapes.add(new DrawableLine(redirectPos, getSupportPos(1), Color.green));
		shapes.add(new DrawableLine(redirectPos, getSupportPos(-1), Color.green));
		shapes.add(new DrawableCircle(Circle.createCircle(redirectPos, Geometry.getBotRadius() + 15), Color.red));
		tLast = getWorldFrame().getTimestamp();

		angleCurrent = value(angleDegMin, angleDegMax, angleDegChangeSpeed);

		if (redirectPos.distanceTo(getBall().getPos()) > distance - 300)
		{
			IVector2 destSec1 = getSupportPos(1);
			IVector2 destSec2 = getSupportPos(-1);
			if (destSec1.distanceTo(getBall().getPos()) < destSec2.distanceTo(getBall().getPos()))
			{
				currentReceiverTargetSign = -1;
			} else
			{
				currentReceiverTargetSign = 1;
			}
		}
	}


	@Override
	protected List<IVector2> getOrigins()
	{
		IVector2 destSec1 = getSupportPos(1);
		IVector2 destSec2 = getSupportPos(-1);
		List<IVector2> receivingPositions = new ArrayList<>(3);
		receivingPositions.add(redirectPos);
		receivingPositions.add(destSec1);
		receivingPositions.add(destSec2);
		return receivingPositions;
	}


	private IVector2 getSupportPos(final double sign)
	{
		return redirectPos.addNew(dir.turnNew(AngleMath.deg2rad(sign * angleCurrent) / 2.0).scaleTo(distance));
	}


	@Override
	protected IVector2 getReceiverTarget(IVector2 origin)
	{
		if (origin == redirectPos)
		{
			return getSupportPos(currentReceiverTargetSign);
		}

		return redirectPos;
	}


	@Override
	protected EReceiveMode getReceiveMode(IVector2 origin)
	{
		if (origin == redirectPos)
		{
			return EReceiveMode.REDIRECT;
		}
		return EReceiveMode.RECEIVE;
	}


	@Override
	protected boolean doGoalKick(IVector2 origin)
	{
		if (origin == redirectPos)
		{
			return goalKick;
		}
		return false;
	}


	@Override
	protected double getMaxReceivingBallSpeed(IVector2 origin)
	{
		if (origin == redirectPos)
		{
			// kicks from redirect origin are towards bots that should receive the ball
			return value(maxReceiveBallSpeedMin, maxReceiveBallSpeedMax, maxReceiveBallSpeedChangeSpeed);
		}
		return value(maxRedirectBallSpeedMin, maxRedirectBallSpeedMax, maxRedirectBallSpeedChangeSpeed);
	}


	private double value(
			double min,
			double max,
			double speed
	)
	{
		double tDiff = timePassed / 1e9 / 60;
		double offset = speed * tDiff;
		double range = max - min;
		if (Math.abs(range) < 1e-5)
		{
			return min;
		}
		return min + (offset % range);
	}
}
