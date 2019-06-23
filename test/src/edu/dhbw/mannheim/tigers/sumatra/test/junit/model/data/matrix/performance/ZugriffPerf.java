/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 3, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * test, which is more performant: double[][] or double[]
 * @author Birgit
 * 
 */
public class ZugriffPerf extends APerformanceTest
{
	
	private int			l;
	private double		dataVal;
	private final int	times		= 10;
	private final int	timesOps	= timesEasyOps * 5;
	
	
	/**
	 * 
	 */
	public ZugriffPerf()
	{
		super();
	}
	
	
	/**
	 *
	 */
	@BeforeClass
	public static void beforeClass()
	{
		System.out.println("##########################################################");
		System.out.println("ZugriffPerformance");
		System.out.println("##########################################################");
		System.out.println("--> Many tests deactivated");
	}
	
	
	/**
	 *
	 */
	@AfterClass
	public static void afterClass()
	{
		System.out.println("##########################################################");
		System.out.println("ZugriffPerformance End");
		System.out.println("##########################################################");
	}
	
	
	/**
	 */
	@Test
	public void testEinfach()
	{
		output = false;
		
		EinfachZugriff(times);
		EinfachSpeicher(times);
		EinfachAnlegen(times);
		EinfachZugriffUndSpeicher(times);
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testEinfachMany()
	{
		output = true;
		System.out.println("Number of runs: " + timesOps);
		
		EinfachZugriff(timesOps);
		EinfachSpeicher(timesOps);
		EinfachAnlegen(timesOps / 50);
		EinfachZugriffUndSpeicher(timesOps);
	}
	
	
	/**
	 */
	@Test
	public void testZweifach()
	{
		output = false;
		
		ZweifachZugriff(times);
		ZweifachSpeicher(times);
		// ZweifachAnlegen(times);
		ZweifachZugriffUndSpeicher(times);
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testZweifachMany()
	{
		output = true;
		System.out.println("Number of runs: " + timesOps);
		
		ZweifachZugriff(timesOps);
		ZweifachSpeicher(timesOps);
		// ZweifachAnlegen(timesOps / 50);
		ZweifachZugriffUndSpeicher(timesOps);
		
	}
	
	
	/**
	 * 
	 * @param times
	 */
	public void EinfachZugriff(int times)
	{
		if (output)
		{
			System.out.println("EinfachZugriff");
		}
		
		// 4*14
		final int rows = 14;
		final int coles = 4;
		final double[] data = new double[rows * coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j * coles;
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
				l = j * coles;
				for (int k = 0; k < coles; k++)
				{
					if (data[l + k] < 0)
					{
						
					}
				}
			}
			
		}
		endTimer();
		System.out.println(dataVal);
	}
	
	
	/**
	 * 
	 * @param times
	 */
	public void ZweifachZugriff(int times)
	{
		if (output)
		{
			System.out.println("ZweifachZugriff");
		}
		
		// 4*14
		final int rows = 14;
		final int coles = 4;
		final double[][] data = new double[rows][coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j * coles;
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
				l = j * coles;
				for (int k = 0; k < coles; k++)
				{
					if (data[j][k] < 0)
					{
						
					}
				}
			}
			
		}
		endTimer();
		System.out.println(dataVal);
	}
	
	
	/**
	 * 
	 * @param times
	 */
	public void EinfachSpeicher(int times)
	{
		if (output)
		{
			System.out.println("EinfachSpeicher");
		}
		
		// 4*14
		final int rows = 14;
		final int coles = 4;
		final double[] data = new double[rows * coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j * coles;
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
				l = j * coles;
				for (int k = 0; k < coles; k++)
				{
					data[l + k] = dataVal;
				}
			}
			
		}
		endTimer();
		System.out.println(data[l % 2]);
	}
	
	
	/**
	 * 
	 * @param times
	 */
	public void ZweifachSpeicher(int times)
	{
		if (output)
		{
			System.out.println("ZweifachSpeicher");
		}
		
		// 4*14
		final int rows = 14;
		final int coles = 4;
		final double[][] data = new double[rows][coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j * coles;
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
				l = j * coles;
				for (int k = 0; k < coles; k++)
				{
					data[j][k] = dataVal;
				}
			}
			
		}
		endTimer();
	}
	
	
	/**
	 * 
	 * @param times
	 */
	public void EinfachAnlegen(int times)
	{
		if (output)
		{
			System.out.println("EinfachAnlegen");
		}
		
		// 4*14
		final int rows = 14;
		final int coles = 4;
		double[] data = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			for (int j = 0; j < rows; j++)
			{
				for (int k = 0; k < coles; k++)
				{
					data = new double[rows * coles];
				}
			}
			
		}
		endTimer();
		if (data != null)
		{
			System.out.println(data[l % 2]);
		}
	}
	
	
	// public void ZweifachAnlegen(int times)
	// {
	// if (output)
	// System.out.println("ZweifachAnlegen");
	//
	// // 4*14
	// int rows = 14;
	// int coles = 4;
	// double[][] data = null;
	// startTimer();
	// for (int i = 0; i < times; i++)
	// {
	// for (int j = 0; j < rows; j++)
	// {
	// for (int k = 0; k < coles; k++)
	// {
	// data = new double[rows][coles];
	// }
	// }
	//
	// }
	// endTimer();
	// }
	//
	/**
	 * 
	 * @param times
	 */
	public void EinfachZugriffUndSpeicher(int times)
	{
		if (output)
		{
			System.out.println("EinfachZugriffUndSpeicher");
		}
		
		// 4*14
		final int rows = 14;
		final int coles = 4;
		final double[] data = new double[rows * coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j * coles;
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
				l = j * coles;
				for (int k = 0; k < coles; k++)
				{
					data[l + k] = data[k];
				}
			}
			
		}
		endTimer();
		System.out.println(data[l % 2]);
	}
	
	
	/**
	 * 
	 * @param times
	 */
	public void ZweifachZugriffUndSpeicher(int times)
	{
		if (output)
		{
			System.out.println("ZweifachZugriffUndSpeicher");
		}
		
		// 4*14
		final int rows = 14;
		final int coles = 4;
		final double[][] data = new double[rows][coles];
		
		for (int j = 0; j < rows; j++)
		{
			l = j * coles;
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
				l = j * coles;
				for (int k = 0; k < coles; k++)
				{
					data[j][k] = data[0][k];
				}
			}
			
		}
		endTimer();
	}
}
