package de.karlNet.login;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.karlNet.login.LoginLogoutBean;
import de.karlNet.login.NotLoggedInException;

@Aspect
public class AuthenticationIntercepter {
	@Autowired
	private LoginLogoutBean loginBean;
	private boolean authenticationIsActived = false;

	public void setAuthenticationIsActived(boolean authenticationIsActived) {
		this.authenticationIsActived = authenticationIsActived;
	}

	@PostConstruct
	public void init() {
		this.authenticationIsActived = true;
	}

	@Around("execution(* de.karlNet.generica..*.*(..))")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable{ 
		if (!this.authenticationIsActived || this.loginBean.isLoggedIn()) {
			return joinPoint.proceed(); 
		} else {
			throw new NotLoggedInException();
		}
	}

}
