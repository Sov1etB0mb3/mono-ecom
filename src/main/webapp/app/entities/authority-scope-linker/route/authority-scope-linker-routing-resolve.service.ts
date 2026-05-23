import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IAuthorityScopeLinker } from '../authority-scope-linker.model';
import { AuthorityScopeLinkerService } from '../service/authority-scope-linker.service';

const authorityScopeLinkerResolve = (route: ActivatedRouteSnapshot): Observable<null | IAuthorityScopeLinker> => {
  const id = route.params.id;
  if (id) {
    return inject(AuthorityScopeLinkerService)
      .find(id)
      .pipe(
        mergeMap((authorityScopeLinker: HttpResponse<IAuthorityScopeLinker>) => {
          if (authorityScopeLinker.body) {
            return of(authorityScopeLinker.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default authorityScopeLinkerResolve;
