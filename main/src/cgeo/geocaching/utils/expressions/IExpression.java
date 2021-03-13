package cgeo.geocaching.utils.expressions;

import java.util.List;

public interface IExpression<T extends IExpression> {

    String getTypeId();

    void setConfig(String value);

    String getConfig();

    void addChild(T child);

    List<T> getChildren();

}
