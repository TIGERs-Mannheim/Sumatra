/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.kick.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.Setter;


/**
 * Sample for redirect model identification.
 * Velocities are measured relative to robot orientation. 0rad is in forward axis of robot.
 */
@Getter
public class RedirectModelSample
{
	/**
	 * Incoming ball velocity in [m/s].
	 */
	private final double inVelocityX;
	private final double inVelocityY;

	/**
	 * Outgoing ball velocity in [m/s].
	 */
	private final double outVelocityX;
	private final double outVelocityY;

	/**
	 * Energy transfer from incoming to outgoing spin.
	 */
	private final double spinFactor;

	/**
	 * Commanded kick speed for this redirect in [m/s].
	 */
	private final double kickSpeed;

	@Setter
	private boolean sampleUsed = true;


	/**
	 * Required for jackson binding.
	 */
	protected RedirectModelSample()
	{
		inVelocityX = 0;
		inVelocityY = 0;
		outVelocityX = 0;
		outVelocityY = 0;
		spinFactor = 0;
		kickSpeed = 0;
	}


	public RedirectModelSample(IVector2 inVelocity, IVector2 outVelocity, double spinFactor, double kickSpeed)
	{
		inVelocityX = inVelocity.x();
		inVelocityY = inVelocity.y();
		outVelocityX = outVelocity.x();
		outVelocityY = outVelocity.y();
		this.spinFactor = spinFactor;
		this.kickSpeed = kickSpeed;
	}


	@JsonIgnore
	public IVector2 getInVelocity()
	{
		return Vector2.fromXY(inVelocityX, inVelocityY);
	}


	@JsonIgnore
	public IVector2 getOutVelocity()
	{
		return Vector2.fromXY(outVelocityX, outVelocityY);
	}


	/**
	 * Get a factor describing remaining ball velocity perpendicular to a robot front after impact.
	 *
	 * @return
	 */
	@JsonIgnore
	public double getRedirectRestitutionCoefficient()
	{
		IVector2 kick = Vector2.fromX(kickSpeed);
		IVector2 redirDiff = getOutVelocity().subtractNew(kick);

		return Math.abs(redirDiff.x() / inVelocityX);
	}


	/**
	 * Get a factor describing remaining ball velocity parallel to a robot front after impact.
	 * This occurs due to an energy transfer from sliding surfaces (ball on kicker) to ball rotation.
	 *
	 * @return
	 */
	@JsonIgnore
	public double getVerticalSpinFactor()
	{
		IVector2 kick = Vector2.fromX(kickSpeed);
		IVector2 redirDiff = getOutVelocity().subtractNew(kick);

		return Math.abs(redirDiff.y() / inVelocityY);
	}
}
