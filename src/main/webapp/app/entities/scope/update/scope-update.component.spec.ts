import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { ScopeService } from '../service/scope.service';
import { IScope } from '../scope.model';
import { ScopeFormService } from './scope-form.service';

import { ScopeUpdateComponent } from './scope-update.component';

describe('Scope Management Update Component', () => {
  let comp: ScopeUpdateComponent;
  let fixture: ComponentFixture<ScopeUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let scopeFormService: ScopeFormService;
  let scopeService: ScopeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ScopeUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(ScopeUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ScopeUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    scopeFormService = TestBed.inject(ScopeFormService);
    scopeService = TestBed.inject(ScopeService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const scope: IScope = { id: 1074 };

      activatedRoute.data = of({ scope });
      comp.ngOnInit();

      expect(comp.scope).toEqual(scope);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IScope>>();
      const scope = { id: 6081 };
      jest.spyOn(scopeFormService, 'getScope').mockReturnValue(scope);
      jest.spyOn(scopeService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ scope });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: scope }));
      saveSubject.complete();

      // THEN
      expect(scopeFormService.getScope).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(scopeService.update).toHaveBeenCalledWith(expect.objectContaining(scope));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IScope>>();
      const scope = { id: 6081 };
      jest.spyOn(scopeFormService, 'getScope').mockReturnValue({ id: null });
      jest.spyOn(scopeService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ scope: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: scope }));
      saveSubject.complete();

      // THEN
      expect(scopeFormService.getScope).toHaveBeenCalled();
      expect(scopeService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IScope>>();
      const scope = { id: 6081 };
      jest.spyOn(scopeService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ scope });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(scopeService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
