/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.UninitializedID;


/**
 * This {@link DynamicPosition} represents either a normal position vector
 * or an updateable position connected with an object id
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DynamicPosition implements IVector2
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2			pos	= Vector2.ZERO_VECTOR;
	private final AObjectID	trackedId;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param obj
	 */
	public DynamicPosition(final ATrackedObject obj)
	{
		trackedId = obj.getId();
		pos = obj.getPos();
	}
	
	
	/**
	 * @param pos
	 */
	public DynamicPosition(final IVector2 pos)
	{
		this.pos = pos;
		trackedId = new UninitializedID();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Update position by consulting {@link SimpleWorldFrame}
	 * 
	 * @param swf
	 */
	public final void update(final SimpleWorldFrame swf)
	{
		if (trackedId.isBall())
		{
			pos = swf.getBall().getPos();
		} else if (trackedId.isBot())
		{
			TrackedTigerBot bot = swf.getBot((BotID) trackedId);
			if (bot != null)
			{
				pos = bot.getPos();
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the trackedId
	 */
	public final AObjectID getTrackedId()
	{
		return trackedId;
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	public final void setPos(final IVector2 pos)
	{
		this.pos = pos;
	}
	
	
	@Override
	public float x()
	{
		return pos.x();
	}
	
	
	@Override
	public float y()
	{
		return pos.y();
	}
	
	
	@Override
	public Vector2 normalizeNew()
	{
		return pos.normalizeNew();
	}
	
	
	@Override
	public Vector2 addNew(final IVector2 vector)
	{
		return pos.addNew(vector);
	}
	
	
	@Override
	public Vector2 subtractNew(final IVector2 vector)
	{
		return pos.subtractNew(vector);
	}
	
	
	@Override
	public Vector2 multiplyNew(final float factor)
	{
		return pos.multiplyNew(factor);
	}
	
	
	@Override
	public Vector2 scaleToNew(final float newLength)
	{
		return pos.scaleToNew(newLength);
	}
	
	
	@Override
	public Vector2 turnNew(final float angle)
	{
		return pos.turnNew(angle);
	}
	
	
	@Override
	public Vector2 turnToNew(final float angle)
	{
		return pos.turnToNew(angle);
	}
	
	
	@Override
	public IVector2 roundNew(final int digits)
	{
		return pos.roundNew(digits);
	}
	
	
	@Override
	public float getLength2()
	{
		return pos.getLength2();
	}
	
	
	@Override
	public boolean isZeroVector()
	{
		return pos.isZeroVector();
	}
	
	
	@Override
	public boolean isVertical()
	{
		return pos.isVertical();
	}
	
	
	@Override
	public boolean isHorizontal()
	{
		return pos.isHorizontal();
	}
	
	
	@Override
	public float getAngle()
	{
		return pos.getAngle();
	}
	
	
	@Override
	public float scalarProduct(final IVector2 v)
	{
		return pos.scalarProduct(v);
	}
	
	
	@Override
	public IVector2 projectToNew(final IVector2 v)
	{
		return pos.projectToNew(v);
	}
	
	
	@Override
	public boolean equals(final IVector2 vec, final float tolerance)
	{
		return pos.equals(vec, tolerance);
	}
	
	
	@Override
	public Vector2 absNew()
	{
		return pos.absNew();
	}
	
	
	@Override
	public IVector2 getNormalVector()
	{
		return pos.getNormalVector();
	}
	
	
	@Override
	public String getSaveableString()
	{
		if (trackedId.isBot())
		{
			BotID botId = (BotID) trackedId;
			return trackedId.getNumber() + " " + botId.getTeamColor().name();
		} else if (trackedId.isBall())
		{
			return "-1";
		}
		return pos.x() + "," + pos.y();
	}
	
	
	@Override
	public boolean similar(final IVector2 vec, final float treshold)
	{
		return pos.similar(vec, treshold);
	}
	
	
	@Override
	public String toString()
	{
		return "DynamicPosition [pos=" + pos + ", trackedId=" + trackedId + "]";
	}
	
	
	/**
	 * String must look like "0 BLUE" or "0,0"
	 * 
	 * @param value
	 * @return
	 */
	public static Object valueOf(final String value)
	{
		try
		{
			return new DynamicPosition(Vector2.valueOf(value));
		} catch (NumberFormatException err)
		{
			// This is not a simple position, go on with id detection
		}
		
		String[] values = value.replaceAll("[,;]", " ").split("[ ]");
		List<String> finalValues = new ArrayList<String>(2);
		for (String val : values)
		{
			if (!val.trim().isEmpty() && !val.contains(","))
			{
				finalValues.add(val.trim());
			}
		}
		if ((finalValues.size() > 2) || (finalValues.size() < 1))
		{
			throw new NumberFormatException("Format does not conform to: id[[, ]color]. Values: " + finalValues);
		}
		int id = Integer.valueOf(finalValues.get(0));
		if (id == -1)
		{
			return new DynamicPosition(new TrackedBall(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR,
					1,
					false));
		}
		if (finalValues.size() != 2)
		{
			throw new NumberFormatException("missing bot id color");
		}
		ETeamColor color = ETeamColor.valueOf(finalValues.get(1));
		return new DynamicPosition(new TrackedTigerBot(BotID.createBotId(id, color), AVector2.ZERO_VECTOR,
				AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0, 0, 1, null, color));
	}
}
