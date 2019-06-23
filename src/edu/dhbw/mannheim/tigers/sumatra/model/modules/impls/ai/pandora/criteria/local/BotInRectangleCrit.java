/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 17, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.IRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * Checks if there is at least one bot in the specified rectangle
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BotInRectangleCrit extends ACriterion
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IRectangle	rectangle;
	private final boolean		invertResult;
	
	/**
	 */
	public enum EField
	{
		/**  */
		OUR_HALF,
		/**  */
		THEIR_HALF,
		/**  */
		FULL_FIELD
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param field
	 * @param invertResult
	 */
	public BotInRectangleCrit(EField field, boolean invertResult)
	{
		super(ECriterion.BOT_IN_RECTANGLE);
		float halfLength = AIConfig.getGeometry().getFieldLength() / 2;
		float width = AIConfig.getGeometry().getFieldWidth();
		switch (field)
		{
			case FULL_FIELD:
				rectangle = AIConfig.getGeometry().getField();
				break;
			case OUR_HALF:
				rectangle = new Rectangle(new Vector2(-halfLength, -width / 2), halfLength, width);
				break;
			case THEIR_HALF:
				rectangle = new Rectangle(new Vector2(0, -width / 2), halfLength, width);
				break;
			default:
				rectangle = null;
				throw new IllegalArgumentException();
		}
		this.invertResult = invertResult;
	}
	
	
	/**
	 * @param rect
	 * @param invertResult
	 */
	public BotInRectangleCrit(Rectangle rect, boolean invertResult)
	{
		super(ECriterion.BOT_IN_RECTANGLE);
		rectangle = rect;
		this.invertResult = invertResult;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected float doCheckCriterion(AIInfoFrame currentFrame)
	{
		for (TrackedTigerBot bot : currentFrame.worldFrame.tigerBotsAvailable.values())
		{
			if (rectangle.isPointInShape(bot.getPos()))
			{
				if (invertResult)
				{
					return 0f;
				}
				return 1f;
			}
		}
		if (invertResult)
		{
			return 1f;
		}
		return 0f;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
