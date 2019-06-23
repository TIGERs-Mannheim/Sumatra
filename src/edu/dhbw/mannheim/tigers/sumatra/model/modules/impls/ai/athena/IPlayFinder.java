/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s):
 * Oliver Steinbrecher
 * Daniel Waigand
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;


/**
 * This interface encapsulates classes that are - more or less - capable of choosing a appropriate play for the current
 * situation
 * 
 * @author Oliver Steinbrecher, Daniel Waigand, Gero
 * 
 */
public interface IPlayFinder
{
	/**
	 * Choose {@link APlay}s according to the current situation
	 * 
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of currentFrame
	 * @param currentFrame
	 * @param previousFrame (never null, Athena cares for this by dropping the very first frame)
	 */
	public void choosePlays(List<APlay> plays, AIInfoFrame currentFrame, AIInfoFrame previousFrame);
}
