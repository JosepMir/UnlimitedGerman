package com.josepmir.germanincontext.util;

/**
 * Created by josep on 23.04.14.
 */
public class StringExtensions {
    public static String SafeSubstring(String input, int startIndex, int length)
    {
        // Todo: Check that startIndex + length does not cause an arithmetic overflow
        if (input.length() >= (startIndex + length))
        {
            return input.substring(startIndex, length);
        }
        else
        {
            if (input.length() > startIndex)
            {
                return input.substring(startIndex);
            }
            else
            {
                return "";
            }
        }
    }
}
