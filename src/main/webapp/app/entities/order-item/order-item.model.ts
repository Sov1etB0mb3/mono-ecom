import { IProduct } from 'app/entities/product/product.model';
import { IOrder } from 'app/entities/order/order.model';

export interface IOrderItem {
  id: number;
  quantity?: number | null;
  priceAtPurchase?: number | null;
  product?: Pick<IProduct, 'id'> | null;
  order?: Pick<IOrder, 'id'> | null;
}

export type NewOrderItem = Omit<IOrderItem, 'id'> & { id: null };
