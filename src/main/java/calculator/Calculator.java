package calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.function.Function;

public class Calculator {

    // enum с типами токенов
    public enum TokenType {
        NUMBER,
        BINARY_OPERATION
    }

    // enum с типами операций
    public enum OperationType {
        MINUS,
        PLUS,
        MULTIPLY,
        DIVIDE
    }

    public int getOperationPriority(OperationToken token) {
        return switch (token.opType()) {
            case OperationType.PLUS, OperationType.MINUS -> 1;            
            case OperationType.MULTIPLY, OperationType.DIVIDE -> 2;
            default -> throw new RuntimeException("Unexpected token: " + token);
        };
    }

    // enum с типом числа
    public enum DigitType {
        ARABIC,
        ROMAN,
        NONE
    }
    DigitType type;

    // интерфейс токена
    public interface Token {
        TokenType type();
    }

    // реализация токена числа
    public record NumberToken(
        Integer value
    ) implements Token {
        @Override
        public TokenType type() {
            return TokenType.NUMBER;
        }
    }

    // реализация токена операции
    public record OperationToken(
        OperationType opType
    ) implements Token {
        @Override
        public TokenType type() {
            return TokenType.BINARY_OPERATION;
        }
    }

    // Преобразование выражения к польской нотации
    public List<Token> toPostfixNotation(String input) {

        List<Token> tokens = Parser.getTokens(input, this.type);
        List<Token> postfixExpression = new ArrayList<>();
        Stack<Token> operationStack = new Stack<>();

        for(Token token : tokens) {
            switch (token.type()) {
                case TokenType.BINARY_OPERATION:
                    while (!operationStack.isEmpty() && getOperationPriority((OperationToken)token) <=
                    getOperationPriority((OperationToken)operationStack.peek()) ) {
                        postfixExpression.add(operationStack.pop());
                    }
                    operationStack.push(token);
                    break;
                case TokenType.NUMBER:
                    postfixExpression.add(token);
                    break;
                default:
                    throw new RuntimeException("Unexpected token type: " + token.type());
            }
        }
        while (!operationStack.isEmpty()) {
            postfixExpression.add(operationStack.pop());
        }
        return postfixExpression;
    }

    public String Calculate(String input) {
        // Устанавливаем тип цифр
        if (input.matches(".*[0-9].*")) {
            this.type = DigitType.ARABIC;
        } else {
            this.type = DigitType.ROMAN;
        }


        Stack<Integer> operandsStack = new Stack<>();
        List<Token> postfixExpression = toPostfixNotation(input);
        for (Token token : postfixExpression) {
            if (token instanceof NumberToken numberToken) {
                operandsStack.push(numberToken.value());
            } else if (token instanceof OperationToken operation) {
                Integer rhv = operandsStack.pop();
                Integer lhv = operandsStack.pop();
                operandsStack.push(switch (operation.opType()) {
                    case PLUS -> lhv + rhv;
                    case MINUS -> lhv - rhv;
                    case MULTIPLY -> lhv * rhv;
                    case DIVIDE -> lhv / rhv;
                    }
                );
            }
        }

        if (this.type == DigitType.ROMAN) {
            Integer answ = operandsStack.pop();
            if (answ < 1) {
                throw new RuntimeException("Can't calculate non-positive roman values");
            }
            return Parser.integerToRoman(answ);
        }

        return Integer.toString(operandsStack.pop());
    }

    private static class Parser {

        public static List<Token> getTokens(String source, DigitType digitType) {
            StringTokenizer tokenizer = new StringTokenizer(source, " -+*/", true);
            List<Token> tokens = new ArrayList<>();
            Function<String, Integer> parseMethod = digitType == DigitType.ROMAN ? Parser::romanToInt : Parser::arabicToInt;
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if(token.isBlank()) {
                    continue;
                } else if (token.matches("(\\+|-|/|\\*)"))
                {
                    tokens.add(
                        switch (token) {
                            case "+" -> new OperationToken(OperationType.PLUS);
                            case "-" -> new OperationToken(OperationType.MINUS);
                            case "*" -> new OperationToken(OperationType.MULTIPLY);
                            case "/" -> new OperationToken(OperationType.DIVIDE);
                            default -> throw new RuntimeException("Unexpected token: " + token);
                        }
                    );
                    continue;
                }

                tokens.add(new NumberToken(parseMethod.apply(token)));
            }

            return tokens;
        }

        public static int romanToInt(String roman) {
            String regex = "^X*(IX|IV|V?I{0,3})$";
            if (!roman.matches(regex)) {
                throw new IllegalArgumentException("Invalid Roman numeral: " + roman);
            }

            Map<Character, Integer> m = new HashMap<>();
        
            m.put('I', 1);
            m.put('V', 5);
            m.put('X', 10);
            m.put('L', 50);
            m.put('C', 100);
            m.put('D', 500);
            m.put('M', 1000);
            
            int ans = 0;
            
            for (int i = 0; i < roman.length(); i++) {
                if (i < roman.length() - 1 && m.get(roman.charAt(i)) < m.get(roman.charAt(i + 1))) {
                    ans -= m.get(roman.charAt(i));
                } else {
                    ans += m.get(roman.charAt(i));
                }
            }
            
            if (ans > 10) {
                throw new RuntimeException("Calculator accepts only numbers no more than 10");
            }
            
            return ans;
        }


        public static Integer arabicToInt(String arabic) {
            int ans = 0;
            ans = Integer.parseInt(arabic);

            if (ans > 10) {
                throw new RuntimeException("Calculator accepts only numbers no more than 10");
            }

            return ans;
        }

        public static String integerToRoman(Integer num) {
            String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
            String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
            String[] hrns = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
            String[] ths = {"", "M", "MM", "MMM"};

            return ths[num / 1000] + hrns[(num % 1000) / 100] + tens[(num % 100) / 10] + ones[num % 10];
        }
    }
}