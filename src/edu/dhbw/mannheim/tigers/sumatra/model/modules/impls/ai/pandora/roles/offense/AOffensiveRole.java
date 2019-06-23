/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.awt.Color;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensiveConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class AOffensiveRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	protected static final Logger	log		= Logger.getLogger(AOffensiveRole.class.getName());
	protected int						animator	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public AOffensiveRole()
	{
		super(ERole.OFFENSIVE);
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
		features.add(EFeature.MOVE);
		features.add(EFeature.BARRIER);
	}
	
	
	/**
	 * This method is called before the state machine update.
	 * Use this for role-global actions. <br>
	 */
	@Override
	protected void beforeUpdate()
	{
		animator++;
		changeStateIfNecessary();
	}
	
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * 
	 */
	public void changeStateIfNecessary()
	{
		if (getAiFrame().getTacticalField() != null)
		{
			if (getAiFrame().getTacticalField().getOffensiveStrategy() != null)
			{
				if (getAiFrame().getTacticalField().getOffensiveStrategy().getCurrentOffensivePlayConfiguration() != null)
				{
					if (getAiFrame().getTacticalField().getOffensiveStrategy()
							.getCurrentOffensivePlayConfiguration().containsKey(getBotID()))
					{
						if (getCurrentState() != getAiFrame().getTacticalField().getOffensiveStrategy()
								.getCurrentOffensivePlayConfiguration().get(getBotID()))
						{
							triggerEvent(getAiFrame().getTacticalField().getOffensiveStrategy()
									.getCurrentOffensivePlayConfiguration().get(getBotID()));
						}
					}
				}
			}
		}
	}
	
	
	protected void printDebugInformation(final String text)
	{
		if (OffensiveConstants.isShowDebugInformations())
		{
			log.debug(text);
		}
	}
	
	
	protected void visualizeTarget(final IVector2 target)
	{
		Circle targetCircle = new Circle(target, 150 + (int) (Math.sin(animator / 5) * 100));
		Color bigTargetColor = new Color(50, 10, 125, 60);
		DrawableCircle dtargetCircle = new DrawableCircle(targetCircle, bigTargetColor);
		dtargetCircle.setFill(true);
		
		Circle targetCircle2 = new Circle(target, 50 + (int) (Math.sin(animator / 5) * 30));
		DrawableCircle dtargetCircle2 = new DrawableCircle(targetCircle2, new Color(10, 10, 200, 60));
		dtargetCircle2.setFill(true);
		DrawableCircle dtargetCircle3 = new DrawableCircle(targetCircle, bigTargetColor);
		
		getAiFrame().getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(dtargetCircle);
		getAiFrame().getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(dtargetCircle2);
		getAiFrame().getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(dtargetCircle3);
	}
}
