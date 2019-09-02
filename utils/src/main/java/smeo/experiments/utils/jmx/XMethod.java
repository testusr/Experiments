/*
 * XMethod.java
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

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Object encapsulating the same information as a <code>java.lang.reflect.Method</code> but that we can
 * instantiate explicitly.
 */
public class XMethod {
	private final String name;

	private final Class<?>[] paramTypes;

	private final Class<?> returnType;

	public XMethod(Method m) {
		this(m.getName(), m.getParameterTypes(), m.getReturnType());
	}

	public XMethod(String name, Class<?>[] paramTypes, Class<?> returnType) {
		this.name = name;
		this.paramTypes = paramTypes;
		this.returnType = returnType;
	}

	@Override
	public boolean equals(Object x) {
		if (!(x instanceof XMethod))
			return false;
		XMethod xm = (XMethod) x;
		return (name.equals(xm.name) && returnType.equals(xm.returnType) && Arrays.equals(paramTypes, xm.paramTypes));
	}

	public String getName() {
		return name;
	}

	public Class<?>[] getParameterTypes() {
		return paramTypes.clone();
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public int hashCode() {
		return name.hashCode() + returnType.hashCode() +
				Arrays.hashCode(paramTypes);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(returnType.getName()).append(" ").append(name).append("(");
		String comma = "";
		for (Class<?> paramType : paramTypes) {
			sb.append(comma).append(paramType.getName());
			comma = ", ";
		}
		sb.append(")");
		return sb.toString();
	}
}
