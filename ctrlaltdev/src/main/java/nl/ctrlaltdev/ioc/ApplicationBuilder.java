package nl.ctrlaltdev.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ApplicationBuilder : an Inversion of Control / Dependency Injection based
 * application construction facility. Just throw in the building blocks of the
 * application and the ApplicationBuilder will try to resolve the dependencies.
 * 
 * @author E.Hooijmeijer / (C) 2003-2005 E.Hooijmeijer / Licence : LGPL 2.1
 */
public class ApplicationBuilder {

	/** interface to customize the behaviour of the ApplicationBuilder */
	public interface Configuration {
		/**
		 * @return true when the specified constructor may be used to construct
		 *         the type.
		 */
		public boolean isApplicableConstructor(Class<?> aType, Constructor<?> aConstructor);

		/** @return true when the specified interface is exposed to other classes */
		public boolean isInterfaceExposedBy(Class<?> anInterface, Class<?> aType);

		/**
		 * @return the name for the specified argument or null if there is no
		 *         name.
		 */
		public String getNameForConstructorArgument(Class<?> theType, int nr, Class<?> aType);
	}

	/** dummy - do nothing configuration */
	public static class DummyConfiguration implements Configuration {
		public boolean isApplicableConstructor(Class<?> aType, Constructor<?> c) {
			return true;
		}

		public String getNameForConstructorArgument(Class<?> theType, int nr, Class<?> aType) {
			return null;
		}

		public boolean isInterfaceExposedBy(Class<?> aType, Class<?> anInterface) {
			return true;
		}
	}

	/** thrown when the ApplicationBuilder cannot build the types. */
	public static class BuildException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2110305585840975895L;

		public BuildException(String msg) {
			super(msg);
		}

