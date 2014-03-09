package de.karlNet.generica.genericForm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Property {
	private static final long serialVersionUID = 20120521L;

	private String name;
	private Object value;
	private Object target;
	private Method setter;
	private Method getter;
	private List selectItems;
	
	public Property(String name, Object target) {
		this.name = name;
		this.target = target;
		for (Method method : target.getClass().getDeclaredMethods()) {
			if(method.getName().startsWith("set")) {
				if(method.getName().toLowerCase().endsWith(name.toLowerCase())) {
					this.setter = method;
				}
			} else if(method.getName().startsWith("get")) {
				if(method.getName().toLowerCase().endsWith(name.toLowerCase())) {
					this.getter = method;
				}
			}
		}
	}
	
	public Property(String name, Object target, List selectItems) {
		this.name = name;
		this.target = target;
		for (Method method : target.getClass().getDeclaredMethods()) {
			if(method.getName().startsWith("set")) {
				if(method.getName().toLowerCase().endsWith(name.toLowerCase())) {
					setter = method;
				}
			}else if(method.getName().startsWith("get")) {
				if(method.getName().toLowerCase().endsWith(name.toLowerCase())) {
					this.getter = method;
				}
			}
		}
		this.selectItems = selectItems;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		try {
			return getter.invoke(this.target, null);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setValue(Object value) {
		try {
			setter.invoke(target, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public List getSelectItems() {
		return selectItems;
	}
}
