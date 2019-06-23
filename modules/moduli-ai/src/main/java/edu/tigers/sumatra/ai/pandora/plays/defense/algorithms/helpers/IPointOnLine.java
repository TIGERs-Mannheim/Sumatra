/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;


/**
 * Interface for classes which provide a method to get a defense point on a shoot vector of an enemy bot.
 */
public interface IPointOnLine
{
	/**
	 * Returns a new defense point on the shoot vector of the enemy bot covered by the given def point.
	 * 
	 * @param defPoint
	 * @param frame
	 * @param curDefender
	 * @return
	 */
	public DefensePoint getPointOnLine(DefensePoint defPoint, final MetisAiFrame frame, DefenderRole curDefender);
}
