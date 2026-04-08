import { IAuthority } from 'app/entities/admin/authority/authority.model';
import { IScope } from 'app/entities/scope/scope.model';

export interface IAuthorityScopeLinker {
  id: number;
  authority?: IAuthority | null;
  scope?: IScope | null;
}

export type NewAuthorityScopeLinker = Omit<IAuthorityScopeLinker, 'id'> & { id: null };
