package com.rdapps.gamepad;

import com.rdapps.gamepad.util.ByteUtils;
import org.junit.Assert;
import org.junit.Test;


public class ByteUtilsTest {


    @Test
    public void testCrc() {
        byte b = ByteUtils.crc8(new byte[]{0, 0, 0});
        Assert.assertEquals(b, 0);
    }
}