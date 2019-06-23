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
import edu.tigers.sumatra.skillsystem.driver.PositionDriver;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.statemachine.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PosTest2Skill extends AMoveSkill
{
	private final PositionDriver	d	= new PositionDriver();
	
	private final IVector3			p1, p2;
	private final double				dist;
	
	
	/**
	 * @param p1
	 * @param p2
	 * @param dist
	 */
	public PosTest2Skill(final IVector3 p1, final IVector3 p2, final double dist)
	{
		super(ESkill.POS_TEST_2);
		this.p1 = p1;
		this.p2 = p2;
		this.dist = dist;
		IState one = new OneState();
		IState two = new TwoState();
		setInitialState(one);
		addTransition(one, EEvent.NEXT, two);
		addTransition(two, EEvent.NEXT, one);
		setPathDriver(d);
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
			if ((tWaitStart == 0) && (GeoMath.distancePP(getPos(), d.getDestination()) < 10)
					&& (Math.abs(AngleMath.difference(getAngle(), d.getOrientation())) < 0.01))
			{
				tWaitStart = getWorldFrame().getTimestamp();
			}
			if ((tWaitStart != 0) && (GeoMath.distancePP(getPos(), d.getDestination()) > 50)
					&& (Math.abs(AngleMath.difference(getAngle(), d.getOrientation())) > 0.1))
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
			d.setDestination(p1.getXYVector());
			d.setOrientation(p1.z());
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.ONE;
		}
	}
	
	private class TwoState extends BaseState
	{
		private double step = 0;
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			d.setDestination(p2.getXYVector());
			d.setOrientation(p2.z());
			step = 0;
		}
		
		
		@Override
		public void doUpdate()
		{
			super.doUpdate();
			
			d.setDestination(GeoMath.stepAlongLine(p2.getXYVector(), p1.getXYVector(), step));
			
			if (step < dist)
			{
				step += 6;
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.TWO;
		}
	}
}
