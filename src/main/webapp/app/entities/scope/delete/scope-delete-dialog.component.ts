import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IScope } from '../scope.model';
import { ScopeService } from '../service/scope.service';

@Component({
  templateUrl: './scope-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class ScopeDeleteDialogComponent {
  scope?: IScope;

  protected scopeService = inject(ScopeService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.scopeService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
