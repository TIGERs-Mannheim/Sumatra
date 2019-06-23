/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.XYSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.MoveConstraints;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerRotateZ2;


/**
 * The base class for all move-skills. Holds basic behavior and PID-controller for rotation etc.
 * 
 * @author Gero, OliverS, DanielW
 * @deprecated
 */
public abstract class AMoveSkill extends ASkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// move
	private final float							MAX_VELOCITY		= AIConfig.getSkills().getMaxVelocity();				// 2.627f;
	private final float							MAX_BRAKING_DIST	= AIConfig.getSkills().getMaxBreakingDistance();
	private final float							POS_TOLERANCE		= AIConfig.getTolerances().getPositioning();
	private final boolean						USE_SPLINES			= AIConfig.getSkills().getUseSplines();
	// private final static float ACCELERATION_INC = AIConfig.getSkills().getAccelerationInc();
	

	protected final PIDControllerRotateZ2	pidRotateSpeed;
	// private final PIDControllerZ2 pidX;
	// private final PIDControllerZ2 pidY;
	
	private boolean								moveComplete		= false;
	private boolean								rotateComplete		= false;
	

	// Timing
	private static final long					NEVER_PROCESSED	= -1;
	/** Stores the last {@link System#nanoTime()} {@link #calcActions()} was called, used for timing */
	private long									lastProcessed		= NEVER_PROCESSED;
	

	// spline evaluation
	private float									t						= 0.0f;
	
	private Vector2f								initPosition		= new Vector2f(0, 0);
	private MoveConstraints						moveConstraints	= new MoveConstraints();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param skill
	 */
	public AMoveSkill(ESkillName skill)
	{
		this(skill, AIConfig.getSkills().getRotatePIDConf());
	}
	

	/**
	 * @param skill
	 */
	public AMoveSkill(ESkillName skill, PIDControllerConfig pidConfig)
	{
		super(skill, ESkillGroup.MOVE);
		
		pidRotateSpeed = new PIDControllerRotateZ2(pidConfig);
		// pidX = new PIDControllerZ2(2, 0.1f, 0, 10, 0.1f); //quick pid paramters
		// pidY = new PIDControllerZ2(2, 0.1f, 0, 10, 0.1f);
		//
		// logging
		// CSVExporter.createInstance("move", "move", false);
		// CSVExporter exporter = CSVExporter.getInstance("move");
		// exporter.setHeader("time", "setv_x", "setv_y", "actualv_x", "actualv_y", "x", "y", "deltaT");
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public ArrayList<ACommand> calcEntryActions(ArrayList<ACommand> cmds)
	{
		// Safety check
		TrackedTigerBot bot = getBot();
		if (bot == null)
		{
			return cmds;
		}
		
		initPosition = getBot().pos;
		
		return cmds;
	}
	

	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		// Safety check
		TrackedTigerBot bot = getBot();
		if (bot == null)
		{
			return cmds;
		}
		
		// Step 0: Timing
		final long now = System.nanoTime();
		final double deltaT; // [s]
		if (lastProcessed == NEVER_PROCESSED)
		{
			// Okay, first time this thing got ever called. Take the skills 'actual period'.
			// Not very precise, but better then nothing!
			deltaT = this.getPeriod() / 1e9f;
		} else
		{
			// Now we can be exact
			deltaT = (now - lastProcessed) / 1e9f;
		}
		lastProcessed = now;
		

		// Step 1: Calculate move vector
		
		final IVector2 desiredMovement;
		if (!USE_SPLINES)
		{
			desiredMovement = calcMovementStraight(getWorldFrame(), deltaT);
		} else
		{
			desiredMovement = calcMovementSpline(getWorldFrame(), deltaT);
		}
		
		// do some logging
		// CSVExporter exporter = CSVExporter.getInstance("move");
		// if (exporter != null)
		// {
		// IVector2 setV = desiredMovement.turnNew(AIMath.PI_HALF + getBot().angle + AIMath.PI);
		// IVector2 actV = getBot().vel;
		// exporter.addValues(System.nanoTime(), setV.x(), setV.y(), actV.x(), actV.y(), getBot().pos.x(),
		// getBot().pos.y(), deltaT);
		// }
		
		// Step 2: Calculate rotation
		final float targetOrientation = calcTargetOrientation(desiredMovement.turnNew(AIMath.PI_HALF + getBot().angle
				+ AIMath.PI));
		final float turnspeed = calcTurnspeed(bot, targetOrientation);
		
		// Step 3: Merge
		mergeMovement(desiredMovement, turnspeed, deltaT, cmds);
		
		// Completed?
		if (moveComplete && rotateComplete)
		{
			// only complete when both are complete at the same cycle
			// exporter.close();
			complete();
		} else
		{
			// otherwise reset
			moveComplete = false;
			rotateComplete = false;
		}
		
		return cmds;
	}
	

	/**
	 * @param bot
	 * @param targetOrientation
	 * @return The calculated turn speed [rad/s]
	 */
	protected float calcTurnspeed(TrackedTigerBot bot, float targetOrientation)
	{
		float currentTurnSpeed = 0;
		
		// Get turn-speed from PID-controller
		currentTurnSpeed = pidRotateSpeed.process(bot.angle, targetOrientation, 0);
		
		// System.out.println("current error in deg " + AIMath.deg(pidRotateSpeed.getPreviousError()));
		// System.out.println("current rot speed: " + getBot().aVel);
		
		// complete when angle error and turn speed are small enough
		if (Math.abs(pidRotateSpeed.getPreviousError()) < AIConfig.getTolerances().getViewAngle()
				&& Math.abs(getBot().aVel) < AIConfig.getSkills().getRotationSpeedThreshold())
		{
			rotateComplete = true;
			currentTurnSpeed = 0;
		}
		
		// System.out.println("current speed " + currentTurnSpeed);
		return currentTurnSpeed;
	}
	

	/**
	 * @param wFrame
	 * @param target (world-coordinate-system)
	 * 
	 * @return The next step on the bots way to the given target [m/s]
	 */
	protected IVector2 calcMovementStraight(WorldFrame wFrame, double deltaT)
	{
		Path path = calcPath(wFrame, moveConstraints);
		
		// Check: Destination reached?
		float distanceToTarget = path.getLength(getBot().pos);
		if (path.path.size() <= 1 && distanceToTarget < POS_TOLERANCE
				&& getBot().vel.getLength2() < AIConfig.getSkills().getMoveSpeedThreshold())
		{
			moveComplete = true;
			return Vector2.ZERO_VECTOR; // No need to move anywhere
		}
		
		// vector from botpos to first pathpoint
		IVector2 moveVec = path.path.get(0).subtractNew(getBot().pos);
		Line origPath = new Line();
		if (!initPosition.equals(path.path.get(0)))
		{
			origPath.setPoints(initPosition, path.path.get(0));
			IVector2 nearest = AIMath.leadPointOnLine(getBot().pos, origPath);
			Vector2 vecToOrigPath = getBot().pos.subtractNew(nearest);
			
			moveVec = moveVec.normalizeNew();
			vecToOrigPath.normalize();
			moveVec = moveVec.addNew(vecToOrigPath.multiply(-0.2f));
		}
		

		// ##### Velocity
		float velocity = MAX_VELOCITY; // [m/s]
		// need to calculate the velocities for all comming pathpoints
		for (int i = path.size() - 1; i >= 0; i--)
		{
			float cornerAngle = path.getCornerAngle(getBot().pos, i);
			float v = calcVelocity(path.getLength(getBot().pos, i) / 1000, 0, cornerAngle, deltaT);
			
			// take the smallest velocity
			if (v < velocity)
			{
				velocity = v;
			}
		}
		
		// apply velocity with pid control (choose either this or accelerate function below)
		// IVector2 setpoint = moveVec.scaleToNew(velocity);
		// float vx = pidX.process(getBot().vel.x(), setpoint.x(), deltaT);
		// float vy = pidY.process(getBot().vel.y(), setpoint.y(), deltaT);
		//
		// moveVec = new Vector2(vx, vy);
		
		// apply velocity with accelerate function
		moveVec = moveVec.turnNew(-getBot().aVel * 0.2f);
		moveVec = accelerate(moveVec, velocity, getBot());
		

		// ##### Convert to local bot-system
		moveVec = moveVec.turnNew(AIMath.PI_HALF - getBot().angle);
		
		if (path.changed)
		{
			// Safety check
			TrackedTigerBot bot = getBot();
			if (bot != null)
			{
				initPosition = bot.pos;
			}
		}
		
		return moveVec;
	}
	

	/**
	 * @param wFrame
	 * @param target (world-coordinate-system)
	 * 
	 * @return The next step on the bots way to the given target [m/s]
	 */
	protected IVector2 calcMovementStraightComplex(WorldFrame wFrame, double deltaT)
	{
		Path path = calcPath(wFrame, moveConstraints);
		
		// Check: Destination reached?
		float distanceToTarget = path.getLength(getBot().pos);
		if (path.path.size() <= 1 && distanceToTarget < POS_TOLERANCE
				&& getBot().vel.getLength2() < AIConfig.getSkills().getMoveSpeedThreshold())
		{
			moveComplete = true;
			return Vector2.ZERO_VECTOR; // No need to move anywhere
		}
		
		// vector from botpos to first pathpoint
		IVector2 moveVec = path.path.get(0).subtractNew(getBot().pos);
		Line origPath = new Line();
		if (!initPosition.equals(path.path.get(0)))
		{
			origPath.setPoints(initPosition, path.path.get(0));
			IVector2 nearest = AIMath.leadPointOnLine(getBot().pos, origPath);
			Vector2 vecToOrigPath = getBot().pos.subtractNew(nearest);
			
			moveVec = moveVec.normalizeNew();
			vecToOrigPath.normalize();
			float multi = 1 / (Math.abs(getBot().vel.getLength2() + 0.1f)) * 0.05f;
			moveVec = moveVec.addNew(vecToOrigPath.multiply(multi));
		}
		

		// ##### Velocity
		float velocity = MAX_VELOCITY; // [m/s]
		// need to calculate the velocities for all comming pathpoints
		for (int i = path.size() - 1; i >= 0; i--)
		{
			float cornerAngle = path.getCornerAngle(getBot().pos, i);
			float v = calcVelocity(path.getLength(getBot().pos, i) / 1000, 0, cornerAngle, deltaT);
			
			// take the smallest velocity
			if (v < velocity)
			{
				velocity = v;
			}
		}
		
		// apply velocity with pid control (choose either this or accelerate function below)
		// IVector2 setpoint = moveVec.scaleToNew(velocity);
		// float vx = pidX.process(getBot().vel.x(), setpoint.x(), deltaT);
		// float vy = pidY.process(getBot().vel.y(), setpoint.y(), deltaT);
		//
		// moveVec = new Vector2(vx, vy);
		
		// apply velocity with accelerate function
		moveVec = moveVec.turnNew(-getBot().aVel * 0.5f);
		moveVec = accelerate(moveVec, velocity, getBot());
		

		// ##### Convert to local bot-system
		moveVec = moveVec.turnNew(AIMath.PI_HALF - getBot().angle);
		
		if (path.changed)
		{
			// Safety check
			TrackedTigerBot bot = getBot();
			if (bot != null)
			{
				initPosition = bot.pos;
			}
		}
		
		return moveVec;
	}
	

	/**
	 * calculates the appropriate velocity dependent on path corners,
	 * distance to end or pathpoints and curvature
	 * 
	 * @param distanceToNextPoint to next pathpoint
	 * @param curvature the paths curvature; 0 if a straight line
	 * @param deltaT
	 * @return
	 */
	private float calcVelocity(float distance, float curvature, float cornerAngle, double deltaT)
	{
		final float cornerFactor = MAX_BRAKING_DIST / AIMath.PI;
		final float maxBreakingDistance = MAX_BRAKING_DIST - cornerFactor * cornerAngle; // TODO DanielW: this linear
																													// function might not be optimal.
																													// some type of exp?
		final float disiredVelocity;
		if (distance <= maxBreakingDistance)
		{
			// near to the end
			disiredVelocity = MAX_VELOCITY * distance / MAX_BRAKING_DIST;
		} else
		{
			// loads of space to go
			disiredVelocity = (MAX_VELOCITY - Math.abs(curvature * 20));// TODO DanielW scaling, prevent negative;
																							// into
			// config; need when near target?
		}
		return disiredVelocity;
	}
	

	private static float calcMaxAccel(Vector2 startPoint, Vector2 maxPoint, Vector2 endPoint, float cur)
	{
		float maxAccel = 0.0f;
		
		if (Math.abs(cur) < maxPoint.x)
		{
			maxAccel = Math.abs(cur) * (maxPoint.y - startPoint.y) / (maxPoint.x - startPoint.x) + startPoint.y;
		} else if (Math.abs(cur) < endPoint.x)
		{
			maxAccel = (Math.abs(cur) - maxPoint.x) * (endPoint.y - maxPoint.y) / (endPoint.x - startPoint.x) + maxPoint.y;
		} else
		{
			maxAccel = 0.0f;
		}
		
		return maxAccel;
	}
	

	/**
	 * this is a acceleration function, which considers current bot velocity and direction to
	 * 
	 * 
	 * @param direction
	 * @param disiredVelocity
	 * @return
	 */
	public static IVector2 accelerate(IVector2 direction, float disiredVelocity, TrackedTigerBot bot)
	{
		final IVector2 desiredVelocityVec = direction.scaleToNew(disiredVelocity);
		
		Vector2 localDesiredVelo = desiredVelocityVec.turnNew(AIMath.PI_HALF - bot.angle);
		Vector2 localCurrentVelo = bot.vel.turnNew(AIMath.PI_HALF - bot.angle);
		
		float newX = localDesiredVelo.x();
		float newY = localDesiredVelo.y();
		float curX = localCurrentVelo.x();
		float curY = localCurrentVelo.y();
		
		float maxXAccel = 0.2f;
		float maxYAccel = 0.5f;
		float maxXDeccel = 1.0f;
		float maxYDeccel = 1.0f;
		
		float xChange = newX - curX; // can be positive or negative
		float yChange = newY - curY;
		
		boolean modX = false;
		@SuppressWarnings("unused")
		boolean modY = false;
		
		if (Math.abs(newX) > Math.abs(curX)) // x accel
		{
			if (Math.abs(xChange) > maxXAccel)
			{
				newX = curX + Math.signum(xChange) * maxXAccel;
				modX = true;
			}
		}
		
		if (Math.abs(newX) < Math.abs(curX)) // x deccel
		{
			if (Math.abs(xChange) > maxXDeccel)
			{
				newX = curX + Math.signum(xChange) * maxXDeccel;
				modX = true;
			}
		}
		
		if (Math.abs(newY) > Math.abs(curY)) // y accel
		{
			if (Math.abs(yChange) > maxYAccel)
			{
				newY = curY + Math.signum(yChange) * maxYAccel;
				modY = true;
			}
		}
		
		if (Math.abs(newY) < Math.abs(curY)) // y deccel
		{
			if (Math.abs(yChange) > maxYDeccel)
			{
				newY = curY + Math.signum(yChange) * maxYDeccel;
				modY = true;
			}
		}
		
		if (maxYAccel > maxXAccel)
		{
			if (modX)
			{
				// => limit y
				newY = curY + Math.signum(yChange) * maxXAccel;
			}
		}
		
		Vector2 result = new Vector2(newX, newY);
		
		result = result.turn(-AIMath.PI_HALF + bot.angle);
		
		return result;
	}
	

	/**
	 * this is a acceleration function, which considers current bot velocity and direction to
	 * 
	 * 
	 * @param direction
	 * @param disiredVelocity
	 * @return
	 */
	public static IVector2 accelerateComplex(IVector2 direction, float disiredVelocity, TrackedTigerBot bot)
	{
		final IVector2 desiredVelocityVec = direction.scaleToNew(disiredVelocity);
		
		Vector2 localDesiredVelo = desiredVelocityVec.turnNew(AIMath.PI_HALF - bot.angle);
		Vector2 localCurrentVelo = bot.vel.turnNew(AIMath.PI_HALF - bot.angle);
		
		float newX = localDesiredVelo.x();
		float newY = localDesiredVelo.y();
		float curX = localCurrentVelo.x();
		float curY = localCurrentVelo.y();
		
		float maxXAccel = calcMaxAccel(new Vector2(0, 0.05f), new Vector2(1.2f, 0.2f), new Vector2(2.0f, 0.0f), curX);
		float maxYAccel = calcMaxAccel(new Vector2(0, 0.15f), new Vector2(1.2f, 0.4f), new Vector2(2.0f, 0.0f), curY);
		float maxXDeccel = calcMaxAccel(new Vector2(0, 0.3f), new Vector2(1.0f, 0.3f), new Vector2(5.0f, 0.3f), curX);
		float maxYDeccel = calcMaxAccel(new Vector2(0, 0.5f), new Vector2(1.0f, 0.5f), new Vector2(5.0f, 0.5f), curY);
		;
		
		float incXreal;
		float incYreal;
		float xChange;
		float yChange;
		
		do
		{
			xChange = newX - curX; // can be positive or negative
			yChange = newY - curY;
			
			float incX = xChange;
			float incY = yChange;
			
			// System.out.println("Prop before: " + xChange/yChange);
			
			if (Math.abs(newX) > Math.abs(curX)) // x accel
			{
				if (Math.abs(xChange) > maxXAccel)
				{
					incX = Math.signum(xChange) * maxXAccel;
				}
			}
			
			if (Math.abs(newX) < Math.abs(curX)) // x deccel
			{
				if (Math.abs(xChange) > maxXDeccel)
				{
					incX = Math.signum(xChange) * maxXDeccel;
				}
			}
			
			if (Math.abs(newY) > Math.abs(curY)) // y accel
			{
				if (Math.abs(yChange) > maxYAccel)
				{
					incY = Math.signum(yChange) * maxYAccel;
				}
			}
			
			if (Math.abs(newY) < Math.abs(curY)) // y deccel
			{
				if (Math.abs(yChange) > maxYDeccel)
				{
					incY = Math.signum(yChange) * maxYDeccel;
				}
			}
			
			if (Math.abs(xChange) < Math.abs(yChange))
			{
				incXreal = Math.abs(incY / yChange) * xChange;
				incYreal = incY;
			} else
			{
				incYreal = Math.abs(incX / xChange) * yChange;
				incXreal = incX;
			}
			
			// System.out.println("Prop after: " + incXreal/incYreal);
			
			newX = curX + incXreal;
			newY = curY + incYreal;
			
			// System.out.println("Inc real X: " + incXreal);
			// System.out.println("Inc real Y: " + incYreal);
		} while (Math.abs(xChange - incXreal) > 0.01 || Math.abs(yChange - incYreal) > 0.01);
		
		Vector2 result = new Vector2(newX, newY);
		
		result = result.turn(-AIMath.PI_HALF + bot.angle);
		
		return result;
	}
	

	/**
	 * @param wFrame
	 * @param target (world-coordinate-system)
	 * 
	 * @return The next step on the bots way to the given target [m/s]
	 */
	protected IVector2 calcMovementSpline(WorldFrame wFrame, double deltaT)
	{
		Path path = calcPath(wFrame, moveConstraints);
		// TODO DanielW: move the call to spline calculation here!
		XYSpline spline = path.getSpline();
		
		if (path.changed)
		{
			t = 0.0f;
			System.out.println("changed");
		}
		
		path.getPathGuiFeatures().setVirtualVehicle(t);
		
		// System.out.println("t is :" + t);
		float distanceToTarget = AIMath.distancePP(path.getGoal(), getBot().pos);
		
		// #### Evaulate the spline
		
		IVector2 virtualPosition = spline.evaluateFunction(t);
		IVector2 virtualMove = spline.getTangentialVector(t);
		
		float virtualDistanceToTarget = AIMath.distancePP(path.getGoal(), spline.evaluateFunction(t));
		
		// check complete
		if (distanceToTarget < POS_TOLERANCE && getBot().vel.getLength2() < AIConfig.getSkills().getMoveSpeedThreshold())
		{
			moveComplete = true;
			return Vector2.ZERO_VECTOR; // No need to move anywhere
		}
		
		float virtualVelocity = calcVelocity(virtualDistanceToTarget / 1000, spline.getCurvature(t), 0, deltaT);
		float velocity = calcVelocity(distanceToTarget / 1000, spline.getCurvature(t), 0, deltaT);
		System.out.println("velocity is " + velocity);
		
		// virtualVelocity = (virtualVelocity + velocity) / 2; // TODO /2 is probably false
		
		Vector2 scaledVirtualMove = virtualMove.scaleToNew(virtualVelocity);
		System.out.println(virtualVelocity);
		
		Vector2 botToVirtual = virtualPosition.subtractNew(getBot().pos);
		
		Vector2 moveVec = botToVirtual.addNew(scaledVirtualMove.multiplyNew(10.0f));
		// Vector2 moveVec = new Vector2(botToVirtual);
		
		moveVec.scaleTo(velocity);
		
		path.getPathGuiFeatures().setCurrentMove(moveVec);
		
		float tStep = (float) ((virtualVelocity - (scaledVirtualMove.scalarProduct(botToVirtual) * 0.5 / 1000)) * 1000 * deltaT);
		if (tStep < 0) // always go forward or wait
			tStep = 0;
		
		t += tStep;
		System.out.println("t step is: " + tStep);
		
		if (t > spline.getMaxTValue())
		{
			t = spline.getMaxTValue();
		}
		
		// ##### Convert to local bot-system
		IVector2 turnedMoveVec = moveVec.turnNew(AIMath.PI_HALF - getBot().angle);
		
		return turnedMoveVec;
	}
	

	/**
	 * @param move [m/s]
	 * @param rotation [rad/s]
	 * @param deltaT [s]
	 * @param cmds
	 */
	private void mergeMovement(IVector2 move, float rotation, double deltaT, ArrayList<ACommand> cmds)
	{
		// System.out.println("");
		
		// System.out.println("deltaT: " + deltaT);
		
		// float degValue = AIMath.deg(rotation) * deltaT / 2.0f;
		
		// float rotationCompensation = (float) (getBot().aVel * deltaT / 2f);
		// // System.out.println("deltaT: " + deltaT + " rotcomp: " + rotationCompensation);
		// IVector2 moveTurned = move.turnNew(rotationCompensation);
		// Create command
		cmds.add(new TigerMotorMoveV2(move, rotation, 0));
		// cmds.add(new TigerMotorMoveV2(move, 0, 0));
	}
	

	/**
	 * calculate a path
	 * by default, this is asking Sisyphus to do that
	 * can be overridden
	 * 
	 * @param wFrame
	 * @param constraints
	 * @return The planned path
	 */
	protected Path calcPath(WorldFrame wFrame, MoveConstraints constraints)
	{
		// ##### Path
		// Safety check: If target outside field, choose nearest point inside! (suggested by MalteM)
		// and calculate a path
		Rectanglef field = AIConfig.getGeometry().getField();
		return getSisyphus().calcPath(wFrame, getBot().id, field.nearestPointInside(getTarget()),
				constraints.isBallObstacle(), constraints.isGoalie(), constraints.getGameSituation());
	}
	

	/**
	 * Must be implemented by subclasses
	 * 
	 * @param move the current move-vector in <em>global</em> system
	 * 
	 * @return The direction - in wcs - the bot should look at
	 */
	protected abstract float calcTargetOrientation(IVector2 move);
	

	/**
	 * @return The final target of the skill
	 */
	protected abstract IVector2 getTarget();
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setMoveConstraints(MoveConstraints newMoveConstraints)
	{
		this.moveConstraints = newMoveConstraints;
	}
}
