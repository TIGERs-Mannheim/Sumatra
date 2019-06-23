/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.05.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * This skill directly applies given velocities to the bot. no control here.
 * so you need to supervise where the bot goes.
 * 
 * @author DanielW
 * 
 */
public class DirectMove extends ASkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final IVector2	vel;
	private final float		v;
	private final float		w;
	private final int			runTime;
	private final long		startTime;
	private final boolean	accelerate;
	
	
	// CSVExporter exporter = CSVExporter.getInstance("direct");
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * directly apply specified velocies to the bot. after completition of this skill the velocity is not reset
	 * @param vel the velocity to apply
	 * @param v
	 * @param w
	 * @param runtime ms
	 * @param accelerate true: use accelerate function; false: step
	 */
	public DirectMove(IVector2 vel, float v, float w, int runtime, boolean accelerate)
	{
		super(ESkillName.DIRECT_MOVE, ESkillGroup.MOVE);
		this.vel = vel;
		this.v = v;
		this.w = w;
		this.runTime = runtime;
		this.startTime = System.nanoTime();
		this.accelerate = accelerate;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		

		// IVector2 actVel = getBot().vel.turnNew(AIMath.PI_HALF - getBot().angle);
		// IVector2 actPos = getBot().pos;
		// Header("time", "setx", "sety", "setv", "actx","acty","actv");
		// if (!exporter.isClosed())
		// {
		// exporter.addValues(System.nanoTime(), vel.x(), vel.y(), v, actVel.x(), actVel.y(), getBot().aVel);
		// }
		if (accelerate)
		{
			IVector2 nextVelocity = AMoveSkillV2.accelerateComplex(vel, vel.getLength2(), getBot());
			
			cmds.add(new TigerMotorMoveV2(nextVelocity, w, v));
			

		} else
		{
			cmds.add(new TigerMotorMoveV2(vel, w, v));
		}
		

		if ((System.nanoTime() - startTime) / 1e6 >= runTime)
		{
			// previously set velocities are kept!
			complete();
		}
		
		return cmds;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		DirectMove dmove = (DirectMove) newSkill;
		return this.vel.equals(dmove.vel) && AIMath.isZero(this.v - dmove.v) && AIMath.isZero(this.w - dmove.w)
				&& this.runTime == dmove.runTime;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
