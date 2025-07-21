/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


/**
 *
 */
public class KickerFrontLineCollisionObjectTest
{

	@Test
	public void testCollision()
	{
		ILineSegment obstacleLine = Lines.segmentFromPoints(Vector2.fromXY(0, -2), Vector2.fromXY(0, 2));
		IVector3 vel = Vector3.zero();
		IVector2 normal = Vector2.fromX(1);
		KickerFrontLineCollisionObject colHandler = new KickerFrontLineCollisionObject(obstacleLine, vel, vel, normal,
				BotID.noBot());

		Optional<ICollision> collision;

		collision = colHandler.getCollision(Vector3.fromXY(1, 0), Vector3.fromXY(-1, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal()).isEqualTo(normal);
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(0, 0));

		collision = colHandler.getCollision(Vector3.fromXY(-1, 0), Vector3.fromXY(1, 0));
		assertThat(collision).isNotPresent();

		collision = colHandler.getCollision(Vector3.fromXY(1, 1), Vector3.fromXY(-1, -1));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal()).isEqualTo(normal);
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(0, 0));

		collision = colHandler.getCollision(Vector3.fromXY(0, 0), Vector3.fromXY(-1, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal()).isEqualTo(normal);
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(0, 0));

		collision = colHandler.getCollision(Vector3.fromXY(1, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal()).isEqualTo(normal);
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(0, 0));

		collision = colHandler.getCollision(Vector3.fromXY(0, -1), Vector3.fromXY(0, 1));
		assertThat(collision).isPresent();

		collision = colHandler.getCollision(Vector3.fromXY(1, 2), Vector3.fromXY(-1, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal()).isEqualTo(normal);
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(0, 1));

		collision = colHandler.getCollision(Vector3.fromXY(2, 0), Vector3.fromXY(1, 0));
		assertThat(collision).isNotPresent();

		collision = colHandler.getCollision(Vector3.fromXY(0, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal()).isEqualTo(normal);
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(0, 0));
	}


	@Test
	public void testSurfaceVel()
	{
		// Position in mm, mm
		// Velocities in m/s, m/s, rad/s
		var obstacleLine = Lines.segmentFromPoints(Vector2.fromXY(0, -2), Vector2.fromXY(0, 2));
		var vel = Vector3.fromXYZ(1, 0, 3);
		var normal = Vector2.fromX(1);
		var colHandler = new KickerFrontLineCollisionObject(obstacleLine, vel, vel, normal, BotID.noBot());

		IVector2 surfaceVel;


		//              <--|
		//                 |
		// ^ +y            O
		// |               |
		// +-> +x          |-->
		surfaceVel = colHandler.getSurfaceVel(Vector2.zero());
		assertThat(surfaceVel).isEqualTo(vel.getXYVector());

		surfaceVel = colHandler.getSurfaceVel(Vector2.fromX(1));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector());
		surfaceVel = colHandler.getSurfaceVel(Vector2.fromX(-1));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector());

		surfaceVel = colHandler.getSurfaceVel(Vector2.fromY(1));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromX(-3)));
		surfaceVel = colHandler.getSurfaceVel(Vector2.fromY(-1));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromX(3)));

		surfaceVel = colHandler.getSurfaceVel(Vector2.fromY(2));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromX(-6)));
		surfaceVel = colHandler.getSurfaceVel(Vector2.fromY(-2));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromX(6)));
	}
}