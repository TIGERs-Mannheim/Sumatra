/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 3, 2015
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.learning.lcase;

import java.util.List;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public interface ILearningCase
{
	/**
	 * @param frame
	 * @return
	 */
	public boolean isFinished(AthenaAiFrame frame);
	
	
	/**
	 * @param roles
	 * @param aiFrame
	 */
	public void update(List<ARole> roles, AthenaAiFrame aiFrame);
	
	
	/**
	 * @param frame
	 * @param roles
	 * @return
	 */
	public boolean isReady(AthenaAiFrame frame, List<ARole> roles);
	
	
	/**
	 * @param frame
	 * @return
	 */
	public boolean isActive(AthenaAiFrame frame);
	
	
	/**
	 * @return
	 */
	public String getReadyCriteria();
	
}
