# configurable

Simply save and load field values to/from a file by adding an annotation to a field.

## Behavior

* class is registered
* Registration is looking for existing config file
* if found: apply all stored configs to all classes
* values are converted from String to Object and vice versa (see String2ValueConverter)

## Limitations

* fields must not be final

## Usage

Here are some hints, how to use the lib. Refer to ConfigRegistration.java for more info.

### Annotate a field:

```java

@Configurable(comment = "Document this field", defValue = "false", spezis = { "TYPE1", "TYPE2" })
private static boolean fieldBool;
```

options:

* comment: document the field
* defValue: default value if no value is stored yet. Optional for static fields, but recommended
* spezis: 0 or more "specializations". Makes it possible to store different values at the same time
* defValueSpezis: if you use spezis, use this instead of defValue
* category: force config category. This is useful if you have muliple different categories (files) in one class.

### Register a class:

```java
<fields with@Configurable:>


static
{
	ConfigRegistration.registerClass("yourCategory", < your class>.class);
}

<no@Configurables!!>
```

### Register a callback

```java
ConfigRegistration.registerConfigurableCallback("<your category>",new IConfigObserver()
{
	@Override
	public void afterApply (IConfigClient configClient)
	{
		// called after all values were applied.
	}
});
```

### Apply config and spezis

If you have instance (non-static) fields, you have to apply the config after instantiation.
If you want to apply a different spezi, you have to do so as well:

```java
ConfigRegistration.applySpezis(obj, cat, spezi);
```

### Save values to file

```java
ConfigRegistration.save("<yourCategory>");
```

Files are saved as config/<yourCategory>.xml
