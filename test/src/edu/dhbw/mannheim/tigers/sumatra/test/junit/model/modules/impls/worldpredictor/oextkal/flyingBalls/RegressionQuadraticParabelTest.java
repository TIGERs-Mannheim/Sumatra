package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Coord;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.RegParab;

public class RegressionQuadraticParabelTest {

	@Test
	public void testQuadraticParabel() {
		Coord[] flyBallCoords = new Coord[Def.t.size];

		for(int i = 0; i < edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def.t.size; i++)
		{
			flyBallCoords[i] = new Coord(Def.t.distance[i], Def.t.height[i]);
		}

		RegParab parabel = new RegParab(flyBallCoords);
	
		/*
		System.out.println("A: "+parabel.getA());
		System.out.println("B: "+parabel.getB());
		System.out.println("C: "+parabel.getC());
		System.out.println("D: "+parabel.getD());
		System.out.println("E: "+parabel.getE());
		System.out.println("Alpha: "+parabel.getAlpha());
		*/
		
		assertEquals(Def.t.a, parabel.getA(), Math.abs(Def.t.a*Def.hund));
		assertEquals(Def.t.b, parabel.getB(), Math.abs(Def.t.b*Def.hund));
		assertEquals(Def.t.c, parabel.getC(), 0.1);
		assertEquals(Def.t.d, parabel.getD(), Math.abs(Def.t.d*Def.hund));
	    assertEquals(Def.t.e, parabel.getE(), Math.abs(Def.t.e*Def.hund));
	    assertEquals(Def.t.alpha, parabel.getAlpha(), Math.abs(Def.t.alpha*Def.hund));
	    
	    for(int i = 0; i < Def.t.size; i++)
	    {
	    	assertEquals(Def.t.height[i], parabel.f(Def.t.distance[i]), Def.t.height[i]*Def.hund);
	    }
	}

}
