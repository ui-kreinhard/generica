package de.karlNet.validator;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.karlNet.dbhandler.DBHandler;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class LoginTest extends BaseTest {
	@Autowired
	private DBHandler dbHandler;
	
	@Test
	public void testNUll() {
		System.out.println(null==null);
	}
	
	@Test
	public void successfulLogin() {
		boolean login = false;
		try {
			login = this.dbHandler.login("postgres", "he8.st:!");
		} catch (SQLException e) {
			Assert.fail();
		}
		Assert.assertTrue(login);
	}
	
	@Test
	public void failedLogin() {
		boolean login = false;
		try {
			login = this.dbHandler.login("postgres", "he8.st:");
		} catch (SQLException e) {
			Assert.fail();
		}
		Assert.assertTrue(!login);
	}
	
	@Test
	public void failedLoginWrong() {
		boolean login = false;
		try {
			login = this.dbHandler.login("postgress", "he8.st:!");
		} catch (SQLException e) {
			Assert.fail();
		}
		Assert.assertTrue(!login);
	}
}
