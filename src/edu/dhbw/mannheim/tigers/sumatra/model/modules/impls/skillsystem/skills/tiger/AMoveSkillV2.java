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
import java.util.Iterator;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
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
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerZ2;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PidState;


/**
 * The base class for all move-skills. Holds basic behavior and PID-controller for rotation etc.
 * 
 * @author Gero, OliverS, DanielW, AndreR
 */
public abstract class AMoveSkillV2 extends ASkill
{
	public static class AccelerationFacade
	{
		public Vector2	maxXAccelBeg	= new Vector2();
		public Vector2	maxXAccelMid	= new Vector2();
		public Vector2	maxXAccelEnd	= new Vector2();
		public Vector2	maxYAccelBeg	= new Vector2();
		public Vector2	maxYAccelMid	= new Vector2();
		public Vector2	maxYAccelEnd	= new Vector2();
		public Vector2	maxXDeccelBeg	= new Vector2();
		public Vector2	maxXDeccelMid	= new Vector2();
		public Vector2	maxXDeccelEnd	= new Vector2();
		public Vector2	maxYDeccelBeg	= new Vector2();
		public Vector2	maxYDeccelMid	= new Vector2();
		public Vector2	maxYDeccelEnd	= new Vector2();
	}
	
	public static class PIDFacade
	{
		public PIDControllerConfig	velocity		= new PIDControllerConfig();
		public PIDControllerConfig	orientation	= new PIDControllerConfig();
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// move
	private final float					POS_TOLERANCE			= AIConfig.getTolerances().getPositioning();
	private final float					MOVE_SPEED_THRESHOLD	= AIConfig.getSkills().getMoveSpeedThreshold();
	

	protected final PIDControllerZ2	pidW;
	private final PIDControllerZ2		pidLen;
	
	private boolean						moveComplete			= false;
	private boolean						rotateComplete			= false;
	
	private float							totalOrientation		= 0.0f;
	private float							lastRawOrientation	= 0.0f;
	

	// Timing
	private static final long			NEVER_PROCESSED		= -1;
	/** Stores the last {@link System#nanoTime()} {@link #calcActions()} was called, used for timing */
	private long							lastProcessed			= NEVER_PROCESSED;
	

	private MoveConstraints				moveConstraints		= new MoveConstraints();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param skill
	 */
	public AMoveSkillV2(ESkillName skill)
	{
		this(skill, AIConfig.getSkills().getRotatePIDConf());
	}
	

