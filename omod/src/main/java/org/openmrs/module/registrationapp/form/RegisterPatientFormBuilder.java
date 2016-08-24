/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.registrationapp.form;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.registrationapp.model.Field;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationapp.model.Question;
import org.openmrs.module.registrationapp.model.Section;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Builds a registration form structure from the app configuration.
 */
public class RegisterPatientFormBuilder {

	protected final static Log log = LogFactory.getLog(RegisterPatientFormBuilder.class);

	/**
	 * Builds the navigable form structure for the specified app descriptor
	 *
	 * @param app the app descriptor
	 * @return the form structure
	 * @throws IOException
	 * @should flatten the widget config
	 */
	public static NavigableFormStructure buildFormStructure(AppDescriptor app) throws IOException {
		NavigableFormStructure formStructure = new NavigableFormStructure();

		// preload the demographics section; if there is a user-specified demographics section, it will override this, but remain at the front by rules of a LinkedHashMap
		Section demographics = new Section();
		demographics.setId("demographics");
		demographics.setLabel("registrationapp.patient.demographics.label");
		formStructure.addSection(demographics);

		ArrayNode sections = (ArrayNode) app.getConfig().get("sections");
		for (JsonNode i : sections) {
			ObjectNode config = (ObjectNode) i;

			ObjectMapper objectMapper = new ObjectMapper();
			Section section = objectMapper.convertValue(config, Section.class);

			if (section.getQuestions() != null) {
				for (Question question : section.getQuestions()) {
					if (question.getFields() != null) {
						for (Field field : question.getFields()) {
							ObjectNode widget = field.getWidget();
							String providerName = widget.get("providerName").getTextValue();
							String fragmentId = widget.get("fragmentId").getTextValue();
							JsonNode fieldConfig = widget.get("config");
							//Groovy doesn't know how to handle ArrayNode and ObjectNode therefore we need to convert 
							//them to List and Map respectively. Also TextNode.toString() includes quotes, we need
							//to extract the actual text value excluding the quotes
							FragmentConfiguration fragConfig = new FragmentConfiguration((Map) flatten(fieldConfig));
							FragmentRequest fragmentRequest = new FragmentRequest(providerName, fragmentId, fragConfig);
							field.setFragmentRequest(fragmentRequest);
						}
					}
				}
			}

			formStructure.addSection(section);
		}

		return formStructure;
	}

	/**
	 * A utility method that converts the specified JsonNode to a value that we can be used in
	 * groovy. If it's a TextNode it extracts the actual text and if it's an ArrayNode or ObjectNode
	 * it gets converted to a List or Map respectively. Note that the method returns the same value
	 * for other node types and recursively applies the same logic to nested arrays and objects.
	 *
	 * @param node a JsonNode to flatten
	 * @return the flattened value
	 */
	private static Object flatten(JsonNode node) {
		Object obj = node;
		if (node != null) {
			if (node.isTextual()) {
				obj = node.getTextValue();
			} else if (node.isBoolean()) {
				obj = node.getBooleanValue();
			} else if (node.isNumber()) {
				obj = node.getNumberValue();
			} else if (node.isArray()) {
				List<Object> list = new ArrayList<Object>();
				Iterator<JsonNode> itemIterator = node.getElements();
				while (itemIterator.hasNext()) {
					list.add(flatten(itemIterator.next()));
				}
				obj = list;
			} else if (node.isObject()) {
				Map<String, Object> map = new HashMap<String, Object>();
				Iterator<String> fieldNameIterator = node.getFieldNames();
				while (fieldNameIterator.hasNext()) {
					String fName = fieldNameIterator.next();
					map.put(fName, flatten(node.get(fName)));
				}
				obj = map;
			}
		}
		return obj;
	}

	public static void resolvePersonAttributeFields(NavigableFormStructure form, Person person,
													Map<String, String[]> parameterMap) {
		List<Field> fields = form.getFields();
		if (fields != null && fields.size() > 0) {
			for (Field field : fields) {
				if (StringUtils.equals(field.getType(), "personAttribute")) {
					String[] parameterValues = parameterMap.get(field.getFormFieldName());
					if (parameterValues != null && parameterValues.length > 0) {
						if (parameterValues.length > 1) {
							log.warn("Multiple values for a single person attribute type not supported, ignoring extra values");
						}
						String parameterValue = parameterValues[0];
						if (parameterValue != null) {
							PersonAttributeType personAttributeByUuid = Context.getPersonService()
									.getPersonAttributeTypeByUuid(field.getUuid());
							if (personAttributeByUuid != null) {
								PersonAttribute attribute = new PersonAttribute(personAttributeByUuid, parameterValue);
								person.addAttribute(attribute);
							}
						}
					}
				}
			}
		}
	}

	public static void resolvePatientIdentifierFields(NavigableFormStructure form, Patient patient,
													Map<String, String[]> parameterMap) {
		List<Field> fields = form.getFields();
		if (fields != null && fields.size() > 0) {
			for (Field field : fields) {
				if (StringUtils.equals(field.getType(), "patientIdentifier")) {
					String[] parameterValues = parameterMap.get(field.getFormFieldName());
					if (parameterValues != null && parameterValues.length > 0) {
						if (parameterValues.length > 1) {
							log.warn("Multiple values for a single patient identifier type not supported, ignoring extra values");
						}
						String parameterValue = parameterValues[0];
						if (parameterValue != null) {
							PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(field.getUuid());
							if (identifierType  != null) {

								// void any existing identifiers of this type
								for (PatientIdentifier oldIdentifier : patient.getPatientIdentifiers(identifierType)) {
									oldIdentifier.setVoided(true);
									oldIdentifier.setVoidedBy(Context.getAuthenticatedUser());
									oldIdentifier.setDateVoided(new Date());
									oldIdentifier.setVoidReason("updated via registration app");
								}

								// add the new identifier
								PatientIdentifier identifier = new PatientIdentifier(parameterValue, identifierType, null);
								patient.addIdentifier(identifier);
							}
						}
					}
				}
			}
		}
	}
}
