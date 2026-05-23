import { ICartItem, NewCartItem } from './cart-item.model';

export const sampleWithRequiredData: ICartItem = {
  id: 12545,
  quantity: 12390,
};

export const sampleWithPartialData: ICartItem = {
  id: 14799,
  quantity: 11067,
  price: 31912.89,
};

export const sampleWithFullData: ICartItem = {
  id: 9768,
  quantity: 32077,
  price: 22346.9,
};

export const sampleWithNewData: NewCartItem = {
  quantity: 917,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
