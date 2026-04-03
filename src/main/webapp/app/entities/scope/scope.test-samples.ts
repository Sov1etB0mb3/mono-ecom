import { IScope, NewScope } from './scope.model';

export const sampleWithRequiredData: IScope = {
  id: 24888,
  name: 'furthermore ferociously which',
};

export const sampleWithPartialData: IScope = {
  id: 27447,
  name: 'valiantly',
  description: 'so honored potable',
};

export const sampleWithFullData: IScope = {
  id: 32548,
  name: 'rosemary defiantly focalise',
  description: 'idealistic by',
};

export const sampleWithNewData: NewScope = {
  name: 'descendant boo unkempt',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
