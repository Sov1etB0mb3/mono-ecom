import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IScope } from '../scope.model';
import { ScopeService } from '../service/scope.service';
import { ScopeFormGroup, ScopeFormService } from './scope-form.service';

@Component({
  selector: 'jhi-scope-update',
  templateUrl: './scope-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ScopeUpdateComponent implements OnInit {
  isSaving = false;
  scope: IScope | null = null;

  protected scopeService = inject(ScopeService);
  protected scopeFormService = inject(ScopeFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ScopeFormGroup = this.scopeFormService.createScopeFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ scope }) => {
      this.scope = scope;
      if (scope) {
        this.updateForm(scope);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const scope = this.scopeFormService.getScope(this.editForm);
    if (scope.id !== null) {
      this.subscribeToSaveResponse(this.scopeService.update(scope));
    } else {
      this.subscribeToSaveResponse(this.scopeService.create(scope));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IScope>>): void {
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

  protected updateForm(scope: IScope): void {
    this.scope = scope;
    this.scopeFormService.resetForm(this.editForm, scope);
  }
}
