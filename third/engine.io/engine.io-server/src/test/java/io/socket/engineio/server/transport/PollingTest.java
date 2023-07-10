package io.socket.engineio.server.transport;

import io.socket.engineio.server.Emitter;
import io.socket.engineio.server.parser.Parser;
import io.socket.engineio.server.parser.Packet;
import io.socket.engineio.server.HttpServletResponseImpl;
import io.socket.engineio.server.ServletInputStreamWrapper;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
public final class PollingTest {

    @Test
    public void testName() {
        final Polling polling = new Polling(new Object(), Parser.PROTOCOL_V4);

        assertEquals(Polling.NAME, polling.getName());
    }

    @Test
    public void testWritable_normal() {
        final Polling polling = new Polling(new Object(), Parser.PROTOCOL_V4);

        assertFalse(polling.isWritable());
    }

    @Test
    public void testOnRequest_error() throws IOException {
        final Polling polling = Mockito.spy(new Polling(new Object(), Parser.PROTOCOL_V4));

        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doAnswer(invocationOnMock -> "DELETE").when(request).getMethod();
        Mockito.doAnswer(invocationOnMock -> {
            final HashMap<String, String> queryMap = new HashMap<>();
            queryMap.put("transport", Polling.NAME);
            return queryMap;
        }).when(request).getAttribute("query");

        final HttpServletResponseImpl response = new HttpServletResponseImpl();

        polling.onRequest(request, response);

        assertEquals(500, response.getStatus());
    }

