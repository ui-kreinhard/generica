package de.karlNet.validator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.karlNet.dbhandler.DBHandler;
import de.karlNet.generica.formElement.FieldOrderMapping;
import de.karlNet.generica.genericTable.daos.ClassDefinitionWithFK;
import de.karlNet.generica.genericTable.daos.DataDAO;
import de.karlNet.generica.genericTable.daos.SchemaDAO;
import de.karlNet.generica.menu.Menu;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class TestData extends BaseTest {
	@Autowired
	private SchemaDAO schemaDAO;
	@Autowired
	private DataDAO dataDAO;

	@Autowired
	private DBHandler dbHandler;

	@Test
	public void testReadoutWithConcreteClass() throws Exception {
		List<?> data = this.dataDAO.readOutViewOrTableWithoutMapping("menu",
				Menu.class);
		Assert.assertTrue(data.size() > 0);
		Menu first = (Menu) data.get(0);
		Assert.assertTrue(first.getId() == 0);
		Assert.assertTrue(first.getMenulabel().equals("ROOT"));
		Assert.assertTrue(first.getLink().equals("ROOT"));
	}

	@Test
	public void testSP() throws Exception {
		String ret = (String)this.dataDAO.executeStoredProcedure("checkin_checkout_time()");
		if(ret.equals("Checked out")) {
			// check out
			ret = (String)this.dataDAO.executeStoredProcedure("checkin_checkout_time()");
		} else if(ret.equals("Checked In")) {
			
		} else {
			Assert.assertFalse(false);
		}
		ret = (String)this.dataDAO.executeStoredProcedure("checkin_checkout_time()");
		Assert.assertTrue("Checked out".equals(ret));
		System.out.println(ret);
	}
	
	@Test
	public void testCount() throws SQLException {
		Assert.assertTrue(this.dataDAO.countTableRows("menu", null) > 0);
	}

	@Test
	public void testCountFiltered() throws SQLException {
		HashMap<String, String> filter = new HashMap<String, String>();
		filter.put("link", "ROOT");
		Assert.assertTrue(this.dataDAO.countTableRows("menu", filter) > 0);
	}

	@Test
	public void testLikeReadOut() throws SQLException, Exception {
		HashMap<String, String> filter = new HashMap<String, String>();
		filter.put("menuLabel", "Admin*");
		Assert.assertTrue(this.dataDAO.readOutViewOrTable("menu",
				this.schemaDAO.getColumnWithResolvedMapping("menu"), null,
				SortOrder.ASCENDING, 0, 100, filter).size() > 0);
	}

	@Test
	public void testExactReadOut() throws SQLException, Exception {
		HashMap<String, String> filter = new HashMap<String, String>();
		filter.put("menuLabel", "Administration");
		Assert.assertTrue(this.dataDAO.readOutViewOrTable("menu",
				this.schemaDAO.getColumnWithResolvedMapping("menu"), null,
				SortOrder.ASCENDING, 0, 100, filter).size() > 0);
	}

	@Test
	public void testReadOutViewOrTable() throws SQLException, Exception {
		List<?> data = this.dataDAO.readOutViewOrTable("menu",
				this.schemaDAO.getColumnWithResolvedMapping("menu"), null,
				SortOrder.ASCENDING, 0, 100, null);
		Assert.assertTrue(data.size() > 0);
	}

	@Test
	public void testReadOutWithMapping() throws SQLException, Exception {
		List<?> data = this.dataDAO.readOutViewOrTable("TABLE_ACTIONS",
				this.schemaDAO.getColumnWithResolvedMapping("TABLE_ACTIONS"), null,
				SortOrder.ASCENDING, 0, 100, null);
		Assert.assertTrue(data.size() > 0);
	}

	@Test
	public void testReadOutWithColumnSelection() throws NoSuchMethodException,
			SecurityException, SQLException, Exception {
		ClassDefinitionWithFK data = this.schemaDAO
				.getColumnDefinitionWithMappingValues("TABLE_ACTIONS");
	}

	@Test
	public void testDeletion() throws SQLException {
		Integer[] objectsToBeDeleted = { 1017, 1018, 1019 };
		try {
			this.dataDAO.delete("menu", objectsToBeDeleted);
		} catch (Exception e) {
		}
		int countStart = this.dataDAO.countTableRows("menu", null);
		this.dbHandler
				.executeUpdate(this.dbHandler
						.prepareStatement("INSERT INTO menu(id, link, menuLabel) VALUES(1017,'abc','abc')"));
		this.dbHandler
				.executeUpdate(this.dbHandler
						.prepareStatement("INSERT INTO menu(id, link, menuLabel) VALUES(1018,'abc','abc')"));
		this.dbHandler
				.executeUpdate(this.dbHandler
						.prepareStatement("INSERT INTO menu(id, link, menuLabel) VALUES(1019,'abc','abc')"));

		this.dataDAO.delete("menu", objectsToBeDeleted);
		int countEnd = this.dataDAO.countTableRows("menu", null);
		Assert.assertTrue(countStart == countEnd);
	}

	@Test
	public void testDeletionWithFilter() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		List<FieldOrderMapping> fieldOrderMappings = new ArrayList<FieldOrderMapping>();
		{
			FieldOrderMapping fieldOrderMapping = new FieldOrderMapping();
			fieldOrderMapping.setColumn_name("test1");
			fieldOrderMapping.setTable_name("test");
			fieldOrderMappings.add(fieldOrderMapping);
		}
		{
			FieldOrderMapping fieldOrderMapping = new FieldOrderMapping();
			fieldOrderMapping.setColumn_name("test2");
			fieldOrderMapping.setTable_name("test");
			fieldOrderMappings.add(fieldOrderMapping);
		}
		{
			FieldOrderMapping fieldOrderMapping = new FieldOrderMapping();
			fieldOrderMapping.setColumn_name("test3");
			fieldOrderMapping.setTable_name("test");
			fieldOrderMappings.add(fieldOrderMapping);
		}
		for (FieldOrderMapping fieldOrderMapping : fieldOrderMappings) {
			this.dataDAO.create(fieldOrderMapping, "formelements_order");
		}
		HashMap<String, String> filter = new HashMap<String, String>();
		filter.put("table_name", "test");
		this.dataDAO.delete("formelements_order", filter);
		Assert.assertTrue(this.dataDAO.countTableRows("formelements_order", filter) <= 0);

	}

	@Test
	public void testGenericInsertion() throws SQLException, Exception {
		List<?> data = this.dataDAO
				.readOutViewOrTableWithoutMapping("TABLE_ACTIONS",
						this.schemaDAO.getColumnClass("TABLE_ACTIONS"));
		Object o = data.get(0);
		

		this.dataDAO.create(o, "TABLE_ACTIONS");
		Method setId = o.getClass().getMethod("setId", Integer.class);
		setId.invoke(o, 1001);
		this.dataDAO.create(o, "TABLE_ACTIONS");

		this.dataDAO.delete("TABLE_ACTIONS", 1001);
	}
	
	@Test
	public void readoutWithoutParams() throws Exception {
		List<?> data = this.dataDAO.readOutViewOrTable("TABLE_ACTIONS");
		Assert.assertTrue(data.size()>0);
	}
	
	@Test
	public void testPKReadout() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {
		this.dataDAO.getByPK("TABLE_ACTIONS", this.schemaDAO.getColumnClass("TABLE_ACTIONS"), 7);
		this.dataDAO.getByPK("TABLE_ACTIONS", 7);

	}
}
