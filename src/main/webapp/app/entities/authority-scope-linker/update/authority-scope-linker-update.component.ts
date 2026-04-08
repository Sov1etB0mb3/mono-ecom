import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IAuthority } from 'app/entities/admin/authority/authority.model';
import { AuthorityService } from 'app/entities/admin/authority/service/authority.service';
import { IScope } from 'app/entities/scope/scope.model';
import { ScopeService } from 'app/entities/scope/service/scope.service';
import { AuthorityScopeLinkerService } from '../service/authority-scope-linker.service';
import { IAuthorityScopeLinker } from '../authority-scope-linker.model';
import { AuthorityScopeLinkerFormGroup, AuthorityScopeLinkerFormService } from './authority-scope-linker-form.service';

@Component({
  selector: 'jhi-authority-scope-linker-update',
  templateUrl: './authority-scope-linker-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class AuthorityScopeLinkerUpdateComponent implements OnInit {
  isSaving = false;
  authorityScopeLinker: IAuthorityScopeLinker | null = null;

  authoritiesSharedCollection: IAuthority[] = [];
  scopesSharedCollection: IScope[] = [];

  protected authorityScopeLinkerService = inject(AuthorityScopeLinkerService);
  protected authorityScopeLinkerFormService = inject(AuthorityScopeLinkerFormService);
  protected authorityService = inject(AuthorityService);
  protected scopeService = inject(ScopeService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AuthorityScopeLinkerFormGroup = this.authorityScopeLinkerFormService.createAuthorityScopeLinkerFormGroup();

  compareAuthority = (o1: IAuthority | null, o2: IAuthority | null): boolean => this.authorityService.compareAuthority(o1, o2);

  compareScope = (o1: IScope | null, o2: IScope | null): boolean => this.scopeService.compareScope(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ authorityScopeLinker }) => {
      this.authorityScopeLinker = authorityScopeLinker;
      if (authorityScopeLinker) {
        this.updateForm(authorityScopeLinker);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const authorityScopeLinker = this.authorityScopeLinkerFormService.getAuthorityScopeLinker(this.editForm);
    if (authorityScopeLinker.id !== null) {
      this.subscribeToSaveResponse(this.authorityScopeLinkerService.update(authorityScopeLinker));
    } else {
      this.subscribeToSaveResponse(this.authorityScopeLinkerService.create(authorityScopeLinker));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAuthorityScopeLinker>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(authorityScopeLinker: IAuthorityScopeLinker): void {
    this.authorityScopeLinker = authorityScopeLinker;
    this.authorityScopeLinkerFormService.resetForm(this.editForm, authorityScopeLinker);

    this.authoritiesSharedCollection = this.authorityService.addAuthorityToCollectionIfMissing<IAuthority>(
      this.authoritiesSharedCollection,
      authorityScopeLinker.authority,
    );
    this.scopesSharedCollection = this.scopeService.addScopeToCollectionIfMissing<IScope>(
      this.scopesSharedCollection,
      authorityScopeLinker.scope,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.authorityService
      .query()
      .pipe(map((res: HttpResponse<IAuthority[]>) => res.body ?? []))
      .pipe(
        map((authorities: IAuthority[]) =>
          this.authorityService.addAuthorityToCollectionIfMissing<IAuthority>(authorities, this.authorityScopeLinker?.authority),
        ),
      )
      .subscribe((authorities: IAuthority[]) => (this.authoritiesSharedCollection = authorities));

    this.scopeService
      .query()
      .pipe(map((res: HttpResponse<IScope[]>) => res.body ?? []))
      .pipe(map((scopes: IScope[]) => this.scopeService.addScopeToCollectionIfMissing<IScope>(scopes, this.authorityScopeLinker?.scope)))
      .subscribe((scopes: IScope[]) => (this.scopesSharedCollection = scopes));
  }
}
