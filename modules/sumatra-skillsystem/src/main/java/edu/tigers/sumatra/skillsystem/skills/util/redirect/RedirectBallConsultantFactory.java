/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util.redirect;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author chris
 */
public class RedirectBallConsultantFactory
{
	private static final Logger log = Logger.getLogger(RedirectBallConsultantFactory.class.getName());
	static
	{
		ConfigRegistration.registerClass("skills", RedirectBallConsultantFactory.class);
	}
	
	private IVector2		ballVelAtCollision;
	private double			ballRedirectAngle;
	private double			desiredVelocity;
	@Configurable(comment = "switch between different consultant implementation")
	private Consultant	consultant	= Consultant.PHYSICS_BASED;
	
	
	/**
	 * Default Constructor
	 */
	public RedirectBallConsultantFactory()
	{
		ballRedirectAngle = Double.MAX_VALUE;
		desiredVelocity = Double.MAX_VALUE;
		ballVelAtCollision = null;
	}
	
	
	/**
	 * Creates an RedirectBallConsultant if all necessary fields are set
	 * 
	 * @return the redirectBallConsultant or null if some fields missing
	 */
	public ARedirectBallConsultant create()
	{
		if (allFieldsSet())
		{
			switch (consultant)
			{
				case PHYSICS_BASED:
					return new RedirectBallConsultant(ballVelAtCollision, ballRedirectAngle, desiredVelocity);
				
			}
		}
		log.warn("Tried to create a RedirectBallConsultant with missing fields returned null");
		return null;
	}
	
	
	@SuppressWarnings("squid:S1244") // Ignore comparing doubles. This should only check if fields are set or not
	private boolean allFieldsSet()
	{
		return ballVelAtCollision != null && Double.MAX_VALUE != ballRedirectAngle && desiredVelocity != Double.MAX_VALUE;
	}
	
	
	public RedirectBallConsultantFactory setBallVelAtCollision(final IVector2 ballVelAtCollision)
	{
		this.ballVelAtCollision = ballVelAtCollision;
		return this;
	}
	
	
	public RedirectBallConsultantFactory setBallRedirectAngle(final double ballRedirectAngle)
	{
		this.ballRedirectAngle = ballRedirectAngle;
		return this;
	}
	
	
	public RedirectBallConsultantFactory setDesiredVelocity(final double desiredVelocity)
	{
		this.desiredVelocity = desiredVelocity;
		return this;
	}
	
	private enum Consultant
	{
		PHYSICS_BASED
	}
	
}
