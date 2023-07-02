/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;

import edu.tigers.sumatra.process.ProcessKiller;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


/**
 * A wrapper around the ssl-game-controller executable
 */
@Log4j2
public class SslGameControllerProcess implements Runnable
{
	private static final String BINARY_NAME = "ssl-game-controller";
	private static final Path BINARY_DIR = Paths.get("data");
	private static final File BINARY_FILE = BINARY_DIR.resolve(BINARY_NAME).toFile();

	private final ProcessKiller processKiller = new ProcessKiller();

	@Getter
	private final int gcUiPort;
	private final String publishAddress;
	private final String timeAcquisitionMode;
	private final Thread shutdownHook = new Thread(this::stop);

	@Setter
	private boolean useSystemBinary = false;

	private Process process = null;


	public SslGameControllerProcess(int gcUiPort, String publishAddress, String timeAcquisitionMode)
	{
		this.gcUiPort = gcUiPort;
		this.publishAddress = publishAddress;
		this.timeAcquisitionMode = timeAcquisitionMode;
	}


	public void killAllRunningProcesses()
	{
		try
		{
			processKiller.killProcess(BINARY_NAME);
		} catch (IOException e)
		{
			log.error("Failed to kill running GC processes", e);
		}
	}


	@Override
	public void run()
	{
		Thread.currentThread().setName(BINARY_NAME);

		if (!setupBinary())
		{
			return;
		}

		Runtime.getRuntime().addShutdownHook(shutdownHook);

		Path engineConfig = Path.of("config", "engine.yaml");
		Path engineConfigDefault = Path.of("config", "engine-default.yaml");
		if (!Files.exists(engineConfig) && Files.exists(engineConfigDefault))
		{
			log.info("Initialize engine.yaml with engine-default.yaml");
			try
			{
				Files.copy(engineConfigDefault, engineConfig);
			} catch (IOException e)
			{
				log.warn("Could not copy default engine config", e);
			}
		}

		try
		{
			log.debug("Starting with: {} {} {}", gcUiPort, timeAcquisitionMode, publishAddress);
			List<String> command = new ArrayList<>();
			command.add(BINARY_FILE.getAbsolutePath());
			command.add("-address");
			command.add(":" + gcUiPort);
			command.add("-timeAcquisitionMode");
			command.add(timeAcquisitionMode);
			if (!publishAddress.isBlank())
			{
				command.add("-publishAddress");
				command.add(publishAddress);
			}

			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			builder.directory(Paths.get("").toAbsolutePath().toFile());
			Process gcProcess = builder.start();
			process = gcProcess;
			log.debug("game-controller process started");

			Scanner s = new Scanner(gcProcess.getInputStream());
			inputLoop(s);
			s.close();
		} catch (IOException e)
		{
			if (!"Stream closed".equals(e.getMessage()))
			{
				log.warn("Could not execute ssl-game-controller", e);
			}
		}
		// thread safe local variable
		Process p = process;
		if (p != null && !p.isAlive() && p.exitValue() != 0)
		{
			log.warn("game-controller has returned a non-zero exit code: {}", p.exitValue());
		}
		log.debug("game-controller process thread finished");
		Runtime.getRuntime().removeShutdownHook(shutdownHook);
	}


	private Optional<File> findBinaryInPath()
	{
		return Arrays.stream(System.getenv("PATH").split("[:;]"))
				.map(Path::of)
				.map(p -> p.resolve(BINARY_NAME).toFile())
				.filter(File::exists)
				.findFirst();
	}


	@SneakyThrows
	private boolean setupBinary()
	{
		if (BINARY_FILE.exists())
		{
			try
			{
				Files.delete(BINARY_FILE.toPath());

			} catch (IOException e)
			{
				log.warn("Could not delete existing binary: {}", BINARY_FILE, e);
				return false;
			}
		}

		File binaryDir = BINARY_DIR.toFile();
		if (binaryDir.mkdirs())
		{
			log.info("Binary dir created: {}", binaryDir);
		}
		if (!writeResourceToFile(BINARY_FILE))
		{
			return false;
		}
		BINARY_FILE.deleteOnExit();
		if (!BINARY_FILE.canExecute() && !BINARY_FILE.setExecutable(true))
		{
			log.warn("Binary is not executable and could not be made executable: {}", BINARY_FILE);
			return false;
		}
		return true;
	}


	private InputStream getBinaryInputStream()
	{
		if (useSystemBinary)
		{
			return findBinaryInPath().map(file -> {
				log.debug("Using system-installed ssl-game-controller: {}", file);
				try
				{
					return new FileInputStream(file);
				} catch (FileNotFoundException e)
				{
					log.warn("Could not find ssl-game-controller binary: {}", file);
					return null;
				}
			}).orElse(null);
		}
		return ClassLoader.getSystemClassLoader().getResourceAsStream(BINARY_NAME);
	}


	private boolean writeResourceToFile(File targetFile)
	{
		try (InputStream in = getBinaryInputStream())
		{
			if (in == null)
			{
				log.warn("Could not find binary");
				return false;
			}

			try (FileOutputStream out = new FileOutputStream(targetFile))
			{
				IOUtils.copy(in, out);
			}
			return true;
		} catch (IOException e)
		{
			log.warn("Could not copy binary to temporary file", e);
		}
		return false;
	}


	private void inputLoop(final Scanner s)
	{
		while (s.hasNextLine())
		{
			String line = s.nextLine();
			if (line != null)
			{
				log.debug("GC: {}", line);
			}
		}
	}


	public void stop()
	{
		Process gcProcess = process;
		process = null;
		if (gcProcess == null)
		{
			return;
		}

		gcProcess.destroy();
		try
		{
			if (!gcProcess.waitFor(1, TimeUnit.SECONDS))
			{
				log.warn("Process could not be stopped and must be killed");
				gcProcess.destroyForcibly();
			}
		} catch (InterruptedException e)
		{
			log.warn("Interrupted while waiting for the process to exit");
			Thread.currentThread().interrupt();
		}
	}
}
