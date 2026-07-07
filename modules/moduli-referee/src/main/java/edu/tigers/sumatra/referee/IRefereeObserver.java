package edu.tigers.sumatra.referee;

import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;


/**
 * @author Gero
 */
public interface IRefereeObserver
{
	/**
	 * @param refMsg
	 */
	default void onNewRefereeMsg(final SslGcRefereeMessage.Referee refMsg)
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
