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

describe('Scope e2e test', () => {
  const scopePageUrl = '/scope';
  const scopePageUrlPattern = new RegExp('/scope(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const scopeSample = { name: 'archive' };

  let scope;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/scopes+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/scopes').as('postEntityRequest');
    cy.intercept('DELETE', '/api/scopes/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (scope) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/scopes/${scope.id}`,
      }).then(() => {
        scope = undefined;
      });
    }
  });

  it('Scopes menu should load Scopes page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('scope');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Scope').should('exist');
    cy.url().should('match', scopePageUrlPattern);
  });

  describe('Scope page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(scopePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Scope page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/scope/new$'));
        cy.getEntityCreateUpdateHeading('Scope');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', scopePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/scopes',
          body: scopeSample,
        }).then(({ body }) => {
          scope = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/scopes+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/scopes?page=0&size=20>; rel="last",<http://localhost/api/scopes?page=0&size=20>; rel="first"',
              },
              body: [scope],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(scopePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Scope page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('scope');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', scopePageUrlPattern);
      });

      it('edit button click should load edit Scope page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Scope');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', scopePageUrlPattern);
      });

      it('edit button click should load edit Scope page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Scope');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', scopePageUrlPattern);
      });

      it('last delete button click should delete instance of Scope', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('scope').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', scopePageUrlPattern);

        scope = undefined;
      });
    });
  });

  describe('new Scope page', () => {
    beforeEach(() => {
      cy.visit(`${scopePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Scope');
    });

    it('should create an instance of Scope', () => {
      cy.get(`[data-cy="name"]`).type('knottily next');
      cy.get(`[data-cy="name"]`).should('have.value', 'knottily next');

      cy.get(`[data-cy="description"]`).type('technician however');
      cy.get(`[data-cy="description"]`).should('have.value', 'technician however');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        scope = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', scopePageUrlPattern);
    });
  });
});
