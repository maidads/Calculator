import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Double.valueOf;
import static java.lang.Math.pow;
import static java.lang.System.out;


/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
public class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        List<String> postfix = infix2Postfix(tokens);
        return evalPostfix(postfix);
    }

    // ------  Evaluate RPN expression -------------------
    double evalPostfix(List<String> postfix) {
        Deque<String> stack = new ArrayDeque<String>();
        double d1;
        double d2;
        double temp;
        if (postfix.size() == 1){
            return valueOf(postfix.get(0));
        }
        for (String pf:postfix){
            if (!isOperator(pf)){
                stack.push(pf);
            } else {
                try {
                    d1 = Double.valueOf(stack.pop());                               // transform string to a double
                    d2 = Double.valueOf(stack.pop());                               // pop next ToS
                    temp = applyOperator(pf,d1,d2);                                 // temp result
                    stack.push(String.valueOf(temp));                               // put back on stack
                }
                catch (NoSuchElementException e){
                    throw new IllegalArgumentException(MISSING_OPERAND);
                }
            }
        }
        return Double.valueOf(stack.pop());                                 // return final result
    }


    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix --------------------

    List<String> infix2Postfix(List<String> infix) {
        List<String> postfix = new ArrayList<String>();                       // A list
        Deque<String> stack = new ArrayDeque<String>();
        String operators = "-+*/^()";
        int countOperators = 0;
        int countOperands = 0;
        for (int i = 0; i < infix.size(); i++) {
            if (!operators.contains(infix.get(i))) {
                postfix.add(infix.get(i));
                countOperands++;
            } else if (operators.contains(infix.get(i))) {
                if (infix.get(i).equals(")")) {
                    for (int k = 0; k < stack.size(); k++) {
                        if (!stack.peek().equals("(")) {
                            postfix.add(stack.pop());
                            countOperators++;
                        } else {
                            stack.pop();
                            break;
                        }
                    }
                } else if (stack.isEmpty() || stack.peek().equals("(")) {
                    stack.push(infix.get(i));
                } else if (infix.get(i).contains("(")) {
                    stack.push(infix.get(i));
                } else if (getPrecedence(stack.peek()) > getPrecedence(infix.get(i))) {
                    while (!stack.isEmpty() && getPrecedence(stack.peek()) > getPrecedence(infix.get(i))) {     //pre. stack > pre. infix
                        postfix.add(stack.pop());
                        countOperators++;
                    }
                    stack.push(infix.get(i));
                } else if (getPrecedence(stack.peek()) < getPrecedence(infix.get(i))) {
                    stack.push(infix.get(i));
                } else if (getPrecedence(stack.peek()) == getPrecedence(infix.get(i))) {                // pre. stack = pre. infix
                    if (getAssociativity(infix.get(i)) == Assoc.LEFT) {
                        postfix.add(stack.pop());
                        stack.push(infix.get(i));
                        countOperators++;
                    } else {
                        stack.push(infix.get(i));
                    }
                }
            }
        }
        while (!stack.isEmpty() && !stack.peek().equals("(")) {        //infix is empty now, so pop out all operators in the stack to postfix
            postfix.add(stack.pop());
            countOperators++;
        }
    if (countOperators + 1 < countOperands) {
         throw new IllegalArgumentException(MISSING_OPERATOR);
        }
       // System.out.println(postfix);
        return postfix;
    }


    boolean isOperator(String tokens){
        String[] operators = {"+","-","*","/","^","(",")"};
        for (String o : operators) {
            if (o.contains(tokens)) {
                return true;
            }
        }
        return false;
    }



    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------

boolean isOperator ( char ch){
    return (ch == '(' || ch == ')' || ch == '^' || ch == '/' || ch == '*' || ch == '+' || ch == '-');
}


    List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<String>();
        char[] arr = expr.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char ch2 : expr.toCharArray()) {
            if (Character.isDigit(ch2)) {                           // if Digit
                sb.append(ch2);
            } else if (isOperator(ch2)) {                           // if Operator
                if (sb.length() != 0) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                    sb.append(ch2);
                    tokens.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(ch2);                                 //if sb empty, ex. + 10
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
            } else if (sb.length() != 0) {                          // if Whitespace
                tokens.add(sb.toString());
                sb.setLength(0);
            }
        }
        if (sb.length() != 0) {                                     // empty sb
            tokens.add(sb.toString());
            sb.setLength(0);
        }
        if (!tokens.contains("(") && tokens.contains(")") || tokens.contains("(") && !tokens.contains(")")) {
            throw new IllegalArgumentException(MISSING_OPERATOR);
        }
          // System.out.println(tokens);
            return tokens;
        }
    }












