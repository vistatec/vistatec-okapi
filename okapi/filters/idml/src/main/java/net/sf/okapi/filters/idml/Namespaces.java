/*
 * =============================================================================
 *   Copyright (C) 2010-2013 by the Okapi Framework contributors
 * -----------------------------------------------------------------------------
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =============================================================================
 */

package net.sf.okapi.filters.idml;

class Namespaces {

    private static final String DEFAULT_NAMESPACE_PREFIX = "";
    private static final String DEFAULT_NAMESPACE_URI = null;

    private static final String ID_PACKAGE_NAMESPACE_PREFIX = "idPkg";
    private static final String ID_PACKAGE_NAMESPACE_URI = "http://ns.adobe.com/AdobeInDesign/idml/1.0/packaging";

    private static final String XML_NAMESPACE_PREFIX = "xml";
    private static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";

    static Namespace getDefaultNamespace() {
        return new Namespace(DEFAULT_NAMESPACE_PREFIX, DEFAULT_NAMESPACE_URI);
    }

    static Namespace getIdPackageNamespace() {
        return new Namespace(ID_PACKAGE_NAMESPACE_PREFIX, ID_PACKAGE_NAMESPACE_URI);
    }

    static Namespace getXmlNamespace() {
        return new Namespace(XML_NAMESPACE_PREFIX, XML_NAMESPACE_URI);
    }
}
