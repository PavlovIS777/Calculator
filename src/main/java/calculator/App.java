package calculator;

public class App {
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        String input = System.console().readLine();
        System.out.println(calc.Calculate(input));
    }
}