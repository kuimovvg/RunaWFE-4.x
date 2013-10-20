package ru.cg.runaex.database.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Петров А.
 */
public class AsciiToNativeTest {

  @Test
  public void asciiToNativeTest() {
    assertTrue("runaex".equals(AsciiToNative.convert("runaex")));
    assertTrue("\\u0411\\u0430\\u0437\\u0430\\u0414\\u0430\\u043D\\u043D\\u044B\\u0445".equals(AsciiToNative.convert("БазаДанных")));
  }
}
