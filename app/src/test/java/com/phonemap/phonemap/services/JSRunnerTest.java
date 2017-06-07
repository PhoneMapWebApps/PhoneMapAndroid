package com.phonemap.phonemap.services;


import android.content.Intent;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;
import org.liquidplayer.service.MicroService;

import java.net.URI;
import java.net.URISyntaxException;

import static com.phonemap.phonemap.constants.API.ON_DESTROY;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JSRunnerTest {
    private MockContext mockContext;
    private Intent mockIntent;

    private MicroService mockService;
    private JSRunner jsRunner;
    private ShutdownReceiver shutdownReceiver;


    @Before
    public void setup() {
        mockContext = new MockContext();
        mockService = mock(MicroService.class);
        mockIntent = mock(Intent.class);

        shutdownReceiver = new ShutdownReceiver(mockService);
        jsRunner = new JSRunner(mockService);
    }

    @Test
    public void convertPathToURIFailsOnEmptyString() {
        try {
            jsRunner.convertPathToURI("");
        } catch (URISyntaxException e) {
            return;
        }
        fail();
    }

    @Test
    public void convertPathToURIDoesNotFailOnFilename() {
        URI uri;

        try {
            uri = jsRunner.convertPathToURI("code.js");
        } catch (URISyntaxException e) {
            fail();
            return;
        }

        assertTrue(uri.toString().contains("code.js"));
        assertTrue(uri.toString().contains("file://"));
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
