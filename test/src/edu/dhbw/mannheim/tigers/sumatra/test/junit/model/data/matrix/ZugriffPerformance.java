/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 3, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance.APerformanceTest;



/**
 * test, which is more performant: double[][] or double[]
 * @author Birgit
 * 
 */
public class ZugriffPerformance extends APerformanceTest
{
	
	public static void main(String[] args)
	{
		ZugriffPerformance test = new ZugriffPerformance();
		test.test();
	}
	
	private int l;
	private double dataVal;

	@Test
	public void test()
	{
		System.out.println("##########################################################");
		System.out.println("ZugriffPerformance");
		System.out.println("##########################################################");
		
		output = false;
		int times = 10;

		EinfachZugriff(times);
		EinfachSpeicher(times);
		EinfachAnlegen(times);
		EinfachZugriffUndSpeicher(times);
		
		ZweifachZugriff(times);
		ZweifachSpeicher(times);
		ZweifachAnlegen(times);
		ZweifachZugriffUndSpeicher(times);
		
		output = true;
		times = timesEasyOps*5;
		System.out.println("Number of runs: " + times);

		EinfachZugriff(times);
		EinfachSpeicher(times);
		EinfachAnlegen(times/50);
		EinfachZugriffUndSpeicher(times);
		
		ZweifachZugriff(times);
		ZweifachSpeicher(times);
		ZweifachAnlegen(times/50);
		ZweifachZugriffUndSpeicher(times);

	}
	

	public void EinfachZugriff(int times)
	{
		if (output)
			System.out.println("EinfachZugriff");
		
		// 4*14
		int rows = 14;
		int coles = 4;
		double[] data = new double[rows*coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j*coles;
			for (int k = 0; k < coles; k++)
			{
				data[l + k] = j + k;
			}
		}
		
		startTimer();
		for (int i = 0; i < times; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				l = j*coles;
				for (int k = 0; k < coles; k++)
				{
					if(data[l+k] < 0)
					{
						
					}
				}
			}
			
		}
		endTimer();
		System.out.println(dataVal);
	}
	
	public void ZweifachZugriff(int times)
	{
		if (output)
			System.out.println("ZweifachZugriff");
		
		// 4*14
		int rows = 14;
		int coles = 4;
		double[][] data = new double[rows][coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j*coles;
			for (int k = 0; k < coles; k++)
			{
				data[j][k] = j + k;
			}
		}
		
		startTimer();
		for (int i = 0; i < times; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				l = j*coles;
				for (int k = 0; k < coles; k++)
				{
					if(data[j][k] < 0)
					{
						
					}
				}
			}
			
		}
		endTimer();
		System.out.println(dataVal);
	}
	
	
	
	public void EinfachSpeicher(int times)
	{
		if (output)
			System.out.println("EinfachSpeicher");
		
		// 4*14
		int rows = 14;
		int coles = 4;
		double[] data = new double[rows*coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j*coles;
			for (int k = 0; k < coles; k++)
			{
				data[l + k] = j + k;
			}
		}
		
		startTimer();
		for (int i = 0; i < times; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				l = j*coles;
				for (int k = 0; k < coles; k++)
				{
					data[l+k] = dataVal;
				}
			}
			
		}
		endTimer();
		System.out.println(data[l%2]);
	}
	
	
	public void ZweifachSpeicher(int times)
	{
		if (output)
			System.out.println("ZweifachSpeicher");
		
		// 4*14
		int rows = 14;
		int coles = 4;
		double[][] data = new double[rows][coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j*coles;
			for (int k = 0; k < coles; k++)
			{
				data[j][k] = j + k;
			}
		}
		
		startTimer();
		for (int i = 0; i < times; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				l = j*coles;
				for (int k = 0; k < coles; k++)
				{
					data[j][k] = dataVal;
				}
			}
			
		}
		endTimer();
		System.out.println(data[l%2]);
	}
	
	public void EinfachAnlegen(int times)
	{
		if (output)
			System.out.println("EinfachAnlegen");
		
		// 4*14
		int rows = 14;
		int coles = 4;
		double[] data = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				for (int k = 0; k < coles; k++)
				{
					 data = new double[rows*coles];
				}
			}
			
		}
		endTimer();
		System.out.println(data[l%2]);
	}
	
	
	public void ZweifachAnlegen(int times)
	{
		if (output)
			System.out.println("ZweifachAnlegen");
		
		// 4*14
		int rows = 14;
		int coles = 4;
		double[][] data = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				for (int k = 0; k < coles; k++)
				{
					data = new double[rows][coles];
				}
			}
			
		}
		endTimer();
		System.out.println(data[l%2]);
	}
	
	
	public void EinfachZugriffUndSpeicher(int times)
	{
		if (output)
			System.out.println("EinfachZugriffUndSpeicher");
		
		// 4*14
		int rows = 14;
		int coles = 4;
		double[] data = new double[rows*coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j*coles;
			for (int k = 0; k < coles; k++)
			{
				data[l + k] = j + k;
			}
		}
		
		startTimer();
		for (int i = 0; i < times; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				l = j*coles;
				for (int k = 0; k < coles; k++)
				{
					data[l+k] = data[k];
				}
			}
			
		}
		endTimer();
		System.out.println(data[l%2]);
	}
	
	public void ZweifachZugriffUndSpeicher(int times)
	{
		if (output)
			System.out.println("ZweifachZugriffUndSpeicher");
		
		// 4*14
		int rows = 14;
		int coles = 4;
		double[][] data = new double[rows][coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j*coles;
			for (int k = 0; k < coles; k++)
			{
				data[j][k] = j + k;
			}
		}
		
		startTimer();
		for (int i = 0; i < times; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				l = j*coles;
				for (int k = 0; k < coles; k++)
				{
					data[j][k] = data[0][k];
				}
			}
			
		}
		endTimer();
		System.out.println(data[l%2]);
	}
}
