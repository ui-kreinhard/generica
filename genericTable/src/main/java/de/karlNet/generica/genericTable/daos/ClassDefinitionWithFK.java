package de.karlNet.generica.genericTable.daos;

import java.util.HashMap;
import java.util.List;

public class ClassDefinitionWithFK {
	private Class<?> classDefinition;
	private HashMap<String, List> valueMap = new HashMap<String, List>();
	
	
	public ClassDefinitionWithFK(Class<?> classDefinition) {
		this.classDefinition = classDefinition;
	}

	public Class<?> getClassDefinition() {
		return classDefinition;
	}
	
	public boolean isFK(String property) {
		property = property.toLowerCase();
		return this.valueMap.containsKey(property);
	}
	
	public void addSelectionValues(String property, List values) {
		assert(values!=null);
		property = property.toLowerCase();
		this.valueMap.put(property, values);
	}
	
	public List getSelectionValues(String property) {
		property = property.toLowerCase();
		return this.valueMap.get(property);
	}
}