/*
    // List String (not char) because numbers (with many chars)
    List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<String>();
        char[] arr = expr.toCharArray();
        for (char ch: expr.toCharArray()) {
            if (Character.isDigit(ch) && expr.contains(" ")) {
                throw new IllegalArgumentException(MISSING_OPERATOR);
            }
            expr = expr.replace(" ", "");
            String[] split = expr.split("(?<=[\\d.])(?=[^\\d.])|(?<=[^\\d.])(?=[\\d.])|(?=[()])|(?<=[()])");
            for (String s : split) {
                // System.out.println(s);
                tokens.add(s);
            }
        }
            System.out.println(tokens);
            return tokens;
        }
}



*/
/*

List<String> tokenize(String expr) {
    List<String> tokens = new ArrayList<String>();
    char[] arr = expr.toCharArray();
    StringBuilder sb = new StringBuilder();
    StringBuilder sc = new StringBuilder();
    for (char ch : expr.toCharArray()) {
        if (!Character.isWhitespace(ch)) {
            sb.append(ch);
        }
    }
    String s = sb.toString();
    for (char ch2 : s.toCharArray()) {
        if (Character.isDigit(ch2)) {
            sc.append(ch2);
        } else if (isOperator(ch2)) {
            if (sc.length() != 0) {
                tokens.add(sc.toString());
                sc.setLength(0);
                sc.append(ch2);
                tokens.add(sc.toString());
                sc.setLength(0);
            } else {
                sc.append(ch2);
                tokens.add(sc.toString());
                sc.setLength(0);
            }
        }
        }
        if(sc.length()!=0){
            tokens.add(sc.toString());
            sc.setLength(0);
        }

        System.out.println(tokens);
        return tokens;

    }

    boolean isOperator ( char ch){
        return (ch == '(' || ch == ')' || ch == '^' || ch == '/' || ch == '*' || ch == '+' || ch == '-');
    }
    boolean isOperator ( char ch){
        return (ch == '(' || ch == ')' || ch == '^' || ch == '/' || ch == '*' || ch == '+' || ch == '-');
    }


    Double evalPostfix(List<String> postfix) {
        Deque<String> stack = new ArrayDeque<String>();
        if (postfix.size() == 1) {
            return Double.valueOf(postfix.get(0));
        }
        for (int j = 0; j < postfix.size(); j++) {
            if (!OPERATORS.contains(postfix.get(j))) {
                stack.push(postfix.get(j));
            } else {
                double x = Double.valueOf(stack.pop());
                double y = Double.valueOf(stack.pop());
                stack.push(applyOperator(postfix.get(j), x, y) + "");
            }
        }
        //System.out.println(stack.peek());
        return Double.valueOf(stack.peek());
    }



 List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<String>();
        char[] arr = expr.toCharArray();
        // 10 10 -> sb==10 stöter på mellanslag -> lägg sb i tokens och töm sb
        StringBuilder sb = new StringBuilder();
        StringBuilder sc = new StringBuilder();
        for (char ch : expr.toCharArray()) {
            if (!Character.isWhitespace(ch)) {
                sb.append(ch);
            }
        }
        String s = sb.toString();
        for (char ch2 : s.toCharArray()) {
            if (Character.isDigit(ch2)) {
                sc.append(ch2);
            } else if (isOperator(ch2)) {
                if (sc.length() != 0) {
                    tokens.add(sc.toString());
                    sc.setLength(0);
                    sc.append(ch2);
                    tokens.add(sc.toString());
                    sc.setLength(0);
                } else {
                    sc.append(ch2);
                    tokens.add(sc.toString());
                    sc.setLength(0);
                }
            }
        }
        if(sc.length()!=0){
            tokens.add(sc.toString());
            sc.setLength(0);
        }

        System.out.println(tokens);
        return tokens;

    }
    boolean isOperator ( char ch){
        return (ch == '(' || ch == ')' || ch == '^' || ch == '/' || ch == '*' || ch == '+' || ch == '-');
    }

}


 */
/*
    List<String> tokenize(String expr) {
        if (noOperator(expr) && expr.contains(" ")) {
            throw new IllegalArgumentException(MISSING_OPERATOR);
        }
        expr = expr.replace(" ", "");
        List<String> tokens = new ArrayList<String>();                          // Creating a List of type String using ArrayList
        String[] split = expr.split("(?<=[\\d.])(?=[^\\d.])|(?<=[^\\d.])(?=[\\d.])|(?=[()])|(?<=[()])");
       for (String s : split) {
            //System.out.println(s);

            tokens.add(s);
        }
       if (!tokens.contains("(") && tokens.contains(")") || tokens.contains("(") && !tokens.contains(")")) {
           throw new IllegalArgumentException(MISSING_OPERATOR);
       }
        //out.println(tokens);
        return tokens;
    }

 */