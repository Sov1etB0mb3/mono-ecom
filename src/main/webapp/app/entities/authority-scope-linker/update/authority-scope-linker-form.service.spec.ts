import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../authority-scope-linker.test-samples';

import { AuthorityScopeLinkerFormService } from './authority-scope-linker-form.service';

describe('AuthorityScopeLinker Form Service', () => {
  let service: AuthorityScopeLinkerFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthorityScopeLinkerFormService);
  });

  describe('Service methods', () => {
    describe('createAuthorityScopeLinkerFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createAuthorityScopeLinkerFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            authority: expect.any(Object),
            scope: expect.any(Object),
          }),
        );
      });

      it('passing IAuthorityScopeLinker should create a new form with FormGroup', () => {
        const formGroup = service.createAuthorityScopeLinkerFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            authority: expect.any(Object),
            scope: expect.any(Object),
          }),
        );
      });
    });

    describe('getAuthorityScopeLinker', () => {
      it('should return NewAuthorityScopeLinker for default AuthorityScopeLinker initial value', () => {
        const formGroup = service.createAuthorityScopeLinkerFormGroup(sampleWithNewData);

        const authorityScopeLinker = service.getAuthorityScopeLinker(formGroup) as any;

        expect(authorityScopeLinker).toMatchObject(sampleWithNewData);
      });

      it('should return NewAuthorityScopeLinker for empty AuthorityScopeLinker initial value', () => {
        const formGroup = service.createAuthorityScopeLinkerFormGroup();

        const authorityScopeLinker = service.getAuthorityScopeLinker(formGroup) as any;

        expect(authorityScopeLinker).toMatchObject({});
      });

      it('should return IAuthorityScopeLinker', () => {
        const formGroup = service.createAuthorityScopeLinkerFormGroup(sampleWithRequiredData);

        const authorityScopeLinker = service.getAuthorityScopeLinker(formGroup) as any;

        expect(authorityScopeLinker).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IAuthorityScopeLinker should not enable id FormControl', () => {
        const formGroup = service.createAuthorityScopeLinkerFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewAuthorityScopeLinker should disable id FormControl', () => {
        const formGroup = service.createAuthorityScopeLinkerFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
