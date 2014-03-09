package de.karlNet.validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import de.karlNet.generica.genericForm.Validator;
@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ValidatorTestExtension extends Validator {

	public String buildSelectPart(Method methodToBeValidated,
			Object objectToBeValidated) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		return super.buildSelectPart(methodToBeValidated, objectToBeValidated);
	}

	public String buildUnionPart(Method methodToBeValidated,
			Object objectToBeValidated) {
		return super.buildUnionPart(methodToBeValidated, objectToBeValidated);
	}

	public String buildWithPart(Method methodToBeValidated,
			Object objectToBeValidated) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		return super.buildWithPart(methodToBeValidated, objectToBeValidated);
	}
	public String buildWholeQuery(Object objectToBeValidated) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return super.buildWholeQuery(objectToBeValidated);
	}
}
