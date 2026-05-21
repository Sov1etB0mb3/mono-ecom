import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpHeaders } from '@angular/common/http';

import SharedModule from 'app/shared/shared.module';
import { ItemCountComponent } from 'app/shared/pagination';
import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { ProductService } from 'app/entities/product/service/product.service';
import { CartService } from 'app/cart/cart.service';
import { IProduct } from 'app/entities/product/product.model';
import { ITEMS_PER_PAGE, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { finalize } from 'rxjs';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [SharedModule, RouterModule, ItemCountComponent],
})
export default class HomeComponent implements OnInit {
  account = signal<Account | null>(null);
  products = signal<IProduct[]>([]);
  isLoading = signal(false);
  page = 1;
  totalItems = 0;
  readonly itemsPerPage = ITEMS_PER_PAGE;

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
    const queryObject = {
      page: this.page - 1,
      size: this.itemsPerPage,
      eagerload: true,
      sort: 'id,asc',
    };
    this.productService
      .query(queryObject)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe(res => {
        this.products.set(res.body ?? []);
        this.totalItems = Number(res.headers.get(TOTAL_COUNT_RESPONSE_HEADER));
      });
  }

  navigateToPage(page: number): void {
    this.page = page;
    this.loadProducts();
  }

  addToCart(product: IProduct): void {
    this.cartService.addItemToCart({ product: { id: product.id }, quantity: 1 }).subscribe();
  }

  login(): void {
    this.loginService.login();
  }
}
