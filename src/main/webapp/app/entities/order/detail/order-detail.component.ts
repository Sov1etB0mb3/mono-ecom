import { Component, inject, input, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IOrder } from '../order.model';
import { IOrderItem } from '../../order-item/order-item.model';

@Component({
  selector: 'jhi-order-detail',
  templateUrl: './order-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class OrderDetailComponent implements OnInit {
  order = input<IOrder | null>(null);
  orderItems: IOrderItem[] = [];

  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  ngOnInit(): void {
    const orderId = this.order()?.id;
    if (orderId) {
      const url = this.applicationConfigService.getEndpointFor(`api/orders/${orderId}/items`);
      this.http.get<IOrderItem[]>(url).subscribe(items => {
        this.orderItems = items;
      });
    }
  }

  previousState(): void {
    window.history.back();
  }
}
