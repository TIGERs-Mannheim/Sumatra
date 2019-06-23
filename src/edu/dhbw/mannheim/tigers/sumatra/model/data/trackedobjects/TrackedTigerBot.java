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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.RobotMotionResult_V2;


/**
 * Simple data holder describing TIGER-bots recognized and tracked by the
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
public class TrackedTigerBot extends TrackedBot
{
	/**  */
	private static final long	serialVersionUID	= -8452911236731387383L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** not final for ObjectDB */
	private boolean				ballContact;
	
	/** special treatment, when bot is manual controlled */
	private transient boolean	manualControl		= false;
	
	private transient Path		path					= null;
	
	private transient ABot		bot					= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Providing a <strong>hard, deep</strong> copy of original
	 * @param o
	 */
	public TrackedTigerBot(TrackedTigerBot o)
	{
		this(o.getId(), o.getPos(), o.getVel(), o.getAcc(), o.getHeight(), o.getAngle(), o.getaVel(), o.getaAcc(),
				o.confidence, o.bot);
	}
	
	
	/**
	 * Create TrackedTigerBot from TrackedBot
	 * @param o
	 */
	protected TrackedTigerBot(TrackedBot o, ABot bot)
	{
		this(o.getId(), o.getPos(), o.getVel(), o.getAcc(), o.getHeight(), o.getAngle(), o.getaVel(), o.getaAcc(),
				o.confidence, bot);
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
	 * @param bot
	 */
	public TrackedTigerBot(AObjectID id, IVector2 pos, IVector2 vel, IVector2 acc, int height, float angle, float aVel,
			float aAcc, float confidence, ABot bot)
	{
		super(id, pos, vel, acc, height, angle, aVel, aAcc, confidence, ETeam.TIGERS);
		this.bot = bot;
	}
	
	
	/**
	 * static factory for creating a TrackedTigerBot
	 * 
	 * @param id
	 * @param motion
	 * @param height
	 * @param bot
	 * @return
	 */
	public static TrackedTigerBot motionToTrackedBot(AObjectID id, RobotMotionResult_V2 motion, int height, ABot bot)
	{
		TrackedBot trackedBot = TrackedBot.motionToTrackedBot(id, motion, height);
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
	 * 
	 * @return
	 */
	public boolean isManualControl()
	{
		return manualControl;
	}
	
	
	/**
	 * 
	 * @param manualControl
	 */
	public void setManualControl(boolean manualControl)
	{
		this.manualControl = manualControl;
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
	 * @return the path
	 */
	public final Path getPath()
	{
		return path;
	}
	
	
	/**
	 * @param path the path to set
	 */
	public final void setPath(Path path)
	{
		this.path = path;
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
	public final void setBallContact(boolean ballContact)
	{
		this.ballContact = ballContact;
	}
}
