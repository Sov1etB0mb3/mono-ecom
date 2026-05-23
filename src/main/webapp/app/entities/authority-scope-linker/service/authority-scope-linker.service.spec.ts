import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IAuthorityScopeLinker } from '../authority-scope-linker.model';
import {
  sampleWithFullData,
  sampleWithNewData,
  sampleWithPartialData,
  sampleWithRequiredData,
} from '../authority-scope-linker.test-samples';

import { AuthorityScopeLinkerService } from './authority-scope-linker.service';

const requireRestSample: IAuthorityScopeLinker = {
  ...sampleWithRequiredData,
};

describe('AuthorityScopeLinker Service', () => {
  let service: AuthorityScopeLinkerService;
  let httpMock: HttpTestingController;
  let expectedResult: IAuthorityScopeLinker | IAuthorityScopeLinker[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(AuthorityScopeLinkerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a AuthorityScopeLinker', () => {
      const authorityScopeLinker = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(authorityScopeLinker).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a AuthorityScopeLinker', () => {
      const authorityScopeLinker = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(authorityScopeLinker).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a AuthorityScopeLinker', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of AuthorityScopeLinker', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a AuthorityScopeLinker', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a AuthorityScopeLinker', () => {
      const queryObject: any = {
        page: 0,
        size: 20,
        query: '',
        sort: [],
      };
      service.search(queryObject).subscribe(() => expectedResult);

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
      expect(expectedResult).toBe(null);
    });

    describe('addAuthorityScopeLinkerToCollectionIfMissing', () => {
      it('should add a AuthorityScopeLinker to an empty array', () => {
        const authorityScopeLinker: IAuthorityScopeLinker = sampleWithRequiredData;
        expectedResult = service.addAuthorityScopeLinkerToCollectionIfMissing([], authorityScopeLinker);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(authorityScopeLinker);
      });

      it('should not add a AuthorityScopeLinker to an array that contains it', () => {
        const authorityScopeLinker: IAuthorityScopeLinker = sampleWithRequiredData;
        const authorityScopeLinkerCollection: IAuthorityScopeLinker[] = [
          {
            ...authorityScopeLinker,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addAuthorityScopeLinkerToCollectionIfMissing(authorityScopeLinkerCollection, authorityScopeLinker);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a AuthorityScopeLinker to an array that doesn't contain it", () => {
        const authorityScopeLinker: IAuthorityScopeLinker = sampleWithRequiredData;
        const authorityScopeLinkerCollection: IAuthorityScopeLinker[] = [sampleWithPartialData];
        expectedResult = service.addAuthorityScopeLinkerToCollectionIfMissing(authorityScopeLinkerCollection, authorityScopeLinker);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(authorityScopeLinker);
      });

      it('should add only unique AuthorityScopeLinker to an array', () => {
        const authorityScopeLinkerArray: IAuthorityScopeLinker[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const authorityScopeLinkerCollection: IAuthorityScopeLinker[] = [sampleWithRequiredData];
        expectedResult = service.addAuthorityScopeLinkerToCollectionIfMissing(authorityScopeLinkerCollection, ...authorityScopeLinkerArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const authorityScopeLinker: IAuthorityScopeLinker = sampleWithRequiredData;
        const authorityScopeLinker2: IAuthorityScopeLinker = sampleWithPartialData;
        expectedResult = service.addAuthorityScopeLinkerToCollectionIfMissing([], authorityScopeLinker, authorityScopeLinker2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(authorityScopeLinker);
        expect(expectedResult).toContain(authorityScopeLinker2);
      });

      it('should accept null and undefined values', () => {
        const authorityScopeLinker: IAuthorityScopeLinker = sampleWithRequiredData;
        expectedResult = service.addAuthorityScopeLinkerToCollectionIfMissing([], null, authorityScopeLinker, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(authorityScopeLinker);
      });

      it('should return initial array if no AuthorityScopeLinker is added', () => {
        const authorityScopeLinkerCollection: IAuthorityScopeLinker[] = [sampleWithRequiredData];
        expectedResult = service.addAuthorityScopeLinkerToCollectionIfMissing(authorityScopeLinkerCollection, undefined, null);
        expect(expectedResult).toEqual(authorityScopeLinkerCollection);
      });
    });

    describe('compareAuthorityScopeLinker', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareAuthorityScopeLinker(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 7201 };
        const entity2 = null;

        const compareResult1 = service.compareAuthorityScopeLinker(entity1, entity2);
        const compareResult2 = service.compareAuthorityScopeLinker(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 7201 };
        const entity2 = { id: 4144 };

        const compareResult1 = service.compareAuthorityScopeLinker(entity1, entity2);
        const compareResult2 = service.compareAuthorityScopeLinker(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 7201 };
        const entity2 = { id: 7201 };

        const compareResult1 = service.compareAuthorityScopeLinker(entity1, entity2);
        const compareResult2 = service.compareAuthorityScopeLinker(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
