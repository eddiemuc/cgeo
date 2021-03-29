package cgeo.geocaching.utils.expressions;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;

public class ExpressionParserTest {

    private final ExpressionParser<LambdaExpression<Integer, Integer>> calculator = new ExpressionParser<>();

    @Before
    public void before() {
        calculator
            .register(() -> LambdaExpression.createSimple("", (s, i) -> Integer.parseInt(s)))
            .register(() -> LambdaExpression.createSimple("x", (s, i) -> i))
            .register(() -> LambdaExpression.createSimple("length", (s, i) -> s == null ? 0 : s.length()))
            .register(() -> LambdaExpression.createBinary("+", 5, false, (s, i, pair) -> pair.left + pair.right))
            .register(() -> LambdaExpression.createBinary("-", 5, true, (s, i, pair) -> pair.left - pair.right))
            .register(() -> LambdaExpression.createBinary("*", 10, false, (s, i, pair) -> pair.left * pair.right))
            .register(() -> LambdaExpression.createBinary(":", 10, true, (s, i, pair) -> pair.left / pair.right))
            .register(() -> LambdaExpression.createBinary("^", 20, true, (s, i, pair) -> (int) Math.pow(pair.left, pair.right)))
            .register(() -> LambdaExpression.createUnary("neg", (s, i, pair) -> -pair));
    }

    @Test
    public void simple() throws ParseException {
        assertLambdaExpression("2", true, 0, 2);
        assertLambdaExpression(":2", false, 0, 2);
        assertLambdaExpression("x", true, 3, 3);
        assertLambdaExpression("length:abcde", true, 0, 5);
    }

    @Test
    public void simpleBinaryOp() throws ParseException {
        assertLambdaExpression("2 + x", false, 4, 6);
        assertLambdaExpression("2 + 3 + x", true, 4, 9);
    }

    @Test
    public void complexBinaryOp() throws ParseException {
        assertLambdaExpression("2 + neg x", false, -4, 6);
    }

    @Test
    public void binaryOpPriority() throws ParseException {
        //check right parenthesis setting
        assertLambdaExpression("x - 5 * 3", true, 30, 15);
        assertLambdaExpression("(x - 5) * 3", true, 30, 75);

        //check right parenthesis setting
        assertLambdaExpression("(3 + 4) - (1 + 2)", false, 0, 4);
        assertLambdaExpression("3 + 4 - (1 + 2)", true, 0, 4);
        assertLambdaExpression("(8 - 2) + (3 - 1)", false, 0, 8);

        assertLambdaExpression("2 + 3 * x + 1", true, 4, 15);
        assertLambdaExpression("(2 + 3) * x + 1", false, 4, 21);
        assertLambdaExpression("x - 5 - 3", false, 20, 12);
        assertLambdaExpression("x - (5 - 3)", false, 20, 18);

    }

    @Test
    public void veryComplex() throws ParseException {
        assertLambdaExpression("x ^ 2", false, 4, 16);
        assertLambdaExpression("x ^ 2 + 3 * x", false, 4, 28);
        assertLambdaExpression("neg (x + 5) * neg -3", false, 4, -27);
        assertLambdaExpression("5 ^ 2 + (neg (x + 5)) * neg -3", false, 4, -2);
        assertLambdaExpression("x ^ 2 + 3 * x - (5 ^ 2 + (neg (x + 5)) * neg -3)", false, 4, 30);
    }

    @Test
    public void escapedCharacters() throws ParseException {
        //length contains all possible escaped characters, should be 7 (backslash, space, tab, linebreak, ), (, :, a)
        assertLambdaExpression("(8 \\: 4) + length:\\\\\\ \\\t\\\n\\)\\(\\:a   + x", false, 5, 15);
    }

    @Test
    public void whitespaces() throws ParseException {
        assertLambdaExpression("  length:1234567  ", false, 0, 7);
        assertLambdaExpression("   length:1234567  +  (    \n \t  2    *     x  )   ", false, 8, 23);
    }

    @Test
    public void expressionConfigs() throws ParseException {
        assertLambdaExpression("length:234", true, 0, 3);
    }

    @Test
    public void parsingErrors() {
        assertParseException("", "Unexpected end of expression");
        assertParseException("()", "Expression expected, but none was found");
        assertParseException("(5 + 3", "Expected closing parenthesis");
        assertParseException("(5 +", "Unexpected end of expression");
        assertParseException("+ 5", "Unexpected binary expression");
        assertParseException("length : 234", "Expression expected, but none was found");
        assertParseException("nonexisting:x", "No expression type found for id");
        assertParseException("5 +:x 3", "may not have a configuration but has");
        assertParseException("3 + x a", "Expected binary expression but got");
        assertParseException("3 + x )", "Unexpected leftover in expression");
    }

    private void assertParseException(final String expressionString, final String expectedMessageContains) {
        try {
            final LambdaExpression<Integer, Integer> exp = calculator.create(expressionString);
            fail("Expected ParseException for '" + expressionString + "', but got: '" + calculator.getConfig(exp) + "'");
        } catch (ParseException pe) {
            assertThat(pe.getMessage()).as("ParsingException message").contains(expectedMessageContains);
        }
    }

    private void assertLambdaExpression(final String expressionString, final boolean testConfigEquality, final Integer ... paramExpextedResultPairs) throws ParseException {
        final LambdaExpression<Integer, Integer> exp = calculator.create(expressionString);
        for (int i = 0; i < paramExpextedResultPairs.length; i += 2) {
            assertThat(exp.call(paramExpextedResultPairs[i])).isEqualTo(paramExpextedResultPairs[i + 1]);
        }
        final String config = calculator.getConfig(exp);
        if (testConfigEquality) {
            assertThat(calculator.getConfig(exp)).isEqualTo(expressionString);
        }
        final LambdaExpression<Integer, Integer> exp2 = calculator.create(config);
        for (int i = 0; i < paramExpextedResultPairs.length; i += 2) {
            assertThat(exp2.call(paramExpextedResultPairs[i])).as("Exp:'" + expressionString + "' -> '" + config + "'").isEqualTo(paramExpextedResultPairs[i + 1]);
        }
    }
}
