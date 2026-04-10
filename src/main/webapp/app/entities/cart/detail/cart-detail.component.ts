import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { ICart } from '../cart.model';

@Component({
  selector: 'jhi-cart-detail',
  templateUrl: './cart-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class CartDetailComponent {
  cart = input<ICart | null>(null);

  previousState(): void {
    window.history.back();
  }
}
