package edu.tigers.sumatra.sim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.vision.AVisionFilter;


/**
 * The abstract simulator is used by both, the local and the remote simulator
 */
public abstract class ASumatraSimulator extends AVisionFilter implements Runnable
{
	private static final Logger log = Logger.getLogger(ASumatraSimulator.class.getName());
	
	protected final Collection<ISimulatorActionCallback> simulatorActionCallbacks = new ArrayList<>();
	protected final Collection<ISimulatorObserver> simulatorObservers = new ArrayList<>();
	private ExecutorService service = null;
	
	
	@Override
	public void startModule()
	{
		service = Executors.newSingleThreadExecutor(new NamedThreadFactory("SumatraSimulator"));
		service.execute(this);
	}
	
	
	@Override
	public void stopModule()
	{
		service.shutdownNow();
		try
		{
			boolean terminated = service.awaitTermination(1, TimeUnit.SECONDS);
			if (!terminated)
			{
				log.error("Could not terminate simulator");
			}
		} catch (InterruptedException e)
		{
			log.error("Interrupted while awaiting termination", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	public void addSimulatorActionCallback(ISimulatorActionCallback cb)
	{
		simulatorActionCallbacks.add(cb);
	}
	
	
	public void removeSimulatorActionCallback(ISimulatorActionCallback cb)
	{
		simulatorActionCallbacks.remove(cb);
	}
	
	
	public void addSimulatorObserver(ISimulatorObserver cb)
	{
		simulatorObservers.add(cb);
	}
	
	
	public void removeSimulatorObserver(ISimulatorObserver cb)
	{
		simulatorObservers.remove(cb);
	}
}
