import { IUser } from 'app/entities/user/user.model';

export interface ICart {
  id: number;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewCart = Omit<ICart, 'id'> & { id: null };
