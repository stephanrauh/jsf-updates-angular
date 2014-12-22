/**
 *  (C) 2013-2014 Stephan Rauh http://www.beyondjava.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.beyondjava.angularFaces.core.transformation;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;

import de.beyondjava.angularFaces.core.ELTools;
import de.beyondjava.angularFaces.core.NGBeanAttributeInfo;

/** Brings JSR 303 annotations to the client. */
public class AddTypeInformationCallback implements VisitCallback {

	@Override
	public VisitResult visit(VisitContext arg0, UIComponent component) {
		if (component instanceof UIInput) {
			NGBeanAttributeInfo infos = ELTools.getBeanAttributeInfos(component);
			if (infos.isRequired()) {
				if ("".equals(AttributeUtilities.getAttribute(component, "required"))
						|| false == (Boolean)AttributeUtilities.getAttribute(component, "required")) {
					((UIInput) component).setRequired(true);
					component.getPassThroughAttributes().put("required", "");
				}
			}
			if (infos.getMax() > 0) {
				if (null == AttributeUtilities.getAttribute(component, "max"))
					component.getPassThroughAttributes().put("max", infos.getMax());
			}
			if (infos.getMin() > 0) {
				if (null == AttributeUtilities.getAttribute(component, "min"))
					component.getPassThroughAttributes().put("min", infos.getMin());
			}
			if (infos.getMaxSize() > 0) {
				component.getPassThroughAttributes().put("ng-maxlength", infos.getMaxSize());
				final Object ml = AttributeUtilities.getAttribute(component, "maxlength");
				int maxlength=ml instanceof Long? ((Long)ml).intValue():(Integer) ml;
				if (maxlength<0) {
					component.getPassThroughAttributes().put("maxlength", infos.getMaxSize());
				} else 
					component.getPassThroughAttributes().put("ng-maxlength", maxlength);
				

			}
			if (infos.getMinSize() > 0) {
				component.getPassThroughAttributes().put("ng-minlength", infos.getMinSize());
			}
			if (infos.isNumeric()) {
				setType(component, "number");
			} else if (infos.isDate()) {
				setType(component, "date");
//				Iterator<UIComponent> facetsAndChildren = component.getFacetsAndChildren();
//				while (facetsAndChildren.hasNext()) {
//					UIComponent c = facetsAndChildren.next();
//				}
//				FacesContext context = FacesContext.getCurrentInstance();
//				Application application = context.getApplication();
//				final ViewDeclarationLanguage viewDeclarationLanguage = application.getViewHandler()
//				        .getViewDeclarationLanguage(context, context.getViewRoot().getViewId());
//				UIComponent converter = viewDeclarationLanguage
//				        .createComponent(context, "http://java.sun.com/jsf/core", "convertDateTime", null);
//				Locale locale = context.getExternalContext().getRequestLocale();
////				converter.setLocal(locale);
//				component.getChildren().add(converter);
			} else if (infos.isBoolean()) {
				setType(component, "checkbox");
			}
		}
		return VisitResult.ACCEPT;
	}

	private void setType(UIComponent component, String type) {
		if (component.getClass().getName().equals("org.primefaces.component.inputtext.InputText")) {
			Method method;
			try {
				method = component.getClass().getMethod("getType");
				Object previousType = method.invoke(component);
				if (previousType == null || "text".equals(previousType)) {
					method = component.getClass().getMethod("setType", String.class);
					method.invoke(component, type);
				}
			} catch (ReflectiveOperationException e) {
				// catch block required by compiler, can't happen in reality
			}
		} else {
			if (null == component.getAttributes().get("type") && null == component.getPassThroughAttributes().get("type")) {
				component.getPassThroughAttributes().put("type", type);
			}
		}
	}

}
