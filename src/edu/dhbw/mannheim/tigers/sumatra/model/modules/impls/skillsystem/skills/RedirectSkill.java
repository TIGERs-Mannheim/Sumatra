/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Prepare for redirect
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectSkill extends PositionSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final DynamicPosition	target;
	
	@Configurable
	private static float				shootSpeed	= 4.0f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param target
	 */
	public RedirectSkill(final DynamicPosition target)
	{
		super(ESkillName.REDIRECT);
		this.target = target;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void doCalcActions(final List<ACommand> cmds)
	{
		TrackedTigerBot tBot = getWorldFrame().getBot(getBot().getBotID());
		IVector3 poss = AiMath.calcRedirectPositions(tBot, getWorldFrame().getBall(), target, shootSpeed);
		setDestination(poss.getXYVector());
		setOrientation(poss.z());
		// this is not good practice, but unless the skill is not used productively, thats ok...
		// it is to ensure that the kicker is always armed, even if it had kicked already
		getDevices().kickMax(cmds);
	}
	
	
	@Override
	public List<ACommand> calcExitActions(final List<ACommand> cmds)
	{
		getDevices().disarm(cmds);
		return cmds;
	}
	
	
	@Override
	public List<ACommand> calcEntryActions(final List<ACommand> cmds)
	{
		getDevices().kickMax(cmds);
		return cmds;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
