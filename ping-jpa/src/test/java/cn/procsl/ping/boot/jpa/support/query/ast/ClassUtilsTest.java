
package cn.procsl.ping.boot.jpa.support.query.ast;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilsTest {

    // 示例类用于测试
    static class Example {
        public String getName() {
            return "name";
        }

        public boolean isActive() {
            return true;
        }

        public void setName(String name) {
        }

        public boolean isSomethingElse() {
            return false;
        }

        public int getNumber() {
            return 1;
        }

        public static void staticMethod() {
        } // 静态方法不应被识别

        private void privateMethod() {
        } // 私有方法不应被识别
    }

    @Test
    void testExtractGetAndIsMethods() {
        List<Method> methods = ClassUtils.extractGetAndIsMethods(Example.class);

        assertEquals(4, methods.size(), "Should find 4 get and is methods");

        assertTrue(methods.stream().anyMatch(m -> m.getName().equals("getName")), "Should find getName method");
        assertTrue(methods.stream().anyMatch(m -> m.getName().equals("isActive")), "Should find isActive method");
        assertTrue(methods.stream().anyMatch(m -> m.getName().equals("isSomethingElse")), "Should find isSomethingElse method");
        assertTrue(methods.stream().anyMatch(m -> m.getName().equals("getNumber")), "Should find getNumber method");
    }

    @Test
    void testIsGetMethod() throws NoSuchMethodException {
        Method getNameMethod = Example.class.getMethod("getName");
        assertTrue(ClassUtils.isGetMethod(getNameMethod), "getName should be recognized as get method");

        Method setNameMethod = Example.class.getMethod("setName", String.class);
        assertFalse(ClassUtils.isGetMethod(setNameMethod), "setName should not be recognized as get method");

        Method staticMethod = Example.class.getMethod("staticMethod");
        assertFalse(ClassUtils.isGetMethod(staticMethod), "staticMethod should not be recognized as get method");

        Method privateMethod = Example.class.getDeclaredMethod("privateMethod");
        assertFalse(ClassUtils.isGetMethod(privateMethod), "privateMethod should not be recognized as get method");
    }

    @Test
    void testIsIsMethod() throws NoSuchMethodException {
        Method isActiveMethod = Example.class.getMethod("isActive");
        assertTrue(ClassUtils.isIsMethod(isActiveMethod), "isActive should be recognized as is method");

        Method isSomethingElseMethod = Example.class.getMethod("isSomethingElse");
        assertTrue(ClassUtils.isIsMethod(isSomethingElseMethod), "isSomethingElse should be recognized as is method");

        Method getNumberMethod = Example.class.getMethod("getNumber");
        assertFalse(ClassUtils.isIsMethod(getNumberMethod), "getNumber should not be recognized as is method");

        Method staticMethod = Example.class.getMethod("staticMethod");
        assertFalse(ClassUtils.isIsMethod(staticMethod), "staticMethod should not be recognized as is method");

        Method privateMethod = Example.class.getDeclaredMethod("privateMethod");
        assertFalse(ClassUtils.isIsMethod(privateMethod), "privateMethod should not be recognized as is method");
    }

    @Test
    void testIsSetMethod() throws NoSuchMethodException {
        Method setNameMethod = Example.class.getMethod("setName", String.class);
        assertTrue(ClassUtils.isSetMethod(setNameMethod), "setName should be recognized as set method");

        Method getNameMethod = Example.class.getMethod("getName");
        assertFalse(ClassUtils.isSetMethod(getNameMethod), "getName should not be recognized as set method");

        Method staticMethod = Example.class.getMethod("staticMethod");
        assertFalse(ClassUtils.isSetMethod(staticMethod), "staticMethod should not be recognized as set method");

        Method privateMethod = Example.class.getDeclaredMethod("privateMethod");
        assertFalse(ClassUtils.isSetMethod(privateMethod), "privateMethod should not be recognized as set method");
    }

    @Test
    void testExtractMethodNames() {
        List<Method> methods = ClassUtils.extractGetAndIsMethods(Example.class);
        Set<String> fieldNames = ClassUtils.extractMethodNames(methods);

        assertEquals(4, fieldNames.size(), "Should find 3 unique field names");

        assertTrue(fieldNames.contains("name"), "Should find field name 'name'");
        assertTrue(fieldNames.contains("number"), "Should find field name 'number'");
        assertTrue(fieldNames.contains("active"), "Should find field name 'active'");
        assertTrue(fieldNames.contains("somethingElse"), "Should find field name 'somethingElse'");
    }

    @Test
    void testExtractMethodNamesWithEmptyMethods() {
        // 用于测试没有任何 get, set 和 is 方法的类
        class EmptyClass {
        }

        List<Method> methods = ClassUtils.extractGetAndIsMethods(EmptyClass.class);
        Set<String> fieldNames = ClassUtils.extractMethodNames(methods);

        assertTrue(fieldNames.isEmpty(), "Field names should be empty for a class without get, set, or is methods");
    }

    @Test
    void testIsSimpleTypeWithSimpleTypes() {
        assertTrue(ClassUtils.isSimpleType(String.class), "String should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(Date.class), "Date should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(Long.class), "Long should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(Integer.class), "Integer should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(Double.class), "Double should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(Float.class), "Float should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(Byte.class), "Byte should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(Short.class), "Short should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(Boolean.class), "Boolean should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(Character.class), "Character should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(int.class), "int should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(double.class), "double should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(long.class), "long should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(float.class), "float should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(byte.class), "byte should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(short.class), "short should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(boolean.class), "boolean should be recognized as simple type");
        assertTrue(ClassUtils.isSimpleType(char.class), "char should be recognized as simple type");
    }

    @Test
    void testIsSimpleTypeWithNonSimpleTypes() {
        assertFalse(ClassUtils.isSimpleType(Object.class), "Object should not be recognized as simple type");
        assertFalse(ClassUtils.isSimpleType(Class.class), "Class should not be recognized as simple type");
        assertFalse(ClassUtils.isSimpleType(System.class), "System should not be recognized as simple type");
    }

    @Test
    void testIsSimpleTypeWithNull() {
        assertFalse(ClassUtils.isSimpleType(null), "null should not be recognized as simple type");
    }

    @Test
    void testIsContainerTypeWithContainerTypes() {
        assertTrue(ClassUtils.isContainerType(Collection.class), "Collection should be recognized as container type");
        assertTrue(ClassUtils.isContainerType(List.class), "List should be recognized as container type");
        assertTrue(ClassUtils.isContainerType(Map.class), "Map should be recognized as container type");
        assertTrue(ClassUtils.isContainerType(Set.class), "Set should be recognized as container type");
        assertTrue(ClassUtils.isContainerType(String[].class), "Array should be recognized as container type");
    }

    @Test
    void testIsContainerTypeWithNonContainerTypes() {
        assertFalse(ClassUtils.isContainerType(String.class), "String should not be recognized as container type");
        assertFalse(ClassUtils.isContainerType(Integer.class), "Integer should not be recognized as container type");
        assertFalse(ClassUtils.isContainerType(Object.class), "Object should not be recognized as container type");
    }

    @Test
    void testIsContainerTypeWithNull() {
        assertFalse(ClassUtils.isContainerType(null), "null should not be recognized as container type");
    }


}
