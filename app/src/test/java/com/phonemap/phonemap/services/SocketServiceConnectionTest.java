package com.phonemap.phonemap.services;


import android.content.ComponentName;
import android.os.IBinder;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class SocketServiceConnectionTest {
    private JSRunner mockRunner = mock(JSRunner.class);

    private ComponentName mockComponentName = mock(ComponentName.class);

    @Before
    public void setup() {
        PowerMockito.mockStatic(Log.class);
        BDDMockito.given(Log.e(anyString(), anyString())).will(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        });
    }

    @Test
    public void requestsNewSubtaskUponConnection() {
        InOrder inOrder = inOrder(mockRunner);

        IBinder mockIBinder = mock(IBinder.class);

        SocketServiceConnection socketServiceConnection = new SocketServiceConnection(mockRunner);
        socketServiceConnection.onServiceConnected(mockComponentName, mockIBinder);


        inOrder.verify(mockRunner, times(1)).setMessengerSender(Matchers.<MessengerSender>any());
        inOrder.verify(mockRunner, times(1)).requestNewSubtask();
    }

    @Test
    public void stopsServiceOnDisconnect() {
        SocketServiceConnection socketServiceConnection = new SocketServiceConnection(mockRunner);
        socketServiceConnection.onServiceDisconnected(mockComponentName);

        verify(mockRunner, times(1)).stopSelf();
    }
}
