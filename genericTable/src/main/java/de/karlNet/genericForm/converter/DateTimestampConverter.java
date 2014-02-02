package de.karlNet.genericForm.converter;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("de.karlNet.converter.DateTimestampConverter")
public class DateTimestampConverter implements Converter {
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm");

	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {

		// TODO Auto-generated method stub
		try {
			return new Timestamp(dateFormat.parse(value).getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		// TODO Auto-generated method stub
		Timestamp st = (Timestamp) value;
		return this.dateFormat.format(st);
	}

}
