/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillPositioningCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Move to a given destination and orientation with PositionController
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PositionSkill extends ASkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private DynamicPosition			destination;
	private float						orientation;
	
	@Configurable(comment = "If in vel mode, this is multiplied with position error", speziType = EBotType.class, spezis = { "GRSIM" })
	private static float				velModeParamPos		= 0.005f;
	
	@Configurable(comment = "If in vel mode, this is multiplied with rotation error", speziType = EBotType.class, spezis = { "GRSIM" })
	private static float				velModeParamOrient	= 2.0f;
	
	
	@Configurable(comment = "max dist [mm] that destination may differ from bot pos. If greater, dest is modified.")
	private static float				maxDistanceFast		= 400;
	
	@Configurable(comment = "Max dist [mm] in slow mode")
	private static float				maxDistanceSlow		= 200;
	
	private float						maxDistance				= maxDistanceFast;
	
	@Configurable(comment = "What kind of command should be send to the bot?", speziType = EBotType.class, spezis = { "GRSIM" })
	private static ECommandMode	defaultCommandMode	= ECommandMode.VEL;
	private ECommandMode				commandMode				= defaultCommandMode;
	
	protected enum ECommandMode
	{
		POS,
		VEL;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Do not use this constructor, if you extend from this class
	 * 
	 * @param dest
	 * @param orient
	 */
	public PositionSkill(final IVector2 dest, final float orient)
	{
		this(ESkillName.POSITION, dest, orient);
	}
	
	
	/**
	 * Use this if you extend from this skill
	 * 
	 * @param skillName
	 * @param dest
	 * @param orient
	 */
	protected PositionSkill(final ESkillName skillName, final IVector2 dest, final float orient)
	{
		super(skillName);
		destination = new DynamicPosition(dest);
		orientation = orient;
	}
	
	
	/**
	 * Use this if you extend from this skill
	 * 
	 * @param skillName
	 */
	protected PositionSkill(final ESkillName skillName)
	{
		super(skillName);
		destination = null;
		orientation = 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public final List<ACommand> calcActions(final List<ACommand> cmds)
	{
		doCalcActions(cmds);
		if (destination != null)
		{
			Vector2 dest;
			if (GeoMath.distancePP(destination, getPos()) > maxDistance)
			{
				dest = GeoMath.stepAlongLine(getPos(), destination, maxDistance);
			} else
			{
				dest = new Vector2(destination);
			}
			float orient = orientation;
			switch (commandMode)
			{
				case POS:
					
					if (getWorldFrame().isInverted())
					{
						dest.multiply(-1);
						orient = AngleMath.normalizeAngle(orientation + AngleMath.PI);
					}
					cmds.add(new TigerSkillPositioningCommand(dest, orient));
					break;
				case VEL:
					final TigerMotorMoveV2 move = new TigerMotorMoveV2();
					IVector2 error = dest.subtractNew(getPos()).multiply(velModeParamPos);
					IVector2 localVel = AiMath.convertGlobalBotVector2Local(error, getAngle());
					move.setX(localVel.x());
					move.setY(localVel.y());
					
					float errorW = orient - getAngle();
					move.setW(AngleMath.normalizeAngle(errorW) * velModeParamOrient);
					cmds.add(move);
					break;
				default:
					break;
			}
		}
		return cmds;
	}
	
	
	/**
	 * This is called before PositionSkill calcs actions
	 * 
	 * @param cmds
	 */
	protected void doCalcActions(final List<ACommand> cmds)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * drive slow
	 */
	public final void slow()
	{
		maxDistance = maxDistanceSlow;
	}
	
	
	/**
	 * @return the destination
	 */
	public final IVector2 getDestination()
	{
		return destination;
	}
	
	
	/**
	 * @return the orientation
	 */
	public final float getOrientation()
	{
		return orientation;
	}
	
	
	/**
	 * @param destination the destination to set
	 */
	public final void setDestination(final IVector2 destination)
	{
		this.destination = new DynamicPosition(destination);
	}
	
	
	/**
	 * @param orientation the orientation to set
	 */
	public final void setOrientation(final float orientation)
	{
		this.orientation = orientation;
	}
	
	
	/**
	 * @param commandMode the commandMode to set
	 */
	public final void setCommandMode(final ECommandMode commandMode)
	{
		this.commandMode = commandMode;
	}
	
}
