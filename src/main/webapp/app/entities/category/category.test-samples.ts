import dayjs from 'dayjs/esm';

import { ICategory, NewCategory } from './category.model';

export const sampleWithRequiredData: ICategory = {
  id: 8109,
  name: 'midst croon cautiously',
};

export const sampleWithPartialData: ICategory = {
  id: 31319,
  name: 'um swiftly fast',
  description: 'than sting',
  createdBy: 'favorite inject fooey',
  createdDate: dayjs('2026-04-02T12:22'),
};

export const sampleWithFullData: ICategory = {
  id: 28780,
  name: 'ouch',
  description: 'self-confidence',
  createdBy: 'because cheap',
  createdDate: dayjs('2026-04-02T06:04'),
  lastModifiedBy: 'purple whether surge',
  lastModifiedDate: dayjs('2026-04-03T01:52'),
};

export const sampleWithNewData: NewCategory = {
  name: 'where pfft regularly',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
