package com.phonemap.phonemap.services;


import android.content.Intent;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;
import org.liquidplayer.service.MicroService;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;

import static com.phonemap.phonemap.constants.API.ON_DESTROY;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JSRunnerTest {
    private MicroService mockService;
    private JSRunner runner;
    private ShutdownReceiver shutdownReceiver;

    private MockContext mockContext;
    private Intent mockIntent;


    @Before
    public void setup() {
        mockContext = new MockContext();
        mockService = Mockito.mock(MicroService.class);
        mockIntent = Mockito.mock(Intent.class);

        shutdownReceiver = new ShutdownReceiver(mockService);
        runner = new JSRunner(mockService);
    }

    @Test
    public void testConvertPathToURIFailsOnEmptyString() {
        try {
            runner.convertPathToURI("");
        } catch (URISyntaxException e) {
            return;
        }
        fail();
    }

    @Test
    public void testConvertPathToURIDoesNotFailOnFilename() {
        URI uri;

        try {
            uri = runner.convertPathToURI("code.js");
        } catch (URISyntaxException e) {
            fail();
            return;
        }

        assertTrue(uri.toString().contains("code.js"));
        assertTrue(uri.toString().contains("file://"));
    }

    @Test
    public void emits_onDestroy_when_phone_shutdown() {
        doReturn(Intent.ACTION_SHUTDOWN).when(mockIntent).getAction();

        shutdownReceiver.onReceive(mockContext, mockIntent);
        verify(mockService, times(1)).emit(ON_DESTROY, true);
    }

    @Test
    public void doesnt_emit_onDestroy_when_screen_turn_on() {
        doReturn(Intent.ACTION_SCREEN_ON).when(mockIntent).getAction();

        shutdownReceiver.onReceive(mockContext, mockIntent);
        verify(mockService, times(1)).emit(ON_DESTROY, false);
    }

    @Test
    public void doesnt_emit_onDestroy_when_power_disconnected() {
        doReturn(Intent.ACTION_POWER_DISCONNECTED).when(mockIntent).getAction();

        shutdownReceiver.onReceive(mockContext, mockIntent);
        verify(mockService, times(1)).emit(ON_DESTROY, false);
    }
}
