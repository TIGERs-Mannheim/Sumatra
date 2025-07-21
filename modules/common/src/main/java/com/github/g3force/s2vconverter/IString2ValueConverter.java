package com.github.g3force.s2vconverter;

import java.util.List;


/**
 * Interface for string to value converters.
 */
public interface IString2ValueConverter
{
	/**
	 * @param clazz the implementing class
	 * @return true, if the class can be handled by this converter
	 */
	boolean supportedClass(Class<?> clazz);


	/**
	 * Parse given String-value according to implementation of this parameter
	 *
	 * @param impl  the implementing class
	 * @param value the value encoded as String
	 * @return an instance of type impl with the given value
	 */
	Object parseString(final Class<?> impl, final String value);

	/**
	 * Parse given String-value according to implementation of this parameter (type-safe)
	 *
	 * @param impl  the implementing class
	 * @param value the value encoded as String
	 * @param <T>   the implementation type
	 * @return an instance of type impl with the given value
	 */
	@SuppressWarnings({ "unchecked" })
	default <T> T parse(final Class<T> impl, final String value)
	{
		return (T) parseString(impl, value);
	}

	/**
	 * Parse given String-value according to implementation of this parameter
	 *
	 * @param impl          the implementing class
	 * @param value         the value encoded as String
	 * @param genericsImpls possible generics implementations (e.g. for Lists)
	 * @return an instance of type impl with the given value
	 */
	default Object parseString(
			final Class<?> impl,
			final List<Class<?>> genericsImpls,
			final String value)
	{
		return null;
	}

	/**
	 * Parse given String-value according to implementation of this parameter
	 *
	 * @param impl          the implementing class
	 * @param value         the value encoded as String
	 * @param genericsImpls possible generics implementations (e.g. for Lists)
	 * @param <T>           the implementation type
	 * @return an instance of type impl with the given value
	 */
	@SuppressWarnings({ "unchecked" })
	default <T> T parse(
			final Class<T> impl,
			final List<Class<?>> genericsImpls,
			final String value)
	{
		return (T) parseString(impl, genericsImpls, value);
	}

	/**
	 * Convert an object instance to a persistable and parsable String value
	 *
	 * @param impl  the implementing class
	 * @param value the object instance to convert
	 * @return a String representing the value
	 */
	String toString(final Class<?> impl, final Object value);
}
