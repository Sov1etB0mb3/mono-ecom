import { Routes } from '@angular/router';

const cartRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./cart.component').then(m => m.CartComponent),
    data: {
      pageTitle: 'monoEcomApp.cartPage.title',
    },
  },
];

export default cartRoute;
