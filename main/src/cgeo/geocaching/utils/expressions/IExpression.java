package cgeo.geocaching.utils.expressions;

import java.util.List;

public interface IExpression<T extends IExpression> {

    enum ExpressionType { SIMPLE, OPERATOR_UNARY, OPERATOR_BINARY }

    String getId();

    void setConfig(String values);

    String getConfig();

    void addChildren(T left, T right);

    T getChildLeft();

    T getChildRight();

    default ExpressionType getType() {
        return ExpressionType.SIMPLE;
    }

    default int getOperatorBinaryPriority() {
        return 10;
    }

    default boolean getOperatorBinaryOrderSensitive() {
        return true;
    }

}
