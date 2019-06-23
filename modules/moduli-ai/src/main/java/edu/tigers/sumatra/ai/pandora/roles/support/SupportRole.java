/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support;

import static edu.tigers.sumatra.ai.math.DefenseMath.ReceiveData;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.defense.PassReceiverCalc;
import edu.tigers.sumatra.ai.metis.support.SupportPositionGenerationCalc;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.support.states.FakePassReceiverState;
import edu.tigers.sumatra.ai.pandora.roles.support.states.GlobalPositionRunnerState;
import edu.tigers.sumatra.ai.pandora.roles.support.states.MoveToGlobalPosState;
import edu.tigers.sumatra.ai.pandora.roles.support.states.PassTargetFollowerState;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statemachine.IEvent;


/**
 * Highly "coachable" supporter role, trigger different support behavior with different states
 */
public class SupportRole extends ARole
{
	private IVector2 globalPosition;
	
	@Configurable(comment = "Activate the global position runner. Otherwise drive to global pos and optimize")
	private static boolean useGlobalPositionRunner = false;
	
	
	/**
	 * All possible supporter states
	 */
	public enum EEvent implements IEvent
	{
		MOVE,
		RUN_THROUGH_GLOBAL_POSITIONS,
		LOCAL_OPTIMIZATION,
		FAKE_RECEIVER
	}
	
	
	/**
	 * Constructor. What else?
	 */
	public SupportRole()
	{
		super(ERole.SUPPORT);
		setInitialState(new MoveToGlobalPosState(this));
		addTransition(EEvent.MOVE, new MoveToGlobalPosState(this));
		addTransition(EEvent.RUN_THROUGH_GLOBAL_POSITIONS, new GlobalPositionRunnerState(this));
		addTransition(EEvent.LOCAL_OPTIMIZATION, new PassTargetFollowerState(this));
		addTransition(EEvent.FAKE_RECEIVER, new FakePassReceiverState(this));
	}
	
	
	public EEvent getGlobalEvent()
	{
		ReceiveData receiveData = new ReceiveData(getBot(), getBall().getTrajectory().getPlanarCurve(),
				getBall().getPos());
		if (receiveData.getDistToBallCurve() < PassReceiverCalc.getValidReceiveDistance()
				&& getBall().getVel().getLength() > 0.01)
		{
			return EEvent.FAKE_RECEIVER;
		}
		if (globalPosition != null
				&& getPos().distanceTo(getBall().getPos()) < SupportPositionGenerationCalc.getMinSupporterDistance())
		{
			return EEvent.MOVE;
		}
		if (globalPosition == null)
		{
			if (useGlobalPositionRunner)
			{
				return EEvent.RUN_THROUGH_GLOBAL_POSITIONS;
			} else
			{
				return EEvent.LOCAL_OPTIMIZATION;
			}
		}
		return null;
	}
	
	
	public IVector2 getGlobalPosition()
	{
		return globalPosition;
	}
	
	
	public void setGlobalPosition(IVector2 globalPosition)
	{
		this.globalPosition = globalPosition;
	}
	
}
