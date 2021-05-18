package com.mirea.bsbo0419.latexdroid.apis.RPN;
import java.util.*;
import java.lang.*;

public class RPN_Core {

    public static Double calc(List<String> expression) {
        Deque<Double> stack = new ArrayDeque<Double>();
        for (String x : expression) {
            switch (x) {
                case "+":
                    stack.push(stack.pop() + stack.pop());
                    break;
                case "-": {
                    Double b = stack.pop(), a = stack.pop();
                    stack.push(a - b);
                    break;
                }
                case "*":
                    stack.push(stack.pop() * stack.pop());
                    break;
                case "/": {
                    Double b = stack.pop(), a = stack.pop();
                    stack.push(a / b);
                    break;
                }
                case "-u":
                    stack.push(-stack.pop());
                    break;
                default:
                    if (x.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
                        stack.push(Double.valueOf(x));
                    else {
                        return 0.0;
                    }
                    break;
            }
        }
        return stack.pop();
    }

    /*public static Double calc() {
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        List<String> expression = RPN_Parser.getParsedStr(s);
        for (String x : expression) System.out.print(x + " ");  //RPN output
        if (!expression.contains("Error")) {
            System.out.println();
            System.out.println(calc(expression));               //Result output
            return calc(expression);
        }
        return null;
    }*/
}
