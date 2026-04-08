import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IAuthorityScopeLinker } from '../authority-scope-linker.model';
import { AuthorityScopeLinkerService } from '../service/authority-scope-linker.service';

@Component({
  templateUrl: './authority-scope-linker-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class AuthorityScopeLinkerDeleteDialogComponent {
  authorityScopeLinker?: IAuthorityScopeLinker;

  protected authorityScopeLinkerService = inject(AuthorityScopeLinkerService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.authorityScopeLinkerService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
