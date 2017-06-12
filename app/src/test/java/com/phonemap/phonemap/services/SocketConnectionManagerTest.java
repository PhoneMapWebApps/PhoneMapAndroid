package com.phonemap.phonemap.services;

import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;

import com.phonemap.phonemap.constants.Phone;
import com.phonemap.phonemap.constants.Preferences;
import com.phonemap.phonemap.constants.Sockets;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.phonemap.phonemap.constants.Requests.FORCE_TASK;
import static com.phonemap.phonemap.constants.Requests.TASK_ID;
import static io.socket.emitter.Emitter.Listener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class SocketConnectionManagerTest {
    @Mock Socket mockSocket;
    @Mock Bundle mockBundle;
    @Mock Messenger mockMessenger;

    @Captor ArgumentCaptor<String> stringCaptor;
    @Captor ArgumentCaptor<Emitter.Listener> listenerCaptor;
    @Captor ArgumentCaptor<JSONObject> payloadCaptor;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void testConstructorConnectsSocket() {
        new SocketConnectionManager(mockSocket);
        verify(mockSocket, times(1)).connect();
        verify(mockSocket, times(7)).on(stringCaptor.capture(), listenerCaptor.capture());
    }

    @Test
    public void nullDoesntBundleToNull() {
        SocketConnectionManager socketConnectionManager = new SocketConnectionManager(mockSocket);
        JSONObject object = socketConnectionManager.bundleToJSON(null);
        assertNotEquals(object, null);
        assertEquals(object.length(), 0);
    }

    @Test
    public void emptyBundleDoesntBundleToNull() {
        SocketConnectionManager socketConnectionManager = new SocketConnectionManager(mockSocket);
        JSONObject object = socketConnectionManager.bundleToJSON(mockBundle);
        assertNotEquals(object, null);
        assertEquals(object.length(), 0);
    }

    @Test
    public void canBundleOneElementBundle() {
        SocketConnectionManager socketConnectionManager = new SocketConnectionManager(mockSocket);
        when(mockBundle.isEmpty()).thenReturn(false);

        Set<String> set = new HashSet<>();
        set.add("foo");

        when(mockBundle.keySet()).thenReturn(set);
        when(mockBundle.get("foo")).thenReturn("bar");

        JSONObject object = socketConnectionManager.bundleToJSON(mockBundle);
        assertEquals(object.length(), 1);
        assertTrue(object.has("foo"));

        try {
            assertEquals(object.get("foo"), "bar");
        } catch (JSONException e) {
            fail("Did not expect an exception");
        }
    }

    @Test
    public void canBundleMultiElementBundle() {
        SocketConnectionManager socketConnectionManager = new SocketConnectionManager(mockSocket);
        when(mockBundle.isEmpty()).thenReturn(false);

        Set<String> set = new  HashSet<>();
        set.add("foo");
        set.add("bar");

        when(mockBundle.keySet()).thenReturn(set);
        when(mockBundle.get("foo")).thenReturn("bar");
        when(mockBundle.get("bar")).thenReturn("baz");

        JSONObject object = socketConnectionManager.bundleToJSON(mockBundle);
        assertEquals(object.length(), 2);
        assertTrue(object.has("foo"));
        assertTrue(object.has("bar"));

        try {
            assertEquals(object.get("foo"), "bar");
            assertEquals(object.get("bar"),"baz");
        } catch (JSONException e) {
            fail("Did not expect an exception");
        }
    }

    private Socket testSocket() {
        Socket mockSocket = Mockito.mock(Socket.class);
        doReturn(null).when(mockSocket).on(eq(Socket.EVENT_CONNECT), listenerCaptor.capture());
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                listenerCaptor.getValue().call();
                return null;
            }
        }).when(mockSocket).connect();
        return mockSocket;
    }

    private SocketConnectionManager testSocketConnectionManager(final String TEST_PHONE_ID, final Socket mockSocket) {
        final int preferredTask = 1;
        final boolean autostartEnabled = true;

        SocketConnectionManager socketConnectionManager = new SocketConnectionManager(mockSocket) {
            @Override
            public Bundle emptyBundle() {
                Bundle bundle = Mockito.mock(Bundle.class);
                doNothing().when(bundle).putString(anyString(), anyString());
                doNothing().when(bundle).putInt(anyString(), anyInt());
                doNothing().when(bundle).putBoolean(anyString(), anyBoolean());
                doReturn(preferredTask).when(bundle).get(TASK_ID);
                doReturn(autostartEnabled).when(bundle).get(FORCE_TASK);

                Set<String> keys = new HashSet<>();
                keys.add(TASK_ID);
                keys.add(FORCE_TASK);
                doReturn(keys).when(bundle).keySet();

                return bundle;
            }
        };
        socketConnectionManager.phone = new Phone(socketConnectionManager) {
            @Override
            public String id() {
                return TEST_PHONE_ID;
            }
        };
        socketConnectionManager.preferences = new Preferences(socketConnectionManager) {
            @Override
            public int preferredTask() {
                return preferredTask;
            }

            @Override
            public boolean autostartEnabled() {
                return autostartEnabled;
            }
        };
        return socketConnectionManager;
    }

    @Test
    public void makesCorrectServerCallOnInstantiation() throws JSONException {
        final String TEST_PHONE_ID = "test";

        Socket mockSocket = testSocket();
        doReturn(true).when(mockSocket).connected();

        SocketConnectionManager socketConnectionManager = testSocketConnectionManager(TEST_PHONE_ID, mockSocket);
        socketConnectionManager.addReadyRunner(mockMessenger);

        verify(mockSocket).on(eq(Socket.EVENT_CONNECT), Matchers.<Listener>any());
        verify(mockSocket, times(1)).connect();
        verify(mockSocket, times(1)).emit(eq(Sockets.REQUEST_NEW_SUBTASK), payloadCaptor.capture());
        assertEquals(payloadCaptor.getValue().getString(Sockets.ID), TEST_PHONE_ID);
        assertEquals(payloadCaptor.getValue().getInt(TASK_ID), 1);
        assertEquals(payloadCaptor.getValue().getBoolean(FORCE_TASK), true);
    }
}
