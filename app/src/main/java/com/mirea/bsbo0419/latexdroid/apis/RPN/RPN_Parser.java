package com.mirea.bsbo0419.latexdroid.apis.RPN;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.StringTokenizer;

public class RPN_Parser {
    private static final String operations = "+-*/";
    private static final String delimiters = "()" + operations;

    private static boolean isDelimiter(String token) {
        return delimiters.contains(token);
    }

    private static boolean isOperator(String token) {
        return operations.contains(token);
    }

    private static int getPriority(String token) {
        return "(+-*/)".indexOf(token);
    }

    private static String clearString(String s) {
        return s.replaceAll("[a-zA-Z ]", "");
    }

    private static boolean isDigit(String str) {
        return Character.isDigit(str.charAt(0));
    }

    private static List<String> getErrorRes(){
        ArrayList<String> err = new ArrayList<>();
        err.add("Error");
        return err;
    }

    public static List<String> getParsedStr(String arg) {
        List<String> result = new ArrayList<String>();
        Deque<String> stack = new ArrayDeque<String>();
        String s = clearString(arg);
        if (s.equals("")) return getErrorRes();
        StringTokenizer tokenizer = new StringTokenizer(s, delimiters, true);
        String curr = "";
        while (tokenizer.hasMoreTokens()) {
            curr = tokenizer.nextToken();
            if (isDigit(curr)) result.add(curr);
            if (curr.equals("(")) stack.push("(");
            if (curr.equals(")")){
                stack.remove("(");
                while (stack.peek() != null && !stack.peek().equals("("))
                    result.add(stack.pop());
            }
            if (isOperator(curr)) {
                while (!stack.isEmpty() && (getPriority(curr) <= getPriority(stack.peek()))) {
                    result.add(stack.pop());
                }
                stack.push(curr);
            }
        }
        while (!stack.isEmpty()) {
            if (isOperator(stack.peek())) result.add(stack.pop());
            else {
                return getErrorRes();
            }
        }
        return result;
    }
}
