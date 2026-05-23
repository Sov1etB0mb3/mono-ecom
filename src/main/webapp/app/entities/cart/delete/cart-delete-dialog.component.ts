import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ICart } from '../cart.model';
import { CartService } from '../service/cart.service';

@Component({
  templateUrl: './cart-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class CartDeleteDialogComponent {
  cart?: ICart;

  protected cartService = inject(CartService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.cartService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
