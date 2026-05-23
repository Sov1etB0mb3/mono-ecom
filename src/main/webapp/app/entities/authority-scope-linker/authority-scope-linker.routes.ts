import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import AuthorityScopeLinkerResolve from './route/authority-scope-linker-routing-resolve.service';

const authorityScopeLinkerRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/authority-scope-linker.component').then(m => m.AuthorityScopeLinkerComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/authority-scope-linker-detail.component').then(m => m.AuthorityScopeLinkerDetailComponent),
    resolve: {
      authorityScopeLinker: AuthorityScopeLinkerResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/authority-scope-linker-update.component').then(m => m.AuthorityScopeLinkerUpdateComponent),
    resolve: {
      authorityScopeLinker: AuthorityScopeLinkerResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/authority-scope-linker-update.component').then(m => m.AuthorityScopeLinkerUpdateComponent),
    resolve: {
      authorityScopeLinker: AuthorityScopeLinkerResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default authorityScopeLinkerRoute;
