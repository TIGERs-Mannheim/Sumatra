/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class SingleSidedLineCollisionTest
{
	
	@Test
	public void testCollision()
	{
		ILine obstacleLine = Line.fromPoints(Vector2.fromXY(0, -2), Vector2.fromXY(0, 2));
		IVector3 vel = Vector3.zero();
		IVector2 normal = Vector2.fromX(1);
		SingleSidedLineCollision colHandler = new SingleSidedLineCollision(obstacleLine, vel, normal);
		
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
		assertThat(collision).isNotPresent();
		
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
}