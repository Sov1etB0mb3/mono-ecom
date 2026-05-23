import { Routes } from '@angular/router';

const orderRoute: Routes = [
  {
    path: 'my-orders',
    loadComponent: () => import('./my-orders/my-orders.component').then(m => m.default),
    data: { pageTitle: 'monoEcomApp.order.myOrders.title' },
  },
  {
    path: 'payment/checkout',
    loadComponent: () => import('./checkout/checkout.component').then(m => m.CheckoutComponent),
    data: { pageTitle: 'monoEcomApp.checkout.title' },
  },
  {
    path: 'payment/success',
    loadComponent: () => import('./success/success.component').then(m => m.PaymentSuccessComponent),
    data: { pageTitle: 'monoEcomApp.payment.success.title' },
  },
  {
    path: 'payment/cancel',
    loadComponent: () => import('./cancel/cancel.component').then(m => m.PaymentCancelComponent),
    data: { pageTitle: 'monoEcomApp.payment.cancel.title' },
  },
];

export default orderRoute;
