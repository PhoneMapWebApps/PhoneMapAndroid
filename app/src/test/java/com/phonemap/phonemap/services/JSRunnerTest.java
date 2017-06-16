package com.phonemap.phonemap.services;


import org.junit.Before;
import org.junit.Test;
import org.liquidplayer.service.MicroService;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class JSRunnerTest {
    private JSRunner jsRunner;

    @Before
    public void setup() {
        MicroService mockService = mock(MicroService.class);
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
}
