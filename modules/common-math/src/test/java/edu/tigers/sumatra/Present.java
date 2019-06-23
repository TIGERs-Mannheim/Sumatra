/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra;

import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;


/**
 * Matcher class to evaluate functions that return optional values. It has two static methods which can either target
 * empty or non-empty optionals. If a non-empty optional is to be expected, a second matcher can be provided which
 * matches the value that is contained in the optional.
 *
 * @param <T> The type of the value which is contained in the optional to be tested
 * @author Lukas Magel
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Present<T> extends TypeSafeMatcher<Optional<T>>
{
	
	private final MatcherMode	mode;
	private final Matcher<T>	presentMatcher;
	
	
	private Present(MatcherMode mode, Matcher<T> presentMatcher)
	{
		this.mode = mode;
		this.presentMatcher = presentMatcher;
	}
	
	
	/**
	 * Returns a matcher instance that matches empty optionals.
	 * 
	 * @param <E>
	 *           The type of the optional value
	 * @return
	 * 			A new matcher instance
	 */
	@Factory
	public static <E> Present<E> isNotPresent()
	{
		return new Present<>(MatcherMode.NOT_PRESENT, null);
	}
	
	
	/**
	 * Returns a matcher instance that matches non-empty optionals. If a value is present, it is passed on to the
	 * {@code presentMatcher}, which is provided as argument, for further matching.
	 *
	 * @param <E>
	 *           The type of the optional value
	 * @param presentMatcher
	 *           The matcher to further evaluate the value if one is present in the optional
	 * @return
	 * 			A new matcher instance
	 */
	@Factory
	public static <E> Present<E> isPresentAnd(Matcher<E> presentMatcher)
	{
		return new Present<>(MatcherMode.PRESENT, presentMatcher);
	}
	
	
	@Override
	public boolean matchesSafely(final Optional<T> optItem)
	{
		switch (mode)
		{
			case PRESENT:
				return optItem.map(presentMatcher::matches).orElse(false);
			case NOT_PRESENT:
				return !optItem.isPresent();
			default:
				throw new IllegalArgumentException("Please add enum value to switch case: " + mode);
		}
	}
	
	
	@Override
	public void describeTo(final Description description)
	{
		switch (mode)
		{
			case PRESENT:
				description.appendText("is present and ")
						.appendDescriptionOf(presentMatcher);
				break;
			case NOT_PRESENT:
				description.appendText("is not present");
				break;
			default:
				throw new IllegalArgumentException("Please add enum value to switch case: " + mode);
		}
	}
	
	
	@Override
	public void describeMismatch(final Object item, final Description description)
	{
		if (!(item instanceof Optional))
		{
			super.describeMismatch(item, description);
		} else
		{
			Optional<?> optItem = (Optional<?>) item;
			switch (mode)
			{
				case PRESENT:
					describePresentMismatch(description, optItem);
					break;
				case NOT_PRESENT:
					describeNonPresentMismatch(description);
				default:
					throw new IllegalArgumentException("Please add enum value to switch case: " + mode);
			}
		}
	}
	
	
	private void describePresentMismatch(Description description, Optional<?> item)
	{
		if (item.isPresent())
		{
			presentMatcher.describeMismatch(item, description);
		} else
		{
			description.appendText("The optional was expected to contain a value");
		}
	}
	
	
	private void describeNonPresentMismatch(Description description)
	{
		description.appendText("The optional was expected to contain a value");
	}
	
	private enum MatcherMode
	{
		PRESENT,
		NOT_PRESENT
	}
}
