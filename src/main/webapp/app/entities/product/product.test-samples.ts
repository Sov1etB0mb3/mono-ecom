import dayjs from 'dayjs/esm';

import { IProduct, NewProduct } from './product.model';

export const sampleWithRequiredData: IProduct = {
  id: 11737,
  name: 'hm sleepily',
  quantity: 17511,
  price: 1843.12,
};

export const sampleWithPartialData: IProduct = {
  id: 31533,
  name: 'almost tempting fervently',
  quantity: 15504,
  price: 28504.85,
  createdDate: dayjs('2026-04-02T09:44'),
};

export const sampleWithFullData: IProduct = {
  id: 4403,
  name: 'cafe',
  quantity: 21233,
  price: 2026.75,
  createdBy: 'hence yuck',
  createdDate: dayjs('2026-04-03T04:09'),
  lastModifiedBy: 'until solemnly',
  lastModifiedDate: dayjs('2026-04-02T09:22'),
};

export const sampleWithNewData: NewProduct = {
  name: 'anti inject why',
  quantity: 26563,
  price: 4718.75,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
