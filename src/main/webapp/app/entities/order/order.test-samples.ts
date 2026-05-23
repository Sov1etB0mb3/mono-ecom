import dayjs from 'dayjs/esm';

import { IOrder, NewOrder } from './order.model';

export const sampleWithRequiredData: IOrder = {
  id: 26110,
};

export const sampleWithPartialData: IOrder = {
  id: 20558,
  total: 29949.07,
  createdBy: 'idle glossy',
  lastModifiedBy: 'untrue punctually',
  lastModifiedDate: dayjs('2026-04-07T09:44'),
};

export const sampleWithFullData: IOrder = {
  id: 27813,
  status: 'SHIPPED',
  subTotal: 7491.96,
  total: 3767.98,
  createdBy: 'exactly indeed',
  createdDate: dayjs('2026-04-07T15:57'),
  lastModifiedBy: 'whoa kettledrum conceal',
  lastModifiedDate: dayjs('2026-04-07T19:10'),
};

export const sampleWithNewData: NewOrder = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
