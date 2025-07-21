/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * Encode the contact (time/duration) to the ball.
 */
@Value
@AllArgsConstructor
public class BallContact
{
	long current;
	long start;
	long end;
	long visionStart;
	long visionEnd;

	@Configurable(
			comment = "Time horizon [s] that is used to check if the Bot had ball contact",
			defValue = "0.2"
	)
	private static double recentContactHorizon = 0.2;

	static
	{
		ConfigRegistration.registerClass("wp", BallContact.class);
	}

	@SuppressWarnings("unused")
	public BallContact()
	{
		current = 0;
		start = 0;
		end = 0;
		visionStart = 0;
		visionEnd = 0;
	}


	public static BallContact def(long timestamp)
	{
		return new BallContact(timestamp, (long) -1e9, (long) -1e9, (long) -1e9, (long) -1e9);
	}


	public boolean hasContact()
	{
		return current == end;
	}


	public boolean hasNoContact()
	{
		return !hasContact();
	}


	public boolean hasContactFromVision()
	{
		return current == visionEnd;
	}


	public boolean hasNoContactFromVision()
	{
		return !hasContactFromVision();
	}


	public boolean hasContactFromVisionOrBarrier()
	{
		return hasContact() || hasContactFromVision();
	}


	public double getContactDuration()
	{
		if (hasNoContact())
		{
			return 0;
		}
		return (end - start) * 1e-9;
	}


	public double getContactDurationFromVision()
	{
		if (hasNoContactFromVision())
		{
			return 0;
		}
		return (visionEnd - visionStart) * 1e-9;
	}


	/**
	 * @param horizon the time horizon in seconds
	 * @return true, if the ball had ball contact within given horizon
	 */
	public boolean hadContact(double horizon)
	{
		return (current - end) * 1e-9 < horizon;
	}


	/**
	 * @return true, if the ball had contact within the last 0.2 seconds
	 */
	public boolean hadRecentContact()
	{
		return hadContact(recentContactHorizon);
	}


	public boolean hadContactFromVision(double horizon)
	{
		return (current - visionEnd) * 1e-9 < horizon;
	}


	/**
	 * @return true, if the ball had contact within the last 0.2 seconds
	 */
	public boolean hadRecentContactFromVision()
	{
		return hadContactFromVision(recentContactHorizon);
	}

}
