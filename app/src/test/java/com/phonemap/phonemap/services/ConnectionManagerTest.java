package com.phonemap.phonemap.services;

import android.os.Messenger;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

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

    @Before
    public void setup() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void testConstructorConnectsSocket() {
        new ConnectionManager(socket);
        verify(socket, times(2)).connect();
        verify(socket, atLeast(1)).on(stringCaptor.capture(), listenerCaptor.capture());
    }
}
