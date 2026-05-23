import dayjs from 'dayjs/esm';
import { ICategory } from 'app/entities/category/category.model';

export interface IProduct {
  id: number;
  name?: string | null;
  quantity?: number | null;
  price?: number | null;
  createdBy?: string | null;
  createdDate?: dayjs.Dayjs | null;
  lastModifiedBy?: string | null;
  lastModifiedDate?: dayjs.Dayjs | null;
  category?: Pick<ICategory, 'id' | 'name'> | null;
}

export type NewProduct = Omit<IProduct, 'id'> & { id: null };
