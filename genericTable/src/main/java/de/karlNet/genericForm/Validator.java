package de.karlNet.genericForm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.karlNet.genericTable.daos.DataDAO;
import de.karlNet.genericTable.dbhandler.DBHandler;
import javax.annotation.*;

@Component
public class Validator {

	@PostConstruct
	public void init() throws Exception {
		this.getCheckConstraints();
	}

	@Autowired
	private DataDAO dataDAO;

	private HashMap<String, check_constraints> hashMap = new HashMap<String, check_constraints>();

	private String createHashtableIdentifier(String tableName, String columnName) {
		return tableName.toLowerCase() + "_" + columnName.toLowerCase();
	}

	private String createHashtableIdentifier(check_constraints check_Constraint) {
		return this.createHashtableIdentifier(check_Constraint.getTable_name(),
				check_Constraint.getColumn_name());
	}

	private void getCheckConstraints() throws Exception {
		List<?> readOutViewOrTable = this.dataDAO.readOutViewOrTable(
				"check_constraints", check_constraints.class);
		this.hashMap = new HashMap<String, check_constraints>();
		for (Object object : readOutViewOrTable) {
			check_constraints check_Constraint = (check_constraints) object;
			this.hashMap.put(this.createHashtableIdentifier(check_Constraint),
					check_Constraint);
		}
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
			Object objectToBeValidated) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		String methodName = methodToBeValidated.getName();
		String columnName = methodName.replaceFirst("get", "").toLowerCase();
		String withPart = "to_check_" + columnName + " as (";
		String selectPart = this.buildSelectPart(methodToBeValidated,
				objectToBeValidated);
		String unionPart = this.buildUnionPart(methodToBeValidated,
				objectToBeValidated);
		withPart += selectPart + " " + unionPart;
		withPart += ")";
		return withPart;
	}

	protected String buildCheckPart(Method methodToBeValidated,
			Object objectToBeValidated) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		String methodName = methodToBeValidated.getName();
		String columnName = methodName.replaceFirst("get", "").toLowerCase();
		String tableName = objectToBeValidated.getClass().getSimpleName().toLowerCase();
		String checkPart = "select count(*)::int, " + "'" + tableName + "'," + "'" + columnName + "'" + " from to_check_" + columnName
				+ " where 1=1";
		String createHashtableIdentifier = this.createHashtableIdentifier(
				objectToBeValidated.getClass().getSimpleName().toLowerCase(),
				columnName.toLowerCase());
		if (this.hashMap.containsKey(createHashtableIdentifier)) {
			checkPart += " AND "
					+ this.hashMap.get(createHashtableIdentifier)
							.getCheck_clause();
		}
		return checkPart;
	}

	protected String buildWherePart(Method methodToBeValidated,
			Object objectToBeValidated) {
		String ret = "";
		return ret;
	}

	protected String buildWholeQuery(Object objectToBeValidated)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		// dummy so we can directly union all without any special case f*ckup
		String query = "with dummy as (select 1)";
		for (Method method : objectToBeValidated.getClass()
				.getDeclaredMethods()) {
			String methodName = method.getName();
			String columnName = methodName.replaceFirst("get", "")
					.toLowerCase();

			String createHashtableIdentifier = this.createHashtableIdentifier(
					objectToBeValidated.getClass().getSimpleName()
							.toLowerCase(), columnName.toLowerCase());
			if (this.hashMap.containsKey(createHashtableIdentifier)) {
				if (method.getName().startsWith("get")) {
					query += ", "
							+ this.buildWithPart(method, objectToBeValidated);
				}
			}
		}
		// append checkPart
		// dummy so we can directly union all without any special case f*ckup
		query += "select null as is_valid, null as table_name, null as column_name where 1!=?";
		for (Method method : objectToBeValidated.getClass()
				.getDeclaredMethods()) {
			String methodName = method.getName();
			String columnName = methodName.replaceFirst("get", "")
					.toLowerCase();

			String createHashtableIdentifier = this.createHashtableIdentifier(
					objectToBeValidated.getClass().getSimpleName()
							.toLowerCase(), columnName.toLowerCase());
			if (this.hashMap.containsKey(createHashtableIdentifier)) {
				if (method.getName().startsWith("get")) {
					query += " union all "
							+ this.buildCheckPart(method, objectToBeValidated);
				}
			}
		}
		return query;
	}

	public List<ValidationResult> validate(Object objectToBeValidated) throws SQLException, Exception {
		String queryToBeExecuted = this.buildWholeQuery(objectToBeValidated);
		List uncastedValidationResults = this.dataDAO.readOutViewOrTable(ValidationResult.class,
				null, null, 0, 5000, null, queryToBeExecuted);
		List<ValidationResult> validationResults = new ArrayList<ValidationResult>(uncastedValidationResults);
		return validationResults;
	}
}
