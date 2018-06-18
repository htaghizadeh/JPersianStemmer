package com.intellitext;

import com.intellitext.persian.Stemmer;

/**
 *
 * @author htaghizadeh
 */
public class Main {
    public static void main(String[] args) {
        
        String[] sWords = {"گزارشات", "می دانم",
                "اسلامی", "کتابخانه ای", "علی ای", "نشانی های", "زیباست", "آسمانم", "امامتان", "آسمانهای", "آسمان", "رسانه‌ای", "آسمانش", "موشهای", "کشورهای", "چند رسانه‌ای", "بيابند", "پدران", "ازدواج‌هاي", "کنند", "میکند", "می کند", "نمیکند", "نمیکنند", "می کنند",  "علي اي", "نتایج"};

        Stemmer ps = new Stemmer();
        for (String word : sWords) {
            System.out.println(ps.run(word));             
        }

    }
}
