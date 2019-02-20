/*******************************************************************************
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 * 
 * Unless you have purchased  a commercial license agreement from Jaspersoft,
 * the following license terms  apply:
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.jaspersoft.studio.data.random;

import net.sf.jasperreports.data.DataAdapterContributorFactory;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.extensions.ExtensionsRegistry;
import net.sf.jasperreports.extensions.ExtensionsRegistryFactory;
import net.sf.jasperreports.extensions.SingletonExtensionRegistry;

public class RandomDataAdapterServiceExtensionsRegistryFactory implements ExtensionsRegistryFactory {
	private static final ExtensionsRegistry extensionsRegistry = new SingletonExtensionRegistry<DataAdapterContributorFactory>(
			DataAdapterContributorFactory.class, RandomDataAdapterServiceFactory.getInstance());

	public ExtensionsRegistry createRegistry(String registryId, JRPropertiesMap properties) {
		return extensionsRegistry;
	}
}
