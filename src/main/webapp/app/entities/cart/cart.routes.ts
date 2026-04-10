import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import CartResolve from './route/cart-routing-resolve.service';

const cartRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/cart.component').then(m => m.CartComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/cart-detail.component').then(m => m.CartDetailComponent),
    resolve: {
      cart: CartResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/cart-update.component').then(m => m.CartUpdateComponent),
    resolve: {
      cart: CartResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/cart-update.component').then(m => m.CartUpdateComponent),
    resolve: {
      cart: CartResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default cartRoute;
