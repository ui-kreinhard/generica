package de.karlNet.validator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.karlNet.generica.genericForm.ValidationResult;
import de.karlNet.generica.genericTable.daos.DataDAO;
import de.karlNet.generica.menu.Menu;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class ValidatorTest  extends BaseTest {
	@Autowired
	private ValidatorTestExtension validatorTestExtension;

	@Autowired
	private DataDAO dataDAO;

	@Test
	public void testExecuted() throws SQLException, Exception {
		Menu objectToBeValidated = new Menu();
		objectToBeValidated.setId(-1);
		objectToBeValidated.setLink("helo");
		objectToBeValidated.setMenu_idmenu(2);
		List<ValidationResult> validationResults = this.validatorTestExtension.validate(objectToBeValidated);
		for (ValidationResult validationResult : validationResults) {
			System.out.println(validationResult);
		}
	}

	@Test
	public void testSelectPart() throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Menu objectToBeValidated = new Menu();
		objectToBeValidated.setId(1);
		objectToBeValidated.setLink("helo");
		objectToBeValidated.setMenu_idmenu(2);
		objectToBeValidated.setMenulabel("mymenulabel");
		ValidatorTestExtension validatorTestExtension = new ValidatorTestExtension();
		Method method = objectToBeValidated.getClass().getMethod("getId", null);
		String ret = validatorTestExtension.buildSelectPart(method,
				objectToBeValidated);
		String anObject = "select '1' as id";
		Assert.assertTrue(ret.equals(anObject));

	}

	@Test
	public void testUnionPart() throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Menu objectToBeValidated = new Menu();
		ValidatorTestExtension validatorTestExtension = new ValidatorTestExtension();
		Method method = objectToBeValidated.getClass().getMethod("getId", null);
		String ret = validatorTestExtension.buildUnionPart(method,
				objectToBeValidated);
		String anObject = "union all select id from menu where 1!=1";
		Assert.assertTrue(ret.equals(anObject));
	}

	@Test
	public void testWithPart() throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Menu objectToBeValidated = new Menu();
		objectToBeValidated.setId(1);
		objectToBeValidated.setLink("helo");
		objectToBeValidated.setMenu_idmenu(2);
		objectToBeValidated.setMenulabel("mymenulabel");
		ValidatorTestExtension validatorTestExtension = new ValidatorTestExtension();
		Method method = objectToBeValidated.getClass().getMethod("getId", null);
		String ret = validatorTestExtension.buildWithPart(method,
				objectToBeValidated);
		System.out.println(ret);
		String anObject = "to_check_id as (select '1' as id union all select id from menu where 1!=1)";
		Assert.assertTrue(ret.equals(anObject));
	}

	@Test
	public void testWhole() throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Menu objectToBeValidated = new Menu();
		objectToBeValidated.setId(1);
		objectToBeValidated.setLink("helo");
		objectToBeValidated.setMenu_idmenu(2);
		objectToBeValidated.setMenulabel("mymenulabel");
		String ret = validatorTestExtension
				.buildWholeQuery(objectToBeValidated);
		System.out.println(ret);
	}
}
