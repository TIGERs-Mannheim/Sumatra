/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 1, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.latency;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.WriterConsumer;
import com.carrotsearch.junitbenchmarks.XMLConsumer;
import com.carrotsearch.junitbenchmarks.h2.H2Consumer;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.MessageConnection;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.Messaging;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IReceiving;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.sending.ISending;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.SimpleReceiver;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.test.SimpleSender;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;


/**
 * supressed warning of deprecation, because MethodRule is deprecated, but is used here for a simple benchmark, which is
 * used to let the test run several times
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
@SuppressWarnings("deprecation")
@BenchmarkOptions(benchmarkRounds = LatencyTest.ROUNDS, warmupRounds = 0, concurrency = LatencyTest.ROUNDS)
public class LatencyTest
{
	
	private static final int			MAX_MESSAGES	= 1000000;
	/**  */
	public static final int				ROUNDS			= 1;
	
	private static final Random		rand;
	private static final String		HOST				= "localhost";
	private static final int			PORT				= 1883;
	private static SummaryStatistics	sumStats;
	static
	{
		rand = new Random(System.currentTimeMillis());
		Logger.getRootLogger().setLevel(Level.OFF);
		sumStats = new SummaryStatistics(MAX_MESSAGES, ROUNDS);
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testQos0SimpleImplLocal()
	{
		try
		{
			IReceiving sRcv = new SimpleReceiver(HOST, PORT);
			ISending sSnd = new SimpleSender(ETopics.TEST_LATENCY_LOCAL, HOST, PORT);
			qos0MeasureLocal(sRcv, sSnd);
		} catch (MqttException err)
		{
			err.printStackTrace();
			fail();
		}
		
	}
	
	
	/**
	 */
	@Test
	// @Ignore
	public void testQos0ComplexImplLocal()
	{
		MessageConnection con = Messaging.getConnection(rand.nextInt());
		con.setHostPort(HOST, PORT);
		con.connect();
		IReceiving sRcv = con;
		ISending sSnd = con;
		qos0MeasureLocal(sRcv, sSnd);
		con.disconnect();
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testQos0ComplexImplMany()
	{
		MessageConnection con = Messaging.getConnection(rand.nextInt());
		con.setHostPort(HOST, PORT);
		con.connect();
		IReceiving sRcv = con;
		ISending sSnd = con;
		qos0MeasureLocal(sRcv, sSnd);
		con.disconnect();
	}
	
	
	private void qos0MeasureLocal(IReceiving sRcv, ISending sSnd)
	{
		System.out.println("---------LOCAL---------");
		
		LatencyReceiver latencyStatistics = new LatencyReceiver(MAX_MESSAGES);
		
		sRcv.addMessageReceiver(ETopics.TEST_LATENCY_LOCAL, latencyStatistics);
		
		sendMessgaes(sSnd);
		
		latencyStatistics.calcStats();
		
		System.out.println(latencyStatistics.toString());
		
		sumStats.addStats(latencyStatistics.getStats());
		sRcv.removeMessageReceiver(ETopics.TEST_LATENCY_LOCAL, latencyStatistics);
		
		System.out.println("Last received ID: " + latencyStatistics.getLastId());
		System.out.println("---------LOCAL-FIN--------");
		System.out.println("");
	}
	
	
	private void sendMessgaes(ISending sSnd)
	{
		List<LatencyMessage> objs = new ArrayList<LatencyMessage>(MAX_MESSAGES);
		for (int i = 0; i < MAX_MESSAGES; i++)
		{
			LatencyMessage latLoc = new LatencyMessage(i, sSnd);
			objs.add(latLoc);
		}
		
		long start = System.nanoTime();
		long middle = start;
		
		List<Long> middles = new ArrayList<Long>(10);
		int i = 0;
		for (LatencyMessage latLoc : objs)
		{
			latLoc.timeMeasure();
			if ((i % (MAX_MESSAGES / 10)) == 0)
			{
				System.out.print(".");
				long newMiddle = System.nanoTime();
				middles.add(newMiddle - middle);
				middle = newMiddle;
			}
			// try
			// {
			// Thread.sleep(1000);
			// } catch (InterruptedException err)
			// {
			// err.printStackTrace();
			// }
			i++;
		}
		long end = System.nanoTime();
		System.out.print("\n");
		
		long timeMs = TimeUnit.NANOSECONDS.toMillis(end - start);
		System.out.printf("Time to send %d messages: %dms\n", MAX_MESSAGES, timeMs);
		
		try
		{
			Thread.sleep(20000);
		} catch (InterruptedException err)
		{
			err.printStackTrace();
		}
		
		DescriptiveStatistics stat = new DescriptiveStatistics(MAX_MESSAGES);
		long lastStart = 0;
		for (LatencyMessage latLoc : objs)
		{
			if (lastStart != 0)
			{
				long diff = latLoc.getStart() - lastStart;
				stat.addValue(diff);
			}
			lastStart = latLoc.getStart();
		}
		System.out.println("---------Send messages diff---------");
		System.out.printf("max: %.1f min: %.1f mean:%.1f\n", stat.getMax(), stat.getMin(), stat.getMean());
		System.out.println("---------Time per tenth of messages---------");
		for (Long m : middles)
		{
			System.out.printf("%dms ", TimeUnit.NANOSECONDS.toMillis(m));
		}
		System.out.println();
	}
	
	
	/**
	 */
	@AfterClass
	public static void summaryStatistics()
	{
		System.out.println(sumStats.toString());
	}
	
	
	/** @deprecated */
	@Deprecated
	@Rule
	public MethodRule					benchmarkRun	= new BenchmarkRule(h2Consumer, writerConsumer, xmlConsumer);
	
	private static final File		dbFile			= new File("benchmark/" + LatencyTest.class.getName());
	// private static final File dbFileFull = new File(dbFile.getName() +
	// ".h2.db");
	private static final File		xmlFile			= new File("benchmark/" + LatencyTest.class.getName() + ".xml");
	
	private static H2Consumer		h2Consumer;
	private static WriterConsumer	writerConsumer;
	private static XMLConsumer		xmlConsumer;
	
	
	/**
	 * @throws SQLException
	 * @throws IOException
	 */
	@BeforeClass
	public static void checkFile() throws SQLException, IOException
	{
		// This condition will delete the database, it it exists. For historical
		// values leave this in comment.
		// if (dbFileFull.exists())
		// assertTrue(dbFileFull.delete());
		
		h2Consumer = new H2Consumer(dbFile);
		writerConsumer = new WriterConsumer();
		xmlConsumer = new XMLConsumer(xmlFile);
		
	}
}
