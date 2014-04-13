package de.karlNet.generica.genericForm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;

import de.karlNet.generica.genericTable.daos.ClassCache;
import de.karlNet.generica.genericTable.daos.DataDAO;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Validator {
	@Autowired
	private ClassCache classCache;

	@PostConstruct
	public void init() throws Exception {
		this.getCheckConstraints();
	}

	@Autowired
	private DataDAO dataDAO;

	private HashMap<String, check_constraints> hashMap = new HashMap<String, check_constraints>();

	private String createHashtableIdentifier(String tableName, String columnName) {
		return this.createHashtableIdentifier(tableName, columnName, null);
	}

	private String createHashtableIdentifier(String tableName,
			String columnName, Integer version) {
		String versionString = "";
		if (version != null) {
			versionString = version + "_";
		}
		return tableName.toLowerCase() + "_" + versionString
				+ columnName.toLowerCase();
	}

	private String createHashtableIdentifier(check_constraints check_Constraint) {
		return this
				.createHashtableIdentifier(check_Constraint.getTable_name(),
						check_Constraint.getColumn_name(),
						this.classCache.getVersion());
	}

	private void getCheckConstraints() throws Exception {
		List<?> readOutViewOrTable = this.dataDAO.readOutViewOrTable(
				"check_constraints", check_constraints.class);
		this.hashMap = new HashMap<String, check_constraints>();
		for (Object object : readOutViewOrTable) {
			check_constraints check_Constraint = (check_constraints) object;
			String createHashtableIdentifier = this
					.createHashtableIdentifier(check_Constraint);
			System.out.println(createHashtableIdentifier);
			this.hashMap.put(createHashtableIdentifier, check_Constraint);
		}
	}

	private void getUniqueConstraints() {

	}

	protected String buildColumnValuePart(Method methodToBeValidated,
			Object objectToBeValidated) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		String query = ", ";
		String methodName = methodToBeValidated.getName();

		String columnName = methodName.replaceFirst("get", "").toLowerCase();
		Object value = methodToBeValidated.invoke(objectToBeValidated, null);
		if(value==null) {
			query += " null ";
		} else {
			query += " '" + value + "' ";
		}
		query += " as " + columnName;
		return query;
	}

	protected String buildSelectPart(Method methodToBeValidated,
			Object objectToBeValidated) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		String query = "select";
		String methodName = methodToBeValidated.getName();

		String columnName = methodName.replaceFirst("get", "").toLowerCase();
		Object value = methodToBeValidated.invoke(objectToBeValidated, null);
		if(value==null) {
			query += " null ";
		} else {
			query += " '" + value + "' ";
		}
		query += " as " + columnName;
		return query;
	}

	protected String buildUnionPart(Method methodToBeValidated,
			Object objectToBeValidated) {
		String unionPart = "union all select";
		String methodName = methodToBeValidated.getName();
		String columnName = methodName.replaceFirst("get", "").toLowerCase();
		unionPart += " " + columnName;
		unionPart += " from "
				+ objectToBeValidated.getClass().getSimpleName().toLowerCase()
						.replaceAll("_resolved", "")
						.replaceAll("_" + this.classCache.getVersion(), "");
		unionPart += " where 1!=1";
		return unionPart;
	}

	protected String buildWithPart(Object objectToBeValidated)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		String query = ", to_check as ( select 'dummy' ";
		for (Method method : objectToBeValidated.getClass()
				.getDeclaredMethods()) {
			if (method.getName().startsWith("get")) {
				query += this.buildColumnValuePart(method, objectToBeValidated);
			}
		}
		String tableName = objectToBeValidated.getClass().getSimpleName().toLowerCase().replaceAll("_resolved","").replaceAll( "_" + this.classCache.getVersion(), "");
		query += " union all " +
				"SELECT 'dummy'" ;

		// okay now append a union part so we have correct types
		for (Method method : objectToBeValidated.getClass()
				.getDeclaredMethods()) {
			if (method.getName().startsWith("get")) {
				String methodName = method.getName();
				String columnName = methodName.replaceFirst("get", "").toLowerCase();
				query += ", " + columnName;
			}
		}
		query += " FROM " + tableName + " where 1!=1";
		query += ")";
		return query;
	}

	protected String buildCheckPart(Method methodToBeValidated,
			Object objectToBeValidated) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		String methodName = methodToBeValidated.getName();
		String columnName = methodName.replaceFirst("get", "").toLowerCase();
		String tableName = objectToBeValidated.getClass().getSimpleName()
				.toLowerCase().replace("_resolved", "");
		
		String createHashtableIdentifier = this.createHashtableIdentifier(
				objectToBeValidated.getClass().getSimpleName().toLowerCase()
						.replace("_resolved", ""), columnName.toLowerCase());
		String checkPart = "";
		if (this.hashMap.containsKey(createHashtableIdentifier)) {
			 checkPart += " union all "
						+ "select count(*)::int, " + "'" + tableName + "',"
						+ "'" + columnName + "'" + " from to_check"
						+ " where 1=1";
			checkPart += " AND "
					+ this.hashMap.get(createHashtableIdentifier)
							.getCheck_clause();
			checkPart += " having count(*) <= 0";
		}
		return checkPart;
	}

	protected String buildWholeQuery(Object objectToBeValidated)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		// dummy so we can directly union all without any special case f*ckup
		String query = "with dummy as (select 1)";
		query += this.buildWithPart(objectToBeValidated);
		// append checkPart
		// dummy so we can directly union all without any special case f*ckup
		query += "select null as is_valid, null as table_name, null as column_name where 1!=?";
		for (Method method : objectToBeValidated.getClass()
				.getDeclaredMethods()) {
			if (method.getName().startsWith("get")) {
				query +=  this.buildCheckPart(method, objectToBeValidated);
			}
		}
		return query;
	}

	public List<ValidationResult> validate(Object objectToBeValidated)
			throws SQLException, Exception {
		String queryToBeExecuted = this.buildWholeQuery(objectToBeValidated);
		List uncastedValidationResults = this.dataDAO.readOutViewOrTable(
				ValidationResult.class, null, null, 0, 5000, null,
				queryToBeExecuted);
		List<ValidationResult> validationResults = new ArrayList<ValidationResult>(
				uncastedValidationResults);
		return validationResults;
	}
}
