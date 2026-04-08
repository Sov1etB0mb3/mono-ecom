import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IAuthorityScopeLinker, NewAuthorityScopeLinker } from '../authority-scope-linker.model';

export type PartialUpdateAuthorityScopeLinker = Partial<IAuthorityScopeLinker> & Pick<IAuthorityScopeLinker, 'id'>;

export type EntityResponseType = HttpResponse<IAuthorityScopeLinker>;
export type EntityArrayResponseType = HttpResponse<IAuthorityScopeLinker[]>;

@Injectable({ providedIn: 'root' })
export class AuthorityScopeLinkerService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/authority-scope-linkers');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/authority-scope-linkers/_search');

  create(authorityScopeLinker: NewAuthorityScopeLinker): Observable<EntityResponseType> {
    return this.http.post<IAuthorityScopeLinker>(this.resourceUrl, authorityScopeLinker, { observe: 'response' });
  }

  update(authorityScopeLinker: IAuthorityScopeLinker): Observable<EntityResponseType> {
    return this.http.put<IAuthorityScopeLinker>(
      `${this.resourceUrl}/${this.getAuthorityScopeLinkerIdentifier(authorityScopeLinker)}`,
      authorityScopeLinker,
      { observe: 'response' },
    );
  }

  partialUpdate(authorityScopeLinker: PartialUpdateAuthorityScopeLinker): Observable<EntityResponseType> {
    return this.http.patch<IAuthorityScopeLinker>(
      `${this.resourceUrl}/${this.getAuthorityScopeLinkerIdentifier(authorityScopeLinker)}`,
      authorityScopeLinker,
      { observe: 'response' },
    );
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IAuthorityScopeLinker>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IAuthorityScopeLinker[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IAuthorityScopeLinker[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<IAuthorityScopeLinker[]>()], asapScheduler)));
  }

  getAuthorityScopeLinkerIdentifier(authorityScopeLinker: Pick<IAuthorityScopeLinker, 'id'>): number {
    return authorityScopeLinker.id;
  }

  compareAuthorityScopeLinker(o1: Pick<IAuthorityScopeLinker, 'id'> | null, o2: Pick<IAuthorityScopeLinker, 'id'> | null): boolean {
    return o1 && o2 ? this.getAuthorityScopeLinkerIdentifier(o1) === this.getAuthorityScopeLinkerIdentifier(o2) : o1 === o2;
  }

  addAuthorityScopeLinkerToCollectionIfMissing<Type extends Pick<IAuthorityScopeLinker, 'id'>>(
    authorityScopeLinkerCollection: Type[],
    ...authorityScopeLinkersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const authorityScopeLinkers: Type[] = authorityScopeLinkersToCheck.filter(isPresent);
    if (authorityScopeLinkers.length > 0) {
      const authorityScopeLinkerCollectionIdentifiers = authorityScopeLinkerCollection.map(authorityScopeLinkerItem =>
        this.getAuthorityScopeLinkerIdentifier(authorityScopeLinkerItem),
      );
      const authorityScopeLinkersToAdd = authorityScopeLinkers.filter(authorityScopeLinkerItem => {
        const authorityScopeLinkerIdentifier = this.getAuthorityScopeLinkerIdentifier(authorityScopeLinkerItem);
        if (authorityScopeLinkerCollectionIdentifiers.includes(authorityScopeLinkerIdentifier)) {
          return false;
        }
        authorityScopeLinkerCollectionIdentifiers.push(authorityScopeLinkerIdentifier);
        return true;
      });
      return [...authorityScopeLinkersToAdd, ...authorityScopeLinkerCollection];
    }
    return authorityScopeLinkerCollection;
  }
}
