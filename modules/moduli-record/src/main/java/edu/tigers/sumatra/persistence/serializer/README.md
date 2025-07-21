# Generic object serializer

The generic object serializer is capable of (de)serializing most objects into an compact binary format.
All objects can be serialized with this serializer, with the following exceptions:

- Record classes that are not accessible to the serializer (e.g. not public)
- Classes that track state outside of the heap (e.g. classes themself, reflection fields, files)
  without specialized serializers handling these classes.
- Cyclically referenced objects


## Generic objects

Objects and records are serialized by iterating over each nonstatic, nontransient field, serializing those sequentially.
As reflection access to objects can be limited due to Java modules (java.base being the most important),
Unsafe is used instead of reflection on regular objects (unavailable for records).
To identify the type of object (and differentiate null objects) regular objects (excluding primitives) are prefixed with the object type as integer.

The GenericSerializer is capable of serializing any type of object and is therefore the main interface for any access.
ObjectSerializer and RecordSerializer are serializers for generic classes.
Other serializers like the ArraySerializer, CollectionSerializer, EnumMapSerializer, EnumSerializer, MapSerializer or StringSerializer
are optimizing and/or enabling the serialization of special object types.

For successful deserialization the information about object type ids and object structure is serialized in a separate metadata file.
This metadata file just consists of consecutively serialized serializers to be able to reconstruct the class structure at the time of serialization.
The object structure of the metadata file is static, as such each (nontransient) field change to existing serializers needs to change the version at the start of the metadata file.
The static serializer serializer metadata (and metadata serializer itself) is implemented in the GenericSerializer.

## Primitives

Primitives (boolean, char, byte, short, int, long, float, double) are mostly serialized by their little-endian representation, with the following exceptions for data size reasons:

- char, short and int are serialized in a variable length format similar to the Minecraft protocol VarInt format.
- doubles are serialized as floats.

Accurate information regarding Object serialization can be found in the MappedDataOutputStream.
