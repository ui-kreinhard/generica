package de.karlNet.validator;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;

import de.karlNet.login.LoginLogoutBean;
import de.karlNet.login.NotLoggedInException;

public class BaseTest {
	@Autowired
	private LoginLogoutBean loginBean;
	
	@Before
	public void setupAuthentication() throws NotLoggedInException, SQLException {
		if(loginBean.isLoggedIn()) {
			return;
		}
		this.loginBean.login("postgres", "he8.st:!");
	}
}
