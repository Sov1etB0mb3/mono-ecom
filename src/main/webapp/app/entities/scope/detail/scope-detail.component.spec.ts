import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { ScopeDetailComponent } from './scope-detail.component';

describe('Scope Management Detail Component', () => {
  let comp: ScopeDetailComponent;
  let fixture: ComponentFixture<ScopeDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScopeDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./scope-detail.component').then(m => m.ScopeDetailComponent),
              resolve: { scope: () => of({ id: 6081 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(ScopeDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScopeDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load scope on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', ScopeDetailComponent);

      // THEN
      expect(instance.scope()).toEqual(expect.objectContaining({ id: 6081 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
