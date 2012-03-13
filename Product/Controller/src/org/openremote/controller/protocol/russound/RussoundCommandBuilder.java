package org.openremote.controller.protocol.russound;

import java.util.List;

import org.jdom.Element;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.utils.CommandUtil;

/**
 * 
 * @author Marcus Redeker
 */
public class RussoundCommandBuilder implements CommandBuilder {

	/**
	 * {@inheritDoc}
	 */
	public Command build(Element element) {

		RussoundCommand cmd = new RussoundCommand();

		List<Element> propertyElements = element.getChildren(CommandBuilder.XML_ELEMENT_PROPERTY, element.getNamespace());
		for (Element el : propertyElements) {
			if ("command".equals(el.getAttributeValue(CommandBuilder.XML_ATTRIBUTENAME_NAME))) {
				cmd.setCommand(CommandUtil.parseStringWithParam(element, el.getAttributeValue(CommandBuilder.XML_ATTRIBUTENAME_VALUE)));
			}
		}
		return cmd;
	}

}
