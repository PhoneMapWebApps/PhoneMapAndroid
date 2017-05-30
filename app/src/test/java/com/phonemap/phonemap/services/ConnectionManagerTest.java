package com.phonemap.phonemap.services;

import android.os.Messenger;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.phonemap.phonemap.constants.Sockets.SOCKET_GET_CODE;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class ConnectionManagerTest {
    @Mock Socket socket;
    @Mock Messenger replyTo;

    @Captor ArgumentCaptor<String> stringCaptor;
    @Captor ArgumentCaptor<Emitter.Listener> listenerCaptor;

    private ConnectionManager manager;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Log.class);

        manager = new ConnectionManager(socket);
    }

    @Test
    public void testConstructorConnectsSocket() {
        new ConnectionManager(socket);
        verify(socket, times(2)).connect();
        verify(socket, atLeast(1)).on(stringCaptor.capture(), listenerCaptor.capture());
    }

    @Test
    public void testSetIDListenerHandlesCorrectJSON() throws JSONException {
        JSONObject id = new JSONObject();
        id.put("id", 0);

        manager.setIdListener.call(id);

        verify(socket, times(1)).emit(SOCKET_GET_CODE);
    }

    @Test
    public void testSetIDListenerDoesNotProceedOnEmptyJSON() throws JSONException {
        JSONObject id = new JSONObject();

        manager.setIdListener.call(id);

        verify(socket, times(0)).emit(SOCKET_GET_CODE);
    }
}
