/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.08.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.referee;

import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;


/**
 * @author Gero
 */
public interface IRefereeObserver
{
	/**
	 * @param refMsg
	 */
	default void onNewRefereeMsg(final SSL_Referee refMsg)
	{
	}
	
	
	/**
	 * A new referee message source was selected.
	 * 
	 * @param src
	 */
	default void onRefereeMsgSourceChanged(final ARefereeMessageSource src)
	{
	}
}
