package com.flotype.bridge.kitchensink;

import java.util.List;
import java.util.Map;

import com.flotype.bridge.ServiceClient;

public interface SomeServiceClient extends ServiceClient {
	public void someFn(Integer a, Float b, String c, boolean d, Object e, List f, Map h);
	public void someFn(Object a, Object b);
}