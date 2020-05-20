package com.jerryshao.apinet.common;

import java.util.List;

public abstract class ImmutableList implements List {

	public static final ImmutableList EMPTY_LIST = new EmptyImmutableList();
	
}
