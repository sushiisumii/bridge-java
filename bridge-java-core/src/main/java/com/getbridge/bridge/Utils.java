package com.getbridge.bridge;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class Utils<T> {

	public static final String DEFAULT_REDIRECTOR = "http://redirector.flotype.com";
	public static final BridgeEventHandler DEFAULT_EVENT_HANDLER = new BridgeEventHandler();
	public static final boolean DEFAULT_RECONNECT = true;
	public static final String DEFAULT_SECURE_REDIRECTOR = "https://redirector.flotype.com";
	public static int logLevel = 5;

	private static ObjectMapper readMapper = new ObjectMapper();

	private static Map<Class<?>, Class<?>> autoboxMap = new HashMap<Class<?>, Class<?>>();
	
	static {
		autoboxMap.put(byte.class, Byte.class);
		autoboxMap.put(Byte.class, byte.class);
		autoboxMap.put(short.class, Short.class);
		autoboxMap.put(Short.class, short.class);
		autoboxMap.put(int.class, Integer.class);
		autoboxMap.put(Integer.class, int.class);
		autoboxMap.put(long.class, Long.class);
		autoboxMap.put(Long.class, long.class);
		autoboxMap.put(double.class, Double.class);
		autoboxMap.put(Double.class, double.class);
		autoboxMap.put(boolean.class, Boolean.class);
		autoboxMap.put(Boolean.class, boolean.class);
	}
	
	@SuppressWarnings("unchecked")
	protected static Map<String, Object> deserialize(Bridge bridge, byte[] json)
			throws JsonParseException, JsonMappingException, IOException {

		// Return a request object parsed by mapper
		Map<String, Object> jsonObj = readMapper.readValue(json,
				new TypeReference<Map<String, Object>>() {
				});
		jsonObj = (Map<String, Object>) constructRefs(bridge, jsonObj);
		return jsonObj;
	}

	@SuppressWarnings("unchecked")
	public static Object constructRefs(Bridge bridge, Map<String, Object> theMap) {
		Object pathchain;
		if ((pathchain = theMap.get("ref")) != null) {
			return new Reference(bridge, (List<String>) pathchain,
					(List<String>) theMap.get("operations"));
		}

		for (Map.Entry<String, Object> entry : (theMap).entrySet()) {

			Object value = entry.getValue();

			if (value != null && value instanceof HashMap) {
				value = constructRefs(bridge, (Map<String, Object>) value);
			} else if (value != null && value instanceof ArrayList) {
				value = constructRefs(bridge, (List<Object>) value);
			}

			theMap.put(entry.getKey(), value);
		}

		return theMap;
	}

	@SuppressWarnings("unchecked")
	static Object constructRefs(Bridge bridge, List<Object> list) {

		int idx = 0;
		for (Object value : list) {
			if (value != null && value instanceof HashMap) {
				value = constructRefs(bridge, (Map<String, Object>) value);
			} else if (value != null && value instanceof ArrayList) {
				value = constructRefs(bridge, (List<Object>) value);
			}
			list.set(idx, value);
			idx++;
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	protected static <T> T createProxy(InvocationHandler handler,
			Class<T> proxiedClass) {
		return (T) java.lang.reflect.Proxy.newProxyInstance(
				proxiedClass.getClassLoader(), new Class[] { proxiedClass },
				handler);
	}

	protected static String generateRandomId() {
		return Long.toHexString(Double.doubleToLongBits(Math.random()));
	}

	protected static Object normalizeValue(Object value) {
		Class<?> klass = value.getClass();
		if (klass == Double.class || klass == Integer.class) {
			// All numbers are floats
			return ((Number) value).floatValue();
		} else {
			return value;
		}
	}

	protected static byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
				(byte) (value >>> 8), (byte) value };
	}

	protected static List<String> getMethods(Class<?> klass) {
		Map<Method, Boolean> methodMap = new HashMap<Method, Boolean>();
		for (Method m : klass.getMethods()) {
			methodMap.put(m, true);
		}

		Method[] methods = klass.getDeclaredMethods();
		List<String> methodNames = new ArrayList<String>();

		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			if (methodMap.get(m) != null && methodMap.get(m) == true) {
				methodNames.add(m.getName());
			}
		}
		return methodNames;
	}

	public static boolean isRemoteObject(Class<?> klass) {
		if(klass == BridgeRemoteObject.class) {
			return true;
		}
		
		Class[] interfaces = klass.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (interfaces[i].equals(BridgeRemoteObject.class)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static Class<?> autobox(Class<?> klass){
		return autoboxMap.get(klass);
	}

	public static Object defaultValueForPrimitive(Class<?> primitive) {
		boolean bool = false;
		int i = 0;
		float f = 0;
		double d = 0;
		byte b = 0x0;
		char c = 0;
		long l = 0;

		if (primitive.equals(boolean.class)) {
			return bool;
		} else if (primitive.equals(int.class)) {
			return i;
		} else if (primitive.equals(float.class)) {
			return f;
		} else if (primitive.equals(double.class)) {
			return d;
		} else if (primitive.equals(byte.class)) {
			return b;
		} else if (primitive.equals(char.class)) {
			return c;
		} else if (primitive.equals(long.class)) {
			return l;
		}

		return null;
	}
}
