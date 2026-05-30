import { Component, OnInit, TemplateRef, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpHeaders } from '@angular/common/http';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ItemCountComponent } from 'app/shared/pagination';
import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { EntityArrayResponseType, ProductService } from 'app/entities/product/service/product.service';
import { CartService } from 'app/cart/cart.service';
import { IProduct } from 'app/entities/product/product.model';
import { ITEMS_PER_PAGE, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { catchError, finalize, Observable, of, tap } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { SortService, SortState, sortStateSignal } from '../shared/sort';
import { DEFAULT_SORT_DATA } from '../config/navigation.constants';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [SharedModule, RouterModule, ItemCountComponent, FormsModule],
})
export default class HomeComponent implements OnInit {
  private static readonly NOT_SORTABLE_FIELDS_AFTER_SEARCH = ['name', 'createdBy', 'lastModifiedBy'];
  account = signal<Account | null>(null);
  products = signal<IProduct[]>([]);
  isLoading = signal(false);
  toastMessage = signal<string | null>(null);
  selectedProduct = signal<IProduct | null>(null);
  currentSearch = '';
  page = 1;
  totalItems = 0;
  readonly itemsPerPage = ITEMS_PER_PAGE;
  sortState = sortStateSignal({});

  readonly confirmModal = viewChild<TemplateRef<any>>('confirmModal');

  protected readonly sortService = inject(SortService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  private readonly accountService = inject(AccountService);
  private readonly loginService = inject(LoginService);
  private readonly productService = inject(ProductService);
  private readonly cartService = inject(CartService);
  private readonly modalService = inject(NgbModal);

  private toastTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    this.accountService.identity().subscribe(account => {
      this.account.set(account);
    });
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading.set(true);
    const queryObject = {
      page: this.page - 1,
      size: this.itemsPerPage,
      eagerload: true,
      query: this.currentSearch,
      sort: 'id,asc',
    };
    // this.productService
    //   .query(queryObject)
    //   .pipe(
    //     catchError(() => {
    //       this.isLoading.set(false);
    //       return of({ body: [] as IProduct[], headers: new HttpHeaders() });
    //     }),
    //     finalize(() => this.isLoading.set(false)),
    //   )
    //   .subscribe(res => {
    //     this.products.set(res.body ?? []);
    //     this.totalItems = Number(res.headers.get(TOTAL_COUNT_RESPONSE_HEADER));
    //   });
    this.queryBackend().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
  }

  getDefaultSortState(): SortState {
    return this.sortService.parseSortParam(this.activatedRoute.snapshot.data[DEFAULT_SORT_DATA]);
  }
  search(query: string): void {
    this.page = 1;
    this.currentSearch = query;
    const { predicate } = this.sortState();
    if (query && predicate && HomeComponent.NOT_SORTABLE_FIELDS_AFTER_SEARCH.includes(predicate)) {
      this.navigateToWithComponentValues(this.getDefaultSortState());
      return;
    }
    this.navigateToWithComponentValues(this.sortState());
  }
  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(this.page, event, this.currentSearch);
  }

  navigateToPage(page: number): void {
    this.page = page;
    this.loadProducts();
  }

  confirmAddToCart(product: IProduct): void {
    this.selectedProduct.set(product);
    this.modalService.open(this.confirmModal(), { centered: true }).result.then(
      () => this.addToCart(product),
      () => {},
    );
  }

  addToCart(product: IProduct): void {
    this.cartService.addItemToCart({ product: { id: product.id }, quantity: 1 }).subscribe(() => {
      this.showToast(`${product.name} added to cart`);
    });
  }
  login(): void {
    this.loginService.login();
  }
  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.products.set(dataFromBody);
  }
  protected fillComponentAttributesFromResponseBody(data: IProduct[] | null): IProduct[] {
    return data ?? [];
  }
  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems = Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER));
  }
  protected handleNavigation(page: number, sortState: SortState, currentSearch?: string): void {
    const queryParamsObj = {
      search: currentSearch,
      page,
      size: this.itemsPerPage,
      sort: this.sortService.buildSortParam(sortState),
    };
    this.loadProducts();
  }
  protected queryBackend(): Observable<EntityArrayResponseType> {
    const { page, currentSearch } = this;

    this.isLoading.set(true);
    const pageToLoad: number = page;
    const queryObject: any = {
      page: pageToLoad - 1,
      size: this.itemsPerPage,
      eagerload: true,
      query: currentSearch,
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    if (this.currentSearch && this.currentSearch !== '') {
      return this.productService.search(queryObject).pipe(tap(() => this.isLoading.set(false)));
    }
    return this.productService.query(queryObject).pipe(tap(() => this.isLoading.set(false)));
  }
  private showToast(message: string): void {
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastMessage.set(message);
    this.toastTimer = setTimeout(() => this.toastMessage.set(null), 3000);
  }
}
