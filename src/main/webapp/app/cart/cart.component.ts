import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { finalize } from 'rxjs';

import SharedModule from 'app/shared/shared.module';
import { CartService } from './cart.service';
import { ICustomizedCartItem } from './cart.model';

@Component({
  selector: 'jhi-cart',
  templateUrl: './cart.component.html',
  imports: [RouterModule, SharedModule],
})
export class CartComponent implements OnInit {
  cart = inject(CartService).cartSignal;
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  private readonly cartService = inject(CartService);

  ngOnInit(): void {
    if (!this.cart()) {
      this.loadCart();
    }
  }

  loadCart(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.cartService
      .getCart()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({ error: () => this.errorMessage.set('Failed to load cart') });
  }

  increaseQuantity(item: ICustomizedCartItem): void {
    if (!item.id) return;
    this.cartService.updateItemQuantity(item.id, item.quantity + 1).subscribe();
  }

  decreaseQuantity(item: ICustomizedCartItem): void {
    if (!item.id) return;
    if (item.quantity <= 1) {
      this.cartService.removeCartItem(item.id).subscribe();
      return;
    }
    this.cartService.updateItemQuantity(item.id, item.quantity - 1).subscribe();
  }

  removeItem(item: ICustomizedCartItem): void {
    if (!item.id) return;
    this.cartService.removeCartItem(item.id).subscribe();
  }
}
