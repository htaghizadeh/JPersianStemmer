package com.intellitext.persian;

import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author htaghizadeh
 */
public class Stemmer {
    public static final PatriciaTrie<Byte> lexicon = new PatriciaTrie<>();
    public static final PatriciaTrie<String> mokassarDic = new PatriciaTrie<>();
    public static final PatriciaTrie<String> cache = new PatriciaTrie<>();
    public static final PatriciaTrie<VerbStem> verbDic = new PatriciaTrie<>();
    public static final PatriciaTrie<Byte> verbStemDic = new PatriciaTrie<>();
    public static final List<Rule> _ruleList = new ArrayList<>();

    private static final String[] verbAffix = {"*ا", "*ار", "*ش", "*نده", "وا*", "اثر*", "فرو*", "پیش*", "گرو*","*ه","*گار","*ن"};
    private static final String[] suffix = {"کار", "ناک", "وار", "آسا", "آگین", "بار", "بان", "دان", "زار", "سار", "سان", "لاخ", "مند", "دار", "مرد", "کننده", "گرا", "نما", "متر"};
    private static final String[] prefix =  {"بی", "با", "پیش", "غیر", "فرو", "هم", "نا", "یک"};
    private static final String[] prefixException = {"غیر"};
    private static final String[] suffixZamir = { "م", "ت", "ش" };
    private static final String[] suffixException = {"ها", "تر", "ترین", "ام", "ات", "اش"};

    private static final String PATTERN_FILE_NAME = "Patterns.fa";
    private static final String VERB_FILE_NAME = "VerbList.fa";
    private static final String DIC_FILE_NAME = "Dictionary.fa";
    private static final String MOKASSAR_FILE_NAME = "Mokassar.fa";
    private static final String VERBSTEM_FILE_NAME = "all_verbs_stems.fa";

    private static final int patternCount = 1;
    private static final boolean enableCache = true;
    private static final boolean enableVerb = false;

    private static final Pattern _english_chars = Pattern.compile("[a-z,:/`;'\\?A-Z *+~!@#=\\[\\]{}\\$%^&*().0-9]+");
    private static final Pattern _numbers = Pattern.compile("[0-9,.]+");

    private static final Pattern _mokassar_zamir1 = Pattern.compile("^(?<stem>.+?)((?<=(ا|و))ی)?(ها)?(ی)?((ات)?( تان|تان| مان|مان| شان|شان)|ی|م|ت|ش|ء)$");
    private static final Pattern _mokassar_zamir2 = Pattern.compile("^(?<stem>.+?)((?<=(ا|و))ی)?(ها)?(ی)?(ات|ی|م|ت|ش| تان|تان| مان|مان| شان|شان|ء)$");

