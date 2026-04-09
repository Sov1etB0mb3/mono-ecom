import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IOrder } from '../order.model';

@Component({
  selector: 'jhi-order-detail',
  templateUrl: './order-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class OrderDetailComponent {
  order = input<IOrder | null>(null);

  previousState(): void {
    window.history.back();
  }
}
