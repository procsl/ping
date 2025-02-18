package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.Order;
import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import cn.procsl.ping.boot.jpa.support.query.ast.DotExpression;
import cn.procsl.ping.boot.jpa.support.query.ast.Expression;
import cn.procsl.ping.boot.jpa.support.query.ast.OrderExpression;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@RequiredArgsConstructor
class OrderFieldExpression implements OrderExpression {

    final Field field;
    final Projection projection;
    final ReferenceBy ref;
    final Order order;
    final int index;

    public Expression getExpression() {
        String name = this.field.getName();
        if (ref == null) {
            return new DotExpression(projection.alias(), name);
        }
        if (ref.target() == null || ref.target().isEmpty()) {
            return new DotExpression(ref.ref(), name);
        }
        return new DotExpression(ref.ref(), ref.target());
    }

    @Override
    public String toExpString() {
        return this.getExpression().toExpString() + " " + this.order.sort().toString();
    }

    @Override
    public int sort() {
        if (this.order.order() == Integer.MAX_VALUE) {
            return this.index;
        }
        return this.order.order();
    }

}
