export interface ICustomizedProduct {
  id: number;
  name?: string | null;
  quantity?: number | null;
  price?: number | null;
  category?: { id: number; name?: string | null } | null;
}

export interface ICustomizedCartItem {
  id?: number | null;
  quantity: number;
  price?: number | null;
  product?: ICustomizedProduct | null;
  availableStock?: number | null;
  isAvailable?: boolean;
}

export interface ICartResponse {
  id?: number | null;
  user?: { id: number; login?: string | null } | null;
  cartItems?: ICustomizedCartItem[] | null;
  totalPrice?: number | null;
}

export interface IAddToCartRequest {
  product: { id: number };
  quantity: number;
}
