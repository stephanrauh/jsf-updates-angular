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
package de.beyondjava.angularFaces.core.tagTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.view.Location;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributes;

/**
 * Alternative - simpler, more flexible, less optimized - version of TagAttributesImpl.
 */
public final class AFTagAttributes extends TagAttributes {

    private TagAttribute[] attrs;

    private Tag tag;

    public AFTagAttributes(TagAttribute[] attrs) {
        init(attrs);
    }

	private void init(TagAttribute[] attrs) {
		this.attrs = attrs;
	}
	
	public void addAttribute(Location location, String ns, String myLocalName, String qName, String value) {
		TagAttribute[] newAttrs = Arrays.copyOf(attrs, attrs.length+1);
		newAttrs[attrs.length] = TagAttributeUtilities.createTagAttribute(location, ns, myLocalName, qName, value);
		init(newAttrs);
	}

    /**
     * Return an array of all TagAttributesImpl in this set
     * 
     * @return a non-null array of TagAttributesImpl
     */
    @Override
    public TagAttribute[] getAll() {
        return this.attrs;
    }

    /**
     * Using no namespace, find the TagAttribute
     * 
     * @see #get(String, String)
     * @param localName
     *            tag attribute name
     * @return the TagAttribute found, otherwise null
     */
    @Override
    public TagAttribute get(String localName) {
        return get("", localName);
    }

    /**
     * Find a TagAttribute that matches the passed namespace and local name.
     * 
     * @param ns
     *            namespace of the desired attribute
     * @param localName
     *            local name of the attribute
     * @return a TagAttribute found, otherwise null
     */
    @Override
    public TagAttribute get(String ns, String localName) {
        if (ns != null && localName != null)
        	for (TagAttribute a:attrs)
        		if (localName.equals(a.getLocalName()))
        			if (ns.equals(a.getNamespace()))
        				return a;
        return null;
    }

    /**
     * This method is used exclusively to get the pass through attributes!
     * Namespaces http://xmlns.jcp.org/jsf/passthrough and http://xmlns.jcp.org/jsf/passthrough
     * Get all TagAttributesImpl for the passed namespace
     * 
     * @param namespace
     *            namespace to search
     * @return a non-null array of TagAttributesImpl
     */
    @Override
    public TagAttribute[] getAll(String namespace) {
    	if (namespace==null) namespace="";
    	List<TagAttribute> list = new ArrayList<TagAttribute>();
    	for (TagAttribute a:attrs) {
    		if (namespace.equals(a.getNamespace())) {
    			list.add(a);
    		}
    	}
    	TagAttribute[] result = new TagAttribute[list.size()];
    	list.toArray(result);
    	return result;
    }

    @Override
    public String[] getNamespaces() {
    	Map<String, String> m = new HashMap<String, String>();
    	for (TagAttribute a:attrs) {
    		String namespace = a.getNamespace();
    		if (!m.containsKey(namespace)) {
    			m.put(namespace, namespace);
    		}
    	}
    	String[] namespaces = new String[m.size()];
    	m.values().toArray(namespaces);
        return namespaces;
    }

    @Override
    public Tag getTag() {
        return this.tag;
    }
    
    public void setTag(Tag tag) {
        this.tag = tag;
        for (TagAttribute a : attrs) {
            a.setTag(tag);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.attrs.length; i++) {
            sb.append(this.attrs[i]);
            sb.append(' ');
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
