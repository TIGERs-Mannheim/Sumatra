/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import edu.tigers.sumatra.ids.EAiTeam;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class SerializerTest
{
	private final String teststring = "teststring";
	private final AllPrimitiveClass object = new AllPrimitiveClass(
			true, 'T', (byte) 2, (short) 3, 4, 5, 6, 7, teststring, null);
	private final AllPrimitiveRecord r = new AllPrimitiveRecord(
			true, 'T', (byte) 2, (short) 3, 4, 5, 6, 7, teststring, null);
	private final Object deletedClass = new Object();

	private final Object[] objectArray = new Object[] { object, r, null };
	private final boolean[] boolArray = new boolean[] { false, true };
	private final char[] charArray = new char[] { 'T', 's' };
	private final byte[] byteArray = new byte[] { Byte.MIN_VALUE, Byte.MAX_VALUE };
	private final short[] shortArray = new short[] { Short.MIN_VALUE, Short.MAX_VALUE };
	private final int[] intArray = new int[] { Integer.MIN_VALUE, Integer.MAX_VALUE };
	private final long[] longArray = new long[(int) MappedDataOutputStream.BUFFER_SIZE / Long.BYTES];
	private final float[] floatArray = new float[(int) MappedDataOutputStream.BUFFER_SIZE / Float.BYTES];
	private final double[] doubleArray = new double[] { Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
			Float.MIN_VALUE, Float.MAX_VALUE };

	private final Collection<Object> collection = new ArrayList<>(Arrays.stream(objectArray).toList());
	private final Map<EAiTeam, Object> enumMap = new EnumMap<>(EAiTeam.class);
	private final Map<String, Object> map = new HashMap<>();


	SerializerTest()
	{
		enumMap.put(EAiTeam.BLUE, object);
		map.put(teststring, object);
		longArray[0] = Long.MIN_VALUE;
		longArray[1] = Long.MAX_VALUE;
		floatArray[0] = Float.NaN;
		floatArray[1] = Float.NEGATIVE_INFINITY;
		floatArray[2] = Float.POSITIVE_INFINITY;
		floatArray[3] = Float.MIN_VALUE;
		floatArray[4] = Float.MAX_VALUE;
	}


	@Test
	void testSerializer() throws IOException
	{
		File metadata = File.createTempFile("sumatra-serializer-test", ".metadata");
		File db = File.createTempFile("sumatra-serializer-test", ".db");

		// Serialize
		//noinspection ResultOfMethodCallIgnored
		metadata.delete();
		GenericSerializer serializer = new GenericSerializer(metadata.toPath());
		MappedDataOutputStream stream = new MappedDataOutputStream(db.toPath());

		db.deleteOnExit();
		metadata.deleteOnExit();

		serializer.serialize(stream, teststring);
		serializer.serialize(stream, object);
		serializer.serialize(stream, r);
		serializer.serialize(stream, objectArray);
		serializer.serialize(stream, boolArray);
		serializer.serialize(stream, charArray);
		serializer.serialize(stream, byteArray);
		serializer.serialize(stream, shortArray);
		serializer.serialize(stream, intArray);
		serializer.serialize(stream, longArray);
		serializer.serialize(stream, floatArray);
		serializer.serialize(stream, doubleArray);
		serializer.serialize(stream, collection);
		serializer.serialize(stream, enumMap);
		serializer.serialize(stream, map);
		serializer.serialize(stream, null);
		serializer.serialize(stream, deletedClass);

		serializer.close();
		stream.close();

		deserialize(metadata.toPath(), db.toPath());
	}


	@Test
	void outdatedTest() throws IOException, URISyntaxException
	{
		ClassLoader loader = getClass().getClassLoader();

		deserialize(
				Paths.get(Objects.requireNonNull(loader.getResource("serializertest_outdated.metadata")).toURI()),
				Paths.get(Objects.requireNonNull(loader.getResource("serializertest_outdated.db")).toURI())
		);
	}


	private void deserialize(Path metadata, Path db) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate((int) db.toFile().length());
		try (FileChannel channel = FileChannel.open(db, StandardOpenOption.READ))
		{
			channel.read(buffer);
		}
		buffer.flip();

		try (GenericSerializer serializer = new GenericSerializer(metadata))
		{
			assertEquals(teststring, serializer.deserialize(buffer));
			assertEquals(object, serializer.deserialize(buffer));
			assertEquals(r, serializer.deserialize(buffer));
			assertArrayEquals(objectArray, (Object[]) serializer.deserialize(buffer));
			assertArrayEquals(boolArray, (boolean[]) serializer.deserialize(buffer));
			assertArrayEquals(charArray, (char[]) serializer.deserialize(buffer));
			assertArrayEquals(byteArray, (byte[]) serializer.deserialize(buffer));
			assertArrayEquals(shortArray, (short[]) serializer.deserialize(buffer));
			assertArrayEquals(intArray, (int[]) serializer.deserialize(buffer));
			assertArrayEquals(longArray, (long[]) serializer.deserialize(buffer));
			assertArrayEquals(floatArray, (float[]) serializer.deserialize(buffer));
			assertArrayEquals(doubleArray, (double[]) serializer.deserialize(buffer));
			assertEquals(collection, serializer.deserialize(buffer));
			assertEquals(enumMap, serializer.deserialize(buffer));
			assertEquals(map, serializer.deserialize(buffer));
			assertNull(serializer.deserialize(buffer));
			assertEquals(Object.class, serializer.deserialize(buffer).getClass());
		}
	}


	@EqualsAndHashCode
	@AllArgsConstructor
	private static class AllPrimitiveClass
	{
		private boolean bool;
		private char c;
		private byte b;
		private short s;
		private int i;
		private long l;
		private float f;
		private double d;
		private Object o;
		private Object n;
	}

	// Access cannot be private, otherwise the record constructor is not publicly callable
	public record AllPrimitiveRecord(boolean bool, char c, byte b, short s, int i, long l, float f, double d, Object o,
	                                 Object n) {}
}
