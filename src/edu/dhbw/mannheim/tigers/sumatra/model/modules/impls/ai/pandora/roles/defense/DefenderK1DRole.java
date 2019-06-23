/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperPlus1DefenderPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;


/**
 * <a href="http://www.gockel-09.de/98=99=5.jpg">Defender</a>
 * Role for the {@link KeeperPlus1DefenderPlay}.
 * 
 * @see KeeperK1DRole
 * @author Malte
 * 
 */
public class DefenderK1DRole extends AK1DRole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -2684416234784760936L;
	
	
	// -------------F-------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public DefenderK1DRole()
	{
		super(ERole.DEFENDER_K1D);
		// Adjust these parameters as good as possible!
		/**  */
		gap = AIConfig.getRoles().getDefenderK1D().getGap();
		/**  */
		radius = AIConfig.getRoles().getDefenderK1D().getRadius();
		
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

}
