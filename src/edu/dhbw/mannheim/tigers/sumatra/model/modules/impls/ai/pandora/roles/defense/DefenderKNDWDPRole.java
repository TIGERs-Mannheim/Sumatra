/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.11.2012
 * Author(s): Philipp
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndBlockV2Skill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndBlockV2Skill.EBlockModus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * DefenseRole for {@linkg DefensePoints}. Posionited a bot at a specififc position and look at a target (normally ball
 * position)
 * 
 * @author PhilippP
 * 
 */
public class DefenderKNDWDPRole extends ADefenseRole
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --- analyzing specifications ---
	/** Point to protect against **/
	private DefensePoint														defPoint		= new DefensePoint(-(AIConfig.getGeometry()
																											.getFieldLength() / 2)
																											+ AIConfig.getGeometry()
																													.getPenaltyAreaOur()
																													.getRadiusOfPenaltyArea()
																											+ AIConfig.getGeometry()
																													.getBotRadius(), 0);
	
	private float																ycoord		= 0;
	/** Intercection where the goalline an the ball vector cut */
	private IVector2															intersectPoint;
	private final Goal														goal			= AIConfig.getGeometry().getGoalOur();
	/** */
	public static final Comparator<? super DefenderKNDWDPRole>	YCOMPARATOR	= new YComparator();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public DefenderKNDWDPRole()
	{
		super(ERole.DEFENDER_KNDWDP, false, true);
		setInitialState(new NormalDefendState());
		addTransition(EStateId.NORMAL, EEvent.INTERSECTION, new FastBlockState());
		addTransition(EStateId.FAST, EEvent.INTERSECTION_DONE, new NormalDefendState());
		addEndTransition(EStateId.NORMAL, EEvent.DONE);
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private enum EStateId
	{
		NORMAL,
		FAST
	}
	
	private enum EEvent
	{
		INTERSECTION,
		INTERSECTION_DONE,
		DONE
	}
	
	
	// --------------------------------------------------------------------------
	
	@Override
	public void updateMoveCon(AIInfoFrame currentFrame)
	{
		// nothing to do
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set the Defender Position
	 * @param defPoint - Position to Defend
	 */
	public void setDefPoint(DefensePoint defPoint)
	{
		this.defPoint = defPoint;
	}
	
	
	/**
	 * @return
	 */
	private float getY()
	{
		return ycoord;
	}
	
	
	/**
	 * Returns the acutal defensPoint
	 * 
	 * @return DefensPoints
	 * 
	 */
	public IVector2 getDefPoint()
	{
		
		return defPoint;
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
	
	
	/**
	 * Set the intersection point and the flag to change the State of the role to FastBlock Modus
	 * @param intersectPoint
	 * 
	 * @param intersection
	 */
	public void setIntersectionPoint(IVector2 intersectPoint, boolean intersection)
	{
		this.intersectPoint = intersectPoint;
		if (intersection == true)
		{
			nextState(EEvent.INTERSECTION);
		}
		
	}
	
	// --------------------------------------------------------------------------
	// --- InnerClasses --------------------------------------------------------
	// --------------------------------------------------------------------------
	private static class YComparator implements Comparator<DefenderKNDWDPRole>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= 1794858044291002364L;
		
		
		@Override
		public int compare(DefenderKNDWDPRole v1, DefenderKNDWDPRole v2)
		{
			if (v1.getY() > v2.getY())
			{
				return 1;
			} else if (v1.getY() < v2.getY())
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
	
	
	private class FastBlockState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndBlockV2Skill(EBlockModus.DEFENDER));
		}
		
		
		@Override
		public void doUpdate()
		{
			// Check if ther still is a intersection
			// Switch state if intersect is not in goal and keeper is away from ball
			if (((intersectPoint == null) || (intersectPoint.y() < goal.getGoalPostRight().y())
					|| (intersectPoint.y() > goal.getGoalPostLeft().y()) || (getAiFrame().worldFrame.ball.getVel().x() > 0))
					|| getAiFrame().worldFrame.getBall().getVel().equals(Vector2.ZERO_VECTOR, 0.1f))
			{
				nextState(EEvent.INTERSECTION_DONE);
			}
			
			nextState(EEvent.INTERSECTION_DONE);
			
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			nextState(EEvent.INTERSECTION_DONE);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.FAST;
		}
	}
	
	private class NormalDefendState implements IRoleState
	{
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
			
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			
		}
		
		
		@Override
		public void doEntryActions()
		{
			getMoveCon().setBotsObstacle(false);
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (defPoint != null)
			{
				updateDestination(defPoint);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.NORMAL;
		}
	}
	
	
}
