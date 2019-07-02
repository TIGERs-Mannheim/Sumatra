package edu.tigers.sumatra.referee;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/**
 * A wrapper around the ssl-game-controller executable
 */
public class SslGameControllerProcess implements Runnable
{
	private final Logger log = Logger.getLogger(SslGameControllerProcess.class.getName());
	
	private final int gcUiPort;
	
	private Process process = null;
	private SslGameControllerClient client = null;
	private CountDownLatch clientLatch = new CountDownLatch(1);
	
	
	public SslGameControllerProcess(final int gcUiPort)
	{
		this.gcUiPort = gcUiPort;
	}
	
	
	private String locateModulesFolder() throws FileNotFoundException
	{
		StringBuilder prefix = new StringBuilder();
		for (int i = 0; i < 3; i++)
		{
			String path = prefix + "modules";
			if (new File(path).isDirectory())
			{
				return path;
			}
			prefix.append("../");
		}
		throw new FileNotFoundException("Could not locate modules folder");
	}
	
	
	@Override
	public void run()
	{
		Thread.currentThread().setName("ssl-game-controller");
		
		final String modulesFolder;
		try
		{
			modulesFolder = locateModulesFolder();
		} catch (FileNotFoundException e)
		{
			log.warn("No ssl-game-controller binary file found.", e);
			return;
		}
		
		String binaryFolder = modulesFolder + "/moduli-referee/target/ssl-game-controller";
		final File[] files = new File(binaryFolder).listFiles();
		if (files == null || files.length == 0)
		{
			log.error("No ssl-game-controller binary file found.");
			return;
		}
		Arrays.sort(files);
		
		File binaryFile = files[files.length - 1];
		
		if (files.length > 1)
		{
			log.info("Found multiple ssl-game-controller binaries. Choosing " + binaryFile);
		}
		
		try
		{
			if (!binaryFile.canExecute() && !binaryFile.setExecutable(true))
			{
				log.warn("Binary is not executable and could not be made executable.");
				return;
			}
			
			ProcessBuilder builder = new ProcessBuilder(binaryFile.getAbsolutePath(),
					"-address", "localhost:" + gcUiPort);
			builder.redirectErrorStream(true);
			builder.directory(Paths.get("").toAbsolutePath().toFile());
			process = builder.start();
			log.debug("game-controller process started");
			
			Scanner s = new Scanner(process.getInputStream());
			inputLoop(s);
			s.close();
		} catch (IOException e)
		{
			if (!"Stream closed".equals(e.getMessage()))
			{
				log.warn("Could not execute ssl-game-controller", e);
			}
		}
		if (process != null && !process.isAlive() && process.exitValue() != 0)
		{
			log.warn("game-controller has returned a non-zero exit code: " + process.exitValue());
		}
		log.debug("game-controller process thread finished");
	}
	
	
	private void inputLoop(final Scanner s)
	{
		while (s.hasNextLine())
		{
			String line = s.nextLine();
			if (line != null)
			{
				processLogLine(line);
			}
		}
	}
	
	
	private void processLogLine(final String line)
	{
		// Remove log date: 2018/10/29 10:00:10
		String truncatedLine = line.replaceFirst("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+:[0-9]+ ", "");
		log.debug(truncatedLine);
		
		if (truncatedLine.contains("UI is available at"))
		{
			createClient(truncatedLine);
		}
	}
	
	
	private void createClient(final String truncatedLine)
	{
		final Pattern pattern = Pattern.compile("localhost:([0-9]+)");
		final Matcher matcher = pattern.matcher(truncatedLine);
		if (matcher.find())
		{
			String port = matcher.group(1);
			final URI uri = URI.create("http://localhost:" + port + "/api/control");
			log.debug("Connecting to " + uri);
			client = new SslGameControllerClient(uri);
			try
			{
				// wait a moment to allow the controller to actually listen for connections
				Thread.sleep(100);
				// connection should be established within one second
				boolean connected = client.connectBlocking(1, TimeUnit.SECONDS);
				if (!connected)
				{
					log.warn("Timed out waiting to connect to the game-controller.");
					client = null;
				}
				clientLatch.countDown();
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		} else
		{
			log.error("Could not extract port from log line, where a port was expected.");
		}
	}
	
	
	public void stop()
	{
		if (process == null)
		{
			return;
		}
		
		process.destroy();
		try
		{
			if (!process.waitFor(1, TimeUnit.SECONDS))
			{
				log.warn("Process could not be stopped and must be killed");
				process.destroyForcibly();
			}
		} catch (InterruptedException e)
		{
			log.warn("Interrupted while waiting for the process to exit");
			Thread.currentThread().interrupt();
		}
		process = null;
	}
	
	
	public Optional<SslGameControllerClient> getClient()
	{
		return Optional.ofNullable(client);
	}
	
	
	/**
	 * @return a connected client, waiting if necessary until it is available and connected
	 */
	public Optional<SslGameControllerClient> getClientBlocking()
	{
		if (client != null)
		{
			return getClient();
		}
		try
		{
			final boolean latchTriggered = clientLatch.await(10, TimeUnit.SECONDS);
			if (!latchTriggered)
			{
				log.warn("Timed out waiting to get the client");
			}
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		return getClient();
	}
}
