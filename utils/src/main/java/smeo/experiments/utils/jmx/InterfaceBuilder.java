/*
 * InterfaceBuilder.java
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamically building a MBean interface directly writing byte code
 * 
 */
public class InterfaceBuilder {
	private static final int CONSTANT_Utf8 = 1, CONSTANT_Class = 7;
	private int poolIndex = 1;

	private final Map<List<?>, Integer> poolMap = new LinkedHashMap<List<?>, Integer>();

	private InterfaceBuilder() {
	}

	/**
	 * Return the byte code for an interface called {@code name} that
	 * contains the given {@code methods}. Every method in the generated
	 * interface will be declared to throw {@link Exception}.
	 */
	public static byte[] buildInterface(String name, XMethod[] methods) {
		try {
			return new InterfaceBuilder().build(name, methods);
		} catch (IOException e) {
			// we're only writing arrays, so this "can't happen"
			throw new RuntimeException(e);
		}
	}

	private byte[] build(String name, XMethod[] methods) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);

		dout.writeInt(0xcafebabe); // u4 magic
		dout.writeShort(0); // u2 minor_version
		dout.writeShort(45); // u2 major_version (Java 1.0.2)

		byte[] afterConstantPool = buildAfterConstantPool(name, methods);

		writeConstantPool(dout);
		dout.write(afterConstantPool);
		return bout.toByteArray();
	}

	private byte[] buildAfterConstantPool(String name, XMethod[] methods)
			throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);

		dout.writeShort(Modifier.PUBLIC | Modifier.INTERFACE | Modifier.ABSTRACT);
		// u2 access_flags
		dout.writeShort(classConstant(name));
		// u2 this_class
		dout.writeShort(classConstant(Object.class.getName()));
		// u2 super_class
		dout.writeShort(0); // u2 interfaces_count
		dout.writeShort(0); // u2 fields_count

		dout.writeShort(methods.length); // u2 methods_count
		for (int i = 0; i < methods.length; i++) {
			dout.writeShort(Modifier.PUBLIC | Modifier.ABSTRACT);
			// u2 access_flags
			dout.writeShort(stringConstant(methods[i].getName()));
			// u2 name_index
			dout.writeShort(stringConstant(methodDescriptor(methods[i])));
			// u2 descriptor_index
			dout.writeShort(1); // u2 attributes_count
			dout.writeShort(stringConstant("Exceptions"));
			// u2 attribute_name_index
			dout.writeInt(4); // u4 attribute_length:
			dout.writeShort(1); // (u2 number_of_exceptions
			dout.writeShort(classConstant(Exception.class.getName()));
			// + u2 exception_index)
		}

		dout.writeShort(0); // u2 attributes_count (for class)
		return bout.toByteArray();
	}

	private String classCode(Class<?> c) {
		if (c == void.class) {
			return "V";
		}
		Class<?> arrayClass = Array.newInstance(c, 0).getClass();
		return arrayClass.getName().substring(1).replace('.', '/');
	}

	private int classConstant(String s) {
		int classNameIndex = stringConstant(s.replace('.', '/'));
		return constant(CONSTANT_Class, classNameIndex);
	}

	private int constant(Object... data) {
		List<?> dataList = Arrays.asList(data);
		if (poolMap.containsKey(dataList)) {
			return poolMap.get(dataList);
		}
		poolMap.put(dataList, poolIndex);
		return poolIndex++;
	}

	private String methodDescriptor(XMethod method) {
		StringBuilder sb = new StringBuilder("(");
		for (Class<?> param : method.getParameterTypes()) {
			sb.append(classCode(param));
		}
		sb.append(")").append(classCode(method.getReturnType()));
		return sb.toString();
	}

	private int stringConstant(String s) {
		return constant(CONSTANT_Utf8, s);
	}

	private void writeConstantPool(DataOutputStream dout) throws IOException {
		dout.writeShort(poolIndex);
		int i = 1;
		for (List<?> data : poolMap.keySet()) {
			assert (poolMap.get(data).equals(i++));
			int tag = (Integer) data.get(0);
			dout.writeByte(tag); // u1 tag
			switch (tag) {
			case CONSTANT_Utf8:
				dout.writeUTF((String) data.get(1));
				break; // u2 length + u1 bytes[length]
			case CONSTANT_Class:
				dout.writeShort((Integer) data.get(1));
				break; // u2 name_index
			default:
				throw new AssertionError();
			}
		}
	}
}
