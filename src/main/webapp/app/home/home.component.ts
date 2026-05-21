import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { ProductService } from 'app/entities/product/service/product.service';
import { CartService } from 'app/cart/cart.service';
import { IProduct } from 'app/entities/product/product.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [SharedModule, RouterModule],
})
export default class HomeComponent implements OnInit {
  account = signal<Account | null>(null);
  products = signal<IProduct[]>([]);
  isLoading = signal(false);

  private readonly accountService = inject(AccountService);
  private readonly loginService = inject(LoginService);
  private readonly productService = inject(ProductService);
  private readonly cartService = inject(CartService);

  ngOnInit(): void {
    this.accountService.identity().subscribe(account => {
      this.account.set(account);
      this.loadProducts();
    });
  }

  loadProducts(): void {
    this.isLoading.set(true);
    this.productService
      .query()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe(res => this.products.set(res.body ?? []));
  }

  addToCart(product: IProduct): void {
    this.cartService.addItemToCart({ product: { id: product.id }, quantity: 1 }).subscribe();
  }

  login(): void {
    this.loginService.login();
  }
}
