import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { IScope, NewScope } from '../scope.model';

export type PartialUpdateScope = Partial<IScope> & Pick<IScope, 'id'>;

export type EntityResponseType = HttpResponse<IScope>;
export type EntityArrayResponseType = HttpResponse<IScope[]>;

@Injectable({ providedIn: 'root' })
export class ScopeService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/scopes');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/scopes/_search');

  create(scope: NewScope): Observable<EntityResponseType> {
    return this.http.post<IScope>(this.resourceUrl, scope, { observe: 'response' });
  }

  update(scope: IScope): Observable<EntityResponseType> {
    return this.http.put<IScope>(`${this.resourceUrl}/${this.getScopeIdentifier(scope)}`, scope, { observe: 'response' });
  }

  partialUpdate(scope: PartialUpdateScope): Observable<EntityResponseType> {
    return this.http.patch<IScope>(`${this.resourceUrl}/${this.getScopeIdentifier(scope)}`, scope, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IScope>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IScope[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IScope[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<IScope[]>()], asapScheduler)));
  }

  getScopeIdentifier(scope: Pick<IScope, 'id'>): number {
    return scope.id;
  }

  compareScope(o1: Pick<IScope, 'id'> | null, o2: Pick<IScope, 'id'> | null): boolean {
    return o1 && o2 ? this.getScopeIdentifier(o1) === this.getScopeIdentifier(o2) : o1 === o2;
  }

  addScopeToCollectionIfMissing<Type extends Pick<IScope, 'id'>>(
    scopeCollection: Type[],
    ...scopesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const scopes: Type[] = scopesToCheck.filter(isPresent);
    if (scopes.length > 0) {
      const scopeCollectionIdentifiers = scopeCollection.map(scopeItem => this.getScopeIdentifier(scopeItem));
      const scopesToAdd = scopes.filter(scopeItem => {
        const scopeIdentifier = this.getScopeIdentifier(scopeItem);
        if (scopeCollectionIdentifiers.includes(scopeIdentifier)) {
          return false;
        }
        scopeCollectionIdentifiers.push(scopeIdentifier);
        return true;
      });
      return [...scopesToAdd, ...scopeCollection];
    }
    return scopeCollection;
  }
}
