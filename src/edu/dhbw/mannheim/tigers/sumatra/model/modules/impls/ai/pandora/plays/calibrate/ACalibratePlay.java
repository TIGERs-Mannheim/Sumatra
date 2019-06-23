/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.calibrate;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.csvexporter.CSVExporter;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveBallToRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;


/**
 * Base class for plays that want to calibate sth
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public abstract class ACalibratePlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IVector2		ballShootStartPos;
	private BallGetterRole		ballGetterRole;
	private MoveBallToRole		ballPreparerRole;
	
	private CSVExporter			csvExporter;
	
	private final List<Float>	durations					= new ArrayList<Float>();
	private static final float	START_X_FROM_GOAL_LINE	= 200f;
	private int						distancesCtr				= 0;
	private EState					state							= EState.PREPARE_BALL;
	
	enum EState
	{
		PREPARE_BALL,
		GET,
		DO;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public ACalibratePlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		setTimeout(Long.MAX_VALUE);
		
		IVector2 goalCenter = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		IVector2 goalCenterToBallPos = new Vector2(START_X_FROM_GOAL_LINE,
				(AIConfig.getGeometry().getFieldWidth() / 2 / 5) * 4);
		ballShootStartPos = goalCenter.addNew(goalCenterToBallPos);
		
		fillDurations();
		prepareNext(aiFrame);
		addAggressiveRole(ballPreparerRole, aiFrame.worldFrame.ball.getPos());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private void prepareNext(AIInfoFrame aiFrame)
	{
		ballPreparerRole = new MoveBallToRole(ballShootStartPos);
		ballPreparerRole.setPenaltyAreaAllowed(true);
		
		if (distancesCtr >= durations.size())
		{
			changeToFinished();
			return;
		}
		
		IVector2 target = ballShootStartPos.addNew(new Vector2(AIConfig.getGeometry().getFieldLength(), 0));
		
		ballGetterRole = new BallGetterRole(target, EBallContact.DISTANCE);
		ballGetterRole.setPenaltyAreaAllowed(true);
		
		state = EState.PREPARE_BALL;
		doPrepareNext(target, distancesCtr);
		distancesCtr++;
	}
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
	}
	
	
	@Override
	protected final void afterUpdate(AIInfoFrame currentFrame)
	{
		switch (state)
		{
			case PREPARE_BALL:
				if (ballPreparerRole.isCompleted())
				{
					switchRoles(ballPreparerRole, ballGetterRole, currentFrame);
					state = EState.GET;
				}
				break;
			case GET:
				if (ballGetterRole.isCompleted())
				{
					switchRoles(ballGetterRole, getDoerRole(), currentFrame);
					state = EState.DO;
					CSVExporter.createInstance(getIdentifier(), getIdentifier(), true);
					csvExporter = CSVExporter.getInstance(getIdentifier());
					csvExporter.setAdditionalInfo("Duration: " + getDurations().get(distancesCtr - 1));
					csvExporter.setHeader("px", "py", "pz", "vx", "vy", "vz", "vel3", "vel2");
				}
				break;
			case DO:
				IVector3 ballPos3 = currentFrame.worldFrame.ball.getPos3();
				IVector2 ballVel2 = currentFrame.worldFrame.ball.getVel();
				IVector3 ballVel3 = currentFrame.worldFrame.ball.getVel3();
				csvExporter.addValues(ballPos3.x(), ballPos3.y(), ballPos3.z(), ballVel3.x(), ballVel3.y(), ballVel3.z(),
						ballVel3.getLength3(), ballVel2.getLength2());
				if (doAfterUpdate(currentFrame))
				{
					ARole oldDoerRole = getDoerRole();
					prepareNext(currentFrame);
					switchRoles(oldDoerRole, ballPreparerRole, currentFrame);
					csvExporter.close();
				}
				break;
		}
		
	}
	
	
	/**
	 * Do your after update stuff.
	 * You have the ball in front of you!
	 * 
	 * @param currentFrame
	 * @return if you are done
	 */
	protected abstract boolean doAfterUpdate(AIInfoFrame currentFrame);
	
	
	/**
	 * Put durations into list
	 */
	protected abstract void fillDurations();
	
	
	/**
	 * your doer role
	 * 
	 * @return your doer role
	 */
	protected abstract ARole getDoerRole();
	
	
	/**
	 * Do your prepare next
	 * 
	 * @param target
	 */
	protected abstract void doPrepareNext(IVector2 target, int run);
	
	
	protected abstract String getIdentifier();
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the distancesCtr
	 */
	public final int getDistancesCtr()
	{
		return distancesCtr;
	}
	
	
	/**
	 * @param distancesCtr the distancesCtr to set
	 */
	public final void setDistancesCtr(int distancesCtr)
	{
		this.distancesCtr = distancesCtr;
	}
	
	
	/**
	 * @return the state
	 */
	public final EState getState()
	{
		return state;
	}
	
	
	/**
	 * @param state the state to set
	 */
	public final void setState(EState state)
	{
		this.state = state;
	}
	
	
	/**
	 * @return the durations
	 */
	public final List<Float> getDurations()
	{
		return durations;
	}
}
