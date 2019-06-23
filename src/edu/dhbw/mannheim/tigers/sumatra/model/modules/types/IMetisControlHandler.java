/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.05.2012
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.MetisCalculators;


/**
 * This interface is used by the gui to control the ai-submodule
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis}.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public interface IMetisControlHandler
{
	
	/**
	 * Sets the xml-configuration class for {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.Metis}.
	 * @param config
	 */
	void setConfiguration(MetisCalculators config);
	
	
	/**
	 * Load analyzing results from an other game.
	 */
	void loadAnalyzingResults();
	
	
	/**
	 * Store analyzing results from the actual game.
	 */
	void persistAnalyzingResults();
}
