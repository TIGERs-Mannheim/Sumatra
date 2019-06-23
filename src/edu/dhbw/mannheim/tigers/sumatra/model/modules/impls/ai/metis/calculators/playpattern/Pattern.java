/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.04.2012
 * Author(s): Paul
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern;

import java.io.Serializable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * This represents a detected play pattern of the enemy. The actual situation can be compared with this pattern.
 * 
 * @author PaulB , OliverS
 * 
 */
public class Pattern implements Serializable, Comparable<Pattern>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long					serialVersionUID				= -8169924221595742209L;
	
	
	private final transient float				minX								= -AIConfig.getGeometry().getFieldLength() / 2;
	private final transient float				maxX								= AIConfig.getGeometry().getFieldLength() / 2;
	
	private final transient float				minY								= -AIConfig.getGeometry().getFieldWidth() / 2;
	private final transient float				maxY								= AIConfig.getGeometry().getFieldWidth() / 2;
	
	private static final transient float	ANGLE_MIN						= -AngleMath.PI;
	private static final transient float	ANGLE_MAX						= AngleMath.PI;
	
	private static final transient int		SCALE_MATCHING_SCORE			= 10;
	
	private static transient int				patternIndexCounter			= 0;
	private static final transient int		UNINITIALIZED_PATTERN_ID	= -1;
	
	
	private static final float					ANGLE_DIFF_TOLL				= 0.5f;
	private transient int						patternIndex					= UNINITIALIZED_PATTERN_ID;
	
	
	private final IVector2						patternPasserPos;
	private final float							patternPasserAngle;
	private final IVector2						patternRecieverPos;
	private final float							patternReceiverAngle;
	
	/** score which is used as a metric how the actual frame matches to this pattern. [0-1] */
	private transient double					matchingScore;
	
	/** indicates if this pattern was loaded from log file. */
	private final transient boolean			isPersisted;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param passerPosition
	 * @param recieverPosition
	 * @param passerAngle
	 * @param receiverAngle
	 * @param isPersisted true when pattern was loaded from log
	 * @throws IllegalArgumentException
	 */
	public Pattern(IVector2 passerPosition, IVector2 recieverPosition, float passerAngle, float receiverAngle,
			boolean isPersisted)
	{
		if ((passerPosition == null) || (recieverPosition == null))
		{
			throw new IllegalArgumentException("Pattern cannot be initialized with 'NULL' bots.");
		}
		
		patternPasserPos = passerPosition;
		patternPasserAngle = passerAngle;
		patternRecieverPos = recieverPosition;
		patternReceiverAngle = receiverAngle;
		this.isPersisted = isPersisted;
	}
	
	
	/**
	 * @param passerPosition
	 * @param recieverPosition
	 * @param passerAngle
	 * @param receiverAngle
	 * @throws IllegalArgumentException
	 */
	public Pattern(IVector2 passerPosition, IVector2 recieverPosition, float passerAngle, float receiverAngle)
	{
		this(passerPosition, recieverPosition, passerAngle, receiverAngle, false);
	}
	
	
	/**
	 * This ctor is used to recreate persisted pattern with a valid id.
	 * [isPersisted = true]
	 * 
	 * @param pattern
	 */
	public Pattern(Pattern pattern)
	{
		this(pattern.getPasser(), pattern.getReciever(), pattern.getPasserAngle(), pattern.getReceiverAngle(), true);
		initializeIndex();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Compares the actual frame with this pattern. {@link #matchingScore} will be refreshed.
	 * 
	 * @param aiFrame
	 */
	public void compare(AIInfoFrame aiFrame)
	{
		final TrackedBot actualPasser = getPasser(aiFrame);
		if (actualPasser == null)
		{
			return;
		}
		final TrackedBot actualReceiver = getReciever(aiFrame, actualPasser.getId());
		if (actualReceiver == null)
		{
			return;
		}
		
		// score
		final double botScore = SCALE_MATCHING_SCORE
				* (calcPositionDifference(actualPasser.getPos(), patternPasserPos) + calcPositionDifference(
						actualReceiver.getPos(), patternRecieverPos));
		
		// score which checks if the ball is near the passer position of this pattern
		final double ballScore = SCALE_MATCHING_SCORE
				* calcPositionDifference(aiFrame.worldFrame.ball.getPos(), patternPasserPos);
		
		
		final double angleScore = SCALE_MATCHING_SCORE
				* (calcAngleDistance(patternPasserAngle, actualPasser.getAngle()) + calcAngleDistance(patternReceiverAngle,
						actualReceiver.getAngle()));
		
		
		matchingScore = 1 - (botScore + ballScore + angleScore);
		
		if (matchingScore < 0)
		{
			matchingScore = 0;
		}
	}
	
	
	/**
	 * Calculates the difference of the two positions.
	 * ATTENTION: This is NOT an euclidean distance!!
	 * 
	 * @param position1
	 * @param position2
	 * @return
	 */
	private double calcPositionDifference(IVector2 position1, IVector2 position2)
	{
		final double distanceX = Math.pow((getNormalX(position2.x()) - getNormalX(position1.x())), 2);
		final double distanceY = Math.pow((getNormalY(position2.y()) - getNormalY(position1.y())), 2);
		return distanceX + distanceY;
	}
	
	
	/**
	 * distance between angles
	 * 
	 * @param patternAngle
	 * @param angle
	 * @return
	 */
	private float calcAngleDistance(float patternAngle, float angle)
	{
		final float normalPatternAngle = getNormalAngle(patternAngle);
		final float normalAngle = getNormalAngle(angle);
		
		final float angleDiff = Math.abs(normalPatternAngle - normalAngle);
		
		if (angleDiff > ANGLE_DIFF_TOLL)
		{
			return 1 - angleDiff;
		}
		return angleDiff;
	}
	
	
	/**
	 * Estimates the passer in the actual frame.
	 * 
	 * @param aiFrame
	 * @return passer
	 */
	public TrackedBot getPasser(AIInfoFrame aiFrame)
	{
		return AiMath.getNearestBot(aiFrame.worldFrame.foeBots, patternPasserPos);
	}
	
	
	/**
	 * Estimates the receiver in the actual frame.
	 * 
	 * @param aiFrame
	 * @param passerID
	 * @return passer
	 */
	private TrackedBot getReciever(AIInfoFrame aiFrame, BotID passerID)
	{
		// remove passer from foeBot map
		final IBotIDMap<TrackedBot> receiverCandidates = new BotIDMap<TrackedBot>(aiFrame.worldFrame.foeBots);
		receiverCandidates.remove(passerID);
		
		return AiMath.getNearestBot(receiverCandidates, patternRecieverPos);
	}
	
	
	@Override
	public String toString()
	{
		return ("Pattern id:" + patternIndex + " passer pos.: " + patternPasserPos + " receiver pos.: " + patternRecieverPos);
	}
	
	
	@Override
	public int compareTo(Pattern pattern)
	{
		// sort descending
		if (getMatchingScore() > pattern.getMatchingScore())
		{
			return -1;
		} else if (getMatchingScore() < pattern.getMatchingScore())
		{
			return 1;
		}
		return 0;
	}
	
	
	/**
	 * Initializes this pattern with a valid ID.
	 */
	public final void initializeIndex()
	{
		if (patternIndexCounter != Integer.MAX_VALUE)
		{
			incPatternIndexCounter();
		} else
		{
			resetPatternIndexCounter();
		}
		patternIndex = patternIndexCounter;
	}
	
	
	/**
	 * increases pattern counter
	 */
	private static void incPatternIndexCounter()
	{
		patternIndexCounter++;
	}
	
	
	/**
	 * reset pattern counter
	 */
	private static void resetPatternIndexCounter()
	{
		patternIndexCounter = 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public IVector2 getPasser()
	{
		return patternPasserPos;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public IVector2 getReciever()
	{
		return patternRecieverPos;
	}
	
	
	/**
	 * @return the patternPasserAngle
	 */
	public float getPasserAngle()
	{
		return patternPasserAngle;
	}
	
	
	/**
	 * @return the patternReceiverAngle
	 */
	public float getReceiverAngle()
	{
		return patternReceiverAngle;
	}
	
	
	/**
	 * Returns index of this pattern. Each pattern Id is unique.
	 * 
	 * @return index of this pattern. UNINITIALIZED_PATTERN_ID==-1
	 */
	public int getIndex()
	{
		return patternIndex;
	}
	
	
	/**
	 * Returns the score which is used as a metric how the
	 * actual frame matches to this pattern.
	 * 
	 * @return matchingScore [0-1]
	 */
	public double getMatchingScore()
	{
		return matchingScore;
	}
	
	
	/**
	 * Normalize x-Coordinate.
	 * 
	 * @param x
	 * @return normalized x
	 */
	private float getNormalX(float x)
	{
		return (x - minX) / (maxX - minX);
	}
	
	
	/**
	 * Normalize y-Coordinate.
	 * 
	 * @param y
	 * @return normalized y
	 */
	private float getNormalY(float y)
	{
		return (y - minY) / (maxY - minY);
	}
	
	
	/**
	 * Normalize bot angle.
	 * 
	 * @param angle
	 * @return normalized angle
	 */
	private float getNormalAngle(float angle)
	{
		return (angle - ANGLE_MIN) / (ANGLE_MAX - ANGLE_MIN);
	}
	
	
	/**
	 * @return the isPersisted
	 */
	public boolean isPersisted()
	{
		return isPersisted;
	}
	
	
	/**
	 * @param pat
	 * @return if pattern is like an already found pattern
	 */
	public boolean isSameAs(Pattern pat)
	{
		if ((patternPasserPos.subtractNew(pat.patternPasserPos).getLength2() < (AIConfig.getGeometry().getBotRadius() * 3))
				&& (patternRecieverPos.subtractNew(pat.patternRecieverPos).getLength2() < (AIConfig.getGeometry()
						.getBotRadius() * 3)))
		{
			return true;
		}
		
		return false;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Float.floatToIntBits(patternPasserAngle);
		result = (prime * result) + ((patternPasserPos == null) ? 0 : patternPasserPos.hashCode());
		result = (prime * result) + Float.floatToIntBits(patternReceiverAngle);
		result = (prime * result) + ((patternRecieverPos == null) ? 0 : patternRecieverPos.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		Pattern other = (Pattern) obj;
		if (Float.floatToIntBits(patternPasserAngle) != Float.floatToIntBits(other.patternPasserAngle))
		{
			return false;
		}
		if (patternPasserPos == null)
		{
			if (other.patternPasserPos != null)
			{
				return false;
			}
		} else if (!patternPasserPos.equals(other.patternPasserPos))
		{
			return false;
		}
		if (Float.floatToIntBits(patternReceiverAngle) != Float.floatToIntBits(other.patternReceiverAngle))
		{
			return false;
		}
		if (patternRecieverPos == null)
		{
			if (other.patternRecieverPos != null)
			{
				return false;
			}
		} else if (!patternRecieverPos.equals(other.patternRecieverPos))
		{
			return false;
		}
		return true;
	}
	
}
