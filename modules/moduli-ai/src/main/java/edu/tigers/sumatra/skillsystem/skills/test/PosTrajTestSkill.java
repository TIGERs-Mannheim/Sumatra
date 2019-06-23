/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 4, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;
import edu.tigers.sumatra.statemachine.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PosTrajTestSkill extends MoveToTrajSkill
{
	private final IVector3 p1, p2;
	
	
	/**
	 * @param p1
	 * @param p2
	 */
	public PosTrajTestSkill(final IVector3 p1, final IVector3 p2)
	{
		super(ESkill.POS_TRAJ_TEST);
		this.p1 = p1;
		this.p2 = p2;
		IState one = new OneState();
		IState two = new TwoState();
		setInitialState(one);
		addTransition(one, EEvent.NEXT, two);
		addTransition(two, EEvent.NEXT, one);
	}
	
	
	private enum EStateId
	{
		ONE,
		TWO
	}
	
	private enum EEvent
	{
		NEXT
	}
	
	private abstract class BaseState implements IState
	{
		long tWaitStart = 0;
		
		
		@Override
		public void doEntryActions()
		{
			tWaitStart = 0;
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((tWaitStart == 0) && (GeoMath.distancePP(getPos(), getMoveCon().getDestination()) < 10)
					&& (Math.abs(AngleMath.difference(getAngle(), getMoveCon().getTargetAngle())) < 0.01))
			{
				tWaitStart = getWorldFrame().getTimestamp();
			}
			if ((tWaitStart != 0) && (GeoMath.distancePP(getPos(), getMoveCon().getDestination()) > 50)
					&& (Math.abs(AngleMath.difference(getAngle(), getMoveCon().getTargetAngle())) > 0.1))
			{
				tWaitStart = 0;
			}
			
			if ((tWaitStart != 0) && (((getWorldFrame().getTimestamp() - tWaitStart) / 1e9) > 0.5))
			{
				triggerEvent(EEvent.NEXT);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
	}
	
	
	private class OneState extends BaseState
	{
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().updateDestination(p1.getXYVector());
			getMoveCon().updateTargetAngle(p1.z());
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.ONE;
		}
	}
	
	private class TwoState extends BaseState
	{
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().updateDestination(p2.getXYVector());
			getMoveCon().updateTargetAngle(p2.z());
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.TWO;
		}
	}
}
