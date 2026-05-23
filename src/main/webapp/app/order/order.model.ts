export interface ICheckoutResponse {
  sessionUrl: string;
  orderId: number;
}

export interface IOrder {
  id: number;
  status?: string;
  subTotal?: number;
  total?: number;
  createdDate?: string;
}
