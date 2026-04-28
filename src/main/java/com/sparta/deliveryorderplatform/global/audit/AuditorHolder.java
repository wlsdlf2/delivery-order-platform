package com.sparta.deliveryorderplatform.global.audit;

public class AuditorHolder {

	private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

	public static void set(String username) {
		HOLDER.set(username);
	}

	public static String get() {
		return HOLDER.get();
	}

	public static void clear() {
		HOLDER.remove();
	}
}
