package finalproject.homesecurity.Utils;

/**
 * Created by Robbie on 28/10/2015.
 */
public class CleanUserId {
    public static String RemoveSpecialCharacters(String s)
    {
        String result = "";
        for (int i = 0; i < s.length(); i++)
        {
            if (Character.isLetterOrDigit(s.charAt(i)) && !Character.isWhitespace(s.charAt(i))
                    && s.charAt(i) != '.')
            {
                result += s.charAt(i);
            }
        }
        System.out.println(result);
        return result;
    }
}
