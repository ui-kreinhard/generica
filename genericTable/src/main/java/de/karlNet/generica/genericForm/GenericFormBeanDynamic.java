package de.karlNet.generica.genericForm;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;
import org.primefaces.extensions.model.dynaform.DynaFormControl;
import org.primefaces.extensions.model.dynaform.DynaFormLabel;
import org.primefaces.extensions.model.dynaform.DynaFormModel;
import org.primefaces.extensions.model.dynaform.DynaFormRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;

import de.karlNet.generica.formElement.FieldOrderMappingAndTranslation;
import de.karlNet.generica.genericTable.TableBean;
import de.karlNet.generica.genericTable.daos.ClassDefinitionWithFK;
import de.karlNet.generica.genericTable.daos.ColumnModel;
import de.karlNet.generica.genericTable.daos.DataDAO;
import de.karlNet.generica.genericTable.daos.SchemaDAO;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GenericFormBeanDynamic {
	private String viewName = "";

	private String viewNameForSubObjects;
	private Object objectToBeHandled;
	private Object subObjectToBeHandled;
	private List<Object> subObjects = new ArrayList<Object>();
	private List<ColumnModel> columnModels = null;

	@Autowired
	private SchemaDAO schemaDAO;
	@Autowired
	private DataDAO dataDAO;
	@Autowired
	private TableBean tableBean;

	private DynaFormModel model = new DynaFormModel();

	private DynaFormModel subModel = new DynaFormModel();
	@Autowired
	private Validator validator;

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public List<Object> getSubObjects() {
		return subObjects;
	}

	public void setSubObjects(List<Object> subObjects) {
		this.subObjects = subObjects;
	}

	public DynaFormModel getSubModel() {
		return subModel;
	}

	public void setSubModel(DynaFormModel subModel) {
		this.subModel = subModel;
	}

	public List<ColumnModel> getColumnModels() {
		return columnModels;
	}

	public void setColumnModels(List<ColumnModel> columnModels) {
		this.columnModels = columnModels;
	}

	private Object findFirstNonEmptyElement(Object[] selectedObjects) {
		if (selectedObjects == null) {
			return null;
		}
		for (int i = 0; i < selectedObjects.length; i++) {
			if (selectedObjects[i] != null && !selectedObjects.equals("")) {
				return selectedObjects[i];
			}

		}
		return null;
	}

	private Object createGuiElements(DynaFormModel parentFormModel,
			String viewName, Object[] selectedObjects) throws Exception {
		Object objectToBeHandled = null;

		ClassDefinitionWithFK fkDefintionContainer = this.schemaDAO
				.getColumnDefinitionWithMappingValues(viewName);
		Class<?> mappedTableClass = fkDefintionContainer.getClassDefinition();
		Object seleObject = this.findFirstNonEmptyElement(selectedObjects);
		if (seleObject != null) {
			this.tableBean.setSelectedObjects(new Object[] {});
			objectToBeHandled = this.dataDAO.getByPK(this.viewName, seleObject);

		} else {
			objectToBeHandled = mappedTableClass.newInstance();
		}

		List<Method> declaredMethods = new ArrayList<Method>();
		for (Method method : mappedTableClass.getDeclaredMethods()) {
			if (method.getName().startsWith("get")) {
				declaredMethods.add(method);
			}
		}
		final HashMap<String, FieldOrderMappingAndTranslation> orderMapping = new HashMap<String, FieldOrderMappingAndTranslation>();

		if (this.schemaDAO.tableExists("formelements_order")) {
			HashMap<String, String> filter = new HashMap<String, String>();
			filter.put("table_name", viewName);
			List fieldOrderMappings = this.dataDAO
					.readOutViewOrTableWithoutMapping(
							"formelement_orders_and_translation",
							FieldOrderMappingAndTranslation.class, filter);
			if (fieldOrderMappings.size() > 0) {
				for (Object object : fieldOrderMappings) {
					FieldOrderMappingAndTranslation fieldOrderMapping = (FieldOrderMappingAndTranslation) object;
					orderMapping.put(fieldOrderMapping.getColumn_name()
							.toLowerCase(), fieldOrderMapping);
				}
				Collections.sort(declaredMethods, new Comparator<Method>() {
					public int compare(Method a, Method b) {
						Integer aOrderId = orderMapping.get(
								a.getName().replace("get", "").toLowerCase())
								.getId();
						Integer bOrderId = orderMapping.get(
								(b.getName().replace("get", "").toLowerCase()))
								.getId();
						return aOrderId.compareTo(bOrderId);
					}
				});
			}
		}
		for (Method method : declaredMethods) {
			if (method.getName().startsWith("get")) {
				String attributeName = method.getName().replaceAll("get", "");
				DynaFormRow row = parentFormModel.createRegularRow();

				String translation = attributeName;
				if (orderMapping.get(attributeName.toLowerCase()) != null) {
					translation = orderMapping.get(attributeName.toLowerCase())
							.getTranslation();
				}
				DynaFormLabel label = row.addLabel(translation);
				if (fkDefintionContainer.isFK(attributeName)) {
					List selectItems = fkDefintionContainer
							.getSelectionValues(attributeName);
					DynaFormControl control = row.addControl(new Property(
							attributeName, objectToBeHandled, selectItems),
							"select");
				} else if (method.getReturnType().equals(String.class)) {
					DynaFormControl control = row.addControl(new Property(
							attributeName, objectToBeHandled), "input");
					label.setForControl(control);
				} else if (method.getReturnType().equals(Boolean.class)) {
					DynaFormControl control = row.addControl(new Property(
							attributeName, objectToBeHandled), "booleanchoice");
					label.setForControl(control);
				} else if (method.getReturnType().equals(Timestamp.class)) {
					DynaFormControl control = row.addControl(new Property(
							attributeName, objectToBeHandled), "timestamp");
					label.setForControl(control);
				}
				// else if (method.getReturnType().equals(Integer.class)) {
				// if
				// (!this.schemaDAO.getPrimaryKeyOfTable(viewName).toLowerCase()
				// .equals(attributeName.toLowerCase())) {
				// DynaFormRow row = parentFormModel.createRegularRow();
				//
				// DynaFormLabel label = row.addLabel(attributeName);
				// DynaFormControl control = row.addControl(
				// new Property(attributeName,
				// this.objectToBeHandled), "input");
				// label.setForControl(control);
				// }
				// }
			}

		}
		return objectToBeHandled;
	}

	public boolean checkViewName() throws SecurityException, Exception {
		Object[] selectedObjects = this.tableBean.getSelectedObjects();
		model = new DynaFormModel();
		this.objectToBeHandled = this.createGuiElements(this.model,
				this.viewName, selectedObjects);
		// now get data for table
		// check if there's a fk relation in tableschema
		// if so add table element with the coresponding data
		this.viewNameForSubObjects = this.schemaDAO
				.get1NfKConstraints(this.viewName);
		if (viewNameForSubObjects != null) {
			this.columnModels = this.schemaDAO
					.getColumnModels(viewNameForSubObjects);
			DynaFormRow row = model.createRegularRow();

			DynaFormLabel label = row.addLabel("table");
			DynaFormControl control = row.addControl(new Property("table",
					this.objectToBeHandled), "table");
			label.setForControl(control);

			this.subModel = new DynaFormModel();
			this.subObjects.clear();
			this.subObjectToBeHandled = this.createGuiElements(subModel,
					viewNameForSubObjects, null);
		} else {
			this.columnModels = null;
		}
		return true;
	}

	public void addSubelement() throws Exception {
		this.subObjects.add(this.subObjectToBeHandled);
		this.subModel = new DynaFormModel();
		RequestContext.getCurrentInstance().update("subObjects");
		this.subObjectToBeHandled = this.createGuiElements(subModel,
				viewNameForSubObjects, null);
	}

	public Object getObjectToBeHandled() throws SecurityException, Exception {
		this.checkViewName();
		return objectToBeHandled;
	}

	public void setObjectToBeHandled(Object objectToBeHandled) {
		this.objectToBeHandled = objectToBeHandled;
	}

	public String create() throws Exception {
		List<ValidationResult> validationResults = this.validator
				.validate(this.objectToBeHandled);
		for (ValidationResult validationResult : validationResults) {
	        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid input for:", validationResult.getColumn_name()));  
		}
		if(!validationResults.isEmpty()) {
			return "";
		}

		this.dataDAO.create(this.objectToBeHandled, this.viewName);
		for (Object subObject : this.subObjects) {
			this.dataDAO.create(subObject, this.viewNameForSubObjects);
		}
		String viewNameForReturn = this.viewName;
		this.viewName = "";
		this.subObjects.clear();
		return "/dynamiccolumn.xhtml?viewname=" + viewNameForReturn;
	}

	public DynaFormModel getModel() throws SecurityException, Exception {
		return model;
	}

}
