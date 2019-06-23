/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class CircleCollisionTest
{
	
	@Test
	public void testCollision()
	{
		IVector2 center = Vector2.fromXY(100, -200);
		ICircle circle = Circle.createCircle(center, 300);
		IVector3 vel = Vector3.zero();
		CircleCollision colHandler = new CircleCollision(circle, vel);
		
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
		CircleCollision colHandler = new CircleCollision(circle, vel);
		
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
	
}