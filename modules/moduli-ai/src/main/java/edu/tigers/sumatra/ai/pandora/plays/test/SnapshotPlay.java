/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.test.kick.ABallPreparationPlay;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.snapshot.SnapObject;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.statemachine.AState;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Paths;


/**
 * Position robots and ball based on a snapshot.
 */
@Log4j2
public class SnapshotPlay extends ABallPreparationPlay
{
	private Snapshot snapshot;


	public SnapshotPlay()
	{
		super(EPlay.SNAPSHOT);
		setUseAssistant(true);
		setExecutionState(new ExecutionState());
	}


	public void setSnapshotFile(String snapshot)
	{
		try
		{
			this.snapshot = Snapshot.loadFromFile(Paths.get(snapshot));
		} catch (IOException e)
		{
			log.warn("Failed to open snapshot", e);
		}
	}


	@Override
	protected boolean ready()
	{
		return snapshot != null && !getRoles().isEmpty();
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		if (snapshot != null && snapshot.getBall() != null)
		{
			setBallTargetPos(snapshot.getBall().getPos().getXYVector());

			getShapes(EAiShapesLayer.TEST_BALL_PLACEMENT).add(
					new DrawableCircle(Circle.createCircle(snapshot.getBall().getPos().getXYVector(), 300)).setColor(
							Color.green)
			);
			getShapes(EAiShapesLayer.TEST_BALL_PLACEMENT).add(
					new DrawableAnnotation(snapshot.getBall().getPos().getXYVector(), "Start")
							.withOffsetY(200)
							.withCenterHorizontally(true)
							.withFontHeight(100)
							.setColor(Color.cyan)
			);
			if (snapshot.getPlacementPos() != null)
			{
				getShapes(EAiShapesLayer.TEST_BALL_PLACEMENT).add(
						new DrawableCircle(Circle.createCircle(snapshot.getPlacementPos(), 300)).setColor(
								Color.cyan)
				);
			}

		}
		super.doUpdateBeforeRoles();
	}


	private class ExecutionState extends AState
	{
		@Override
		public void doUpdate()
		{
			super.doUpdate();

			findOtherRoles(MoveRole.class).forEach(r -> reassignRole(r, MoveRole.class, MoveRole::new));
			findRoles(MoveRole.class).forEach(this::updateRole);
		}


		private void updateRole(MoveRole role)
		{
			SnapObject snapObject = snapshot.getBots().get(role.getBotID());
			if (snapObject != null)
			{
				role.updateDestination(snapObject.getPos().getXYVector());
				role.updateTargetAngle(snapObject.getPos().z());
				if (role.getPos().distanceTo(snapObject.getPos().getXYVector()) < 300)
				{
					role.getMoveCon().setBotsObstacle(false);
					role.getMoveCon().setBallObstacle(false);
				} else
				{
					role.getMoveCon().physicalObstaclesOnly();
				}
			}
		}
	}
}
