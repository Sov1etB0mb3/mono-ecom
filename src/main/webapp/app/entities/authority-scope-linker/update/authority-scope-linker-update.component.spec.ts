import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IAuthority } from 'app/entities/admin/authority/authority.model';
import { AuthorityService } from 'app/entities/admin/authority/service/authority.service';
import { IScope } from 'app/entities/scope/scope.model';
import { ScopeService } from 'app/entities/scope/service/scope.service';
import { IAuthorityScopeLinker } from '../authority-scope-linker.model';
import { AuthorityScopeLinkerService } from '../service/authority-scope-linker.service';
import { AuthorityScopeLinkerFormService } from './authority-scope-linker-form.service';

import { AuthorityScopeLinkerUpdateComponent } from './authority-scope-linker-update.component';

describe('AuthorityScopeLinker Management Update Component', () => {
  let comp: AuthorityScopeLinkerUpdateComponent;
  let fixture: ComponentFixture<AuthorityScopeLinkerUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let authorityScopeLinkerFormService: AuthorityScopeLinkerFormService;
  let authorityScopeLinkerService: AuthorityScopeLinkerService;
  let authorityService: AuthorityService;
  let scopeService: ScopeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AuthorityScopeLinkerUpdateComponent],
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
      .overrideTemplate(AuthorityScopeLinkerUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(AuthorityScopeLinkerUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    authorityScopeLinkerFormService = TestBed.inject(AuthorityScopeLinkerFormService);
    authorityScopeLinkerService = TestBed.inject(AuthorityScopeLinkerService);
    authorityService = TestBed.inject(AuthorityService);
    scopeService = TestBed.inject(ScopeService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Authority query and add missing value', () => {
      const authorityScopeLinker: IAuthorityScopeLinker = { id: 4144 };
      const authority: IAuthority = { name: '572a7ecc-bf76-43f4-8026-46b42fba586d' };
      authorityScopeLinker.authority = authority;

      const authorityCollection: IAuthority[] = [{ name: '572a7ecc-bf76-43f4-8026-46b42fba586d' }];
      jest.spyOn(authorityService, 'query').mockReturnValue(of(new HttpResponse({ body: authorityCollection })));
      const additionalAuthorities = [authority];
      const expectedCollection: IAuthority[] = [...additionalAuthorities, ...authorityCollection];
      jest.spyOn(authorityService, 'addAuthorityToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ authorityScopeLinker });
      comp.ngOnInit();

      expect(authorityService.query).toHaveBeenCalled();
      expect(authorityService.addAuthorityToCollectionIfMissing).toHaveBeenCalledWith(
        authorityCollection,
        ...additionalAuthorities.map(expect.objectContaining),
      );
      expect(comp.authoritiesSharedCollection).toEqual(expectedCollection);
    });

    it('should call Scope query and add missing value', () => {
      const authorityScopeLinker: IAuthorityScopeLinker = { id: 4144 };
      const scope: IScope = { id: 6081 };
      authorityScopeLinker.scope = scope;

      const scopeCollection: IScope[] = [{ id: 6081 }];
      jest.spyOn(scopeService, 'query').mockReturnValue(of(new HttpResponse({ body: scopeCollection })));
      const additionalScopes = [scope];
      const expectedCollection: IScope[] = [...additionalScopes, ...scopeCollection];
      jest.spyOn(scopeService, 'addScopeToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ authorityScopeLinker });
      comp.ngOnInit();

      expect(scopeService.query).toHaveBeenCalled();
      expect(scopeService.addScopeToCollectionIfMissing).toHaveBeenCalledWith(
        scopeCollection,
        ...additionalScopes.map(expect.objectContaining),
      );
      expect(comp.scopesSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const authorityScopeLinker: IAuthorityScopeLinker = { id: 4144 };
      const authority: IAuthority = { name: '572a7ecc-bf76-43f4-8026-46b42fba586d' };
      authorityScopeLinker.authority = authority;
      const scope: IScope = { id: 6081 };
      authorityScopeLinker.scope = scope;

      activatedRoute.data = of({ authorityScopeLinker });
      comp.ngOnInit();

      expect(comp.authoritiesSharedCollection).toContainEqual(authority);
      expect(comp.scopesSharedCollection).toContainEqual(scope);
      expect(comp.authorityScopeLinker).toEqual(authorityScopeLinker);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAuthorityScopeLinker>>();
      const authorityScopeLinker = { id: 7201 };
      jest.spyOn(authorityScopeLinkerFormService, 'getAuthorityScopeLinker').mockReturnValue(authorityScopeLinker);
      jest.spyOn(authorityScopeLinkerService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ authorityScopeLinker });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: authorityScopeLinker }));
      saveSubject.complete();

      // THEN
      expect(authorityScopeLinkerFormService.getAuthorityScopeLinker).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(authorityScopeLinkerService.update).toHaveBeenCalledWith(expect.objectContaining(authorityScopeLinker));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAuthorityScopeLinker>>();
      const authorityScopeLinker = { id: 7201 };
      jest.spyOn(authorityScopeLinkerFormService, 'getAuthorityScopeLinker').mockReturnValue({ id: null });
      jest.spyOn(authorityScopeLinkerService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ authorityScopeLinker: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: authorityScopeLinker }));
      saveSubject.complete();

      // THEN
      expect(authorityScopeLinkerFormService.getAuthorityScopeLinker).toHaveBeenCalled();
      expect(authorityScopeLinkerService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAuthorityScopeLinker>>();
      const authorityScopeLinker = { id: 7201 };
      jest.spyOn(authorityScopeLinkerService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ authorityScopeLinker });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(authorityScopeLinkerService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareAuthority', () => {
      it('should forward to authorityService', () => {
        const entity = { name: '572a7ecc-bf76-43f4-8026-46b42fba586d' };
        const entity2 = { name: 'c56c1cf7-aca8-48fe-ad81-eeebbf872cb1' };
        jest.spyOn(authorityService, 'compareAuthority');
        comp.compareAuthority(entity, entity2);
        expect(authorityService.compareAuthority).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareScope', () => {
      it('should forward to scopeService', () => {
        const entity = { id: 6081 };
        const entity2 = { id: 1074 };
        jest.spyOn(scopeService, 'compareScope');
        comp.compareScope(entity, entity2);
        expect(scopeService.compareScope).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
