package net.risesoft.support;

public class OrderRequest {

    private OrderProp orderProp = OrderProp.FILE_NAME;
    private boolean orderAsc = true;

    public OrderRequest() {}

    public OrderRequest(OrderProp orderProp, boolean orderAsc) {
        this.orderProp = orderProp;
        this.orderAsc = orderAsc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderRequest that = (OrderRequest)o;

        if (orderAsc != that.orderAsc) {
            return false;
        }
        return orderProp == that.orderProp;
    }

    public OrderProp getOrderProp() {
        return orderProp;
    }

    public void setOrderProp(OrderProp orderProp) {
        this.orderProp = orderProp;
    }

    @Override
    public int hashCode() {
        int result = orderProp != null ? orderProp.hashCode() : 0;
        result = 31 * result + (orderAsc ? 1 : 0);
        return result;
    }

    public boolean isOrderAsc() {
        return orderAsc;
    }

    public void setOrderAsc(boolean orderAsc) {
        this.orderAsc = orderAsc;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OrderRequest{");
        sb.append("orderProp=").append(orderProp);
        sb.append(", orderAsc=").append(orderAsc);
        sb.append('}');
        return sb.toString();
    }
}
