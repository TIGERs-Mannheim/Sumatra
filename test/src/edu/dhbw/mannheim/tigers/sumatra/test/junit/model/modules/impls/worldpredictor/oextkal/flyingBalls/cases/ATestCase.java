package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls.cases;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Coord;


public abstract class ATestCase {
	
	public Coord startPos;
	public Coord roboPos;
	public Coord view;
	
	public double viewAngle;
	
	public Coord[] ballBottom;

	public Coord[] ballFly;
	
	public double[] timeExtern;
	
	public double[] timeIntern;
	
	public double[] height;
	
	public double[] distance;
	
	public int camID;

	
	public double a;
	public double b;
	public double c;
	public double d;
	public double e;
	public double alpha;
	
	public double v0;
	
	public int size;
	
	public double timeOffsetExtern;
	public double timeOffsetIntern;
	
	public int botsNumber;
	//zeitpunkt, bot, (x,y,angle)
	public double[][][] bots;

}
