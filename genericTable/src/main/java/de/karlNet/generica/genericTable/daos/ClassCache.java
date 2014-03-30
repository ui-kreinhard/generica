package de.karlNet.generica.genericTable.daos;

import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;

import org.springframework.stereotype.Component;

@Component
public class ClassCache {
	private int version = 0;
	private HashMap<String, Class<?>> classCacheMap = new HashMap<String, Class<?>>();

	public int getVersion() {
		return version;
	}

	
	public void putInCacache(String viewName, Class<?> defintion) {
		this.classCacheMap.put(viewName + "_" + version, defintion);
	}

	public Class<?> getFromCache(String className) {
		return this.classCacheMap.get(className + "_" + version);
	}

	public boolean isInCache(String className) {
		return this.classCacheMap.containsKey(className + "_" + version);
	}
	
	public void clearCache() {
		this.version++;
		this.classCacheMap.clear();
	}
	
	public Class<?> createBeanClass(final String className,
			final Map<String, Class<?>> properties) {
		
		final BeanGenerator beanGenerator = new BeanGenerator();

		beanGenerator.setNamingPolicy(new NamingPolicy() {
			public String getClassName(final String prefix,
					final String source, final Object key, final Predicate names) {
				return className + "_" + version;
			}
		});
		BeanGenerator.addProperties(beanGenerator, properties);
		return (Class<?>) beanGenerator.createClass();
	}
}
