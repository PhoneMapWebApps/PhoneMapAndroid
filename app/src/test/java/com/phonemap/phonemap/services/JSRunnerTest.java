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

    @Before
    public void setup() {
        mockService = Mockito.mock(MicroService.class);
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
        Intent mockIntent = Mockito.mock(Intent.class);
        doReturn(Intent.ACTION_SHUTDOWN).when(mockIntent).getAction();

        MockContext mockContext = new MockContext();

        ShutdownReceiver shutdownReceiver = new ShutdownReceiver(runner);
        shutdownReceiver.onReceive(mockContext, mockIntent);

        verify(mockService, times(1)).emit(ON_DESTROY, true);
    }
}
