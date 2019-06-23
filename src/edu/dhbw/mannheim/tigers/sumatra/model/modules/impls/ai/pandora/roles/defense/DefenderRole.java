/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.11.2012
 * Author(s): Philipp
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * DefenseRole for {@linkg DefensePoints}. Posionited a bot at a specififc position and look at a target (normally ball
 * position)
 * 
 * @author PhilippP
 */
public class DefenderRole extends ARole
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --- analyzing specifications ---
	/** Point to protect against **/
	private DefensePoint												defPoint				= new DefensePoint(
																											-(AIConfig.getGeometry()
																													.getFieldLength() / 2)
																													+ AIConfig
																															.getGeometry()
																															.getPenaltyAreaOur()
																															.getRadiusOfPenaltyArea()
																													+ AIConfig.getGeometry()
																															.getBotRadius(), 0);
	
	/**  */
	public static final Comparator<? super DefenderRole>	Y_COMPARATOR		= new YComparator();
	
	
	@Configurable
	private static float												marginDefenseArea	= 300;
	@Configurable
	private static float												chipKickLength		= 1000;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public DefenderRole()
	{
		super(ERole.DEFENDER);
		setInitialState(new NormalDefendState());
		addEndTransition(EStateId.NORMAL, EEvent.DONE);
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private enum EStateId
	{
		NORMAL,
	}
	
	private enum EEvent
	{
		DONE,
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set the Defender Position
	 * 
	 * @param defPoint - Position to Defend
	 */
	public void setDefPoint(final DefensePoint defPoint)
	{
		this.defPoint = defPoint;
	}
	
	
	/**
	 * Returns the acutal defensPoint
	 * 
	 * @return DefensPoints
	 */
	public IVector2 getDefPoint()
	{
		
		return defPoint;
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.MOVE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- InnerClasses --------------------------------------------------------
	// --------------------------------------------------------------------------
	private static class YComparator implements Comparator<DefenderRole>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= 1794858044291002364L;
		
		
		@Override
		public int compare(final DefenderRole v1, final DefenderRole v2)
		{
			if (v1.getPos().y() > v2.getPos().y())
			{
				return 1;
			} else if (v1.getPos().y() < v2.getPos().y())
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
	
	
	private class NormalDefendState implements IRoleState
	{
		private MoveAndStaySkill	skill	= null;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		@Override
		public void doEntryActions()
		{
			skill = new MoveAndStaySkill();
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.getMoveCon().setBotsObstacle(false);
			
			if (defPoint != null)
			{
				skill.getMoveCon().updateDestination(defPoint);
				if (defPoint.getProtectAgainst() != null)
				{
					skill.getMoveCon().updateLookAtTarget(defPoint.getProtectAgainst());
				} else
				{
					if (!(getWFrame().ball.getPos().x() < (getPos().x() + AIConfig.getGeometry().getBotRadius())))
					{
						skill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
					} else
					{
						skill.getMoveCon().updateLookAtTarget(new Vector2(0, 0));
					}
				}
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.NORMAL;
		}
	}
	
}
