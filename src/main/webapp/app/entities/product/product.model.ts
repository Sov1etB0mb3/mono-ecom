import { ICategory } from 'app/entities/category/category.model';

export interface IProduct {
  id: number;
  category?: Pick<ICategory, 'id'> | null;
}

export type NewProduct = Omit<IProduct, 'id'> & { id: null };
