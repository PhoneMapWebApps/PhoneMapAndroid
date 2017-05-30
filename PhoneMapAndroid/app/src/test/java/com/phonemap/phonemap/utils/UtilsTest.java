package com.phonemap.phonemap.utils;

import android.os.Bundle;
import android.test.mock.MockContext;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.phonemap.phonemap.utils.Utils.bundleToJSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UtilsTest {

    @Test
    public void handlesNullBundle() {
        JSONObject object = bundleToJSON(null);
        assertNotEquals(object, null);
        assertEquals(object.length(), 0);
    }
}