package com.phonemap.phonemap.services;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class SocketConnectionManagerTest {
    @Mock Socket mockSocket;
    @Mock Bundle mockBundle;

    @Captor ArgumentCaptor<String> stringCaptor;
    @Captor ArgumentCaptor<Emitter.Listener> listenerCaptor;

    private SocketConnectionManager socketConnectionManager;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Log.class);
        socketConnectionManager = new SocketConnectionManager(mockSocket);
    }

    @Test
    public void testConstructorConnectsSocket() {
        verify(mockSocket, times(1)).connect();
        verify(mockSocket, times(5)).on(stringCaptor.capture(), listenerCaptor.capture());
    }

    @Test
    public void nullDoesntBundleToNull() {
        JSONObject object = socketConnectionManager.bundleToJSON(null);
        assertNotEquals(object, null);
        assertEquals(object.length(), 0);
    }

    @Test
    public void emptyBundleDoesntBundleToNull() {
        JSONObject object = socketConnectionManager.bundleToJSON(mockBundle);
        assertNotEquals(object, null);
        assertEquals(object.length(), 0);
    }

    @Test
    public void canBundleOneElementBundle() {
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
}
