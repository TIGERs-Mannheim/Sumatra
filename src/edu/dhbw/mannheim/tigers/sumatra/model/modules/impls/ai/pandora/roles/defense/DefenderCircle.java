/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.10.2014
 * Author(s): christian
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.PositionSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * TODO christian, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author christian
 */
public class DefenderCircle extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * TODO christian, add comment!
	 * - What should this type do (in one sentence)?
	 * - If not intuitive: A simple example how to use this class
	 * 
	 * @author christian
	 */
	public enum EDefendBehavior
	{
		/**  */
		NORMAL,
		/**  */
		// LOOK_AT_BALL,
		/**  */
		// DO_COMPLETE;
	}
	
	private enum EStateId
	{
		MOVING;
	}
	
	@SuppressWarnings("unused")
	private enum EEvent
	{
		DONE,
		DEST_UPDATE
	}
	
	
	// private final EDefendBehavior behavior;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public DefenderCircle()
	{
		super(ERole.DEFENDER_CIRCLE);
		
		
		setInitialState(new DefenderCircleState());
		
		
	}
	
	/**
	 * Drive from top left corner to bottom left corner around the penalty area
	 * 
	 * @author christian
	 */
	public class DefenderCircleState implements IRoleState
	{
		private PositionSkill		pskill;
		private IVector2				finalDest;
		private IVector2				nextDest;
		private static final float	MIN_DIST_PENALTY	= 200;
		private static final float	MIN_DEST_DIST		= 400;
		private IVector2				posMid;
		private IVector2				negMid;
		
		
		/**
		  * 
		  */
		public DefenderCircleState()
		{
			// Left ist unsere Seite
			finalDest = AIConfig.getGeometry().getField().topLeft();
			
			posMid = getMidPoint(AIConfig.getGeometry().getField().bottomLeft(), AIConfig.getGeometry().getField()
					.bottomRight());
			negMid = getMidPoint(AIConfig.getGeometry().getField().topLeft(), AIConfig.getGeometry().getField()
					.topRight());
			
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
			
			
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		private void updateDestination()
		{
			if (GeoMath.distancePP(finalDest, getPos()) < MIN_DEST_DIST)
			{
				if (getPos().y() > 0)
				{
					finalDest = new Vector2f(AIConfig.getGeometry().getField().bottomLeft().x() + 100, AIConfig
							.getGeometry()
							.getField().bottomLeft().y());
					System.out.println("Changed finalDest");
				} else
				{
					finalDest = new Vector2f(AIConfig.getGeometry().getField().topLeft().x() + 100, AIConfig.getGeometry()
							.getField()
							.topLeft().y());
					System.out.println("Changed finalDest");
				}
			}
			
			double pos_distance = GeoMath.distancePL(AIConfig.getGeometry().getPenaltyAreaOur().getPenaltyCirclePos()
					.nearestPointOutside(getPos()),
					getPos(), finalDest);
			double neg_distance = GeoMath.distancePL(AIConfig.getGeometry().getPenaltyAreaOur().getPenaltyCircleNeg()
					.nearestPointOutside(getPos()),
					getPos(), finalDest);
			
			double distance = Math.min(pos_distance, neg_distance);
			
			if (distance < MIN_DIST_PENALTY)
			{
				try
				{
					if (getPos().x() > AIConfig.getGeometry().getPenaltyAreaOur().getPenaltyAreaFrontLine()
							.getXValue(getPos().y()))
					
					{
						nextDest = new Vector2f(getPos().x() + MIN_DIST_PENALTY, finalDest.y());
					}
					else
					{
						if (finalDest.y() < 0)
						{
							nextDest = posMid;
						}
						else
						{
							nextDest = negMid;
						}
					}
				} catch (MathException err)
				{
					err.printStackTrace();
				}
			} else
			{
				nextDest = finalDest;
			}
			pskill.setDestination(nextDest);
		}
		
		
		// @SuppressWarnings("unused")
		// private Vector2f getMidPoint(final IVector2 dest)
		// {
		// return getMidPoint(dest, getPos());
		// }
		
		
		private Vector2f getMidPoint(final IVector2 dest, final IVector2 start
				)
		{
			float x = (float) (start.x() + (0.5 * (dest.x() - start.x())));
			float y = (float) (start.y() + (0.5 * (dest.y() - start.y())));
			return new Vector2f(x, y);
			
		}
		
		
		@Override
		public void doEntryActions()
		{
			pskill = new PositionSkill(new Vector2f(), 0);
			updateDestination();
			setNewSkill(pskill);
			
			
		}
		
		
		@Override
		public void doExitActions()
		{
			
		}
		
		
		@Override
		public void doUpdate()
		{
			updateDestination();
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MOVING;
		}
		
		
		// @SuppressWarnings("unused")
		// private double getPanaltyDistanceOur()
		// {
		//
		// IVector2 meV2 = getPos();
		// IVector2 dest;
		// if (getPos().y() < 0)
		// {
		// dest = AIConfig.getGeometry().getPenaltyAreaOur().getPenaltyCircleNeg().nearestPointOnCircle(meV2);
		// } else
		// {
		// dest = AIConfig.getGeometry().getPenaltyAreaOur().getPenaltyCirclePos().nearestPointOnCircle(meV2);
		// }
		// return getPointDistance(dest);
		// }
		
		
		// private double getPointDistance(final IVector2 dest)
		// {
		// IVector2 myPos = getPos();
		// double myX = myPos.x();
		// double myY = myPos.y();
		// double destX = dest.x();
		// double destY = dest.y();
		// double distX = myX - destX;
		// double distY = myY - destY;
		// double squareX = Math.pow(distX, 2);
		// double squareY = Math.pow(distY, 2);
		// return Math.sqrt(squareX + squareY);
		// }
		
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