    static
    {
        try {
            loadRule();
            loadLexicon();
            loadMokassarDic();
            if (enableVerb) {
                loadVerbStemDic();
                loadVerbDic();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading Stemmer data: " + e.toString());
        }
    }

    public Stemmer() {
    }
    
    private static List<String> loadData(String resourceName) throws IOException {
        List<String> result = new ArrayList<>();
        BufferedReader in = null;
        try (Reader reader = new BufferedReader(new InputStreamReader(Stemmer.class.getResourceAsStream(resourceName)))) {
            try {
                String sLine = null;
                in = new BufferedReader(reader);
                while ((sLine = in.readLine()) != null) {
                    sLine = sLine.trim();
                    if (sLine.isEmpty() || sLine.equals("#"))
                        continue;
                    result.add(sLine);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error loading Stemmer data: " + e.toString());
            }
        }
        
        return result;
    }

    private static void loadVerbStemDic() throws IOException {
        if (verbStemDic.isEmpty()) {
            List<String> sLines = loadData(VERBSTEM_FILE_NAME);
            for (String sLine : sLines) {
                String[] arr = sLine.split("#", -1);
                verbStemDic.put(arr[0].trim(), null);
                verbStemDic.put(arr[1].trim(), null);
            }
        }
    }

    private static void loadVerbDic() throws IOException {
        if (verbDic.isEmpty()) {
            List<String> sLines = loadData(VERB_FILE_NAME);
            for (String sLine : sLines) {
                String[] arr = sLine.split("\t", -1);
                verbDic.put(arr[0].trim(), new VerbStem(arr[1].trim(), arr[2].trim()));
            }
        }
    }
    
    private static void loadRule() throws IOException {
        if (_ruleList.isEmpty()) {
            List<String> sLines = loadData(PATTERN_FILE_NAME);
            for (String sLine : sLines) {
                _ruleList.add(Rule.createFromText(sLine));
            }
        }
    }
    
    private static void loadLexicon() throws IOException {
        if (lexicon.isEmpty()) {
            List<String> sLines = loadData(DIC_FILE_NAME);
            for (String sLine : sLines) {
                lexicon.put(sLine.trim(), null);
            }
        }
    }

    private static void loadMokassarDic() throws IOException {
        if (mokassarDic.isEmpty()) {
            List<String> sLines = loadData(MOKASSAR_FILE_NAME);
            for (String sLine : sLines) {
                String[] arr = sLine.split("\t", -1);
                mokassarDic.put(arr[0].trim(), arr[1].trim());
            }
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
        Pattern pRule = _mokassar_zamir1;
        if (bState)            
            pRule = _mokassar_zamir2;

        return extractStem(sInput, pRule);
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
        for (String sPrefix : Stemmer.prefix)
        {
            if (sWord.startsWith(sPrefix))
                return sPrefix;
        }

        return "";
    }
    
    private String getPrefixException(String sWord)
    {
        for (String sPrefix : Stemmer.prefixException)
        {
            if (sWord.startsWith(sPrefix))
                return sPrefix;
        }

        return "";
    }

    private String getSuffix(String sWord)
    {
        for (String sSuffix : Stemmer.suffix)
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
            if (!result && inRange(sWord.indexOf(' '), 2, l-1)) {
                result = validation(sWord.replace(" ", ""));
            }
        }
            // دیندار
            // دین دار
        if (!result) {
            String sSuffix = getSuffix(sWord);
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

    private boolean isMatch(String sInput, Pattern pRule) {
        return pRule.matcher(sInput).matches();
    }

    private String extractStem(String sInput, Pattern pRule, String sReplacement) {
	    return pRule.matcher(sInput).replaceAll(sReplacement).trim();
    }
    
    private String extractStem(String sInput, Pattern pRule) {
        return extractStem(sInput, pRule,"${stem}");
    }

    private boolean inVerbStems(String input) {
        if (input.isEmpty())
            return false;

        String _input = input;
        if ('ن' == input.charAt(0))
            _input = input.substring(1);
        else if (input.charAt(0) == 'ا')
            _input = 'آ' + input.substring(1);


        return (verbStemDic.containsKey(input) || verbStemDic.containsKey(_input));
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
        for (Rule rule: _ruleList) {
            if (terminate)
                return terminate;
            
            final String[] sReplace = rule.getSubstitution().split(";", -1);
            final Pattern pattern = rule.getBody();

            if (!isMatch(input, pattern))
                continue;
            
            int k = 0;
            for (String t : sReplace) {
                if (k > 0) 
                    break;

                s = extractStem(input, pattern, t);
                if (s.length() < rule.getMinLength() || s.equals(input))
                    continue;

                if ("K".equals(rule.getPoS())) { // Kasre Ezafe
                    if (stemList.isEmpty()) {
                        sTemp = getMokassarStem(s);
                        if (!sTemp.isEmpty()) {
                            stemList.add(sTemp);//, pattern + " [جمع مکسر]");
                            k++;
                        } else if (normalizeValidation(s, !s.startsWith("می "))) {
                            stemList.add(s);//, pattern);
                            k++;
                        } else {
                            //addToLog("", pattern + " : {" + s + "}");
                        }
                    }
                }
                else if ("V".equals(rule.getPoS())) { // Verb
                    sTemp = verbValidation(s);
                    if (!sTemp.isEmpty()) {
                        stemList.add(s/* pattern + " : [" + sTemp + "]"*/);
                        k++;
                    } else {
                        //addToLog("", pattern + " : {تمام وندها}");
                    }
                }
                else if ("VE".equals(rule.getPoS()) || "VS".equals(rule.getPoS())) { // Verb
                    sTemp = verbValidation(s);
                    if (inVerbStems(s)) {
                        stemList.add(s/* pattern + " : [" + sTemp + "]"*/);
                        k++;
                    } else {
                        //addToLog("", pattern + " : {تمام وندها}");
                    }
                }
                else {
                    if (normalizeValidation(s, !s.startsWith("می "))) {
                        stemList.add(s/*, pattern*/);
                        if (rule.getState())
                            terminate = true;
                        k++;
                    } else {
                        //addToLog("", pattern + " : {" + s + "}");
                    }
                }
            }
        }
        return terminate;
    }

    private boolean isEnglish(String sInput) {
        return _english_chars.matcher(sInput).matches();
    }

    private boolean isNumber(String sInput) {
        return _numbers.matcher(sInput).matches();
    }

    public final String run(String input) {
        
        input = normalization(input).trim();

        if (input.isEmpty()) 
            return "";

        //Integer or english 
        if (isEnglish(input) || isNumber(input) || (input.length() <= 2))
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
            
            for (String sSuffix : Stemmer.suffixZamir) {
                if (secondStem.equals(firstStem.concat(sSuffix)))
                    return firstStem;
            }            
        }
        return lastStem;
    }
}