		public BuildException(String msg, Throwable t) {
			super(msg, t);
		}
	}

	/**
	 * sorts constructors in such a way that the constructor with the most
	 * arguments comes first.
	 */
	private static class ConstructorSorter implements Comparator<Constructor<?>> {
		public int compare(Constructor<?> c1, Constructor<?> c2) {
			return c2.getParameterTypes().length - c1.getParameterTypes().length;
		}
	}

	private Map<Class<?>, Object> interfaceRegistry = new HashMap<>();
	private Map<Class<?>, Object> typeRegistry = new HashMap<>();
	private Map<String, Object> nameRegistry = new HashMap<>();

	private Configuration myCFG;
	private ApplicationBuilder myParent;
	private ConstructorSorter myConstructorSorter = new ConstructorSorter();
	private List<String> log = new ArrayList<String>();

	/**
	 * Constructs a new ApplicationBuilder.
	 */
	public ApplicationBuilder() {
		myCFG = new DummyConfiguration();
		myParent = null;
	}

	/**
	 * Constructs a new ApplicationBuilder with the specified configuration
	 * 
	 * @param cfg
	 *            the configuration
	 */
	public ApplicationBuilder(Configuration cfg) {
		myCFG = cfg;
		myParent = null;
	}

	/**
	 * Constructs a new ApplicationBuilder which uses its parent builder for
	 * types it does not know about.
	 * 
	 * @param parent
	 *            the parent application builder.
	 */
	public ApplicationBuilder(ApplicationBuilder parent) {
		this();
		myParent = parent;
	}

	/**
	 * Constructs a new ApplicationBuilder which uses its parent builder for
	 * types it does not know about and a custom configuration.
	 * 
	 * @param parent
	 *            the parent application builder.
	 * @param cfg
	 *            the configuration
	 */
	public ApplicationBuilder(ApplicationBuilder parent, Configuration cfg) {
		this(cfg);
		myParent = parent;
	}

	/**
	 * returns the object of the specific type or implementing the specified
	 * interface depending on what the class object represents (a class or an
	 * interface).
	 * 
	 * @param type
	 *            the class or interface to get an implementation for.
	 * @return the implementation of the class or interface or null if it is not
	 *         known here.
	 */
	public Object get(Class<?> type) {
		Object result = null;
		//
		if (type.isInterface())
			result = interfaceRegistry.get(type);
		else
			result = typeRegistry.get(type);
		//
		if (result != null)
			return result;
		return (myParent == null ? null : myParent.get(type));
	}

	/**
	 * returns the object with the specific name.
	 * 
	 * @param name
	 *            the name of the instance you're looking for (or a class name)
	 * @return the implementation of the named type or null if it is not known
	 *         here.
	 */
	public Object get(String name) {
		Object result = null;
		//
		result = nameRegistry.get(name);
		if (result != null)
			return result;
		//
		return (myParent == null ? null : myParent.get(name));
	}

	public void register(Class<?> type, Object instance) {
		if (type.isInterface())
			interfaceRegistry.put(type, instance);
		else
			typeRegistry.put(type, instance);
	}

	public void register(Object instance) {
		if (instance == null)
			throw new NullPointerException("Cannot register a NULL type.");
		Class<?>[] types = getInterfaces(new Class<?>[] { instance.getClass() });
		for (int t = 0; t < types.length; t++) {
			register(types[t], instance);
		}
		register(instance.getClass(), instance);
	}

	/** extracts all or only allowed interfaces for a specific type */
	protected Class<?>[] getInterfaces(Class<?>[] types) {
		//
		List<Class<?>> all = new ArrayList<Class<?>>();
		for (int t = 0; t < types.length; t++) {
			Class<?>[] interfaces = types[t].getInterfaces();
			for (int y = 0; y < interfaces.length; y++) {
				if (myCFG.isInterfaceExposedBy(types[t], interfaces[y]))
					all.add(interfaces[y]);
			}
		}
		//
		return all.toArray(new Class<?>[all.size()]);
	}

	/**
	 * returns the applicable constructors for the type. Filtered and public
	 * constructors only. The constructors are sorted on argument size - more
	 * arguments first.
	 */
	protected Constructor<?>[] getApplicableConstructors(Class<?> type) throws BuildException {
		Constructor<?>[] con = type.getConstructors();
		if (con.length == 1)
			return con;
		List<Constructor<?>> tmp = new ArrayList<>();
		for (int t = 0; t < con.length; t++) {
			if (Modifier.isPublic(con[t].getModifiers())) {
				if (myCFG.isApplicableConstructor(type, con[t])) {
					tmp.add(con[t]);
				}
			}
		}
		Collections.sort(tmp, myConstructorSorter);
		//
		if (tmp.size() == 0)
			new BuildException(type.getName() + " has no applicable constructors.");
		//
		return tmp.toArray(new Constructor<?>[tmp.size()]);
	}

	/**
	 * resolves (constructor) dependencies, constructs and registers the
	 * instances by type.
	 * 
	 * @param types
	 *            the types to instantiate and register.
	 * @throws BuildException
	 *             if the construction of the types fails.
	 */
	public void build(Class<?>[] types) throws BuildException {
		String[] tmp = new String[types.length];
		build(tmp, types);
	}

	/**
	 * resolves (constructor) dependencies, constructs and registers the
	 * instances by type.
	 * 
	 * @param namedTypes
	 *            the names and types to instantiate and register.
	 * @throws BuildException
	 *             if the construction of the types fails.
	 */
	public void build(Map<String, Class<?>> namedTypes) throws BuildException {
		build(namedTypes.keySet().toArray(new String[namedTypes.size()]), namedTypes.values().toArray(new Class<?>[namedTypes.size()]));
	}

	/**
	 * resolves (constructor) dependencies, constructs and registers the
	 * instances by name or type. After a succesful invocation you may use the
	 * various get methods on this class to retrieve your instances. Also a
	 * construction log can be retrieved using getLogLines
	 * 
	 * @param names
	 *            the names the types should be registered under.
	 * @param types
	 *            the types to instantiate.
	 * @throws BuildException
	 *             if the construction of the types fails.
	 * @throws NullPointerException
	 *             if any of the arguments is null, or any of the elements
	 *             inside the types array is null (names may be null, the type
	 *             will then be registered under its interfaces and class name.
	 */
	public void build(String[] names, Class<?>[] types) throws BuildException {
		log.clear();

		if (names.length != types.length)
			throw new BuildException("Length of names and types do not match.");
		//
		// Determine maximum depth
		//
		int maxConstructorDepth = 0;
		for (int t = 0; t < types.length; t++) {
			if (types[t] == null)
				throw new NullPointerException("Cannot build with a NULL type.");
			int nr = getApplicableConstructors(types[t]).length;
			if (nr > maxConstructorDepth)
				maxConstructorDepth = nr;
		}

		boolean doneOne = false;
		boolean complete = false;
		int currentDepth = 0;
		while (!complete) {
			complete = true;
			doneOne = false;
			for (int t = 0; t < types.length; t++) {
				//
				// Check if this type already exists.
				//
				Object tmp = nameRegistry.get(names[t]);
				if (tmp == null)
					tmp = typeRegistry.get(types[t]);
				if (tmp == null)
					tmp = interfaceRegistry.get(types[t]);

				if (tmp == null) {
					//
					// No, so determine its dependencies.
					//
					complete = false;
					Constructor<?>[] appCon = getApplicableConstructors(types[t]);
					if (currentDepth >= appCon.length)
						continue;
					Constructor<?> c = appCon[currentDepth];

					Class<?>[] deps = c.getParameterTypes();
					Object[] imps = new Object[deps.length];
					//
					// Determine if the deps can be satisfied.
					//
					boolean foundAll = true;
					for (int z = 0; z < deps.length; z++) {
						String name = myCFG.getNameForConstructorArgument(types[t], z, deps[z]);
						if (name != null) {
							imps[z] = get(name);
						} else {
							imps[z] = get(deps[z]);
						}
						if (imps[z] == null) {
							log.add("Unable to construct " + types[t].getName() + " : missing parameter instance of type " + deps[z].getName());
							foundAll = false;
						}
					}
					//
					// Create the instance if all params are there.
					//
					if (foundAll) {
						Object instance = null;
						try {
							instance = c.newInstance(imps);
						} catch (InvocationTargetException x) {
							throw new BuildException("Unable to instantiate " + types[t].getName(), x.getTargetException());
						} catch (InstantiationException x) {
							throw new BuildException("Unable to instantiate " + types[t].getName(), x);
						} catch (IllegalAccessException x) {
							throw new BuildException("Unable to instantiate " + types[t].getName(), x);
						}
						log.add("Successfully instantiated " + c.getName() + " to " + instance);
						// Register the instances in the appropriate
						// location(s).
						if ((names[t] == null) || (names[t].length() == 0)) {
							Class<?>[] ifs = getInterfaces(new Class<?>[] { types[t] });
							for (int z = 0; z < ifs.length; z++) {
								interfaceRegistry.put(ifs[z], instance);
								log.add("Registered " + instance + " to interface " + ifs[z].getName());
							}
							typeRegistry.put(types[t], instance);
							log.add("Registered " + instance + " to class " + types[t].getName());
						} else {
							nameRegistry.put(names[t], instance);
							log.add("Registered " + instance + " to name " + names[t]);
						}
						//
						// Allow another complete cycle.
						//
						doneOne = true;
						currentDepth = 0;
					}
				}
			}
			if ((!doneOne) && (!complete) && (currentDepth == maxConstructorDepth))
				throw new BuildException("Unable to construct application.\n" + getLogLines());
			if (!doneOne)
				currentDepth++;
		}
		log.add("Done.");
	}

	/** returns the log lines of the latest build. */
	public String getLogLines() {
		StringBuffer sb = new StringBuffer();
		String[] tmp = log.toArray(new String[log.size()]);
		for (int t = 0; t < tmp.length; t++) {
			sb.append(tmp[t]);
			sb.append("\n");
		}
		return sb.toString();
	}

}
