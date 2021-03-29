package cgeo.geocaching.utils.expressions;

import androidx.annotation.NonNull;
import androidx.core.util.Supplier;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionParser<T extends IExpression<T>> {


    private static final char OPEN_PAREN = '(';
    private static final char CLOSE_PAREN = ')';
    private static final char TYPEID_CONFIG_SEPARATOR = ':';
    private static final char ESCAPE_CHAR = (char) 92; //backslash

    private final Map<String, Supplier<T>> registeredExpressions = new HashMap<>();

    public ExpressionParser<T> register(final Supplier<T> expressionCreator) {
        final String typeId = expressionCreator.get().getId();
        this.registeredExpressions.put(typeId == null ? "" : typeId.trim(), expressionCreator);
        return this;
    }

    public T create(@NonNull final String config) throws ParseException {
        return new Parser(config).parse();
    }

    public String getConfig(@NonNull final T exp) {
        final StringBuilder sb = new StringBuilder();
        writeConfig(exp, null, false, sb);
        return sb.toString();
    }


    private void writeConfig(final T exp, final T parent, final boolean isLeftChild, final StringBuilder stringBuilder) {

        switch (exp.getType()) {
            case SIMPLE:
                final String expConfig = exp.getConfig();
                if (exp.getId().isEmpty() && expConfig != null && !expConfig.isEmpty()) {
                    stringBuilder.append(escape(expConfig));
                } else {
                    stringBuilder.append(escape(exp.getId()));
                    if (expConfig != null) {
                        stringBuilder.append(TYPEID_CONFIG_SEPARATOR).append(escape(expConfig));
                    }
                }
                break;
            case OPERATOR_UNARY:
                stringBuilder.append(escape(exp.getId()));
                stringBuilder.append(" ");
                writeConfig(exp.getChildLeft(), exp, true, stringBuilder);
                break;
            case OPERATOR_BINARY:
                final boolean parenthesesNecessary = parent != null && (
                    parent.getType() != IExpression.ExpressionType.OPERATOR_BINARY ||
                    exp.getOperatorBinaryOrderSensitive() ||
                    parent.getOperatorBinaryPriority() > exp.getOperatorBinaryPriority() ||
                    (parent.getOperatorBinaryPriority() == exp.getOperatorBinaryPriority() && !isLeftChild && parent.getOperatorBinaryOrderSensitive()));
                if (parenthesesNecessary) {
                    stringBuilder.append(OPEN_PAREN);
                }
                writeConfig(exp.getChildLeft(), exp, true, stringBuilder);
                stringBuilder.append(" ").append(escape(exp.getId())).append(" ");
                writeConfig(exp.getChildRight(), exp, false, stringBuilder);
                if (parenthesesNecessary) {
                    stringBuilder.append(CLOSE_PAREN);
                }
                break;
        }
    }

    private String escape(final String raw) {
        return raw.replaceAll(""  + ESCAPE_CHAR + ESCAPE_CHAR, ""  + ESCAPE_CHAR + ESCAPE_CHAR + ESCAPE_CHAR + ESCAPE_CHAR)
            .replaceAll("" + TYPEID_CONFIG_SEPARATOR, "" + ESCAPE_CHAR + ESCAPE_CHAR + TYPEID_CONFIG_SEPARATOR)
            .replaceAll("(\\s)", "" + ESCAPE_CHAR + ESCAPE_CHAR + "$1")
            .replaceAll("" + ESCAPE_CHAR + OPEN_PAREN, "" + ESCAPE_CHAR + ESCAPE_CHAR + OPEN_PAREN)
            .replaceAll("" + ESCAPE_CHAR + CLOSE_PAREN, "" + ESCAPE_CHAR + ESCAPE_CHAR + CLOSE_PAREN);

    }

    private class Parser {
        private final String config;
        private int idx = 0;

        Parser(final String config) {
            this.config = config;
        }

        @NonNull
        public T parse() throws ParseException {
            final T result = parseNext();
            moveToNextToken();
            if (config.length() != idx) {
                throwParseException("Unexpected leftover in expression");
            }
            return result;
        }

        /** Parses next expression starting from idx and leaving idx at next token AFTER expression */
        private T parseNext() throws ParseException {
            checkEndOfExpression();

            final List<T> expressions = new ArrayList<>();
            final List<T> operators = new ArrayList<>();
            int maxPrio = -1;

            expressions.add(parseNextNonBinaryExpression());
            while (true) {
                moveToNextToken();
                if (idx >= config.length() || config.charAt(idx) == CLOSE_PAREN) {
                    break;
                }

                final T op = parseNextRawExpression();
                if (op.getType() != IExpression.ExpressionType.OPERATOR_BINARY) {
                    throwParseException("Expected binary expression but got " + ExpressionParser.this.toString(op));
                }
                final T ex = parseNextNonBinaryExpression();
                expressions.add(ex);
                operators.add(op);
                if (maxPrio < op.getOperatorBinaryPriority()) {
                    maxPrio = op.getOperatorBinaryPriority();
                }
            }

            //now what to do with the ops and exps?
            while (expressions.size() > 1) {
                int newMaxPrio = -1;
                for(int i=0; i < operators.size(); i++) {
                    final T op = operators.get(i);
                    if (op.getOperatorBinaryPriority() == maxPrio) {
                        op.addChildren(expressions.get(i), expressions.get(i+1));
                        operators.remove(i);
                        expressions.set(i, op);
                        expressions.remove(i + 1);
                        i--;
                    } else if (newMaxPrio < op.getOperatorBinaryPriority()) {
                        newMaxPrio = op.getOperatorBinaryPriority();
                    }
                }
                maxPrio = newMaxPrio;
            }
            return expressions.get(0);
        }

        private T parseNextNonBinaryExpression() throws ParseException {
            checkEndOfExpression();

            if (currentCharIs(OPEN_PAREN)) {
                idx++;
                final T result = parseNext();
                if (!currentCharIs(CLOSE_PAREN)) {
                    throwParseException("Expected closing parenthesis");
                }
                idx++;
                return result;
            }

            final T exp = parseNextRawExpression();
            if (exp.getType() == IExpression.ExpressionType.OPERATOR_BINARY) {
                throwParseException("Unexpected binary expression " + ExpressionParser.this.toString(exp));
            }
            if (exp.getType() == IExpression.ExpressionType.OPERATOR_UNARY) {
                exp.addChildren(parseNextNonBinaryExpression(), null);
            }
            return exp;

        }

        private T parseNextRawExpression() throws ParseException {
            moveToNextToken();
            final String typeId = parseToNextDelim().trim();

            String typeConfig = null;
            if (currentCharIs(TYPEID_CONFIG_SEPARATOR, false)) {
                idx++;
                typeConfig = parseToNextDelim(); //NO TRIM! leading and trailing whitespaces must be preserved
            }
            if (typeId.isEmpty() && (typeConfig == null || typeConfig.isEmpty())) {
                throwParseException("Expression expected, but none was found");
            }

            final T expression;
            if (registeredExpressions.containsKey(typeId)) {
                expression = registeredExpressions.get(typeId).get();
                if (typeConfig != null) {
                    if (expression.getType() != IExpression.ExpressionType.SIMPLE) {
                        throwParseException("'" + ExpressionParser.this.toString(expression) + "' may not have a configuration but has '" + typeConfig + "'");
                    }
                    expression.setConfig(typeConfig);
                }
            } else if (registeredExpressions.containsKey("") && typeConfig == null) {
                expression = registeredExpressions.get("").get();
                if (expression.getType() != IExpression.ExpressionType.SIMPLE) {
                    throwParseException("default expression must be of SIMPLE type");
                }
                expression.setConfig(typeId);
            } else  {
                expression = null; //make compiler happy, value will never be used
                throwParseException("No expression type found for id '" + typeId + "' and no default expression could be applied");
            }

            return expression;
        }

        private String parseToNextDelim()  {
            final StringBuilder result = new StringBuilder();
            boolean nextCharIsEscaped = false;
            boolean done = false;
            while (!done) {
                if (idx >= config.length()) {
                    break;
                }
                final char cc = config.charAt(idx);
                final char c = Character.isWhitespace(cc) ? ' ' : cc;
                switch (c) {
                    case ESCAPE_CHAR:
                        if (nextCharIsEscaped) {
                            result.append(ESCAPE_CHAR);
                        }
                        nextCharIsEscaped = !nextCharIsEscaped;
                        break;
                    case TYPEID_CONFIG_SEPARATOR:
                    case OPEN_PAREN:
                    case ' ':
                    case CLOSE_PAREN:
                        if (!nextCharIsEscaped) {
                            done = true;
                            break;
                        }
                        result.append(cc);
                        nextCharIsEscaped = false;
                        break;
                    default:
                        result.append(cc);
                        nextCharIsEscaped = false;
                        break;
                }
                if (!done) {
                    idx++;
                }
            }
            return result.toString();
        }

        private void moveToNextToken() {
            while (idx < config.length() && Character.isWhitespace(config.charAt(idx))) {
                idx++;
            }
        }

        private boolean currentCharIs(final char c) {
            return currentCharIs(c, true);
        }

        private boolean currentCharIs(final char c, final boolean moveToNextToken) {
            if (moveToNextToken) {
                moveToNextToken();
            }
            return idx < config.length() && config.charAt(idx) == c;
        }

        private void checkEndOfExpression() throws ParseException {
            moveToNextToken();
            if (idx >= config.length()) {
                throwParseException("Unexpected end of expression");
            }
        }

        private void throwParseException(final String message) throws ParseException {
            String markedConfig = config;
            if (idx >= config.length()) {
                markedConfig += "[]";
            } else {
                markedConfig = markedConfig.substring(0, idx) + "[" + markedConfig.charAt(idx) + "]" + markedConfig.substring(idx + 1);
            }
            throw new ParseException("Problem parsing '" + markedConfig + "' (pos marked with []: " + idx + "): " + message, idx);
        }

    }

    public String toString(final IExpression<T> exp) {
        if (exp == null) {
            return "'null'";
        }
        return "'" + (exp.getId().isEmpty() ? "<default>" : exp.getId()) + "'(" + exp.getType() +
            (exp.getType() == IExpression.ExpressionType.SIMPLE ? ":" + exp.getConfig() : "") + ")";
    }

}
