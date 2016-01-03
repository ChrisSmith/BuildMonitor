package org.collegelabs.buildmonitor.buildmonitor2.util;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.collegelabs.buildmonitor.buildmonitor2.util.Linq.toList;
import static org.collegelabs.buildmonitor.buildmonitor2.util.Linq.where;

public class LinqTests extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWhere() throws Exception {
        List<String> foo = new ArrayList<>();
        foo.add("FOO");
        foo.add("BAR");
        foo.add("FOO2");

        ArrayList<String> list = toList(where(foo, s -> s.startsWith("FOO")));
        Assert.assertEquals(2, list.size());
    }
}