	/**
	 * @param skill
	 */
	public AMoveSkillV2(ESkillName skill, PIDControllerConfig pidConfig)
	{
		super(skill, ESkillGroup.MOVE);
		
		final PIDControllerConfig wConf = AIConfig.getSkills().getPids().orientation;
		final PIDControllerConfig lConf = AIConfig.getSkills().getPids().velocity;
		
		pidW = new PIDControllerZ2(wConf.p, wConf.i, wConf.d, wConf.maxOutput, wConf.slewRate);
		pidLen = new PIDControllerZ2(lConf.p, lConf.i, lConf.d, lConf.maxOutput, lConf.slewRate); // high P Factor =>
																																// early brake
		
		// CSVExporter.createInstance("move", "move", false);
		// CSVExporter exporter = CSVExporter.getInstance("move");
		// exporter.setHeader("time", "set_x", "actual_x", "set_y", "actual_y", "set_w", "actual_w", "deltaT");
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
		
		totalOrientation = bot.angle;
		lastRawOrientation = bot.angle;
		
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
		
		Vector2 curPos = new Vector2(bot.pos);
		final float curOrient = bot.angle;
		
		// calc total bot angle (multiturn)
		float diff = curOrient - lastRawOrientation;
		lastRawOrientation = curOrient;
		float addOrient = diff;
		if (diff < -AIMath.PI)
		{
			addOrient = AIMath.PI_TWO + diff;
		}
		
		if (diff > AIMath.PI)
		{
			addOrient = diff - AIMath.PI_TWO;
		}
		
		totalOrientation += addOrient;
		
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
		

		// Process path planning
		Path path = calcPath(getWorldFrame(), moveConstraints);
		

		// Calculate path point jumping distance with velocity, uses fancy inertia to smooth movement
		// ...
		// TODO:...
		float posJumpDiff = 100f; // [mm] // HotFIX GuntherB & AndreR, from 300
		if (!moveConstraints.isFastMove())
		{
			posJumpDiff = 30f; // [mm]
		}
		IVector2 nextPP = curPos;
		
		for (Iterator<IVector2> it = path.path.iterator(); it.hasNext(); nextPP = it.next())
		{
			if (AIMath.distancePP(nextPP, bot.pos) < posJumpDiff)
			{
				// close enough to path point => take next one
			} else
			{
				break;
			}
		}
		
		// Check: Destination reached?
		final float distanceToTarget = path.getLength(getBot().pos);
		if (path.path.size() <= 1 && distanceToTarget < POS_TOLERANCE && getBot().vel.getLength2() < MOVE_SPEED_THRESHOLD)
		{
			moveComplete = true;
		}
		
		Vector2 dstPos = new Vector2(nextPP);
		float dstOrient = calcTargetOrientation(dstPos);
		
		// compute shortest rotation distance
		float rotateDist = dstOrient - bot.angle;
		if (rotateDist < -AIMath.PI)
		{
			rotateDist = AIMath.PI_TWO + rotateDist;
		}
		if (rotateDist > AIMath.PI)
		{
			rotateDist -= AIMath.PI_TWO;
		}
		

		// complete when angle error and turn speed are small enough
		if (Math.abs(rotateDist) < AIConfig.getTolerances().getViewAngle()
				&& Math.abs(getBot().aVel) < AIConfig.getSkills().getRotationSpeedThreshold())
		{
			rotateComplete = true;
		}
		
		// --- Direction ---
		// Transform positions to length+orient system
		Vector2 toDst = dstPos.subtractNew(curPos); // poiting from bot to destination
		float toDstLength = toDst.getLength2() * 0.001f;
		// calculate speed
		PIDControllerConfig veloConfig = AIConfig.getSkills().getPids().velocity;
		
		float arbitraryVelocityFactor = 1;
		if(!moveConstraints.isFastMove()){
			arbitraryVelocityFactor = 0.6f;
		}
		
		float outVelo = toDstLength * veloConfig.p * arbitraryVelocityFactor + veloConfig.i;
		
		if (moveComplete)
			outVelo = 0;
		
		Vector2 outVec = new Vector2(accelerateComplex(toDst, outVelo, bot));
		
		// --- Rotation ---
		
		PIDControllerConfig orientConfig = AIConfig.getSkills().getPids().orientation;
		float outW = rotateDist * orientConfig.p + orientConfig.i;
		// System.out.println("Desired W: " + outW);
		
		outW = accelerateRotation(outVec, outW, bot);
		// System.out.println("Limited. " + outW);
		
		// Compensate roatation
		outVec.turn((float) (-(outW) * deltaT));
		
		// do some logging
		// CSVExporter exporter = CSVExporter.getInstance("move");
		// if (exporter != null)
		// {
		// exporter.addValues(System.nanoTime(), dstPos.x, curPos.x, dstPos.y, curPos.y, dstOrient, totalOrientation,
		// deltaT);
		// }
		
		// Completed?
		if (moveComplete && rotateComplete)
		{
			// only complete when both are complete at the same cycle
			cmds.add(new TigerMotorMoveV2(Vector2.ZERO_VECTOR, 0, 0));
			// exporter.close();
			complete();
		} else
		{
			// otherwise reset
			moveComplete = false;
			rotateComplete = false;
			
			cmds.add(new TigerMotorMoveV2(AIMath.convertGlobalBotVector2Local(outVec, curOrient), outW, 0));
		}
		
		return cmds;
	}
	

	// calcs accel at a given velocity
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
	