    @Test
    public void testOnRequest_poll() throws IOException {
        final Polling polling = Mockito.spy(new Polling(new Object(), Parser.PROTOCOL_V4));

        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doAnswer(invocationOnMock -> "GET").when(request).getMethod();
        Mockito.doAnswer(invocationOnMock -> {
            final HashMap<String, String> queryMap = new HashMap<>();
            queryMap.put("transport", Polling.NAME);
            return queryMap;
        }).when(request).getAttribute("query");

        final HttpServletResponseImpl response = new HttpServletResponseImpl();

        final Emitter.Listener drainListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            polling.send(new ArrayList<Packet<?>>(){{
                add(new Packet<>(Packet.MESSAGE, "Test Data"));
            }});
            return null;
        }).when(drainListener).call();
        polling.on("drain", drainListener);

        polling.onRequest(request, response);

        Mockito.verify(drainListener, Mockito.times(1)).call();

        final String responseString = new String(response.getByteOutputStream().toByteArray(), StandardCharsets.UTF_8);
        Parser.PROTOCOL_V4.decodePayload(responseString, (packet, index, total) -> {
            assertEquals(1, total);
            assertEquals(Packet.MESSAGE, packet.type);
            assertEquals("Test Data", packet.data);
            return true;
        });
    }

    @Test
    public void testOnRequest_poll_jsonp() throws IOException {
        final Polling polling = Mockito.spy(new Polling(new Object(), Parser.PROTOCOL_V4));

        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doAnswer(invocationOnMock -> "GET").when(request).getMethod();
        Mockito.doAnswer(invocationOnMock -> {
            final HashMap<String, String> queryMap = new HashMap<>();
            queryMap.put("transport", Polling.NAME);
            queryMap.put("j", "100");
            return queryMap;
        }).when(request).getAttribute("query");

        final HttpServletResponseImpl response = new HttpServletResponseImpl();

        final Emitter.Listener drainListener = Mockito.mock(Emitter.Listener.class);
        Mockito.doAnswer(invocation -> {
            polling.send(new ArrayList<Packet<?>>(){{
                add(new Packet<>(Packet.MESSAGE, "Test Data"));
            }});
            return null;
        }).when(drainListener).call();
        polling.on("drain", drainListener);

        polling.onRequest(request, response);

        Mockito.verify(drainListener, Mockito.times(1)).call();

        final String responseString = new String(response.getByteOutputStream().toByteArray(), StandardCharsets.UTF_8);
        assertTrue(responseString.startsWith("___eio[100]("));

        final String payloadString = responseString.substring("___eio[100](".length() + 1, responseString.length() - 2);
        Parser.PROTOCOL_V4.decodePayload(payloadString, (packet, index, total) -> {
            assertEquals(1, total);
            assertEquals(Packet.MESSAGE, packet.type);
            assertEquals("Test Data", packet.data);
            return true;
        });
    }

    @Test
    public void testOnRequest_data() {
        final String messageData = "Test Data";

        final Polling polling = Mockito.spy(new Polling(new Object(), Parser.PROTOCOL_V4));
        polling.on("packet", args -> {
            final Packet<?> packet = (Packet<Object>) args[0];
            assertEquals(Packet.MESSAGE, packet.type);
            assertEquals(messageData, packet.data);
        });

        final Packet<String> requestPacket = new Packet<>(Packet.MESSAGE, messageData);
        Parser.PROTOCOL_V4.encodePayload(new ArrayList<Packet<?>>() {{ add(requestPacket); }}, true, dataString -> {
            final byte[] data = ((String) dataString).getBytes(StandardCharsets.UTF_8);
            final ByteArrayInputStream requestInputStream = new ByteArrayInputStream(data);
            final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            Mockito.doAnswer(invocationOnMock -> "POST").when(request).getMethod();
            Mockito.doAnswer(invocationOnMock -> {
                final HashMap<String, String> queryMap = new HashMap<>();
                queryMap.put("transport", Polling.NAME);
                return queryMap;
            }).when(request).getAttribute("query");
            Mockito.doAnswer(invocationOnMock -> "application/octet-stream").when(request).getContentType();
            Mockito.doAnswer(invocationOnMock -> data.length).when(request).getContentLength();
            try {
                Mockito.doAnswer(invocationOnMock -> new ServletInputStreamWrapper(requestInputStream)).when(request).getInputStream();
            } catch (IOException ignore) {
            }

            final HttpServletResponseImpl response = new HttpServletResponseImpl();

            try {
                polling.onRequest(request, response);
            } catch (IOException ignore) {
            }

            Mockito.verify(polling, Mockito.times(1))
                    .emit(Mockito.eq("packet"), Mockito.any(Packet.class));
        });
    }

    @Test
    @Ignore
    public void testOnRequest_data_jsonp() {
        final String messageData = "Test Data";

        final Polling polling = Mockito.spy(new Polling(new Object(), Parser.PROTOCOL_V4));
        polling.on("packet", args -> {
            final Packet<?> packet = (Packet<Object>) args[0];
            assertEquals(Packet.MESSAGE, packet.type);
            assertEquals(messageData, packet.data);
        });

        final byte[] data = "d=10:4Test Data".getBytes(StandardCharsets.UTF_8);
        final ByteArrayInputStream requestInputStream = new ByteArrayInputStream(data);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doAnswer(invocationOnMock -> "POST").when(request).getMethod();
        Mockito.doAnswer(invocationOnMock -> {
            final HashMap<String, String> queryMap = new HashMap<>();
            queryMap.put("transport", Polling.NAME);
            queryMap.put("j", "100");
            return queryMap;
        }).when(request).getAttribute("query");
        Mockito.doAnswer(invocationOnMock -> "application/octet-stream").when(request).getContentType();
        Mockito.doAnswer(invocationOnMock -> data.length).when(request).getContentLength();
        try {
            Mockito.doAnswer(invocationOnMock -> new ServletInputStreamWrapper(requestInputStream)).when(request).getInputStream();
        } catch (IOException ignore) {
        }

        final HttpServletResponseImpl response = new HttpServletResponseImpl();

        try {
            polling.onRequest(request, response);
        } catch (IOException ignore) {
        }

        Mockito.verify(polling, Mockito.times(1))
                .emit(Mockito.eq("packet"), Mockito.any(Packet.class));
    }

    @Test
    public void testOnRequest_async() throws IOException {
        final Polling polling = Mockito.spy(new Polling(new Object(), Parser.PROTOCOL_V4));

        final AsyncContext asyncContext = Mockito.mock(AsyncContext.class);

        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doAnswer(invocationOnMock -> "GET").when(request).getMethod();
        Mockito.doAnswer(invocationOnMock -> {
            final HashMap<String, String> queryMap = new HashMap<>();
            queryMap.put("transport", Polling.NAME);
            return queryMap;
        }).when(request).getAttribute("query");
        Mockito.doAnswer(invocation -> true)
                .when(request)
                .isAsyncSupported();
        Mockito.doAnswer(invocation -> asyncContext)
                .when(request)
                .startAsync();
        Mockito.doAnswer(invocation -> asyncContext)
                .when(request)
                .getAsyncContext();

        final HttpServletResponseImpl response = new HttpServletResponseImpl();

        polling.onRequest(request, response);

        Mockito.verify(polling, Mockito.times(0))
                .send(Mockito.anyList());
        Mockito.verify(request, Mockito.times(1)).startAsync();

        final ArrayList<Packet<?>> sendPacketList = new ArrayList<Packet<?>>(){{
            add(new Packet<>(Packet.MESSAGE, "Test Data"));
        }};
        polling.send(sendPacketList);

        Mockito.verify(polling, Mockito.times(1))
                .send(Mockito.eq(sendPacketList));

        final String responseString = new String(response.getByteOutputStream().toByteArray(), StandardCharsets.UTF_8);
        final Parser.DecodePayloadCallback<Object> decodePayloadCallback = Mockito.mock(Parser.DecodePayloadCallback.class);
        Mockito.doAnswer(invocation -> {
            final Packet<String> packet = invocation.getArgument(0);

            assertEquals(Packet.MESSAGE, packet.type);
            assertEquals("Test Data", packet.data);
            return true;
        }).when(decodePayloadCallback).call(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
        Parser.PROTOCOL_V4.decodePayload(responseString, decodePayloadCallback);
        Mockito.verify(decodePayloadCallback, Mockito.times(1))
                .call(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void testClose_client() {
        final Polling polling = Mockito.spy(new Polling(new Object(), Parser.PROTOCOL_V4));

        final Packet<String> requestPacket = new Packet<>(Packet.CLOSE);
        Parser.PROTOCOL_V4.encodePayload(new ArrayList<Packet<?>>() {{ add(requestPacket); }}, true, dataString -> {
            final byte[] data = ((String) dataString).getBytes(StandardCharsets.UTF_8);
            final ByteArrayInputStream requestInputStream = new ByteArrayInputStream(data);
            final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            Mockito.doAnswer(invocationOnMock -> "POST").when(request).getMethod();
            Mockito.doAnswer(invocationOnMock -> {
                final HashMap<String, String> queryMap = new HashMap<>();
                queryMap.put("transport", Polling.NAME);
                return queryMap;
            }).when(request).getAttribute("query");
            Mockito.doAnswer(invocationOnMock -> "application/octet-stream").when(request).getContentType();
            Mockito.doAnswer(invocationOnMock -> data.length).when(request).getContentLength();
            try {
                Mockito.doAnswer(invocationOnMock -> new ServletInputStreamWrapper(requestInputStream)).when(request).getInputStream();
            } catch (IOException ignore) {
            }

            final HttpServletResponseImpl response = new HttpServletResponseImpl();

            try {
                polling.onRequest(request, response);
            } catch (IOException ignore) {
            }

            Mockito.verify(polling, Mockito.times(1))
                    .emit(Mockito.eq("close"));
        });
    }

    @Test
    public void testClose_server1() throws IOException {
        final Polling polling = Mockito.spy(new Polling(new Object(), Parser.PROTOCOL_V4));

        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.doAnswer(invocationOnMock -> "GET").when(request).getMethod();
        Mockito.doAnswer(invocationOnMock -> {
            final HashMap<String, String> queryMap = new HashMap<>();
            queryMap.put("transport", Polling.NAME);
            return queryMap;
        }).when(request).getAttribute("query");

        final HttpServletResponseImpl response = new HttpServletResponseImpl();

        polling.close();
        polling.onRequest(request, response);

        Mockito.verify(polling, Mockito.times(1))
                .emit(Mockito.eq("drain"));
        Mockito.verify(polling, Mockito.times(1))
                .emit(Mockito.eq("close"));

        final String responseString = response.getByteOutputStream().toString(StandardCharsets.UTF_8);
        Parser.PROTOCOL_V4.decodePayload(responseString, (packet, index, total) -> {
            assertEquals(2, total);
            if (index == 0) {
                assertEquals(Packet.NOOP, packet.type);
            } else if (index == 1) {
                assertEquals(Packet.CLOSE, packet.type);
            }
            return true;
        });
    }

    @Test
    public void testClose_server2() {
        final Polling polling = Mockito.spy(new Polling(new Object(), Parser.PROTOCOL_V4));

        polling.on("drain", args -> polling.close());

        final Packet<String> requestPacket = new Packet<>(Packet.CLOSE);
        Parser.PROTOCOL_V4.encodePayload(new ArrayList<Packet<?>>() {{ add(requestPacket); }}, true, data -> {
            final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            Mockito.doAnswer(invocationOnMock -> "GET").when(request).getMethod();
            Mockito.doAnswer(invocationOnMock -> {
                final HashMap<String, String> queryMap = new HashMap<>();
                queryMap.put("transport", Polling.NAME);
                return queryMap;
            }).when(request).getAttribute("query");

            final HttpServletResponseImpl response = new HttpServletResponseImpl();

            try {
                polling.onRequest(request, response);
            } catch (IOException ignore) {
            }

            Mockito.verify(polling, Mockito.times(1))
                    .emit(Mockito.eq("drain"));
            Mockito.verify(polling, Mockito.times(1))
                    .emit(Mockito.eq("close"));

            final String responseString = new String(response.getByteOutputStream().toByteArray(), StandardCharsets.UTF_8);
            Parser.PROTOCOL_V4.decodePayload(responseString, (packet, index, total) -> {
                assertEquals(1, total);
                assertEquals(Packet.CLOSE, packet.type);
                return true;
            });
        });
    }
}