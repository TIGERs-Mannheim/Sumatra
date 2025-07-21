/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class CircleCollisionObjectTest
{

	@Test
	public void testCollision()
	{
		IVector2 center = Vector2.fromXY(100, -200);
		ICircle circle = Circle.createCircle(center, 300);
		IVector3 vel = Vector3.zero();
		CircleCollisionObject colHandler = new CircleCollisionObject(circle, vel);

		Optional<ICollision> collision;

		collision = colHandler.getCollision(center.getXYZVector(), center.getXYZVector());
		assertThat(collision).isNotPresent();

		collision = colHandler.getCollision(Vector3.fromXY(400, -200), Vector3.fromXY(400, -200));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(400, -200));

		collision = colHandler.getCollision(Vector3.fromXY(401, -200), Vector3.fromXY(400, -200));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(400, -200));

		collision = colHandler.getCollision(Vector3.fromXY(401, -200), Vector3.fromXY(399, -200));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(400, -200));

		collision = colHandler.getCollision(Vector3.fromXY(401, -200), Vector3.fromXY(-401, -200));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(400, -200));

		collision = colHandler.getCollision(Vector3.fromXY(400, -200), Vector3.fromXY(-400, -200));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(400, -200));

		collision = colHandler.getCollision(Vector3.fromXY(100, 600), Vector3.fromXY(100, -200));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(0, 1));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(100, 100));

		collision = colHandler.getCollision(Vector3.fromXY(100, -600), Vector3.fromXY(100, -200));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(0, -1));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(100, -500));
	}


	@Test
	public void testInsideCollision()
	{
		IVector2 center = Vector2.fromXY(100, -200);
		ICircle circle = Circle.createCircle(center, 300);
		IVector3 vel = Vector3.zero();
		CircleCollisionObject colHandler = new CircleCollisionObject(circle, vel);

		Optional<ICollision> collision;

		collision = colHandler.getInsideCollision(Vector3.fromXY(100, -200));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromX(1));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(400, -200));

		collision = colHandler.getInsideCollision(Vector3.fromXY(400, -200));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromX(1));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(400, -200));

		collision = colHandler.getInsideCollision(Vector3.fromXY(100, -500));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromY(-1));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(100, -500));

		collision = colHandler.getInsideCollision(Vector3.fromXY(300, -200));
		assertThat(collision).isPresent();
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromX(1));
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(400, -200));

		collision = colHandler.getInsideCollision(Vector3.fromXY(500, -200));
		assertThat(collision).isNotPresent();
	}


	@Test
	public void testSurfaceVel()
	{
		// Position in mm, mm
		// Velocities in m/s, m/s, rad/s
		var center = Vector2.fromXY(-500, 500);
		var circle = Circle.createCircle(center, 1);
		var vel = Vector3.fromXYZ(1, 0, 3);
		var colHandler = new CircleCollisionObject(circle, vel);


		IVector2 surfaceVel;


		//              <--|
		//                 |
		// ^ +y            O
		// |               |
		// +-> +x          |-->
		surfaceVel = colHandler.getSurfaceVel(center.addNew(Vector2.zero()));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector());

		surfaceVel = colHandler.getSurfaceVel(center.addNew(Vector2.fromX(1)));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromY(3)));
		surfaceVel = colHandler.getSurfaceVel(center.addNew(Vector2.fromX(-1)));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromY(-3)));
		surfaceVel = colHandler.getSurfaceVel(center.addNew(Vector2.fromY(1)));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromX(-3)));
		surfaceVel = colHandler.getSurfaceVel(center.addNew(Vector2.fromY(-1)));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromX(3)));


		surfaceVel = colHandler.getSurfaceVel(center.addNew(Vector2.fromX(2)));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromY(3)));
		surfaceVel = colHandler.getSurfaceVel(center.addNew(Vector2.fromX(-2)));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromY(-3)));
		surfaceVel = colHandler.getSurfaceVel(center.addNew(Vector2.fromY(2)));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromX(-3)));
		surfaceVel = colHandler.getSurfaceVel(center.addNew(Vector2.fromY(-2)));
		assertThat(surfaceVel).isEqualTo(vel.getXYVector().add(Vector2.fromX(3)));
	}
}