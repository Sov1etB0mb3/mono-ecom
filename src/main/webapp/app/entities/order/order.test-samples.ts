import { IOrder, NewOrder } from './order.model';

export const sampleWithRequiredData: IOrder = {
  id: 26110,
};

export const sampleWithPartialData: IOrder = {
  id: 8286,
  total: 21314.13,
};

export const sampleWithFullData: IOrder = {
  id: 27813,
  status: 'SHIPPED',
  subTotal: 7491.96,
  total: 3767.98,
};

export const sampleWithNewData: NewOrder = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
