import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IAuthorityScopeLinker } from '../authority-scope-linker.model';

@Component({
  selector: 'jhi-authority-scope-linker-detail',
  templateUrl: './authority-scope-linker-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class AuthorityScopeLinkerDetailComponent {
  authorityScopeLinker = input<IAuthorityScopeLinker | null>(null);

  previousState(): void {
    window.history.back();
  }
}