	public static float accelerateRotation(IVector2 direction, float desiredW, TrackedTigerBot bot)
	{
		float botTargetVelo = direction.getLength2();
		
		// Mapping: Bot velocity => max angular velocity
		float maxVelocity = calcMaxAccel(new Vector2(0, 3.0f), new Vector2(1.0f, 0.5f), new Vector2(3.0f, 0),
				botTargetVelo);
		// Mapping: Bot angular velocity => max angular acceleration
		float maxAccel = calcMaxAccel(new Vector2(0, 1.0f), new Vector2(1.0f, 0.5f), new Vector2(3.0f, 0.01f), bot.aVel);
		float maxDeccel = calcMaxAccel(new Vector2(0, 1.0f), new Vector2(1.0f, 0.5f), new Vector2(3.0f, 0.01f), bot.aVel);
		
		if (Math.abs(bot.aVel) > maxVelocity)
		{
			// hard brake
			return Math.signum(bot.aVel) * maxVelocity;
		}
		
		float change = desiredW - bot.aVel;
		float inc = change;
		
		if (Math.abs(desiredW) >= Math.abs(bot.aVel)) // x accel
		{
			if (Math.abs(change) > maxAccel)
			{
				inc = Math.signum(change) * maxAccel;
			}
		}
		
		if (Math.abs(desiredW) < Math.abs(bot.aVel)) // x deccel
		{
			if (Math.abs(change) > maxDeccel)
			{
				inc = Math.signum(change) * maxDeccel;
			}
		}
		
		return bot.aVel + inc;
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
		final AccelerationFacade accel = AIConfig.getSkills().getAcceleration();
		
		final IVector2 desiredVelocityVec = direction.scaleToNew(disiredVelocity);
		
		Vector2 localDesiredVelo = AIMath.convertGlobalBotVector2Local(desiredVelocityVec, bot.angle);
		Vector2 localCurrentVelo = AIMath.convertGlobalBotVector2Local(bot.vel, bot.angle);
		
		float newX = localDesiredVelo.x();
		float newY = localDesiredVelo.y();
		float curX = localCurrentVelo.x();
		float curY = localCurrentVelo.y();
		
		float maxXAccel = calcMaxAccel(accel.maxXAccelBeg, accel.maxXAccelMid, accel.maxXAccelEnd, curX);
		float maxYAccel = calcMaxAccel(accel.maxYAccelBeg, accel.maxYAccelMid, accel.maxYAccelEnd, curY);
		float maxXDeccel = calcMaxAccel(accel.maxXDeccelBeg, accel.maxXDeccelMid, accel.maxXDeccelEnd, curX);
		float maxYDeccel = calcMaxAccel(accel.maxYDeccelBeg, accel.maxYDeccelMid, accel.maxYDeccelEnd, curY);
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
			
			if (Math.abs(newX) >= Math.abs(curX)) // x accel
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
			
			if (Math.abs(newY) >= Math.abs(curY)) // y accel
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
		
		return AIMath.convertLocalBotVector2Global(result, bot.angle);
	}
	

	/**
	 * calculate a path
	 * by default, this is asking Sisyphus to do that
	 * can be overridden
	 * 
	 * @param wFrame
	 * @param target
	 * @return
	 */
	protected Path calcPath(WorldFrame wFrame, MoveConstraints constraints)
	{
		// ##### Path
		// Safety check: If target outside field, choose nearest point inside! (suggested by MalteM)
		// and calculate a path
//		System.out.println("Role to:" + getTarget());
		Rectanglef field = AIConfig.getGeometry().getField();
		Rectangle extField = new Rectangle(field.topLeft().addNew(new Vector2(-90, +140)), field.xExtend() + 180, field.yExtend() + 280);
		return getSisyphus().calcPath(wFrame, getBot().id, extField.nearestPointInside(getTarget()),
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
	

	public PidState getPidStateW()
	{
		return pidW.getState();
	}
	

	public PidState getPidStateLen()
	{
		return pidLen.getState();
	}
	

	public void setPidStateW(PidState newPidStateW)
	{
		pidW.setState(newPidStateW);
	}
	

	public void setPidStateLen(PidState newPidStateLen)
	{
		pidLen.setState(newPidStateLen);
	}
}
