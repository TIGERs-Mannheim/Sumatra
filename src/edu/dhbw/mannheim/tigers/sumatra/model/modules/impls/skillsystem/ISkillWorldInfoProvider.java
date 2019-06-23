/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;

/**
 * Used for a passive update of active skills
 * 
 * @author Gero
 * 
 */
public interface ISkillWorldInfoProvider
{
	/**
	 * @return	The newest {@link WorldFrame} available. May be null at the beginning!
	 */
	public WorldFrame getCurrentWorldFrame();
	
	
	/**
	 * @return An instance of {@link Sisyphus}
	 */
	public Sisyphus getSisyphus();
}
