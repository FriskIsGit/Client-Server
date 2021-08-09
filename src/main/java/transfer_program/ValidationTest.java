package transfer_program;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValidationTest {
    @Test
    public void test1(){
        assertTrue(UtilityFunctions.validateIPv4("10.0.0.2"));
    }
    @Test
    public void test2(){
        assertFalse(UtilityFunctions.validateIPv4("x0.0.0.2"));
    }
    @Test
    public void test3(){
        assertFalse(UtilityFunctions.validateIPv4("80.0.0.z"));
    }
    @Test
    public void test4(){
        assertFalse(UtilityFunctions.validateIPv4("80.0,0.134"));
    }
    @Test
    public void test5(){
        assertTrue(UtilityFunctions.validateIPv4("230.0.0.134"));
    }
}
