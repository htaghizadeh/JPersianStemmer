package htz.ir.stemming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import java.util.*;
import java.util.Collections;

//import org.crcis.utils.TrieImpl;
//import org.crcis.utils.TrieInterface;

import com.google.code.regexp.*;
import global.utils.CharSequenceKeyAnalyzer;
import global.utils.PatriciaTrie;
import global.utils.Utils;
import java.io.InputStreamReader;

/**
 *
 * @author htaghizadeh
 */
public class PersianStemmer {
    public static final PatriciaTrie<String, Byte> lexicon = new PatriciaTrie<>(new CharSequenceKeyAnalyzer());
    public static final PatriciaTrie<String, String> mokassarDic = new PatriciaTrie<>(new CharSequenceKeyAnalyzer());
    public static final PatriciaTrie<String, String> cache = new PatriciaTrie<>(new CharSequenceKeyAnalyzer());
    public static final PatriciaTrie<String, VerbStem> verbDic = new PatriciaTrie<>(new CharSequenceKeyAnalyzer());
    public static final ArrayList<StemmingRule> _ruleList = new ArrayList<>();

    private static final String[] verbAffix = {"*ش", "*نده", "*ا", "*ار", "وا*", "اثر*", "فرو*", "پیش*", "گرو*","*ه","*گار","*ن"};
    private static final String[] suffix = {"كار", "ناك", "وار", "آسا", "آگین", "بار", "بان", "دان", "زار", "سار", "سان", "لاخ", "مند", "دار", "مرد", "کننده", "گرا", "نما", "متر"};
    private static final String[] prefix =  {"بی", "با", "پیش", "غیر", "فرو", "هم", "نا", "یک"};
    private static final String[] prefixException = {"غیر"};
    private static final String[] suffixZamir = { "م", "ت", "ش" };
    private static final String[] suffixException = { "ها", "تر", "ترین", "ام", "ات", "اش" };    

    private static final String PATTERN_FILE_NAME = "/htz/ir/stemming/data/Patterns.fa";
    private static final String VERB_FILE_NAME = "/htz/ir/stemming/data/VerbList.fa";
    private static final String DIC_FILE_NAME = "/htz/ir/stemming/data/Dictionary.fa";
    private static final String MOKASSAR_FILE_NAME = "/htz/ir/stemming/data/Mokassar.fa";
    private static final int patternCount = 1;
    private static final boolean enableCache = true;
    private static final boolean enableVerb = false;

    public PersianStemmer() {    
        try {
            loadRule();
            loadLexicon();
            loadMokassarDic();
            if (enableVerb) 
                loadVerbDic();
        }
        catch (IOException e) {System.out.println("There was a problem: " + e);}      
    }
    
