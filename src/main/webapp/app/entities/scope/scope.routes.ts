import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import ScopeResolve from './route/scope-routing-resolve.service';

const scopeRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/scope.component').then(m => m.ScopeComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/scope-detail.component').then(m => m.ScopeDetailComponent),
    resolve: {
      scope: ScopeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/scope-update.component').then(m => m.ScopeUpdateComponent),
    resolve: {
      scope: ScopeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/scope-update.component').then(m => m.ScopeUpdateComponent),
    resolve: {
      scope: ScopeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default scopeRoute;
