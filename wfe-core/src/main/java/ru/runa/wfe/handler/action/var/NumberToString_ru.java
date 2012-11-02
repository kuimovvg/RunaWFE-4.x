package ru.runa.wfe.handler.action.var;

public class NumberToString_ru {
    public static void main(String[] args) {
        System.out.println(numberToString(2));
        System.out.println(numberToString(15));
        System.out.println(numberToString(57));

        System.out.println();

        Word day = new Word(MALE, new String[] { "день", "дня", "дней" });

        System.out.println(numberToString(0, day));
        System.out.println(numberToString(2, day));
        System.out.println(numberToString(15, day));
        System.out.println(numberToString(571, day));
        System.out.println(numberToString(-1000, day));
        System.out.println(numberToString(786783655377454L, day));
        System.out.println(numberToString(9786783655377454L, day));

        System.out.println();

        System.out.println(numberToShortString(0, day));
        System.out.println(numberToShortString(2, day));
        System.out.println(numberToShortString(15, day));
        System.out.println(numberToShortString(571, day));

        System.out.println();

        Word obj = new Word(FEMALE, new String[] { "сепулька", "сепульки", "сепулек" });
        System.out.println(numberToString(0, obj));
        System.out.println(numberToString(2, obj));
        System.out.println(numberToString(15, obj));
        System.out.println(numberToString(571, obj));
        System.out.println(numberToString(1000, obj));
    }

    public static int MALE = 0;
    public static int FEMALE = 1;

    public static class Word {
        public int p;
        public String[] s;

        public Word(int param, String[] str) {
            p = param;
            s = str;
        }
    }

    private static class Num {
        public String[] s;
        int p;

        public Num(String[] str, int param) {
            p = param;
            s = str;
        }
    }

    private static Word[] words = { new Word(1, new String[] { "тысяча", "тысячи", "тысяч" }),
            new Word(0, new String[] { "миллион", "миллиона", "миллионов" }), new Word(0, new String[] { "миллиард", "миллиарда", "миллиардов" }),
            new Word(0, new String[] { "триллион", "триллиона", "триллионов" }) };
    private static String[] hundreds = { "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот" };
    private static String[] decades = { "десять", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто" };
    private static String[] teens = { "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать",
            "восемнадцать", "девятнадцать" };
    private static Num[] digits = { new Num(new String[] { "ноль", "ноль" }, 2), new Num(new String[] { "один", "одна" }, 0),
            new Num(new String[] { "два", "две" }, 1), new Num(new String[] { "три", "три" }, 1), new Num(new String[] { "четыре", "четыре" }, 1),
            new Num(new String[] { "пять", "пять" }, 2), new Num(new String[] { "шесть", "шесть" }, 2), new Num(new String[] { "семь", "семь" }, 2),
            new Num(new String[] { "восемь", "восемь" }, 2), new Num(new String[] { "девять", "девять" }, 0) };

    public static String numberToShortString(long number, Word word) {
        String answer = "";
        number %= 100;
        if (number / 10 == 1) {
            if (answer.length() > 0) {
                answer += " ";
            }
            answer += word.s[2];
            return answer;
        }
        number %= 10;
        answer += word.s[digits[(int) number].p];
        return answer;

    }

    public static String numberToString(long number) {
        return numberToString(number, new Word(0, new String[] { "", "", "" }));
    }

    public static String numberToString(long number, Word word) {
        String answer = "";
        if (number < 0) {
            answer = "минус";
            number *= -1;
        }
        for (int a = words.length - 1; a >= 0; a--) {
            long st = 1;
            for (int b = 0; b <= a; b++) {
                st *= 1000;
            }
            long val = number / st;
            if (val / 1000 > 0) {
                return "Слишком большое число " + word.s[2];
            }
            if (val > 0) {
                if (answer.length() > 0) {
                    answer += " ";
                }
                answer += shortNumberToString((int) val, words[a]);
                number = number % st;
            }
        }
        if (number > 0 || answer.length() == 0) {
            if (answer.length() > 0) {
                answer += " ";
            }
            answer += shortNumberToString((int) number, word);
        } else {
            answer += " " + word.s[2];
        }
        char first = (char) (answer.charAt(0) + 'А' - 'а');
        answer = "" + first + answer.substring(1);
        return answer;
    }

    private static String shortNumberToString(int number, Word word) {
        String answer = "";
        if (number / 100 > 0) {
            answer = hundreds[number / 100 - 1];
        }
        number %= 100;
        if (number / 10 == 1) {
            if (answer.length() > 0) {
                answer += " ";
            }
            answer += teens[number - 10] + (word.s[2].length() > 0 ? (" " + word.s[2]) : "");
            return answer;
        }
        if (number / 10 > 1) {
            if (answer.length() > 0) {
                answer += " ";
            }
            answer += decades[number / 10 - 1];
        }
        number %= 10;
        if (number > 0 || answer.length() == 0) {
            if (answer.length() > 0) {
                answer += " ";
            }
            answer += digits[number].s[word.p];
        }
        String ws = word.s[digits[number].p];
        if (ws.length() > 0) {
            answer += " " + ws;
        }
        return answer;
    }
}
