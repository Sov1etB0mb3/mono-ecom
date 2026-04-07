import { IUser } from 'app/entities/user/user.model';
import { OrderStatus } from 'app/entities/enumerations/order-status.model';

export interface IOrder {
  id: number;
  status?: keyof typeof OrderStatus | null;
  subTotal?: number | null;
  total?: number | null;
  user?: Pick<IUser, 'id'> | null;
}

export type NewOrder = Omit<IOrder, 'id'> & { id: null };
