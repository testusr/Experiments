/*
 * InterfaceClassLoader.java
 *                ,
 * Copyright 2006 Eamonn McManus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package smeo.experiments.utils.jmx;

/**
 * A ClassLoader that builds arbitrary interfaces containing arbitrary lists
 * of methods.
 */
public class InterfaceClassLoader extends ClassLoader {

	public InterfaceClassLoader(ClassLoader parent) {
		super(parent);
	}

	public synchronized Class<?> findOrBuildInterface(String name, XMethod[] methods) {
		Class<?> c;
		c = findLoadedClass(name);
		if (c != null) {
			return c;
		}
		byte[] classBytes = InterfaceBuilder.buildInterface(name, methods);
		return defineClass(name, classBytes, 0, classBytes.length);
	}

}
