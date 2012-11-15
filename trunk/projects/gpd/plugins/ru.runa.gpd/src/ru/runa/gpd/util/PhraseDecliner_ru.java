package ru.runa.gpd.util;

public class PhraseDecliner_ru extends PhraseDecliner {
 
    @Override
    public String declineDuration(String number, String base) {
        Rule rule = Rule.find(base);
        int pos;
        if (number.endsWith("11") || number.endsWith("12") || number.endsWith("13") || number.endsWith("14")) {
            pos = 2;
        } else if (number.endsWith("2") || number.endsWith("3") || number.endsWith("4")) {
            pos = 1;
        } else if (number.endsWith("1")) {
            pos = 0;
        } else {
            pos = 2;
        }
        return number + " " + applyRule(base, rule, pos);
    }

    private String applyRule(String base, Rule rule, int pos) {
        String[] words = base.split(" ", -1);
        String result = "";
        int i = 0;
        int ri = pos * 2;
        if (words.length == 2) {
            result += words[i].substring(0, words[i].length() - rule.charsToRemove[ri]);
            result += rule.charsToAdd[ri];
            result += " ";
            i++;
        }
        result += words[i].substring(0, words[i].length() - rule.charsToRemove[ri + 1]);
        result += rule.charsToAdd[ri + 1];
        return result;
    }

    private enum Rule {
        MIN(new int[] { 2, 1, 0, 0, 1, 1 }, new String[] { "ая", "а", "", "", "х", "" }), W(new int[] { 2, 1, 0, 0, 1, 1 }, new String[] { "ая", "я",
                "", "", "х", "ь" }), D(new int[] { 1, 2, 1, 1, 1, 1 }, new String[] { "й", "ень", "х", "я", "х", "ей" }), H(new int[] { 1, 1, 1, 1,
                1, 1 }, new String[] { "й", "", "х", "а", "х", "ов" }), Y(new int[] { 1, 1, 1, 1, 1, 4 }, new String[] { "й", "", "х", "а", "х",
                "лет" }), MONTH(new int[] { 1, 1, 1, 1, 1, 1 }, new String[] { "й", "", "х", "а", "х", "ев" });

        private final int[] charsToRemove;
        private final String[] charsToAdd;

        private Rule(int[] charsToRemove, String[] charsToAdd) {
            this.charsToRemove = charsToRemove;
            this.charsToAdd = charsToAdd;
        }

        public static Rule find(String base) {
            if (base.contains("часы")) {
                return H;
            } else if (base.contains("минуты")) {
                return MIN;
            } else if (base.contains("дни")) {
                return D;
            } else if (base.contains("недели")) {
                return W;
            } else if (base.contains("месяцы")) {
                return MONTH;
            } else if (base.contains("года")) {
                return Y;
            } else if (base.contains("секунды")) {
                return MIN;
            } else {
                throw new IllegalArgumentException(base);
            }
        }
    }
}
