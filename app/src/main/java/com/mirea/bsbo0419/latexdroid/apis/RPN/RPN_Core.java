package com.mirea.bsbo0419.latexdroid.apis.RPN;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

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
}
