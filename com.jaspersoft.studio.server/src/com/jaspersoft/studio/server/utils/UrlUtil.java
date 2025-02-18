/*******************************************************************************
 * Copyright © 2010-2023. Cloud Software Group, Inc. All rights reserved.
 *******************************************************************************/
package com.jaspersoft.studio.server.utils;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class UrlUtil {
	public static URI fixUri(URI uri) {
		if (uri.getHost() == null) {
			try {
				URL url = new URL(uri.toASCIIString());

				final Field hostField = URI.class.getDeclaredField("host");
				hostField.setAccessible(true);
				hostField.set(uri, url.getHost());

				final Field portField = URI.class.getDeclaredField("port");
				portField.setAccessible(true);
				portField.set(uri, url.getPort());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return uri;
	}
}
