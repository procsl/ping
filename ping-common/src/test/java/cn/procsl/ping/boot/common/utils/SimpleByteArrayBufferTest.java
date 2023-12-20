package cn.procsl.ping.boot.common.utils;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleByteArrayBufferTest {

    @Test
    public void testRead() {
        SimpleByteArrayBuffer buffer = new SimpleByteArrayBuffer(10);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.write(data, 0, data.length);

        byte[] result = new byte[5];
        int bytesRead = buffer.read(result, 0, result.length);

        assertEquals(5, bytesRead);
        assertArrayEquals(data, result);
    }

    @Test
    public void testReadWithInvalidRequest() {
        SimpleByteArrayBuffer buffer = new SimpleByteArrayBuffer(10);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.write(data, 0, data.length);

        byte[] result = new byte[5];

        try {
            buffer.read(result, 0, 10);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The buffer length is not enough to accommodate the read request", e.getMessage());
        }
    }

    @Test
    public void testWrite() {
        SimpleByteArrayBuffer buffer = new SimpleByteArrayBuffer(10);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.write(data, 0, data.length);

        byte[] result = new byte[5];
        int bytesRead = buffer.read(result, 0, result.length);

        assertEquals(5, bytesRead);
        assertArrayEquals(data, result);
    }

    @Test
    public void testWriteWithInvalidRequest() {
        SimpleByteArrayBuffer buffer = new SimpleByteArrayBuffer(10);
        byte[] data = {1, 2, 3, 4, 5};

        try {
            buffer.write(data, 0, 10);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("The buffer length is not enough to accommodate the read request", e.getMessage());
        }
    }

    @Test
    public void testEnsureCapacity() {
        SimpleByteArrayBuffer buffer = new SimpleByteArrayBuffer(5);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.write(data, 0, data.length);

        byte[] result = new byte[5];
        int bytesRead = buffer.read(result, 0, result.length);

        assertEquals(5, bytesRead);
        assertArrayEquals(data, result);
    }

    @Test
    public void testIsEmpty() {
        SimpleByteArrayBuffer buffer = new SimpleByteArrayBuffer(10);
        assertTrue(buffer.isEmpty());

        byte[] data = {1, 2, 3, 4, 5};
        buffer.write(data, 0, data.length);
        assertFalse(buffer.isEmpty());

        byte[] result = new byte[5];
        buffer.read(result, 0, result.length);
        assertTrue(buffer.isEmpty());
    }


    @Test
    public void testRead2() {
        SimpleByteArrayBuffer buffer = new SimpleByteArrayBuffer(10);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.write(data, 0, data.length);

        byte[] result = new byte[5];
        int bytesRead = buffer.read(result, 0, result.length);

        Assertions.assertEquals(bytesRead, 5);
        Assertions.assertEquals(result[0], 1);
        Assertions.assertEquals(result[1], 2);
        Assertions.assertEquals(result[2], 3);
        Assertions.assertEquals(result[3], 4);
        Assertions.assertEquals(result[4], 5);
    }

    @Test
    public void testWrite2() {
        SimpleByteArrayBuffer buffer = new SimpleByteArrayBuffer(10);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.write(data, 0, data.length);

        Assertions.assertEquals(buffer.size(), 5);
        Assertions.assertFalse(buffer.isEmpty());
    }

    @Test
    public void testEnsureCapacity2() {
        SimpleByteArrayBuffer buffer = new SimpleByteArrayBuffer(5);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.write(data, 0, data.length);

        byte[] newData = {6, 7, 8};
        buffer.write(newData, 0, newData.length);

        Assertions.assertEquals(buffer.size(), 8);
        Assertions.assertFalse(buffer.isEmpty());
    }

}