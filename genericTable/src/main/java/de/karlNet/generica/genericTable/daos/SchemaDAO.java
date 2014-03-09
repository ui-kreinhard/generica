package de.karlNet.generica.genericTable.daos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import de.karlNet.dbhandler.DBHandler;
import de.karlNet.generica.formElement.FieldOrderMapping;
import de.karlNet.generica.formElement.FieldOrderMappingAndTranslation;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SchemaDAO {
	@Autowired
	private ClassCache classCache;
	@Autowired
	private DataDAO dataDAO;

	@Autowired
	private DBHandler dbHandler;

	public String get1NfKConstraints(String viewName) throws SQLException {
		String query = 
				"SELECT " +
						"tc.table_name " +
				"FROM " +
				"information_schema.table_constraints AS tc " +
				"JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name " +
				"JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name " +
				"WHERE " +
					"constraint_type = 'FOREIGN KEY' AND ccu.table_name=?";
		PreparedStatement preparedStatement = this.dbHandler.prepareStatement(query);
		preparedStatement.setString(1, viewName);
		ResultSet resultSet = this.dbHandler.executeQuery(preparedStatement);
		if(!resultSet.next()) {
			return null;
		}
		String tableName = resultSet.getString(1);
		return tableName;
	}

	public void clearClassCache() {
		System.out.println("cleaning out class cache");
		this.classCache.clearCache();
	}

	public String getOrderTableName(String tableName) {
		return tableName + "_order";
	}

	public boolean checkOrderTableExists(String tableName) throws SQLException {
		return this.tableExists(this.getOrderTableName(tableName));
	}

	public boolean tableExists(String tableName) throws SQLException {
		String query = "SELECT count(*) FROM tables where LOWER(table_name)=LOWER(?)";
		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement(query);
		preparedStatement.setString(1, tableName);
		ResultSet resultSet = this.dbHandler.executeQuery(preparedStatement);
		resultSet.next();
		int count = resultSet.getInt(1);
		return count > 0;
	}

	public boolean existMapping(String viewName, String attributeName)
			throws SQLException {
		return this.tableExists(this.getMappingName(viewName, attributeName));
	}

	public Class<?> getColumnClass(String viewName) throws SQLException {
		if (this.classCache.isInCache(viewName)) {
			return this.classCache.getFromCache(viewName);
		}

		Map<String, Class<?>> properties = this.getPropertyMap(viewName);
		final Class<?> beanClass = this.classCache.createBeanClass("generics."
				+ viewName, properties);
		this.classCache.putInCacache(viewName, beanClass);
		return beanClass;
	}

	public ClassDefinitionWithFK getColumnDefinitionWithMappingValues(
			String viewName) throws Exception {
		Class<?> columnDefition = this.getColumnClass(viewName);
		ClassDefinitionWithFK classDefinitionWithFK = new ClassDefinitionWithFK(
				columnDefition);
		for (Method properties : columnDefition.getDeclaredMethods()) {
			String attributeName = properties.getName();
			if (attributeName.startsWith("get")) {
				attributeName = StringUtils.uncapitalize(attributeName.replace(
						"get", ""));
				if (isFK(viewName, attributeName)) {
					List selectValues = this.dataDAO
							.readOutViewOrTable(viewName + "_" + attributeName
									+ "_rel_mapping");
					classDefinitionWithFK.addSelectionValues(attributeName,
							selectValues);
				}
			}
		}
		return classDefinitionWithFK;
	}

	public List<ColumnModel> getColumnModels(String viewName) throws Exception {
		Class<?> definition = this.getColumnWithResolvedMapping(viewName);

		List<ColumnModel> columnModels = this.getColumnModels(viewName,
				definition);
		if (this.tableExists("formelements_order")) {
			// read out table
			HashMap<String, String> filter = new HashMap<String, String>();
			filter.put("table_name", viewName);
			List fieldOrderMappings = this.dataDAO
					.readOutViewOrTableWithoutMapping("formelement_orders_and_translation",
							FieldOrderMappingAndTranslation.class, filter);
			if (fieldOrderMappings.size() > 0) {
				// create hasmap with mapping coulmName -> id
				final HashMap<String, FieldOrderMappingAndTranslation> orderMapping = new HashMap<String, FieldOrderMappingAndTranslation>();
				for (Object object : fieldOrderMappings) {
					FieldOrderMappingAndTranslation fieldOrderMapping = (FieldOrderMappingAndTranslation) object;
					orderMapping.put(fieldOrderMapping.getColumn_name()
							.toLowerCase(), fieldOrderMapping);
				}
				for (ColumnModel columnModel : columnModels) {
					String translation = orderMapping.get(columnModel.getProperty()
								.toLowerCase()).getTranslation();
					columnModel.setHeader(translation);
				}
				Collections.sort(columnModels, new Comparator<ColumnModel>() {
					public int compare(ColumnModel a, ColumnModel b) {
						Integer aOrderId = orderMapping.get(a.getProperty()
								.toLowerCase()).getId();
						Integer bOrderId = orderMapping.get(b.getProperty()
								.toLowerCase()).getId();
						return aOrderId.compareTo(bOrderId);
					}
				});
			}
		}
		return columnModels;
	}

	public List<ColumnModel> getColumnModels(String viewName,
			Class<?> definition) {
		List<ColumnModel> columnModels = new ArrayList<ColumnModel>();
		for (Method method : definition.getDeclaredMethods()) {
			if (method.getName().startsWith("get")) {
				String fieldName = method.getName().replaceAll("get", "");
				columnModels.add(new ColumnModel(fieldName.toUpperCase(),
						StringUtils.uncapitalize(fieldName)));
			}
		}
		return columnModels;
	}

	public Class<?> getColumnWithResolvedMapping(String viewName)
			throws SQLException, NoSuchMethodException, SecurityException {

		if (this.classCache.isInCache(viewName + "_resolved")) {
			return this.classCache.getFromCache(viewName + "_resolved");
		}

		Map<String, Class<?>> propertiesTemp = this.getPropertyMap(viewName);
		Map<String, Class<?>> properties = new TreeMap<String, Class<?>>();
		for (String columnName : propertiesTemp.keySet()) {
			Class<?> type = propertiesTemp.get(columnName);

			if (this.existMapping(viewName, columnName)) {
				String mappingName = this.getMappingName(viewName, columnName);
				type = this.getColumnWithResolvedMapping(mappingName)
						.getMethod("get" + "Label", null).getReturnType();
				properties.put(columnName, type);
			} else {
				properties.put(columnName, type);
			}
		}
		final Class<?> beanClass = this.classCache.createBeanClass("generics."
				+ viewName + "_resolved", properties);
		this.classCache.putInCacache(viewName + "_resolved", beanClass);

		return beanClass;
	}

	public String getMappingName(String viewName, String attributeName) {
		return (viewName + "_" + attributeName.toLowerCase() + "_rel_mapping");
	}

	public String getPrimaryKeyOfTable(String tableName) throws SQLException {
		String query = "SELECT      k.COLUMN_NAME FROM     information_schema.table_constraints t         LEFT JOIN     information_schema.key_column_usage k USING (constraint_name , table_schema , table_name) WHERE     t.constraint_type = 'PRIMARY KEY' AND t.table_schema = ? AND t.table_name = ?";

		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement(query);
		preparedStatement.setString(2, tableName);
		preparedStatement.setString(1, this.dbHandler.getDatabase());
		ResultSet resultSet = this.dbHandler.executeQuery(preparedStatement);

		if (!resultSet.next()) {
			return "id";
		}
		return resultSet.getString(1);
	}

	public Object getPrimaryKeyValueOfObject(String tableName,
			Object objectToReadout) throws SQLException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		String primaryKey = this.getPrimaryKeyOfTable(tableName);
		Class<?> mappingClass = objectToReadout.getClass();
		try {
			Method m = mappingClass.getMethod(
					"get" + StringUtils.capitalize(primaryKey), null);
			return m.invoke(objectToReadout, null);
		} catch (java.lang.NoSuchMethodException e) {
			return null;
		}
	}

	public Object getPrimaryKeyValue(String tableName, Object objectToReadout)
			throws SQLException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		String primaryKey = this.getPrimaryKeyOfTable(tableName);
		Class<?> mappingClass = this.getColumnWithResolvedMapping(tableName);
		try {
			objectToReadout.getClass().getMethod(
					"get" + StringUtils.capitalize(primaryKey), null);

		} catch (java.lang.NoSuchMethodException e) {
			return "";
		}
		return mappingClass.getMethod(
				"get" + Character.toUpperCase(primaryKey.charAt(0))
						+ primaryKey.substring(1), null).invoke(
				objectToReadout, null);
	}

	private Map<String, Class<?>> getPropertyMap(String viewName)
			throws SQLException {

		String query = "SELECT * FROM " + viewName + " where "
				+ this.getPrimaryKeyOfTable(viewName) + " = '1'";
		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement(query);
		ResultSet resultSet = this.dbHandler.executeQuery(preparedStatement);
		ResultSetMetaData rsmd = resultSet.getMetaData();
		resultSet.next();
		final Map<String, Class<?>> properties = new TreeMap<String, Class<?>>();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			String columnName = rsmd.getColumnName(i);
			Object value = resultSet.getObject(i);
			Class<?> type = value.getClass();
			if (type == null) {
				continue;
			}
			properties.put(columnName, type);
		}
		return properties;
	}

	public List<String> getTablesAndViews() throws SQLException {
		List<String> tablesAndViews = new ArrayList<String>();
		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement("SELECT table_name FROM information_schema.tables WHERE table_schema = ? ORDER BY table_name DESC;");
		preparedStatement.setString(1, this.dbHandler.getDatabase());
		ResultSet resultSet = this.dbHandler.executeQuery(preparedStatement);
		while (resultSet.next()) {
			tablesAndViews.add(resultSet.getString("table_name"));
		}
		return tablesAndViews;
	}

	public boolean isFK(String viewName, String attributeName)
			throws SQLException {
		return this.existMapping(viewName, attributeName);
	}
}
