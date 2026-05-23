import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { ICheckoutResponse, IOrder } from './order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/orders');

  checkout(): Observable<ICheckoutResponse> {
    return this.http.post<ICheckoutResponse>(`${this.resourceUrl}/checkout`, {});
  }

  getMyOrders(): Observable<IOrder[]> {
    return this.http.get<IOrder[]>(`${this.resourceUrl}/my-orders`);
  }

  payOrder(orderId: number): Observable<ICheckoutResponse> {
    return this.http.post<ICheckoutResponse>(`${this.resourceUrl}/pay/${orderId}`, {});
  }

  cancelOrder(orderId: number): Observable<void> {
    return this.http.post<void>(`${this.resourceUrl}/cancel/${orderId}`, {});
  }
}
