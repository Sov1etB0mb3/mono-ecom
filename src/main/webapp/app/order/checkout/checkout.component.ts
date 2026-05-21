import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { OrderService } from '../order.service';

@Component({
  selector: 'jhi-checkout',
  templateUrl: './checkout.component.html',
  imports: [SharedModule],
})
export class CheckoutComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.orderService.checkout().subscribe({
      next: res => {
        window.location.href = res.sessionUrl;
      },
      error: () => {
        this.router.navigate(['/payment/cancel']);
      },
    });
  }
}
