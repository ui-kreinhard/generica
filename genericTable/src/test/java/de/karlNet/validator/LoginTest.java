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
			login = this.dbHandler.connectWithUsernamePassword("postgres", "he8.st:!");
		} catch (SQLException e) {
			Assert.fail();
		}
		Assert.assertTrue(login);
	}
	
	@Test
	public void failedLogin() {
		boolean login = false;
		try {
			login = this.dbHandler.connectWithUsernamePassword("postgres", "he8.st:");
		} catch (SQLException e) {
			Assert.fail();
		}
		Assert.assertTrue(!login);
	}
	
	@Test
	public void failedLoginWrong() {
		boolean login = false;
		try {
			login = this.dbHandler.connectWithUsernamePassword("postgress", "he8.st:!");
		} catch (SQLException e) {
			Assert.fail();
		}
		Assert.assertTrue(!login);
	}
	
	@Test
	public void loginLogoutTest() {
		boolean login = false;
		try {
			login = this.dbHandler.connectWithUsernamePassword("postgres", "he8.st:!");
			Assert.assertTrue(login);
			this.dbHandler.disconnect();
		} catch (Exception e) {
			Assert.fail();
		}
		// this query has now to fail
		try {
			this.dbHandler.executeQuery("Select 1");
		} catch(Exception e) {
			Assert.assertTrue(true);
			return;
		}
		// okay no exception cautght...
		Assert.assertFalse(true);
	}
}
