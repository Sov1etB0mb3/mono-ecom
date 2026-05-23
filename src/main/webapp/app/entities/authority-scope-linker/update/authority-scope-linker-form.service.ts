import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IAuthorityScopeLinker, NewAuthorityScopeLinker } from '../authority-scope-linker.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAuthorityScopeLinker for edit and NewAuthorityScopeLinkerFormGroupInput for create.
 */
type AuthorityScopeLinkerFormGroupInput = IAuthorityScopeLinker | PartialWithRequiredKeyOf<NewAuthorityScopeLinker>;

type AuthorityScopeLinkerFormDefaults = Pick<NewAuthorityScopeLinker, 'id'>;

type AuthorityScopeLinkerFormGroupContent = {
  id: FormControl<IAuthorityScopeLinker['id'] | NewAuthorityScopeLinker['id']>;
  authority: FormControl<IAuthorityScopeLinker['authority']>;
  scope: FormControl<IAuthorityScopeLinker['scope']>;
};

export type AuthorityScopeLinkerFormGroup = FormGroup<AuthorityScopeLinkerFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AuthorityScopeLinkerFormService {
  createAuthorityScopeLinkerFormGroup(
    authorityScopeLinker: AuthorityScopeLinkerFormGroupInput = { id: null },
  ): AuthorityScopeLinkerFormGroup {
    const authorityScopeLinkerRawValue = {
      ...this.getFormDefaults(),
      ...authorityScopeLinker,
    };
    return new FormGroup<AuthorityScopeLinkerFormGroupContent>({
      id: new FormControl(
        { value: authorityScopeLinkerRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      authority: new FormControl(authorityScopeLinkerRawValue.authority),
      scope: new FormControl(authorityScopeLinkerRawValue.scope),
    });
  }

  getAuthorityScopeLinker(form: AuthorityScopeLinkerFormGroup): IAuthorityScopeLinker | NewAuthorityScopeLinker {
    return form.getRawValue() as IAuthorityScopeLinker | NewAuthorityScopeLinker;
  }

  resetForm(form: AuthorityScopeLinkerFormGroup, authorityScopeLinker: AuthorityScopeLinkerFormGroupInput): void {
    const authorityScopeLinkerRawValue = { ...this.getFormDefaults(), ...authorityScopeLinker };
    form.reset(
      {
        ...authorityScopeLinkerRawValue,
        id: { value: authorityScopeLinkerRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): AuthorityScopeLinkerFormDefaults {
    return {
      id: null,
    };
  }
}
