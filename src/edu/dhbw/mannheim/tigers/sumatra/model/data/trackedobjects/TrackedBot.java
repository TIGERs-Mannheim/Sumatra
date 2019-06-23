/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects;

import net.sf.oval.constraint.NotNull;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.UninitializedID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.RobotMotionResult_V2;


/**
 * Simple data holder describing bots recognized and tracked by the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor}
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see ATrackedObject
 * @author Gero
 */
@Persistent(version = 1)
public class TrackedBot extends ATrackedObject
{
	/** mm */
	@NotNull
	private final Vector2	pos;
	/** m/s */
	@NotNull
	private final Vector2	vel;
	/** m/s^2 */
	private final Vector2	acc;
	
	/** mm */
	private final int			height;
	/** rad not final for mirroring */
	private float				angle;
	/** rad/s */
	private final float		aVel;
	/** rad/s^2 */
	private final float		aAcc;
	
	private final BotID		botId;
	
	
	@SuppressWarnings("unused")
	protected TrackedBot()
	{
		super(new UninitializedID(), 0);
		pos = new Vector2();
		acc = new Vector2();
		vel = new Vector2();
		height = 0;
		angle = 0;
		aVel = 0;
		aAcc = 0;
		botId = BotID.createBotId(id.getNumber(), ETeamColor.YELLOW);
	}
	
	
	/**
	 * @param id
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param height
	 * @param angle
	 * @param aVel
	 * @param aAcc
	 * @param confidence
	 * @param team
	 */
	protected TrackedBot(final AObjectID id, final IVector2 pos, final IVector2 vel, final IVector2 acc,
			final int height, final float angle, final float aVel,
			final float aAcc, final float confidence, final ETeam team, final ETeamColor teamColor)
	{
		super(id, confidence);
		this.pos = new Vector2(pos);
		this.acc = new Vector2(acc);
		this.vel = new Vector2(vel);
		this.height = height;
		this.angle = angle;
		this.aVel = aVel;
		this.aAcc = aAcc;
		botId = BotID.createBotId(id.getNumber(), teamColor);
	}
	
	
	/**
	 * @param id
	 * @param motion
	 * @param height
	 * @return
	 */
	protected static TrackedBot motionToTrackedBot(final AObjectID id, final RobotMotionResult_V2 motion,
			final int height,
			final ETeamColor color)
	{
		IVector2 pos = new Vector2f((float) (motion.x / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT),
				(float) (motion.y / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT));
		final float v = (float) (motion.v / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V);
		
		final double xVel = v * Math.cos(motion.movementAngle);
		final double yVel = v * Math.sin(motion.movementAngle);
		final IVector2 vel = new Vector2f((float) xVel, (float) yVel);
		final IVector2 acc = new Vector2f(0.0f, 0.0f);
		
		final float angle = (float) motion.orientation;
		final float aVel = (float) ((motion.angularVelocity + motion.trackSpeed) / WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal);
		final float aAcc = 0f;
		
		final float confidence = (float) motion.confidence;
		return new TrackedBot(id, pos, vel, acc, height, angle, aVel, aAcc, confidence, ETeam.UNKNOWN, color);
	}
	
	
	/**
	 * Mirror position, velocity and acceleration over x and y axis.
	 * This instance will be modified!
	 * Do NEVER call this in the AI!
	 */
	public void mirrorBot()
	{
		pos.setX(-pos.x);
		pos.setY(-pos.y);
		vel.setX(-vel.x);
		vel.setY(-vel.y);
		acc.setX(-acc.x);
		acc.setY(-acc.y);
		angle = AngleMath.normalizeAngle(angle + AngleMath.PI);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public BotID getId()
	{
		return botId;
	}
	
	
	/**
	 * @return the aAcc
	 */
	public float getaAcc()
	{
		return aAcc;
	}
	
	
	/**
	 * Angle is relative to the x coordinates in mathematical way. Be aware that the field coordinates are mirrored on
	 * the field. In Sumatra Visualizer the first quadrant is the bottom right instead of bottom up.
	 * 
	 * @return the angle
	 */
	public float getAngle()
	{
		return angle;
	}
	
	
	/**
	 * @return the aVel
	 */
	public float getaVel()
	{
		return aVel;
	}
	
	
	/**
	 * @return the height
	 */
	public int getHeight()
	{
		return height;
	}
	
	
	@Override
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return vel;
	}
	
	
	@Override
	public IVector2 getAcc()
	{
		return acc;
	}
	
	
	/**
	 * @return the teamColor
	 */
	public final ETeamColor getTeamColor()
	{
		return botId.getTeamColor();
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("TrackedBot [pos=");
		builder.append(pos);
		builder.append(", vel=");
		builder.append(vel);
		builder.append(", acc=");
		builder.append(acc);
		builder.append(", angle=");
		builder.append(angle);
		builder.append(", aVel=");
		builder.append(aVel);
		builder.append(", aAcc=");
		builder.append(aAcc);
		builder.append(", botId=");
		builder.append(botId);
		builder.append("]");
		return builder.toString();
	}
}
