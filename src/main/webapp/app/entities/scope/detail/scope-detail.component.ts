import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IScope } from '../scope.model';

@Component({
  selector: 'jhi-scope-detail',
  templateUrl: './scope-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class ScopeDetailComponent {
  scope = input<IScope | null>(null);

  previousState(): void {
    window.history.back();
  }
}
