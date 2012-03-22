package com.flotype.bridge;

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

	public static final int DEFAULT_PORT = -1;
	public static final String DEFAULT_HOST = "http://redirector.flotype.com";
	public static final BridgeEventHandler DEFAULT_EVENT_HANDLER = new BridgeEventHandler();
	public static final boolean DEFAULT_RECONNECT = true;
	public static int logLevel = 5;

	@SuppressWarnings("unchecked")
	protected static Map<String, Object> deserialize(Bridge bridge, byte[] json)
	throws JsonParseException, JsonMappingException, IOException {

		// Create object mapper
		ObjectMapper mapper = new ObjectMapper();

		// Return a request object parsed by mapper
		Map<String, Object> jsonObj =
			mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
		jsonObj = (Map<String, Object>) constructRefs(bridge, jsonObj);
		return jsonObj;
	}

	@SuppressWarnings("unchecked")
	public static Object constructRefs(Bridge bridge, Map<String, Object> theMap) {
		Object pathchain;
		if ((pathchain = theMap.get("ref")) != null) {
			return new Reference(bridge, (List<String>) pathchain, (List<String>) theMap.get("operations"));
		}

		for (Map.Entry<String, Object> entry : (theMap).entrySet()) {

			Object value = entry.getValue();

			if (value != null
					&& value instanceof HashMap) {
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
			if (value != null
					&& value instanceof HashMap) {
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
	protected static <T> T createProxy(InvocationHandler handler, Class<T> proxiedClass){
		return (T) java.lang.reflect.Proxy.newProxyInstance(proxiedClass.getClassLoader(),
                new Class[] { proxiedClass },
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

	protected static List<String> getMethods(Class<?> klass){
		Method[] methods = klass.getDeclaredMethods();
		List<String> methodNames = new ArrayList<String>();

		for(int i = 0; i < methods.length; i++) {
			methodNames.add(methods[i].getName());
		}
		return methodNames;
	}

	public static boolean contains(Object[] container,
			Object item) {
		for(int i = 0; i < container.length; i++){
			if(container[i].equals(item)){
				return true;
			}
		}
		return false;
	}
}