    private String loadData(String resourceName) throws IOException {
        
        Reader reader = null;
        StringBuilder sbContent = new StringBuilder();
        BufferedReader in = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(resourceName)));
            try {
                String sLine = null;
                in = new BufferedReader(reader); 
                while ((sLine = in.readLine()) != null) {
                    if (sLine.trim().isEmpty())
                        continue;
                    sbContent.append(sLine).append("\n");
                }
            } catch (IOException e) {System.out.println("There was a problem: " + e);} 

        } finally {
          reader.close();
        }
        
        return sbContent.toString();
    }
    
    private void loadVerbDic() throws IOException {
        
        if (! verbDic.isEmpty())
            return;

        String[] sLines = loadData(VERB_FILE_NAME).split("\n");        
        for (String sLine : sLines) {
            String[] arr = sLine.split("\t");
            verbDic.put(arr[0].trim(), new VerbStem(arr[1].trim(), arr[2].trim()));
        }  
    }    
    
    private void loadRule() throws IOException {        
        if (! _ruleList.isEmpty())
            return;

        String[] sLines = loadData(PATTERN_FILE_NAME).split("\n");        
        for (String sLine : sLines) {
            String[] arr = sLine.split(",");
            _ruleList.add(new StemmingRule(arr[0], arr[1], arr[2].charAt(0), Byte.parseByte(arr[3]), Boolean.parseBoolean(arr[4])));
        }
    }
    
    private void loadLexicon() throws IOException {
        
        if (lexicon.size() > 0)
            return;
        
        String[] sLines = loadData(DIC_FILE_NAME).split("\n");        
        for (String sLine : sLines) {
            lexicon.put(sLine.trim(), null);
        }
    }

    private void loadMokassarDic() throws IOException {
        
        if (mokassarDic.size() > 0)
            return;

        String[] sLines = loadData(MOKASSAR_FILE_NAME).split("\n");        
        for (String sLine : sLines) {
            String[] arr = sLine.split("\t");
            mokassarDic.put(arr[0].trim(), arr[1].trim());            
        }
    }
    
    private String normalization(String s) {
        
        StringBuilder newString = new StringBuilder();        
        for(int i=0;i<s.length();i++){            
            switch (s.charAt(i)) {
                case 'ي':
                    newString.append('ی');
                    break;
                //case 'ة':
                case 'ۀ':
                    newString.append('ه');
                    break;
                case '‌':
                    newString.append(' ');
                    break;
                case '‏':
                    newString.append(' ');
                    break;
                case 'ك':
                    newString.append('ک');
                    break;
                case 'ؤ':
                    newString.append('و');
                    break;
                case 'إ':
                case 'أ':
                    newString.append('ا');
                    break;
                case '\u064B': //FATHATAN
                case '\u064C': //DAMMATAN
                case '\u064D': //KASRATAN
                case '\u064E': //FATHA
                case '\u064F': //DAMMA
                case '\u0650': //KASRA
                case '\u0651': //SHADDA
                case '\u0652': //SUKUN
                    break;
                default:
                    newString.append(s.charAt(i));
            }
        }
        return newString.toString();
        
    }

    private boolean validation(String sWord) {
        return (lexicon.containsKey(sWord));
    }

    private String isMokassar(String sInput, boolean bState)
    {
        String sRule = "^(?<stem>.+?)((?<=(ا|و))ی)?(ها)?(ی)?((ات)?( تان|تان| مان|مان| شان|شان)|ی|م|ت|ش|ء)$";
        if (bState)            
            sRule = "^(?<stem>.+?)((?<=(ا|و))ی)?(ها)?(ی)?(ات|ی|م|ت|ش| تان|تان| مان|مان| شان|شان|ء)$";
            
        return extractStem(sInput, sRule);
    }
        
    private String getMokassarStem(String sWord) {
        if (mokassarDic.containsKey(sWord))
            return mokassarDic.get(sWord);
        else {
            String sNewWord = isMokassar(sWord, true);
            if (mokassarDic.containsKey(sNewWord))
                return mokassarDic.get(sNewWord);
            else
            {
                sNewWord = isMokassar(sWord, false);
                if (mokassarDic.containsKey(sNewWord))
                    return mokassarDic.get(sNewWord);
            }
        }
        
        return "";
    }
    
    private String verbValidation(String sWord) {
        if (sWord.indexOf(' ') > -1)
            return "";
        
        for (int j = 0; j < verbAffix.length; j++) {
            String sTemp = "";
            if (j == 0 && (sWord.charAt(sWord.length() - 1) == 'ا' || sWord.charAt(sWord.length() - 1) == 'و')) {
                sTemp = verbAffix[j].replace("*", sWord + "ی");
            }
            else {
                sTemp = verbAffix[j].replace("*", sWord);
            }

            if (normalizeValidation(sTemp, true)) 
                return verbAffix[j];
        }
        
        return "";
    } 
    
    private boolean inRange(int d, int from, int to) {
        return (d >= from && d <= to);
    }
    
    private String getPrefix(String sWord)
    {
        for (String sPrefix : PersianStemmer.prefix)
        {
            if (sWord.startsWith(sPrefix))
                return sPrefix;
        }

        return "";
    }
    
    private String getPrefixException(String sWord)
    {
        for (String sPrefix : PersianStemmer.prefixException)
        {
            if (sWord.startsWith(sPrefix))
                return sPrefix;
        }

        return "";
    }

    private String getSuffix(String sWord)
    {
        for (String sSuffix : PersianStemmer.suffix)
        {
            if (sWord.endsWith(sSuffix))
                return sSuffix;
        }

        return "";
    }
    
    private boolean normalizeValidation(String sWord, boolean bRemoveSpace) {

        final int l = sWord.trim().length() - 2;
        sWord = sWord.trim();
        boolean result = validation(sWord);

        if (!result && sWord.indexOf('ا') == 0) {
            result = validation(sWord.replaceFirst("ا", "آ"));
        }

        if (!result && inRange(sWord.indexOf('ا'), 1, l)) {
            result = validation(sWord.replace('ا', 'أ'));
        }

        if (!result && inRange(sWord.indexOf('ا'), 1, l)) {
            result = validation(sWord.replace('ا', 'إ'));
        }

        if (!result && inRange(sWord.indexOf("ئو"), 1, l)) {
            result = validation(sWord.replace("ئو", "ؤ"));
        }
        
        if (!result && sWord.endsWith("ء"))
            result = validation(sWord.replace("ء", ""));        

        if (!result && inRange(sWord.indexOf("ئ"), 1, l))
            result = validation(sWord.replace("ئ", "ی"));
            
        if (bRemoveSpace) {
            if (!result && inRange(sWord.indexOf(' '), 1, l)) {
                result = validation(sWord.replace(" ", ""));
            }
        }
            // دیندار
            // دین دار
        if (!result) {
            String sSuffix = getSuffix(sWord) ;
            if (!sSuffix.isEmpty()) 
                result = validation(sSuffix.equals("مند") ? sWord.replace(sSuffix, "ه " + sSuffix) : sWord.replace(sSuffix, " " + sSuffix));
        }

        if (!result) {
            String sPrefix = getPrefix(sWord) ;
            if (!sPrefix.isEmpty()) {
                if (sWord.startsWith(sPrefix + " "))
                    result = validation(sWord.replace(sPrefix + " ", sPrefix));
                else
                    result = validation(sWord.replace(sPrefix, sPrefix + " "));            
            }
        }

        if (!result)
        {
            String sPrefix = getPrefixException(sWord) ;
            if (!sPrefix.isEmpty()) {
                if (sWord.startsWith(sPrefix + " "))
                    result = validation(sWord.replaceFirst(sPrefix + " ", ""));
                else
                    result = validation(sWord.replaceFirst(sPrefix, ""));            
            }
        }
        
        return result;
    }

    private boolean isMatch(String sInput, String sRule) {
        return Pattern.compile(sRule).matcher(sInput).matches();
    }

    private String extractStem(String sInput, String sRule, String sReplacement) {                
	return Pattern.compile(sRule).matcher(sInput).replaceAll(sReplacement).trim();
    }
    
    private String extractStem(String sInput, String sRule) {                
        return extractStem(sInput, sRule,  "${stem}");
    }

    private String getVerb(String input)
    {
        if (verbDic.containsKey(input)) {
            VerbStem vs = verbDic.get(input);
            if (validation(vs.getPresent()))
                return vs.getPresent();
            
            return vs.getPast();
        }
        
        return "";
    }
    
    private boolean PatternMatching(String input, List<String> stemList)
    {
        boolean terminate = false;
        String s = "";
        String sTemp = "";
        for (StemmingRule rule: _ruleList) {
            if (terminate)
                return terminate;
            
            final String[] sReplace = rule.getSubstitution().split(";");
            final String pattern = rule.getBody();

            if (!isMatch(input, pattern))
                continue;
            
            int k = 0;
            for (String t : sReplace) {
                if (k > 0) 
                    break;

                s = extractStem(input, pattern, t);
                if (s.length() < rule.getMinLength()) 
                    continue;

                switch (rule.getPoS()) {
                    case 'K': // Kasre Ezafe
                        if (stemList.isEmpty()) {
                            sTemp = getMokassarStem(s);
                            if (!sTemp.isEmpty()) {
                                stemList.add(sTemp);//, pattern + " [جمع مکسر]");
                                k++;
                            }
                            else if (normalizeValidation(s, true)) {
                                stemList.add(s);//, pattern);
                                k++;
                            }
                            else {
                                //addToLog("", pattern + " : {" + s + "}");
                            }
                        }
                        break;
                    case 'V': // Verb
                        
                        sTemp = verbValidation(s);
                        if (!sTemp.isEmpty()) {
                            stemList.add(s/* pattern + " : [" + sTemp + "]"*/);
                            k++;
                        }
                        else {
                            //addToLog("", pattern + " : {تمام وندها}");
                        }
                        break;
                    default:
                        if (normalizeValidation(s, true)) {
                            stemList.add(s/*, pattern*/);
                            if (rule.getState())
                                terminate = true;
                            k++;
                        }
                        else {
                            //addToLog("", pattern + " : {" + s + "}");
                        }
                        break;
                }
            }
        }
        return terminate;
    }
    
    public final String run(String input) {
        
        input = normalization(input).trim();

        if (input.isEmpty()) 
            return "";

        //Integer or english 
        if (Utils.isEnglish(input) || Utils.isNumber(input) || (input.length() <= 2))
            return input;

        if (enableCache && cache.containsKey(input))
            return cache.get(input);

        String s = getMokassarStem(input);
        if (normalizeValidation(input, false)) {
            //stemList.add(input/*, "[فرهنگ لغت]"*/);
            if (enableCache)
               cache.put(input, input);
            return input;
        }else if (!s.isEmpty()) {
            //addToLog(s/*, "[جمع مکسر]"*/);
            //stemList.add(s);
            if (enableCache)
                cache.put(input, s);
            return s;
        }
        
        List<String> stemList = new ArrayList<>();
        boolean terminate = PatternMatching(input, stemList);
        
        if (enableVerb) {
            s = getVerb(input);
            if (!s.isEmpty()){
                stemList.clear();
                stemList.add(s);
            }
        }

        if (stemList.isEmpty()) {
            if (normalizeValidation(input, true)) {
                //stemList.add(input, "[فرهنگ لغت]");
                if (enableCache)
                    cache.put(input, input); //stemList.get(0));
                return input;//stemList.get(0);
            }
            stemList.add(input);//, "");            
        }

        if (terminate && stemList.size() > 1) {
            return nounValidation(stemList);
        }

        final int I = 0;
        if (patternCount != 0) {
            if (patternCount < 0)
                Collections.reverse(stemList);
            else
                Collections.sort(stemList);

            while (I < stemList.size() && (stemList.size() > Math.abs(patternCount))) {
                stemList.remove(I);
                //patternList.remove(I);
            }
        }

        if (enableCache)
            cache.put(input, stemList.get(0));
        return stemList.get(0);
    }
        
    /*private void addToLog(String sStem) {
        
        if (sStem.isEmpty() || stemList.contains(sStem)) 
            return;

        stemList.add(sStem);
        //patternList.add(sRule);
    }    */
    
    public final int stem(char[] s, int len) /*throws Exception*/ {
        
        StringBuilder input = new StringBuilder();
        for (int i=0; i< len; i++) {
            input.append(s[i]);
        }        
        String sOut = this.run(input.toString());
        
        if (sOut.length() > s.length)
            s =  new char[sOut.length()];
        for (int i=0; i< sOut.length(); i++) {
            s[i] = sOut.charAt(i);
        }            
        /*try {
            for (int i=0; i< Math.min(sOut.length(), s.length); i++) {
                s[i] = sOut.charAt(i);
            }    
        }
        catch (Exception e) {
            throw new Exception("stem: "+sOut+" - input: "+ input.toString());
        }*/
        
        return sOut.length();        
        
    }    

    private String nounValidation(List<String> stemList)
    {
        Collections.sort(stemList);
        int lastIdx = stemList.size() -1;
        String lastStem = stemList.get(lastIdx);

        if (lastStem.endsWith("ان")) {
            return lastStem;
        }
        else {
            String firstStem = stemList.get(0);
            String secondStem = stemList.get(1).replace(" ", "");

            /*if (secondStem.equals(firstStem.concat("م"))) {
                return firstStem;
            }
            else if (secondStem.equals(firstStem.concat("ت"))) {
                return firstStem;
            }
            else if (secondStem.equals(firstStem.concat("ش"))) {
                return firstStem;
            }*/
            
            for (String sSuffix : PersianStemmer.suffixZamir) {
                if (secondStem.equals(firstStem.concat(sSuffix)))
                    return firstStem;
            }            
        }
        return lastStem;
    }
}