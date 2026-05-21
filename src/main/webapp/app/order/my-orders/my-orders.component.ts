import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { OrderService } from '../order.service';
import { IOrder } from '../order.model';

@Component({
  selector: 'jhi-my-orders',
  templateUrl: './my-orders.component.html',
  imports: [SharedModule, RouterModule],
})
export default class MyOrdersComponent implements OnInit {
  orders = signal<IOrder[]>([]);
  isLoading = signal(false);

  private readonly orderService = inject(OrderService);

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading.set(true);
    this.orderService.getMyOrders().subscribe(orders => {
      this.orders.set(orders);
      this.isLoading.set(false);
    });
  }
}
