import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IProduct } from 'app/entities/product/product.model';
import { ProductService } from 'app/entities/product/service/product.service';
import { ICart } from 'app/entities/cart/cart.model';
import { CartService } from 'app/entities/cart/service/cart.service';
import { CartItemService } from '../service/cart-item.service';
import { ICartItem } from '../cart-item.model';
import { CartItemFormGroup, CartItemFormService } from './cart-item-form.service';

@Component({
  selector: 'jhi-cart-item-update',
  templateUrl: './cart-item-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class CartItemUpdateComponent implements OnInit {
  isSaving = false;
  cartItem: ICartItem | null = null;

  productsSharedCollection: IProduct[] = [];
  cartsSharedCollection: ICart[] = [];

  protected cartItemService = inject(CartItemService);
  protected cartItemFormService = inject(CartItemFormService);
  protected productService = inject(ProductService);
  protected cartService = inject(CartService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: CartItemFormGroup = this.cartItemFormService.createCartItemFormGroup();

  compareProduct = (o1: IProduct | null, o2: IProduct | null): boolean => this.productService.compareProduct(o1, o2);

  compareCart = (o1: ICart | null, o2: ICart | null): boolean => this.cartService.compareCart(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ cartItem }) => {
      this.cartItem = cartItem;
      if (cartItem) {
        this.updateForm(cartItem);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const cartItem = this.cartItemFormService.getCartItem(this.editForm);
    if (cartItem.id !== null) {
      this.subscribeToSaveResponse(this.cartItemService.update(cartItem));
    } else {
      this.subscribeToSaveResponse(this.cartItemService.create(cartItem));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICartItem>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(cartItem: ICartItem): void {
    this.cartItem = cartItem;
    this.cartItemFormService.resetForm(this.editForm, cartItem);

    this.productsSharedCollection = this.productService.addProductToCollectionIfMissing<IProduct>(
      this.productsSharedCollection,
      cartItem.product,
    );
    this.cartsSharedCollection = this.cartService.addCartToCollectionIfMissing<ICart>(this.cartsSharedCollection, cartItem.cart);
  }

  protected loadRelationshipsOptions(): void {
    this.productService
      .query()
      .pipe(map((res: HttpResponse<IProduct[]>) => res.body ?? []))
      .pipe(map((products: IProduct[]) => this.productService.addProductToCollectionIfMissing<IProduct>(products, this.cartItem?.product)))
      .subscribe((products: IProduct[]) => (this.productsSharedCollection = products));

    this.cartService
      .query()
      .pipe(map((res: HttpResponse<ICart[]>) => res.body ?? []))
      .pipe(map((carts: ICart[]) => this.cartService.addCartToCollectionIfMissing<ICart>(carts, this.cartItem?.cart)))
      .subscribe((carts: ICart[]) => (this.cartsSharedCollection = carts));
  }
}
