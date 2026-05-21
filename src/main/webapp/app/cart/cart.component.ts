import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import SharedModule from 'app/shared/shared.module';
import { CartService, CartResponseType } from './cart.service';
import { ICartResponse, ICustomizedCartItem } from './cart.model';

@Component({
  selector: 'jhi-cart',
  templateUrl: './cart.component.html',
  imports: [RouterModule, SharedModule],
})
export class CartComponent implements OnInit {
  cart = signal<ICartResponse | null>(null);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  private readonly cartService = inject(CartService);

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.cartService
      .getCart()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (res: CartResponseType) => this.cart.set(res.body),
        error: () => this.errorMessage.set('Failed to load cart'),
      });
  }

  increaseQuantity(item: ICustomizedCartItem): void {
    if (!item.id) return;
    this.updateQuantity(item.id, item.quantity + 1);
  }

  decreaseQuantity(item: ICustomizedCartItem): void {
    if (!item.id) return;
    const newQty = item.quantity - 1;
    if (newQty < 1) {
      this.removeItem(item);
      return;
    }
    this.updateQuantity(item.id, newQty);
  }

  removeItem(item: ICustomizedCartItem): void {
    if (!item.id) return;
    this.isLoading.set(true);
    this.cartService
      .removeCartItem(item.id)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (res: CartResponseType) => this.cart.set(res.body),
        error: () => this.errorMessage.set('Failed to remove item'),
      });
  }

  private updateQuantity(id: number, quantity: number): void {
    this.isLoading.set(true);
    this.cartService
      .updateItemQuantity(id, quantity)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (res: CartResponseType) => this.cart.set(res.body),
        error: () => this.errorMessage.set('Failed to update quantity'),
      });
  }
}
