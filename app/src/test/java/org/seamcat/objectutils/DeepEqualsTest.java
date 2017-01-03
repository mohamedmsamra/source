package org.seamcat.objectutils;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DeepEqualsTest {

    private DeepEquals deepEquals;

    @Before
    public void setup() {
        deepEquals = new DeepEquals();
    }

    @Test
    public void testBasic() {
        Double a_d = 2.3;
        Double b_d = 2.3;
        Assert.assertTrue(deepEquals.compare(a_d, b_d));
        b_d = 3.2;
        Assert.assertFalse(deepEquals.compare(a_d, b_d));
        
        
        String a_s = "string";
        String b_s = "string";
        Assert.assertTrue( deepEquals.compare(a_s, b_s) );
        b_s = "wrong";
        Assert.assertFalse(deepEquals.compare(a_d, b_d));

        Integer a_i = 3;
        Integer b_i = 3;
        Assert.assertTrue( deepEquals.compare(a_i, b_i));
        b_i = 4;
        Assert.assertFalse(deepEquals.compare(a_i, b_i));

        Float a_f = 1.3f;
        Float b_f = 1.3f;
        Assert.assertTrue( deepEquals.compare(a_f, b_f));
        b_f = 3.1f;
        Assert.assertFalse(deepEquals.compare(a_f, b_f));

        Boolean a_b = true;
        Boolean b_b = true;
        Assert.assertTrue( deepEquals.compare(a_b, b_b));
        b_b = false;
        Assert.assertFalse(deepEquals.compare(a_b, b_b));

        Long a_l = 42L;
        Long b_l = 42L;
        Assert.assertTrue( deepEquals.compare(a_l, b_l));
        b_l = 24L;
        Assert.assertFalse(deepEquals.compare(a_l, b_l));
    }


    @Test
    public void nullTest() {
        Assert.assertFalse( deepEquals.compare("A", null) );
        Assert.assertFalse(deepEquals.compare(null, "A"));
        Assert.assertTrue( deepEquals.compare(null, null));
    }

    
    @Test
    public void objectTest() {
        ExampleClass a = new ExampleClass();
        a.f = 23.4f;
        ExampleClass b = new ExampleClass();
        b.f = 42.f;
        Assert.assertFalse(deepEquals.compare(a, b));
        
        b.f = 23.4f;
        Assert.assertTrue( deepEquals.compare(a, b));
    }


    @Test
    public void listTest() {
        List<String> a = Arrays.asList("first", "second");
        List<String> b = Arrays.asList("first", "other");
        Assert.assertFalse( deepEquals.compare(a,b));
        
        b.set(1, "second");
        Assert.assertTrue( deepEquals.compare(a,b));
    }
    
    @Test
    public void mapTest() {
        HashMap<String, String> a = new HashMap<String, String>();
        a.put("a","one");
        a.put("b","two");

        HashMap<String, String> b = new HashMap<String, String>();
        b.put("a", "one");
        b.put("b", "one");
        Assert.assertFalse( deepEquals.compare(a,b));

        
        b.remove("b");
        b.put("b", "two");
        Assert.assertTrue( deepEquals.compare(a,b));
    }
    
    @Test
    public void arrayTest() {
        ExampleClass a = new ExampleClass();
        a.strings = new String[2];
        a.strings[0] = "first";
        a.strings[1] = "second";
        a.ints = new int[2];
        a.ints[0] = 3;
        a.ints[1] = 3;
        
        ExampleClass b = new ExampleClass();
        b.strings = new String[2];
        b.strings[0] = "first";
        b.strings[1] = "second";
        b.ints = new int[2];
        b.ints[0] = 3;
        b.ints[1] = 3;
        
        Assert.assertTrue( deepEquals.compare(a, b));
        b.ints[1] = 6;
        Assert.assertFalse( deepEquals.compare(a, b));
    }
    
    @Test
    public void testCycle() {
        Cycle a = new Cycle();
        Cycle b = new Cycle();
        a.name = "name";
        a.other = b;
        
        b.name = "name";
        b.other = a;
        Assert.assertTrue( deepEquals.compare( a,b));

        b.name = "otherName";
        Assert.assertFalse(deepEquals.compare(a, b));

    }




}
