import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';

@Component({
  selector: 'jhi-payment-cancel',
  templateUrl: './cancel.component.html',
  imports: [RouterModule, SharedModule],
})
export class PaymentCancelComponent {}
