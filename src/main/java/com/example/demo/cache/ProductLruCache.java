package com.example.demo.cache;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProductLruCache<K, V> extends LinkedHashMap<K, V> {
	private final int capacity;

	public ProductLruCache() {
		this(25);
	}

	public ProductLruCache(int capacity) {
		super(capacity, 0.75f, true);
		this.capacity = capacity;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > capacity;
	}
}