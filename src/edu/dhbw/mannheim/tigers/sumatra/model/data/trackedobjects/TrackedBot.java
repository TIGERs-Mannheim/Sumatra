/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;


/**
 * Simple data holder describing bots recognized and tracked by the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor}
 * 
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see ATrackedObject
 * @author Gero
 * 
 */
@Embeddable
public class TrackedBot extends ATrackedObject
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long	serialVersionUID	= 3617073411160054690L;
	
	
	/** mm */
	private IVector2				pos;
	/** m/s */
	private IVector2				vel;
	/** m/s^2 */
	private IVector2				acc;
	
	/** mm, not final for ObjectDB */
	private int						height;
	/** rad, not final for ObjectDB */
	private float					angle;
	/** rad/s,not final for ObjectDB */
	private float					aVel;
	/** rad/s^2, not final for ObjectDB */
	private float					aAcc;
	/** team, the bot belongs to, not final for ObjectDB */
	private ETeam					team					= ETeam.UNKNOWN;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param o
	 */
	protected TrackedBot(TrackedBot o, ETeam team)
	{
		this(o.id, o.getPos(), o.getVel(), o.getAcc(), o.getHeight(), o.getAngle(), o.getaVel(), o.getaAcc(),
				o.confidence, team);
	}
	
	
	/**
	 * 
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
	protected TrackedBot(AObjectID id, IVector2 pos, IVector2 vel, IVector2 acc, int height, float angle, float aVel,
			float aAcc, float confidence, ETeam team)
	{
		super(id, confidence);
		this.pos = pos;
		this.acc = acc;
		this.vel = vel;
		this.height = height;
		this.angle = angle;
		this.aVel = aVel;
		this.aAcc = aAcc;
		this.team = team;
	}
	
	
	/**
	 * 
	 * @param id
	 * @param motion
	 * @param height
	 * @return
	 */
	protected static TrackedBot motionToTrackedBot(AObjectID id, RobotMotionResult_V2 motion, int height)
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
		return new TrackedBot(id, pos, vel, acc, height, angle, aVel, aAcc, confidence, ETeam.OPPONENTS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public BotID getId()
	{
		return new BotID(id.getNumber(), team);
	}
	
	
	@Override
	public String toString()
	{
		return "TrackedBot [height=" + getHeight() + ", angle=" + getAngle() + ", aVel=" + getaVel() + ", aAcc="
				+ getaAcc() + ", team=" + team + "]";
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
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param height the height to set
	 */
	protected void setHeight(int height)
	{
		this.height = height;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param angle the angle to set
	 */
	protected void setAngle(float angle)
	{
		this.angle = angle;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param aVel the aVel to set
	 */
	protected void setaVel(float aVel)
	{
		this.aVel = aVel;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param aAcc the aAcc to set
	 */
	protected void setaAcc(float aAcc)
	{
		this.aAcc = aAcc;
	}
	
	
	/**
	 * This method is not for setting the value. It only prevents eclipse from making the value final. The value has not
	 * to be final, because it is written and read from a db for the learning play finder.
	 * @param team the team to set
	 */
	protected void setTeam(ETeam team)
	{
		this.team = team;
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
	
}
