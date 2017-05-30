package com.phonemap.phonemap.services;


import android.net.Uri;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

public class JSRunnerTest {

    JSRunner runner;

    @Before
    public void setup() {
        runner = new JSRunner();
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
}
