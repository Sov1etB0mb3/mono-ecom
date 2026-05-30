import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { ICartResponse, IAddToCartRequest } from './cart.model';

export type CartResponseType = HttpResponse<ICartResponse>;

@Injectable({ providedIn: 'root' })
export class CartService {
  readonly cartSignal = signal<ICartResponse | null>(null);

  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/cart');

  getCart(): Observable<CartResponseType> {
    return this.http
      .get<ICartResponse>(`${this.resourceUrl}/items`, { observe: 'response' })
      .pipe(tap(res => this.cartSignal.set(res.body)));
  }

  addItemToCart(item: IAddToCartRequest): Observable<CartResponseType> {
    return this.http.post<ICartResponse>(this.resourceUrl, item, { observe: 'response' }).pipe(tap(res => this.cartSignal.set(res.body)));
  }

  updateItemQuantity(id: number, quantity: number): Observable<CartResponseType> {
    return this.http
      .patch<ICartResponse>(`${this.resourceUrl}/items`, { id, quantity }, { observe: 'response' })
      .pipe(tap(res => this.cartSignal.set(res.body)));
  }

  removeCartItem(id: number): Observable<CartResponseType> {
    return this.http
      .delete<ICartResponse>(`${this.resourceUrl}/items/${id}`, { observe: 'response' })
      .pipe(tap(res => this.cartSignal.set(res.body)));
  }

  getCartItemCount(): number {
    const cart = this.cartSignal();
    if (!cart?.cartItems) {
      return 0;
    }
    // return cart.cartItems.reduce((sum, item) => sum + item.quantity, 0);
    return cart.cartItems.length;
  }
}
