import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('AuthorityScopeLinker e2e test', () => {
  const authorityScopeLinkerPageUrl = '/authority-scope-linker';
  const authorityScopeLinkerPageUrlPattern = new RegExp('/authority-scope-linker(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const authorityScopeLinkerSample = {};

  let authorityScopeLinker;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/authority-scope-linkers+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/authority-scope-linkers').as('postEntityRequest');
    cy.intercept('DELETE', '/api/authority-scope-linkers/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (authorityScopeLinker) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/authority-scope-linkers/${authorityScopeLinker.id}`,
      }).then(() => {
        authorityScopeLinker = undefined;
      });
    }
  });

  it('AuthorityScopeLinkers menu should load AuthorityScopeLinkers page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('authority-scope-linker');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('AuthorityScopeLinker').should('exist');
    cy.url().should('match', authorityScopeLinkerPageUrlPattern);
  });

  describe('AuthorityScopeLinker page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(authorityScopeLinkerPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create AuthorityScopeLinker page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/authority-scope-linker/new$'));
        cy.getEntityCreateUpdateHeading('AuthorityScopeLinker');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', authorityScopeLinkerPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/authority-scope-linkers',
          body: authorityScopeLinkerSample,
        }).then(({ body }) => {
          authorityScopeLinker = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/authority-scope-linkers+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [authorityScopeLinker],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(authorityScopeLinkerPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details AuthorityScopeLinker page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('authorityScopeLinker');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', authorityScopeLinkerPageUrlPattern);
      });

      it('edit button click should load edit AuthorityScopeLinker page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('AuthorityScopeLinker');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', authorityScopeLinkerPageUrlPattern);
      });

      it('edit button click should load edit AuthorityScopeLinker page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('AuthorityScopeLinker');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', authorityScopeLinkerPageUrlPattern);
      });

      it('last delete button click should delete instance of AuthorityScopeLinker', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('authorityScopeLinker').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', authorityScopeLinkerPageUrlPattern);

        authorityScopeLinker = undefined;
      });
    });
  });

  describe('new AuthorityScopeLinker page', () => {
    beforeEach(() => {
      cy.visit(`${authorityScopeLinkerPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('AuthorityScopeLinker');
    });

    it('should create an instance of AuthorityScopeLinker', () => {
      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        authorityScopeLinker = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', authorityScopeLinkerPageUrlPattern);
    });
  });
});
