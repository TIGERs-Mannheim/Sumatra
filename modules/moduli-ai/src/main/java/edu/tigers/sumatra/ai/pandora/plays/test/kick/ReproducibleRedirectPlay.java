/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test.kick;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.test.kick.RedirectTestRole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statemachine.AState;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


/**
 * Perform reproducible redirects.
 * One robot passes to a second robot which redirects the ball to a target.
 * The initial position of the redirect bot can differ from the pass target of the first bot.
 */
public class ReproducibleRedirectPlay extends ABallPreparationPlay
{
	private final IVector2 ballPos;
	private final IVector2 receivingPos;
	private final IVector2 passTarget;
	private final IVector2 redirectTarget;
	private final boolean receive;
	private final RedirectTestRole.EKickMode kickMode;


	public ReproducibleRedirectPlay(
			IVector2 ballPos,
			IVector2 receivingPos,
			IVector2 passTarget,
			IVector2 redirectTarget,
			boolean receive,
			RedirectTestRole.EKickMode kickMode
	)
	{
		super(EPlay.REPRODUCIBLE_REDIRECT);
		this.ballPos = ballPos;
		this.receivingPos = receivingPos;
		this.passTarget = passTarget;
		this.redirectTarget = redirectTarget;
		this.receive = receive;
		this.kickMode = kickMode;

		setExecutionState(new ExecutionState());
	}


	@Override
	protected ARole onAddRole()
	{
		return new RedirectTestRole();
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		setBallTargetPos(ballPos);

		List<IDrawableShape> shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.TEST_KICK);
		shapes.add(new DrawableLine(Line.fromPoints(ballPos, receivingPos), Color.BLUE));
		shapes.add(new DrawableLine(Line.fromPoints(ballPos, passTarget), Color.GREEN));
		shapes.add(new DrawableLine(Line.fromPoints(receivingPos, redirectTarget), Color.MAGENTA));
	}


	@Override
	protected void handleNonPlacingRole(ARole role)
	{
		if (!(role instanceof RedirectTestRole))
		{
			role = switchRoles(role, new RedirectTestRole());
		}
		RedirectTestRole redirectTestRole = (RedirectTestRole) role;
		redirectTestRole.changeToWait();
		redirectTestRole.setDesiredBallPosition(receivingPos);
	}


	@Override
	protected boolean ready()
	{
		return getRoles().size() == 2;
	}


	private class ExecutionState extends AState
	{
		private boolean ballMoved;


		@Override
		public void doEntryActions()
		{
			for (ARole role : new ArrayList<>(getRoles()))
			{
				if (!(role instanceof RedirectTestRole))
				{
					switchRoles(role, new RedirectTestRole());
				}
			}
			ARole passingRole = getClosestToBall();

			for (ARole role : getRoles())
			{
				RedirectTestRole redirectTestRole = (RedirectTestRole) role;
				if (redirectTestRole == passingRole)
				{
					redirectTestRole.setTarget(passTarget);
					redirectTestRole.setKickMode(kickMode);
					redirectTestRole.changeToPass();
				} else
				{
					redirectTestRole.setTarget(redirectTarget);
					if (receive)
					{
						redirectTestRole.changeToReceive();
					} else
					{
						redirectTestRole.changeToRedirect();
					}
				}
			}

			ballMoved = false;
		}


		@Override
		public void doUpdate()
		{
			if (getBall().getPos().distanceTo(ballPos) > 500)
			{
				ballMoved = true;
			}
			if (ballMoved && (getBall().getVel().getLength2() < 1.0
					|| getBall().getTrajectory().getTravelLine().isPointInFront(ballPos)))
			{
				stateMachine.triggerEvent(EEvent.EXECUTED);
			}
		}
	}
}
