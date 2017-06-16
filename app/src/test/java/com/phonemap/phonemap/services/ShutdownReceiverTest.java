package com.phonemap.phonemap.services;


import android.content.Intent;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;
import org.liquidplayer.service.MicroService;

import static com.phonemap.phonemap.constants.API.ON_DESTROY;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ShutdownReceiverTest {
    private MockContext mockContext;
    private Intent mockIntent;

    private MicroService mockService;
    private ShutdownReceiver shutdownReceiver;

    @Before
    public void setup() {
        mockContext = new MockContext();
        mockService = mock(MicroService.class);
        mockIntent = mock(Intent.class);

        JSRunner jsRunner = new JSRunner(mockService);
        shutdownReceiver = new ShutdownReceiver(mockService, jsRunner);
    }
    @Test
    public void emitsOnDestroyOnPhoneShutdown() {
        doReturn(Intent.ACTION_SHUTDOWN).when(mockIntent).getAction();

        shutdownReceiver.onReceive(mockContext, mockIntent);
        verify(mockService, times(1)).emit(ON_DESTROY, true);
    }

    @Test
    public void doesntEmitOnDestroyWhenScreenTurnsOn() {
        doReturn(Intent.ACTION_SCREEN_ON).when(mockIntent).getAction();

        shutdownReceiver.onReceive(mockContext, mockIntent);
        verify(mockService, times(1)).emit(ON_DESTROY, false);
    }

    @Test
    public void doesntEmitOnDestroyWhenPowerDisconnected() {
        doReturn(Intent.ACTION_POWER_DISCONNECTED).when(mockIntent).getAction();

        shutdownReceiver.onReceive(mockContext, mockIntent);
        verify(mockService, times(1)).emit(ON_DESTROY, false);
    }
}
