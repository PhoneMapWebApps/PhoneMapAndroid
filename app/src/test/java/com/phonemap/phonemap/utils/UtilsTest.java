package com.phonemap.phonemap.utils;

import android.os.Bundle;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static com.phonemap.phonemap.utils.Utils.bundleToJSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

    @Mock
    Bundle bundle;

    @Test
    public void testBundleToJSONNullBundle() {
        JSONObject object = bundleToJSON(null);
        assertNotEquals(object, null);
        assertEquals(object.length(), 0);
    }

    @Test
    public void testBundleToJSONEmptyBundle() {
        JSONObject object = bundleToJSON(bundle);
        assertNotEquals(object, null);
        assertEquals(object.length(), 0);
    }

    @Test
    public void testBundleToJSONOneElement() {
        when(bundle.isEmpty()).thenReturn(false);

        Set<String> set = new  HashSet<>();
        set.add("foo");

        when(bundle.keySet()).thenReturn(set);
        when(bundle.get("foo")).thenReturn("bar");

        JSONObject object = bundleToJSON(bundle);
        assertEquals(object.length(), 1);
        assertTrue(object.has("foo"));

        try {
            assertEquals(object.get("foo"), "bar");
        } catch (JSONException e) {
            fail("Did not expect an exception");
        }
    }

    @Test
    public void testBundleToJSONMultipleElements() {
        when(bundle.isEmpty()).thenReturn(false);

        Set<String> set = new  HashSet<>();
        set.add("foo");
        set.add("bar");

        when(bundle.keySet()).thenReturn(set);
        when(bundle.get("foo")).thenReturn("bar");
        when(bundle.get("bar")).thenReturn("baz");

        JSONObject object = bundleToJSON(bundle);
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