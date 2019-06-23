/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 20, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;


/**
 * Observer for actions from GUI
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public interface IFeatureChangedObserver
{
	/**
	 * Feature has changed to state
	 * 
	 * @param feature
	 * @param state
	 */
	void onFeatureChanged(EFeature feature, EFeatureState state);
	
	
	/**
	 * Apply all features to all bots
	 * 
	 * @param features
	 */
	void onApplyFeaturesToAll(Map<EFeature, EFeatureState> features);
}
