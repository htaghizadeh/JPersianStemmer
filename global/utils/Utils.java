package global.utils;

import java.io.BufferedReader;
import java.io.Reader;

/**
 *
 * @author htaghizadeh
 */
public class Utils {
    
    public static String exactMatch(String q) {
        q = q.replaceAll("\"", "");
        /*if (!q.startsWith("\""))
            q = "\"".concat(q);
        if (!q.endsWith("\""))
            q = q.concat("\"");*/
        
        q = "\"".concat(q).concat("\"");
        return q;
    }
    
    public static boolean isAdvanceQuery(String input) {
        return input.matches(".*[+:-].*");
    }        
    
    public static boolean isPhraseQuery(String input) {
        return input.matches("\".+\"");
    }        

    public static boolean isEnglish(String input) {
        return input.matches("[a-z,:/`;'\\?A-Z *+~!@#=\\[\\]{}\\$%^&*().0-9]+");        
    }
    
    public static boolean isNumber(String input) {
        return input.matches("[0-9,.]+");
    }
    
    public static int wordCount(String input) {
        return input.trim().isEmpty() ? 0 : input.trim().split("\\s+").length;    
    }

    public static BufferedReader getBufferedReader(Reader reader) {
        return (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
    }     
}
