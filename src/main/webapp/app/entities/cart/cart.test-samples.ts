import { ICart, NewCart } from './cart.model';

export const sampleWithRequiredData: ICart = {
  id: 20875,
};

export const sampleWithPartialData: ICart = {
  id: 17644,
};

export const sampleWithFullData: ICart = {
  id: 4081,
};

export const sampleWithNewData: NewCart = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
