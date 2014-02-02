package de.karlNet.genericForm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.karlNet.genericTable.dbhandler.DBHandler;

@Component
public class Validator {

	@Autowired
	private DBHandler dbHandler;

	private void getCheckConstraints() {

	}

	private void getUniqueConstraints() {

	}

	protected String buildSelectPart(Method methodToBeValidated,
			Object objectToBeValidated) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		String query = "select";
		String methodName = methodToBeValidated.getName();

		String columnName = methodName.replaceFirst("get", "").toLowerCase();
		Object value = methodToBeValidated.invoke(objectToBeValidated, null);
		query += " '" + value + "' as " + columnName;
		return query;
	}

	protected String buildUnionPart(Method methodToBeValidated,
			Object objectToBeValidated) {
		String unionPart = "union all select";
		String methodName = methodToBeValidated.getName();
		String columnName = methodName.replaceFirst("get", "").toLowerCase();
		unionPart += " " + columnName;
		unionPart += " from "
				+ objectToBeValidated.getClass().getSimpleName().toLowerCase();
		unionPart += " where 1!=1";
		return unionPart;
	}

	protected String buildWithPart(Method methodToBeValidated,
			Object objectToBeValidated)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		String methodName = methodToBeValidated.getName();
		String columnName = methodName.replaceFirst("get", "").toLowerCase();
		String withPart = "to_check_" + columnName + " as (";
		String selectPart = this.buildSelectPart(methodToBeValidated, objectToBeValidated);
		String unionPart = this.buildUnionPart(methodToBeValidated, objectToBeValidated);
		withPart += selectPart + " " + unionPart;
		withPart += ")";
		return withPart;
	}
	
	protected String buildCheckPart(Method methodToBeValidated,
			Object objectToBeValidated)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		String methodName = methodToBeValidated.getName();
		String columnName = methodName.replaceFirst("get", "").toLowerCase();
		String checkPart = "select count(*) from to_check_" + columnName + " where 1=1";
		
		return checkPart;
	}
	
	protected String buildWholeQuery(Object objectToBeValidated) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// dummy so we can directly union all without any special case f*ckup
		String query = "with dummy as (select 1)";
		for(Method method : objectToBeValidated.getClass().getDeclaredMethods()) {
			if(method.getName().startsWith("get")) {
				query+= ", " + this.buildWithPart(method, objectToBeValidated);
			}
		}
		// append checkPart
		// dummy so we can directly union all without any special case f*ckup
		query += "select null where 1!=1";
		for(Method method : objectToBeValidated.getClass().getDeclaredMethods()) {
			if(method.getName().startsWith("get")) {
				query+= " union all " + this.buildCheckPart(method, objectToBeValidated);
			}
		}
		return query;
	}

	public void validate(String viewName, Object objectToBeValidated) {

	}
}
