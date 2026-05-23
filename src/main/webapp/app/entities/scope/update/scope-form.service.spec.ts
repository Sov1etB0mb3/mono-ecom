import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../scope.test-samples';

import { ScopeFormService } from './scope-form.service';

describe('Scope Form Service', () => {
  let service: ScopeFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ScopeFormService);
  });

  describe('Service methods', () => {
    describe('createScopeFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createScopeFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            description: expect.any(Object),
          }),
        );
      });

      it('passing IScope should create a new form with FormGroup', () => {
        const formGroup = service.createScopeFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            description: expect.any(Object),
          }),
        );
      });
    });

    describe('getScope', () => {
      it('should return NewScope for default Scope initial value', () => {
        const formGroup = service.createScopeFormGroup(sampleWithNewData);

        const scope = service.getScope(formGroup) as any;

        expect(scope).toMatchObject(sampleWithNewData);
      });

      it('should return NewScope for empty Scope initial value', () => {
        const formGroup = service.createScopeFormGroup();

        const scope = service.getScope(formGroup) as any;

        expect(scope).toMatchObject({});
      });

      it('should return IScope', () => {
        const formGroup = service.createScopeFormGroup(sampleWithRequiredData);

        const scope = service.getScope(formGroup) as any;

        expect(scope).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IScope should not enable id FormControl', () => {
        const formGroup = service.createScopeFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewScope should disable id FormControl', () => {
        const formGroup = service.createScopeFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
