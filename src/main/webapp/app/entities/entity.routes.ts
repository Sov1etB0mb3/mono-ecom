import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'monoEcomApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'category',
    data: { pageTitle: 'monoEcomApp.category.home.title' },
    loadChildren: () => import('./category/category.routes'),
  },
  {
    path: 'product',
    data: { pageTitle: 'monoEcomApp.product.home.title' },
    loadChildren: () => import('./product/product.routes'),
  },
  {
    path: 'scope',
    data: { pageTitle: 'monoEcomApp.scope.home.title' },
    loadChildren: () => import('./scope/scope.routes'),
  },
  {
    path: 'authority-scope-linker',
    data: { pageTitle: 'monoEcomApp.authorityScopeLinker.home.title' },
    loadChildren: () => import('./authority-scope-linker/authority-scope-linker.routes'),
  },
  {
    path: 'order',
    data: { pageTitle: 'monoEcomApp.order.home.title' },
    loadChildren: () => import('./order/order.routes'),
  },
  {
    path: 'order-item',
    data: { pageTitle: 'monoEcomApp.orderItem.home.title' },
    loadChildren: () => import('./order-item/order-item.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
