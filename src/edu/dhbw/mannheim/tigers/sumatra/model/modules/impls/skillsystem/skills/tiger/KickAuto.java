/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.06.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * TODO Malte, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Malte
 */
public class KickAuto extends ASkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// private static final float TOLERANCE = 20;
	
	private long	startTime	= -1;
	// private float startDistance;
	
	private float	kickLength	= -1;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param kickLength : how long the ball shall be kicked, will kick with Max if kickLength equals -1
	 * @param skill
	 * @param group
	 */
	public KickAuto(float kickLength)
	{
		super(ESkillName.KICK_AUTO, ESkillGroup.MOVE);
		this.kickLength = kickLength;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public ArrayList<ACommand> calcEntryActions(ArrayList<ACommand> cmds)
	{
		startTime = System.nanoTime();
		
		// // Distance ball
		// final IVector2 ballPos = getWorldFrame().ball.pos;
		// final IVector2 kickerPos = AIMath.getKickerPosFromBot(getWorldFrame(), getBot().id);
		// startDistance = AIMath.distancePP(ballPos, kickerPos);
		
		return cmds;
	}
	

	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		final long now = System.nanoTime();
		if (now - startTime < 5e7)
		{
			// Vorfahren
			cmds.add(new TigerMotorMoveV2(new Vector2(0, 1.25f)));
		} else
		{
			// Kicken
			
			if (kickLength < 0)
			{
				// maximum strength
				cmds.add(new TigerKickerKickV2(TigerKickerKickV2.MAX_DURATION, TigerKickerKickV2.Mode.ARM));
			} else
			{
				cmds.add(new TigerKickerKickV2(kickLength, TigerKickerKickV2.Mode.ARM));
			}
			
			// System.out.println("Kick!!!");
			complete();
		}
		
		return cmds;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		return true;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
