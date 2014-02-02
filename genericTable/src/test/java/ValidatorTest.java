import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import de.karlNet.menu.Menu;

public class ValidatorTest {

	@Test
	public void testSelectPart() throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Menu objectToBeValidated = new Menu();
		objectToBeValidated.setId(1);
		objectToBeValidated.setLink("helo");
		objectToBeValidated.setMenu_idmenu(2);
		objectToBeValidated.setMenulabel("mymenulabel");
		ValidatorTestExtension validatorTestExtension = new ValidatorTestExtension();
		Method method = objectToBeValidated.getClass().getMethod("getId", null);
		String ret = validatorTestExtension
				.buildSelectPart(method,objectToBeValidated);
		String anObject = "select '1' as id";
		System.out.println(ret);
		System.out.println(anObject);
		Assert.assertTrue(ret.equals(anObject));

	}

	@Test
	public void testUnionPart() throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Menu objectToBeValidated = new Menu();
		ValidatorTestExtension validatorTestExtension = new ValidatorTestExtension();
		Method method = objectToBeValidated.getClass().getMethod("getId", null);
		String ret = validatorTestExtension.buildUnionPart(method, objectToBeValidated);
		String anObject = "union all select id from menu where 1!=1";
		System.out.println(anObject);
		System.out.println(ret);
		Assert.assertTrue(ret
				.equals(anObject));
	}
	
	@Test
	public void testWithPart() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Menu objectToBeValidated = new Menu();
		objectToBeValidated.setId(1);
		objectToBeValidated.setLink("helo");
		objectToBeValidated.setMenu_idmenu(2);
		objectToBeValidated.setMenulabel("mymenulabel");
		ValidatorTestExtension validatorTestExtension = new ValidatorTestExtension();
		Method method = objectToBeValidated.getClass().getMethod("getId", null);
		String ret = validatorTestExtension
				.buildWithPart(method, objectToBeValidated);
		String anObject = "to_check_id as (select '1' as id union all select id from menu where 1!=1)";
		System.out.println(ret);
		System.out.println(anObject);
		Assert.assertTrue(ret.equals(anObject));
	}
	
	@Test
	public void testWhole() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Menu objectToBeValidated = new Menu();
		objectToBeValidated.setId(1);
		objectToBeValidated.setLink("helo");
		objectToBeValidated.setMenu_idmenu(2);
		objectToBeValidated.setMenulabel("mymenulabel");
		ValidatorTestExtension validatorTestExtension = new ValidatorTestExtension();
		String ret = validatorTestExtension.buildWholeQuery(objectToBeValidated);
		System.out.println(ret);
	}
}
