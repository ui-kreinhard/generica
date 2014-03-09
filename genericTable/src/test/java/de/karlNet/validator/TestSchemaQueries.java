package de.karlNet.validator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.karlNet.generica.genericTable.daos.ColumnModel;
import de.karlNet.generica.genericTable.daos.DataDAO;
import de.karlNet.generica.genericTable.daos.SchemaDAO;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class TestSchemaQueries extends BaseTest {
	@Autowired
	private SchemaDAO schemaDAO;
	@Autowired
	private DataDAO dataDAO;
	
	@Test
	public void test1nfkQuery() throws Exception {
		String viewName = this.schemaDAO.get1NfKConstraints("table1");
		Assert.assertTrue(viewName!=null);
		List<ColumnModel> columnModels = this.schemaDAO.getColumnModels(viewName);
		for (ColumnModel columnModel : columnModels) {
			System.out.println(columnModel);
		}
	}
	
	@Test
	public void testOrderQuery() throws Exception {
		List<ColumnModel> columnModels = this.schemaDAO.getColumnModels("menu");
		Assert.assertTrue(columnModels.size()>0);
		Assert.assertTrue(columnModels.get(0).getProperty().equals("id"));
		Assert.assertTrue(columnModels.get(1).getProperty().equals("menulabel"));
		Assert.assertTrue(columnModels.get(2).getProperty().equals("menu_idmenu"));
		Assert.assertTrue(columnModels.get(3).getProperty().equals("link"));
	}
	
	@Test
	public void testDate() throws SQLException {
		Class<?> columnClass = this.schemaDAO.getColumnClass("worktimes_view");
		Method[] declaredMethods = columnClass.getDeclaredMethods();
		for (Method method : declaredMethods) {
			System.out.println(method.getReturnType());
		}
	}
	
	@Test
	public void testSchemaTablesAndViews() throws SQLException {
		List<String> tablesOrViews = this.schemaDAO.getTablesAndViews();
		Assert.assertTrue(this.schemaDAO.getTablesAndViews().size()>0);
		Assert.assertTrue(tablesOrViews.contains("TABLE_ACTIONS"));
		Assert.assertTrue(tablesOrViews.contains("menu"));
		Assert.assertTrue(tablesOrViews.contains("formelements_order"));
	}
	
	
		
	@Test
	public void testPK() throws SQLException {
		Assert.assertTrue(this.schemaDAO.getPrimaryKeyOfTable("menu").equals("id"));
		Assert.assertTrue(this.schemaDAO.getPrimaryKeyOfTable("TABLE_ACTIONS").equals("id"));
	}
	
	@Test
	public void readoutPKValue() throws SQLException, Exception {
		Object pkValue = this.schemaDAO.getPrimaryKeyValue("menu", 11);
		Assert.assertTrue(pkValue!=null);
		Assert.assertTrue(pkValue.equals(11));
	}
	
	
	
	@Test
	public void testFK() throws SQLException {
		Assert.assertTrue(this.schemaDAO.isFK("menu", "menu_idmenu"));
	}
}
