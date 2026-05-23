import { IProduct } from 'app/entities/product/product.model';
import { ICart } from 'app/entities/cart/cart.model';

export interface ICartItem {
  id: number;
  quantity?: number | null;
  price?: number | null;
  product?: Pick<IProduct, 'id'> | null;
  cart?: Pick<ICart, 'id'> | null;
}

export type NewCartItem = Omit<ICartItem, 'id'> & { id: null };
