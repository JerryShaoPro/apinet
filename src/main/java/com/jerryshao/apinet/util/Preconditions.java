package com.jerryshao.apinet.util;

public final class Preconditions {
	private Preconditions() {
	}
	
    public static <T> T checkNotNull(T reference) {
        if (null == reference) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (null == reference) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }
}
