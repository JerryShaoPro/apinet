package com.jerryshao.apinet.common;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


final class EmptyImmutableList extends ImmutableList {
	public boolean add(Object e) {
		throw new AssertionError("unwriteable");
	}

	public void add(int index, Object element) {
		throw new AssertionError("unwriteable");
	}

	public boolean addAll(Collection c) {
		throw new AssertionError("unwriteable");
	}

	public boolean addAll(int index, Collection c) {
		throw new AssertionError("unwriteable");
	}

	public void clear() {
	}

	public boolean contains(Object o) {
		return false;
	}

	public boolean containsAll(Collection c) {
		return false;
	}

	public Object get(int index) {
		throw new AssertionError("unreachable");
	}

	public int indexOf(Object o) {
		return -1;
	}

	public boolean isEmpty() {
		return true;
	}

	public Iterator iterator() {
		return Collections.emptyList().iterator();
	}

	public int lastIndexOf(Object o) {
		return -1;
	}

	public ListIterator listIterator() {
		return Collections.emptyList().listIterator();
	}

	public ListIterator listIterator(int index) {
		return Collections.emptyList().listIterator();
	}

	public boolean remove(Object o) {
		throw new AssertionError("unwriteable");
	}

	public Object remove(int index) {
		throw new AssertionError("unwriteable");
	}

	public boolean removeAll(Collection c) {
		throw new AssertionError("unwriteable");
	}

	public boolean retainAll(Collection c) {
		return false;
	}

	public Object set(int index, Object element) {
		throw new AssertionError("unwriteable");
	}

	public int size() {
		return 0;
	}

	public List subList(int fromIndex, int toIndex) {
		return Collections.emptyList();
	}

	private static final Object[] EMPTY_ARRAY = new Object[0];
	
	public Object[] toArray() {
		return EMPTY_ARRAY;
	}

	public Object[] toArray(Object[] a) {
		if (a.length > 0) {
			a[0] = null;
		}
		return a;
	}
}
