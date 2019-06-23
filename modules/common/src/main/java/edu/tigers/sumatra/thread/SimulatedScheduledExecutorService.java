/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.thread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.NotImplementedException;


/**
 * Fake a scheduled executor service by allowing to manually execute the task
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulatedScheduledExecutorService implements ScheduledExecutorService
{
	private boolean					shutdown		= false;
	private final List<Runnable>	runnables	= new ArrayList<>();
	
	
	@Override
	public void shutdown()
	{
		runnables.clear();
		shutdown = true;
	}
	
	
	@Override
	public List<Runnable> shutdownNow()
	{
		shutdown();
		shutdown = true;
		return new ArrayList<>(0);
	}
	
	
	@Override
	public boolean isShutdown()
	{
		return shutdown;
	}
	
	
	@Override
	public boolean isTerminated()
	{
		return shutdown;
	}
	
	
	@Override
	public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException
	{
		return true;
	}
	
	
	@Override
	public <T> Future<T> submit(final Callable<T> task)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public <T> Future<T> submit(final Runnable task, final T result)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public Future<?> submit(final Runnable task)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout,
			final TimeUnit unit)
			throws InterruptedException
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException,
			ExecutionException
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public void execute(final Runnable command)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit)
	{
		runnables.add(command);
		return new MyScheduledFuture<Object>(command);
	}
	
	
	@Override
	public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period,
			final TimeUnit unit)
	{
		runnables.add(command);
		return new MyScheduledFuture<Object>(command);
	}
	
	
	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay,
			final TimeUnit unit)
	{
		runnables.add(command);
		return new MyScheduledFuture<Object>(command);
	}
	
	private class MyScheduledFuture<T> implements ScheduledFuture<T>
	{
		private final Runnable	runnable;
		
		
		/**
		 * @param runnable
		 */
		public MyScheduledFuture(final Runnable runnable)
		{
			this.runnable = runnable;
		}
		
		
		@Override
		public long getDelay(final TimeUnit unit)
		{
			return 0;
		}
		
		
		@Override
		public int compareTo(final Delayed o)
		{
			return 0;
		}
		
		
		@Override
		public boolean cancel(final boolean mayInterruptIfRunning)
		{
			runnables.remove(runnable);
			return true;
		}
		
		
		@Override
		public boolean isCancelled()
		{
			return false;
		}
		
		
		@Override
		public boolean isDone()
		{
			return true;
		}
		
		
		@Override
		public T get() throws InterruptedException,
				ExecutionException
		{
			return null;
		}
		
		
		@Override
		public T get(final long timeout, final TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException
		{
			return null;
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = (prime * result) + getOuterType().hashCode();
			return result;
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			return false;
		}
		
		
		private SimulatedScheduledExecutorService getOuterType()
		{
			return SimulatedScheduledExecutorService.this;
		}
		
	}
	
	
	/**
	 * 
	 */
	public void runAll()
	{
		for (Runnable r : runnables)
		{
			r.run();
		}
	}
}
