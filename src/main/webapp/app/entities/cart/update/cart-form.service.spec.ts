import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../cart.test-samples';

import { CartFormService } from './cart-form.service';

describe('Cart Form Service', () => {
  let service: CartFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CartFormService);
  });

  describe('Service methods', () => {
    describe('createCartFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createCartFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });

      it('passing ICart should create a new form with FormGroup', () => {
        const formGroup = service.createCartFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });
    });

    describe('getCart', () => {
      it('should return NewCart for default Cart initial value', () => {
        const formGroup = service.createCartFormGroup(sampleWithNewData);

        const cart = service.getCart(formGroup) as any;

        expect(cart).toMatchObject(sampleWithNewData);
      });

      it('should return NewCart for empty Cart initial value', () => {
        const formGroup = service.createCartFormGroup();

        const cart = service.getCart(formGroup) as any;

        expect(cart).toMatchObject({});
      });

      it('should return ICart', () => {
        const formGroup = service.createCartFormGroup(sampleWithRequiredData);

        const cart = service.getCart(formGroup) as any;

        expect(cart).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ICart should not enable id FormControl', () => {
        const formGroup = service.createCartFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewCart should disable id FormControl', () => {
        const formGroup = service.createCartFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
