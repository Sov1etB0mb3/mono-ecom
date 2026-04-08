import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { AuthorityScopeLinkerDetailComponent } from './authority-scope-linker-detail.component';

describe('AuthorityScopeLinker Management Detail Component', () => {
  let comp: AuthorityScopeLinkerDetailComponent;
  let fixture: ComponentFixture<AuthorityScopeLinkerDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthorityScopeLinkerDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./authority-scope-linker-detail.component').then(m => m.AuthorityScopeLinkerDetailComponent),
              resolve: { authorityScopeLinker: () => of({ id: 7201 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(AuthorityScopeLinkerDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthorityScopeLinkerDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load authorityScopeLinker on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', AuthorityScopeLinkerDetailComponent);

      // THEN
      expect(instance.authorityScopeLinker()).toEqual(expect.objectContaining({ id: 7201 }));
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
