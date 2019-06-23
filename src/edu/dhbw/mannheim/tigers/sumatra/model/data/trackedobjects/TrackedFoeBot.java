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
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;


/**
 * Simple data holder describing FOE-bots recognized and tracked by the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor}
 * 
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see TrackedBot
 * @see ATrackedObject
 * @author Gero
 * 
 */
@Embeddable
public class TrackedFoeBot extends TrackedBot implements Comparable<TrackedBot>
{
	/**  */
	private static final long	serialVersionUID	= -8452911236731387383L;
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param o
	 */
	public TrackedFoeBot(TrackedFoeBot o)
	{
		this(o.getId(), o.getPos(), o.getVel(), o.getAcc(), o.getHeight(), o.getAngle(), o.getaVel(), o.getaAcc(),
				o.confidence);
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
	 */
	public TrackedFoeBot(AObjectID id, IVector2 pos, IVector2 vel, IVector2 acc, int height, float angle, float aVel,
			float aAcc, float confidence)
	{
		super(id, pos, vel, acc, height, angle, aVel, aAcc, confidence, ETeam.OPPONENTS);
	}
	
	
	/**
	 * Create TrackedFoeBot from TrackedBot
	 * @param tb trackedBot
	 */
	protected TrackedFoeBot(TrackedBot tb)
	{
		this(tb.getId(), tb.getPos(), tb.getVel(), tb.getAcc(), tb.getHeight(), tb.getAngle(), tb.getaVel(),
				tb.getaAcc(), tb.confidence);
	}
	
	
	/**
	 * static factory for creating a TrackedTigerBot
	 * 
	 * @param id
	 * @param motion
	 * @param height
	 * @return
	 */
	public static TrackedFoeBot motionToTrackedBot(AObjectID id, RobotMotionResult_V2 motion, int height)
	{
		TrackedBot trackedBot = TrackedBot.motionToTrackedBot(id, motion, height);
		return new TrackedFoeBot(trackedBot);
	}
	
	
	@Override
	public int compareTo(TrackedBot tfb)
	{
		if (getId().getNumber() < tfb.getId().getNumber())
		{
			return -1;
		} else if (getId().getNumber() == tfb.getId().getNumber())
		{
			return 0;
		}
		return 1;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
