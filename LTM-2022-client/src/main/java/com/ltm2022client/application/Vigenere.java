package com.ltm2022client.application;

public class Vigenere {
    private static int Mod_26(int key)
    {
        return (key % 26 + 26) % 26;
    }

    private static String cipher(String input, String key, boolean encipher)
    {
        for (int i = 0; i < key.length(); ++i)
            if (!Character.isLetter(key.charAt(i)))
                return null; // Error

        String output = "";
        int nonAlphaCharCount = 0;

        for (int i = 0; i < input.length(); ++i)
        {
            if (Character.isLetter(input.charAt(i)))
            {
                boolean cIsUpper = Character.isUpperCase(input.charAt(i));
                char offset = cIsUpper ? 'A' : 'a';
                int keyIndex = (i - nonAlphaCharCount) % key.length();
                int k = (Character.toUpperCase(key.charAt(keyIndex))) - offset;
                k = encipher ? k : -k;
                char ch = (char)((Mod_26(((input.charAt(i) + k) - offset))) + offset);
                output += ch;
            }
            else
            {
                output += input.charAt(i);
                ++nonAlphaCharCount;
            }
        }

        return output;
    }

    public static String Encode(String input, String key)
    {
        return cipher(input, key, true);
    }

    public static String Decode(String input, String key)
    {
        return cipher(input, key, false);
    }
}
