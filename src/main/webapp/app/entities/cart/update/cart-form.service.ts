import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ICart, NewCart } from '../cart.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICart for edit and NewCartFormGroupInput for create.
 */
type CartFormGroupInput = ICart | PartialWithRequiredKeyOf<NewCart>;

type CartFormDefaults = Pick<NewCart, 'id'>;

type CartFormGroupContent = {
  id: FormControl<ICart['id'] | NewCart['id']>;
  user: FormControl<ICart['user']>;
};

export type CartFormGroup = FormGroup<CartFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CartFormService {
  createCartFormGroup(cart: CartFormGroupInput = { id: null }): CartFormGroup {
    const cartRawValue = {
      ...this.getFormDefaults(),
      ...cart,
    };
    return new FormGroup<CartFormGroupContent>({
      id: new FormControl(
        { value: cartRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      user: new FormControl(cartRawValue.user),
    });
  }

  getCart(form: CartFormGroup): ICart | NewCart {
    return form.getRawValue() as ICart | NewCart;
  }

  resetForm(form: CartFormGroup, cart: CartFormGroupInput): void {
    const cartRawValue = { ...this.getFormDefaults(), ...cart };
    form.reset(
      {
        ...cartRawValue,
        id: { value: cartRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): CartFormDefaults {
    return {
      id: null,
    };
  }
}
