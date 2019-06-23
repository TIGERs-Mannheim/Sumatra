package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls.cases;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Coord;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;

public class TestCaseFour extends ATestCase{

	public TestCaseFour()
	{
	startPos = new Coord(500,-300);
	viewAngle = 1.047;
	

	
	
	//Zur Kalkulation der RoboterPosition:
	view = new Coord(0.577,1);
	roboPos = new Coord(startPos.x() - (Math.cos(viewAngle)*Def.BOT_RADIUS),
			              startPos.y() - (Math.sin(viewAngle)*Def.BOT_RADIUS));
	//System.out.println("originRobo: "+roboPos.toString());
	
	size = 8;
	

	

	ballBottom = new Coord[size];
	//ballBottom[0] = new Coord( 750.12, 405.07);
	ballBottom[0] = new Coord( 503.85, -293.31);
	ballBottom[1] = new Coord( 949.49, 855.667);
	ballBottom[2] = new Coord(1189.569, 1334.998);
	ballBottom[3] = new Coord(1451.069, 1798.312);
	ballBottom[4] = new Coord(1708.797, 2199.991);
	ballBottom[5] = new Coord(1939.230, 2507.670);
	ballBottom[6] = new Coord(2126.991, 2709.417);
	ballBottom[7] = new Coord(2266.890, 2812.880);
	
	ballFly = new Coord[size];
	//ballFly[0] = new Coord( 885.67,  368.00);
	ballFly[0] = new Coord( 501.56,  -294.45);
	ballFly[1] = new Coord(1078.50,  702.01);
	ballFly[2] = new Coord(1271.34,  1036.01);
	ballFly[3] = new Coord(1464.18,  1370.01);
	ballFly[4] = new Coord(1657.02,  1704.01);
	ballFly[5] = new Coord(1849.85,  2038.02);
	ballFly[6] = new Coord(2042.69,  2372.02);
	ballFly[7] = new Coord(2235.53,  2706.02);

	timeOffsetExtern = 12345678;
	timeOffsetIntern = 111;
		
	timeIntern = new double[size];
	timeIntern[0] = 200;
	timeIntern[1] = 300;
	timeIntern[2] = 400;
	timeIntern[3] = 500;
	timeIntern[4] = 600;
	timeIntern[5] = 700;
	timeIntern[6] = 800;
	timeIntern[7] = 900;
	
	for(int i = 0; i < size; i++)
	{
		timeIntern[i] = timeIntern[i] + timeOffsetIntern;
	}
	
	timeExtern = new double[size];
	for(int i = 0; i < size; i++)
	{
		timeExtern[i] = timeIntern[i] + timeOffsetExtern;
	}
	
	height = new double[size];
	//height[0] =  723.05;
	height[0] =    9.17;
	height[1] =  937.43;
	height[2] = 1053.71;
	height[3] = 1071.88;
	height[4] =  991.96;
	height[5] =  813.94;
	height[6] =  537.81;
	height[7] =  163.59;
				
	distance = new double[size];
	//distance[0] = 771.35;
	distance[0] =    7.71;
	distance[1] = 1157.02;
	distance[2] = 1542.69;
	distance[3] = 1928.37;
	distance[4] = 2314.04;
	distance[5] = 2699.70;
	distance[6] = 3085.38;
	distance[7] = 3471.05;
	
	a =    -0.00033;
	b =     1.19;
	c =     0;
	d = -1806.9867;
    e =   1076.74145;
	alpha = 0.87266;

	
	v0 = 6;
	
	botsNumber = 3;

	bots = new double[size][botsNumber][3];
	
	//init
	for(int i = 0; i < size; i++)
	{
		for(int j = 0; j < botsNumber; j++)
		{
			for(int k = 0; k < 3; k++)
			{
				bots[i][j][k] = 0;
			}
		}
	}
	
	//timestep1
	bots[0][0][0] = roboPos.x();
	bots[0][0][1] = roboPos.y();
	bots[0][0][2] = viewAngle;
	
	
	camID = 1;
	
	Def.setParameter(
			  4000,
			  1500,
			  200,
			  1,
			  Def.DUMMY,
			  Def.DUMMY,
			  0);


	
	
	
}
}
