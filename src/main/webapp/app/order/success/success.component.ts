import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';

@Component({
  selector: 'jhi-payment-success',
  templateUrl: './success.component.html',
  imports: [RouterModule, SharedModule],
})
export class PaymentSuccessComponent implements OnInit {
  sessionId = signal<string | null>(null);

  private readonly route = inject(ActivatedRoute);

  ngOnInit(): void {
    this.sessionId.set(this.route.snapshot.queryParamMap.get('session_id'));
  }
}
