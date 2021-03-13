package cgeo.geocaching.utils.expressions;

import androidx.annotation.NonNull;
import androidx.core.util.Supplier;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionParser<T extends IExpression<T>> {


    private static final char OPEN_PAREN = '(';
    private static final char CLOSE_PAREN = ')';
    private static final char LOGIC_SEPARATOR = ';';
    private static final char TYPEID_CONFIG_SEPARATOR = ':';
    private static final char ESCAPE_CHAR = (char) 92; //backslash

    private final Map<String, Supplier<T>> registeredExpressions = new HashMap<>();


    public ExpressionParser<T> register(final Supplier<T> expressionCreator) {
        final String typeId = expressionCreator.get().getTypeId();
        this.registeredExpressions.put(typeId == null ? "" : typeId.trim(), expressionCreator);
        return this;
    }

    public T create(@NonNull final String config) throws ParseException {
        return new Parser(config).parse();
    }

    public String getConfig(@NonNull final T exp) {
        final StringBuilder sb = new StringBuilder();
        writeConfig(exp, sb);
        return sb.toString();
    }


    private void writeConfig(final T exp, final StringBuilder stringBuilder) {
        stringBuilder.append(escape(exp.getTypeId()));
        final String expConfig = exp.getConfig();
        if (expConfig != null) {
            stringBuilder.append(TYPEID_CONFIG_SEPARATOR).append(escape(expConfig));
        }
        final List<T> children = exp.getChildren();
        if (children != null && !children.isEmpty()) {
            stringBuilder.append(OPEN_PAREN);
            boolean first = true;
            for (T child : children) {
                if (!first) {
                    stringBuilder.append(LOGIC_SEPARATOR);
                }
                first = false;
                writeConfig(child, stringBuilder);
            }
            stringBuilder.append(CLOSE_PAREN);
        }
    }

    private String escape(final String raw) {
        return raw.replaceAll(""  + ESCAPE_CHAR + ESCAPE_CHAR, ""  + ESCAPE_CHAR + ESCAPE_CHAR + ESCAPE_CHAR + ESCAPE_CHAR)
            .replaceAll("" + LOGIC_SEPARATOR, "" + ESCAPE_CHAR + ESCAPE_CHAR + LOGIC_SEPARATOR)
            .replaceAll("" + TYPEID_CONFIG_SEPARATOR, "" + ESCAPE_CHAR + ESCAPE_CHAR + TYPEID_CONFIG_SEPARATOR)
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
            skipWhitespaces();
            if (config.length() != idx) {
                throwParseException("Unexpected leftover in expression");
            }
            return result;
        }

        /** Parses next expression starting from idx and leaving idx at next token AFTER expression */
        private T parseNext() throws ParseException {
            checkEndOfExpression();
            final T exp = parseNextExpression();
            skipWhitespaces();

            if (idx >= config.length() || config.charAt(idx) != OPEN_PAREN) {
                return exp;
            }

            idx++;
            while (true) {
                exp.addChild(parseNext());
                checkEndOfExpression();
                final boolean isClosingParen = config.charAt(idx) == CLOSE_PAREN;
                if (!isClosingParen && config.charAt(idx) != LOGIC_SEPARATOR) {
                    throwParseException("Expected '" + CLOSE_PAREN + "' or '" + LOGIC_SEPARATOR + "' but found '" + config.charAt(idx) + "'");
                }
                idx++;
                if (isClosingParen) {
                    return exp;
                }
            }
        }

        private T parseNextExpression() throws ParseException {
            final String typeId = parseToNextDelim().trim();

            String typeConfig = null;
            if (idx < config.length() && config.charAt(idx) == TYPEID_CONFIG_SEPARATOR) {
                idx++;
                typeConfig = parseToNextDelim().trim();
            }
            if (typeId.isEmpty() && typeConfig == null) {
                throwParseException("Expression expected, but none was found");
            }

            final T expression;
            if (registeredExpressions.containsKey(typeId)) {
                expression = registeredExpressions.get(typeId).get();
                if (typeConfig != null) {
                    expression.setConfig(typeConfig);
                }
            } else if (registeredExpressions.containsKey("") && typeConfig == null) {
                expression = registeredExpressions.get("").get();
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
                final char c = config.charAt(idx);
                switch (c) {
                    case ESCAPE_CHAR:
                        if (nextCharIsEscaped) {
                            result.append(ESCAPE_CHAR);
                        }
                        nextCharIsEscaped = !nextCharIsEscaped;
                        break;
                    case TYPEID_CONFIG_SEPARATOR:
                    case LOGIC_SEPARATOR:
                    case OPEN_PAREN:
                    case CLOSE_PAREN:
                        if (!nextCharIsEscaped) {
                            done = true;
                            break;
                        }
                        result.append(c);
                        nextCharIsEscaped = false;
                        break;
                    default:
                        result.append(c);
                        nextCharIsEscaped = false;
                        break;
                }
                if (!done) {
                    idx++;
                }
            }
            return result.toString();
        }

        private void skipWhitespaces() {
            while (idx < config.length() && Character.isWhitespace(config.charAt(idx))) {
                idx++;
            }
        }

        private void checkEndOfExpression() throws ParseException {
            skipWhitespaces();
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

}
