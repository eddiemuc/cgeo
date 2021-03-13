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
            .register(() -> new LambdaExpression<>("", (s, i) -> Integer.parseInt(s)))
            .register(() -> new LambdaExpression<>("x", (s, i) -> i))
            .register(() -> new LambdaExpression<>("length", (s, i) -> s == null ? 0 : s.length()))
            .register(ExpressionParserTest::getPlusExpression)
            .register(ExpressionParserTest::getMinusExpression)
            .register(ExpressionParserTest::getTimesExpression)
            .register(ExpressionParserTest::getDivideExpression)
            .register(ExpressionParserTest::getPowExpression);
    }

    private static LambdaExpression<Integer, Integer> getPlusExpression() {
        return new LambdaExpression<>("+", (s, i, list) -> {
            int res = 0;
            for (Integer in : list) {
                res += in;
            }
            return res;
        });
    }

    private static LambdaExpression<Integer, Integer> getMinusExpression() {
        return new LambdaExpression<>("-", (s, i, list) -> {
            int res = 0;
            boolean first = true;
            for (Integer in : list) {
                if (first) {
                    res = in;
                } else {
                    res -= in;
                }
                first = false;
            }
            return res;
        });
    }

    private static LambdaExpression<Integer, Integer> getTimesExpression() {
        return new LambdaExpression<>("*", (s, i, list) -> {
            int res = list.isEmpty() ? 0 : 1;
            for (Integer in : list) {
                res *= in;
            }
            return res;
        });
    }

    private static LambdaExpression<Integer, Integer> getDivideExpression() {
        return new LambdaExpression<>(":", (s, i, list) -> {
            int res = 0;
            boolean first = true;
            for (Integer in : list) {
                if (first) {
                    res = in;
                } else {
                    res /= in;
                }
                first = false;
            }
            return res;
        });
    }

    private static LambdaExpression<Integer, Integer> getPowExpression() {
        return new LambdaExpression<>("^", (s, i, list) -> {
            int res = 0;
            final int factor = s == null ? 1 : Integer.parseInt(s);
            for (Integer in : list) {
                res += Math.pow(in, factor);
            }
            return res;
        });
    }

    @Test
    public void simple() throws ParseException {
        assertLambdaExpression(":2", true, 0, 2);
        assertLambdaExpression("2", false, 0, 2);
        assertLambdaExpression("x", true, 3, 3);
        assertLambdaExpression("length:abcde", true, 0, 5);
    }

    @Test
    public void simpleGroup() throws ParseException {
        assertLambdaExpression("+(:2;:3;x)", true, 4, 9);
    }

    @Test
    public void escapedCharacters() throws ParseException {
        assertLambdaExpression("\\::notused(*(length:\\)\\:\\\\\\(b\\;;:4);x)", true, 8, 3); //length should be 6
    }

    @Test
    public void whitespaces() throws ParseException {
        assertLambdaExpression("  length  :  1234567  ", false, 0, 7);
        assertLambdaExpression("   +   (  length  :  1234567  ;   :  1   ;   x ; -  (  3  ;  1  )  )  ", false, 8, 18);
    }

    @Test
    public void parameterizedGroupExpression() throws ParseException {
        assertLambdaExpression("^:2(x;:2)", true, 5, 29);
    }

    @Test
    public void parsingErrors() {
        assertParseException("", "Unexpected end of expression");
        assertParseException("()", "Expression expected, but none was found");
        assertParseException("+(5;3", "Unexpected end of expression");
        assertParseException("+(5;3;", "Unexpected end of expression");
        assertParseException("+(nonexisting:x)", "No expression type found for id");
        assertParseException("+(3;x)a", "Unexpected leftover in expression");
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
            assertThat(exp2.call(paramExpextedResultPairs[i])).isEqualTo(paramExpextedResultPairs[i + 1]);
        }
    }
}
