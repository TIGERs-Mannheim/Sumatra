/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 13, 2014
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.AutoReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg;


/**
 * Interface which defines the contract for an {@link AutoReferee} referee case.
 * Each case is supposed to monitor the game for a certain rule violation or other
 * event which is relevant for a referee.
 * All <code>IRefereeCase</code>s are registered with the <code>AutoReferee</code>.
 * When the game is running the <code>AutoReferee</code> periodically forwards all {@link AIInfoFrame}s to the cases who
 * in turn can return a {@link RefereeCaseMsg} if they detected something.
 * 
 * @author Lukas Magel
 */
public interface IRefereeCase
{
	/**
	 * Called by the {@link AutoReferee} when a new {@link AIInfoFrame} has arrived.
	 * If the case detects a rule violation it is supposed to return
	 * a {@link RefereeCaseMsg} in turn.
	 * 
	 * @param frame
	 * @return an instance of <code>RefereeCaseMsg</code> or null if nothing was detected
	 */
	List<RefereeCaseMsg> process(MetisAiFrame frame);
	
	
	/**
	 * Is called whenever the AutoReferee is restarted.
	 */
	void reset();
}
