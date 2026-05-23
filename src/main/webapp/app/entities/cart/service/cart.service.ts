import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { ICart, NewCart } from '../cart.model';

export type PartialUpdateCart = Partial<ICart> & Pick<ICart, 'id'>;

export type EntityResponseType = HttpResponse<ICart>;
export type EntityArrayResponseType = HttpResponse<ICart[]>;

@Injectable({ providedIn: 'root' })
export class CartService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/carts');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/carts/_search');

  create(cart: NewCart): Observable<EntityResponseType> {
    return this.http.post<ICart>(this.resourceUrl, cart, { observe: 'response' });
  }

  update(cart: ICart): Observable<EntityResponseType> {
    return this.http.put<ICart>(`${this.resourceUrl}/${this.getCartIdentifier(cart)}`, cart, { observe: 'response' });
  }

  partialUpdate(cart: PartialUpdateCart): Observable<EntityResponseType> {
    return this.http.patch<ICart>(`${this.resourceUrl}/${this.getCartIdentifier(cart)}`, cart, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ICart>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICart[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ICart[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<ICart[]>()], asapScheduler)));
  }

  getCartIdentifier(cart: Pick<ICart, 'id'>): number {
    return cart.id;
  }

  compareCart(o1: Pick<ICart, 'id'> | null, o2: Pick<ICart, 'id'> | null): boolean {
    return o1 && o2 ? this.getCartIdentifier(o1) === this.getCartIdentifier(o2) : o1 === o2;
  }

  addCartToCollectionIfMissing<Type extends Pick<ICart, 'id'>>(
    cartCollection: Type[],
    ...cartsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const carts: Type[] = cartsToCheck.filter(isPresent);
    if (carts.length > 0) {
      const cartCollectionIdentifiers = cartCollection.map(cartItem => this.getCartIdentifier(cartItem));
      const cartsToAdd = carts.filter(cartItem => {
        const cartIdentifier = this.getCartIdentifier(cartItem);
        if (cartCollectionIdentifiers.includes(cartIdentifier)) {
          return false;
        }
        cartCollectionIdentifiers.push(cartIdentifier);
        return true;
      });
      return [...cartsToAdd, ...cartCollection];
    }
    return cartCollection;
  }
}
