/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3f;
import lombok.Setter;


/**
 * A collision object that can have a velocity and can be sticky (for dribbling).
 */
public class KickerFrontLineCollisionObject extends LineSegmentCollisionObject
{
	private final IVector3 botVel;
	private final IVector3 lineVel;
	private final BotID botID;
	@Setter
	private IVector3 impulse = Vector3f.ZERO_VECTOR;
	@Setter
	private IVector3 acc = Vector3f.ZERO_VECTOR;
	@Setter
	private boolean sticky;
	@Setter
	private double dampFactor;
	@Setter
	private double dampFactorOrthogonal;


	public KickerFrontLineCollisionObject(final ILineSegment obstacleLine, final IVector3 botVel, final IVector3 lineVel,
			final IVector2 normal, final BotID botID)
	{
		super(obstacleLine, normal, false);
		this.botVel = botVel;
		this.lineVel = lineVel;
		this.botID = botID;
		this.dampFactor = 0.5;
		this.dampFactorOrthogonal = 0;
	}


	@Override
	public IVector3 getImpulse()
	{
		return impulse;
	}


	@Override
	public IVector2 stick(IVector2 pos)
	{
		return obstacleLine.getPathCenter();
	}


	@Override
	public boolean isSticky()
	{
		return sticky;
	}


	@Override
	public double getDampFactor()
	{
		return dampFactor;
	}


	@Override
	public double getDampFactorOrthogonal()
	{
		return dampFactorOrthogonal;
	}


	@Override
	public IVector3 getAcc()
	{
		return acc;
	}


	@Override
	public IVector3 getVel()
	{
		return botVel;
	}


	@Override
	public IVector2 getSurfaceVel(IVector2 collisionPos)
	{
		var connection = Vector2.fromPoints(obstacleLine.closestPointOnPath(collisionPos), obstacleLine.getPathCenter());
		var rotationSpeed = connection.getNormalVector().multiply(lineVel.z());
		return lineVel.getXYVector().addNew(rotationSpeed);
	}


	@Override
	public BotID getBotID()
	{
		return botID;
	}
}
