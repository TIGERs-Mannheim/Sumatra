/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 29, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Test role for testing fast changing destinations
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class DestChangedTestRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IVector2	diffDest;
	private final IVector2	diffLookAt;
	private final int			freq;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param diffDest
	 * @param diffLookAt
	 * @param freq
	 */
	public DestChangedTestRole(IVector2 diffDest, IVector2 diffLookAt, int freq)
	{
		super(ERole.DEST_CHANGED);
		this.diffDest = diffDest;
		this.diffLookAt = diffLookAt;
		this.freq = freq;
		setInitialState(new MainState());
	}
	
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		MAIN
	}
	
	
	private class MainState implements IRoleState
	{
		private MoveToSkill	skill	= null;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new MoveAndStaySkill();
			skill.getMoveCon().getDestCon().updateDestination(getPos());
			skill.getMoveCon().updateLookAtTarget(Vector2.ZERO_VECTOR);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getWFrame().getId().getFrameNumber() % freq) == 0)
			{
				IVector2 random = new Vector2((Math.random() * diffDest.x()), (Math.random() * diffDest.y()));
				skill.getMoveCon().updateDestination(random);
				IVector2 lookAt = new Vector2(-200 + (Math.random() * diffLookAt.x()), -1500
						+ (Math.random() * diffLookAt.y()));
				skill.getMoveCon().updateLookAtTarget(lookAt);
			}
			getAiFrame().addDebugShape(
					new DrawableCircle(new Circle(skill.getMoveCon().getDestCon().getDestination(), 80), Color.white));
			getAiFrame().addDebugShape(
					new DrawableCircle(new Circle(skill.getMoveCon().getAngleCon().getTarget(), 20), Color.red));
			getAiFrame().addDebugShape(
					new DrawableLine(Line.newLine(skill.getMoveCon().getAngleCon().getTarget(), getPos())));
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ISkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ISkill skill, BotID botID)
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MAIN;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fillNeededFeatures(List<EFeature> features)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
