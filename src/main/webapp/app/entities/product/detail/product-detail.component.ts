import { Component, inject, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { AccountService } from 'app/core/auth/account.service';
import { IProduct } from '../product.model';

@Component({
  selector: 'jhi-product-detail',
  templateUrl: './product-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class ProductDetailComponent {
  product = input<IProduct | null>(null);

  protected readonly accountService = inject(AccountService);

  previousState(): void {
    window.history.back();
  }
}
