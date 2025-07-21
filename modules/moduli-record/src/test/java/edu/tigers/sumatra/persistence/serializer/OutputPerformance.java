/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Benchmark for testing the performance of MappedDataOutputStream.write(byte).
 * Be careful when adjusting benchmark parameters, as the files written during the benchmark will get large FAST.
 * In case of hard jvm crashes during the benchmark look into your tmp folder to remove eventual residuals.
 */
@State(Scope.Thread)
public class OutputPerformance
{
	private File tempFile;
	private MappedDataOutputStream stream;


	@Setup(Level.Trial)
	public void setup() throws IOException
	{
		tempFile = File.createTempFile("sumatra-serializer-perf", ".db");
		stream = new MappedDataOutputStream(tempFile.toPath());
	}


	@Benchmark
	@Warmup(iterations = 2, batchSize = 1024 * 1024, time = 200, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 8, batchSize = 1024 * 1024, time = 200, timeUnit = TimeUnit.MILLISECONDS)
	@BenchmarkMode(Mode.Throughput)
	public void bench() throws IOException
	{
		stream.write((byte) 1);
	}


	@TearDown(Level.Trial)
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void teardown()
	{
		tempFile.delete();
	}


	public static void main(String[] args) throws RunnerException
	{
		new Runner(new OptionsBuilder().include(OutputPerformance.class.getSimpleName()).build()).run();
	}
}
