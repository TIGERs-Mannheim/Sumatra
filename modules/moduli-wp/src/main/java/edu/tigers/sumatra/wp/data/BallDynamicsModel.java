/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import java.util.List;
import java.util.Optional;

import Jama.Matrix;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.shapes.triangle.Triangle;
import edu.tigers.sumatra.wp.data.MotionContext.BotInfo;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallDynamicsModel
{
	/** [mm/s²] */
	private final double				acc;
	private double						accSlide;
	
	private static final double	A_FALL						= -9810;
	private static final double	BALL_DAMP_KICKER_FACTOR	= 0.37;
	private static final double	BALL_DAMP_GROUND_FACTOR	= 0.37;
	
	private boolean					modelStraightKick			= false;
	private final boolean			handleCollision;
	private double						minVelForCollision		= 0;
	
	private double						confOnCollision			= 1;
	private IVector2					ballDir						= Vector2.X_AXIS;
	
	
	/**
	 * @param acc
	 */
	public BallDynamicsModel(final double acc)
	{
		this(acc, true);
	}
	
	
	/**
	 * @param acc
	 * @param handleCollision
	 */
	public BallDynamicsModel(final double acc, final boolean handleCollision)
	{
		this.acc = acc * 1000;
		this.handleCollision = handleCollision;
	}
	
	
	/**
	 * @param state
	 * @param dt
	 * @param context
	 * @return
	 */
	public Matrix dynamics(final Matrix state, final double dt,
			final MotionContext context)
	{
		double x = state.get(0, 0);
		double y = state.get(1, 0);
		double z = state.get(2, 0);
		double vx = state.get(3, 0);
		double vy = state.get(4, 0);
		double vz = state.get(5, 0);
		
		final double ax; // = state.get(6, 0);
		final double ay; // = state.get(7, 0);
		double az; // = state.get(8, 0);
		accSlide = 10 * acc;
		
		// velocity
		final double v = Math.sqrt((vx * vx) + (vy * vy));
		IVector2 dir = new Vector2(vx, vy);
		
		if ((z > 0) || (vz > 0))
		{
			// flying ball
			ax = 0.0;
			ay = 0.0;
			az = A_FALL;
		} else
		{
			// not flying or z < 0
			if ((Math.abs(vz) + (A_FALL * dt)) >= 0)
			{
				// damp on ground
				z = 0;
				vz = -BALL_DAMP_GROUND_FACTOR * vz;
				az = A_FALL;
			} else
			{
				z = 0;
				vz = 0;
				az = 0;
			}
			
			if (v > 2000)
			{
				ax = (accSlide * dir.x()) / v;
				ay = (accSlide * dir.y()) / v;
			} else if ((v != 0) && ((v + (acc * dt)) >= 0))
			{
				ax = (acc * dir.x()) / v;
				ay = (acc * dir.y()) / v;
			} else
			{
				vx = 0.0;
				vy = 0.0;
				ax = 0.0;
				ay = 0.0;
			}
		}
		
		// update position
		x = (x + (vx * dt)) + (0.5 * dt * dt * ax);
		y = (y + (vy * dt)) + (0.5 * dt * dt * ay);
		z = (z + (vz * dt)) + (0.5 * dt * dt * az);
		
		// calculate ball's velocity from current acceleration
		vx = vx + (dt * ax);
		vy = vy + (dt * ay);
		vz = vz + (dt * az);
		
		if (z < 0)
		{
			z = 0;
		}
		
		
		final Matrix newState = state.copy();
		newState.set(0, 0, x);
		newState.set(1, 0, y);
		newState.set(2, 0, z);
		newState.set(3, 0, vx);
		newState.set(4, 0, vy);
		newState.set(5, 0, vz);
		newState.set(6, 0, ax);
		newState.set(7, 0, ay);
		newState.set(8, 0, az);
		
		double confidence = state.get(9, 0);
		// cooldown of confidence
		confidence = Math.max(0, Math.min(1, confidence + (dt / 0.1)));
		// double tLastCollision = state.get(9, 0) + dt;
		newState.set(9, 0, confidence);
		
		Matrix outState = handleCollision(state, newState, dt, context);
		if ((v >= minVelForCollision) &&
				handleCollision)
		{
			return outState;
		}
		newState.set(9, 0, outState.get(9, 0));
		return newState;
	}
	
	
	private Matrix handleCollision(final Matrix state, final Matrix newState, final double dt,
			final MotionContext context)
	{
		if (newState.get(2, 0) > 170)
		{
			// ball is flying
			return newState;
		}
		
		ballDir = new Vector2(state.get(3, 0), state.get(4, 0));
		
		Matrix outState = newState;
		
		ILine goalLine1 = Line.newLine(
				Geometry.getGoalOur().getGoalPostLeft()
						.addNew(new Vector2(-Geometry.getGoalDepth() + Geometry.getBallRadius(), 0)),
				Geometry.getGoalOur().getGoalPostRight()
						.addNew(new Vector2(-Geometry.getGoalDepth() + Geometry.getBallRadius(), 0)));
		ILine goalLine2 = Line.newLine(
				Geometry.getGoalTheir().getGoalPostLeft()
						.addNew(new Vector2(Geometry.getGoalDepth() - Geometry.getBallRadius(), 0)),
				Geometry.getGoalTheir().getGoalPostRight()
						.addNew(new Vector2(Geometry.getGoalDepth() - Geometry.getBallRadius(), 0)));
		outState = handleCollisionLine(state, outState, dt, context, goalLine1, AVector3.ZERO_VECTOR);
		outState = handleCollisionLine(state, outState, dt, context, goalLine2, AVector3.ZERO_VECTOR);
		
		for (BotInfo info : context.getBots().values())
		{
			IVector3 pos = info.getPos();
			Circle botHull = new Circle(pos.getXYVector(), Geometry.getBotRadius()
					+ Geometry.getBallRadius());
			
			double theta = Math.acos((info.getCenter2DribblerDist() + Geometry.getBallRadius())
					/ (Geometry.getBotRadius() + Geometry.getBallRadius()));
			IVector2 leftBotEdge = pos.getXYVector()
					.addNew(new Vector2(pos.z() - theta).scaleTo(Geometry.getBotRadius() + Geometry.getBallRadius()));
			IVector2 rightBotEdge = pos.getXYVector()
					.addNew(new Vector2(pos.z() + theta).scaleTo(Geometry.getBotRadius() + Geometry.getBallRadius()));
			
			IVector2 newBallPos = new Vector2(outState.get(0, 0), outState.get(1, 0));
			IVector2 oldBallPos = new Vector2(state.get(0, 0), state.get(1, 0));
			
			Optional<IVector2> collision = getCircleCollision(state, outState, botHull);
			if (collision.isPresent())
			{
				// ball is within bot, but allow it to be in front of kicker
				IVector2 center2Ball = collision.get().subtractNew(botHull.center());
				if (!center2Ball.isZeroVector())
				{
					
					double angleDiff = Math.abs(AngleMath.getShortestRotation(center2Ball.getAngle(), pos.z()));
					
					if (angleDiff >= theta)
					{
						// ball is NOT in front of kicker
						
						boolean isCollision = false;
						if (botHull.isPointInShape(newBallPos) || botHull.isPointInShape(oldBallPos))
						{
							isCollision = true;
						} else
						{
							Rectangle rectState = new Rectangle(oldBallPos, newBallPos);
							if (rectState.isPointInShape(collision.get()))
							{
								isCollision = true;
							}
						}
						
						if (isCollision)
						{
							outState = handleCollisionPoint(state, newState, dt, context, collision.get(), center2Ball,
									AVector3.ZERO_VECTOR);
							return outState;
						}
					} else
					{
						// ball is in front of kicker
						
						Triangle tri = new Triangle(botHull.center(), leftBotEdge, rightBotEdge);
						boolean forceCollision = false;
						if (tri.isPointInShape(newBallPos) || tri.isPointInShape(oldBallPos))
						{
							forceCollision = true;
						}
						
						ILine frontLine = Line.newLine(leftBotEdge, rightBotEdge);
						
						Optional<IVector2> frontCollision = Optional.empty();
						if (newBallPos.equals(oldBallPos) && forceCollision)
						{
							frontCollision = Optional.of(GeoMath.leadPointOnLine(newBallPos, frontLine));
						} else
						{
							frontCollision = getLineCollision(state, outState, frontLine);
							Rectangle rectState = new Rectangle(oldBallPos, newBallPos);
							if (frontCollision.isPresent() &&
									!forceCollision &&
									!rectState.isPointInShape(frontCollision.get()))
							{
								frontCollision = Optional.empty();
							}
						}
						
						if (botHull.isPointInShape(newBallPos))
						{
							// possible kick soon
							outState.set(9, 0, confOnCollision);
							
							ballDir = new Vector2(pos.z()).scaleTo(info.getKickSpeed() * 1000);
							
							if (((info.getDribbleRpm() > 0) && (info.getKickSpeed() == 0)))
							{
								IVector2 kickerPos = info.getPos().getXYVector()
										.addNew(new Vector2(info.getPos().z())
												.scaleTo(info.getCenter2DribblerDist() + Geometry.getBallRadius()));
								// if (modelStraightKick)
								// {
								outState.set(0, 0, kickerPos.x());
								outState.set(1, 0, kickerPos.y());
								// } else
								// {
								// IVector2 pullVel = info.getVel().getXYVector().multiplyNew(1.05);
								// outState.set(3, 0, pullVel.x());
								// outState.set(3, 0, pullVel.y());
								// }
							}
						}
						
						if (frontCollision.isPresent())
						{
							
							IVector2 normal = new Vector2(pos.z());
							IVector3 shootVector;
							if (modelStraightKick)
							{
								IVector2 shootVector2 = new Vector2(pos.z()).scaleTo(info.getKickSpeed() * 1000);
								double shootZ = 0;
								if (info.isChip())
								{
									shootZ = info.getKickSpeed() * 1000;
								}
								shootVector = new Vector3(shootVector2, shootZ);
							} else
							{
								shootVector = AVector3.ZERO_VECTOR;
							}
							
							if (modelStraightKick && (info.getDribbleRpm() > 0))
							{
								outState.set(3, 0, shootVector.x());
								outState.set(4, 0, shootVector.y());
								outState.set(5, 0, shootVector.z());
								outState.set(9, 0, confOnCollision);
							} else
							{
								outState = handleCollisionPoint(state, newState, dt, context, frontCollision.get(), normal,
										shootVector);
							}
							return outState;
						}
					}
				}
			}
		}
		
		return outState;
	}
	
	
	private Matrix handleCollisionLine(final Matrix state, final Matrix newState, final double dt,
			final MotionContext context, final ILine obstacleLine, final IVector3 shootVector)
	{
		IVector2 p1 = new Vector2(state.get(0, 0), state.get(1, 0));
		IVector2 p2 = new Vector2(newState.get(0, 0), newState.get(1, 0));
		ILine stateLine = Line.newLine(p1, p2);
		
		IVector2 collision;
		if (stateLine.directionVector().getLength() < 0.01)
		{
			collision = stateLine.supportVector();
		} else
		{
			try
			{
				collision = GeoMath.intersectionPoint(obstacleLine, stateLine);
			} catch (MathException e)
			{
				return newState;
			}
		}
		
		Rectangle rect = new Rectangle(obstacleLine.supportVector(),
				obstacleLine.supportVector().addNew(obstacleLine.directionVector()));
		Rectangle rectState = new Rectangle(stateLine.supportVector(),
				stateLine.supportVector().addNew(stateLine.directionVector()));
		if (!rect.isPointInShape(collision))
		{
			return newState;
		}
		if (!rectState.isPointInShape(collision))
		{
			return newState;
		}
		
		IVector2 oldPos = new Vector2(state.get(0, 0), state.get(1, 0));
		IVector2 normal = getCollisionNormal(obstacleLine, oldPos, collision);
		return handleCollisionPoint(state, newState, dt, context, collision,
				normal, shootVector);
	}
	
	
	private Optional<IVector2> getCircleCollision(final Matrix state, final Matrix newState, final Circle cirleObstacle)
	{
		IVector2 p1 = new Vector2(state.get(0, 0), state.get(1, 0));
		IVector2 p2 = new Vector2(newState.get(0, 0), newState.get(1, 0));
		ILine stateLine = Line.newLine(p1, p2);
		
		if (stateLine.directionVector().isZeroVector() && cirleObstacle.isPointInShape(p1))
		{
			return Optional.of(cirleObstacle.nearestPointOutside(p1));
		}
		
		List<IVector2> intersections = cirleObstacle.lineIntersections(stateLine);
		IVector2 collision;
		if (intersections.isEmpty())
		{
			return Optional.empty();
		} else if (intersections.size() == 1)
		{
			collision = intersections.get(0);
		} else
		{
			double dist1 = GeoMath.distancePP(stateLine.supportVector(), intersections.get(0));
			double dist2 = GeoMath.distancePP(stateLine.supportVector(), intersections.get(1));
			if (dist1 < dist2)
			{
				collision = intersections.get(0);
			} else
			{
				collision = intersections.get(1);
			}
		}
		
		return Optional.of(collision);
	}
	
	
	private Optional<IVector2> getLineCollision(final Matrix state, final Matrix newState, final ILine obstacleLine)
	{
		IVector2 p1 = new Vector2(state.get(0, 0), state.get(1, 0));
		IVector2 p2 = new Vector2(newState.get(0, 0), newState.get(1, 0));
		ILine stateLine = Line.newLine(p1, p2);
		
		IVector2 collision;
		if (stateLine.directionVector().getLength() < 0.01)
		{
			collision = stateLine.supportVector();
		} else
		{
			try
			{
				collision = GeoMath.intersectionPoint(obstacleLine, stateLine);
			} catch (MathException e)
			{
				return Optional.empty();
			}
		}
		
		Rectangle rect = new Rectangle(obstacleLine.supportVector(),
				obstacleLine.supportVector().addNew(obstacleLine.directionVector()));
		if (!rect.isPointInShape(collision))
		{
			return Optional.empty();
		}
		
		return Optional.of(collision);
	}
	
	
	private Matrix handleCollisionPoint(final Matrix state, final Matrix newState, final double dt,
			final MotionContext context, final IVector2 collision, final IVector2 normal, final IVector3 shootVector)
	{
		IVector2 colVel = new Vector2((newState.get(3, 0) + state.get(3, 0)) / 2,
				(newState.get(4, 0) + state.get(4, 0)) / 2);
		IVector2 newPos = new Vector2(newState.get(0, 0), newState.get(1, 0));
		IVector2 outVel = ballCollision(colVel, normal);
		outVel = outVel.addNew(shootVector);
		double posOffset = GeoMath.distancePP(collision, newPos);
		IVector2 outPos = collision.addNew(outVel.scaleToNew(posOffset + 1));
		
		final Matrix corState = newState.copy();
		corState.set(0, 0, outPos.x());
		corState.set(1, 0, outPos.y());
		corState.set(3, 0, outVel.x());
		corState.set(4, 0, outVel.y());
		corState.set(5, 0, newState.get(5, 0) + shootVector.z());
		corState.set(9, 0, confOnCollision);
		return corState;
	}
	
	
	private IVector2 getCollisionNormal(final ILine line, final IVector2 oldPos, final IVector2 colPos)
	{
		IVector2 normal = line.directionVector().getNormalVector();
		double angle = GeoMath.angleBetweenVectorAndVector(oldPos.subtractNew(colPos), normal);
		if (angle > AngleMath.PI_HALF)
		{
			return normal.multiplyNew(-1);
		}
		return normal;
	}
	
	
	private IVector2 ballCollision(final IVector2 ballVel, final IVector2 collisionNormal)
	{
		if (ballVel.isZeroVector())
		{
			return ballVel;
		}
		double velInfAngle = AngleMath.normalizeAngle(ballVel.getAngle() + AngleMath.PI);
		double velAngleDiff = AngleMath.getShortestRotation(velInfAngle, collisionNormal.getAngle());
		IVector2 outVel = new Vector2(collisionNormal).turn(velAngleDiff).scaleTo(
				ballVel.getLength2() * (1 - BALL_DAMP_KICKER_FACTOR));
		return outVel;
	}
	
	
	/**
	 * @param modelStraightKick the modelStraightKick to set
	 */
	public void setModelStraightKick(final boolean modelStraightKick)
	{
		this.modelStraightKick = modelStraightKick;
	}
	
	
	/**
	 * @param minVelForCollision the minVelForCollision to set
	 */
	public void setMinVelForCollision(final double minVelForCollision)
	{
		this.minVelForCollision = minVelForCollision;
	}
	
	
	/**
	 * @return the ballDir
	 */
	public IVector2 getPropBallVelDir()
	{
		return ballDir;
	}
}
