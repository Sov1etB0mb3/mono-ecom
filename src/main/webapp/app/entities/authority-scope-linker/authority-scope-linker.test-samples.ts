import { IAuthorityScopeLinker, NewAuthorityScopeLinker } from './authority-scope-linker.model';

export const sampleWithRequiredData: IAuthorityScopeLinker = {
  id: 21383,
};

export const sampleWithPartialData: IAuthorityScopeLinker = {
  id: 18857,
};

export const sampleWithFullData: IAuthorityScopeLinker = {
  id: 731,
};

export const sampleWithNewData: NewAuthorityScopeLinker = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
