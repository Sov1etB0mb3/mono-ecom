export interface IScope {
  id: number;
  name?: string | null;
  description?: string | null;
}

export type NewScope = Omit<IScope, 'id'> & { id: null };
