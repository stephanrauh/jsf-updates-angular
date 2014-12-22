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

import java.util.Collection;
import java.util.Locale;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

import de.beyondjava.angularFaces.components.puiModelSync.PuiModelSync;
import de.beyondjava.angularFaces.components.puiModelSync.PuiScriptRenderer;
import de.beyondjava.angularFaces.core.tagTransformer.AngularTagDecorator;

/**
 * Converts EL expressions to Angular expressions
 */
public class PuiAngularTransformer implements SystemEventListener {

	private static final Logger LOGGER = Logger.getLogger("de.beyondjava.angularFaces.core.transformation.PuiAngularTransformer");

	static {
		LOGGER.info("AngularFaces utility class PuiELTransformer ready for use.");
	}

	@Override
	public void processEvent(SystemEvent event) throws AbortProcessingException {
		Object source = event.getSource();
		if (source instanceof UIViewRoot) {
			long timer = System.nanoTime();

			final FacesContext context = FacesContext.getCurrentInstance();
			boolean isProduction = context.isProjectStage(ProjectStage.Production);
			if ((!isProduction) && (!AngularTagDecorator.isActive())) {
				FacesContext
						.getCurrentInstance()
						.addMessage(
								null,
								new FacesMessage(
										FacesMessage.SEVERITY_FATAL,
										"Configuration error: ",
										"Add javax.faces.FACELETS_DECORATORS=de.beyondjava.angularFaces.core.tagTransformer.AngularTagDecorator to the context init parameters in the web.xml"));
				LOGGER.severe("Add javax.faces.FACELETS_DECORATORS=de.beyondjava.angularFaces.core.tagTransformer.AngularTagDecorator to the context init parameters in the web.xml");
			} else {
				final UIViewRoot root = (UIViewRoot) source;
				boolean ajaxRequest = context.getPartialViewContext().isAjaxRequest();
				boolean angularFacesRequest = ajaxRequest && isAngularFacesRequest();
				if (!angularFacesRequest || PuiModelSync.isJSFAttributesTableEmpty()) {
					PuiModelSync.initJSFAttributesTable();
					FindNGControllerCallback ngControllerCallback = new FindNGControllerCallback();
					LOGGER.fine(((System.nanoTime() - timer) / 1000) / 1000.0d + " ms find NGControllerCallback");
					root.visitTree(new FullVisitContext(context), ngControllerCallback);
					if (!angularFacesRequest) {
						addJavascript(root, context, isProduction);
					}
					time("add NGModel", new Runnable() {
						public void run() {
							root.visitTree(new FullVisitContext(context), new AddNGModelAndIDCallback());
						}
					});
					time("add type information", new Runnable() {
						public void run() {
							root.visitTree(new FullVisitContext(context), new AddTypeInformationCallback());
						}
					});
					if (!ajaxRequest) {
						time("internationalization", new Runnable() {
							public void run() {
								root.visitTree(new FullVisitContext(context), new TranslationCallback());
							}
						});
					}
				}
			}
			long time = System.nanoTime() - timer;
			LOGGER.fine((time / 1000) / 1000.0d + " ms");
		}
	}

	private void addJavascript(UIViewRoot root, FacesContext context, boolean isProduction) {
		{
			UIOutput output = new UIOutput();
			output.setRendererType("javax.faces.resource.Script");
			if (isProduction)
				output.getAttributes().put("name", "jquery.min-1.11.1.js");
			else
				output.getAttributes().put("name", "jquery-1.11.1.js");
			output.getAttributes().put("library", "jQuery");
			root.addComponentResource(context, output, "head");
		}
		{
			UIOutput output = new UIOutput();
			output.setRendererType("javax.faces.resource.Script");
			if (isProduction) {
				output.getAttributes().put("name", "angular.min.js");
			} else {
				output.getAttributes().put("name", "angular.js");
			}
			output.getAttributes().put("library", "AngularJS");

			root.addComponentResource(context, output, "head");
		}
		{
			UIOutput output = new UIOutput();
			output.setRendererType("javax.faces.resource.Script");
			if (isProduction) {
				output.getAttributes().put("name", "angular-messages.min.js");
			} else {
				output.getAttributes().put("name", "angular-messages.js");
			}
			output.getAttributes().put("library", "AngularJS");

			root.addComponentResource(context, output, "head");
		}
		{
			UIOutput output = new UIOutput();
			output.setRendererType("javax.faces.resource.Script");
			if (isProduction) {
				output.getAttributes().put("name", "angularfaces.all.min.js");
			} else {
				output.getAttributes().put("name", "angularfaces-core.js");
			}
			output.getAttributes().put("library", "AngularFaces");
			root.addComponentResource(context, output, "head");
		}
		{
			UIOutput output = new UIOutput();
			output.setRendererType("javax.faces.resource.Script");
			if (isProduction) {
			} else {
				output.getAttributes().put("name", "angularfaces-directives.js");
			}
			output.getAttributes().put("library", "AngularFaces");
			root.addComponentResource(context, output, "head");
		}
		{
			UIOutput output = new UIOutput();
			output.setRendererType("javax.faces.resource.Script");
			if (isProduction) {
				output.getAttributes().put("name", "jua-0.1.0-min.js");
			} else {
				output.getAttributes().put("name", "jua-0.1.0.js");
			}
			output.getAttributes().put("library", "AngularFaces");
			root.addComponentResource(context, output, "head");
		}

		{
			Locale locale = context.getExternalContext().getRequestLocale();
			String language = locale.getLanguage();
			UIOutput output = new UIOutput();
			output.setRendererType("javax.faces.resource.Script");
			output.getAttributes().put("name", "messages_" + language + ".js");
			output.getAttributes().put("library", "AngularFaces");
			root.addComponentResource(context, output, "head");
		}
	}

	@Override
	public boolean isListenerForSource(Object source) {
		if (source instanceof UIComponent) {
			return true;
		}
		return false;
	}

	private void time(String description, Runnable runnable) {
		long timer = System.nanoTime();
		runnable.run();
		long time = System.nanoTime() - timer;
		LOGGER.fine((time / 1000) / 1000.0d + " ms " + description);
	}

	private boolean isAngularFacesRequest() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		PartialViewContext pvc = ctx.getPartialViewContext();
		Collection<String> myRenderIds = pvc.getRenderIds();
		boolean isAngularFacesRequest = false;
		if (null != myRenderIds) {
			if (myRenderIds.contains("angular")) {
				isAngularFacesRequest = true;
			} else {
				for (Object id : myRenderIds) {
					if (id instanceof String) {
						if (((String) id).endsWith(":angular")) {
							isAngularFacesRequest = true;
							break;
						}
					}
				}
			}
		}
		return isAngularFacesRequest;
	}

}
