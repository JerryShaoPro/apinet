package com.jerryshao.apinet.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jerryshao.apinet.common.ImmutableList;


public class ObjectsFactory {
	private ObjectsFactory() {	}
	
	/**
	 * ����������������Ϻ������Ϣ��������Ӧ��ʵ������<br>
	 * @see #createObject(Class)
	 * @param <T>
	 * @param classStrs ��������
	 * @param clazz �����Ϣ
	 * @return ʵ������
	 * @throws Exception 
	 */
	public static final <T> List<? extends T> createObjects(Collection<String> classStrs, Class<? extends T> clazz) throws Exception {
		Preconditions.checkNotNull(clazz);
		
		if (null == classStrs || classStrs.size() == 0) {
			return ImmutableList.EMPTY_LIST;
		}
		
		List<T> instances = new ArrayList<T>();
		for (String classStr : classStrs) {
			Class<? extends T> subClazz = (Class<? extends T>) Classes.getDefaultClassLoader().loadClass(classStr);
			instances.add(createObject(subClazz));
		}
		
		return instances;
	}
	
	/**
	 * �������������Ϣ���ϣ�������Ӧ��ʵ������<br>
	 * @see #createObject(Class)
	 * @param <T>
	 * @param classes ����Ϣ����
	 * @return ʵ������
	 * @throws Exception 
	 */
	public static final <T> List<? extends T> createObjects(Collection<Class<? extends T>> classes) throws Exception {
		if (null == classes || classes.size() == 0) {
			return ImmutableList.EMPTY_LIST;
		}
		
		List<T> instances = new ArrayList<T>();
		for (Class<? extends T> clazz : classes) {
			instances.add(createObject(clazz));
		}
		return instances;
	}
	
	/**
	 * ��������Ϣ��������Ӧ��ʵ��<br>
	 * �շ������ȳ���ͨ��û���κβ����Ĺ�����������ʵ����Ȼ����ʹ�þ�̬����get()������
	 * 
	 * @param <T>
	 * @param clazz ����Ϣ
	 * @return ʵ��
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public static final <T> T createObject(Class<? extends T> clazz) throws Exception {
		Preconditions.checkNotNull(clazz);
		
		Constructor constructor = null;
		try {
			constructor = clazz.getDeclaredConstructor(null);
		} catch (Exception ignore) {
		}
		
		if (constructor != null) {
			if (! constructor.isAccessible()) {
				constructor.setAccessible(true);
			}
			
			return (T) constructor.newInstance(null);
		} else {
			Method getInstanceMethod = clazz.getDeclaredMethod("get", null);
			if (getInstanceMethod != null && Modifier.isStatic(getInstanceMethod.getModifiers()) && clazz.isAssignableFrom(getInstanceMethod.getReturnType())) {
				return (T) getInstanceMethod.invoke(null, null);
			}
		}
		
		return null;
	}
	
	/**
	 * ��������������Ӧ��ʵ��
	 * @param classStr �����jerryshao.apinetapiserver.Apiserver
	 * @return ������Ӧ��ʵ��
	 * @throws ClassNotFoundException
	 */
	public static final Object createObject(String classStr) throws Exception {
		Preconditions.checkNotNull(classStr);
		
		Class clazz = Class.forName(classStr);
		return createObject(clazz);
	}
	
}
