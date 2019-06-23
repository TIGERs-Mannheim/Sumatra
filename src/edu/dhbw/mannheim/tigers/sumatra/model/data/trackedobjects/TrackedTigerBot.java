/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;


/**
 * Simple data holder describing TIGER-bots recognized and tracked by the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor}
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @see TrackedBot
 * @see ATrackedObject
 * @author Gero
 */
@Persistent
public class TrackedTigerBot extends TrackedBot
{
	/**  */
	private static final long	serialVersionUID	= -8452911236731387383L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private boolean				ballContact;
	
	private transient boolean	visible				= true;
	private transient ABot		bot					= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private TrackedTigerBot()
	{
		super();
	}
	
	
	/**
	 * Create TrackedTigerBot from TrackedBot
	 * 
	 * @param o
	 */
	protected TrackedTigerBot(final TrackedBot o, final ABot bot)
	{
		this(o.getId(), o.getPos(), o.getVel(), o.getAcc(), o.getHeight(), o.getAngle(), o.getaVel(), o.getaAcc(),
				o.confidence, bot, o.getTeamColor());
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
	 * @param bot
	 * @param color
	 */
	public TrackedTigerBot(final AObjectID id, final IVector2 pos, final IVector2 vel, final IVector2 acc,
			final int height, final float angle, final float aVel,
			final float aAcc, final float confidence, final ABot bot, final ETeamColor color)
	{
		super(id, pos, vel, acc, height, angle, aVel, aAcc, confidence, ETeam.TIGERS, color);
		this.bot = bot;
	}
	
	
	/**
	 * Create a deep copy
	 * 
	 * @param o
	 */
	public TrackedTigerBot(final TrackedTigerBot o)
	{
		this(o.id, o.getPos(), o.getVel(), o.getAcc(), o.getHeight(), o.getAngle(), o.getaVel(), o.getaAcc(), o
				.getConfidence(), o.getBot(), o.getTeamColor());
		ballContact = o.ballContact;
	}
	
	
	/**
	 * static factory for creating a TrackedTigerBot
	 * 
	 * @param id
	 * @param motion
	 * @param height
	 * @param bot
	 * @param color
	 * @return
	 */
	public static TrackedTigerBot motionToTrackedBot(final AObjectID id, final RobotMotionResult_V2 motion,
			final int height, final ABot bot,
			final ETeamColor color)
	{
		TrackedBot trackedBot = TrackedBot.motionToTrackedBot(id, motion, height, color);
		return new TrackedTigerBot(trackedBot, bot);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the botType
	 */
	public final EBotType getBotType()
	{
		if (bot == null)
		{
			return EBotType.UNKNOWN;
		}
		return bot.getType();
	}
	
	
	/**
	 * @return
	 */
	public boolean isManualControl()
	{
		return (getBot() != null) && getBot().isManualControl();
	}
	
	
	/**
	 * @param manualControl
	 */
	public void setManualControl(final boolean manualControl)
	{
		if (getBot() != null)
		{
			getBot().setManualControl(manualControl);
		}
	}
	
	
	/**
	 * @return the ballContact
	 */
	public boolean hasBallContact()
	{
		return ballContact;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("TrackedTigerBot [bot=");
		builder.append(bot);
		builder.append(", pos=");
		builder.append(getPos());
		builder.append("]");
		return builder.toString();
	}
	
	
	/**
	 * @return the bot
	 */
	public final ABot getBot()
	{
		return bot;
	}
	
	
	/**
	 * Used by WP, do not modify!
	 * 
	 * @param ballContact the ballContact to set
	 */
	public final void setBallContact(final boolean ballContact)
	{
		this.ballContact = ballContact;
	}
	
	
	/**
	 * @return the visible
	 */
	public final boolean isVisible()
	{
		return visible;
	}
	
	
	/**
	 * @param visible the visible to set
	 */
	public final void setVisible(final boolean visible)
	{
		this.visible = visible;
	}
}
