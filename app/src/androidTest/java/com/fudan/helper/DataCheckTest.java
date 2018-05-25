package com.fudan.helper;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by leiwe on 2018/4/27.
 * Thank you for reading, everything gonna to be better.
 */
public class DataCheckTest {

    @Test
    public void isLevelB() {
        boolean flag=DataCheck.isLevelB("18817543545","");
        assertEquals(false,flag);
    }
}