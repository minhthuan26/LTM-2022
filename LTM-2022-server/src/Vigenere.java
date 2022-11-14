import java.util.Locale;

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

    public static void main(String[] args) {
        String str = "https://m.media-amazon.com/images/M/MV5BMzg2Mjg1OTk0NF5BMl5BanBnXkFtZTcwMjQ4MTA3Mw@@._V1_QL75_UX380_CR0,0,380,562_.jpg";
        String key = "zfyrxmxeru";

        String des =  Decode(str, key);
        System.out.println("cipher: " + des);

        System.out.println("origin: " + Encode(des, key));
    }
}

