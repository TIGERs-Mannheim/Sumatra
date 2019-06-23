/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 20, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * The Support role tries to break clear and to redirect a ball.
 * It also tries to block opponents.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author Simon Sander <Simon.Sander@dlr.de>
 */
public class SupportRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// private BotID id = null;
	
	// private static final Logger log = Logger.getLogger(SupportRole.class.getName());
	
	private IVector2		support_position	= new Vector2(1000, 0);
	private IVector2		support_target		= AIConfig.getGeometry().getGoalTheir().getGoalCenter();
	
	private float			targetOrientation	= 0;
	
	@Configurable
	private static float	shootSpeed			= 4.0f;
	
	private enum EStateId
	{
		POSITIONING
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public SupportRole()
	{
		super(ERole.SUPPORT);
		
		IRoleState positioner = new PositioningState();
		
		setInitialState(positioner);
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
		features.add(EFeature.BARRIER);
	}
	
	private class PositioningState implements IRoleState
	{
		private MoveToSkill	move_skill	= null;
		
		
		@Override
		public void doEntryActions()
		{
			move_skill = new MoveAndStaySkill();
			setNewSkill(move_skill);
			targetOrientation = getBot().getAngle();
			move_skill.getMoveCon().setArmKicker(true);
			support_position = getPos();
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			getSupportPositionCalc();
			
			paintPosition();
			
			IVector3 poss = AiMath.calcRedirectPositions(support_position, targetOrientation, getWFrame().getBall(),
					support_target, shootSpeed);
			targetOrientation = poss.z();
			
			move_skill.getMoveCon().updateDestination(poss.getXYVector());
			move_skill.getMoveCon().updateTargetAngle(targetOrientation);
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.POSITIONING;
		}
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private void paintPosition()
	{
		BotID id = getBotID();
		
		getAiFrame().addDebugShape(new DrawableCircle(new Circle(support_position, 150), Color.cyan));
		getAiFrame().addDebugShape(new DrawableCircle(new Circle(support_position, 100), Color.cyan));
		
		DrawablePoint position_point_text = new DrawablePoint(support_position, Color.black);
		position_point_text.setText("Postion: Support id:" + id.getNumber());
		getAiFrame().addDebugShape(position_point_text);
		
		getAiFrame().addDebugShape(new DrawableCircle(new Circle(support_target, 150), Color.orange));
		getAiFrame().addDebugShape(new DrawableCircle(new Circle(support_target, 100), Color.orange));
		
		DrawablePoint target_point_text = new DrawablePoint(support_target, Color.black);
		target_point_text.setText("Target: Support id:" + id.getNumber());
		getAiFrame().addDebugShape(target_point_text);
		
		
	}
	
	
	/**
	 * This method is used to get the SupportPosition,
	 * which is calculated at Metis.
	 * 
	 * @author Simon Sander <Simon.Sander@dlr.de>
	 */
	
	private void getSupportPositionCalc()
	{
		BotID id = getBotID();
		
		// IVector2 position = getAiFrame().getTacticalField().getSupportPositions().get(id);
		IVector2 position = getAiFrame().getTacticalField().getSupportPositions().get(id);
		if ((position != null) && ((getWFrame().getBall().getVel().getLength2() < 0.1f) || (support_position == null)))
		{
			support_position = position;
		}
		
		IVector2 target = getAiFrame().getTacticalField().getSupportTargets().get(id);
		if (target != null)
		{
			support_target = target;
		}
	}
}
